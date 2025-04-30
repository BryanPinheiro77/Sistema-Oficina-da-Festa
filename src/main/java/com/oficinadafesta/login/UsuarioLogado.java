package com.oficinadafesta.login;

public class UsuarioLogado {
    private static Usuario usuarioAutenticado;

    public static void setUsuario(Usuario usuario) {
        usuarioAutenticado = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioAutenticado;
    }

    public static void logout() {
        usuarioAutenticado = null;
    }

    public static boolean isLogado() {
        return usuarioAutenticado != null;
    }
}
