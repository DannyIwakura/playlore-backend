package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.dto.AmigoDTO;
import com.playrole.dto.SolicitudAmistadDTO;
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
	public SolicitudAmistadDTO enviarSolicitud(Integer emisorId, Integer receptorId) {
		
		//comprobamos si se está enviado a si mismo
	    if (emisorId.equals(receptorId)) {
	        throw new RuntimeException("No puedes enviarte una solicitud a ti mismo");
	    }
		
		//buscamos una solicitud que esté repetida o no
	    if (solicitudAmistadRepository.buscarSolicitudEntreUsuarios(emisorId, receptorId).isPresent()) {
	        throw new RuntimeException("Ya existe una solicitud o amistad");
	    }

	    //recuperamos el usuario emidor
	    Usuario emisor = usuarioRepository.findById(emisorId)
	            .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));
	    //recuperamos el usuario receptor
	    Usuario receptor = usuarioRepository.findById(receptorId)
	            .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));
	    
	    //una vez recuperados de la base de datos podemos crear la solicitud
	    SolicitudAmistad solicitud = new SolicitudAmistad();
	    solicitud.setEmisorId(emisor);
	    solicitud.setReceptorId(receptor);
	    solicitud.setEstado(EstadoSolicitud.PENDIENTE);
	    solicitud.setFechaPeticion(new Date());
	    //la guardamos en bbdd
	    SolicitudAmistad guardada = solicitudAmistadRepository.save(solicitud);
	    //devolvemos la solicitud creada
	    return SolicitudAmistadDTO.fromEntity(guardada);
   	}
	
	@Override
    public List<SolicitudAmistadDTO> obtenerSolicitudesPendientesRecibidas(Integer userId) {
        return solicitudAmistadRepository
                .findByReceptorIdUserIdAndEstado(userId, EstadoSolicitud.PENDIENTE)
                .stream()
                .map(SolicitudAmistadDTO::fromEntity)
                .toList();
    }

    @Override
    public List<SolicitudAmistadDTO> obtenerSolicitudesPendientesEnviadas(Integer userId) {
        return solicitudAmistadRepository
                .findByEmisorIdUserIdAndEstado(userId, EstadoSolicitud.PENDIENTE)
                .stream()
                .map(SolicitudAmistadDTO::fromEntity)
                .toList();
    }

	@Override
	public List<SolicitudAmistadDTO> obtenerSolicitudesPendientes(Integer userId) {
		return solicitudAmistadRepository
	            .findByReceptorIdUserIdAndEstado(userId, EstadoSolicitud.PENDIENTE)
	            .stream()
	            .map(SolicitudAmistadDTO::fromEntity)
	            .toList();
	}

	@Override
	public SolicitudAmistadDTO aceptarSolicitud(Integer idSolicitud) {
		//recuperamos la solicitud
		SolicitudAmistad solicitud = solicitudAmistadRepository.findById(idSolicitud)
	            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
		//cambiamos el estado y fecha de respuesta
	    solicitud.setEstado(EstadoSolicitud.ACEPTADA);
	    solicitud.setFechaRespuesta(new Date());
	    //la guardamos modificada
	    SolicitudAmistad guardada = solicitudAmistadRepository.save(solicitud);

	    return SolicitudAmistadDTO.fromEntity(guardada);
	}

	@Override
	public boolean existeSolicitud(Integer emisorId, Integer receptorId) {
	    return solicitudAmistadRepository
	           .buscarSolicitudEntreUsuarios(emisorId, receptorId)
	           .isPresent();
	}
	
	@Override
	public List<AmigoDTO> obtenerAmigos(Integer userId) {
		//recuperamos las amistades que esten aceptadas
	    List<SolicitudAmistad> solicitudes =
	        solicitudAmistadRepository
	        .findByEstadoAndEmisorIdUserIdOrEstadoAndReceptorIdUserId(
	                EstadoSolicitud.ACEPTADA, userId,
	                EstadoSolicitud.ACEPTADA, userId
	        );
	    //las devolvemos convirtiendolas a una lista
	    return solicitudes.stream()
	            .map(s -> {
	                if (s.getEmisorId().getUserId().equals(userId)) {
	                    return new AmigoDTO(
	                        s.getReceptorId().getUserId(),
	                        s.getReceptorId().getNombre(),
	                        s.getReceptorId().getAvatar()
	                    );
	                } else {
	                    return new AmigoDTO(
	                        s.getEmisorId().getUserId(),
	                        s.getEmisorId().getNombre(),
	                        s.getEmisorId().getAvatar()
	                    );
	                }
	            })
	            .toList();
	}

	@Override
	public Optional<SolicitudAmistadDTO> obtenerPorId(Integer id) {
		 return solicitudAmistadRepository.findById(id)
		            .map(SolicitudAmistadDTO::fromEntity);
	}
	
	@Override
	public void eliminarSolicitud(Integer idSolicitud) {
	    if (!solicitudAmistadRepository.existsById(idSolicitud)) {
	        throw new RuntimeException("Solicitud no encontrada");
	    }
	    solicitudAmistadRepository.eliminarPorId(idSolicitud);
	}
	
	@Override
	public void eliminarAmistadEntreUsuarios(Integer userId1, Integer userId2) {
		solicitudAmistadRepository.eliminarAmistadEntreUsuarios(userId1, userId2);
	}
}
