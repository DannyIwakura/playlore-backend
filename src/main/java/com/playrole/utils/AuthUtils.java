package com.playrole.utils;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.exception.AccessDeniedException;
import com.playrole.security.CustomUserDetails;
import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {}

    public static Integer obtenerUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario().getUserId();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario().getUserId();
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
