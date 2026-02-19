package com.oficinadafesta.ui.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRow {
    private Long produtoId;
    private String nomeProduto;
    private int quantidade;
    private BigDecimal preco;      // preço unitário
    private String observacao;

    public String getPrecoFormatado() {
        if (preco == null) return "";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(preco);
    }
}
