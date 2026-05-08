package com.playrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;

public interface PersonajeCategoriaRepositoryInterface extends JpaRepository<PersonajeCategoria, Integer> {
    List<PersonajeCategoria> findByIdPersonajeIdPersonaje(Integer idPersonaje);
    List<PersonajeCategoria> findByIdCategoriaIdCategoria(Integer idCategoria);
    boolean existsByIdCategoriaIdCategoria(Integer idCategoria);
}
