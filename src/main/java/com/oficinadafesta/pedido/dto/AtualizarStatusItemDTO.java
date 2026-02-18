package com.oficinadafesta.pedido.dto;

import com.oficinadafesta.enums.StatusPedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtualizarStatusItemDTO {
    private Long idItem;
    private StatusPedido status;
}
