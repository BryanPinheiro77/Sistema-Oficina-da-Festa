package com.oficinadafesta.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PedidoCaixaDTO {
    private String codigoComanda;
    private List<ItemDTO> itens;
    private String formaPagamento;
    private BigDecimal valorPago;
}
