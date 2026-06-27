package com.playrole.chat.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.chat.dto.CanalDTO;
import com.playrole.chat.dto.CrearCanalDTO;
import com.playrole.chat.dto.MiembroCanalDTO;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.service.CanalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/canales")
public class CanalController {

    private final CanalService canalService;

    public CanalController(CanalService canalService) {
        this.canalService = canalService;
    }

    @GetMapping
    public ResponseEntity<List<CanalDTO>> listarCanales(Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(canalService.listarCanalesDisponibles(personajeId));
    }

    @GetMapping("/unidos")
    public ResponseEntity<List<CanalDTO>> listarCanalesUnidos(Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(canalService.listarCanalesUnidos(personajeId));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<CanalDTO>> listarCanalesDisponibles(Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(canalService.listarCanalesPublicosNoUnidos(personajeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanalDTO> obtenerCanal(@PathVariable Integer id, Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(canalService.obtenerCanal(id, personajeId));
    }

    @PostMapping
    public ResponseEntity<CanalDTO> crearCanal(@RequestBody @Valid CrearCanalDTO dto,
                                                Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        boolean esAdmin = esAdmin(authentication);
        return ResponseEntity.ok(canalService.crearCanal(dto, personajeId, esAdmin));
    }

    @PostMapping("/{id}/unirse")
    public ResponseEntity<Map<String, String>> unirse(@PathVariable Integer id,
                                                       Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        canalService.unirseACanal(id, personajeId);
        return ResponseEntity.ok(Map.of("mensaje", "Te has unido al canal"));
    }

    @PostMapping("/{id}/salir")
    public ResponseEntity<Map<String, String>> salir(@PathVariable Integer id,
                                                      Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        canalService.salirDeCanal(id, personajeId);
        return ResponseEntity.ok(Map.of("mensaje", "Has salido del canal"));
    }

    @PostMapping("/{id}/invitar")
    public ResponseEntity<Map<String, String>> invitar(@PathVariable Integer id,
                                                        @RequestBody Map<String, Integer> body,
                                                        Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        canalService.invitarMiembro(id, body.get("personajeId"), personajeId);
        return ResponseEntity.ok(Map.of("mensaje", "Invitación enviada"));
    }

    @DeleteMapping("/{id}/miembros/{personajeId}")
    public ResponseEntity<Map<String, String>> expulsar(@PathVariable Integer id,
                                                         @PathVariable Integer personajeId,
                                                         Authentication authentication) {
        Integer solicitanteId = obtenerPersonajeId(authentication);
        canalService.expulsarMiembro(id, personajeId, solicitanteId);
        return ResponseEntity.ok(Map.of("mensaje", "Miembro expulsado"));
    }

    @PutMapping("/{id}/miembros/{personajeId}/rol")
    public ResponseEntity<Map<String, String>> cambiarRol(@PathVariable Integer id,
                                                           @PathVariable Integer personajeId,
                                                           @RequestBody Map<String, String> body,
                                                           Authentication authentication) {
        Integer solicitanteId = obtenerPersonajeId(authentication);
        RolCanal nuevoRol = RolCanal.valueOf(body.get("rol"));
        canalService.cambiarRol(id, personajeId, nuevoRol, solicitanteId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol actualizado"));
    }

    @GetMapping("/{id}/miembros")
    public ResponseEntity<Page<MiembroCanalDTO>> listarMiembros(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(canalService.listarMiembros(id, personajeId, page, size));
    }

    @GetMapping("/{id}/miembros/online")
    public ResponseEntity<List<MiembroCanalDTO>> listarMiembrosOnline(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        return ResponseEntity.ok(canalService.listarMiembrosOnline(id, personajeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarCanal(@PathVariable Integer id,
                                                              Authentication authentication) {
        Integer personajeId = obtenerPersonajeId(authentication);
        canalService.eliminarCanal(id, personajeId);
        return ResponseEntity.ok(Map.of("mensaje", "Canal eliminado"));
    }

    private Integer obtenerPersonajeId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) {
            return cp.getPersonajeId();
        }
        return null;
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null) return false;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) {
            return "ADMIN".equals(cp.getUsuarioRole());
        }
        return false;
    }
}
