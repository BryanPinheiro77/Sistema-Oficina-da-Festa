package com.oficinadafesta.produto.service;

import com.oficinadafesta.produto.domain.CategoriaProduto;
import com.oficinadafesta.produto.domain.Produto;
import com.oficinadafesta.produto.dto.ProdutoCreateDTO;
import com.oficinadafesta.produto.repository.CategoriaProdutoRepository;
import com.oficinadafesta.produto.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private static final Logger log = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository produtoRepository;
    private final CategoriaProdutoRepository categoriaProdutoRepository;

    public ProdutoService(ProdutoRepository produtoRepository,
                          CategoriaProdutoRepository categoriaProdutoRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaProdutoRepository = categoriaProdutoRepository;
    }

    public Produto criar(ProdutoCreateDTO dto) {
        log.info("Criando produto: nome={}, preco={}, setor={}, categoriaId={}",
                dto.nome(), dto.preco(), dto.setor(), dto.categoriaId());

        CategoriaProduto categoria = categoriaProdutoRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + dto.categoriaId()));

        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setSetor(dto.setor());
        produto.setCategoria(categoria);

        Produto salvo = produtoRepository.save(produto);

        log.info("Produto criado com sucesso: id={}, nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    public List<Produto> listarTodos() {
        log.debug("Listando todos os produtos");
        List<Produto> produtos = produtoRepository.findAll();
        log.debug("Total de produtos encontrados: {}", produtos.size());
        return produtos;
    }
}