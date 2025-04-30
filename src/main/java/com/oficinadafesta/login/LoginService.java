package com.oficinadafesta.login;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.oficinadafesta.login.Usuario;
import com.oficinadafesta.enums.AreaTipo;

@Service
public class LoginService {

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
