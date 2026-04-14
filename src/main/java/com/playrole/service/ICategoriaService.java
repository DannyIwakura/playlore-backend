package com.playrole.service;

import java.util.List;
import java.util.Optional;

import com.playrole.dto.CategoriaDTO;
import com.playrole.model.Categoria;

public interface ICategoriaService {
	
	List<CategoriaDTO> obtenerTodas();
	Optional<Categoria> obtenerEntidadPorId(Integer id);
    CategoriaDTO obtenerPorId(Integer id);
    CategoriaDTO guardar(CategoriaDTO categoriaDTO);
    void eliminar(Integer id);
}
