package com.oficinadafesta.produto.service;

import com.oficinadafesta.produto.domain.CategoriaProduto;
import com.oficinadafesta.produto.repository.CategoriaProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaProdutoService {

    private final CategoriaProdutoRepository categoriaProdutoRepository;

    public CategoriaProdutoService(CategoriaProdutoRepository categoriaProdutoRepository) {
        this.categoriaProdutoRepository = categoriaProdutoRepository;
    }

    public CategoriaProduto salvar(CategoriaProduto categoriaProduto) {
        return categoriaProdutoRepository.save(categoriaProduto);
    }

    public List<CategoriaProduto> listarTodas() {
        return categoriaProdutoRepository.findAll();
    }
}
