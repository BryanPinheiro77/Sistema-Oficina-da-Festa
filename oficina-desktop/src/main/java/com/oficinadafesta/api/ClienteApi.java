package com.oficinadafesta.api;

import com.oficinadafesta.api.dto.ClienteDTO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ClienteApi {
    private final Http http;
    public ClienteApi(Http http) { this.http = http; }

    public ClienteDTO buscarPorTelefone(String telefone) throws Exception {
        String t = URLEncoder.encode(telefone, StandardCharsets.UTF_8);
        return http.get("/clientes/telefone/" + t, ClienteDTO.class);
    }

    public ClienteDTO criar(ClienteDTO dto) throws Exception {
        return http.post("/clientes", dto, ClienteDTO.class);
    }
}
