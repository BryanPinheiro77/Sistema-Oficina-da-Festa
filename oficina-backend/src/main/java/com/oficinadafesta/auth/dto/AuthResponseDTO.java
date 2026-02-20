package com.oficinadafesta.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    public String accessToken;
    public long expiresInSeconds;
    public String setor;
}
