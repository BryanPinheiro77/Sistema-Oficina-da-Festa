package com.oficinadafesta.produto.service;

import com.oficinadafesta.produto.domain.CategoriaProduto;
import com.oficinadafesta.produto.domain.Produto;
import com.oficinadafesta.produto.dto.ProdutoCreateDTO;
import com.oficinadafesta.produto.repository.CategoriaProdutoRepository;
import com.oficinadafesta.produto.repository.ProdutoRepository;
import com.oficinadafesta.shared.security.LoggedUser;
import com.oficinadafesta.shared.security.SecurityUtils;
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
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Criando produto: nome={}, preco={}, setor={}, categoriaId={} | ator=userId:{} setor:{}",
                dto.nome(), dto.preco(), dto.setor(), dto.categoriaId(),
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");

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

    public Produto editar(Long id, ProdutoCreateDTO dto) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Editando produto: id={} | ator=userId:{} setor:{}",
                id,
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        CategoriaProduto categoria = categoriaProdutoRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + dto.categoriaId()));

        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setSetor(dto.setor());
        produto.setCategoria(categoria);

        Produto salvo = produtoRepository.save(produto);
        log.info("Produto editado com sucesso: id={}, nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    public void excluir(Long id) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Excluindo produto: id={} | ator=userId:{} setor:{}",
                id,
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");

        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado: " + id);
        }

        produtoRepository.deleteById(id);
        log.info("Produto excluído com sucesso: id={}", id);
    }

    public List<Produto> listarTodos() {
        log.debug("Listando todos os produtos");
        List<Produto> produtos = produtoRepository.findAll();
        log.debug("Total de produtos encontrados: {}", produtos.size());
        return produtos;
    }
}