package com.oficinadafesta.produto.service;

import com.oficinadafesta.produto.domain.CategoriaProduto;
import com.oficinadafesta.produto.repository.CategoriaProdutoRepository;
import com.oficinadafesta.shared.security.LoggedUser;
import com.oficinadafesta.shared.security.SecurityUtils;
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
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Salvando categoria: nome={}, setor={} | ator=userId:{} setor:{}",
                categoriaProduto.getNome(), categoriaProduto.getSetor(),
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");
        CategoriaProduto salva = categoriaProdutoRepository.save(categoriaProduto);
        log.info("Categoria salva com sucesso: id={}, nome={}", salva.getId(), salva.getNome());
        return salva;
    }

    public CategoriaProduto editar(Long id, CategoriaProduto dto) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Editando categoria: id={} | ator=userId:{} setor:{}",
                id,
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");

        CategoriaProduto categoria = categoriaProdutoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + id));

        categoria.setNome(dto.getNome());
        categoria.setSetor(dto.getSetor());

        CategoriaProduto salva = categoriaProdutoRepository.save(categoria);
        log.info("Categoria editada com sucesso: id={}, nome={}", salva.getId(), salva.getNome());
        return salva;
    }

    public void excluir(Long id) {
        LoggedUser ator = SecurityUtils.getLoggedUserOrNull();
        log.info("Excluindo categoria: id={} | ator=userId:{} setor:{}",
                id,
                ator != null ? ator.userId() : "anon",
                ator != null ? ator.setor() : "anon");

        if (!categoriaProdutoRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada: " + id);
        }

        categoriaProdutoRepository.deleteById(id);
        log.info("Categoria excluída com sucesso: id={}", id);
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