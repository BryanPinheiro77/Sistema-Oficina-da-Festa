package com.oficinadafesta.produto;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaProdutoController {

    private final CategoriaProdutoService categoriaProdutoService;

    public CategoriaProdutoController(CategoriaProdutoService categoriaProdutoService) {
        this.categoriaProdutoService = categoriaProdutoService;
    }

    @PostMapping
    public CategoriaProduto criar(@RequestBody CategoriaProduto categoriaProduto) {
        return categoriaProdutoService.salvar(categoriaProduto);
    }

    @GetMapping
    public List<CategoriaProduto> listar() {
        return categoriaProdutoService.listarTodas();
    }
}
