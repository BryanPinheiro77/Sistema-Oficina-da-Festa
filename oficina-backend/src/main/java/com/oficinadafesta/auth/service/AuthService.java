package com.oficinadafesta.auth.service;

import com.oficinadafesta.auth.domain.Usuario;
import com.oficinadafesta.auth.dto.AuthResponseDTO;
import com.oficinadafesta.auth.repository.UsuarioRepository;
import com.oficinadafesta.shared.exception.UnauthorizedException;
import com.oficinadafesta.shared.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO login(String usuario, String senha) {
        Usuario user = usuarioRepository.findByUsuario(usuario).orElse(null);
        if (user == null) throw new UnauthorizedException("Usuário ou senha inválidos");

        if (!passwordEncoder.matches(senha, user.getSenha())) {
            throw new UnauthorizedException("Usuário ou senha inválidos");
        }

        String token = jwtService.gerarToken(
                user.getId(),
                user.getUsuario(),
                user.getSetor().name()
        );

        return new AuthResponseDTO(
                token,
                jwtService.getExpiresInSeconds(),
                user.getSetor().name()
        );
    }
}