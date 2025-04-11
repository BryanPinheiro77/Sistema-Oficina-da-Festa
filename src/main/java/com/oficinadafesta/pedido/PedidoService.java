package com.oficinadafesta.pedido;

import com.oficinadafesta.cliente.Cliente;
import com.oficinadafesta.cliente.ClienteRepository;
import com.oficinadafesta.comanda.Comanda;
import com.oficinadafesta.comanda.ComandaRepository;
import com.oficinadafesta.dto.*;
import com.oficinadafesta.enums.*;
import com.oficinadafesta.produto.Produto;
import com.oficinadafesta.produto.ProdutoRepository;
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

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ComandaRepository comandaRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, ClienteRepository clienteRepository, ItemPedidoRepository itemPedidoRepository, ComandaRepository comandaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.comandaRepository = comandaRepository;
    }

    public Pedido criarPedido(PedidoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.getClienteId()));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setParaEntrega(dto.isParaEntrega());
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());

        // Calcular taxa de entrega
        BigDecimal taxaEntrega = calcularTaxaEntregaPorDistancia(dto.getDistanciaEntregaKm());
        pedido.setTaxaEntrega(taxaEntrega);

        // Adicionar itens
        List<ItemPedido> itens = dto.getItens().stream().map(itemDTO -> {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProdutoId()));

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPedido(pedido);
            return item;
        }).toList();
        pedido.setItens(itens);

        BigDecimal total = pedido.getTotal();
        BigDecimal valorPago = BigDecimal.ZERO;

        if (dto.getTipoEntrega() == TipoEntrega.RETIRADA) {
            switch (dto.getFormaPagamento()) {
                case PIX -> valorPago = total.multiply(BigDecimal.valueOf(0.5));
                case CARTAO -> valorPago = BigDecimal.ZERO;
                default -> valorPago = total;
            }
        } else {
            valorPago = total; // entrega → pagamento total
        }

        pedido.setValorPago(valorPago);
        pedido.setPagamentoConfirmado(valorPago.compareTo(total) >= 0);

        return pedidoRepository.save(pedido);
    }



    private BigDecimal calcularTaxaEntregaPorDistancia(double distanciaKm) {
        if (distanciaKm <= 1.0) {
            return BigDecimal.valueOf(5.00);
        } else if (distanciaKm <= 2.0) {
            return BigDecimal.valueOf(7.50);
        } else if (distanciaKm <= 3.0) {
            return BigDecimal.valueOf(10.00);
        } else {
            // Exemplo: 10 reais + 2.50 por km extra além de 3km
            return BigDecimal.valueOf(10.00 + (distanciaKm - 3.0) * 2.50);
        }
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
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

    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    public Pedido confirmarPagamento(Long pedidoId, BigDecimal valorPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        pedido.setValorPago(BigDecimal.valueOf(valorPago.doubleValue()));
        pedido.setPagamentoConfirmado(true);
        return pedidoRepository.save(pedido);
    }
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
    }
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
                            pedido.getCliente().getNome(),
                            pedido.getStatus().name(),
                            pedido.getHorarioEntrega() != null ? pedido.getHorarioEntrega().format(formatter) : "",
                            itensDoSetor
                    );
                }).toList();
    }

    public void atualizarStatusItem(Long idItem, StatusItemPedido status) {
        ItemPedido item = itemPedidoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        item.setStatus(status); // Supondo que você adicionou o campo status em ItemPedido
        itemPedidoRepository.save(item);
    }

    public void criarPedidoNoCaixa(PedidoCaixaDTO dto) {
        Comanda comanda = comandaRepository.findById(dto.getCodigoComanda())
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.isAtiva()) {
            throw new RuntimeException("Comanda não está ativa");
        }

        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setFormaPagamento(FormaPagamento.valueOf(dto.getFormaPagamento())); // CORRIGIDO
        pedido.setParaEntrega(false);
        pedido.setEnderecoEntrega("PEDIDO NO CAIXA");
        pedido.setCliente(null); // opcional

        BigDecimal totalPedido = BigDecimal.ZERO;

        List<ItemPedido> itens = new ArrayList<>();
        for (ItemPedidoDTO itemDTO : dto.getItens()) {
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

        if (dto.getValorPago().compareTo(totalPedido) >= 0) {
            pedido.setPagamentoConfirmado(true);
        } else {
            pedido.setPagamentoConfirmado(false);
        }

        pedido.setComanda(comanda); // NOVO

        pedidoRepository.save(pedido);

        comanda.getPedidos().add(pedido);
        comandaRepository.save(comanda);
    }

public PedidoResumoDTO adicionarPedidoCafe(String codigoComanda, AdicionarPedidoCafeDTO dto){
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

        for (AdicionarPedidoCafeDTO.ItemPedidoDTO ItemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(ItemDTO.getProdutoId())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(ItemDTO.getQuantidade());
        item.setStatus(StatusItemPedido.PRONTO);
        item.setPedido(pedido);

        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(ItemDTO.getQuantidade()));
        valorTotal = valorTotal.add(subtotal);
        itens.add(item);

        PedidoResumoDTO.ItemResumoDTO resumoItem = new PedidoResumoDTO.ItemResumoDTO();
        resumoItem.setNomeProduto(produto.getNome());
        resumoItem.setQuantidade(ItemDTO.getQuantidade());
        resumoItem.setPrecoUnitario(produto.getPreco());
        resumoItem.setSubtotal(subtotal);
        itensResumo.add(resumoItem);
        }

        pedido.setValorTotal(valorTotal);
        pedido.setItens(itens);
        pedidoRepository.save(pedido);

        PedidoResumoDTO resumo = new PedidoResumoDTO();
        resumo.setCodigoComanda(codigoComanda);
        resumo.setValorTotal(valorTotal);
        resumo.setItens(itensResumo);

        return resumo;
}
}

