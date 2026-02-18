package com.oficinadafesta.comanda.service;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.PagamentoComandaDTO;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private final PedidoRepository pedidoRepository;

    // ✅ Ativa a próxima comanda disponível
    public Comanda ativarProximaComanda() {
        Optional<Comanda> livre = comandaRepository.findFirstByAtivaFalseOrderByCodigoAsc();

        if (livre.isEmpty()) {
            throw new RuntimeException("Não há comandas disponíveis");
        }

        Comanda comanda = livre.get();
        comanda.setAtiva(true);
        comanda.setBloqueada(false);
        comanda.setPaga(false);

        return comandaRepository.save(comanda);
    }

    // ✅ Verifica se pode liberar saída
    public boolean podeLiberarSaida(String codigo) {
        Comanda comanda = comandaRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        // ⬇️ Pode liberar se estiver paga ou sem valor
        return comanda.isPaga() || comanda.getValorTotal().compareTo(BigDecimal.ZERO) == 0;
    }

    // ✅ Marca comanda como paga e desbloqueia
    public void marcarComoPaga(String codigo) {
        Comanda comanda = comandaRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        comanda.setPaga(true);
        comanda.setBloqueada(false);
        comandaRepository.save(comanda);
    }

    // ✅ Busca comanda pelo código
    public Comanda buscarPorcodigo(String codigo) {
        return comandaRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));
    }

    @Transactional
    public void pagarComanda(String codigo, PagamentoComandaDTO dto){
        Comanda comanda = buscarPorcodigo(codigo);

        if (comanda.isPaga()) {
            throw new RuntimeException("Comanda já foi paga.");
        }

        comanda.setPaga(true);
        comanda.setAtiva(false);
        comanda.setBloqueada(false);

        List<Pedido> pedidos = comanda.getPedidos();
        for (Pedido pedido : pedidos) {
            pedido.setComanda(null);
            pedidoRepository.save(pedido);
        }

        comanda.getPedidos().clear();
        comandaRepository.save(comanda);
    }

    public Comanda fecharComanda(String codigo) {
        Comanda comanda = comandaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        comanda.setPaga(true);
        comanda.setBloqueada(false);

        List<Pedido> pedidos = comanda.getPedidos();

        for (Pedido pedido : pedidos) {
            pedido.setComanda(null);
            pedidoRepository.save(pedido);
        }

        comanda.getPedidos().clear();

        return comandaRepository.save(comanda);
    }

    public ComandaDetalhadaResponseDTO buscarComandaCompleta(String codigo){
        Comanda comanda = comandaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        ComandaDetalhadaResponseDTO dto = new ComandaDetalhadaResponseDTO();
        dto.setCodigo(comanda.getCodigo());
        dto.setPaga(comanda.isPaga());
        dto.setTotal(comanda.getValorTotal());

        List<ComandaDetalhadaResponseDTO.PedidoDTO> pedidos = comanda.getPedidos().stream().map(p -> {
            ComandaDetalhadaResponseDTO.PedidoDTO pedidoDTO = new ComandaDetalhadaResponseDTO.PedidoDTO();
            pedidoDTO.setId(p.getId());
            pedidoDTO.setFormaPagamento(p.getFormaPagamento().name());

            List<ComandaDetalhadaResponseDTO.ItemDTO> itens = p.getItens().stream().map(i -> {
                ComandaDetalhadaResponseDTO.ItemDTO itemDTO = new ComandaDetalhadaResponseDTO.ItemDTO();
                itemDTO.setNomeProduto(i.getProduto().getNome());
                itemDTO.setQuantidade(i.getQuantidade());
                itemDTO.setPreco(i.getProduto().getPreco());
                itemDTO.setStatus(i.getStatus().name());
                return itemDTO;
            }).collect(Collectors.toList());

            pedidoDTO.setItens(itens);
            return pedidoDTO;
        }).collect(Collectors.toList());

        return dto;
    }
}
