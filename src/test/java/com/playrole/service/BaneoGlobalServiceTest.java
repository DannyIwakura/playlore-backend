package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.playrole.chat.service.SesionPersonajeService;
import com.playrole.dto.CrearBaneoDTO;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.BaneoGlobal;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

class BaneoGlobalServiceTest {

    private BaneoGlobalRepository baneoRepository;
    private UsuarioRepositoryInterface usuarioRepository;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private SesionPersonajeService sesionPersonajeService;
    private BaneoGlobalService service;

    @BeforeEach
    void setUp() {
        baneoRepository = mock(BaneoGlobalRepository.class);
        usuarioRepository = mock(UsuarioRepositoryInterface.class);
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        sesionPersonajeService = mock(SesionPersonajeService.class);
        service = new BaneoGlobalService(baneoRepository, usuarioRepository,
                personajeRepository, sesionPersonajeService);
    }

    private CrearBaneoDTO dto(String tipo, Integer id, String duracion) {
        CrearBaneoDTO dto = new CrearBaneoDTO();
        dto.setTipo(tipo);
        dto.setId(id);
        dto.setDuracion(duracion);
        dto.setMotivo("Spam");
        return dto;
    }

    private Usuario usuario(int id, RolUsuario rol) {
        Usuario u = new Usuario(id);
        u.setNombre("Usuario" + id);
        u.setRol(rol);
        return u;
    }

    private void stubSaveConId() {
        when(baneoRepository.save(any(BaneoGlobal.class)))
                .thenAnswer(inv -> {
                    BaneoGlobal b = inv.getArgument(0);
                    b.setIdBaneoGlobal(99);
                    return b;
                });
    }

    @Test
    void banear_sinId_lanzaBadRequest() {
        assertThrows(BadRequestException.class, () -> service.banear(dto("USUARIO", null, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_tipoInvalido_lanzaBadRequest() {
        assertThrows(BadRequestException.class, () -> service.banear(dto("CANAL", 2, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_aSiMismo_lanzaBadRequest() {
        Usuario admin = usuario(1, RolUsuario.ADMIN);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class, () -> service.banear(dto("USUARIO", 1, null), admin));
    }

    @Test
    void banear_aAdministrador_lanzaBadRequest() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario(2, RolUsuario.ADMIN)));

        assertThrows(BadRequestException.class,
                () -> service.banear(dto("USUARIO", 2, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_usuarioYaBaneado_lanzaBadRequest() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario(2, RolUsuario.USER)));
        when(baneoRepository.existsActivoByUsuario(2)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.banear(dto("USUARIO", 2, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_usuarioNoExiste_lanzaResourceNotFound() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.banear(dto("USUARIO", 2, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_usuarioOk_permanenteSinExpiracionYcierraSesiones() {
        stubSaveConId();
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario(2, RolUsuario.USER)));

        var res = service.banear(dto("USUARIO", 2, "PERMANENTE"), usuario(1, RolUsuario.ADMIN));

        assertNotNull(res.getIdBaneoGlobal());
        assertEquals("USUARIO", res.getTipo());
        assertEquals(2, res.getIdObjetivo());
        assertNull(res.getFechaExpiracion());
        verify(sesionPersonajeService).cerrarTodasSesiones(2);
        verify(baneoRepository).save(any(BaneoGlobal.class));
    }

    @Test
    void banear_usuarioOk_duracion24hCalculaExpiracion() throws InterruptedException {
        stubSaveConId();
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario(2, RolUsuario.USER)));

        long antes = System.currentTimeMillis() + 86400000L;
        var res = service.banear(dto("USUARIO", 2, "24H"), usuario(1, RolUsuario.ADMIN));
        long despues = System.currentTimeMillis() + 86400000L;

        assertNotNull(res.getFechaExpiracion());
        assertTrue(res.getFechaExpiracion().getTime() >= antes);
        assertTrue(res.getFechaExpiracion().getTime() <= despues);
    }

    @Test
    void banear_usuarioOk_duracionInvalida_lanzaBadRequest() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario(2, RolUsuario.USER)));

        assertThrows(BadRequestException.class,
                () -> service.banear(dto("USUARIO", 2, "10D"), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_personajeOk_cierraSesionesDelPersonaje() {
        stubSaveConId();
        PerfilPersonaje objetivo = new PerfilPersonaje();
        objetivo.setIdPersonaje(5);
        objetivo.setNombre("Kael");
        when(personajeRepository.findById(5)).thenReturn(Optional.of(objetivo));

        var res = service.banear(dto("PERSONAJE", 5, null), usuario(1, RolUsuario.ADMIN));

        assertEquals("PERSONAJE", res.getTipo());
        assertEquals(5, res.getIdObjetivo());
        verify(sesionPersonajeService).cerrarSesionesDePersonaje(5);
    }

    @Test
    void banear_personajeYaBaneado_lanzaBadRequest() {
        PerfilPersonaje objetivo = new PerfilPersonaje();
        objetivo.setIdPersonaje(5);
        objetivo.setNombre("Kael");
        when(personajeRepository.findById(5)).thenReturn(Optional.of(objetivo));
        when(baneoRepository.existsActivoByPersonaje(5)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.banear(dto("PERSONAJE", 5, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void banear_personajeNoExiste_lanzaResourceNotFound() {
        when(personajeRepository.findById(5)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.banear(dto("PERSONAJE", 5, null), usuario(1, RolUsuario.ADMIN)));
    }

    @Test
    void desbanear_baneoNoExiste_lanzaResourceNotFound() {
        when(baneoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.desbanear(1));
    }

    @Test
    void desbanear_ok_eliminaBaneo() {
        BaneoGlobal baneo = new BaneoGlobal();
        when(baneoRepository.findById(1)).thenReturn(Optional.of(baneo));

        service.desbanear(1);

        verify(baneoRepository).delete(baneo);
    }

    @Test
    void verificarUsuarioNoBaneado_baneado_lanzaAccessDenied() {
        when(baneoRepository.existsActivoByUsuario(2)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.verificarUsuarioNoBaneado(2));
    }

    @Test
    void verificarUsuarioNoBaneado_sinBaneo_noLanza() {
        when(baneoRepository.existsActivoByUsuario(2)).thenReturn(false);

        service.verificarUsuarioNoBaneado(2);
        service.verificarUsuarioNoBaneado(null);
        verify(baneoRepository, never()).existsActivoByUsuario(null);
    }

    @Test
    void verificarNoBaneado_conBaneoDeUsuarioOPersonaje_lanzaAccessDenied() {
        when(baneoRepository.existsActivoByUsuarioOPersonaje(2, 5)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.verificarNoBaneado(2, 5));
    }

    @Test
    void verificarNoBaneado_sinBaneo_noLanza() {
        when(baneoRepository.existsActivoByUsuarioOPersonaje(2, 5)).thenReturn(false);

        service.verificarNoBaneado(2, 5);
        service.verificarNoBaneado(null, 5);
    }

    @Test
    void listar_mapeaEntidadesADto() {
        BaneoGlobal baneo = new BaneoGlobal();
        baneo.setIdBaneoGlobal(1);
        baneo.setUsuario(usuario(2, RolUsuario.USER));
        baneo.setBaneadoPor(usuario(1, RolUsuario.ADMIN));
        baneo.setMotivo("Spam");
        baneo.setFechaBaneo(new Date());
        when(baneoRepository.findAllByOrderByFechaBaneoDesc()).thenReturn(List.of(baneo));

        var lista = service.listar();

        assertEquals(1, lista.size());
        assertEquals("USUARIO", lista.get(0).getTipo());
        assertEquals(2, lista.get(0).getIdObjetivo());
        assertEquals("Spam", lista.get(0).getMotivo());
    }
}
