package com.playrole.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.CategoriaDTO;
import com.playrole.service.ICategoriaService;
import com.playrole.service.IPersonajeCategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

	private final ICategoriaService categoriaService;
	private final IPersonajeCategoriaService personajeCategoriaService;

    public CategoriaController(ICategoriaService categoriaService,
    		IPersonajeCategoriaService personajeCategoriaService) {
        this.categoriaService = categoriaService;
        this.personajeCategoriaService = personajeCategoriaService;
    }

    @GetMapping
    public List<CategoriaDTO> obtenerTodas() {
        return categoriaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public CategoriaDTO obtenerPorId(@PathVariable Integer id) {
        return categoriaService.obtenerPorId(id);
    }

    @PostMapping
    public CategoriaDTO crearCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        return categoriaService.guardar(categoriaDTO);
    }

    @PutMapping("/{id}")
    public CategoriaDTO actualizarCategoria(@PathVariable Integer id, @RequestBody CategoriaDTO categoriaDTO) {
        if (categoriaService.obtenerPorId(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
        categoriaDTO.setIdCategoria(id);
        return categoriaService.guardar(categoriaDTO);
    }

    @DeleteMapping("/{id}")
    public void eliminarCategoria(@PathVariable Integer id) {
    	if (personajeCategoriaService.existePorCategoriaId(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar la categoría porque está asociada a personajes"
            );
        }

        categoriaService.eliminar(id);
    }	
}
