package com.playrole.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.dto.BaneoGlobalDTO;
import com.playrole.dto.CrearBaneoDTO;
import com.playrole.dto.RegistroModeracionDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.model.Usuario;
import com.playrole.security.CustomUserDetails;
import com.playrole.service.AuditoriaModeracionService;
import com.playrole.service.BaneoGlobalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/moderacion")
public class ModeracionController {

    private final BaneoGlobalService baneoGlobalService;
    private final AuditoriaModeracionService auditoriaService;

    public ModeracionController(BaneoGlobalService baneoGlobalService,
                                AuditoriaModeracionService auditoriaService) {
        this.baneoGlobalService = baneoGlobalService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/baneos")
    public ResponseEntity<List<BaneoGlobalDTO>> listarBaneos() {
        return ResponseEntity.ok(baneoGlobalService.listar());
    }

    @PostMapping("/baneos")
    public ResponseEntity<BaneoGlobalDTO> banear(@RequestBody CrearBaneoDTO dto,
                                                 Authentication authentication) {
        Usuario admin = obtenerUsuario(authentication);
        return ResponseEntity.ok(baneoGlobalService.banear(dto, admin));
    }

    @DeleteMapping("/baneos/{id}")
    public ResponseEntity<Map<String, String>> desbanear(@PathVariable Integer id,
                                                         Authentication authentication) {
        Usuario admin = obtenerUsuario(authentication);
        baneoGlobalService.desbanear(id, admin);
        return ResponseEntity.ok(Map.of("mensaje", "Baneo eliminado"));
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<RegistroModeracionDTO>> listarAuditoria() {
        return ResponseEntity.ok(auditoriaService.listar());
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario();
        throw new AccessDeniedException("No autenticado");
    }
}
