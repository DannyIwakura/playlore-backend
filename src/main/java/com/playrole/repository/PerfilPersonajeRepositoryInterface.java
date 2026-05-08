package com.playrole.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.model.PerfilPersonaje;

import jakarta.transaction.Transactional;

import java.util.List;

public interface PerfilPersonajeRepositoryInterface extends JpaRepository<PerfilPersonaje, Integer>,
JpaSpecificationExecutor<PerfilPersonaje> {
	
	List<PerfilPersonaje> findByUserId_UserId(Integer userId);
	Page<PerfilPersonaje> findByUserId_UserId(Integer userId, Pageable pageable);
	
	//metodo con quuery para evitar que JPA haga consultas select al eliminar
	@Modifying
    @Transactional
    @Query("DELETE FROM PerfilPersonaje p WHERE p.idPersonaje = :id")
    void deleteByIdDirect(@Param("id") Integer id);
}
