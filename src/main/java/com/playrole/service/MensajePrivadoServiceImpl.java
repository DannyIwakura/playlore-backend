package com.playrole.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.playrole.model.MensajePrivado;
import com.playrole.model.Usuario;
import com.playrole.repository.MensajePrivadoRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

public class MensajePrivadoServiceImpl implements IMensajePrivadoService {
	
	@Autowired
	private MensajePrivadoRepositoryInterface mensajePrivadoRepository;
	
	@Autowired
	private UsuarioRepositoryInterface usuarioRepository;

	@Override
	public List<MensajePrivado> mensajesRecibidos(Integer idUsuario) {
		return mensajePrivadoRepository.findByReceptorId_UserIdOrderByFechaEnvioDesc(idUsuario);
	}

	@Override
	public List<MensajePrivado> mensajesEnviados(Integer idUsuario) {
		return mensajePrivadoRepository.findByEmisorId_UserIdOrderByFechaEnvioDesc(idUsuario);
	}

	@Override
	public List<MensajePrivado> obtenerMensajesNoLeidos(Integer idUsuario) {
		return mensajePrivadoRepository.findByReceptorId_UserIdAndEstadoOrderByFechaEnvioDesc(idUsuario, 0);
	}

	@Override
	public MensajePrivado enviarMensaje(Integer idEmisor, Integer idReceptor, String contenido) {
		//necesitamos obtener los ids del receptor y emisor
		Usuario emisor = usuarioRepository.findById(idEmisor)
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));
        Usuario receptor = usuarioRepository.findById(idReceptor)
                .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));
        
        //creamos un nuevo mensaje setteando campos
        MensajePrivado mensaje = new MensajePrivado();
        mensaje.setEmisorId(emisor);
        mensaje.setReceptorId(receptor);
        mensaje.setContenido(contenido);
        mensaje.setEstado(0);
        mensaje.setFechaEnvio(new Date());
        
        return mensajePrivadoRepository.save(mensaje);
	}

	@Override
	public List<MensajePrivado> findByEstado(Integer idUsuario, List<Integer> estados) {
		return mensajePrivadoRepository.findByReceptorId_UserIdAndEstadoInOrderByFechaEnvioDesc(idUsuario, estados);
	}

	@Override
	public void marcarComoLeido(Integer idMensaje) {
		//buscamos el mensaje a marcar
		MensajePrivado mensaje = mensajePrivadoRepository.findById(idMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        //camiamos a leido
		mensaje.setEstado(1);
        mensajePrivadoRepository.save(mensaje);
		
	}

	@Override
	public void eliminarMensaje(Integer id) {
		mensajePrivadoRepository.deleteById(id);
	}
}
