package com.oficinadafesta.produto.dto;

import com.oficinadafesta.enums.AreaTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoCreateDTO(
        @NotBlank String nome,
        @NotNull BigDecimal preco,
        @NotNull AreaTipo setor,
        @NotNull Long categoriaId
) {}