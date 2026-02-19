package com.oficinadafesta.api.dto;

public class PedidoItemRequestDTO {
    public Long produtoId;
    public Integer quantidade;
    public String observacao;

    public PedidoItemRequestDTO() {}
    public PedidoItemRequestDTO(Long produtoId, Integer quantidade, String observacao) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.observacao = observacao;
    }
}
