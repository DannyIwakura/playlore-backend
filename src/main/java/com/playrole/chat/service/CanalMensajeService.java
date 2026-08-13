package com.playrole.chat.service;

import com.playrole.chat.dto.MensajeCanalDTO;
import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MensajeCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.exception.TooManyRequestsException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.utils.HtmlUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CanalMensajeService {

    private static final int MAX_MENSAJES_VENTANA = 5;
    private static final long VENTANA_MS = 10_000L;
    private static final int MAX_CONTENIDO_LONGITUD = 2000;

    private final Map<Integer, Deque<Long>> enviosRecientes = new ConcurrentHashMap<>();

    private final MensajeCanalRepository mensajeRepository;
    private final CanalRepository canalRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final CanalPermissionService permissionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BaneoGlobalRepository baneoGlobalRepository;

    public CanalMensajeService(MensajeCanalRepository mensajeRepository,
                                CanalRepository canalRepository,
                                PerfilPersonajeRepositoryInterface personajeRepository,
                                CanalPermissionService permissionService,
                                SimpMessagingTemplate messagingTemplate,
                                BaneoGlobalRepository baneoGlobalRepository) {
        this.mensajeRepository = mensajeRepository;
        this.canalRepository = canalRepository;
        this.personajeRepository = personajeRepository;
        this.permissionService = permissionService;
        this.messagingTemplate = messagingTemplate;
        this.baneoGlobalRepository = baneoGlobalRepository;
    }

    public Page<MensajeCanalDTO> obtenerMensajes(Integer canalId, Integer personajeId, int page, int size) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.LEER_MENSAJES);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaEnvio"));
        return mensajeRepository.findMensajesByCanal(canalId, pageRequest)
                .map(m -> MensajeCanalDTO.fromEntity(m, personajeId));
    }

    @Transactional
    public MensajeCanalDTO enviarMensaje(Integer canalId, Integer personajeId, String contenido) {
        return enviarMensaje(canalId, personajeId, contenido, null);
    }

    @Transactional
    public MensajeCanalDTO enviarMensaje(Integer canalId, Integer personajeId, String contenido, Integer mensajePadreId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.ENVIAR_MENSAJES);

        PerfilPersonaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        if (baneoGlobalRepository.existsActivoByUsuarioOPersonaje(personaje.getUserId().getUserId(), personajeId)) {
            throw new AccessDeniedException("Tu cuenta o personaje está suspendido.");
        }

        verificarAntiFlood(personajeId);

        String sanitizado = HtmlUtils.sanitize(contenido);
        if (sanitizado.length() > MAX_CONTENIDO_LONGITUD) {
            throw new BadRequestException("El mensaje no puede superar los " + MAX_CONTENIDO_LONGITUD + " caracteres");
        }

        MensajeCanal mensaje = new MensajeCanal();
        mensaje.setCanal(canal);
        mensaje.setPersonaje(personaje);
        mensaje.setContenido(sanitizado);
        mensaje.setFechaEnvio(new Date());

        if (mensajePadreId != null) {
            MensajeCanal padre = mensajeRepository.findById(mensajePadreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mensaje a citar no encontrado"));
            if (!padre.getCanal().getIdCanal().equals(canalId)) {
                throw new BadRequestException("No se puede citar un mensaje de otro canal");
            }
            if (padre.isEliminado()) {
                throw new BadRequestException("No se puede citar un mensaje eliminado");
            }
            mensaje.setMensajePadre(padre);
            mensaje.setMensajePadreAutor(padre.getPersonaje().getNombre());
            mensaje.setMensajePadreContenido(padre.getContenido());
        }

        mensaje = mensajeRepository.save(mensaje);

        MensajeCanalDTO dto = MensajeCanalDTO.fromEntity(mensaje, personajeId);

        messagingTemplate.convertAndSend("/topic/canal." + canalId, dto);

        return dto;
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

    @Transactional
    public MensajeCanalDTO editarMensaje(Integer mensajeId, Integer personajeId, String nuevoContenido) {
        MensajeCanal mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        if (!mensaje.getPersonaje().getIdPersonaje().equals(personajeId)) {
            throw new AccessDeniedException("Solo puedes editar tus propios mensajes");
        }

        permissionService.verificarPermiso(mensaje.getCanal().getIdCanal(), personajeId,
                PermisoCanal.EDITAR_MENSAJES_PROPIOS);

        mensaje.setContenido(HtmlUtils.sanitize(nuevoContenido));
        mensaje.setEditado(true);
        mensaje.setFechaEdicion(new Date());
        mensaje = mensajeRepository.save(mensaje);

        MensajeCanalDTO dto = MensajeCanalDTO.fromEntity(mensaje, personajeId);

        messagingTemplate.convertAndSend("/topic/canal." + mensaje.getCanal().getIdCanal(), dto);

        return dto;
    }

    @Transactional
    public void eliminarMensaje(Integer mensajeId, Integer personajeId) {
        MensajeCanal mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        boolean esPropio = mensaje.getPersonaje().getIdPersonaje().equals(personajeId);

        boolean puedeEliminarAjenos = permissionService.tienePermiso(
                mensaje.getCanal().getIdCanal(), personajeId, PermisoCanal.ELIMINAR_MENSAJES_AJENOS);

        if (esPropio) {
            permissionService.verificarPermiso(mensaje.getCanal().getIdCanal(), personajeId,
                    PermisoCanal.ELIMINAR_MENSAJES_PROPIOS);
        } else {
            permissionService.verificarPermiso(mensaje.getCanal().getIdCanal(), personajeId,
                    PermisoCanal.ELIMINAR_MENSAJES_AJENOS);
        }

        mensaje.setEliminado(true);
        mensaje.setEliminadoPorModerador(puedeEliminarAjenos);
        mensaje = mensajeRepository.save(mensaje);

        MensajeCanalDTO dto = MensajeCanalDTO.fromEntity(mensaje, personajeId);
        messagingTemplate.convertAndSend("/topic/canal." + mensaje.getCanal().getIdCanal(), dto);
    }
}
