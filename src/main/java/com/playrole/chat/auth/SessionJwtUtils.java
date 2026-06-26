package com.playrole.chat.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class SessionJwtUtils {

    private final SecretKey key;
    private final long jwtExpirationMs;

    public SessionJwtUtils(
            @Value("${jwt.secret:DefaultSecretKeyForSessions12345678901234567890}") String secret,
            @Value("${jwt.expiration:86400000}") long jwtExpirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generarToken(Integer usuarioId, Integer personajeId, String personajeNombre,
                                String personajeAvatar, String role) {
        return Jwts.builder()
                .setSubject(personajeId.toString())
                .claim("userId", usuarioId)
                .claim("personajeId", personajeId)
                .claim("personajeNombre", personajeNombre)
                .claim("personajeAvatar", personajeAvatar)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Integer obtenerPersonajeId(String token) {
        return obtenerClaims(token).get("personajeId", Integer.class);
    }

    public Integer obtenerUsuarioId(String token) {
        return obtenerClaims(token).get("userId", Integer.class);
    }

    public String obtenerPersonajeNombre(String token) {
        return obtenerClaims(token).get("personajeNombre", String.class);
    }

    public String obtenerPersonajeAvatar(String token) {
        return obtenerClaims(token).get("personajeAvatar", String.class);
    }
}
