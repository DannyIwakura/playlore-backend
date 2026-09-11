package com.playrole.chat.interceptor;

import com.playrole.chat.service.PresenceService;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class CharacterSessionChannelInterceptor implements ChannelInterceptor {

    private final PresenceService presenceService;

    public CharacterSessionChannelInterceptor(@Lazy PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Integer personajeId = (Integer) sessionAttributes.get("personajeId");

                if (personajeId != null) {
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        accessor.setUser(() -> personajeId.toString());
                        String customSessionId = (String) sessionAttributes.get("customSessionId");
                        Integer usuarioId = (Integer) sessionAttributes.get("usuarioId");
                        if (customSessionId != null) {
                            presenceService.onConnect(personajeId, usuarioId, customSessionId);
                        }
                    } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        String customSessionId = (String) sessionAttributes.get("customSessionId");
                        if (customSessionId != null) {
                            presenceService.onDisconnect(personajeId, customSessionId);
                        }
                    }
                }
            }
        }

        return message;
    }
}
