package com.oficinadafesta.produto.domain;

import com.oficinadafesta.enums.AreaTipo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Enumerated(EnumType.STRING)
    private AreaTipo setor;
}
