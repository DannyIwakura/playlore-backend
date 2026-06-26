package com.playrole.chat.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.chat.dto.MensajeCanalDTO;
import com.playrole.chat.service.CanalMensajeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/canales/{canalId}/mensajes")
public class CanalMensajeController {

    private final CanalMensajeService mensajeService;

    public CanalMensajeController(CanalMensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    public ResponseEntity<Page<MensajeCanalDTO>> obtenerMensajes(
            @PathVariable Integer canalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(mensajeService.obtenerMensajes(canalId, personajeId, page, size));
    }

    @PostMapping
    public ResponseEntity<MensajeCanalDTO> enviarMensaje(
            @PathVariable Integer canalId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(mensajeService.enviarMensaje(canalId, personajeId, body.get("contenido")));
    }

    @PutMapping("/{mensajeId}")
    public ResponseEntity<MensajeCanalDTO> editarMensaje(
            @PathVariable Integer canalId,
            @PathVariable Integer mensajeId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(mensajeService.editarMensaje(mensajeId, personajeId, body.get("contenido")));
    }

    @DeleteMapping("/{mensajeId}")
    public ResponseEntity<Map<String, String>> eliminarMensaje(
            @PathVariable Integer canalId,
            @PathVariable Integer mensajeId,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        mensajeService.eliminarMensaje(mensajeId, personajeId);
        return ResponseEntity.ok(Map.of("mensaje", "Mensaje eliminado"));
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
