package com.oficinadafesta.pedido.dto;

import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.TipoEntrega;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoRequestDTO {

    private Long clienteId;

    private FormaPagamento formaPagamento;
    private TipoEntrega tipoEntrega;

    private boolean paraEntrega;
    private String enderecoEntrega;
    private BigDecimal taxaEntrega;
    private double distanciaEntregaKm;

    private LocalDateTime dataRetirada;
    private String horaRetirada;

    private List<PedidoItemRequestDTO> itens;
}
