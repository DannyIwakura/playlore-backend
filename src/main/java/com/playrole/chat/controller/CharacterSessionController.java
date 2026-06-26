package com.playrole.chat.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.chat.dto.IniciarSesionDTO;
import com.playrole.chat.dto.SesionPersonajeDTO;
import com.playrole.chat.service.SesionPersonajeService;
import com.playrole.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/personajes/sesion")
public class CharacterSessionController {

    private final SesionPersonajeService sesionService;

    public CharacterSessionController(SesionPersonajeService sesionService) {
        this.sesionService = sesionService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<SesionPersonajeDTO> iniciarSesion(
            @RequestBody @Valid IniciarSesionDTO dto,
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        SesionPersonajeDTO sesion = sesionService.iniciarSesion(usuarioId, dto.getPersonajeId());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + sesion.getTokenJwt())
                .body(sesion);
    }

    @PostMapping("/cerrar")
    public ResponseEntity<Map<String, String>> cerrarSesion(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        sesionService.cerrarSesion(token);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    @PostMapping("/cerrar-todas")
    public ResponseEntity<Map<String, String>> cerrarTodasSesiones(Authentication authentication) {
        Integer usuarioId = obtenerUsuarioId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        sesionService.cerrarTodasSesiones(usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Todas las sesiones cerradas correctamente"));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<SesionPersonajeDTO>> sesionesActivas(Authentication authentication) {
        Integer usuarioId = obtenerUsuarioId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(sesionService.sesionesActivas(usuarioId));
    }

    private Integer obtenerUsuarioId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) {
            return cp.getUsuarioId();
        }
        if (principal instanceof CustomUserDetails ud) {
            return ud.getUsuario().getUserId();
        }
        return null;
    }
}
