package com.playrole.chat.service;

import com.playrole.chat.dto.InvitacionCanalDTO;
import com.playrole.chat.dto.MensajePrivadoPersonajeDTO;
import com.playrole.chat.enums.EstadoInvitacion;
import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.InvitacionCanal;
import com.playrole.chat.model.MensajePrivadoPersonaje;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.repository.BaneoCanalRepository;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.InvitacionCanalRepository;
import com.playrole.chat.repository.MensajePrivadoPersonajeRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Invitaciones a canales. La invitacion viaja como mensaje privado entre
 * personajes y se guarda aparte para poder aceptarla o rechazarla despues.
 */
@Service
public class InvitacionCanalService {

    private final InvitacionCanalRepository invitacionRepository;
    private final CanalRepository canalRepository;
    private final MiembroCanalRepository miembroRepository;
    private final BaneoCanalRepository baneoRepository;
    private final MensajePrivadoPersonajeRepository mensajePrivadoRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final CanalPermissionService permissionService;
    private final SimpMessagingTemplate messagingTemplate;

    public InvitacionCanalService(InvitacionCanalRepository invitacionRepository,
                                  CanalRepository canalRepository,
                                  MiembroCanalRepository miembroRepository,
                                  BaneoCanalRepository baneoRepository,
                                  MensajePrivadoPersonajeRepository mensajePrivadoRepository,
                                  PerfilPersonajeRepositoryInterface personajeRepository,
                                  CanalPermissionService permissionService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.invitacionRepository = invitacionRepository;
        this.canalRepository = canalRepository;
        this.miembroRepository = miembroRepository;
        this.baneoRepository = baneoRepository;
        this.mensajePrivadoRepository = mensajePrivadoRepository;
        this.personajeRepository = personajeRepository;
        this.permissionService = permissionService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public InvitacionCanalDTO invitar(Integer canalId, Integer invitadoId, Integer solicitanteId) {
        if (invitadoId == null) {
            throw new BadRequestException("Debes indicar el personaje al que quieres invitar");
        }
        if (solicitanteId.equals(invitadoId)) {
            throw new BadRequestException("No puedes invitarte a ti mismo");
        }

        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.GESTIONAR_MIEMBROS);

        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        if (canal.isSuspendidoActivo()) {
            throw new AccessDeniedException("Este canal está suspendido por moderación");
        }

        PerfilPersonaje invitado = personajeRepository.findById(invitadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        if (miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, invitadoId)) {
            throw new BadRequestException("El personaje ya es miembro del canal");
        }
        if (baneoRepository.existsActivoByCanalAndPersonaje(canalId, invitadoId)) {
            throw new AccessDeniedException("El personaje está baneado de este canal");
        }
        if (invitacionRepository.existsByCanalIdCanalAndInvitadoIdPersonajeAndEstado(
                canalId, invitadoId, EstadoInvitacion.PENDIENTE)) {
            throw new BadRequestException("Ya hay una invitación pendiente para este personaje");
        }

        PerfilPersonaje invitador = personajeRepository.findById(solicitanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        InvitacionCanal invitacion = new InvitacionCanal();
        invitacion.setCanal(canal);
        invitacion.setInvitado(invitado);
        invitacion.setInvitador(invitador);
        invitacion.setEstado(EstadoInvitacion.PENDIENTE);
        invitacion.setFechaCreacion(new Date());
        invitacion = invitacionRepository.save(invitacion);

        MensajePrivadoPersonaje mensaje = new MensajePrivadoPersonaje();
        mensaje.setEmisor(invitador);
        mensaje.setReceptor(invitado);
        mensaje.setContenido("Te ha invitado al canal \"" + canal.getNombre() + "\"");
        mensaje.setFechaEnvio(new Date());
        mensaje.setLeido(false);
        mensaje.setInvitacion(invitacion);
        mensaje = mensajePrivadoRepository.save(mensaje);

        // Mismo canal que los mensajes privados: el invitado ve la tarjeta al
        // instante y la conversacion queda en su lista de contactos.
        messagingTemplate.convertAndSend(
                "/topic/privado." + invitadoId,
                MensajePrivadoPersonajeDTO.fromEntity(mensaje, solicitanteId));

        return InvitacionCanalDTO.fromEntity(invitacion);
    }

    @Transactional
    public InvitacionCanalDTO aceptar(Integer invitacionId, Integer personajeId) {
        InvitacionCanal invitacion = obtenerPropia(invitacionId, personajeId);

        if (invitacion.getEstado() == EstadoInvitacion.ACEPTADA) {
            return InvitacionCanalDTO.fromEntity(invitacion);
        }
        if (invitacion.getEstado() == EstadoInvitacion.RECHAZADA) {
            throw new BadRequestException("La invitación ya fue rechazada");
        }

        Canal canal = invitacion.getCanal();
        if (canal.isSuspendidoActivo()) {
            throw new AccessDeniedException("Este canal está suspendido por moderación");
        }
        if (baneoRepository.existsActivoByCanalAndPersonaje(canal.getIdCanal(), personajeId)) {
            throw new AccessDeniedException("Estás baneado de este canal");
        }

        if (!miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canal.getIdCanal(), personajeId)) {
            MiembroCanal miembro = new MiembroCanal();
            miembro.setCanal(canal);
            miembro.setPersonaje(invitacion.getInvitado());
            miembro.setRol(RolCanal.MEMBER);
            miembro.setInvitado(true);
            miembro.setFechaUnion(new Date());
            miembroRepository.save(miembro);
        }

        invitacion.setEstado(EstadoInvitacion.ACEPTADA);
        invitacion.setFechaRespuesta(new Date());
        invitacion = invitacionRepository.save(invitacion);

        notificarRespuesta(invitacion, "INVITACION_ACEPTADA");
        return InvitacionCanalDTO.fromEntity(invitacion);
    }

    @Transactional
    public InvitacionCanalDTO rechazar(Integer invitacionId, Integer personajeId) {
        InvitacionCanal invitacion = obtenerPropia(invitacionId, personajeId);

        if (invitacion.getEstado() == EstadoInvitacion.RECHAZADA) {
            return InvitacionCanalDTO.fromEntity(invitacion);
        }
        if (invitacion.getEstado() == EstadoInvitacion.ACEPTADA) {
            throw new BadRequestException("Ya aceptaste esta invitación");
        }

        invitacion.setEstado(EstadoInvitacion.RECHAZADA);
        invitacion.setFechaRespuesta(new Date());
        invitacion = invitacionRepository.save(invitacion);

        notificarRespuesta(invitacion, "INVITACION_RECHAZADA");
        return InvitacionCanalDTO.fromEntity(invitacion);
    }

    public List<InvitacionCanalDTO> listarPendientes(Integer personajeId) {
        return invitacionRepository
                .findByInvitadoIdPersonajeAndEstadoOrderByFechaCreacionDesc(
                        personajeId, EstadoInvitacion.PENDIENTE)
                .stream()
                .map(InvitacionCanalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Borra las invitaciones de un canal y los mensajes privados que las
     * transportan, para no dejar referencias colgando al eliminar el canal.
     */
    @Transactional
    public void eliminarInvitacionesDeCanal(Integer canalId) {
        List<InvitacionCanal> invitaciones = invitacionRepository.findByCanalIdCanal(canalId);
        if (invitaciones.isEmpty()) return;

        List<Integer> ids = invitaciones.stream()
                .map(InvitacionCanal::getIdInvitacion)
                .collect(Collectors.toList());
        mensajePrivadoRepository.deleteByInvitacionIds(ids);
        invitacionRepository.deleteAll(invitaciones);
    }

    private InvitacionCanal obtenerPropia(Integer invitacionId, Integer personajeId) {
        InvitacionCanal invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada"));

        if (!invitacion.getInvitado().getIdPersonaje().equals(personajeId)) {
            throw new AccessDeniedException("Esta invitación no es tuya");
        }
        return invitacion;
    }

    private void notificarRespuesta(InvitacionCanal invitacion, String tipo) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", tipo);
        evento.put("invitacionId", invitacion.getIdInvitacion());
        evento.put("canalId", invitacion.getCanal().getIdCanal());
        evento.put("canalNombre", invitacion.getCanal().getNombre());
        evento.put("personajeId", invitacion.getInvitado().getIdPersonaje());
        evento.put("personajeNombre", invitacion.getInvitado().getNombre());

        messagingTemplate.convertAndSend(
                "/topic/privado." + invitacion.getInvitador().getIdPersonaje(),
                (Object) evento);
    }
}
