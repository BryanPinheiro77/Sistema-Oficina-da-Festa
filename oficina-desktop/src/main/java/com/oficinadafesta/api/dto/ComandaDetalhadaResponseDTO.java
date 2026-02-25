package com.oficinadafesta.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class ComandaDetalhadaResponseDTO {
    public String codigo;
    public BigDecimal total;
    public boolean paga;
    public List<PedidoDTO> pedidos;

    public static class PedidoDTO {
        public Long id;
        public String formaPagamento;
        public List<ItemDTO> itens;
    }

    public static class ItemDTO {
        public String nomeProduto;
        public Integer quantidade;
        public BigDecimal preco;
        public String status;
    }
}