package com.playrole.service;

import java.util.List;

import com.playrole.dto.MensajePrivadoDTO;
import com.playrole.enums.EstadoMensaje;

public interface IMensajePrivadoService {
	
	MensajePrivadoDTO obtenerMensaje(Integer idMensaje, Integer idUsuario);
	
	List<MensajePrivadoDTO> mensajesRecibidos(Integer idUsuario);

	List<MensajePrivadoDTO> mensajesEnviados(Integer idUsuario);

	List<MensajePrivadoDTO> mensajesNoLeidos(Integer idUsuario);

	MensajePrivadoDTO enviarMensaje(MensajePrivadoDTO mensajeDTO);

	List<MensajePrivadoDTO> filtrarPorEstadoRecibidos(Integer idUsuario, List<EstadoMensaje> estados);

	Long contarNoLeidos(Integer idUsuario);

	void marcarComoLeido(Integer idMensaje, Integer idUsuario);

	void eliminarMensaje(Integer idMensaje, Integer idUsuario);
}
