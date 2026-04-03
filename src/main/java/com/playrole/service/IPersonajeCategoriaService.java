package com.playrole.service;

import java.util.List;
import java.util.Optional;

import com.playrole.dto.PersonajeCategoriaDTO;
import com.playrole.model.PersonajeCategoria;

public interface IPersonajeCategoriaService {
	
	PersonajeCategoria guardar(PersonajeCategoria pc);
    Optional<PersonajeCategoriaDTO> obtenerPorId(Integer id);
    Optional<PersonajeCategoria> obtenerEntidadPorId(Integer id);
    void eliminarPorId(Integer id);
    List<PersonajeCategoriaDTO> obtenerTodos();
    List<PersonajeCategoriaDTO> obtenerPorPersonajeId(Integer idPersonaje);
    List<PersonajeCategoriaDTO> obtenerPorCategoriaId(Integer idCategoria);
    boolean existePorCategoriaId(Integer idCategoria);
}
