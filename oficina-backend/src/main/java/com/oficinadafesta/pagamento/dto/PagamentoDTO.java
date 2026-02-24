package com.oficinadafesta.pagamento.dto;

import com.oficinadafesta.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagamentoDTO(

        @notNull
        @Positive
        BigDecimal valor,

        @NotNull
        FormaPagamento formaPagamento,

        Long pedidoId, // usado para pagamento online
        Long comandaId // usado para pagamento presencial
) {}

