package com.oficinadafesta.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItemRequestDTO {
    private Long produtoId;
    private int quantidade;
    private String observacao; // opcional
}
