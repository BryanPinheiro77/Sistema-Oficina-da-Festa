package com.oficinadafesta.produto.service;

import com.oficinadafesta.produto.domain.CategoriaProduto;
import com.oficinadafesta.produto.repository.CategoriaProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaProdutoService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaProdutoService.class);

    private final CategoriaProdutoRepository categoriaProdutoRepository;

    public CategoriaProdutoService(CategoriaProdutoRepository categoriaProdutoRepository) {
        this.categoriaProdutoRepository = categoriaProdutoRepository;
    }

    public CategoriaProduto salvar(CategoriaProduto categoriaProduto) {
        log.info("Salvando categoria: nome={}, setor={}", categoriaProduto.getNome(), categoriaProduto.getSetor());
        CategoriaProduto salva = categoriaProdutoRepository.save(categoriaProduto);
        log.info("Categoria salva com sucesso: id={}, nome={}", salva.getId(), salva.getNome());
        return salva;
    }

    public List<CategoriaProduto> listarTodas() {
        log.debug("Listando todas as categorias");
        List<CategoriaProduto> categorias = categoriaProdutoRepository.findAll();
        log.debug("Total de categorias encontradas: {}", categorias.size());
        return categorias;
    }

    public CategoriaProduto buscarPorId(Long id) {
        log.debug("Buscando categoria por id={}", id);
        return categoriaProdutoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + id));
    }
}