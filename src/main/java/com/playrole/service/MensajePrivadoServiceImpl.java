package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.dto.MensajePrivadoDTO;
import com.playrole.model.MensajePrivado;
import com.playrole.model.Usuario;
import com.playrole.repository.MensajePrivadoRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

@Service
public class MensajePrivadoServiceImpl implements IMensajePrivadoService {
	
	@Autowired
    private MensajePrivadoRepositoryInterface mensajePrivadoRepository;

    @Autowired
    private UsuarioRepositoryInterface usuarioRepository;

    private MensajePrivadoDTO convertirADTO(MensajePrivado mensaje) {
        return MensajePrivadoDTO.fromEntity(mensaje);
    }

    @Override
    public List<MensajePrivadoDTO> mensajesRecibidos(Integer idUsuario) {
        return mensajePrivadoRepository.findByReceptorId_UserIdOrderByFechaEnvioDesc(idUsuario)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MensajePrivadoDTO> mensajesEnviados(Integer idUsuario) {
        return mensajePrivadoRepository.findByEmisorId_UserIdOrderByFechaEnvioDesc(idUsuario)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MensajePrivadoDTO> obtenerMensajesNoLeidos(Integer idUsuario) {
        return mensajePrivadoRepository.findByReceptorId_UserIdAndEstadoOrderByFechaEnvioDesc(idUsuario, 0)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public MensajePrivadoDTO enviarMensaje(Integer idEmisor, Integer idReceptor, String contenido) {
        Usuario emisor = usuarioRepository.findById(idEmisor)
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));
        Usuario receptor = usuarioRepository.findById(idReceptor)
                .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));

        MensajePrivado mensaje = new MensajePrivado();
        mensaje.setEmisorId(emisor);
        mensaje.setReceptorId(receptor);
        mensaje.setContenido(contenido);
        mensaje.setEstado(0);
        mensaje.setFechaEnvio(new Date());

        MensajePrivado guardado = mensajePrivadoRepository.save(mensaje);
        return convertirADTO(guardado);
    }

    @Override
    public List<MensajePrivadoDTO> findByEstado(Integer idUsuario, List<Integer> estados) {
        return mensajePrivadoRepository.findByReceptorId_UserIdAndEstadoInOrderByFechaEnvioDesc(idUsuario, estados)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public void marcarComoLeido(Integer idMensaje) {
        MensajePrivado mensaje = mensajePrivadoRepository.findById(idMensaje)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        mensaje.setEstado(1);
        mensajePrivadoRepository.save(mensaje);
    }

    @Override
    public void eliminarMensaje(Integer id) {
        mensajePrivadoRepository.deleteById(id);
    }
}
