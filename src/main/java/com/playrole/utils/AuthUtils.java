package com.playrole.utils;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.exception.AccessDeniedException;
import com.playrole.model.Usuario;
import com.playrole.security.CustomUserDetails;
import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {}

    public static Integer obtenerUserId(Authentication authentication) {
        return obtenerUsuario(authentication).getUserId();
    }

    public static Usuario obtenerUsuario(Authentication authentication) {
        if (authentication == null) throw new AccessDeniedException("No autenticado");
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario();
        throw new AccessDeniedException("No autenticado");
    }

    public static boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public static boolean esModerador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                            || "ROLE_MOD".equals(a.getAuthority()));
    }
}
