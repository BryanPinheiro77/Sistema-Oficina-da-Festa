package com.oficinadafesta.auth.service;

import com.oficinadafesta.auth.domain.Usuario;
import com.oficinadafesta.auth.dto.CriarUsuarioDTO;
import com.oficinadafesta.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario criarUsuario(CriarUsuarioDTO dto) {
        if (usuarioRepository.existsByUsuario(dto.getUsuario())) {
            throw new IllegalArgumentException("Já existe usuário com esse nome.");
        }

        Usuario u = new Usuario();
        u.setUsuario(dto.getUsuario());
        u.setSenha(passwordEncoder.encode(dto.getSenha())); // ✅ HASH
        u.setSetor(dto.getSetor());

        return usuarioRepository.save(u);
    }
}