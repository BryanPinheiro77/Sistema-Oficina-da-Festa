package com.oficinadafesta.caixa.dto;

public class ItemPedidoRow {
    private String nomeProduto;
    private int quantidade;
    private String precoFormatado;

    public ItemPedidoRow(String nomeProduto, int quantidade, String precoFormatado) {
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoFormatado = precoFormatado;
    }

    public String getNomeProduto() { return nomeProduto; }
    public int getQuantidade() { return quantidade; }
    public String getPrecoFormatado() { return precoFormatado; }
}
