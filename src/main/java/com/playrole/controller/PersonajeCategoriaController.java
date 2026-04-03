package com.playrole.controller;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.CategoriaDTO;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.dto.PersonajeCategoriaDTO;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;
import com.playrole.service.ICategoriaService;
import com.playrole.service.IPerfilPersonajeService;
import com.playrole.service.IPersonajeCategoriaService;

@RestController
@RequestMapping("/personaje-categorias")
public class PersonajeCategoriaController {
	
	private final IPersonajeCategoriaService personajeCategoriaService;
	private final ICategoriaService categoriaService;
	private final IPerfilPersonajeService personajeService;

    public PersonajeCategoriaController(IPersonajeCategoriaService personajeCategoriaService,
    		ICategoriaService categoriaService,
    		IPerfilPersonajeService personajeService) {
        this.personajeCategoriaService = personajeCategoriaService;
		this.categoriaService = categoriaService;
		this.personajeService = personajeService;
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
    public PersonajeCategoriaDTO crear(@RequestBody PersonajeCategoriaDTO personajeCategoriaDTO) {
    	
    	//validar que vangan las ids
    	if (personajeCategoriaDTO.getIdCategoria() == null) {
            throw new RuntimeException("Debe indicar el ID de la categoría");
        }
    	
        if (personajeCategoriaDTO.getIdPersonaje() == null) {
            throw new RuntimeException("Debe indicar el ID del personaje");
        }

        //obtener la categoraa completa
        CategoriaDTO categoriaDTO = categoriaService.obtenerPorId(personajeCategoriaDTO.getIdCategoria());
        if (categoriaDTO == null) {
            throw new RuntimeException("Categoría no encontrada");
        }
        Categoria categoria = categoriaDTO.toEntity();

        //obtener el personaje completo desde el servicio
        PerfilPersonajeDTO personajeDTO = personajeService.obtenerPersonaje(personajeCategoriaDTO.getIdPersonaje());
        if (personajeDTO == null) {
            throw new RuntimeException("Personaje no encontrado");
        }

        PerfilPersonaje personaje = new PerfilPersonaje();
        personaje.setIdPersonaje(personajeDTO.getIdPersonaje());
        personaje.setNombre(personajeDTO.getNombre());

        //crear la relacoón usando el metodo del DTO
        PersonajeCategoria pc = personajeCategoriaDTO.toEntity(categoria, personaje);

        //guardar la relacion
        PersonajeCategoria guardado = personajeCategoriaService.guardar(pc);

        //devolver DTO con nombres completos
        return PersonajeCategoriaDTO.fromEntity(guardado);
    }

    @PutMapping("/{id}")
    public PersonajeCategoriaDTO actualizar(@PathVariable Integer id, @RequestBody PersonajeCategoriaDTO personajeCategoriaDTO) {
    	// 1️⃣ Obtener la relación completa desde la DB
        PersonajeCategoria existente = personajeCategoriaService.obtenerEntidadPorId(id)
                .orElseThrow(() -> new RuntimeException("PersonajeCategoria no encontrada"));

        //actualizar tipo y fecha si vienen
        if (personajeCategoriaDTO.getTipo() != null) {
            existente.setTipo(personajeCategoriaDTO.getTipo());
        }
        if (personajeCategoriaDTO.getFechaAdicion() != null) {
            existente.setFechaAdicion(personajeCategoriaDTO.getFechaAdicion());
        }

        //actualizar categoría si se indica un nuevo ID
        if (personajeCategoriaDTO.getIdCategoria() != null && !personajeCategoriaDTO.getIdCategoria().equals(existente.getIdCategoria().getIdCategoria())) {
            CategoriaDTO categoriaDTO = categoriaService.obtenerPorId(personajeCategoriaDTO.getIdCategoria());
            if (categoriaDTO == null) {
                throw new RuntimeException("Categoría no encontrada");
            }
            existente.setIdCategoria(categoriaDTO.toEntity());
        }

        //actualizar personaje si se indica un nuevo ID
        if (personajeCategoriaDTO.getIdPersonaje() != null && !personajeCategoriaDTO.getIdPersonaje().equals(existente.getIdPersonaje().getIdPersonaje())) {
            PerfilPersonajeDTO personajeDTO = personajeService.obtenerPersonaje(personajeCategoriaDTO.getIdPersonaje());
            if (personajeDTO == null) {
                throw new RuntimeException("Personaje no encontrado");
            }

            // Creamos entidad mínima con ID y nombre para que fromEntity funcione
            PerfilPersonaje personaje = new PerfilPersonaje();
            personaje.setIdPersonaje(personajeDTO.getIdPersonaje());
            personaje.setNombre(personajeDTO.getNombre());
            existente.setIdPersonaje(personaje);
        }

        //guardar la relación actualizada
        PersonajeCategoria actualizado = personajeCategoriaService.guardar(existente);

        //devolver DTO con nombres correctos
        return PersonajeCategoriaDTO.fromEntity(actualizado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        personajeCategoriaService.eliminarPorId(id);
    }
}
