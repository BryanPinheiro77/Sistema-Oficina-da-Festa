package com.oficinadafesta.auth.dto;

import com.oficinadafesta.enums.AreaTipo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateDTO {

    @Size(min = 3, max = 60)
    private String usuario;

    @NotNull
    private AreaTipo setor;

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public AreaTipo getSetor() { return setor; }
    public void setSetor(AreaTipo setor) { this.setor = setor; }
}