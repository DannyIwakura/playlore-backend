package com.playrole.chat.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class SessionJwtUtilsTest {

	private static final String SECRET = "clave-de-prueba-jjwt-0-13-con-al-menos-32-caracteres";
	private static final long EXPIRATION_MS = 86_400_000L;

	private final SessionJwtUtils sessionJwtUtils = new SessionJwtUtils(SECRET, EXPIRATION_MS);

	@Test
	void tokenDeSesion_roundTrip_conservaClaims() {
		String token = sessionJwtUtils.generarToken(3, 42, "MiPersonaje", "avatar.png", "MEMBER");

		assertTrue(sessionJwtUtils.validarToken(token));
		assertEquals(3, sessionJwtUtils.obtenerUsuarioId(token));
		assertEquals(42, sessionJwtUtils.obtenerPersonajeId(token));
		assertEquals("MiPersonaje", sessionJwtUtils.obtenerPersonajeNombre(token));
		assertEquals("avatar.png", sessionJwtUtils.obtenerPersonajeAvatar(token));
		assertEquals("42", sessionJwtUtils.obtenerClaims(token).getSubject());
	}

	@Test
	void tokenFirmadoConOtroSecret_seRechaza() {
		String token = Jwts.builder()
				.subject("42")
				.claim("personajeId", 42)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
				.signWith(Keys.hmacShaKeyFor("otra-clave-totalmente-distinta-2026-11".getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertFalse(sessionJwtUtils.validarToken(token));
	}

	@Test
	void tokenCaducado_seRechaza() {
		String token = Jwts.builder()
				.subject("42")
				.claim("personajeId", 42)
				.issuedAt(new Date(System.currentTimeMillis() - 2 * EXPIRATION_MS))
				.expiration(new Date(System.currentTimeMillis() - EXPIRATION_MS))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertFalse(sessionJwtUtils.validarToken(token));
	}
}
