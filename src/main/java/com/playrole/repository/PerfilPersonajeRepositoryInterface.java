package com.playrole.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.enums.RolUsuario;
import com.playrole.model.PerfilPersonaje;

import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

public interface PerfilPersonajeRepositoryInterface extends JpaRepository<PerfilPersonaje, Integer>,
JpaSpecificationExecutor<PerfilPersonaje> {
	
	List<PerfilPersonaje> findByUserId_UserId(Integer userId);
	Page<PerfilPersonaje> findByUserId_UserId(Integer userId, Pageable pageable);

	/**
	 * Rol de la cuenta propietaria por personaje, en una sola consulta. Se usa
	 * para marcar el staff en los listados de miembros sin caer en N+1.
	 */
	@Query("SELECT p.idPersonaje AS personajeId, p.userId.rol AS rol FROM PerfilPersonaje p " +
	       "WHERE p.idPersonaje IN :personajeIds")
	List<RolUsuarioPorPersonaje> findRolesUsuarioPorPersonajes(@Param("personajeIds") Collection<Integer> personajeIds);

	@Query("SELECT p FROM PerfilPersonaje p JOIN FETCH p.userId u " +
	       "WHERE u.rol IN (com.playrole.enums.RolUsuario.ADMIN, com.playrole.enums.RolUsuario.MOD) " +
	       "AND (:q IS NULL OR :q = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
	       "     OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))) " +
	       "ORDER BY p.nombre ASC")
	List<PerfilPersonaje> buscarPersonajesDeStaff(@Param("q") String q, Pageable pageable);

	interface RolUsuarioPorPersonaje {
		Integer getPersonajeId();

		RolUsuario getRol();
	}
	
	//metodo con quuery para evitar que JPA haga consultas select al eliminar
	@Modifying
    @Transactional
    @Query("DELETE FROM PerfilPersonaje p WHERE p.idPersonaje = :id")
    void deleteByIdDirect(@Param("id") Integer id);
}
