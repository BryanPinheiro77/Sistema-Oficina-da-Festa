package com.oficinadafesta.pagamento.dto;

import com.oficinadafesta.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(

        Long id,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        LocalDateTime pagoEm,
        Long pedidoId,
        Long comandaId
) {}