package com.oficinadafesta.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ItemPedidoDTO {
    private Long produtoId;
    private String nomeProduto;
    private int quantidade;
    private BigDecimal preco;

    public ItemPedidoDTO() {
    }

    public ItemPedidoDTO(Long produtoId, String nomeProduto, int quantidade, BigDecimal preco) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    // Usado no back-end
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

    // Usado no JavaFX
    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getPrecoFormatado() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(preco);
    }
}
