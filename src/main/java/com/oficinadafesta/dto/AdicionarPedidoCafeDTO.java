package com.oficinadafesta.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdicionarPedidoCafeDTO {
    private List<ItemPedidoDTO> itens;

    @Getter
    @Setter
    public static class ItemPedidoDTO{
        private Long produtoId;
        private Integer quantidade;
    }
}
