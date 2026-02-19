package com.oficinadafesta.api;

import com.oficinadafesta.api.dto.PedidoRequestDTO;

public class PedidoApi {
    private final Http http;
    public PedidoApi(Http http) { this.http = http; }

    public Object criarPedido(PedidoRequestDTO dto) throws Exception {
        // seu backend retorna Pedido (entidade). Você pode receber como Object por enquanto.
        return http.post("/pedidos", dto, Object.class);
    }
}
