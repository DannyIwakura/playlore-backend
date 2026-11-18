package com.playrole.chat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.playrole.chat.auth.SessionJwtUtils;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.enums.TipoCanal;
import com.playrole.chat.model.BaneoCanal;
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
import com.playrole.enums.AccionModeracion;
import com.playrole.enums.EstadoPersonaje;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.BadRequestException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.RegistroModeracion;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.RegistroModeracionRepository;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.security.CustomUserDetails;
import com.playrole.utils.JwtUtils;

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

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private RegistroModeracionRepository registroModeracionRepo;

	@BeforeEach
	void limpiarBase() {
		registroModeracionRepo.deleteAll();
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

	private String token(Usuario usuario) {
		return jwtUtils.generarToken(new CustomUserDetails(usuario));
	}

	private Canal crearCanalPrivado(PerfilPersonaje creador, String nombre) {
		Canal c = new Canal();
		c.setNombre(nombre);
		c.setDescripcion("Sala privada de pruebas");
		c.setTipo(TipoCanal.USER_CREATED);
		c.setPrivado(true);
		c.setVisible(true);
		c.setCreador(creador);
		c.setFechaCreacion(new Date());
		return canalRepo.save(c);
	}

	private Canal crearCanalOficial(PerfilPersonaje responsable, String nombre) {
		Canal c = new Canal();
		c.setNombre(nombre);
		c.setDescripcion("Canal oficial de pruebas");
		c.setTipo(TipoCanal.OFFICIAL);
		c.setPrivado(false);
		c.setVisible(true);
		c.setCreador(responsable);
		c.setFechaCreacion(new Date());
		return canalRepo.save(c);
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

	@Test
	void canalSuspendido_seOcultaYBloqueaAcciones_hastaQueVence() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario miembro = crearUsuario("member", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pMiembro = crearPersonaje(miembro, "Boromir");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		agregarMiembro(canal, pMiembro, RolCanal.MEMBER);
		String tokenMiembro = tokenSesion(miembro, pMiembro);

		canal.setSuspendido(true);
		canal.setSuspendidoHasta(new Date(System.currentTimeMillis() + 60_000));
		canal.setMotivoSuspension("Contenido ilegal");
		canalRepo.save(canal);

		mockMvc.perform(get("/canales")
						.header("Authorization", "Bearer " + tokenMiembro))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(get("/canales/unidos")
						.header("Authorization", "Bearer " + tokenMiembro))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(get("/canales/" + canal.getIdCanal())
						.header("Authorization", "Bearer " + tokenMiembro))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("suspendido")));

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/mensajes")
						.header("Authorization", "Bearer " + tokenMiembro)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"contenido\":\"hola\"}"))
				.andExpect(status().isForbidden());

		canal.setSuspendidoHasta(new Date(System.currentTimeMillis() - 60_000));
		canalRepo.save(canal);

		mockMvc.perform(get("/canales/" + canal.getIdCanal())
						.header("Authorization", "Bearer " + tokenMiembro))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(canal.getIdCanal()));
	}

	@Test
	void canalPrivado_noSeListaNiSeAccedeSinInvitacion() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario otro = crearUsuario("otro", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pOtro = crearPersonaje(otro, "Boromir");
		Canal canal = crearCanalPrivado(pOwner, "Sala Secreta");
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		String tokenOtro = tokenSesion(otro, pOtro);

		mockMvc.perform(get("/canales")
						.header("Authorization", "Bearer " + tokenOtro))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(get("/canales/disponibles")
						.header("Authorization", "Bearer " + tokenOtro))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(get("/canales/" + canal.getIdCanal())
						.header("Authorization", "Bearer " + tokenOtro))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("privado")));

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/unirse")
						.header("Authorization", "Bearer " + tokenOtro))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("privado")));
	}

	@Test
	void staffVeYEntraAPrivadoSinInvitacion_ySeAudita() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario admin = crearUsuario("admin", RolUsuario.ADMIN);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pAdmin = crearPersonaje(admin, "Gandalf");
		Canal canal = crearCanalPrivado(pOwner, "Sala Secreta");
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		String tokenAdmin = tokenSesion(admin, pAdmin);

		mockMvc.perform(get("/canales")
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(canal.getIdCanal()))
				.andExpect(jsonPath("$[0].privado").value(true));

		mockMvc.perform(get("/canales/disponibles")
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(canal.getIdCanal()));

		mockMvc.perform(get("/canales/" + canal.getIdCanal())
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(canal.getIdCanal()));

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/unirse")
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk());

		MiembroCanal miembro = miembroRepo
				.findByCanalIdCanalAndPersonajeIdPersonaje(canal.getIdCanal(), pAdmin.getIdPersonaje())
				.orElseThrow();
		assertEquals(RolCanal.MEMBER, miembro.getRol());
		assertFalse(miembro.isInvitado());

		List<RegistroModeracion> registros = registroModeracionRepo.findAll();
		assertEquals(1, registros.size());
		assertEquals(AccionModeracion.CANAL_ACCESO_MODERADOR, registros.get(0).getAccion());
		assertEquals(canal.getIdCanal(), registros.get(0).getObjetivoCanalId());
		assertEquals(admin.getUserId(), registros.get(0).getModeradorId());
	}

	@Test
	void staffConBaneoAntiguoPuedeEntrarAPrivado() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario mod = crearUsuario("mod", RolUsuario.MOD);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pMod = crearPersonaje(mod, "Legolas");
		Canal canal = crearCanalPrivado(pOwner, "Sala Secreta");
		agregarMiembro(canal, pOwner, RolCanal.OWNER);

		BaneoCanal baneo = new BaneoCanal();
		baneo.setCanal(canal);
		baneo.setPersonaje(pMod);
		baneo.setBaneadoPor(pOwner);
		baneo.setFechaBaneo(new Date());
		baneoCanalRepo.save(baneo);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/unirse")
						.header("Authorization", "Bearer " + tokenSesion(mod, pMod)))
				.andExpect(status().isOk());

		assertTrue(miembroRepo.existsByCanalIdCanalAndPersonajeIdPersonaje(
				canal.getIdCanal(), pMod.getIdPersonaje()));
	}

	@Test
	void staffQueSeUneAPublico_noGeneraAuditoria() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario admin = crearUsuario("admin", RolUsuario.ADMIN);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pAdmin = crearPersonaje(admin, "Gandalf");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/unirse")
						.header("Authorization", "Bearer " + tokenSesion(admin, pAdmin)))
				.andExpect(status().isOk());

		assertTrue(registroModeracionRepo.findAll().isEmpty());
	}

	@Test
	void noSePuedeBanearNiSilenciarAStaff() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario admin = crearUsuario("admin", RolUsuario.ADMIN);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pAdmin = crearPersonaje(admin, "Gandalf");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		agregarMiembro(canal, pAdmin, RolCanal.MEMBER);
		String tokenOwner = tokenSesion(owner, pOwner);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pAdmin.getIdPersonaje() + "/ban")
						.header("Authorization", "Bearer " + tokenOwner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"PERMANENTE\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("moderación")));

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/miembros/" + pAdmin.getIdPersonaje() + "/silenciar")
						.header("Authorization", "Bearer " + tokenOwner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracion\":\"1H\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("moderación")));

		assertTrue(baneoCanalRepo.findAll().isEmpty());
	}

	@Test
	void invitarRechazaPersonajeBaneado() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		Usuario invitado = crearUsuario("invitado", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		PerfilPersonaje pInvitado = crearPersonaje(invitado, "Boromir");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);

		BaneoCanal baneo = new BaneoCanal();
		baneo.setCanal(canal);
		baneo.setPersonaje(pInvitado);
		baneo.setBaneadoPor(pOwner);
		baneo.setFechaBaneo(new Date());
		baneoCanalRepo.save(baneo);

		mockMvc.perform(post("/canales/" + canal.getIdCanal() + "/invitar")
						.header("Authorization", "Bearer " + tokenSesion(owner, pOwner))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"personajeId\":" + pInvitado.getIdPersonaje() + "}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("baneado")));

		assertFalse(miembroRepo.existsByCanalIdCanalAndPersonajeIdPersonaje(
				canal.getIdCanal(), pInvitado.getIdPersonaje()));
	}

	@Test
	void adminCreaCanalOficialConResponsableYAudita() throws Exception {
		Usuario admin = crearUsuario("admin", RolUsuario.ADMIN);
		Usuario responsable = crearUsuario("responsable", RolUsuario.USER);
		PerfilPersonaje pResponsable = crearPersonaje(responsable, "Aragorn");
		String tokenAdmin = token(admin);

		String json = "{\"nombre\":\"Taberna Oficial\",\"descripcion\":\"Punto de encuentro\","
				+ "\"personajeResponsableId\":" + pResponsable.getIdPersonaje() + "}";
		MockMultipartFile datos = new MockMultipartFile(
				"datos", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart("/moderacion/canales/oficiales")
						.file(datos)
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("OFFICIAL"))
				.andExpect(jsonPath("$.privado").value(false))
				.andExpect(jsonPath("$.creadorId").value(pResponsable.getIdPersonaje()));

		Canal canal = canalRepo.findAll().get(0);
		assertEquals(TipoCanal.OFFICIAL, canal.getTipo());
		assertFalse(canal.isPrivado());
		assertEquals(RolCanal.OWNER, miembroRepo
				.findByCanalIdCanalAndPersonajeIdPersonaje(canal.getIdCanal(), pResponsable.getIdPersonaje())
				.orElseThrow().getRol());

		List<RegistroModeracion> registros = registroModeracionRepo.findAll();
		assertEquals(1, registros.size());
		assertEquals(AccionModeracion.CANAL_OFICIAL_CREADO, registros.get(0).getAccion());
	}

	@Test
	void adminMarcaYDesmarcaOficial_forzandoPublico() throws Exception {
		Usuario admin = crearUsuario("admin", RolUsuario.ADMIN);
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		Canal canal = crearCanalPrivado(pOwner, "Sala Secreta");
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		String tokenAdmin = token(admin);

		mockMvc.perform(post("/moderacion/canales/" + canal.getIdCanal() + "/oficial")
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("OFFICIAL"))
				.andExpect(jsonPath("$.privado").value(false));

		Canal marcado = canalRepo.findById(canal.getIdCanal()).orElseThrow();
		assertEquals(TipoCanal.OFFICIAL, marcado.getTipo());
		assertFalse(marcado.isPrivado());

		mockMvc.perform(delete("/moderacion/canales/" + canal.getIdCanal() + "/oficial")
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("USER_CREATED"))
				.andExpect(jsonPath("$.privado").value(false));

		Canal desmarcado = canalRepo.findById(canal.getIdCanal()).orElseThrow();
		assertEquals(TipoCanal.USER_CREATED, desmarcado.getTipo());
		assertFalse(desmarcado.isPrivado());
		assertEquals(2, registroModeracionRepo.findAll().size());
	}

	@Test
	void canalOficial_esSiemprePublicoYNoSePuedeEliminar() throws Exception {
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		Canal canal = crearCanalOficial(pOwner, "Taberna Oficial");
		agregarMiembro(canal, pOwner, RolCanal.OWNER);
		String tokenOwner = tokenSesion(owner, pOwner);

		mockMvc.perform(put("/canales/" + canal.getIdCanal())
						.header("Authorization", "Bearer " + tokenOwner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\":\"Taberna Oficial\",\"descripcion\":\"x\",\"privado\":true}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(containsString("oficial")));

		mockMvc.perform(delete("/canales/" + canal.getIdCanal())
						.header("Authorization", "Bearer " + tokenOwner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"password\":\"Password123\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value(containsString("oficial")));

		assertTrue(canalRepo.existsById(canal.getIdCanal()));
	}

	@Test
	void modNoPuedeMarcarCanalOficial() throws Exception {
		Usuario mod = crearUsuario("mod", RolUsuario.MOD);
		Usuario owner = crearUsuario("owner", RolUsuario.USER);
		PerfilPersonaje pOwner = crearPersonaje(owner, "Aragorn");
		Canal canal = crearCanal(pOwner);
		agregarMiembro(canal, pOwner, RolCanal.OWNER);

		mockMvc.perform(post("/moderacion/canales/" + canal.getIdCanal() + "/oficial")
						.header("Authorization", "Bearer " + token(mod)))
				.andExpect(status().isForbidden());

		assertEquals(TipoCanal.USER_CREATED,
				canalRepo.findById(canal.getIdCanal()).orElseThrow().getTipo());
	}
}
