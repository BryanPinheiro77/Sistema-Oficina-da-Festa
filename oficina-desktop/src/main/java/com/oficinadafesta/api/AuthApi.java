package com.oficinadafesta.api;

import com.oficinadafesta.api.dto.AuthResponseDTO;
import com.oficinadafesta.api.dto.LoginRequest;

public class AuthApi {
    private final Http http;
    public AuthApi(Http http) { this.http = http; }

    public AuthResponseDTO login(String usuario, String senha) throws Exception {
        LoginRequest body = new LoginRequest(usuario, senha);
        return http.post("/auth/login", body, AuthResponseDTO.class);
    }
}