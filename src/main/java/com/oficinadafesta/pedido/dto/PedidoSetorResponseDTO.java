package com.oficinadafesta.pedido.dto;

import lombok.Data;
import java.util.List;

@Data
public class PedidoSetorResponseDTO {
    private long idPedido;
    private String nomeCliente;
    private String status;
    private String horarioEntrega;
    private List<ItemDTO> itens;

    @Data
    public static class ItemDTO {
        private String nomeProduto;
        private int quantidade;

        public ItemDTO(String nomeProduto, int quantidade) {
            this.nomeProduto = nomeProduto;
            this.quantidade = quantidade;
        }
    }

    public PedidoSetorResponseDTO(long idPedido, String nomeCliente, String status, String horarioEntrega, List<ItemDTO> itens) {
        this.idPedido = idPedido;
        this.nomeCliente = nomeCliente;
        this.status = status;
        this.horarioEntrega = horarioEntrega;
        this.itens = itens;
    }
}