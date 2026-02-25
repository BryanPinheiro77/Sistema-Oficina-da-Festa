package com.oficinadafesta.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class PedidoCaixaDTO {
    public Integer codigoComanda;     // ex: 1, 12, 123 (backend formata como "001" se precisar)
    public String formaPagamento;     // "PIX", "CARTAO", "DINHEIRO"
    public BigDecimal valorPago;      // pode ser 0 se NA_COMANDA; depende do teu fluxo
    public List<PedidoItemRequestDTO> itens;
}