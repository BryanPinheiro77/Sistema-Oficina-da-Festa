package com.oficinadafesta.pagamento.dto;

import com.oficinadafesta.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagamentoRequestDTO(

        @NotNull
        @Positive
        BigDecimal valor,

        @NotNull
        FormaPagamento formaPagamento,

        Long pedidoId,
        Long comandaId
) {}