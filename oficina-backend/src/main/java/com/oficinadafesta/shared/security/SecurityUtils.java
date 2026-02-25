package com.oficinadafesta.shared.security;

import com.oficinadafesta.enums.AreaTipo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
private SecurityUtils() {}

    public static Authentication auth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean hasRole(Authentication auth, String role){
    if(auth == null) return false;
        String authority = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }

    public static boolean isAdmin(Authentication auth){
        return hasRole(auth, "ADMIN");
    }

    public static boolean isCaixa(Authentication auth){
        return hasRole(auth, "CAIXA");
    }

    public static boolean isCafe(Authentication auth) {
        return hasRole(auth, "CAFE");
    }

    public static boolean canSeeAll(Authentication auth) {
        return isAdmin(auth) || isCaixa(auth) || isCafe(auth);
    }

    /**
     * Detecta o setor do usuário baseado na ROLE_<SETOR>.
     * Ex.: ROLE_CONFEITARIA -> AreaTipo.CONFEITARIA
     * Ignora roles que não são setores (ex.: ADMIN).
     */
    public static AreaTipo getSetor(Authentication auth){

        if(auth == null) return null;

        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.startsWith("ROLE_") && !role.equals("ROLE_ADMIN")) {
                String setorStr = role.substring(5); // remove "ROLE_"
                try {
                    return AreaTipo.valueOf(setorStr);
                } catch (IllegalArgumentException ignored) {
                    // Role não corresponde a um setor válido, ignora
                }
            }
        }
        return null; // Sem setor encontrado
    }
}
