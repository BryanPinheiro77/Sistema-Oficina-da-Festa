package com.oficinadafesta.api;

import com.oficinadafesta.api.dto.ProdutoDTO;

public class ProdutoApi {
    private final Http http;
    public ProdutoApi(Http http) { this.http = http; }

    public ProdutoDTO[] listarTodos() throws Exception {
        return http.get("/produtos", ProdutoDTO[].class);
    }
}
