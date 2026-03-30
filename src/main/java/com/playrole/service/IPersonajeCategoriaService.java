package com.playrole.service;

import java.util.List;
import java.util.Optional;

import com.playrole.model.PersonajeCategoria;

public interface IPersonajeCategoriaService {
	
	PersonajeCategoria guardar(PersonajeCategoria pc);
    Optional<PersonajeCategoria> obtenerPorId(Integer id);
    void eliminarPorId(Integer id);
    List<PersonajeCategoria> obtenerTodos();
    List<PersonajeCategoria> obtenerPorPersonajeId(Integer idPersonaje);
    List<PersonajeCategoria> obtenerPorCategoriaId(Integer idCategoria);
}
