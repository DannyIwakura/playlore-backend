package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.playrole.enums.AccionModeracion;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.RegistroModeracion;
import com.playrole.model.Usuario;
import com.playrole.repository.RegistroModeracionRepository;

class AuditoriaModeracionServiceTest {

    private RegistroModeracionRepository registroRepository;
    private AuditoriaModeracionService service;

    @BeforeEach
    void setUp() {
        registroRepository = mock(RegistroModeracionRepository.class);
        service = new AuditoriaModeracionService(registroRepository);
    }

    private Usuario usuario(int id, String nombre) {
        Usuario u = new Usuario(id);
        u.setNombre(nombre);
        return u;
    }

    @Test
    void registrar_conUsuarioObjetivo_guardaSnapshots() {
        Usuario admin = usuario(1, "Moderador");
        Usuario objetivo = usuario(2, "Objetivo");

        service.registrar(AccionModeracion.BANEO_CUENTA, admin, objetivo, null, "Spam", null);

        ArgumentCaptor<RegistroModeracion> captor = ArgumentCaptor.forClass(RegistroModeracion.class);
        verify(registroRepository).save(captor.capture());
        RegistroModeracion r = captor.getValue();
        assertEquals(AccionModeracion.BANEO_CUENTA, r.getAccion());
        assertEquals(1, r.getModeradorId());
        assertEquals("Moderador", r.getModeradorNombre());
        assertEquals(2, r.getObjetivoUsuarioId());
        assertEquals("Objetivo", r.getObjetivoUsuarioNombre());
        assertNull(r.getObjetivoPersonajeId());
        assertEquals("Spam", r.getMotivo());
    }

    @Test
    void registrar_conPersonajeObjetivo_guardaSnapshotsDelPersonaje() {
        Usuario admin = usuario(1, "Moderador");
        PerfilPersonaje objetivo = new PerfilPersonaje();
        objetivo.setIdPersonaje(5);
        objetivo.setNombre("Kael");

        service.registrar(AccionModeracion.BANEO_PERSONAJE, admin, null, objetivo, "Spam", "Duración: 24H");

        ArgumentCaptor<RegistroModeracion> captor = ArgumentCaptor.forClass(RegistroModeracion.class);
        verify(registroRepository).save(captor.capture());
        RegistroModeracion r = captor.getValue();
        assertNull(r.getObjetivoUsuarioId());
        assertEquals(5, r.getObjetivoPersonajeId());
        assertEquals("Kael", r.getObjetivoPersonajeNombre());
        assertEquals("Duración: 24H", r.getDetalle());
    }

    @Test
    void registrar_sinModerador_noGuarda() {
        service.registrar(AccionModeracion.DESBANEO, null, usuario(2, "X"), null, null, null);

        verify(registroRepository, never()).save(any());
    }

    @Test
    void listar_mapeaEntidadesADto() {
        RegistroModeracion r = new RegistroModeracion();
        r.setIdRegistro(10);
        r.setAccion(AccionModeracion.DENUNCIA_RESUELTA);
        r.setModeradorId(1);
        r.setModeradorNombre("Moderador");
        r.setObjetivoPersonajeId(5);
        r.setObjetivoPersonajeNombre("Kael");
        r.setFecha(new Date());
        when(registroRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(r));

        var lista = service.listar();

        assertEquals(1, lista.size());
        assertEquals(10, lista.get(0).getIdRegistro());
        assertEquals(AccionModeracion.DENUNCIA_RESUELTA, lista.get(0).getAccion());
        assertEquals("Moderador", lista.get(0).getModeradorNombre());
        assertEquals("Kael", lista.get(0).getObjetivoPersonajeNombre());
    }
}
