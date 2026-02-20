package com.oficinadafesta.ui.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ItemPedidoRow {
    private Long produtoId;
    private String nomeProduto;
    private int quantidade;
    private BigDecimal preco;
    private String observacao;

    public ItemPedidoRow() {}

    public ItemPedidoRow(Long produtoId, String nomeProduto, int quantidade, BigDecimal preco, String observacao) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.preco = preco;
        this.observacao = observacao;
    }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getPrecoFormatado() {
        if (preco == null) return "";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(preco);
    }
}