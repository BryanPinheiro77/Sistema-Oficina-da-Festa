package com.oficinadafesta.api.dto;

public class LoginRequest {
    public String usuario;
    public String senha;

    public LoginRequest() {}
    public LoginRequest(String usuario, String senha) {
        this.usuario = usuario;
        this.senha = senha;
    }
}
