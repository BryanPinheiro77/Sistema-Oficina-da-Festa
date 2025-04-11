package com.oficinadafesta.login;
import com.oficinadafesta.enums.AreaTipo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;
    private String senha;

    @Enumerated(EnumType.STRING)
    private AreaTipo setor;
}
