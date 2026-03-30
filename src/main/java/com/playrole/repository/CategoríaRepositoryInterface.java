package com.playrole.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.model.Categoria;

public interface CategoríaRepositoryInterface extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByNombre(String nombre);
    List<Categoria> findByNombreContainingIgnoreCase(String texto);
    List<Categoria> findByPersonajeCategoriaListIsNotEmpty();
    List<Categoria> findDistinctByPersonajeCategoriaListTipo(String tipo);
}
