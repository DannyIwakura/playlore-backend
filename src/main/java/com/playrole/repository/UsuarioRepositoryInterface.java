package com.playrole.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.model.Usuario;

public interface UsuarioRepositoryInterface extends JpaRepository<Usuario, Integer> {

	boolean existsByEmail(String email);
	Optional<Usuario> findByNombre(String nombre);
}
