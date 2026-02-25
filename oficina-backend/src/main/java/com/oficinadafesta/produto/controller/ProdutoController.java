package com.oficinadafesta.produto.controller;

import com.oficinadafesta.produto.domain.Produto;
import com.oficinadafesta.produto.dto.ProdutoCreateDTO;
import com.oficinadafesta.produto.service.ProdutoService;
import com.oficinadafesta.shared.security.Roles;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoController.class);

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PreAuthorize(Roles.SOMENTE_ADMIN)
    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody ProdutoCreateDTO dto) {
        log.info("POST /produtos - criando produto: nome={}, preco={}, setor={}, categoriaId={}",
                dto.nome(), dto.preco(), dto.setor(), dto.categoriaId());

        Produto salvo = produtoService.criar(dto);

        log.info("Produto criado: id={}, nome={}", salvo.getId(), salvo.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PreAuthorize(Roles.TODOS_SETORES)
    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarTodos());
    }
}