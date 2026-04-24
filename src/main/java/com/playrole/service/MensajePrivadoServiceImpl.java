package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.dto.MensajePrivadoDTO;
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

    @Autowired
    private MensajePrivadoRepositoryInterface mensajePrivadoRepository;

    @Autowired
    private UsuarioRepositoryInterface usuarioRepository;

    @Override
    @Transactional
    public MensajePrivadoDTO obtenerMensaje(Integer idMensaje, Integer idUsuario) {
        MensajePrivado mensaje = mensajePrivadoRepository.findById(idMensaje)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        // Verificar que el usuario pertenece al mensaje y es visible para él
        boolean esEmisor = mensaje.getEmisor().getUserId().equals(idUsuario);
        boolean esReceptor = mensaje.getReceptor().getUserId().equals(idUsuario);

        if (!esEmisor && !esReceptor) {
            throw new AccessDeniedException("No tienes permiso para ver este mensaje");
        }

        // Si lo abre el receptor y no está leído, marcarlo
        if (esReceptor && !mensaje.isLeido()) {
            mensajePrivadoRepository.marcarComoLeido(idMensaje, idUsuario);
            mensaje.setLeido(true);
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
    public List<MensajePrivadoDTO> mensajesArchivados(Integer idUsuario) {
        return mensajePrivadoRepository.findArchivados(idUsuario)
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
    @Transactional
    public MensajePrivadoDTO enviarMensaje(MensajePrivadoDTO dto) {
        Usuario emisor = usuarioRepository.findById(dto.getEmisorId())
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado"));

        Usuario receptor = usuarioRepository.findById(dto.getReceptorId())
                .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado"));

        validarMensaje(dto);

        // Sanitizar contenido
        dto.setContenido(HtmlUtils.sanitize(dto.getContenido()));

        // Crear entidad con valores por defecto del nuevo esquema
        MensajePrivado mensaje = dto.toEntity(emisor, receptor);
        
        MensajePrivado guardado = mensajePrivadoRepository.save(mensaje);
        return MensajePrivadoDTO.fromEntity(guardado, dto.getEmisorId());
    }

    @Override
    @Transactional
    public void marcarComoLeido(Integer idMensaje, Integer idUsuario) {
        mensajePrivadoRepository.marcarComoLeido(idMensaje, idUsuario);
    }

    @Override
    @Transactional
    public void archivarMensaje(Integer idMensaje, Integer idUsuario) {
        mensajePrivadoRepository.archivarParaUsuario(idMensaje, idUsuario);
    }

    @Override
    @Transactional
    public void eliminarMensaje(Integer idMensaje, Integer idUsuario) {
        mensajePrivadoRepository.ocultarParaUsuario(idMensaje, idUsuario);
    }
    
    @Override
    @Transactional
    public void eliminarDefinitivamente(Integer idMensaje, Integer idUsuario) {
        MensajePrivado mensaje = mensajePrivadoRepository.findById(idMensaje)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        boolean esEmisor = mensaje.getEmisor().getUserId().equals(idUsuario);

        if (esEmisor) {
            mensaje.setEliminadoEmisor(true);
        } else {
            mensaje.setEliminadoReceptor(true);
        }
        mensajePrivadoRepository.save(mensaje);

        // Si ambos lo han eliminado → borrar de BD
        if (mensaje.isEliminadoEmisor() && mensaje.isEliminadoReceptor()) {
            mensajePrivadoRepository.delete(mensaje);
        }
    }
    
    @Override
    public List<MensajePrivadoDTO> mensajesPapelera(Integer idUsuario) {
        return mensajePrivadoRepository.findPapelera(idUsuario)
                .stream()
                .map(m -> MensajePrivadoDTO.fromEntity(m, idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public Long contarNoLeidos(Integer idUsuario) {
        return mensajePrivadoRepository.contarNoLeidos(idUsuario);
    }

    @Override
    public List<MensajePrivadoDTO> listarPorCriterio(Integer idUsuario, boolean soloLeidos, boolean soloArchivados) {
        return mensajesRecibidos(idUsuario).stream()
                .filter(m -> !soloLeidos || m.isLeido())
                .filter(m -> !soloArchivados || m.isArchivado())
                .collect(Collectors.toList());
    }

    private void validarMensaje(MensajePrivadoDTO dto) {
        if (dto.getEmisorId().equals(dto.getReceptorId())) {
            throw new BadRequestException("No puedes enviarte mensajes a ti mismo");
        }
        if (dto.getTitulo() == null || dto.getTitulo().isBlank()) {
            throw new BadRequestException("El título no puede estar vacío");
        }
        if (dto.getContenido() == null || dto.getContenido().isBlank()) {
            throw new BadRequestException("El contenido no puede estar vacío");
        }
    }
}