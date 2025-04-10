package com.oficinadafesta.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItensPorSetorResponseDTO {
    private String setor;
    private List<ItemDTO> itens;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDTO{
        private String nomeProduto;
        private int quantidade;
    }
}
