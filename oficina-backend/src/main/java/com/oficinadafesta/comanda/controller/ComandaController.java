package com.oficinadafesta.comanda.controller;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.ComandaResponseDTO;
import com.oficinadafesta.comanda.service.ComandaService;
import com.oficinadafesta.shared.security.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comandas")
public class ComandaController {

    private static final Logger log = LoggerFactory.getLogger(ComandaController.class);

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    // =========================================================
    // 1) Leitura (geral)
    // =========================================================

    @PreAuthorize(Roles.COMANDA_LER)
    @GetMapping("/{codigo}/resumo")
    public ResponseEntity<ComandaResponseDTO> getResumoComanda(@PathVariable String codigo) {
        log.info("Resumo comanda: codigo={}", codigo);

        Comanda comanda = comandaService.buscarPorCodigo(codigo);

        ComandaResponseDTO dto = new ComandaResponseDTO();
        dto.setCodigo(String.format("%03d", comanda.getCodigo()));
        dto.setAtiva(comanda.isAtiva());

        dto.setPaga(comanda.estaPaga());
        dto.setValorTotal(comanda.calcularTotal());

        dto.setPedidos(comanda.getPedidos().stream().map(pedido -> {
            ComandaResponseDTO.PedidoResumoDTO resumo = new ComandaResponseDTO.PedidoResumoDTO();
            resumo.setId(pedido.getId());
            resumo.setStatus(pedido.getStatus());
            resumo.setTotal(pedido.calcularTotal());
            return resumo;
        }).toList());

        return ResponseEntity.ok(dto);
    }

    @PreAuthorize(Roles.COMANDA_LER)
    @GetMapping("/{codigo}/detalhes")
    public ResponseEntity<ComandaDetalhadaResponseDTO> buscarDetalhesComanda(@PathVariable String codigo) {
        log.info("Detalhes comanda: codigo={}", codigo);
        return ResponseEntity.ok(comandaService.buscarComandaCompleta(codigo));
    }

    // Alias mantido para compatibilidade
    @PreAuthorize(Roles.COMANDA_LER)
    @GetMapping("/{codigo}")
    public ResponseEntity<ComandaDetalhadaResponseDTO> buscarDetalhesComandaAlias(@PathVariable String codigo) {
        log.info("Detalhes comanda (alias): codigo={}", codigo);
        return ResponseEntity.ok(comandaService.buscarComandaCompleta(codigo));
    }

    /**
     * Verificação manual (geral). Fluxo oficial:
     * - Device: POST /catraca/saida/validar (X-Device-Key)
     * - Caixa:  GET /caixa/comandas/{codigo}/verificar-saida
     */
    @PreAuthorize(Roles.COMANDA_LER)
    @GetMapping("/{codigo}/verificar-saida")
    public ResponseEntity<Boolean> podeSair(@PathVariable String codigo) {
        log.info("Verificar saída (geral): codigo={}", codigo);
        return ResponseEntity.ok(comandaService.podeLiberarSaida(codigo));
    }
}