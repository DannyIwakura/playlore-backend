package com.playrole.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playrole.model.Categoria;

@Repository
public interface CategoríaRepositoryInterface extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByNombre(String nombre);
}