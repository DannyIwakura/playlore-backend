package com.playrole.security;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.playrole.utils.JwtUtils;

import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            //antes de inciciar sesion hace falta ver si el token es valido
            if (!jwtUtils.validarToken(token)) {
                logger.warn("Token JWT inválido para el request: " + request.getRequestURI());
            }
            
            if (jwtUtils.validarToken(token)) {

                // Los tokens de sesión de personaje (claim personajeId) los
                // procesa CharacterSessionFilter, este filtro solo autentica
                // el JWT de usuario.
                if (jwtUtils.esTokenSesionPersonaje(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                try {
                    String username = jwtUtils.obtenerUsername(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (UsernameNotFoundException | JwtException e) {
                    // Usuario inexistente o token no válido para el filtro de usuario:
                    // se deja pasar sin autenticación (lo resolverá CharacterSessionFilter o 401).
                    logger.warn("No se pudo autenticar el token de usuario: " + e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.equals("/api/usuarios/login");
    }
}
