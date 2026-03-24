package com.playrole.service;

import java.util.List;

import com.playrole.model.MensajePrivado;

public interface IMensajePrivadoService {
	
	List<MensajePrivado> mensajesRecibidos(Long idUsuario);
	List<MensajePrivado> mensajesEnviados(Long idUsuario);
	List<MensajePrivado> obtenerMensajesNoLeidos(Long idUsuario);
	MensajePrivado enviarMensaje(Long idEmisor, Long idReceptor, String contenido);
	List<MensajePrivado> findByEstado(Long idUsuario, List<Integer> estados);
	void marcarComoLeido(Long idMensaje);
	void eliminarMensaje(Long id);
}
