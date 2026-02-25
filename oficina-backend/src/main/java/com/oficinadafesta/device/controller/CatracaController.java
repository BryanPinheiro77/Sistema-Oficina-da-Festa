package com.oficinadafesta.device.controller;

import com.oficinadafesta.comanda.service.ComandaService;
import com.oficinadafesta.device.dto.CatracaCodigoDTO;
import com.oficinadafesta.device.dto.CatracaSaidaValidarResponseDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catraca")
public class CatracaController {

    private static final Logger log = LoggerFactory.getLogger(CatracaController.class);

    private final ComandaService comandaService;

    public CatracaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    // entrada: ativa comanda específica (catraca diz qual saiu)
    @PostMapping("/entrada")
    public ResponseEntity<Void> entrada(@Valid @RequestBody CatracaCodigoDTO body) {
        log.info("POST /catraca/entrada - ativando comanda codigo={}", body.codigo());
        comandaService.ativarComanda(body.codigo());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // saída: valida se pode liberar
    @PostMapping("/saida/validar")
    public ResponseEntity<CatracaSaidaValidarResponseDTO> validarSaida(@Valid @RequestBody CatracaCodigoDTO body) {
        log.info("POST /catraca/saida/validar - codigo={}", body.codigo());
        return ResponseEntity.ok(comandaService.validarSaida(body.codigo()));
    }

    // saída: confirma que passou
    @PostMapping("/saida/confirmar")
    public ResponseEntity<Void> confirmarSaida(@Valid @RequestBody CatracaCodigoDTO body) {
        log.info("POST /catraca/saida/confirmar - codigo={}", body.codigo());
        comandaService.confirmarSaida(body.codigo());
        return ResponseEntity.noContent().build();
    }
}