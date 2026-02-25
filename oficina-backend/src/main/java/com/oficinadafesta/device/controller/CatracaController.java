package com.oficinadafesta.device.controller;

import com.oficinadafesta.device.dto.CatracaCodigoDTO;
import com.oficinadafesta.device.dto.CatracaSaidaValidarResponseDTO;
import com.oficinadafesta.device.service.CatracaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catraca")
public class CatracaController {

    private final CatracaService catracaService;

    public CatracaController(CatracaService catracaService) {
        this.catracaService = catracaService;
    }

    @PostMapping("/entrada")
    public ResponseEntity<Void> entrada(@Valid @RequestBody CatracaCodigoDTO body) {
        catracaService.entrada(body.codigo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/saida/validar")
    public ResponseEntity<CatracaSaidaValidarResponseDTO> validar(@Valid @RequestBody CatracaCodigoDTO body) {
        return ResponseEntity.ok(catracaService.validarSaida(body.codigo()));
    }

    @PostMapping("/saida/confirmar")
    public ResponseEntity<Void> confirmar(@Valid @RequestBody CatracaCodigoDTO body) {
        catracaService.confirmarSaida(body.codigo());
        return ResponseEntity.noContent().build();
    }
}