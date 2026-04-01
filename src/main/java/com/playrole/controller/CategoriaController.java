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

import com.playrole.dto.CategoriaDTO;
import com.playrole.service.ICategoriaService;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

	private final ICategoriaService categoriaService;

    public CategoriaController(ICategoriaService categoriaService) {
        this.categoriaService = categoriaService;
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
    public CategoriaDTO crearCategoria(@RequestBody CategoriaDTO categoriaDTO) {
        return categoriaService.guardar(categoriaDTO);
    }

    @PutMapping("/{id}")
    public CategoriaDTO actualizarCategoria(@PathVariable Integer id, @RequestBody CategoriaDTO categoriaDTO) {
        categoriaDTO.setIdCategoria(id);
        return categoriaService.guardar(categoriaDTO);
    }

    @DeleteMapping("/{id}")
    public void eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminar(id);
    }

    @GetMapping("/tipo/{tipo}")
    public List<CategoriaDTO> obtenerPorTipo(@PathVariable String tipo) {
        return categoriaService.obtenerPorTipo(tipo);
    }
	
	
}
