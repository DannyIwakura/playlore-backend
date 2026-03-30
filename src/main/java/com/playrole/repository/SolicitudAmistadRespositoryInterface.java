package com.playrole.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.enums.EstadoSolicitud;
import com.playrole.model.SolicitudAmistad;

public interface SolicitudAmistadRespositoryInterface extends JpaRepository<SolicitudAmistad, Integer> {

	List<SolicitudAmistad> findByEmisorIdUserId(Integer userId);
	List<SolicitudAmistad> findByReceptorIdUserId(Integer userId);
	Optional<SolicitudAmistad> findByEmisorIdUserIdAndReceptorIdUserId(Integer emisorId, Integer receptorId);
	//metodo para comprobar la duplicidad de peticion en ambas direcciones
	Optional<SolicitudAmistad> findByEmisorIdUserIdAndReceptorIdUserIdOrEmisorIdUserIdAndReceptorIdUserId(
			Integer emisor1, Integer receptor1,
			Integer emisor2, Integer receptor2);
	List<SolicitudAmistad> findByReceptorIdUserIdAndEstado(Integer userId, EstadoSolicitud estado);
	//recuperar solicitudas aceptadas
	List<SolicitudAmistad> findByEstadoAndEmisorIdUserIdOrEstadoAndReceptorIdUserId(
	        EstadoSolicitud estado1, Integer userId1,
	        EstadoSolicitud estado2, Integer userId2);
}