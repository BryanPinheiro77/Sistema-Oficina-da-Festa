package com.oficinadafesta.api.dto;

import java.math.BigDecimal;

public class PagamentoComandaDTO {
    public String formaPagamento; // "PIX", "CARTAO", "DINHEIRO"
    public BigDecimal valorPago;  // opcional (pode mandar null por enquanto)
}