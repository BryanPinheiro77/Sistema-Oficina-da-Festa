package com.oficinadafesta.caixa.controller;

import com.oficinadafesta.caixa.service.CaixaService;
import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.ComandaResponseDTO;
import com.oficinadafesta.comanda.dto.PagamentoComandaDTO;
import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.dto.PedidoCaixaDTO;
import com.oficinadafesta.pedido.dto.PedidoRequestDTO;
import com.oficinadafesta.shared.security.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/caixa")
public class CaixaController {

    private static final Logger log = LoggerFactory.getLogger(CaixaController.class);

    private final CaixaService caixaService;

    public CaixaController(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    // =========================================================
    // 1) PEDIDOS (caixa)
    // =========================================================

    /**
     * Pedido NORMAL criado pelo caixa (ENTREGA/RETIRADA/IMEDIATA).
     * Mantém a regra no PedidoService.criarPedido(dto).
     */
    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping("/pedidos")
    public ResponseEntity<Pedido> criarPedidoNormal(@RequestBody PedidoRequestDTO dto) {
        log.info("POST /caixa/pedidos - pedido normal (tipoEntrega={})", dto.getTipoEntrega());
        Pedido criado = caixaService.criarPedidoNormal(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Pedido de consumo local vinculado a uma comanda ativa.
     * (equivalente ao antigo POST /pedidos/caixa)
     */
    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping("/comandas/pedidos")
    public ResponseEntity<Void> criarPedidoNaComanda(@RequestBody PedidoCaixaDTO dto) {
        log.info("POST /caixa/comandas/pedidos - comanda={}", dto.getCodigoComanda());
        caixaService.criarPedidoNaComanda(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =========================================================
    // 2) COMANDAS (caixa)
    // =========================================================

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @GetMapping("/comandas/{codigo}/resumo")
    public ResponseEntity<ComandaResponseDTO> resumoComanda(@PathVariable String codigo) {
        return ResponseEntity.ok(caixaService.resumoComanda(codigo));
    }

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @GetMapping("/comandas/{codigo}/detalhes")
    public ResponseEntity<ComandaDetalhadaResponseDTO> detalhesComanda(@PathVariable String codigo) {
        return ResponseEntity.ok(caixaService.detalhesComanda(codigo));
    }

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @GetMapping("/comandas/{codigo}/verificar-saida")
    public ResponseEntity<Boolean> verificarSaida(@PathVariable String codigo) {
        return ResponseEntity.ok(caixaService.verificarSaida(codigo));
    }

    @PreAuthorize(Roles.COMANDA_PAGAR_FECHAR)
    @PostMapping("/comandas/{codigo}/pagar")
    public ResponseEntity<Void> pagarComanda(@PathVariable String codigo, @RequestBody PagamentoComandaDTO dto) {
        caixaService.pagarComanda(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(Roles.COMANDA_PAGAR_FECHAR)
    @PostMapping("/comandas/{codigo}/fechar")
    public ResponseEntity<Void> fecharComanda(@PathVariable String codigo) {
        caixaService.fecharComanda(codigo);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 3) CONTINGÊNCIA
    // =========================================================

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping("/comandas/entrada-manual")
    public ResponseEntity<Comanda> entradaManual() {
        Comanda ativada = caixaService.entradaManualProximaComanda();
        return ResponseEntity.ok(ativada);
    }
}