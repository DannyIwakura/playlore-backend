package com.playrole.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.playrole.chat.service.PresenceService;
import com.playrole.enums.EstadoPersonaje;
import com.playrole.enums.RolUsuario;
import com.playrole.enums.TipoCategoria;
import com.playrole.model.Anuncio;
import com.playrole.model.AnuncioCategoria;
import com.playrole.model.BaneoGlobal;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.AnuncioCategoriaRepository;
import com.playrole.repository.AnuncioRepository;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.CategoríaRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.security.CustomUserDetails;
import com.playrole.utils.JwtUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnuncioIntegracionTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepositoryInterface usuarioRepo;
    @Autowired
    private PerfilPersonajeRepositoryInterface personajeRepo;
    @Autowired
    private CategoríaRepositoryInterface categoriaRepo;
    @Autowired
    private PersonajeCategoriaRepositoryInterface personajeCategoriaRepo;
    @Autowired
    private AnuncioRepository anuncioRepo;
    @Autowired
    private AnuncioCategoriaRepository anuncioCategoriaRepo;
    @Autowired
    private BaneoGlobalRepository baneoRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    /** La presencia es en memoria; en los tests la controlamos para ser deterministas. */
    @MockitoBean
    private PresenceService presenceService;

    @BeforeEach
    void limpiarBase() {
        anuncioCategoriaRepo.deleteAll();
        anuncioRepo.deleteAll();
        personajeCategoriaRepo.deleteAll();
        baneoRepo.deleteAll();
        personajeRepo.deleteAll();
        categoriaRepo.deleteAll();
        usuarioRepo.deleteAll();

        reset(presenceService);
        when(presenceService.getOnlineStatusForCharacters(anySet()))
                .thenAnswer(inv -> {
                    Set<Integer> ids = inv.getArgument(0);
                    return ids.stream().collect(Collectors.toMap(id -> id, id -> true));
                });
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

    private PerfilPersonaje crearPersonaje(Usuario owner, String nombre, EstadoPersonaje estado,
            String genero, String raza, String clase, Integer edad) {
        PerfilPersonaje p = new PerfilPersonaje();
        p.setNombre(nombre);
        p.setGenero(genero);
        p.setRaza(raza);
        p.setClase(clase);
        p.setEdadPersonaje(edad);
        p.setEstado(estado);
        p.setAvatar("/images/avatar.png");
        p.setUserId(owner);
        p.setFechaCreacion(new Date());
        return personajeRepo.save(p);
    }

    private Categoria crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setTipo(TipoCategoria.FANDOM);
        return categoriaRepo.save(c);
    }

    private Anuncio crearAnuncio(PerfilPersonaje p, String contenido, boolean activo,
            Categoria... categorias) {
        Anuncio a = new Anuncio();
        a.setContenido(contenido);
        a.setActivo(activo);
        a.setPersonaje(p);
        a.setFechaCreacion(new Date());
        a.setFechaModificacion(new Date());
        Anuncio guardado = anuncioRepo.save(a);
        for (Categoria categoria : categorias) {
            AnuncioCategoria relacion = new AnuncioCategoria();
            relacion.setAnuncio(guardado);
            relacion.setIdCategoria(categoria);
            relacion.setFechaAdicion(new Date());
            anuncioCategoriaRepo.save(relacion);
        }
        return guardado;
    }

    private void banearCuenta(Usuario objetivo, Usuario moderador) {
        BaneoGlobal baneo = new BaneoGlobal();
        baneo.setUsuario(objetivo);
        baneo.setBaneadoPor(moderador);
        baneo.setFechaBaneo(new Date());
        baneoRepo.save(baneo);
    }

    private void banearPersonaje(PerfilPersonaje objetivo, Usuario moderador) {
        BaneoGlobal baneo = new BaneoGlobal();
        baneo.setPersonaje(objetivo);
        baneo.setBaneadoPor(moderador);
        baneo.setFechaBaneo(new Date());
        baneoRepo.save(baneo);
    }

    @Test
    void listarSinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/anuncios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void crear_conJwtDeUsuario_naceInactivoSanitizaYGuardaCategorias() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");

        mockMvc.perform(post("/anuncios")
                        .header("Authorization", "Bearer " + token(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"<p>Buscando grupo</p><script>alert(1)</script>\","
                                + "\"categoriaIds\":[" + categoria.getIdCategoria() + "],"
                                + "\"personajeId\":" + p.getIdPersonaje() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.contenido", containsString("<p>Buscando grupo</p>")))
                .andExpect(jsonPath("$.contenido", not(containsString("<script"))))
                .andExpect(jsonPath("$.personajeId").value(p.getIdPersonaje()))
                .andExpect(jsonPath("$.usuarioId").value(owner.getUserId()))
                .andExpect(jsonPath("$.categorias[0].idCategoria").value(categoria.getIdCategoria()))
                .andExpect(jsonPath("$.categorias[0].nombre").value("Fantasía"));

        assertEquals(1, anuncioRepo.count());
        assertEquals(1, anuncioCategoriaRepo.count());
        assertEquals(false, anuncioRepo.findAll().get(0).isActivo());
    }

    @Test
    void crear_personajeAjeno_devuelve403ConMensaje() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        Usuario ladron = crearUsuario("ladron", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");

        mockMvc.perform(post("/anuncios")
                        .header("Authorization", "Bearer " + token(ladron))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"Hola\",\"categoriaIds\":["
                                + categoria.getIdCategoria() + "],\"personajeId\":"
                                + p.getIdPersonaje() + "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error")
                        .value("No puedes gestionar anuncios de otro personaje"));

        assertEquals(0, anuncioRepo.count());
    }

    @Test
    void crear_sinCategorias_vaciaOAusente_seAcepta() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        String bearer = "Bearer " + token(owner);

        mockMvc.perform(post("/anuncios")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"Hola\",\"categoriaIds\":[],\"personajeId\":"
                                + p.getIdPersonaje() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(0)));

        mockMvc.perform(post("/anuncios")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"Sin campo de categorías\",\"personajeId\":"
                                + p.getIdPersonaje() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(0)));

        assertEquals(2, anuncioRepo.count());
        assertEquals(0, anuncioCategoriaRepo.count());
    }

    @Test
    void activar_cuartoAnuncioDeLaCuenta_devuelve400_ySePuedeTrasDesactivarOtro()
            throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        PerfilPersonaje p1 = crearPersonaje(owner, "Uno", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        PerfilPersonaje p2 = crearPersonaje(owner, "Dos", EstadoPersonaje.ACTIVO,
                "femenino", "Humana", "Clériga", 30);
        Categoria categoria = crearCategoria("Fantasía");
        Anuncio a1 = crearAnuncio(p1, "Uno", false, categoria);
        Anuncio a2 = crearAnuncio(p1, "Dos", false, categoria);
        Anuncio a3 = crearAnuncio(p2, "Tres", false, categoria);
        Anuncio a4 = crearAnuncio(p2, "Cuatro", false, categoria);
        String bearer = "Bearer " + token(owner);

        for (Anuncio a : List.of(a1, a2, a3)) {
            mockMvc.perform(patch("/anuncios/" + a.getIdAnuncio() + "/activo")
                            .header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"activo\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activo").value(true));
        }

        mockMvc.perform(patch("/anuncios/" + a4.getIdAnuncio() + "/activo")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("3 anuncios activos por cuenta")));

        mockMvc.perform(patch("/anuncios/" + a1.getIdAnuncio() + "/activo")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(patch("/anuncios/" + a4.getIdAnuncio() + "/activo")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void listar_categoriaIdsEnOrYFiltros_dejaFueraBaneadosYDesactivados() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        Usuario moderador = crearUsuario("mod", RolUsuario.MOD);
        Usuario otro = crearUsuario("otro", RolUsuario.USER);
        Usuario lector = crearUsuario("lector", RolUsuario.USER);
        Usuario cuentaBaneada = crearUsuario("baneado", RolUsuario.USER);
        Usuario personajeBaneado = crearUsuario("baneadoPj", RolUsuario.USER);
        Usuario desactivado = crearUsuario("desactivado", RolUsuario.USER);

        PerfilPersonaje p = crearPersonaje(owner, "Aragorn", EstadoPersonaje.ACTIVO,
                "masculino", "Dúnedain", "Guerrero", 30);
        PerfilPersonaje pOtro = crearPersonaje(otro, "Legolas", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Arquero", 22);
        PerfilPersonaje pBaneadoCuenta = crearPersonaje(cuentaBaneada, "CuentaBan", EstadoPersonaje.ACTIVO,
                "femenino", "Humana", "Maga", 20);
        PerfilPersonaje pBaneadoPj = crearPersonaje(personajeBaneado, "PjBan", EstadoPersonaje.ACTIVO,
                "femenino", "Humana", "Maga", 20);
        PerfilPersonaje pDesactivado = crearPersonaje(desactivado, "Inactivo", EstadoPersonaje.DESACTIVADO,
                "masculino", "Orco", "Guerrero", 20);

        Categoria c1 = crearCategoria("Acción");
        Categoria c2 = crearCategoria("Romance");
        Categoria c3 = crearCategoria("Misterio");

        Anuncio a1 = crearAnuncio(p, "Uno", true, c1);
        Anuncio a2 = crearAnuncio(p, "Dos", true, c2);
        Anuncio a3 = crearAnuncio(p, "Tres", true, c1, c2);
        Anuncio a4 = crearAnuncio(p, "Cuatro", true, c3);
        Anuncio a5 = crearAnuncio(pOtro, "Cinco", true, c1);
        crearAnuncio(pBaneadoCuenta, "Ban cuenta", true, c1);
        crearAnuncio(pBaneadoPj, "Ban pj", true, c1);
        crearAnuncio(pDesactivado, "Desactivado", true, c1);
        crearAnuncio(p, "Inactivo", false, c1);

        banearCuenta(cuentaBaneada, moderador);
        banearPersonaje(pBaneadoPj, moderador);

        // `lector` no tiene anuncios: ve los de `owner` y los de `otro`.
        String bearer = "Bearer " + token(lector);

        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "20")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hayMas").value(false))
                .andExpect(jsonPath("$.anuncios[*].idAnuncio",
                        containsInAnyOrder(a1.getIdAnuncio(), a2.getIdAnuncio(),
                                a3.getIdAnuncio(), a4.getIdAnuncio(), a5.getIdAnuncio())));

        // En cambio `otro` no ve su propio anuncio (a5), aunque sí los ajenos.
        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "20")
                        .header("Authorization", "Bearer " + token(otro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios[*].idAnuncio",
                        containsInAnyOrder(a1.getIdAnuncio(), a2.getIdAnuncio(),
                                a3.getIdAnuncio(), a4.getIdAnuncio())))
                .andExpect(jsonPath("$.anuncios[*].idAnuncio", not(hasItem(a5.getIdAnuncio()))));

        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "20")
                        .param("categoriaIds", String.valueOf(c1.getIdCategoria()),
                                String.valueOf(c2.getIdCategoria()))
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios[*].idAnuncio",
                        containsInAnyOrder(a1.getIdAnuncio(), a2.getIdAnuncio(),
                                a3.getIdAnuncio(), a5.getIdAnuncio())));

        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "20")
                        .param("nombre", "arag").param("genero", "MASCULINO")
                        .param("raza", "dún").param("clase", "gue")
                        .param("edadMin", "20").param("edadMax", "40")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios[*].idAnuncio",
                        containsInAnyOrder(a1.getIdAnuncio(), a2.getIdAnuncio(),
                                a3.getIdAnuncio(), a4.getIdAnuncio())));

        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "20")
                        .param("excluirIds", String.valueOf(a1.getIdAnuncio()),
                                String.valueOf(a2.getIdAnuncio()))
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios[*].idAnuncio",
                        containsInAnyOrder(a3.getIdAnuncio(), a4.getIdAnuncio(),
                                a5.getIdAnuncio())));
    }

    @Test
    void listar_soloOnlinePorDefecto_usaPresenciaEnBatch() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        Usuario lector = crearUsuario("lector", RolUsuario.USER);
        PerfilPersonaje p1 = crearPersonaje(owner, "Uno", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        PerfilPersonaje p2 = crearPersonaje(owner, "Dos", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");
        Anuncio online = crearAnuncio(p1, "Online", true, categoria);
        Anuncio offline = crearAnuncio(p2, "Offline", true, categoria);
        when(presenceService.getOnlineStatusForCharacters(anySet())).thenReturn(
                Map.of(p1.getIdPersonaje(), true, p2.getIdPersonaje(), false));

        mockMvc.perform(get("/anuncios").header("Authorization", "Bearer " + token(lector)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios", hasSize(1)))
                .andExpect(jsonPath("$.anuncios[0].idAnuncio").value(online.getIdAnuncio()))
                .andExpect(jsonPath("$.anuncios[0].online").value(true));

        mockMvc.perform(get("/anuncios").param("soloOnline", "false")
                        .header("Authorization", "Bearer " + token(lector)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios", hasSize(2)))
                .andExpect(jsonPath("$.anuncios[*].idAnuncio",
                        containsInAnyOrder(online.getIdAnuncio(), offline.getIdAnuncio())));
    }

    @Test
    void listar_conMasCandidatosQueElTamaño_devuelveHayMas() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        Usuario lector = crearUsuario("lector", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");
        Anuncio a1 = crearAnuncio(p, "Uno", true, categoria);
        Anuncio a2 = crearAnuncio(p, "Dos", true, categoria);
        crearAnuncio(p, "Tres", true, categoria);
        String bearer = "Bearer " + token(lector);

        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "1")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios", hasSize(1)))
                .andExpect(jsonPath("$.hayMas").value(true));

        mockMvc.perform(get("/anuncios").param("soloOnline", "false").param("size", "20")
                        .param("excluirIds", String.valueOf(a1.getIdAnuncio()),
                                String.valueOf(a2.getIdAnuncio()))
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anuncios", hasSize(1)))
                .andExpect(jsonPath("$.hayMas").value(false));
    }

    @Test
    void editarYEliminar_ajenosDevuelven403_yPropiosFuncionan() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        Usuario otro = crearUsuario("otro", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");
        Anuncio anuncio = crearAnuncio(p, "Original", false, categoria);
        String otroBearer = "Bearer " + token(otro);
        String body = "{\"contenido\":\"<p>Editado</p>\",\"categoriaIds\":["
                + categoria.getIdCategoria() + "]}";

        mockMvc.perform(put("/anuncios/" + anuncio.getIdAnuncio())
                        .header("Authorization", otroBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error")
                        .value("No puedes gestionar anuncios de otro personaje"));
        mockMvc.perform(delete("/anuncios/" + anuncio.getIdAnuncio())
                        .header("Authorization", otroBearer))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/anuncios/" + anuncio.getIdAnuncio())
                        .header("Authorization", "Bearer " + token(owner))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", containsString("<p>Editado</p>")));
        mockMvc.perform(delete("/anuncios/" + anuncio.getIdAnuncio())
                        .header("Authorization", "Bearer " + token(owner)))
                .andExpect(status().isOk());

        assertEquals(0, anuncioRepo.count());
        assertEquals(0, anuncioCategoriaRepo.count());
    }

    @Test
    void operarAnuncioInexistente_devuelve404() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        String bearer = "Bearer " + token(owner);

        mockMvc.perform(delete("/anuncios/99999").header("Authorization", bearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Anuncio no encontrado"));
        mockMvc.perform(patch("/anuncios/99999/activo")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void miosIncluyeInactivosDeTodosLosPersonajes_yPorPersonajeSoloActivos() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        PerfilPersonaje p1 = crearPersonaje(owner, "Uno", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        PerfilPersonaje p2 = crearPersonaje(owner, "Dos", EstadoPersonaje.ACTIVO,
                "femenino", "Humana", "Maga", 28);
        Categoria categoria = crearCategoria("Fantasía");
        Anuncio activo = crearAnuncio(p1, "Activo", true, categoria);
        crearAnuncio(p2, "Inactivo", false, categoria);
        String bearer = "Bearer " + token(owner);

        mockMvc.perform(get("/anuncios/mios").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/anuncios/personaje/" + p1.getIdPersonaje())
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idAnuncio").value(activo.getIdAnuncio()));
    }

    @Test
    void eliminarCategoria_usadaPorAnuncio_devuelve409() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        Usuario admin = crearUsuario("admin", RolUsuario.ADMIN);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");
        Anuncio anuncio = crearAnuncio(p, "Uno", false, categoria);

        mockMvc.perform(delete("/categorias/" + categoria.getIdCategoria())
                        .header("Authorization", "Bearer " + token(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("asociada a anuncios")));

        mockMvc.perform(delete("/anuncios/" + anuncio.getIdAnuncio())
                        .header("Authorization", "Bearer " + token(owner)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/categorias/" + categoria.getIdCategoria())
                        .header("Authorization", "Bearer " + token(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarPersonaje_borraAnunciosYCategoriasEnCascada() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");
        crearAnuncio(p, "Uno", true, categoria);

        mockMvc.perform(delete("/personajes/" + p.getIdPersonaje())
                        .header("Authorization", "Bearer " + token(owner)))
                .andExpect(status().isOk());

        assertEquals(0, anuncioRepo.count());
        assertEquals(0, anuncioCategoriaRepo.count());
    }

    @Test
    void eliminarCuenta_borraAnunciosYCategoriasEnCascada() throws Exception {
        Usuario owner = crearUsuario("owner", RolUsuario.USER);
        PerfilPersonaje p = crearPersonaje(owner, "Kael", EstadoPersonaje.ACTIVO,
                "masculino", "Elfo", "Mago", 25);
        Categoria categoria = crearCategoria("Fantasía");
        crearAnuncio(p, "Uno", true, categoria);

        mockMvc.perform(delete("/usuarios/" + owner.getUserId())
                        .header("Authorization", "Bearer " + token(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Password123\"}"))
                .andExpect(status().isOk());

        assertEquals(0, anuncioRepo.count());
        assertEquals(0, anuncioCategoriaRepo.count());
        assertEquals(0, usuarioRepo.count());
    }
}
