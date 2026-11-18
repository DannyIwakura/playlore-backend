package com.playrole.chat.interceptor;

import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.chat.service.PresenceService;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CharacterSessionChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CANAL_TOPIC = Pattern.compile("^/topic/canal\\.(\\d+)(?:\\.presence)?$");
    private static final Pattern PRIVADO_TOPIC = Pattern.compile("^/topic/privado\\.(\\d+)$");

    private final PresenceService presenceService;
    private final MiembroCanalRepository miembroRepository;

    public CharacterSessionChannelInterceptor(@Lazy PresenceService presenceService,
                                              MiembroCanalRepository miembroRepository) {
        this.presenceService = presenceService;
        this.miembroRepository = miembroRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand comando = accessor.getCommand();
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        Integer personajeId = sessionAttributes != null
                ? (Integer) sessionAttributes.get("personajeId")
                : null;

        if (StompCommand.SUBSCRIBE.equals(comando)) {
            if (personajeId == null) {
                throw new MessagingException("Se requiere una sesión de personaje activa");
            }
            verificarSuscripcion(accessor.getDestination(), personajeId);
        } else if (StompCommand.SEND.equals(comando)) {
            String destino = accessor.getDestination();
            if (destino != null && !destino.startsWith("/app/")) {
                throw new MessagingException("Destino de envío no permitido");
            }
        }

        if (personajeId != null) {
            if (StompCommand.CONNECT.equals(comando)) {
                accessor.setUser(() -> personajeId.toString());
                String customSessionId = (String) sessionAttributes.get("customSessionId");
                Integer usuarioId = (Integer) sessionAttributes.get("usuarioId");
                if (customSessionId != null) {
                    presenceService.onConnect(personajeId, usuarioId, customSessionId);
                }
            } else if (StompCommand.DISCONNECT.equals(comando)) {
                String customSessionId = (String) sessionAttributes.get("customSessionId");
                if (customSessionId != null) {
                    presenceService.onDisconnect(personajeId, customSessionId);
                }
            }
        }

        return message;
    }

    /**
     * Cierra las fugas de WebSocket: los topics de canal exigen membresía y los
     * privados solo se pueden escuchar desde el propio personaje.
     */
    private void verificarSuscripcion(String destino, Integer personajeId) {
        if (destino == null) return;

        Matcher canal = CANAL_TOPIC.matcher(destino);
        if (canal.matches()) {
            Integer canalId = Integer.valueOf(canal.group(1));
            if (!miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId)) {
                throw new MessagingException("No eres miembro de este canal");
            }
            return;
        }

        Matcher privado = PRIVADO_TOPIC.matcher(destino);
        if (privado.matches()) {
            Integer objetivoId = Integer.valueOf(privado.group(1));
            if (!objetivoId.equals(personajeId)) {
                throw new MessagingException("No puedes escuchar los mensajes privados de otro personaje");
            }
        }
    }
}
