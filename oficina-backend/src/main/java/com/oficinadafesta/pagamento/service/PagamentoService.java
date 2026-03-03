package com.oficinadafesta.pagamento.service;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import com.oficinadafesta.pagamento.domain.Pagamento;
import com.oficinadafesta.pagamento.dto.PagamentoRequestDTO;
import com.oficinadafesta.pagamento.dto.PagamentoResponseDTO;
import com.oficinadafesta.pagamento.repository.PagamentoRepository;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.repository.PedidoRepository;
import com.oficinadafesta.enums.FormaPagamento;

import com.oficinadafesta.shared.security.LoggedUser;
import com.oficinadafesta.shared.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final ComandaRepository comandaRepository;

    @Transactional
    public PagamentoResponseDTO criarPagamento(PagamentoRequestDTO dto) {

        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Criando pagamento: valor={}, forma={}, pedidoId={}, comandaId={} | ator=userId:{} setor:{}",
                dto.valor(), dto.formaPagamento(), dto.pedidoId(), dto.comandaId(),
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");

        Pagamento pagamento = new Pagamento();
        pagamento.setValor(dto.valor());
        pagamento.setFormaPagamento(dto.formaPagamento());

        if (dto.pedidoId() != null) {

            Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

            pagamento.setPedido(pedido);

        } else if (dto.comandaId() != null) {

            Comanda comanda = comandaRepository.findById(dto.comandaId())
                    .orElseThrow(() -> new RuntimeException("Comanda não encontrada."));

            pagamento.setComanda(comanda);

        } else {
            throw new IllegalArgumentException(
                    "Pagamento deve informar pedidoId ou comandaId."
            );
        }

        Pagamento salvo = pagamentoRepository.save(pagamento);

        log.info("Pagamento criado: id={}, valor={}, forma={}",
                salvo.getId(), salvo.getValor(), salvo.getFormaPagamento());

        return new PagamentoResponseDTO(
                salvo.getId(),
                salvo.getValor(),
                salvo.getFormaPagamento(),
                salvo.getPagoEm(),
                salvo.getPedido() != null ? salvo.getPedido().getId() : null,
                salvo.getComanda() != null ? salvo.getComanda().getId() : null
        );

    }

    @Transactional
    public PagamentoResponseDTO buscarPorId(Long id) {

        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));

        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getPagoEm(),
                pagamento.getPedido() != null ? pagamento.getPedido().getId() : null,
                pagamento.getComanda() != null ? pagamento.getComanda().getId() : null
        );
    }

    @Transactional
    public List<PagamentoResponseDTO> listarTodos() {

        return pagamentoRepository.findAll()
                .stream()
                .map(p -> new PagamentoResponseDTO(
                        p.getId(),
                        p.getValor(),
                        p.getFormaPagamento(),
                        p.getPagoEm(),
                        p.getPedido() != null ? p.getPedido().getId() : null,
                        p.getComanda() != null ? p.getComanda().getId() : null
                ))
                .toList();
    }
}