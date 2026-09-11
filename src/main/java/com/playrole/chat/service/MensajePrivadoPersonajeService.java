package com.playrole.chat.service;

import com.playrole.chat.dto.MensajePrivadoPersonajeDTO;
import com.playrole.chat.model.MensajePrivadoPersonaje;
import com.playrole.chat.repository.MensajePrivadoPersonajeRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.exception.TooManyRequestsException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.utils.HtmlUtils;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MensajePrivadoPersonajeService {

    private static final int MAX_MENSAJES_VENTANA = 10;
    private static final long VENTANA_MS = 30_000L;
    private static final int MAX_CONTENIDO_LONGITUD = 2000;

    private final Map<Integer, Deque<Long>> enviosRecientes = new ConcurrentHashMap<>();
    private final MensajePrivadoPersonajeRepository mensajeRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final BaneoGlobalRepository baneoGlobalRepository;

    public MensajePrivadoPersonajeService(MensajePrivadoPersonajeRepository mensajeRepository,
                                           PerfilPersonajeRepositoryInterface personajeRepository,
                                           SimpMessagingTemplate messagingTemplate,
                                           BaneoGlobalRepository baneoGlobalRepository) {
        this.mensajeRepository = mensajeRepository;
        this.personajeRepository = personajeRepository;
        this.messagingTemplate = messagingTemplate;
        this.baneoGlobalRepository = baneoGlobalRepository;
    }

    @Transactional
    public MensajePrivadoPersonajeDTO enviarMensaje(Integer emisorId, Integer receptorId, String contenido) {
        if (emisorId.equals(receptorId)) {
            throw new BadRequestException("No puedes enviarte mensajes a ti mismo");
        }

        if (contenido == null || contenido.isBlank()) {
            throw new BadRequestException("El contenido del mensaje no puede estar vacío");
        }
        if (contenido.length() > MAX_CONTENIDO_LONGITUD) {
            throw new BadRequestException("El mensaje no puede superar " + MAX_CONTENIDO_LONGITUD + " caracteres");
        }

        verificarAntiFlood(emisorId);

        PerfilPersonaje emisor = personajeRepository.findById(emisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje emisor no encontrado"));

        if (baneoGlobalRepository.existsActivoByUsuarioOPersonaje(
                emisor.getUserId().getUserId(), emisorId)) {
            throw new AccessDeniedException("Tu cuenta o personaje está suspendido.");
        }

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

    @Transactional
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

    @Transactional
    public void eliminarConversacion(Integer personajeId, Integer otroPersonajeId) {
        List<MensajePrivadoPersonaje> mensajes = mensajeRepository.findConversacionEntre(personajeId, otroPersonajeId);
        for (MensajePrivadoPersonaje mensaje : mensajes) {
            if (mensaje.getEmisor().getIdPersonaje().equals(personajeId)) {
                mensaje.setEliminadoEmisor(true);
            } else {
                mensaje.setEliminadoReceptor(true);
            }
            mensajeRepository.save(mensaje);
        }
    }

    public List<Integer> obtenerContactos(Integer personajeId) {
        return mensajeRepository.findContactIds(personajeId);
    }

    private void verificarAntiFlood(Integer personajeId) {
        long ahora = System.currentTimeMillis();
        Deque<Long> envios = enviosRecientes.computeIfAbsent(personajeId, k -> new ArrayDeque<>());
        synchronized (envios) {
            while (!envios.isEmpty() && ahora - envios.peekFirst() > VENTANA_MS) {
                envios.pollFirst();
            }
            if (envios.size() >= MAX_MENSAJES_VENTANA) {
                throw new TooManyRequestsException("Estás enviando mensajes demasiado rápido. Espera unos segundos.");
            }
            envios.addLast(ahora);
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void limpiarEnviosExpirados() {
        long ahora = System.currentTimeMillis();
        enviosRecientes.entrySet().removeIf(entry -> {
            Deque<Long> envios = entry.getValue();
            synchronized (envios) {
                while (!envios.isEmpty() && ahora - envios.peekFirst() > VENTANA_MS) {
                    envios.pollFirst();
                }
                return envios.isEmpty();
            }
        });
    }
}
