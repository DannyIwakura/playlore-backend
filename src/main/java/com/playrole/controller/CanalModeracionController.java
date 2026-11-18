package com.playrole.controller;

import com.playrole.dto.CanalModeracionDTO;
import com.playrole.dto.EliminarCanalModeracionDTO;
import com.playrole.dto.RestaurarCanalDTO;
import com.playrole.dto.SuspenderCanalDTO;
import com.playrole.model.Usuario;
import com.playrole.service.CanalModeracionService;
import com.playrole.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Moderacion global de canales con token de usuario. No se reutiliza
 * /canales/** porque esos endpoints exigen sesion de personaje.
 */
@RestController
@RequestMapping("/moderacion/canales")
@PreAuthorize("hasAnyRole('ADMIN','MOD')")
public class CanalModeracionController {

    private final CanalModeracionService canalModeracionService;

    public CanalModeracionController(CanalModeracionService canalModeracionService) {
        this.canalModeracionService = canalModeracionService;
    }

    @GetMapping
    public ResponseEntity<List<CanalModeracionDTO>> listar() {
        return ResponseEntity.ok(canalModeracionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanalModeracionDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(canalModeracionService.obtener(id));
    }

    @PostMapping("/{id}/suspender")
    public ResponseEntity<CanalModeracionDTO> suspender(@PathVariable Integer id,
                                                        @RequestBody(required = false) SuspenderCanalDTO dto,
                                                        Authentication authentication) {
        Usuario moderador = AuthUtils.obtenerUsuario(authentication);
        return ResponseEntity.ok(canalModeracionService.suspender(id, dto, moderador));
    }

    @PostMapping("/{id}/restaurar")
    public ResponseEntity<CanalModeracionDTO> restaurar(@PathVariable Integer id,
                                                        @RequestBody(required = false) RestaurarCanalDTO dto,
                                                        Authentication authentication) {
        Usuario moderador = AuthUtils.obtenerUsuario(authentication);
        return ResponseEntity.ok(canalModeracionService.restaurar(id, dto, moderador));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id,
                                                        @RequestBody(required = false) EliminarCanalModeracionDTO dto,
                                                        Authentication authentication) {
        Usuario admin = AuthUtils.obtenerUsuario(authentication);
        canalModeracionService.eliminar(id, dto, admin);
        return ResponseEntity.ok(Map.of("mensaje", "Canal eliminado definitivamente"));
    }
}
