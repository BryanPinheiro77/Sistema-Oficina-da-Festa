package com.oficinadafesta.pedido.service;

import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.cliente.repository.ClienteRepository;
import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import com.oficinadafesta.enums.AreaTipo;
import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.StatusItemPedido;
import com.oficinadafesta.enums.StatusPedido;
import com.oficinadafesta.enums.TipoEntrega;
import com.oficinadafesta.pedido.domain.ItemPedido;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.dto.*;
import com.oficinadafesta.pedido.repository.ItemPedidoRepository;
import com.oficinadafesta.pedido.repository.PedidoRepository;
import com.oficinadafesta.produto.domain.Produto;
import com.oficinadafesta.produto.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ComandaRepository comandaRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         ClienteRepository clienteRepository,
                         ItemPedidoRepository itemPedidoRepository,
                         ComandaRepository comandaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.comandaRepository = comandaRepository;
    }

    // =========================================================
    // Pedido "normal" (cliente)
    // =========================================================

    public Pedido criarPedido(PedidoRequestDTO dto) {
        log.info("Criando pedido: clienteId={}, tipoEntrega={}, formaPagamento={}, distanciaKm={}",
                dto.getClienteId(), dto.getTipoEntrega(), dto.getFormaPagamento(), dto.getDistanciaEntregaKm());

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.getClienteId()));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setParaEntrega(dto.isParaEntrega());
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());

        BigDecimal taxaEntrega = calcularTaxaEntregaPorDistancia(dto.getDistanciaEntregaKm());
        pedido.setTaxaEntrega(taxaEntrega);

        List<ItemPedido> itens = dto.getItens().stream().map(itemDTO -> {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProdutoId()));

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPedido(pedido);
            // item.setStatus(StatusItemPedido.PENDENTE); // default já é pendente no domain
            return item;
        }).toList();

        pedido.setItens(itens);

        BigDecimal total = pedido.getTotal();
        BigDecimal valorPago = calcularValorPagoInicial(dto, total);

        pedido.setValorPago(valorPago);
        pedido.setPagamentoConfirmado(valorPago.compareTo(total) >= 0);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Pedido criado: pedidoId={}, total={}, valorPago={}, confirmado={}, itens={}",
                salvo.getId(), total, valorPago, salvo.isPagamentoConfirmado(),
                salvo.getItens() != null ? salvo.getItens().size() : 0);

        return salvo;
    }

    private BigDecimal calcularValorPagoInicial(PedidoRequestDTO dto, BigDecimal total) {
        if (dto.getTipoEntrega() == TipoEntrega.RETIRADA) {
            return switch (dto.getFormaPagamento()) {
                case PIX -> total.multiply(BigDecimal.valueOf(0.5));
                case CARTAO -> BigDecimal.ZERO;
                default -> total;
            };
        }
        // entrega → pagamento total
        return total;
    }

    private BigDecimal calcularTaxaEntregaPorDistancia(double distanciaKm) {
        if (distanciaKm <= 1.0) return BigDecimal.valueOf(5.00);
        if (distanciaKm <= 2.0) return BigDecimal.valueOf(7.50);
        if (distanciaKm <= 3.0) return BigDecimal.valueOf(10.00);
        return BigDecimal.valueOf(10.00 + (distanciaKm - 3.0) * 2.50);
    }

    // =========================================================
    // Consultas gerais
    // =========================================================

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
    }

    public Map<AreaTipo, List<ItemPedido>> separarItensPorSetor(Pedido pedido) {
        return pedido.getItens().stream()
                .collect(Collectors.groupingBy(item -> item.getProduto().getCategoria().getSetor()));
    }

    public BigDecimal getTotalPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        BigDecimal totalItens = pedido.getItens().stream()
                .map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalItens.add(pedido.getTaxaEntrega());
    }

    // =========================================================
    // Alterações de status/pagamento
    // =========================================================

    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        StatusPedido anterior = pedido.getStatus();
        pedido.setStatus(novoStatus);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Status pedido atualizado: pedidoId={}, de={} para={}", pedidoId, anterior, novoStatus);
        return salvo;
    }

    public Pedido confirmarPagamento(Long pedidoId, BigDecimal valorPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        BigDecimal total = pedido.getTotal();

        pedido.setValorPago(valorPago);
        pedido.setPagamentoConfirmado(valorPago.compareTo(total) >= 0);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Pagamento atualizado: pedidoId={}, valorPago={}, total={}, confirmado={}",
                pedidoId, valorPago, total, salvo.isPagamentoConfirmado());

        return salvo;
    }

    public void atualizarStatusItem(Long idItem, StatusItemPedido status) {
        ItemPedido item = itemPedidoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        StatusItemPedido anterior = item.getStatus();
        item.setStatus(status);
        itemPedidoRepository.save(item);

        log.info("Status item atualizado: itemId={}, de={} para={}", idItem, anterior, status);
    }

    // =========================================================
    // Setor: fila por setor (DTO)
    // =========================================================

    public List<PedidoSetorResponseDTO> listarPedidosPorSetor(AreaTipo setor) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return pedidoRepository.findAll().stream()
                .filter(pedido -> pedido.getItens().stream()
                        .anyMatch(item -> item.getProduto().getCategoria().getSetor() == setor))
                .map(pedido -> {
                    List<PedidoSetorResponseDTO.ItemDTO> itensDoSetor = pedido.getItens().stream()
                            .filter(item -> item.getProduto().getCategoria().getSetor() == setor)
                            .map(item -> new PedidoSetorResponseDTO.ItemDTO(
                                    item.getProduto().getNome(),
                                    item.getQuantidade()
                            ))
                            .toList();

                    return new PedidoSetorResponseDTO(
                            pedido.getId(),
                            pedido.getCliente() != null ? pedido.getCliente().getNome() : "SEM CLIENTE",
                            pedido.getStatus().name(),
                            pedido.getHorarioEntrega() != null ? pedido.getHorarioEntrega().format(formatter) : "",
                            itensDoSetor
                    );
                })
                .toList();
    }

    // =========================================================
    // Caixa: pedido vinculado a comanda (consumo local)
    // =========================================================

    public void criarPedidoNoCaixa(PedidoCaixaDTO dto) {
        log.info("Criando pedido no caixa: comanda={}, formaPagamento={}, valorPago={}, itens={}",
                dto.getCodigoComanda(), dto.getFormaPagamento(), dto.getValorPago(),
                dto.getItens() != null ? dto.getItens().size() : 0);

        Comanda comanda = comandaRepository.findByCodigo(dto.getCodigoComanda())
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.isAtiva()) {
            log.warn("Comanda não está ativa: codigo={}", dto.getCodigoComanda());
            throw new RuntimeException("Comanda não está ativa");
        }

        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setFormaPagamento(FormaPagamento.valueOf(dto.getFormaPagamento()));
        pedido.setParaEntrega(false);
        pedido.setEnderecoEntrega("PEDIDO NO CAIXA");

        // TODO: se Pedido.cliente for NOT NULL no banco, isso vai quebrar
        pedido.setCliente(null);

        BigDecimal totalPedido = BigDecimal.ZERO;
        List<ItemPedido> itens = new ArrayList<>();

        for (PedidoItemRequestDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setStatus(StatusItemPedido.PENDENTE);

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
            totalPedido = totalPedido.add(subtotal);
            itens.add(item);
        }

        pedido.setItens(itens);
        pedido.setValorPago(dto.getValorPago());
        pedido.setPagamentoConfirmado(dto.getValorPago().compareTo(totalPedido) >= 0);
        pedido.setComanda(comanda);

        Pedido salvo = pedidoRepository.save(pedido);

        // garante lista não nula
        if (comanda.getPedidos() == null) {
            comanda.setPedidos(new ArrayList<>());
        }
        comanda.getPedidos().add(salvo);
        comandaRepository.save(comanda);

        log.info("Pedido no caixa criado: pedidoId={}, comanda={}, totalPedido={}, valorPago={}, confirmado={}",
                salvo.getId(), dto.getCodigoComanda(), totalPedido, dto.getValorPago(), salvo.isPagamentoConfirmado());
    }

    // =========================================================
    // Café: adiciona pedido na comanda (NA_COMANDA)
    // =========================================================

    public PedidoResumoDTO adicionarPedidoCafe(String codigoComanda, AdicionarPedidoCafeDTO dto) {
        log.info("Adicionando pedido café na comanda: comanda={}, itens={}",
                codigoComanda, dto.getItens() != null ? dto.getItens().size() : 0);

        Comanda comanda = comandaRepository.findByCodigo(codigoComanda)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        Pedido pedido = new Pedido();
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setFormaPagamento(FormaPagamento.NA_COMANDA);
        pedido.setParaEntrega(false);
        pedido.setValorPago(BigDecimal.ZERO);
        pedido.setComanda(comanda);

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<ItemPedido> itens = new ArrayList<>();
        List<PedidoResumoDTO.ItemResumoDTO> itensResumo = new ArrayList<>();

        for (AdicionarPedidoCafeDTO.ItemPedidoDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setStatus(StatusItemPedido.PRONTO);
            item.setPedido(pedido);

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));
            valorTotal = valorTotal.add(subtotal);

            itens.add(item);

            PedidoResumoDTO.ItemResumoDTO resumoItem = new PedidoResumoDTO.ItemResumoDTO();
            resumoItem.setNomeProduto(produto.getNome());
            resumoItem.setQuantidade(itemDTO.getQuantidade());
            resumoItem.setPrecoUnitario(produto.getPreco());
            resumoItem.setSubtotal(subtotal);
            itensResumo.add(resumoItem);
        }

        pedido.setValorTotal(valorTotal);
        pedido.setItens(itens);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Pedido café criado: pedidoId={}, comanda={}, total={}, itens={}",
                salvo.getId(), codigoComanda, valorTotal, itens.size());

        PedidoResumoDTO resumo = new PedidoResumoDTO();
        resumo.setCodigoComanda(codigoComanda);
        resumo.setValorTotal(valorTotal);
        resumo.setItens(itensResumo);

        return resumo;
    }
}