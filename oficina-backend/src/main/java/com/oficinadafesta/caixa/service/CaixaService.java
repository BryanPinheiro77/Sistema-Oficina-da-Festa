package com.oficinadafesta.caixa.service;

import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.ComandaResponseDTO;
import com.oficinadafesta.comanda.dto.PagamentoComandaDTO;
import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.service.ComandaService;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.dto.PedidoCaixaDTO;
import com.oficinadafesta.pedido.dto.PedidoRequestDTO;
import com.oficinadafesta.pedido.service.PedidoService;
import com.oficinadafesta.shared.security.LoggedUser;
import com.oficinadafesta.shared.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CaixaService {

    private static final Logger log = LoggerFactory.getLogger(CaixaService.class);

    private final PedidoService pedidoService;
    private final ComandaService comandaService;

    public CaixaService(PedidoService pedidoService, ComandaService comandaService) {
        this.pedidoService = pedidoService;
        this.comandaService = comandaService;
    }

    // =========================================================
    // 1) Pedidos do caixa
    // =========================================================

    /** Pedido "normal" (ENTREGA/RETIRADA/IMEDIATA) criado no caixa */
    public Pedido criarPedidoNormal(PedidoRequestDTO dto) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Caixa -> criando pedido normal: clienteId={}, tipoEntrega={}, formaPagamento={} | ator=userId:{} setor:{}",
                dto.getClienteId(), dto.getTipoEntrega(), dto.getFormaPagamento(),
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");
        return pedidoService.criarPedido(dto);
    }

    /** Pedido de consumo local vinculado à comanda (fluxo caixa) */
    public void criarPedidoNaComanda(PedidoCaixaDTO dto) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Caixa -> criando pedido na comanda: codigoComanda={}, formaPagamento={}, valorPago={} | ator=userId:{} setor:{}",
                dto.getCodigoComanda(), dto.getFormaPagamento(), dto.getValorPago(),
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");
        pedidoService.criarPedidoNoCaixa(dto);
    }

    // =========================================================
    // 2) Comandas no caixa
    // =========================================================

    public ComandaResponseDTO resumoComanda(String codigo) {
        log.info("Caixa -> resumo comanda: codigo={}", codigo);

        Comanda comanda = comandaService.buscarPorCodigo(codigo);

        ComandaResponseDTO dto = new ComandaResponseDTO();
        dto.setCodigo(String.format("%03d", comanda.getCodigo()));
        dto.setAtiva(comanda.isAtiva());

        dto.setPaga(comanda.estaPaga());
        dto.setValorTotal(comanda.calcularTotal());

        dto.setPedidos(comanda.getPedidos().stream().map(pedido -> {
            ComandaResponseDTO.PedidoResumoDTO r = new ComandaResponseDTO.PedidoResumoDTO();
            r.setId(pedido.getId());
            r.setStatus(pedido.getStatus());
            r.setTotal(pedido.calcularTotal());
            return r;
        }).toList());

        return dto;
    }

    public ComandaDetalhadaResponseDTO detalhesComanda(String codigo) {
        log.info("Caixa -> detalhes comanda: codigo={}", codigo);
        return comandaService.buscarComandaCompleta(codigo);
    }

    public boolean verificarSaida(String codigo) {
        log.info("Caixa -> verificar saída: codigo={}", codigo);
        return comandaService.podeLiberarSaida(codigo);
    }

    public void pagarComanda(String codigo, PagamentoComandaDTO dto) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Caixa -> pagar comanda: codigo={} | ator=userId:{} setor:{}",
                codigo,
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");
        comandaService.pagarComanda(codigo, dto);
    }

    public void fecharComanda(String codigo) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Caixa -> fechar comanda: codigo={} | ator=userId:{} setor:{}",
                codigo,
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");
        comandaService.fecharComanda(codigo);
    }

    public Comanda entradaManualProximaComanda() {
        log.info("Caixa -> entrada manual: ativar próxima comanda");
        return comandaService.ativarProximaComanda();
    }
}