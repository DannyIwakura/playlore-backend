package com.playrole.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.playrole.security.CustomUserDetails;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtUtils(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration:86400000}") long jwtExpirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret no configurado. Establece la variable JWT_SECRET");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    //generamos el token cuando se incia sesion
    public String generarToken(CustomUserDetails userDetails) {

        return Jwts.builder()
                .setSubject(userDetails.getUsuario().getNombre())
                .claim("id", userDetails.getUsuario().getUserId())
                .claim("role", userDetails.getUsuario().getRol().name())
                .claim("avatar", userDetails.getUsuario().getAvatar())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
    
    public String obtenerUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public String obtenerAvatar(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("avatar", String.class);
    }

    // Los tokens de sesión de personaje llevan el claim personajeId y
    // los procesa CharacterSessionFilter, no el filtro de usuario.
    public boolean esTokenSesionPersonaje(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .containsKey("personajeId");
        } catch (JwtException e) {
            return false;
        }
    }
    
    public boolean validarToken(String token) {

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }

    }

}
