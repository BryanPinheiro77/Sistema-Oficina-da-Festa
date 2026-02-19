package com.oficinadafesta.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PedidoRequestDTO {
    public Long clienteId;
    public String formaPagamento;     // "PIX" / "CARTAO" / "DINHEIRO"
    public String tipoEntrega;        // "RETIRADA" / "ENTREGA"
    public Boolean paraEntrega;
    public String enderecoEntrega;
    public LocalDateTime dataRetirada;
    public List<PedidoItemRequestDTO> itens;
}
