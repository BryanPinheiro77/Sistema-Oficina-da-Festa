package com.oficinadafesta.pedido.dto;

import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.StatusPedido;
import com.oficinadafesta.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoExpedicaoDTO(
        Long pedidoId,
        String clienteNome,
        String clienteTelefone,
        TipoEntrega tipoEntrega,
        FormaPagamento formaPagamento,
        StatusPedido statusPedido,
        String enderecoEntrega,
        LocalDateTime horarioEntregaRetirada,
        BigDecimal valorTotal,
        List<ItemDTO> itens
) {
    public record ItemDTO(String produtoNome, int quantidade) {}
}