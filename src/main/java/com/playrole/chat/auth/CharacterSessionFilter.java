package com.playrole.chat.auth;

import com.playrole.chat.model.SesionPersonaje;
import com.playrole.chat.repository.SesionPersonajeRepository;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
@Order(1)
public class CharacterSessionFilter extends OncePerRequestFilter {

    private final SessionJwtUtils sessionJwtUtils;
    private final SesionPersonajeRepository sesionRepository;

    public CharacterSessionFilter(SessionJwtUtils sessionJwtUtils, SesionPersonajeRepository sesionRepository) {
        this.sessionJwtUtils = sessionJwtUtils;
        this.sesionRepository = sesionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (sessionJwtUtils.validarToken(token)) {
                Claims claims = sessionJwtUtils.obtenerClaims(token);

                if (claims.containsKey("personajeId")) {
                    Integer personajeId = claims.get("personajeId", Integer.class);
                    Integer usuarioId = claims.get("userId", Integer.class);

                    SesionPersonaje sesion = sesionRepository.findByTokenJwtAndActivaTrue(token).orElse(null);

                    if (sesion != null) {
                        sesion.setUltimaActividad(new Date());
                        sesionRepository.save(sesion);

                        Usuario usuario = sesion.getUsuario();
                        PerfilPersonaje personaje = sesion.getPersonaje();

                        CharacterSessionPrincipal principal = new CharacterSessionPrincipal(
                                usuario, personaje, sesion.getIdSesion(), token);

                        String role = "ROLE_" + usuario.getRol().name();
                        CharacterSessionAuthenticationToken auth =
                                new CharacterSessionAuthenticationToken(principal,
                                        List.of(new SimpleGrantedAuthority(role)));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/usuarios/login")
                || path.startsWith("/api/ws")
                || path.startsWith("/api/images/")
                || path.startsWith("/api/uploads/")
                || path.equals("/api/personajes/sesion/iniciar");
    }
}
