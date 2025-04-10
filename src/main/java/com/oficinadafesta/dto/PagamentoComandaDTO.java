package com.oficinadafesta.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagamentoComandaDTO {
    private BigDecimal valorPago;
    private String formaPagamento;
}
