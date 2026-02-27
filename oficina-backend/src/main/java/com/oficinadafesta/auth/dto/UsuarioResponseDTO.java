package com.oficinadafesta.auth.dto;

public class UsuarioResponseDTO {
    private Long id;
    private String usuario;
    private String setor;

    public UsuarioResponseDTO(Long id, String usuario, String setor) {
        this.id = id;
        this.usuario = usuario;
        this.setor = setor;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getSetor() { return setor; }
}