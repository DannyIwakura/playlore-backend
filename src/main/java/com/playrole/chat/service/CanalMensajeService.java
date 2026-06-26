package com.playrole.chat.service;

import com.playrole.chat.dto.MensajeCanalDTO;
import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MensajeCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.utils.HtmlUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class CanalMensajeService {

    private final MensajeCanalRepository mensajeRepository;
    private final CanalRepository canalRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final CanalPermissionService permissionService;
    private final SimpMessagingTemplate messagingTemplate;

    public CanalMensajeService(MensajeCanalRepository mensajeRepository,
                                CanalRepository canalRepository,
                                PerfilPersonajeRepositoryInterface personajeRepository,
                                CanalPermissionService permissionService,
                                SimpMessagingTemplate messagingTemplate) {
        this.mensajeRepository = mensajeRepository;
        this.canalRepository = canalRepository;
        this.personajeRepository = personajeRepository;
        this.permissionService = permissionService;
        this.messagingTemplate = messagingTemplate;
    }

    public Page<MensajeCanalDTO> obtenerMensajes(Integer canalId, Integer personajeId, int page, int size) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.LEER_MENSAJES);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaEnvio"));
        return mensajeRepository.findMensajesByCanal(canalId, pageRequest)
                .map(m -> MensajeCanalDTO.fromEntity(m, personajeId));
    }

    @Transactional
    public MensajeCanalDTO enviarMensaje(Integer canalId, Integer personajeId, String contenido) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.ENVIAR_MENSAJES);

        PerfilPersonaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        String sanitizado = HtmlUtils.sanitize(contenido);

        MensajeCanal mensaje = new MensajeCanal();
        mensaje.setCanal(canal);
        mensaje.setPersonaje(personaje);
        mensaje.setContenido(sanitizado);
        mensaje.setFechaEnvio(new Date());

        mensaje = mensajeRepository.save(mensaje);

        MensajeCanalDTO dto = MensajeCanalDTO.fromEntity(mensaje, personajeId);

        messagingTemplate.convertAndSend("/topic/canal." + canalId, dto);

        return dto;
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
        mensajeRepository.save(mensaje);
    }
}
