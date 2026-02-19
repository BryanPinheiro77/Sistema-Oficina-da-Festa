package com.oficinadafesta.auth.service;


import com.oficinadafesta.auth.domain.Usuario;
import com.oficinadafesta.auth.repository.UsuarioRepository;
import com.oficinadafesta.login.UsuarioLogado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.oficinadafesta.enums.AreaTipo;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public AreaTipo autenticar(String usuario, String senha){
        Usuario user = usuarioRepository.findByUsuarioAndSenha(usuario,senha);

        if (user != null){
            // Armazena o usuário logado
            UsuarioLogado.setUsuario(user);
            return user.getSetor();
        }
        return null;
    }
}
