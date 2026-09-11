package com.playrole.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.dto.PersonajeCategoriaDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.service.IPerfilPersonajeService;
import com.playrole.service.IPersonajeCategoriaService;
import com.playrole.utils.AuthUtils;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/personaje-categorias")
public class PersonajeCategoriaController {
	
    private final IPersonajeCategoriaService personajeCategoriaService;
    private final IPerfilPersonajeService personajeService;

    public PersonajeCategoriaController(IPersonajeCategoriaService personajeCategoriaService,
                                        IPerfilPersonajeService personajeService) {
        this.personajeCategoriaService = personajeCategoriaService;
        this.personajeService = personajeService;
    }

    @GetMapping
    public List<PersonajeCategoriaDTO> listarTodos() {
        return personajeCategoriaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public PersonajeCategoriaDTO obtenerPorId(@PathVariable Integer id) {
        return personajeCategoriaService.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación Personaje-Categoría no encontrada"));
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
    public PersonajeCategoriaDTO crear(@RequestBody PersonajeCategoriaDTO personajeCategoriaDTO,
                                       Authentication authentication) {
        if (personajeCategoriaDTO.getIdPersonaje() != null) {
            comprobarPropiedadPersonaje(personajeCategoriaDTO.getIdPersonaje(), authentication);
        }
        return personajeCategoriaService.crear(personajeCategoriaDTO);
    }

    @PutMapping("/{id}")
    public PersonajeCategoriaDTO actualizar(@PathVariable Integer id,
                                            @RequestBody PersonajeCategoriaDTO personajeCategoriaDTO,
                                            Authentication authentication) {
        PersonajeCategoriaDTO existente = personajeCategoriaService.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación Personaje-Categoría no encontrada"));
        Integer idPersonaje = personajeCategoriaDTO.getIdPersonaje() != null
                ? personajeCategoriaDTO.getIdPersonaje()
                : existente.getIdPersonaje();
        comprobarPropiedadPersonaje(idPersonaje, authentication);
        return personajeCategoriaService.actualizar(id, personajeCategoriaDTO);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id, Authentication authentication) {
        PersonajeCategoriaDTO existente = personajeCategoriaService.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación Personaje-Categoría no encontrada"));
        comprobarPropiedadPersonaje(existente.getIdPersonaje(), authentication);
        personajeCategoriaService.eliminarPorId(id);
    }

    private void comprobarPropiedadPersonaje(Integer idPersonaje, Authentication authentication) {
        Integer userId = AuthUtils.obtenerUserId(authentication);
        PerfilPersonajeDTO personaje = personajeService.obtenerPersonaje(idPersonaje);
        if (!personaje.getUserId().equals(userId) && !AuthUtils.esModerador(authentication)) {
            throw new AccessDeniedException("No puedes gestionar las categorías de un personaje que no es tuyo");
        }
    }
}
