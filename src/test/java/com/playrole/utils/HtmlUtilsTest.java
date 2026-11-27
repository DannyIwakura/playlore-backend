package com.playrole.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HtmlUtilsTest {

	@Test
	void eliminaScriptsPeroConservaContenido() {
		String resultado = HtmlUtils.sanitize("<script>alert('xss')</script><p>Hola</p>");

		assertFalse(resultado.toLowerCase().contains("<script"));
		assertFalse(resultado.toLowerCase().contains("alert("));
		assertTrue(resultado.contains("Hola"));
	}

	@Test
	void eliminaAtributosDeEvento() {
		String resultado = HtmlUtils.sanitize("<img src=\"https://ejemplo.com/a.png\" onerror=\"alert(1)\">");

		assertFalse(resultado.toLowerCase().contains("onerror"));
		assertTrue(resultado.contains("a.png"));
	}

	@Test
	void eliminaProtocolosPeligrososEnEnlaces() {
		String resultado = HtmlUtils.sanitize("<a href=\"javascript:alert(1)\">pulsa</a>");

		assertFalse(resultado.toLowerCase().contains("javascript:"));
		assertTrue(resultado.contains("pulsa"));
	}

	@Test
	void convierteRgbAHexEnEstilosPermitidos() {
		String resultado = HtmlUtils.sanitize("<span style=\"color: rgb(255, 0, 0)\">texto</span>");

		assertTrue(resultado.contains("#ff0000"));
		assertFalse(resultado.toLowerCase().contains("rgb("));
	}

	@Test
	void conservaEtiquetasPermitidas() {
		String resultado = HtmlUtils.sanitize("<p><strong>negrita</strong> y <em>cursiva</em></p>");

		assertTrue(resultado.contains("<strong>negrita</strong>"));
		assertTrue(resultado.contains("<em>cursiva</em>"));
	}

	@Test
	void entradaNula_devuelveNulo() {
		assertNull(HtmlUtils.sanitize(null));
	}

	@Test
	void cadenaVacia_devuelveVacia() {
		assertEquals("", HtmlUtils.sanitize(""));
	}
}
