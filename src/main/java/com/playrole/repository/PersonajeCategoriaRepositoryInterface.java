package com.playrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;

import jakarta.transaction.Transactional;

public interface PersonajeCategoriaRepositoryInterface extends JpaRepository<PersonajeCategoria, Integer> {
    List<PersonajeCategoria> findByIdPersonajeIdPersonaje(Integer idPersonaje);
    List<PersonajeCategoria> findByIdCategoriaIdCategoria(Integer idCategoria);
    boolean existsByIdCategoriaIdCategoria(Integer idCategoria);

    @Modifying
    @Transactional
    @Query("DELETE FROM PersonajeCategoria pc WHERE pc.idPersonaje.idPersonaje = :idPersonaje")
    void deleteByPersonajeId(@Param("idPersonaje") Integer idPersonaje);
}
