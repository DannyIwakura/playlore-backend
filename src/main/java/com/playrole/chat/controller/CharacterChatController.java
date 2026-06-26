package com.playrole.chat.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.chat.dto.MensajePrivadoPersonajeDTO;
import com.playrole.chat.service.MensajePrivadoPersonajeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat/privado")
public class CharacterChatController {

    private final MensajePrivadoPersonajeService mensajeService;

    public CharacterChatController(MensajePrivadoPersonajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping("/conversaciones")
    public ResponseEntity<List<MensajePrivadoPersonajeDTO>> listarConversaciones(Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(mensajeService.listarConversaciones(personajeId));
    }

    @GetMapping("/contactos")
    public ResponseEntity<List<Integer>> listarContactos(Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(mensajeService.obtenerContactos(personajeId));
    }

    @GetMapping("/{otroPersonajeId}")
    public ResponseEntity<List<MensajePrivadoPersonajeDTO>> obtenerConversacion(
            @PathVariable Integer otroPersonajeId,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(mensajeService.obtenerConversacion(personajeId, otroPersonajeId, personajeId));
    }

    @PostMapping
    public ResponseEntity<MensajePrivadoPersonajeDTO> enviarMensaje(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Integer emisorId = obtenerPersonajeId(authentication);
        Integer receptorId = Integer.parseInt(body.get("receptorId").toString());
        String contenido = (String) body.get("contenido");
        return ResponseEntity.ok(mensajeService.enviarMensaje(emisorId, receptorId, contenido));
    }

    @GetMapping("/no-leidos")
    public ResponseEntity<Map<String, Long>> contarNoLeidos(Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        long count = mensajeService.contarNoLeidos(personajeId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private Integer obtenerPersonajeId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) {
            return cp.getPersonajeId();
        }
        return null;
    }
}
