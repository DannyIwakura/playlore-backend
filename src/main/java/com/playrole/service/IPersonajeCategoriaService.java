package com.playrole.service;

import java.util.List;
import java.util.Optional;

import com.playrole.dto.PersonajeCategoriaDTO;

public interface IPersonajeCategoriaService {
    List<PersonajeCategoriaDTO> obtenerTodos();
    Optional<PersonajeCategoriaDTO> obtenerPorId(Integer id);
    List<PersonajeCategoriaDTO> obtenerPorPersonajeId(Integer idPersonaje);
    List<PersonajeCategoriaDTO> obtenerPorCategoriaId(Integer idCategoria);
    boolean existePorCategoriaId(Integer idCategoria);
    PersonajeCategoriaDTO crear(PersonajeCategoriaDTO dto);
    PersonajeCategoriaDTO actualizar(Integer id, PersonajeCategoriaDTO dto);
    void eliminarPorId(Integer id);
}