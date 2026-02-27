package com.oficinadafesta.auth.service;

import com.oficinadafesta.auth.domain.Usuario;
import com.oficinadafesta.auth.dto.AlterarSenhaDTO;
import com.oficinadafesta.auth.dto.CriarUsuarioDTO;
import com.oficinadafesta.auth.dto.UsuarioResponseDTO;
import com.oficinadafesta.auth.dto.UsuarioUpdateDTO;
import com.oficinadafesta.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO criarUsuario(CriarUsuarioDTO dto) {
        if (usuarioRepository.existsByUsuario(dto.getUsuario())) {
            throw new IllegalArgumentException("Já existe usuário com esse nome.");
        }

        Usuario u = new Usuario();
        u.setUsuario(dto.getUsuario());
        u.setSenha(passwordEncoder.encode(dto.getSenha())); // ✅ HASH
        u.setSetor(dto.getSetor());

        Usuario salvo = usuarioRepository.save(u);
        return toResponse(salvo);
    }

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (dto.getUsuario() != null && !dto.getUsuario().isBlank()) {
            String novoUsuario = dto.getUsuario().trim();

            // evita duplicidade se mudou o username
            if (!novoUsuario.equalsIgnoreCase(u.getUsuario())
                    && usuarioRepository.existsByUsuario(novoUsuario)) {
                throw new IllegalArgumentException("Já existe usuário com esse nome.");
            }

            u.setUsuario(novoUsuario);
        }

        // setor é obrigatório no DTO (NotNull)
        u.setSetor(dto.getSetor());

        Usuario salvo = usuarioRepository.save(u);
        return toResponse(salvo);
    }

    public void alterarSenha(Long id, AlterarSenhaDTO dto) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        u.setSenha(passwordEncoder.encode(dto.getNovaSenha())); // ✅ HASH
        usuarioRepository.save(u);
    }

    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO toResponse(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getUsuario(),
                u.getSetor() == null ? null : u.getSetor().name()
        );
    }
}