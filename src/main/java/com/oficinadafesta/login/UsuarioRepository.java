package com.oficinadafesta.login;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsuarioAndSenha(String usuario, String senha);
}
