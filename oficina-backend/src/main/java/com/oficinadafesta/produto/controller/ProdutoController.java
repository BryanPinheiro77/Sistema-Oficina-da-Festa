package com.oficinadafesta.produto.controller;


import com.oficinadafesta.produto.domain.Produto;
import com.oficinadafesta.produto.service.ProdutoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService){
        this.produtoService = produtoService;
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto){
        return produtoService.salvar(produto);
    }

    @GetMapping
    public List<Produto> listarTodos(){
        return produtoService.listarTodos();
    }
}
