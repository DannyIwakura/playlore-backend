package com.playrole.service;

import java.util.List;

import com.playrole.dto.MensajePrivadoDTO;

public interface IMensajePrivadoService {
	
	List<MensajePrivadoDTO> mensajesRecibidos(Integer idUsuario);
	List<MensajePrivadoDTO> mensajesEnviados(Integer idUsuario);
	List<MensajePrivadoDTO> obtenerMensajesNoLeidos(Integer idUsuario);
	MensajePrivadoDTO enviarMensaje(Integer idEmisor, Integer idReceptor, String contenido);
	List<MensajePrivadoDTO> findByEstado(Integer idUsuario, List<Integer> estados);
	void marcarComoLeido(Integer idMensaje);
	void eliminarMensaje(Integer id);
}
