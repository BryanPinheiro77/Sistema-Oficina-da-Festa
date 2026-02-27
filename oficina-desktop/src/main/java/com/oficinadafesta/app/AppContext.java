package com.oficinadafesta.app;

import com.oficinadafesta.api.*;

public class AppContext {
    public final Http http;
    public final AuthApi authApi;
    public final ClienteApi clienteApi;
    public final ProdutoApi produtoApi;
    public final PedidoApi pedidoApi;
    public final CaixaApi caixaApi;

    public String accessToken;
    public String setor;

    public AppContext(String baseUrl) {
        this.http = new Http(baseUrl);
        this.authApi = new AuthApi(http);
        this.clienteApi = new ClienteApi(http);
        this.produtoApi = new ProdutoApi(http);
        this.pedidoApi = new PedidoApi(http);
        this.caixaApi = new CaixaApi(http);
    }

    public void clearSession() {
        this.accessToken = null;
        this.setor = null;
        this.http.setBearerToken(null);
    }

    public void onUnauthorized(Http.UnauthorizedListener listener) {
        this.http.setUnauthorizedListener(listener);
    }
}