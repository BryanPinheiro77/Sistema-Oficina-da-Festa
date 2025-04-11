package com.oficinadafesta.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PedidoResumoDTO {
    private String codigoComanda;
    private BigDecimal valorTotal;
    private List<ItemResumoDTO> itens;

    @Getter
    @Setter
    public static class ItemResumoDTO {
        private String nomeProduto;
        private int quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
    }
}
