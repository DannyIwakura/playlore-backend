package com.playrole.service;

import java.util.List;

import com.playrole.dto.CategoriaDTO;
import com.playrole.model.Categoria;

public interface ICategoriaService {
	
	List<CategoriaDTO> obtenerTodas();
    CategoriaDTO obtenerPorId(Integer id);
    CategoriaDTO guardar(CategoriaDTO categoriaDTO);
    void eliminar(Integer id);
}
