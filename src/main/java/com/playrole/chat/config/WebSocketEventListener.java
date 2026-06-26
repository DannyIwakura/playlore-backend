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
            String sessionId = accessor.getSessionId();

            if (personajeId != null && sessionId != null) {
                presenceService.onConnect(personajeId, sessionId);
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        var sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            Integer personajeId = (Integer) sessionAttributes.get("personajeId");
            String sessionId = accessor.getSessionId();

            if (personajeId != null && sessionId != null) {
                presenceService.onDisconnect(personajeId, sessionId);
            }
        }
    }
}
