package com.playrole.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;
import com.playrole.service.IPerfilPersonajeService;

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

    @GetMapping("/{id}")
    public PerfilPersonajeDTO obtenerPersonaje(@PathVariable Integer id) {
        return personajeService.obtenerPersonaje(id);
    }

    @PostMapping
    public PerfilPersonajeDTO crearPersonaje(@RequestBody PerfilPersonajeDTO dto) {
        return personajeService.guardarPersonaje(dto);
    }

    @PutMapping("/{id}")
    public PerfilPersonajeDTO actualizarPersonaje(@PathVariable Integer id, @RequestBody PerfilPersonajeDTO dto) {
        return personajeService.modificarPersonaje(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminarPersonaje(@PathVariable Integer id) {
        personajeService.eliminarPersonaje(id);
    }
	

}
