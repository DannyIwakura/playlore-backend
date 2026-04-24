package com.playrole.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.dto.SolicitudAmistadDTO;
import com.playrole.enums.EstadoSolicitud;
import com.playrole.model.SolicitudAmistad;

import jakarta.transaction.Transactional;

public interface SolicitudAmistadRespositoryInterface extends JpaRepository<SolicitudAmistad, Integer> {

	List<SolicitudAmistad> findByEmisorIdUserId(Integer userId);
	List<SolicitudAmistad> findByReceptorIdUserId(Integer userId);
	//metodo para comprobar la duplicidad de peticion en ambas direcciones
	@Query("SELECT s FROM SolicitudAmistad s WHERE " +
		       "(s.emisorId.userId = :id1 AND s.receptorId.userId = :id2) OR " +
		       "(s.emisorId.userId = :id2 AND s.receptorId.userId = :id1)")
	Optional<SolicitudAmistad> buscarSolicitudEntreUsuarios(
	    @Param("id1") Integer id1, 
	    @Param("id2") Integer id2
	);
	List<SolicitudAmistad> findByReceptorIdUserIdAndEstado(Integer userId, EstadoSolicitud estado);
	List<SolicitudAmistad> findByEmisorIdUserIdAndEstado(Integer userId, EstadoSolicitud estado);
	//recuperar solicitudas aceptadas
	List<SolicitudAmistad> findByEstadoAndEmisorIdUserIdOrEstadoAndReceptorIdUserId(
	        EstadoSolicitud estado1, Integer userId1,
	        EstadoSolicitud estado2, Integer userId2);
	
	//metodo con quuery para evitar que JPA haga consultas select al eliminar
	@Transactional
    @Modifying
    @Query("DELETE FROM SolicitudAmistad s " +
           "WHERE (s.emisorId.userId = :userId1 AND s.receptorId.userId = :userId2) " +
           "   OR (s.emisorId.userId = :userId2 AND s.receptorId.userId = :userId1)")
    void eliminarAmistadEntreUsuarios(@Param("userId1") Integer userId1,
                                      @Param("userId2") Integer userId2);
	 //cancelar
	 @Transactional
	 @Modifying
	 @Query("DELETE FROM SolicitudAmistad s WHERE s.id = :id")
	 void eliminarPorId(@Param("id") Integer id);
}