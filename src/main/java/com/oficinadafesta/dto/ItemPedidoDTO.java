package com.oficinadafesta.dto;

public class ItemPedidoDTO {
    private Long produtoId;
    private int quantidade;

    // Getters e setters
    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
