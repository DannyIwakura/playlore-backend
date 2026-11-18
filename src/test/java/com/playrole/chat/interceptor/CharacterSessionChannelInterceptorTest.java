package com.playrole.chat.interceptor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.chat.service.PresenceService;

/**
 * Cubre el cierre de fugas por WebSocket: los topics de canal exigen membresia,
 * los privados solo se escuchan desde el propio personaje y los SEND solo van a
 * /app/.
 */
class CharacterSessionChannelInterceptorTest {

    private MiembroCanalRepository miembroRepository;
    private CharacterSessionChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        miembroRepository = mock(MiembroCanalRepository.class);
        interceptor = new CharacterSessionChannelInterceptor(mock(PresenceService.class), miembroRepository);
    }

    private Message<byte[]> mensaje(StompCommand comando, String destino, Integer personajeId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(comando);
        accessor.setDestination(destino);
        Map<String, Object> atributos = new HashMap<>();
        if (personajeId != null) atributos.put("personajeId", personajeId);
        accessor.setSessionAttributes(atributos);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void subscribeACanalSiendoMiembro_sePermite() {
        when(miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(7, 3)).thenReturn(true);

        assertDoesNotThrow(() -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/canal.7", 3), null));
    }

    @Test
    void subscribeACanalSinSerMiembro_seBloquea() {
        when(miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(7, 3)).thenReturn(false);

        assertThrows(MessagingException.class, () -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/canal.7", 3), null));
    }

    @Test
    void subscribeAPresenciaDeCanalExigeMembresia() {
        when(miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(7, 3)).thenReturn(false);

        assertThrows(MessagingException.class, () -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/canal.7.presence", 3), null));
    }

    @Test
    void subscribeAlPrivadoPropio_sePermite() {
        assertDoesNotThrow(() -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/privado.3", 3), null));
    }

    @Test
    void subscribeAlPrivadoAjeno_seBloquea() {
        assertThrows(MessagingException.class, () -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/privado.9", 3), null));
    }

    @Test
    void subscribeSinSesionDePersonaje_seBloquea() {
        assertThrows(MessagingException.class, () -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/presencia", null), null));
    }

    @Test
    void subscribeAPresenciaGlobal_sePermite() {
        assertDoesNotThrow(() -> interceptor.preSend(
                mensaje(StompCommand.SUBSCRIBE, "/topic/presencia", 3), null));
    }

    @Test
    void sendFueraDeApp_seBloquea() {
        assertThrows(MessagingException.class, () -> interceptor.preSend(
                mensaje(StompCommand.SEND, "/topic/canal.7", 3), null));
    }

    @Test
    void sendAApp_sePermite() {
        assertDoesNotThrow(() -> interceptor.preSend(
                mensaje(StompCommand.SEND, "/app/canal.7.enviar", 3), null));
    }
}
