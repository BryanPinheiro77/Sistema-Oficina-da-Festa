package com.oficinadafesta.comanda.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@Getter @Setter
public class ComandaDetalhadaResponseDTO {
    private String codigo;
    private BigDecimal total;
    private boolean paga;
    private List<PedidoDTO> pedidos;


    @Getter @Setter
    public static class PedidoDTO{
        private Long id;
        private String formaPagamento;
        private List<ItemDTO> itens;
    }

    @Getter @Setter
    public static class ItemDTO{
        private String nomeProduto;
        private Integer quantidade;
        private BigDecimal preco;
        private String status;
    }
}
