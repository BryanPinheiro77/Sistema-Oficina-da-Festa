package com.oficinadafesta.api;

import com.oficinadafesta.api.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.api.dto.PagamentoComandaDTO;
import com.oficinadafesta.api.dto.PedidoCaixaDTO;
import com.oficinadafesta.api.dto.PedidoRequestDTO;

public class CaixaApi {

    private final Http http;

    public CaixaApi(Http http) {
        this.http = http;
    }

    // pedido normal (ENTREGA/RETIRADA/IMEDIATA)
    public Object criarPedidoNormal(PedidoRequestDTO dto) throws Exception {
        return http.post("/caixa/pedidos", dto, Object.class);
    }

    // pedido na comanda (consumo local)
    public void criarPedidoNaComanda(PedidoCaixaDTO dto) throws Exception {
        http.post("/caixa/comandas/pedidos", dto, String.class); // backend retorna 201 sem body
    }

    // detalhes da comanda (inclui itens)
    public ComandaDetalhadaResponseDTO detalhesComanda(String codigo) throws Exception {
        return http.get("/caixa/comandas/" + codigo + "/detalhes", ComandaDetalhadaResponseDTO.class);
    }

    // pagar comanda
    public void pagarComanda(String codigo, PagamentoComandaDTO dto) throws Exception {
        http.post("/caixa/comandas/" + codigo + "/pagar", dto, String.class); // 204
    }
}