package com.oficinadafesta.device.service;

import com.oficinadafesta.comanda.service.ComandaService;
import com.oficinadafesta.device.dto.CatracaSaidaValidarResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CatracaService {

    private static final Logger log = LoggerFactory.getLogger(CatracaService.class);

    private final ComandaService comandaService;

    public CatracaService(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    public void entrada(String codigo) {
        log.info("Catraca entrada: ativar comanda codigo={}", codigo);
        comandaService.ativarComanda(codigo);
    }

    public CatracaSaidaValidarResponseDTO validarSaida(String codigo) {
        log.info("Catraca saída: validar codigo={}", codigo);
        return comandaService.validarSaida(codigo);
    }

    public void confirmarSaida(String codigo) {
        log.info("Catraca saída: confirmar codigo={}", codigo);
        comandaService.confirmarSaida(codigo);
    }
}