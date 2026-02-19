package com.oficinadafesta.pedido.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PedidoCaixaDTO {
    private String codigoComanda;
    private List<PedidoItemRequestDTO> itens;
    private String formaPagamento;
    private BigDecimal valorPago;
}
