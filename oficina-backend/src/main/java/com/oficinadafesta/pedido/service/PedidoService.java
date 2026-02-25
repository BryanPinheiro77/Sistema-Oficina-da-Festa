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
import com.oficinadafesta.shared.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    // 1) Pedido "normal" (cliente)
    // =========================================================

    public Pedido criarPedido(PedidoRequestDTO dto) {
        log.info("Criando pedido (cliente): clienteId={}, tipoEntrega={}, formaPagamento={}, distanciaKm={}",
                dto.getClienteId(), dto.getTipoEntrega(), dto.getFormaPagamento(), dto.getDistanciaEntregaKm());

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + dto.getClienteId()));

        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            log.warn("Tentativa de criar pedido sem itens: clienteId={}", dto.getClienteId());
            throw new IllegalArgumentException("Pedido precisa ter pelo menos 1 item");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);

        // fonte de verdade para entrega/retirada
        pedido.setTipoEntrega(dto.getTipoEntrega());
        pedido.setParaEntrega(dto.getTipoEntrega() == TipoEntrega.ENTREGA);

        // endereço só faz sentido se for ENTREGA
        pedido.setEnderecoEntrega(dto.getTipoEntrega() == TipoEntrega.ENTREGA ? dto.getEnderecoEntrega() : null);

        // taxa só faz sentido se for ENTREGA
        BigDecimal taxaEntrega = (dto.getTipoEntrega() == TipoEntrega.ENTREGA)
                ? calcularTaxaEntregaPorDistancia(dto.getDistanciaEntregaKm())
                : BigDecimal.ZERO;
        pedido.setTaxaEntrega(taxaEntrega);

        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCriadoEm(LocalDateTime.now());

        List<ItemPedido> itens = dto.getItens().stream().map(itemDTO -> {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPedido(pedido);
            return item;
        }).toList();

        pedido.setItens(itens);

        BigDecimal total = pedido.calcularTotal();
        BigDecimal valorPago = calcularValorPagoInicial(dto.getTipoEntrega(), dto.getFormaPagamento(), total);

        pedido.setValorPago(valorPago);
        pedido.setPagamentoConfirmado(valorPago.compareTo(total) >= 0);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Pedido criado: pedidoId={}, cliente={}, tipoEntrega={}, total={}, valorPago={}, confirmado={}, itens={}",
                salvo.getId(),
                cliente.getNome(),
                salvo.getTipoEntrega(),
                total,
                valorPago,
                salvo.isPagamentoConfirmado(),
                salvo.getItens() != null ? salvo.getItens().size() : 0
        );

        return salvo;
    }

    private BigDecimal calcularValorPagoInicial(TipoEntrega tipoEntrega, FormaPagamento formaPagamento, BigDecimal total) {
        // RETIRADA (agendada) e IMEDIATA seguem mesma regra do teu sistema
        if (tipoEntrega == TipoEntrega.RETIRADA || tipoEntrega == TipoEntrega.IMEDIATA) {
            return switch (formaPagamento) {
                case PIX -> total.multiply(BigDecimal.valueOf(0.5));
                case CARTAO -> BigDecimal.ZERO;
                default -> total;
            };
        }
        // ENTREGA → pagamento total (regra atual)
        return total;
    }

    private BigDecimal calcularTaxaEntregaPorDistancia(double distanciaKm) {
        if (distanciaKm <= 1.0) return BigDecimal.valueOf(5.00);
        if (distanciaKm <= 2.0) return BigDecimal.valueOf(7.50);
        if (distanciaKm <= 3.0) return BigDecimal.valueOf(10.00);
        return BigDecimal.valueOf(10.00 + (distanciaKm - 3.0) * 2.50);
    }

    // =========================================================
    // 2) Consultas gerais
    // =========================================================

    public List<Pedido> listarTodos() {
        log.debug("Listando todos os pedidos");
        List<Pedido> pedidos = pedidoRepository.findAll();
        log.debug("Total pedidos encontrados: {}", pedidos.size());
        return pedidos;
    }

    public List<Pedido> listarPorStatus(StatusPedido status) {
        log.debug("Listando pedidos por status={}", status);
        List<Pedido> pedidos = pedidoRepository.findByStatus(status);
        log.debug("Total pedidos encontrados para status={}: {}", status, pedidos.size());
        return pedidos;
    }

    public Pedido buscarPorId(Long id) {
        log.debug("Buscando pedido por id={}", id);
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
    }

    public Map<AreaTipo, List<ItemPedido>> separarItensPorSetor(Pedido pedido) {
        log.debug("Separando itens por setor: pedidoId={}", pedido.getId());
        return pedido.getItens().stream()
                .collect(Collectors.groupingBy(item -> item.getProduto().getCategoria().getSetor()));
    }

    public BigDecimal getTotalPedido(Long pedidoId) {
        log.debug("Calculando total do pedido: pedidoId={}", pedidoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + pedidoId));

        BigDecimal totalItens = pedido.getItens().stream()
                .map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = totalItens.add(pedido.getTaxaEntrega());

        log.debug("Total calculado: pedidoId={}, totalItens={}, taxaEntrega={}, total={}",
                pedidoId, totalItens, pedido.getTaxaEntrega(), total);

        return total;
    }

    // =========================================================
    // 3) EXPEDIÇÃO (RETIRADA/ENTREGA/IMEDIATA) - tela + impressão
    // =========================================================

    public List<PedidoExpedicaoDTO> listarPedidosExpedicao(TipoEntrega tipo, StatusPedido status) {
        log.info("Listando pedidos para expedição: tipo={}, status={}", tipo, status);

        List<PedidoExpedicaoDTO> lista = pedidoRepository.findAll().stream()
                .filter(p -> p.getTipoEntrega() == TipoEntrega.ENTREGA
                        || p.getTipoEntrega() == TipoEntrega.RETIRADA
                        || p.getTipoEntrega() == TipoEntrega.IMEDIATA)
                .filter(p -> tipo == null || p.getTipoEntrega() == tipo)
                .filter(p -> status == null || p.getStatus() == status)
                .map(p -> new PedidoExpedicaoDTO(
                        p.getId(),
                        p.getCliente() != null ? p.getCliente().getNome() : "SEM CLIENTE",
                        p.getCliente() != null ? p.getCliente().getTelefone() : null,
                        p.getTipoEntrega(),
                        p.getFormaPagamento(),
                        p.getStatus(),
                        p.getTipoEntrega() == TipoEntrega.ENTREGA ? p.getEnderecoEntrega() : null,
                        p.getHorarioEntrega(),
                        p.calcularTotal(),
                        p.getItens().stream()
                                .map(i -> new PedidoExpedicaoDTO.ItemDTO(
                                        i.getProduto().getNome(),
                                        i.getQuantidade()
                                ))
                                .toList()
                ))
                .toList();

        log.info("Pedidos para expedição retornados: total={}", lista.size());
        return lista;
    }

    // =========================================================
    // 4) Alterações de status/pagamento
    // =========================================================

    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
        log.info("Atualizando status do pedido: pedidoId={}, novoStatus={}", pedidoId, novoStatus);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + pedidoId));

        StatusPedido anterior = pedido.getStatus();
        pedido.setStatus(novoStatus);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Status atualizado: pedidoId={}, de={} para={}", pedidoId, anterior, novoStatus);
        return salvo;
    }

    public Pedido confirmarPagamento(Long pedidoId, BigDecimal valorPago) {
        log.info("Confirmando pagamento: pedidoId={}, valorPago={}", pedidoId, valorPago);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + pedidoId));

        BigDecimal total = pedido.calcularTotal();

        pedido.setValorPago(valorPago);
        pedido.setPagamentoConfirmado(valorPago.compareTo(total) >= 0);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info("Pagamento atualizado: pedidoId={}, valorPago={}, total={}, confirmado={}",
                pedidoId, valorPago, total, salvo.isPagamentoConfirmado());

        return salvo;
    }

    /**
     * Setor só atualiza item do próprio setor.
     * ADMIN pode tudo.
     */
    public void atualizarStatusItem(Long idItem, StatusItemPedido status) {
        log.info("Atualizando status do item: itemId={}, novoStatus={}", idItem, status);

        ItemPedido item = itemPedidoRepository.findById(idItem)
                .orElseThrow(() -> new EntityNotFoundException("ItemPedido não encontrado: " + idItem));

        Authentication auth = SecurityUtils.auth();

        if (!SecurityUtils.isAdmin(auth)) {
            AreaTipo setorDoUsuario = SecurityUtils.getSetor(auth);
            AreaTipo setorDoItem = item.getProduto().getCategoria().getSetor();

            if (setorDoUsuario == null) {
                log.warn("Acesso negado: user={} sem setor válido tentou atualizar itemId={}",
                        auth != null ? auth.getName() : "anon", idItem);
                throw new AccessDeniedException("Usuário sem setor válido");
            }

            if (setorDoItem == null) {
                log.warn("Acesso negado: itemId={} sem setor definido tentou ser atualizado por user={}",
                        idItem, auth != null ? auth.getName() : "anon");
                throw new AccessDeniedException("Item sem setor definido");
            }

            if (!setorDoItem.equals(setorDoUsuario)) {
                log.warn("Acesso negado: user={} (setor={}) tentou atualizar itemId={} (setorItem={})",
                        auth != null ? auth.getName() : "anon",
                        setorDoUsuario,
                        idItem,
                        setorDoItem);
                throw new AccessDeniedException("Você não pode alterar itens de outro setor");
            }
        }

        StatusItemPedido anterior = item.getStatus();
        item.setStatus(status);
        itemPedidoRepository.save(item);

        log.info("Status item atualizado: itemId={}, de={} para={}", idItem, anterior, status);
    }

    // =========================================================
    // 5) Setor: fila por setor (DTO)
    // =========================================================

    public List<PedidoSetorResponseDTO> listarPedidosPorSetor(AreaTipo setor) {
        log.debug("Listando fila por setor: setor={}", setor);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<PedidoSetorResponseDTO> lista = pedidoRepository.findAll().stream()
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

        log.debug("Fila por setor retornada: setor={}, total={}", setor, lista.size());
        return lista;
    }

    // =========================================================
    // 6) Caixa: pedido vinculado a comanda (consumo local)
    // =========================================================

    public void criarPedidoNoCaixa(PedidoCaixaDTO dto) {
        log.info("Criando pedido no caixa: comanda={}, formaPagamento={}, valorPago={}, itens={}",
                dto.getCodigoComanda(), dto.getFormaPagamento(), dto.getValorPago(),
                dto.getItens() != null ? dto.getItens().size() : 0);

        Comanda comanda = comandaRepository.findByCodigo(dto.getCodigoComanda())
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + dto.getCodigoComanda()));

        if (!comanda.isAtiva()) {
            log.warn("Comanda não está ativa: codigo={}", dto.getCodigoComanda());
            throw new IllegalStateException("Comanda não está ativa");
        }

        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setFormaPagamento(FormaPagamento.valueOf(dto.getFormaPagamento()));
        pedido.setParaEntrega(false);
        pedido.setEnderecoEntrega("PEDIDO NO CAIXA");
        pedido.setCliente(null);

        BigDecimal totalPedido = BigDecimal.ZERO;
        List<ItemPedido> itens = new ArrayList<>();

        for (PedidoItemRequestDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

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

        if (comanda.getPedidos() == null) {
            comanda.setPedidos(new ArrayList<>());
        }
        comanda.getPedidos().add(salvo);
        comandaRepository.save(comanda);

        log.info("Pedido no caixa criado: pedidoId={}, comanda={}, totalPedido={}, valorPago={}, confirmado={}",
                salvo.getId(), dto.getCodigoComanda(), totalPedido, dto.getValorPago(), salvo.isPagamentoConfirmado());
    }

    // =========================================================
    // 7) Café: adiciona pedido na comanda (NA_COMANDA)
    // =========================================================

    public PedidoResumoDTO adicionarPedidoCafe(String codigoComanda, AdicionarPedidoCafeDTO dto) {
        log.info("Adicionando pedido café na comanda: comanda={}, itens={}",
                codigoComanda, dto.getItens() != null ? dto.getItens().size() : 0);

        Comanda comanda = comandaRepository.findByCodigo(codigoComanda)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + codigoComanda));

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
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

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