package com.playrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.model.Usuario;

public interface UsuarioRepositoryInterface extends JpaRepository<Usuario, Integer> {

	boolean existsByEmail(String email);
}
