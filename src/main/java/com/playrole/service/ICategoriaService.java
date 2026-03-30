package com.playrole.service;

import java.util.List;

import com.playrole.model.Categoria;

public interface ICategoriaService {
	
	List<Categoria> obtenerTodas();
    Categoria obtenerPorId(Integer id);
    Categoria guardar(Categoria categoria);
    void eliminar(Integer id);
    List<Categoria> obtenerPorTipo(String tipo);
}
