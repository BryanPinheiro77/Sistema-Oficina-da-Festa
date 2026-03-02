package com.oficinadafesta.shared.security;

import com.oficinadafesta.enums.AreaTipo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    // ─────────────────────── LoggedUser ───────────────────────

    public static LoggedUser getLoggedUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof LoggedUser u) return u;
        return null;
    }

    public static LoggedUser requireLoggedUser() {
        LoggedUser u = getLoggedUserOrNull();
        if (u == null) throw new IllegalStateException("Usuário não autenticado no contexto.");
        return u;
    }

    public static Long requireUserId() { return requireLoggedUser().userId(); }
    public static String requireUsername() { return requireLoggedUser().username(); }
    public static String requireSetor() { return requireLoggedUser().setor(); }

    // ─────────────────────── Roles / Permissões ───────────────────────────

    public static Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + role).equals(a.getAuthority()));
    }

    public static boolean isAdmin(Authentication auth)  { return hasRole(auth, "ADMIN"); }
    public static boolean isCaixa(Authentication auth)  { return hasRole(auth, "CAIXA"); }
    public static boolean isCafe(Authentication auth)   { return hasRole(auth, "CAFE"); }

    public static boolean canSeeAll(Authentication auth) {
        return isAdmin(auth) || isCaixa(auth) || isCafe(auth);
    }

    public static AreaTipo getSetor(Authentication auth) {
        if (auth == null) return null;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.startsWith("ROLE_") && !role.equals("ROLE_ADMIN")) {
                try {
                    return AreaTipo.valueOf(role.substring(5));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }
}