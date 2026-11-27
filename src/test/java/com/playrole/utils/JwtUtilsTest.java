package com.playrole.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.playrole.enums.RolUsuario;
import com.playrole.model.Usuario;
import com.playrole.security.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;

class JwtUtilsTest {

	private static final String SECRET = "clave-de-prueba-jjwt-0-13-con-al-menos-32-caracteres";
	private static final long EXPIRATION_MS = 86_400_000L;

	private final JwtUtils jwtUtils = new JwtUtils(SECRET, EXPIRATION_MS);

	@Test
	void tokenDeUsuario_roundTrip_conservaSubjectYAavatar() {
		Usuario usuario = new Usuario();
		usuario.setUserId(7);
		usuario.setNombre("Arturito");
		usuario.setAvatar("avatar.png");
		usuario.setRol(RolUsuario.USER);

		String token = jwtUtils.generarToken(new CustomUserDetails(usuario));

		assertTrue(jwtUtils.validarToken(token));
		assertEquals("Arturito", jwtUtils.obtenerUsername(token));
		assertEquals("avatar.png", jwtUtils.obtenerAvatar(token));
		assertFalse(jwtUtils.esTokenSesionPersonaje(token));
	}

	@Test
	void tokenDeSesion_firmadoConElMismoSecret_seReconoceComoPersonaje() {
		String token = Jwts.builder()
				.subject("42")
				.claim("personajeId", 42)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertTrue(jwtUtils.validarToken(token));
		assertTrue(jwtUtils.esTokenSesionPersonaje(token));
	}

	@Test
	void tokenFirmadoConOtroSecret_seRechaza() {
		String token = Jwts.builder()
				.subject("intruso")
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
				.signWith(Keys.hmacShaKeyFor("otra-clave-totalmente-distinta-2026-11".getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertFalse(jwtUtils.validarToken(token));
		assertFalse(jwtUtils.esTokenSesionPersonaje(token));
	}

	@Test
	void tokenCaducado_seRechaza() {
		String token = Jwts.builder()
				.subject("caducado")
				.issuedAt(new Date(System.currentTimeMillis() - 2 * EXPIRATION_MS))
				.expiration(new Date(System.currentTimeMillis() - EXPIRATION_MS))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertFalse(jwtUtils.validarToken(token));
	}
}
