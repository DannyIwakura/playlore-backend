package com.playrole.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;
import com.playrole.service.IPerfilPersonajeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/personajes")
public class PerfilPersonajeController {
	
	private final IPerfilPersonajeService personajeService;

    public PerfilPersonajeController(IPerfilPersonajeService personajeService) {
        this.personajeService = personajeService;
    }

    @GetMapping
    public List<PerfilPersonajeDTO> listarPersonajes() {
        return personajeService.listarPersonajes();
    }
    
    @GetMapping("/usuario/{userId}")
    public List<PerfilPersonajeDTO> listarPersonajesPorUsuario(@PathVariable Integer userId) {
        return personajeService.listarPersonajesPorUsuario(userId);
    }

    @GetMapping("/{id}")
    public PerfilPersonajeDTO obtenerPersonaje(@PathVariable Integer id) {
        return personajeService.obtenerPersonaje(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PerfilPersonajeDTO crearPersonaje(
            @RequestPart("personaje") @Valid PerfilPersonajeDTO dto,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {

        return personajeService.guardarPersonaje(dto, avatarFile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PerfilPersonajeDTO actualizarPersonaje(
            @PathVariable Integer id,
            @RequestPart("personaje") @Valid PerfilPersonajeDTO dto,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {

        return personajeService.modificarPersonaje(id, dto, avatarFile);
    }
    
    @PutMapping("/admin/{id}/estado")
    public PerfilPersonajeDTO actualizarEstadoAdmin(@PathVariable Integer id, 
                                                    @RequestBody PerfilPersonajeAdminDTO dto) {
        return personajeService.modificarPersonajeAdmin(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminarPersonaje(@PathVariable Integer id) {
        personajeService.eliminarPersonaje(id);
    }
	

}
