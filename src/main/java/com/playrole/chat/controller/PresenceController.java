package com.playrole.chat.controller;

import com.playrole.chat.service.PresenceService;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/personajes")
public class PresenceController {

    private final PresenceService presenceService;
    private final PerfilPersonajeRepositoryInterface personajeRepository;

    public PresenceController(PresenceService presenceService,
                              PerfilPersonajeRepositoryInterface personajeRepository) {
        this.presenceService = presenceService;
        this.personajeRepository = personajeRepository;
    }

    @GetMapping("/{id}/online")
    public ResponseEntity<Map<String, Boolean>> checkOnline(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("online", presenceService.isOnline(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id,
                                             @RequestBody Map<String, String> body,
                                             Authentication authentication) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        comprobarPropiedadPersonaje(id, authentication);
        presenceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    private void comprobarPropiedadPersonaje(Integer id, Authentication authentication) {
        Integer userId = AuthUtils.obtenerUserId(authentication);
        PerfilPersonaje personaje = personajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));
        if (!personaje.getUserId().getUserId().equals(userId) && !AuthUtils.esModerador(authentication)) {
            throw new AccessDeniedException("No puedes cambiar el estado de un personaje que no es tuyo");
        }
    }
}
