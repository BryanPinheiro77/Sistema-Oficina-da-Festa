package com.oficinadafesta.api;

import com.oficinadafesta.api.dto.LoginRequest;

public class AuthApi {
    private final Http http;
    public AuthApi(Http http) { this.http = http; }

    public String login(String usuario, String senha) throws Exception {
        return http.post("/api/usuarios/login", new LoginRequest(usuario, senha), String.class);
    }
}
