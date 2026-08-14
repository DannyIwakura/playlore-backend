package com.playrole.chat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.playrole.chat.auth.SessionJwtUtils;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.enums.TipoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.model.SesionPersonaje;
import com.playrole.chat.repository.BaneoCanalRepository;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.chat.repository.MensajePrivadoPersonajeRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.chat.repository.SesionPersonajeRepository;
import com.playrole.chat.service.MensajePrivadoPersonajeService;
import com.playrole.enums.EstadoPersonaje;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.BadRequestException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CanalChatIntegracionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepositoryInterface usuarioRepo;

	@Autowired
	private PerfilPersonajeRepositoryInterface personajeRepo;

	@Autowired
	private CanalRepository canalRepo;

	@Autowired
	private MiembroCanalRepository miembroRepo;

	@Autowired
	private SesionPersonajeRepository sesionRepo;

	@Autowired
	private BaneoCanalRepository baneoCanalRepo;

	@Autowired
	private MensajeCanalRepository mensajeCanalRepo;

	@Autowired
	private MensajePrivadoPersonajeRepository mensajePrivadoRepo;

	@Autowired
	private MensajePrivadoPersonajeService dmService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private SessionJwtUtils sessionJwtUtils;

	@BeforeEach
	void limpiarBase() {
		mensajePrivadoRepo.deleteAll();
		mensajeCanalRepo.deleteAll();
		sesionRepo.deleteAll();
		baneoCanalRepo.deleteAll();
		miembroRepo.deleteAll();
		canalRepo.deleteAll();
		personajeRepo.deleteAll();
		usuarioRepo.deleteAll();
	}

	private Usuario crearUsuario(String nombre, RolUsuario rol) {
		Usuario u = new Usuario();
		u.setNombre(nombre);
		u.setEmail(nombre + "@test.com");
		u.setPassword(passwordEncoder.encode("Password123"));
		u.setRol(rol);
		u.setAvatar("avatar.png");
		u.setFechaRegistro(new Date());
		u.setUltimaConexion(new Date());
		return usuarioRepo.save(u);
	}

	private PerfilPersonaje crearPersonaje(Usuario propietario, String nombre) {
		PerfilPersonaje p = new PerfilPersonaje();
		p.setNombre(nombre);
		p.setRaza("Dúnedain");
		p.setClase("Guerrero");
		p.setEstado(EstadoPersonaje.ACTIVO);
		p.setUserId(propietario);
		p.setFechaCreacion(new Date());
		return personajeRepo.save(p);
	}

	private Canal crearCanal(PerfilPersonaje creador) {
		Canal c = new Canal();
		c.setNombre("Sala General");
		c.setDescripcion("Sala de pruebas");
		c.setTipo(TipoCanal.USER_CREATED);
		c.setPrivado(false);
		c.setVisible(true);
		c.setCreador(creador);
		c.setFechaCreacion(new Date());
		return canalRepo.save(c);
	}

	private void agregarMiembro(Canal canal, PerfilPersonaje personaje, RolCanal rol) {
		MiembroCanal m = new MiembroCanal();
		m.setCanal(canal);
		m.setPersonaje(personaje);
		m.setRol(rol);
		m.setFechaUnion(new Date());
		m.setInvitado(false);
		miembroRepo.save(m);
	}

	private String tokenSesion(Usuario usuario, PerfilPersonaje personaje) {
		String token = sessionJwtUtils.generarToken(
				usuario.getUserId(), personaje.getIdPersonaje(), personaje.getNombre(),
				personaje.getAvatar(), usuario.getRol().name());
		SesionPersonaje s = new SesionPersonaje();
		s.setUsuario(usuario);
		s.setPersonaje(personaje);
		s.setTokenJwt(token);
		s.setFechaInicio(new Date());
		s.setUltimaActividad(new Date());
		s.setActiva(true);
		sesionRepo.save(s);
		return token;
	}

	@Test
	void miembroSinPermiso_noPuedeSilenciar() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario miembro = crearUsuario("member", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pMiembro = crearPersonaje(miembro, "Boromir");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		agregarMiembro(canal, pMiembro, RolCanal.MEMBER);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pMiembro.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenSesion(miembro, pMiembro))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"1H\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void ownerSilencia_yDesilencia_aMiembro() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario miembro = crearUsuario("member", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pMiembro = crearPersonaje(miembro, "Boromir");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		agregarMiembro(canal, pMiembro, RolCanal.MEMBER);
		String tokenOwner = tokenSesion(owner, pOwner);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pMiembro.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenOwner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"1H\"}"))
				.andExpect(status().isOk());

		MiembroCanal silenciado = miembroRepo
				.findByCanalIdCanalAndPersonajeIdPersonaje(canal.getIdCanal(), pMiembro.getIdPersonaje())
				.orElseThrow();
		assertNotNull(silenciado.getSilenciadoHasta());
		assertTrue(silenciado.getSilenciadoHasta().after(new Date()));

		mockMvc.perform(get("/canales/" + canal.getIdCanal() + "/miembros")
						.header("Authorization", "Bearer " + tokenOwner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[1].personajeId").value(pMiembro.getIdPersonaje()))
				.andExpect(jsonPath("$.content[1].silenciadoHasta").exists());

		mockMvc.perform(delete("/canales/" + canal.getIdCanal() + "/miembros/" + pMiembro.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenOwner))
				.andExpect(status().isOk());

		MiembroCanal desilenciado = miembroRepo
				.findByCanalIdCanalAndPersonajeIdPersonaje(canal.getIdCanal(), pMiembro.getIdPersonaje())
				.orElseThrow();
		assertNull(desilenciado.getSilenciadoHasta());

		mockMvc.perform(get("/canales/" + canal.getIdCanal() + "/miembros")
						.header("Authorization", "Bearer " + tokenOwner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[1].personajeId").value(pMiembro.getIdPersonaje()))
				.andExpect(jsonPath("$.content[1].silenciadoHasta").value(nullValue()));
	}

	@Test
	void silenciado_noPuedeEnviar_yTrasDesilenciarSi() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario miembro = crearUsuario("member", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pMiembro = crearPersonaje(miembro, "Boromir");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		agregarMiembro(canal, pMiembro, RolCanal.MEMBER);
		String tokenOwner = tokenSesion(owner, pOwner);
		String tokenMiembro = tokenSesion(miembro, pMiembro);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pMiembro.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenOwner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"1H\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/mensajes")
						.header("Authorization", "Bearer " + tokenMiembro)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"contenido\":\"hola\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("silenciado")));

		mockMvc.perform(delete("/canales/" + canal.getIdCanal() + "/miembros/" + pMiembro.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenOwner))
				.andExpect(status().isOk());

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/mensajes")
						.header("Authorization", "Bearer " + tokenMiembro)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"contenido\":\"hola\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.personajeId").value(pMiembro.getIdPersonaje()))
				.andExpect(jsonPath("$.contenido").value("hola"));
	}

	@Test
	void modPuedeSilenciar_soloAMiembros() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario mod = crearUsuario("mod", RolUsuario.USER);
		Usuario miembro = crearUsuario("member", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pMod = crearPersonaje(mod, "Legolas");
		PerfilPersonaje pMiembro = crearPersonaje(miembro, "Boromir");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		agregarMiembro(canal, pMod, RolCanal.MOD);
		agregarMiembro(canal, pMiembro, RolCanal.MEMBER);
		String tokenMod = tokenSesion(mod, pMod);
		String tokenMiembro = tokenSesion(miembro, pMiembro);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pMiembro.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenMod)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"1H\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/mensajes")
						.header("Authorization", "Bearer " + tokenMiembro)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"contenido\":\"hola\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pOwner.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenMod)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"1H\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void mensajePrivado_entrePersonajes() {
		Usuario a = crearUsuario("usera", RolUsuario.USER);
		Usuario b = crearUsuario("userb", RolUsuario.USER);
		PerfilPersonaje pa = crearPersonaje(a, "Aragorn");
		PerfilPersonaje pb = crearPersonaje(b, "Boromir");

		dmService.enviarMensaje(pa.getIdPersonaje(), pb.getIdPersonaje(), "Hola Boromir");
		dmService.enviarMensaje(pb.getIdPersonaje(), pa.getIdPersonaje(), "Hola Aragorn");

		assertEquals(1, dmService.contarNoLeidos(pb.getIdPersonaje()));
		assertEquals(1, dmService.contarNoLeidos(pa.getIdPersonaje()));

		var conversacion = dmService.obtenerConversacion(pa.getIdPersonaje(), pb.getIdPersonaje(), pb.getIdPersonaje());
		assertEquals(2, conversacion.size());

		assertEquals(0, dmService.contarNoLeidos(pb.getIdPersonaje()));
	}

	@Test
	void mensajePrivado_noSePermiteAUnMismo() {
		Usuario a = crearUsuario("usera", RolUsuario.USER);
		PerfilPersonaje pa = crearPersonaje(a, "Aragorn");

		assertThrows(BadRequestException.class,
				() -> dmService.enviarMensaje(pa.getIdPersonaje(), pa.getIdPersonaje(), "a mi mismo"));
	}
}
