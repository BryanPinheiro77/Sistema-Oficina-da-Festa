package com.oficinadafesta.pagamento.service;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import com.oficinadafesta.pagamento.domain.Pagamento;
import com.oficinadafesta.pagamento.dto.PagamentoRequestDTO;
import com.oficinadafesta.pagamento.dto.PagamentoResponseDTO;
import com.oficinadafesta.pagamento.repository.PagamentoRepository;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final ComandaRepository comandaRepository;

    @Transactional
    public PagamentoResponseDTO criarPagamento(PagamentoRequestDTO dto) {

        Pagamento pagamento = new Pagamento();
        pagamento.setValor(dto.valor());
        pagamento.setFormaPagamento(dto.formaPagamento());

        if (dto.pedidoId() != null) {

            Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

            if (pedido.possuiComanda()) {
                throw new IllegalStateException(
                        "Pedido vinculado à comanda deve ser pago pela comanda."
                );
            }

            pagamento.setPedido(pedido);

        } else if (dto.comandaId() != null) {

            Comanda comanda = comandaRepository.findById(dto.comandaId())
                    .orElseThrow(() -> new RuntimeException("Comanda não encontrada."));

            if (!comanda.isAtiva()) {
                throw new IllegalStateException("Comanda está fechada.");
            }

            pagamento.setComanda(comanda);

        } else {
            throw new IllegalArgumentException(
                    "Pagamento deve informar pedidoId ou comandaId."
            );
        }

        pagamentoRepository.save(pagamento);

        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getPagoEm(),
                pagamento.getPedido() != null ? pagamento.getPedido().getId() : null,
                pagamento.getComanda() != null ? pagamento.getComanda().getId() : null
        );
    }
}