package com.playrole.chat.controller;

import com.playrole.chat.service.CanalMensajeService;
import com.playrole.chat.service.MensajePrivadoPersonajeService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final CanalMensajeService canalMensajeService;
    private final MensajePrivadoPersonajeService dmService;

    public ChatWebSocketController(CanalMensajeService canalMensajeService,
                                    MensajePrivadoPersonajeService dmService) {
        this.canalMensajeService = canalMensajeService;
        this.dmService = dmService;
    }

    @MessageMapping("/chat.canal.{canalId}.enviar")
    public void enviarMensajeCanal(@DestinationVariable Integer canalId,
                                    @Payload Map<String, String> payload,
                                    Principal principal) {
        Integer personajeId = Integer.parseInt(principal.getName());
        String contenido = payload.get("contenido");
        if (contenido != null && !contenido.isBlank()) {
            canalMensajeService.enviarMensaje(canalId, personajeId, contenido);
        }
    }

    @MessageMapping("/chat.privado.enviar")
    public void enviarMensajePrivado(@Payload Map<String, Object> payload,
                                      Principal principal) {
        Integer emisorId = Integer.parseInt(principal.getName());
        Integer receptorId = Integer.parseInt(payload.get("receptorId").toString());
        String contenido = (String) payload.get("contenido");
        if (contenido != null && !contenido.isBlank()) {
            dmService.enviarMensaje(emisorId, receptorId, contenido);
        }
    }
}
