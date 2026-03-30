package com.playrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.model.PerfilPersonaje;
import java.util.List;

public interface PerfilPersonajeRepositoryInterface extends JpaRepository<PerfilPersonaje, Integer> {
	
	List<PerfilPersonaje> findByUserId_UserId(Integer userId);
}
