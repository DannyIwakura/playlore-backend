package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.playrole.chat.auth.CharacterSessionAuthenticationToken;
import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.chat.service.PresenceService;
import com.playrole.dto.ActualizarAnuncioDTO;
import com.playrole.dto.AnuncioDTO;
import com.playrole.dto.AnunciosRespuestaDTO;
import com.playrole.dto.CambiarActivoDTO;
import com.playrole.dto.CrearAnuncioDTO;
import com.playrole.enums.EstadoPersonaje;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.model.Anuncio;
import com.playrole.model.AnuncioCategoria;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.AnuncioCategoriaRepository;
import com.playrole.repository.AnuncioRepository;
import com.playrole.repository.CategoríaRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.security.CustomUserDetails;

class AnuncioServiceTest {

    private AnuncioRepository anuncioRepository;
    private AnuncioCategoriaRepository anuncioCategoriaRepository;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private CategoríaRepositoryInterface categoriaRepository;
    private PresenceService presenceService;
    private AnuncioServiceImpl service;

    @BeforeEach
    void setUp() {
        anuncioRepository = mock(AnuncioRepository.class);
        anuncioCategoriaRepository = mock(AnuncioCategoriaRepository.class);
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        categoriaRepository = mock(CategoríaRepositoryInterface.class);
        presenceService = mock(PresenceService.class);
        service = new AnuncioServiceImpl(anuncioRepository, anuncioCategoriaRepository,
                personajeRepository, categoriaRepository, presenceService);
    }

    private Usuario usuario(int id, RolUsuario rol) {
        Usuario u = new Usuario(id);
        u.setNombre("usuario" + id);
        u.setRol(rol);
        return u;
    }

    private PerfilPersonaje personaje(int id, Usuario owner) {
        PerfilPersonaje p = new PerfilPersonaje(id);
        p.setNombre("Personaje " + id);
        p.setEstado(EstadoPersonaje.ACTIVO);
        p.setUserId(owner);
        return p;
    }

    private Categoria categoria(int id) {
        Categoria c = new Categoria(id);
        c.setNombre("Categoría " + id);
        return c;
    }

    private Authentication authUsuario(Usuario u) {
        return new UsernamePasswordAuthenticationToken(new CustomUserDetails(u), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
    }

    private Authentication authSesion(Usuario u, PerfilPersonaje p) {
        return new CharacterSessionAuthenticationToken(
                new CharacterSessionPrincipal(u, p, 1, "token"),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
    }

    private CrearAnuncioDTO crearDto(String contenido, Integer personajeId) {
        CrearAnuncioDTO dto = new CrearAnuncioDTO();
        dto.setContenido(contenido);
        dto.setCategoriaIds(List.of(1));
        dto.setPersonajeId(personajeId);
        return dto;
    }

    private void prepararCategoria() {
        when(categoriaRepository.findAllById(anyCollection())).thenReturn(List.of(categoria(1)));
        when(anuncioCategoriaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crear_naceInactivo_ySanitizaElHtmlAntesDePersistir() {
        Usuario owner = usuario(1, RolUsuario.USER);
        PerfilPersonaje p = personaje(5, owner);
        when(personajeRepository.findById(5)).thenReturn(Optional.of(p));
        prepararCategoria();
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> {
            Anuncio a = inv.getArgument(0);
            a.setIdAnuncio(10);
            return a;
        });

        AnuncioDTO resultado = service.crear(
                crearDto("<p>Hola</p><script>alert(1)</script>", 5), authUsuario(owner));

        assertFalse(resultado.isActivo());
        assertFalse(resultado.getContenido().contains("<script"));
        assertTrue(resultado.getContenido().contains("<p>Hola</p>"));
        assertEquals(1, resultado.getCategorias().size());
        assertEquals(10, resultado.getIdAnuncio());
        assertEquals(5, resultado.getPersonajeId());

        ArgumentCaptor<Anuncio> captor = ArgumentCaptor.forClass(Anuncio.class);
        verify(anuncioRepository).save(captor.capture());
        assertFalse(captor.getValue().isActivo());
        assertEquals(resultado.getContenido(), captor.getValue().getContenido());
    }

    @Test
    void crear_conTokenDeSesion_usaElPersonajeDeLaSesionAunqueElBodyTraigaOtro() {
        Usuario owner = usuario(1, RolUsuario.USER);
        PerfilPersonaje p = personaje(5, owner);
        when(personajeRepository.findById(5)).thenReturn(Optional.of(p));
        prepararCategoria();
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));

        AnuncioDTO resultado = service.crear(
                crearDto("<p>Hola</p>", 999), authSesion(owner, p));

        assertEquals(5, resultado.getPersonajeId());
        verify(personajeRepository).findById(5);
        verify(personajeRepository, never()).findById(999);
    }

    @Test
    void crear_conTokenDeUsuario_sinPersonajeId_devuelve400() {
        CrearAnuncioDTO dto = crearDto("<p>Hola</p>", null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.crear(dto, authUsuario(usuario(1, RolUsuario.USER))));

        assertEquals("Debes indicar el personaje para publicar el anuncio", ex.getMessage());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void crear_sinCategorias_noConsultaElCatalogo() {
        Usuario owner = usuario(1, RolUsuario.USER);
        PerfilPersonaje p = personaje(5, owner);
        when(personajeRepository.findById(5)).thenReturn(Optional.of(p));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));
        CrearAnuncioDTO dto = crearDto("<p>Hola</p>", 5);
        dto.setCategoriaIds(List.of());

        AnuncioDTO resultado = service.crear(dto, authUsuario(owner));

        assertTrue(resultado.getCategorias().isEmpty());
        verify(categoriaRepository, never()).findAllById(anyCollection());
        verify(anuncioCategoriaRepository, never()).saveAll(any());
    }

    @Test
    void crear_categoriaIdsDuplicados_devuelve400() {
        Usuario owner = usuario(1, RolUsuario.USER);
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personaje(5, owner)));
        CrearAnuncioDTO dto = crearDto("<p>Hola</p>", 5);
        dto.setCategoriaIds(List.of(1, 1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.crear(dto, authUsuario(owner)));

        assertEquals("Las categorías seleccionadas no deben repetirse", ex.getMessage());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void crear_personajeAjeno_devuelve403_yModeradorPuede() {
        PerfilPersonaje ajeno = personaje(5, usuario(1, RolUsuario.USER));
        when(personajeRepository.findById(5)).thenReturn(Optional.of(ajeno));
        prepararCategoria();
        Authentication ladron = authUsuario(usuario(2, RolUsuario.USER));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.crear(crearDto("<p>Hola</p>", 5), ladron));
        assertEquals("No puedes gestionar anuncios de otro personaje", ex.getMessage());

        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));
        AnuncioDTO comoMod = service.crear(crearDto("<p>Hola</p>", 5),
                authUsuario(usuario(3, RolUsuario.MOD)));
        assertEquals(5, comoMod.getPersonajeId());
    }

    @Test
    void activar_cuentaConTresActivosEntrePersonajes_devuelve400() {
        Usuario owner = usuario(1, RolUsuario.USER);
        PerfilPersonaje p = personaje(5, owner);
        Anuncio anuncio = anuncio(10, p, false);
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(anuncio));
        when(anuncioRepository.countActivosByUsuario(1)).thenReturn(3L);

        CambiarActivoDTO dto = new CambiarActivoDTO();
        dto.setActivo(true);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.cambiarActivo(10, dto, authUsuario(owner)));

        assertTrue(ex.getMessage().contains("3 anuncios activos por cuenta"));
        verify(anuncioRepository).countActivosByUsuario(1);
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void activarYDesactivar_idempotentesNoConsultanhNiGuardan() {
        Usuario owner = usuario(1, RolUsuario.USER);
        Anuncio activo = anuncio(10, personaje(5, owner), true);
        Anuncio inactivo = anuncio(11, personaje(5, owner), false);
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(activo));
        when(anuncioRepository.findById(11)).thenReturn(Optional.of(inactivo));
        when(anuncioCategoriaRepository.findByAnuncioIdIn(anyCollection())).thenReturn(List.of());

        CambiarActivoDTO mantenerActivo = new CambiarActivoDTO();
        mantenerActivo.setActivo(true);
        assertEquals(10, service.cambiarActivo(10, mantenerActivo, authUsuario(owner)).getIdAnuncio());

        CambiarActivoDTO mantenerInactivo = new CambiarActivoDTO();
        mantenerInactivo.setActivo(false);
        assertEquals(11, service.cambiarActivo(11, mantenerInactivo, authUsuario(owner)).getIdAnuncio());

        verify(anuncioRepository, never()).countActivosByUsuario(any());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void activar_conDosActivosEnLaCuenta_guardaActivo() {
        Usuario owner = usuario(1, RolUsuario.USER);
        Anuncio anuncio = anuncio(10, personaje(5, owner), false);
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(anuncio));
        when(anuncioRepository.countActivosByUsuario(1)).thenReturn(2L);
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(anuncioCategoriaRepository.findByAnuncioIdIn(anyCollection())).thenReturn(List.of());

        CambiarActivoDTO dto = new CambiarActivoDTO();
        dto.setActivo(true);
        AnuncioDTO resultado = service.cambiarActivo(10, dto, authUsuario(owner));

        assertTrue(resultado.isActivo());
        verify(anuncioRepository).save(anuncio);
    }

    @Test
    void editar_actualizaContenidoSanitizado_yReemplazaCategorias() {
        Usuario owner = usuario(1, RolUsuario.USER);
        Anuncio anuncio = anuncio(10, personaje(5, owner), false);
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(anuncio));
        when(categoriaRepository.findAllById(anyCollection())).thenReturn(List.of(categoria(1)));
        when(anuncioCategoriaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarAnuncioDTO dto = new ActualizarAnuncioDTO();
        dto.setContenido("<p>Nuevo</p><img src=x onerror=alert(1)>");
        dto.setCategoriaIds(List.of(1));

        AnuncioDTO resultado = service.editar(10, dto, authUsuario(owner));

        assertFalse(resultado.getContenido().contains("onerror"));
        assertTrue(resultado.getContenido().contains("<p>Nuevo</p>"));
        InOrder inOrder = inOrder(anuncioCategoriaRepository, anuncioRepository);
        inOrder.verify(anuncioCategoriaRepository).deleteByAnuncioId(10);
        inOrder.verify(anuncioRepository).save(anuncio);
    }

    @Test
    void eliminar_borraCategoriasAntesQueElAnuncio() {
        Usuario owner = usuario(1, RolUsuario.USER);
        Anuncio anuncio = anuncio(10, personaje(5, owner), false);
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(anuncio));
        Authentication auth = authUsuario(owner);

        service.eliminar(10, auth);

        InOrder inOrder = inOrder(anuncioCategoriaRepository, anuncioRepository);
        inOrder.verify(anuncioCategoriaRepository).deleteByAnuncioId(10);
        inOrder.verify(anuncioRepository).delete(anuncio);
    }

    @Test
    void eliminar_ajeno_devuelve403() {
        Anuncio anuncio = anuncio(10, personaje(5, usuario(1, RolUsuario.USER)), false);
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(anuncio));

        Authentication ladron = authUsuario(usuario(2, RolUsuario.USER));
        assertThrows(AccessDeniedException.class, () -> service.eliminar(10, ladron));
        verify(anuncioRepository, never()).delete(any(Anuncio.class));
    }

    @Test
    void listar_soloOnline_filtraEnBatchConUnaSolaLlamadaAPresencia() {
        Usuario owner = usuario(1, RolUsuario.USER);
        Anuncio a1 = anuncio(1, personaje(11, owner), true);
        Anuncio a2 = anuncio(2, personaje(12, owner), true);
        when(anuncioRepository.findAll(any(Specification.class))).thenReturn(List.of(a1, a2));
        when(presenceService.getOnlineStatusForCharacters(anySet()))
                .thenReturn(Map.of(11, true, 12, false));
        when(anuncioRepository.findUsuarioIdsByAnuncioIds(anyCollection()))
                .thenReturn(List.of(usuarioProyeccion(1, 1)));
        when(anuncioCategoriaRepository.findByAnuncioIdIn(anyCollection()))
                .thenReturn(List.of(relacion(1, 1)));

        AnunciosRespuestaDTO respuesta = service.listar(null, null, null, null, null, null,
                null, null, true, null, 9);

        assertEquals(1, respuesta.getAnuncios().size());
        assertEquals(1, respuesta.getAnuncios().get(0).getIdAnuncio());
        assertTrue(respuesta.getAnuncios().get(0).isOnline());
        assertEquals(1, respuesta.getAnuncios().get(0).getUsuarioId());
        assertEquals(1, respuesta.getAnuncios().get(0).getCategorias().size());
        verify(presenceService).getOnlineStatusForCharacters(anySet());
    }

    @Test
    void listar_tomaUnLoteYCalculaHayMas() {
        Usuario owner = usuario(1, RolUsuario.USER);
        Anuncio a1 = anuncio(1, personaje(11, owner), true);
        Anuncio a2 = anuncio(2, personaje(11, owner), true);
        Anuncio a3 = anuncio(3, personaje(11, owner), true);
        when(anuncioRepository.findAll(any(Specification.class))).thenReturn(List.of(a1, a2, a3));
        when(presenceService.getOnlineStatusForCharacters(anySet()))
                .thenReturn(Map.of(11, true));
        when(anuncioRepository.findUsuarioIdsByAnuncioIds(anyCollection())).thenReturn(List.of());
        when(anuncioCategoriaRepository.findByAnuncioIdIn(anyCollection())).thenReturn(List.of());

        AnunciosRespuestaDTO respuesta = service.listar(null, null, null, null, null, null,
                null, null, false, List.of(99), 2);

        assertEquals(2, respuesta.getAnuncios().size());
        assertTrue(respuesta.isHayMas());
        // Sin el filtro también se informa del estado; se resuelve en un solo lote.
        assertTrue(respuesta.getAnuncios().get(0).isOnline());
        verify(presenceService).getOnlineStatusForCharacters(anySet());
    }

    private Anuncio anuncio(int id, PerfilPersonaje p, boolean activo) {
        Anuncio a = new Anuncio(id);
        a.setContenido("<p>Contenido</p>");
        a.setActivo(activo);
        a.setPersonaje(p);
        a.setFechaCreacion(new Date());
        return a;
    }

    private AnuncioCategoria relacion(int idAnuncio, int idCategoria) {
        AnuncioCategoria relacion = new AnuncioCategoria();
        relacion.setAnuncio(new Anuncio(idAnuncio));
        relacion.setIdCategoria(categoria(idCategoria));
        return relacion;
    }

    private AnuncioRepository.AnuncioUsuarioId usuarioProyeccion(int anuncioId, int usuarioId) {
        return new AnuncioRepository.AnuncioUsuarioId() {
            @Override
            public Integer getAnuncioId() {
                return anuncioId;
            }

            @Override
            public Integer getUsuarioId() {
                return usuarioId;
            }
        };
    }
}
