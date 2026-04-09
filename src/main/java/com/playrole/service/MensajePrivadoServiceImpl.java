package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.dto.MensajePrivadoDTO;
import com.playrole.enums.EstadoMensaje;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.MensajePrivado;
import com.playrole.model.Usuario;
import com.playrole.repository.MensajePrivadoRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.utils.HtmlUtils;

import jakarta.transaction.Transactional;

@Service
public class MensajePrivadoServiceImpl implements IMensajePrivadoService {

    private final SolicitudAmistadServiceImpl solicitudAmistadServiceImpl;
	
	@Autowired
    private MensajePrivadoRepositoryInterface mensajePrivadoRepository;

    @Autowired
    private UsuarioRepositoryInterface usuarioRepository;

    MensajePrivadoServiceImpl(SolicitudAmistadServiceImpl solicitudAmistadServiceImpl) {
        this.solicitudAmistadServiceImpl = solicitudAmistadServiceImpl;
    }
    
    @Override
    @Transactional
    public MensajePrivadoDTO obtenerMensaje(Integer idMensaje, Integer idUsuario) {

        MensajePrivado mensaje = mensajePrivadoRepository.findById(idMensaje)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        Integer emisorId = mensaje.getEmisorId().getUserId();
        Integer receptorId = mensaje.getReceptorId().getUserId();

        //verificar que el usuario pertenece al mensaje
        if (!idUsuario.equals(emisorId) && !idUsuario.equals(receptorId)) {
            throw new AccessDeniedException("No tienes permiso para ver este mensaje");
        }

        //si lo abre el receptor y esta en estado de no leido marcar como leido
        if (idUsuario.equals(receptorId) && mensaje.getEstadoReceptor() == EstadoMensaje.NO_LEIDO) {
            mensajePrivadoRepository.marcarComoLeido(idMensaje, idUsuario);
            mensaje.setEstadoReceptor(EstadoMensaje.LEIDO);
        }

        return MensajePrivadoDTO.fromEntity(mensaje, idUsuario);
    }

    @Override
    public List<MensajePrivadoDTO> mensajesRecibidos(Integer idUsuario) {
        return mensajePrivadoRepository.findMensajesRecibidos(idUsuario)
                .stream()
                .map(m -> MensajePrivadoDTO.fromEntity(m, idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public List<MensajePrivadoDTO> mensajesEnviados(Integer idUsuario) {
        return mensajePrivadoRepository.findMensajesEnviados(idUsuario)
                .stream()
                .map(m -> MensajePrivadoDTO.fromEntity(m, idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public List<MensajePrivadoDTO> mensajesNoLeidos(Integer idUsuario) {
        return mensajePrivadoRepository.findNoLeidos(idUsuario)
                .stream()
                .map(m -> MensajePrivadoDTO.fromEntity(m, idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public MensajePrivadoDTO enviarMensaje(MensajePrivadoDTO dto) {

        Usuario emisor = usuarioRepository.findById(dto.getEmisorId())
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado"));

        Usuario receptor = usuarioRepository.findById(dto.getReceptorId())
                .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado"));
        
        if (dto.getEmisorId().equals(dto.getReceptorId())) {
            throw new BadRequestException("No puedes enviarte mensajes a ti mismo");
        }
        
        if (dto.getTitulo() == null || dto.getTitulo().isBlank()) {
            throw new BadRequestException("El título del mensaje no puede estar vacío");
        }

        if (dto.getTitulo().length() > 255) {
            throw new BadRequestException("El título no puede superar 255 caracteres");
        }

        if (dto.getContenido() == null || dto.getContenido().isBlank()) {
            throw new BadRequestException("El contenido del mensaje no puede estar vacío");
        }

        if (dto.getContenido().length() > 2000) {
            throw new BadRequestException("El contenido del mensaje no puede superar 2000 caracteres");
        }

        // Sanitizar contenido
        String contenidoLimpio = HtmlUtils.sanitize(dto.getContenido());
        dto.setContenido(contenidoLimpio);

        // Crear entidad
        MensajePrivado mensaje = dto.toEntity(emisor, receptor);

        mensaje.setEstadoEmisor(EstadoMensaje.LEIDO);
        mensaje.setEstadoReceptor(EstadoMensaje.NO_LEIDO);
        mensaje.setFechaEnvio(new Date());

        MensajePrivado guardado = mensajePrivadoRepository.save(mensaje);

        return MensajePrivadoDTO.fromEntity(guardado, dto.getEmisorId());
    }

    @Override
    public void marcarComoLeido(Integer idMensaje, Integer idUsuario) {
    	//recuperamos el mensaje
    	MensajePrivado mensaje = mensajePrivadoRepository.findById(idMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
    	//solo dejamos marcar como leido si es el receptor
        if (!mensaje.getReceptorId().getUserId().equals(idUsuario)) {
            throw new RuntimeException("Solo el receptor puede marcar el mensaje como leído");
        }

        mensajePrivadoRepository.marcarComoLeido(idMensaje, idUsuario);
    }

    @Override
    @Transactional
    public void eliminarMensaje(Integer idMensaje, Integer idUsuario) {
        mensajePrivadoRepository.eliminarParaUsuario(idMensaje, idUsuario);
    }

    @Override
    public Long contarNoLeidos(Integer idUsuario) {
        return mensajePrivadoRepository.contarNoLeidos(idUsuario);
    }

	@Override
	public List<MensajePrivadoDTO> filtrarPorEstadoRecibidos(Integer idUsuario, List<EstadoMensaje> estados) {
		return mensajePrivadoRepository.filtrarPorEstadoRecibidos(idUsuario, estados)
	            .stream()
	            .map(m -> MensajePrivadoDTO.fromEntity(m, idUsuario))
	            .collect(Collectors.toList());
	}
}
