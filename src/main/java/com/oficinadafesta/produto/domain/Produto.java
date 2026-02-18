package com.oficinadafesta.produto.domain;

import com.oficinadafesta.enums.AreaTipo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nome;
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "setor")
    private AreaTipo setor;

    @ManyToOne
    private CategoriaProduto categoria;
}

