package com.oficinadafesta.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PedidoRequestDTO {
    public Long clienteId;
    public String formaPagamento;   // "PIX", "CARTAO", "DINHEIRO"
    public boolean paraEntrega;
    public String enderecoEntrega;
    public String tipoEntrega;      // "RETIRADA" | "ENTREGA"
    public LocalDateTime dataRetirada;
    public List<PedidoItemRequestDTO> itens;

    // se o backend usa distanciaEntregaKm:
    public double distanciaEntregaKm;
}

