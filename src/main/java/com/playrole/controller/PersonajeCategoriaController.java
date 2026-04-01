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

import com.playrole.dto.PersonajeCategoriaDTO;
import com.playrole.model.PersonajeCategoria;
import com.playrole.service.IPersonajeCategoriaService;

@RestController
@RequestMapping("/personaje-categorias")
public class PersonajeCategoriaController {
	
	private final IPersonajeCategoriaService personajeCategoriaService;

    public PersonajeCategoriaController(IPersonajeCategoriaService personajeCategoriaService) {
        this.personajeCategoriaService = personajeCategoriaService;
    }

    @GetMapping
    public List<PersonajeCategoriaDTO> listarTodos() {
        return personajeCategoriaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public PersonajeCategoriaDTO obtenerPorId(@PathVariable Integer id) {
        return personajeCategoriaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("PersonajeCategoria no encontrada"));
    }

    @GetMapping("/personaje/{idPersonaje}")
    public List<PersonajeCategoriaDTO> obtenerPorPersonaje(@PathVariable Integer idPersonaje) {
        return personajeCategoriaService.obtenerPorPersonajeId(idPersonaje);
    }

    @GetMapping("/categoria/{idCategoria}")
    public List<PersonajeCategoriaDTO> obtenerPorCategoria(@PathVariable Integer idCategoria) {
        return personajeCategoriaService.obtenerPorCategoriaId(idCategoria);
    }

    @PostMapping
    public PersonajeCategoriaDTO crear(@RequestBody PersonajeCategoria personajeCategoria) {
        PersonajeCategoria guardado = personajeCategoriaService.guardar(personajeCategoria);
        return PersonajeCategoriaDTO.fromEntity(guardado);
    }

    @PutMapping("/{id}")
    public PersonajeCategoriaDTO actualizar(@PathVariable Integer id, @RequestBody PersonajeCategoria personajeCategoria) {
        personajeCategoria.setId(id);
        PersonajeCategoria actualizado = personajeCategoriaService.guardar(personajeCategoria);
        return PersonajeCategoriaDTO.fromEntity(actualizado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        personajeCategoriaService.eliminarPorId(id);
    }
}
