package com.playrole.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.ActualizarAnuncioDTO;
import com.playrole.dto.AnuncioDTO;
import com.playrole.dto.AnunciosRespuestaDTO;
import com.playrole.dto.CambiarActivoDTO;
import com.playrole.dto.CrearAnuncioDTO;
import com.playrole.service.AnuncioService;
import com.playrole.utils.AuthUtils;

import jakarta.validation.Valid;

/** Endpoints de anuncios RP. `/anuncios/**` ya exige autenticación en SecurityConfig. */
@RestController
@RequestMapping("/anuncios")
public class AnuncioController {

    private final AnuncioService anuncioService;

    public AnuncioController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    @GetMapping
    public AnunciosRespuestaDTO listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String clase,
            @RequestParam(required = false) Integer edadMin,
            @RequestParam(required = false) Integer edadMax,
            @RequestParam(required = false) List<Integer> categoriaIds,
            @RequestParam(defaultValue = "true") boolean soloOnline,
            @RequestParam(required = false) List<Integer> excluirIds,
            @RequestParam(defaultValue = "9") int size,
            Authentication authentication) {
        // El listado público excluye los anuncios de la propia cuenta (tanto con
        // JWT de usuario como con sesión de personaje).
        return anuncioService.listar(AuthUtils.obtenerUserId(authentication),
                nombre, genero, raza, clase, edadMin, edadMax,
                categoriaIds, soloOnline, excluirIds, size);
    }

    @GetMapping("/mios")
    public List<AnuncioDTO> misAnuncios(Authentication authentication) {
        return anuncioService.misAnuncios(AuthUtils.obtenerUserId(authentication));
    }

    @GetMapping("/personaje/{id}")
    public List<AnuncioDTO> porPersonaje(@PathVariable Integer id) {
        return anuncioService.porPersonaje(id);
    }

    @PostMapping
    public AnuncioDTO crear(@Valid @RequestBody CrearAnuncioDTO dto, Authentication authentication) {
        return anuncioService.crear(dto, authentication);
    }

    @PutMapping("/{id}")
    public AnuncioDTO editar(@PathVariable Integer id,
            @Valid @RequestBody ActualizarAnuncioDTO dto, Authentication authentication) {
        return anuncioService.editar(id, dto, authentication);
    }

    @PatchMapping("/{id}/activo")
    public AnuncioDTO cambiarActivo(@PathVariable Integer id,
            @Valid @RequestBody CambiarActivoDTO dto, Authentication authentication) {
        return anuncioService.cambiarActivo(id, dto, authentication);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id, Authentication authentication) {
        anuncioService.eliminar(id, authentication);
    }
}
