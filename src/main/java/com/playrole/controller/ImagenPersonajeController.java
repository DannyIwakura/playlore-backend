package com.playrole.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.dto.ImagenPersonajeDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.security.CustomUserDetails;
import com.playrole.service.IImagenPersonajeService;

@RestController
@RequestMapping("/personajes/{id}/imagenes")
public class ImagenPersonajeController {

    private final IImagenPersonajeService imagenService;

    public ImagenPersonajeController(IImagenPersonajeService imagenService) {
        this.imagenService = imagenService;
    }

    @GetMapping
    public ResponseEntity<List<ImagenPersonajeDTO>> listarImagenes(@PathVariable Integer id) {
        return ResponseEntity.ok(imagenService.listarImagenes(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenPersonajeDTO> subirImagen(
            @PathVariable Integer id,
            @RequestPart("imagenFile") MultipartFile imagenFile,
            Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        return ResponseEntity.ok(imagenService.subirImagen(id, imagenFile, userId));
    }

    @DeleteMapping("/{imagenId}")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Integer id,
            @PathVariable Integer imagenId,
            Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        imagenService.eliminarImagen(imagenId, userId);
        return ResponseEntity.noContent().build();
    }

    private Integer obtenerUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario().getUserId();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario().getUserId();
        throw new AccessDeniedException("No autenticado");
    }
}
