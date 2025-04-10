package com.oficinadafesta.dto;

import lombok.Data;

@Data
public class ItemDTO extends ItemPedidoDTO {
    private Long idProduto;
    private int quantidade;
}
