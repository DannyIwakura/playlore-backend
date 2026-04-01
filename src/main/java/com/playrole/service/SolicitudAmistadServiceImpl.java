package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.enums.EstadoSolicitud;
import com.playrole.model.SolicitudAmistad;
import com.playrole.model.Usuario;
import com.playrole.repository.SolicitudAmistadRespositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

@Service
public class SolicitudAmistadServiceImpl implements ISolicitudAmistadService {

	@Autowired
	private SolicitudAmistadRespositoryInterface solicitudAmistadRepository;
	
	@Autowired
	private UsuarioRepositoryInterface usuarioRepository;
	
	@Override
	public SolicitudAmistad enviarSolicitud(Integer emisorId, Integer receptorId) {
		
		//evitar duplicados
        Optional<SolicitudAmistad> existente =
        		solicitudAmistadRepository.findByEmisorIdUserIdAndReceptorIdUserIdOrEmisorIdUserIdAndReceptorIdUserId(
                        emisorId, receptorId,
                        receptorId, emisorId);
		
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una solicitud entre estos usuarios");
        }
        
        //si no existe duplicidad recuperamos el emisor y el receptor
        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));
        
        //creamos la solicitud como objeto y la guardabmos en bbdd
        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setEmisorId(emisor);
        solicitud.setReceptorId(receptor);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaPeticion(new Date());

        return solicitudAmistadRepository.save(solicitud);
   	}

	@Override
	public List<SolicitudAmistad> obtenerSolicitudesEnviadas(Integer userId) {
		return solicitudAmistadRepository.findByEmisorIdUserId(userId);
	}

	@Override
	public List<SolicitudAmistad> obtenerSolicitudesRecibidas(Integer userId) {
		return solicitudAmistadRepository.findByReceptorIdUserId(userId);
	}

	@Override
	public List<SolicitudAmistad> obtenerSolicitudesPendientes(Integer userId) {
		return solicitudAmistadRepository.findByReceptorIdUserIdAndEstado(userId, EstadoSolicitud.PENDIENTE);
	}

	@Override
	public SolicitudAmistad aceptarSolicitud(Integer idSolicitud) {
		//recuperamos la solicitud
		SolicitudAmistad solicitud = solicitudAmistadRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		//cambismos el estado y fecha de respuesta
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaRespuesta(new Date());

        return solicitudAmistadRepository.save(solicitud);
	}

	@Override
	public SolicitudAmistad rechazarSolicitud(Integer idSolicitud) {
		SolicitudAmistad solicitud = solicitudAmistadRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(new Date());

        return solicitudAmistadRepository.save(solicitud);
	}

	@Override
	public boolean existeSolicitud(Integer emisorId, Integer receptorId) {
		Optional<SolicitudAmistad> solicitud =
				solicitudAmistadRepository.findByEmisorIdUserIdAndReceptorIdUserIdOrEmisorIdUserIdAndReceptorIdUserId(
                        emisorId, receptorId,
                        receptorId, emisorId);

        return solicitud.isPresent();
	}

	@Override
	public List<SolicitudAmistad> obtenerAmistades(Integer userId) {
		return solicitudAmistadRepository.findByEstadoAndEmisorIdUserIdOrEstadoAndReceptorIdUserId(
	            EstadoSolicitud.ACEPTADA, userId,
	            EstadoSolicitud.ACEPTADA, userId
	    );
	}

	@Override
	public Optional<SolicitudAmistad> obtenerPorId(Integer id) {
		return solicitudAmistadRepository.findById(id);
	}
}
