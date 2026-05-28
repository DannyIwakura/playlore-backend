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

import com.playrole.dto.ImagenPersonajeDTO;
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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(imagenService.subirImagen(id, imagenFile, userDetails.getUsuario().getUserId()));
    }

    @DeleteMapping("/{imagenId}")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Integer id,
            @PathVariable Integer imagenId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        imagenService.eliminarImagen(imagenId, userDetails.getUsuario().getUserId());
        return ResponseEntity.noContent().build();
    }
}
