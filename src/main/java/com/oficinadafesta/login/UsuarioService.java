package com.oficinadafesta.login;

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
