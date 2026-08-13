package com.playrole.controller;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.dto.CrearDenunciaDTO;
import com.playrole.dto.DenunciaDTO;
import com.playrole.dto.ResolverDenunciaDTO;
import com.playrole.enums.EstadoDenuncia;
import com.playrole.enums.TipoDenuncia;
import com.playrole.exception.AccessDeniedException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.security.CustomUserDetails;
import com.playrole.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    public ResponseEntity<DenunciaDTO> crear(@RequestBody @Valid CrearDenunciaDTO dto,
                                             Authentication authentication) {
        Usuario denunciante = obtenerUsuario(authentication);
        PerfilPersonaje personaje = obtenerPersonaje(authentication);
        return ResponseEntity.ok(denunciaService.crearDenuncia(dto, denunciante, personaje));
    }

    @GetMapping
    public Page<DenunciaDTO> listar(@RequestParam(defaultValue = "PENDIENTE") EstadoDenuncia estado,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) TipoDenuncia tipo,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                    @RequestParam(required = false) String objetivo,
                                    @RequestParam(required = false) String denunciante,
                                    @RequestParam(required = false) String resueltoPor) {
        return denunciaService.listarDenuncias(estado, tipo, desde, hasta, objetivo, denunciante, resueltoPor, page, size);
    }

    @PutMapping("/{id}/resolver")
    public ResponseEntity<DenunciaDTO> resolver(@PathVariable Integer id,
                                                @RequestBody ResolverDenunciaDTO dto,
                                                Authentication authentication) {
        Usuario admin = obtenerUsuario(authentication);
        return ResponseEntity.ok(denunciaService.resolverDenuncia(id, dto.getEstado(), dto.getDecision(), admin));
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario();
        throw new AccessDeniedException("No autenticado");
    }

    private PerfilPersonaje obtenerPersonaje(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getPersonaje();
        return null;
    }
}
