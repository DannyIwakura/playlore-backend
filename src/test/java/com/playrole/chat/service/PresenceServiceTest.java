package com.playrole.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.playrole.chat.dto.PresenceDTO;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;

class PresenceServiceTest {

    private SimpMessagingTemplate messagingTemplate;
    private MiembroCanalRepository miembroCanalRepository;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private SesionPersonajeService sesionPersonajeService;
    private PresenceService service;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        miembroCanalRepository = mock(MiembroCanalRepository.class);
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        sesionPersonajeService = mock(SesionPersonajeService.class);
        service = new PresenceService(messagingTemplate, miembroCanalRepository,
                personajeRepository, sesionPersonajeService);
    }

    @Test
    void onConnect_marcaOnlineYBroadcastea() {
        service.onConnect(1, null, "s1");

        assertTrue(service.isOnlineStrict(1));
        assertTrue(service.isOnline(1));
        assertEquals("conectado", service.getStatus(1));
        verify(messagingTemplate).convertAndSend(eq("/topic/presencia"), (Object) any(PresenceDTO.class));
    }

    @Test
    void onConnect_conSessionIdNulo_noHaceNada() {
        service.onConnect(1, null, null);

        assertFalse(service.isOnlineStrict(1));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/presencia"), (Object) any());
    }

    @Test
    void onDisconnect_eliminaSesionYBroadcasteaOffline() {
        service.onConnect(1, null, "s1");

        service.onDisconnect(1, "s1");

        assertFalse(service.isOnlineStrict(1));
        verify(messagingTemplate).convertAndSend(eq("/topic/presencia"),
                (Object) org.mockito.ArgumentMatchers.argThat(dto -> !((PresenceDTO) dto).isOnline()));
    }

    @Test
    void dosSesiones_mismoPersonaje_sigueOnlineHastaCerrarTodas() {
        service.onConnect(1, null, "s1");
        service.onConnect(1, null, "s2");

        service.onDisconnect(1, "s1");
        assertTrue(service.isOnlineStrict(1));

        service.onDisconnect(1, "s2");
        assertFalse(service.isOnlineStrict(1));
    }

    @Test
    void onDisconnectBySessionId_remuevePorSesion() {
        service.onConnect(1, null, "s1");

        service.onDisconnectBySessionId("s1");

        assertFalse(service.isOnlineStrict(1));
    }

    @Test
    void onDisconnectBySessionId_conSesionDesconocida_noLanza() {
        service.onDisconnectBySessionId("s-no-existe");
    }

    @Test
    void personajeSecundario_heredaOnlineMientrasOtroPersonajeDelUsuarioActivo() {
        when(sesionPersonajeService.tieneSesionActivaValida(2)).thenReturn(true);
        service.onConnect(1, 100, "s1");
        service.onConnect(2, 100, "s2");

        service.onDisconnect(2, "s2");

        // El personaje 2 sigue apareciendo online porque el 1 tiene WS activo
        // y el 2 conserva una sesión JWT válida.
        assertFalse(service.isOnlineStrict(2));
        assertTrue(service.isOnline(2));
    }

    @Test
    void personajeDesconectado_sinSesionActiva_noHeredaOnlineDeOtrosPersonajes() {
        when(sesionPersonajeService.tieneSesionActivaValida(2)).thenReturn(false);
        service.onConnect(1, 100, "s1");
        service.onConnect(2, 100, "s2");

        service.onDisconnect(2, "s2");

        assertFalse(service.isOnline(2));
    }

    @Test
    void personajeDesconectado_conSesionActivaPeroSinOtroWs_noHeredaOnline() {
        when(sesionPersonajeService.tieneSesionActivaValida(1)).thenReturn(true);
        service.onConnect(1, 100, "s1");

        service.onDisconnect(1, "s1");

        // Aunque tenga sesión válida, nadie más del usuario está conectado.
        assertFalse(service.isOnline(1));
    }

    @Test
    void updateStatus_cambiaEstadoYBroadcastea() {
        service.onConnect(1, null, "s1");

        service.updateStatus(1, "afk");

        assertEquals("afk", service.getStatus(1));
        verify(messagingTemplate).convertAndSend(eq("/topic/presencia"),
                (Object) org.mockito.ArgumentMatchers.argThat(dto ->
                        ((PresenceDTO) dto).isOnline() && "afk".equals(((PresenceDTO) dto).getStatus())));
    }

    @Test
    void updateStatus_conArgumentosNulos_noLanza() {
        service.updateStatus(null, "afk");
        service.updateStatus(1, null);
    }

    @Test
    void getStatus_sinRegistro_devuelveConectado() {
        assertEquals("conectado", service.getStatus(999));
    }

    @Test
    void mapStompSessionA_CustomSession_devuelveSoloUnaVez() {
        service.mapStompSessionToCustomSession("stomp1", "custom1");

        assertEquals("custom1", service.getCustomSessionIdByStompSessionId("stomp1"));
        assertNull(service.getCustomSessionIdByStompSessionId("stomp1"));
    }

    @Test
    void getOnlineStatusForCharacters_devuelveMapaCompleto() {
        service.onConnect(1, null, "s1");

        var resultado = service.getOnlineStatusForCharacters(java.util.Set.of(1, 2));

        assertTrue(resultado.get(1));
        assertFalse(resultado.get(2));
    }
}
