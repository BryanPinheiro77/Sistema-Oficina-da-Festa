package com.oficinadafesta.device.dto;

import java.math.BigDecimal;

public record CatracaSaidaValidarResponseDTO(
        boolean liberar,
        String motivo,
        BigDecimal valorDevido
) {}