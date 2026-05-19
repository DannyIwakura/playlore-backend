package com.playrole.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.playrole.dto.CategoriaDTO;
import com.playrole.model.Categoria;

public interface ICategoriaService {
	
	Page<CategoriaDTO> obtenerTodas(int page, int size);
	Optional<Categoria> obtenerEntidadPorId(Integer id);
    CategoriaDTO obtenerPorId(Integer id);
    CategoriaDTO guardar(CategoriaDTO categoriaDTO);
    void eliminar(Integer id);
}
