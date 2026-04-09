package com.playrole.utils;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.playrole.security.CustomUserDetails;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private final SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    //tiempo en el que expirara el token, en este caso 24h
    private final long jwtExpirationMs = 86400000;
    
    //generamos el token cuando se incia sesion
    public String generarToken(CustomUserDetails userDetails) {

        return Jwts.builder()
                .setSubject(userDetails.getUsuario().getNombre())
                .claim("id", userDetails.getUsuario().getUserId())
                .claim("role", userDetails.getUsuario().getRol().name())
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
    
    public boolean validarToken(String token) {

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }

    }

}