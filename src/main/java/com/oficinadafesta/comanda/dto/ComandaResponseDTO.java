package com.oficinadafesta.comanda.dto;

import com.oficinadafesta.enums.StatusPedido;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ComandaResponseDTO {
    private String codigo;
    private boolean ativa;
    private boolean bloqueada;
    private boolean paga;
    private BigDecimal valorTotal;
    private List<PedidoResumoDTO> pedidos;

    @Data
    public static class PedidoResumoDTO{
        private Long id;
        private StatusPedido status;
        private BigDecimal total;
    }
}
