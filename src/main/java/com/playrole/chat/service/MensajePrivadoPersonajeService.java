package com.playrole.chat.service;

import com.playrole.chat.dto.MensajePrivadoPersonajeDTO;
import com.playrole.chat.model.MensajePrivadoPersonaje;
import com.playrole.chat.repository.MensajePrivadoPersonajeRepository;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.utils.HtmlUtils;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajePrivadoPersonajeService {

    private final MensajePrivadoPersonajeRepository mensajeRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MensajePrivadoPersonajeService(MensajePrivadoPersonajeRepository mensajeRepository,
                                           PerfilPersonajeRepositoryInterface personajeRepository,
                                           SimpMessagingTemplate messagingTemplate) {
        this.mensajeRepository = mensajeRepository;
        this.personajeRepository = personajeRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public MensajePrivadoPersonajeDTO enviarMensaje(Integer emisorId, Integer receptorId, String contenido) {
        if (emisorId.equals(receptorId)) {
            throw new BadRequestException("No puedes enviarte mensajes a ti mismo");
        }

        PerfilPersonaje emisor = personajeRepository.findById(emisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje emisor no encontrado"));

        PerfilPersonaje receptor = personajeRepository.findById(receptorId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje receptor no encontrado"));

        String sanitizado = HtmlUtils.sanitize(contenido);

        MensajePrivadoPersonaje mensaje = new MensajePrivadoPersonaje();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setContenido(sanitizado);
        mensaje.setFechaEnvio(new Date());
        mensaje.setLeido(false);

        mensaje = mensajeRepository.save(mensaje);

        MensajePrivadoPersonajeDTO dto = MensajePrivadoPersonajeDTO.fromEntity(mensaje, emisorId);

        messagingTemplate.convertAndSend(
                "/topic/privado." + receptorId,
                dto);

        return dto;
    }

    public List<MensajePrivadoPersonajeDTO> obtenerConversacion(Integer personajeId1, Integer personajeId2, Integer currentPersonajeId) {
        return mensajeRepository.findConversacionEntre(personajeId1, personajeId2).stream()
                .map(m -> {
                    if (m.getReceptor().getIdPersonaje().equals(currentPersonajeId) && !m.isLeido()) {
                        m.setLeido(true);
                        mensajeRepository.save(m);
                    }
                    return MensajePrivadoPersonajeDTO.fromEntity(m, currentPersonajeId);
                })
                .collect(Collectors.toList());
    }

    public List<MensajePrivadoPersonajeDTO> listarConversaciones(Integer personajeId) {
        return mensajeRepository.findConversaciones(personajeId).stream()
                .map(m -> MensajePrivadoPersonajeDTO.fromEntity(m, personajeId))
                .collect(Collectors.toList());
    }

    public long contarNoLeidos(Integer personajeId) {
        return mensajeRepository.countNoLeidos(personajeId);
    }

    public List<Integer> obtenerContactos(Integer personajeId) {
        return mensajeRepository.findContactIds(personajeId);
    }
}
