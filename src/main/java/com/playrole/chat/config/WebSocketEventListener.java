package com.playrole.chat.config;

import com.playrole.chat.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Component
public class WebSocketEventListener {

    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        var sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Integer personajeId = (Integer) sessionAttributes.get("personajeId");
            String customSessionId = (String) sessionAttributes.get("customSessionId");
            if (personajeId != null && customSessionId != null) {
                Integer usuarioId = (Integer) sessionAttributes.get("usuarioId");
                presenceService.onConnect(personajeId, usuarioId, customSessionId);
                String stompSessionId = accessor.getSessionId();
                if (stompSessionId != null) {
                    presenceService.mapStompSessionToCustomSession(stompSessionId, customSessionId);
                }
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String stompSessionId = event.getSessionId();
        if (stompSessionId == null) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            stompSessionId = accessor.getSessionId();
        }

        if (stompSessionId != null) {
            String customSessionId = presenceService.getCustomSessionIdByStompSessionId(stompSessionId);
            if (customSessionId != null) {
                presenceService.onDisconnectBySessionId(customSessionId);
            } else {
                presenceService.onDisconnectBySessionId(stompSessionId);
            }
        }
    }
}
