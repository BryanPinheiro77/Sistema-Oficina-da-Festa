package com.oficinadafesta.produto.controller;

import com.oficinadafesta.produto.domain.CategoriaProduto;
import com.oficinadafesta.produto.service.CategoriaProdutoService;
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
@RequestMapping("/categorias-produto")
public class CategoriaProdutoController {

    private static final Logger log = LoggerFactory.getLogger(CategoriaProdutoController.class);

    private final CategoriaProdutoService categoriaProdutoService;

    public CategoriaProdutoController(CategoriaProdutoService categoriaProdutoService) {
        this.categoriaProdutoService = categoriaProdutoService;
    }

    @PreAuthorize(Roles.SOMENTE_ADMIN)
    @PostMapping
    public ResponseEntity<CategoriaProduto> criar(@Valid @RequestBody CategoriaProduto categoria) {
        log.info("POST /categorias-produto - criando categoria: nome={}, setor={}",
                categoria.getNome(), categoria.getSetor());

        CategoriaProduto salva = categoriaProdutoService.salvar(categoria);

        log.info("Categoria criada: id={}, nome={}", salva.getId(), salva.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @PreAuthorize(Roles.TODOS_SETORES)
    @GetMapping
    public ResponseEntity<List<CategoriaProduto>> listarTodas() {
        log.debug("GET /categorias-produto - listando categorias");
        List<CategoriaProduto> categorias = categoriaProdutoService.listarTodas();
        log.debug("GET /categorias-produto - total={}", categorias.size());
        return ResponseEntity.ok(categorias);
    }
}