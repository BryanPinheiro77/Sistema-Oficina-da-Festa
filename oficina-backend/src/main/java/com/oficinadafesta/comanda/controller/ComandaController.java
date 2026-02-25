package com.oficinadafesta.comanda.controller;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.ComandaResponseDTO;
import com.oficinadafesta.comanda.dto.PagamentoComandaDTO;
import com.oficinadafesta.comanda.service.ComandaService;
import com.oficinadafesta.shared.security.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    // 1) Entrada (manual / contingência)
    // - Entrada REAL: /catraca/entrada (X-Device-Key)
    // =========================================================
    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping("/entrada")
    public ResponseEntity<Comanda> registrarEntradaManual() {
        log.info("Entrada MANUAL (fallback): ativando próxima comanda livre");
        Comanda ativada = comandaService.ativarProximaComanda();
        return ResponseEntity.ok(ativada);
    }

    // =========================================================
    // 2) Leitura (resumo / caixa / detalhes)
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

        log.info("Resumo retornado: codigo={}, ativa={}, paga={}, total={}, pedidos={}",
                dto.getCodigo(), dto.isAtiva(), dto.isPaga(), dto.getValorTotal(),
                dto.getPedidos() != null ? dto.getPedidos().size() : 0);

        return ResponseEntity.ok(dto);
    }

    /**
     * Caixa visualiza comanda (rota limpa).
     * Se você quiser manter compatibilidade com o desktop, deixe esse path fixo.
     */
    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @GetMapping("/caixa/{codigo}")
    public ResponseEntity<ComandaResponseDTO> visualizarComandaNoCaixa(@PathVariable String codigo) {
        log.info("Visualizar comanda no CAIXA: codigo={}", codigo);

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

    /**
     * Mantive esse endpoint pra não quebrar quem já chama GET /comandas/{codigo}
     * (mas recomendo padronizar para /detalhes)
     */
    @PreAuthorize(Roles.COMANDA_LER)
    @GetMapping("/{codigo}")
    public ResponseEntity<ComandaDetalhadaResponseDTO> buscarDetalhesComandaAlias(@PathVariable String codigo) {
        log.info("Detalhes comanda (alias): codigo={}", codigo);
        return ResponseEntity.ok(comandaService.buscarComandaCompleta(codigo));
    }

    // =========================================================
    // 3) Saída / fechamento / pagamento
    // =========================================================

    /**
     * Verificação manual (tela). A catraca real deve usar:
     * POST /catraca/saida/validar (X-Device-Key)
     */
    @PreAuthorize(Roles.COMANDA_LER)
    @GetMapping("/{codigo}/verificar-saida")
    public ResponseEntity<Boolean> podeSair(@PathVariable String codigo) {
        log.info("Verificar saída (manual): codigo={}", codigo);
        boolean liberar = comandaService.podeLiberarSaida(codigo);
        return ResponseEntity.ok(liberar);
    }

    @PreAuthorize(Roles.COMANDA_PAGAR_FECHAR)
    @PostMapping("/{codigo}/fechar")
    public ResponseEntity<Void> fecharComanda(@PathVariable String codigo) {
        log.info("Fechar comanda: codigo={}", codigo);
        comandaService.fecharComanda(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(Roles.COMANDA_PAGAR_FECHAR)
    @PostMapping("/{codigo}/pagar")
    public ResponseEntity<Void> pagarComanda(@PathVariable String codigo, @RequestBody PagamentoComandaDTO dto) {
        log.info("Pagar comanda: codigo={}", codigo);
        comandaService.pagarComanda(codigo, dto);
        return ResponseEntity.noContent().build();
    }
}