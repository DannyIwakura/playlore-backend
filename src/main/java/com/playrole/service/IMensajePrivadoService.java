package com.playrole.service;

import java.util.List;

import com.playrole.model.MensajePrivado;

public interface IMensajePrivadoService {
	
	List<MensajePrivado> mensajesRecibidos(Integer idUsuario);
	List<MensajePrivado> mensajesEnviados(Integer idUsuario);
	List<MensajePrivado> obtenerMensajesNoLeidos(Integer idUsuario);
	MensajePrivado enviarMensaje(Integer idEmisor, Integer idReceptor, String contenido);
	List<MensajePrivado> findByEstado(Integer idUsuario, List<Integer> estados);
	void marcarComoLeido(Integer idMensaje);
	void eliminarMensaje(Integer id);
}
