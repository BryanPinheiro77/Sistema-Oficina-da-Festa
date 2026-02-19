package com.oficinadafesta.app;

import com.oficinadafesta.api.*;

public class AppContext {
    public final Http http;
    public final AuthApi authApi;
    public final ClienteApi clienteApi;
    public final ProdutoApi produtoApi;
    public final PedidoApi pedidoApi;

    public AppContext(String baseUrl) {
        this.http = new Http(baseUrl);
        this.authApi = new AuthApi(http);
        this.clienteApi = new ClienteApi(http);
        this.produtoApi = new ProdutoApi(http);
        this.pedidoApi = new PedidoApi(http);
    }
}
