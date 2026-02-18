package com.oficinadafesta.pedido.dto;

import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.TipoEntrega;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;  // Importar para data de retirada
import java.util.List;

@Data
public class PedidoRequestDTO {

    private Long clienteId; // agora usamos o ID para buscar o cliente

    private FormaPagamento formaPagamento;
    private TipoEntrega tipoEntrega;

    private boolean paraEntrega;
    private String enderecoEntrega;
    private BigDecimal taxaEntrega;
    private double distanciaEntregaKm;

    // Adicionando data e hora de retirada
    private LocalDateTime dataRetirada;
    private String horaRetirada;

    private List<PedidoItemDTO> itens;

    @Data
    public static class PedidoItemDTO {
        private Long produtoId;
        private int quantidade;
    }
}
