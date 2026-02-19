package com.oficinadafesta.api.dto;

import java.math.BigDecimal;

public class ProdutoDTO {
    public Long id;
    public String nome;
    public BigDecimal preco;
    public String setor;     // se vier do backend
    public String categoria; // se vier do backend (opcional)
}
