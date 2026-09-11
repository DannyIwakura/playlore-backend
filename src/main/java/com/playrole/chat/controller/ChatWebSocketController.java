package com.playrole.chat.controller;

import com.playrole.chat.service.CanalMensajeService;
import com.playrole.chat.service.MensajePrivadoPersonajeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final CanalMensajeService canalMensajeService;
    private final MensajePrivadoPersonajeService dmService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(CanalMensajeService canalMensajeService,
                                    MensajePrivadoPersonajeService dmService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.canalMensajeService = canalMensajeService;
        this.dmService = dmService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.canal.{canalId}.enviar")
    public void enviarMensajeCanal(@DestinationVariable Integer canalId,
                                    @Payload Map<String, String> payload,
                                    Principal principal) {
        Integer personajeId = parsePersonajeId(principal);
        String contenido = payload.get("contenido");
        if (contenido != null && !contenido.isBlank()) {
            Integer mensajePadreId = payload.get("mensajePadreId") != null && !payload.get("mensajePadreId").isBlank()
                    ? Integer.valueOf(payload.get("mensajePadreId"))
                    : null;
            canalMensajeService.enviarMensaje(canalId, personajeId, contenido, mensajePadreId);
        }
    }

    @MessageMapping("/chat.privado.enviar")
    public void enviarMensajePrivado(@Payload Map<String, Object> payload,
                                      Principal principal) {
        Integer emisorId = parsePersonajeId(principal);
        Integer receptorId = Integer.parseInt(payload.get("receptorId").toString());
        String contenido = (String) payload.get("contenido");
        if (contenido != null && !contenido.isBlank()) {
            dmService.enviarMensaje(emisorId, receptorId, contenido);
        }
    }

    @MessageExceptionHandler
    public void handleException(Exception ex, Principal principal) {
        log.warn("Error en WebSocket para {}: {}", principal != null ? principal.getName() : "desconocido",
                ex.getMessage());
    }

    private Integer parsePersonajeId(Principal principal) {
        return Integer.parseInt(principal.getName());
    }
}
