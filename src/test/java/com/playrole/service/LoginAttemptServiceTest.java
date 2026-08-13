package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

	private final LoginAttemptService service = new LoginAttemptService();

	@Test
	void conMenosDeCincoFallos_noBloquea() {
		service.registrarIntentoFallido("ip:1.2.3.4");
		service.registrarIntentoFallido("ip:1.2.3.4");
		service.registrarIntentoFallido("ip:1.2.3.4");
		service.registrarIntentoFallido("ip:1.2.3.4");

		assertFalse(service.estaBloqueado("ip:1.2.3.4"));
	}

	@Test
	void conCincoFallos_bloquea() {
		registrarFallos("ip:1.2.3.4", 5);

		assertTrue(service.estaBloqueado("ip:1.2.3.4"));
	}

	@Test
	void clavesDistintas_sonIndependientes() {
		registrarFallos("ip:malo", 5);

		assertTrue(service.estaBloqueado("ip:malo"));
		assertFalse(service.estaBloqueado("ip:bueno"));
	}

	@Test
	void limpiar_desbloquea() {
		registrarFallos("ip:1.2.3.4", 5);
		service.limpiar("ip:1.2.3.4");

		assertFalse(service.estaBloqueado("ip:1.2.3.4"));
	}

	private void registrarFallos(String key, int veces) {
		for (int i = 0; i < veces; i++) {
			service.registrarIntentoFallido(key);
		}
	}
}
