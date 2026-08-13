package com.playrole.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.dto.BaneoGlobalDTO;
import com.playrole.dto.CrearBaneoDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.model.Usuario;
import com.playrole.security.CustomUserDetails;
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

    public ModeracionController(BaneoGlobalService baneoGlobalService) {
        this.baneoGlobalService = baneoGlobalService;
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
    public ResponseEntity<Map<String, String>> desbanear(@PathVariable Integer id) {
        baneoGlobalService.desbanear(id);
        return ResponseEntity.ok(Map.of("mensaje", "Baneo eliminado"));
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario();
        throw new AccessDeniedException("No autenticado");
    }
}
