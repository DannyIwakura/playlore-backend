package com.playrole.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.playrole.enums.EstadoPersonaje;
import com.playrole.enums.EstadoSolicitud;
import com.playrole.enums.RolUsuario;
import com.playrole.enums.TipoCategoria;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.SolicitudAmistad;
import com.playrole.model.Usuario;
import com.playrole.repository.CategoríaRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;
import com.playrole.repository.SolicitudAmistadRespositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.security.CustomUserDetails;
import com.playrole.utils.JwtUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegracionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepositoryInterface usuarioRepo;

	@Autowired
	private PerfilPersonajeRepositoryInterface personajeRepo;

	@Autowired
	private SolicitudAmistadRespositoryInterface amistadRepo;

	@Autowired
	private CategoríaRepositoryInterface categoriaRepo;

	@Autowired
	private PersonajeCategoriaRepositoryInterface personajeCategoriaRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtils jwtUtils;

	@BeforeEach
	void limpiarBase() {
		personajeCategoriaRepo.deleteAll();
		categoriaRepo.deleteAll();
		personajeRepo.deleteAll();
		amistadRepo.deleteAll();
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

	private String token(Usuario u) {
		return jwtUtils.generarToken(new CustomUserDetails(u));
	}

	private SolicitudAmistad crearSolicitud(Usuario emisor, Usuario receptor) {
		SolicitudAmistad s = new SolicitudAmistad();
		s.setEmisorId(emisor);
		s.setReceptorId(receptor);
		s.setEstado(EstadoSolicitud.PENDIENTE);
		s.setFechaPeticion(new Date());
		return amistadRepo.save(s);
	}

	private PerfilPersonaje crearPersonaje(Usuario propietario) {
		PerfilPersonaje p = new PerfilPersonaje();
		p.setNombre("Aragorn");
		p.setRaza("Dúnedain");
		p.setClase("Guerrero");
		p.setEstado(EstadoPersonaje.ACTIVO);
		p.setUserId(propietario);
		p.setFechaCreacion(new Date());
		return personajeRepo.save(p);
	}

	private Categoria crearCategoria(String nombre) {
		Categoria c = new Categoria();
		c.setNombre(nombre);
		c.setTipo(TipoCategoria.FANDOM);
		return categoriaRepo.save(c);
	}

	@Test
	void peticionSinToken_devuelve401ConJson() throws Exception {
		mockMvc.perform(get("/personajes"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void consultarPendientesRecibidasDeOtro_devuelve403() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);
		crearSolicitud(bob, alice);

		mockMvc.perform(get("/amistades/pendientes-recibidas/" + alice.getUserId())
						.header("Authorization", "Bearer " + token(bob)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error")
						.value("No puedes consultar las solicitudes de otro usuario"));
	}

	@Test
	void aceptarSolicitudDeOtro_devuelve403_yAceptarPropia_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);
		SolicitudAmistad s = crearSolicitud(alice, bob);

		mockMvc.perform(put("/amistades/aceptar/" + s.getId())
						.header("Authorization", "Bearer " + token(alice)))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/amistades/aceptar/" + s.getId())
						.header("Authorization", "Bearer " + token(bob)))
				.andExpect(status().isOk());

		SolicitudAmistad actualizada = amistadRepo.findById(s.getId()).orElseThrow();
		assertEquals(EstadoSolicitud.ACEPTADA, actualizada.getEstado());
	}

	@Test
	void rechazarSolicitudDeOtro_devuelve403_yRechazarPropia_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);
		SolicitudAmistad s = crearSolicitud(alice, bob);

		mockMvc.perform(put("/amistades/rechazar/" + s.getId())
						.header("Authorization", "Bearer " + token(alice)))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/amistades/rechazar/" + s.getId())
						.header("Authorization", "Bearer " + token(bob)))
				.andExpect(status().isOk());

		SolicitudAmistad actualizada = amistadRepo.findById(s.getId()).orElseThrow();
		assertEquals(EstadoSolicitud.RECHAZADA, actualizada.getEstado());
	}

	@Test
	void enviarSolicitud_ignoraEmisorDelBody() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);

		mockMvc.perform(post("/amistades")
						.header("Authorization", "Bearer " + token(alice))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"emisorId\": " + bob.getUserId() + ", \"receptorId\": " + bob.getUserId() + "}"))
				.andExpect(status().isOk());

		var enviadas = amistadRepo.findByEmisorIdUserIdAndEstado(alice.getUserId(), EstadoSolicitud.PENDIENTE);
		assertEquals(1, enviadas.size());
		assertEquals(alice.getUserId(), enviadas.get(0).getEmisorId().getUserId());
	}

	@Test
	void eliminarPersonajeDeOtro_devuelve403_yEliminarPropio_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);
		PerfilPersonaje p = crearPersonaje(alice);

		mockMvc.perform(delete("/personajes/" + p.getIdPersonaje())
						.header("Authorization", "Bearer " + token(bob)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/personajes/" + p.getIdPersonaje())
						.header("Authorization", "Bearer " + token(alice)))
				.andExpect(status().isOk());

		assertEquals(0, personajeRepo.count());
	}

	@Test
	void cambiarEstadoPersonaje_usuarioNormal_devuelve403_yModerador_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario mod = crearUsuario("moderador", RolUsuario.MOD);
		PerfilPersonaje p = crearPersonaje(alice);

		mockMvc.perform(put("/personajes/admin/" + p.getIdPersonaje() + "/estado")
						.header("Authorization", "Bearer " + token(alice))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"estado\":\"DESACTIVADO\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/personajes/admin/" + p.getIdPersonaje() + "/estado")
						.header("Authorization", "Bearer " + token(mod))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"estado\":\"DESACTIVADO\"}"))
				.andExpect(status().isOk());

		PerfilPersonaje actualizado = personajeRepo.findById(p.getIdPersonaje()).orElseThrow();
		assertEquals(EstadoPersonaje.DESACTIVADO, actualizado.getEstado());
	}

	@Test
	void cambiarStatusPersonajeDeOtro_devuelve403_yPropio_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);
		PerfilPersonaje p = crearPersonaje(alice);

		mockMvc.perform(put("/personajes/" + p.getIdPersonaje() + "/status")
						.header("Authorization", "Bearer " + token(bob))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"ocupado\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("No puedes cambiar el estado de un personaje que no es tuyo"));

		mockMvc.perform(put("/personajes/" + p.getIdPersonaje() + "/status")
						.header("Authorization", "Bearer " + token(alice))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"ocupado\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void personajeCategoria_relacionDeOtro_devuelve403_yPropia_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);
		PerfilPersonaje p = crearPersonaje(alice);
		Categoria cat = crearCategoria("Fantasia");

		mockMvc.perform(post("/personaje-categorias")
						.header("Authorization", "Bearer " + token(bob))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"idPersonaje\": " + p.getIdPersonaje() + ", \"idCategoria\": " + cat.getIdCategoria() + "}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("No puedes gestionar las categorías de un personaje que no es tuyo"));

		mockMvc.perform(post("/personaje-categorias")
						.header("Authorization", "Bearer " + token(alice))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"idPersonaje\": " + p.getIdPersonaje() + ", \"idCategoria\": " + cat.getIdCategoria() + "}"))
				.andExpect(status().isOk());
	}

	@Test
	void personajeCategoria_getInexistente_devuelve404ConJson() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);

		mockMvc.perform(get("/personaje-categorias/99999")
						.header("Authorization", "Bearer " + token(alice)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void eliminarCuentaDeOtroComoUsuarioNormal_devuelve403() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);
		Usuario bob = crearUsuario("bob", RolUsuario.USER);

		mockMvc.perform(delete("/usuarios/" + bob.getUserId())
						.header("Authorization", "Bearer " + token(alice)))
				.andExpect(status().isForbidden());
	}

	@Test
	void eliminarMiPropiaCuenta_conPasswordCorrecta_devuelve200() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);

		mockMvc.perform(delete("/usuarios/" + alice.getUserId())
						.header("Authorization", "Bearer " + token(alice))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"password\":\"Password123\"}"))
				.andExpect(status().isOk());

		assertEquals(0, usuarioRepo.count());
	}

	@Test
	void eliminarMiPropiaCuenta_sinPassword_devuelve400() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);

		mockMvc.perform(delete("/usuarios/" + alice.getUserId())
						.header("Authorization", "Bearer " + token(alice)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Debes introducir tu contraseña para eliminar la cuenta"));

		assertEquals(1, usuarioRepo.count());
	}

	@Test
	void eliminarMiPropiaCuenta_passwordIncorrecta_devuelve403() throws Exception {
		Usuario alice = crearUsuario("alice", RolUsuario.USER);

		mockMvc.perform(delete("/usuarios/" + alice.getUserId())
						.header("Authorization", "Bearer " + token(alice))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"password\":\"clave-equivocada\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("Contraseña incorrecta"));

		assertEquals(1, usuarioRepo.count());
	}
}
