package com.playrole.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.playrole.dto.PersonajeCategoriaDTO;
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
                .orElseThrow(() -> new RuntimeException("Relación Personaje-Categoría no encontrada"));
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
    public PersonajeCategoriaDTO crear(@RequestBody PersonajeCategoriaDTO personajeCategoriaDTO) {
        return personajeCategoriaService.crear(personajeCategoriaDTO);
    }

    @PutMapping("/{id}")
    public PersonajeCategoriaDTO actualizar(@PathVariable Integer id, @RequestBody PersonajeCategoriaDTO personajeCategoriaDTO) {
        return personajeCategoriaService.actualizar(id, personajeCategoriaDTO);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        personajeCategoriaService.eliminarPorId(id);
    }
}