package com.oficinadafesta.auth.service;

import com.oficinadafesta.auth.domain.Usuario;
import com.oficinadafesta.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario validarLogin(String usuario, String senha){
        return usuarioRepository.findByUsuarioAndSenha(usuario, senha);
    }
}
