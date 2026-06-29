package com.playrole.chat.service;

import com.playrole.chat.dto.CanalDTO;
import com.playrole.chat.dto.CrearCanalDTO;
import com.playrole.chat.dto.MiembroCanalDTO;
import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.enums.TipoCanal;
import com.playrole.chat.model.BaneoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.repository.BaneoCanalRepository;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CanalService {

    private static final int MAX_CANALES_POR_USUARIO = 10;

    private final CanalRepository canalRepository;
    private final MiembroCanalRepository miembroRepository;
    private final BaneoCanalRepository baneoRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final CanalPermissionService permissionService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public CanalService(CanalRepository canalRepository,
                        MiembroCanalRepository miembroRepository,
                        BaneoCanalRepository baneoRepository,
                        PerfilPersonajeRepositoryInterface personajeRepository,
                        CanalPermissionService permissionService,
                        PresenceService presenceService,
                        SimpMessagingTemplate messagingTemplate) {
        this.canalRepository = canalRepository;
        this.miembroRepository = miembroRepository;
        this.baneoRepository = baneoRepository;
        this.personajeRepository = personajeRepository;
        this.permissionService = permissionService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<CanalDTO> listarCanalesDisponibles(Integer personajeId) {
        List<Canal> canales = canalRepository.findByVisibleTrueOrderByFechaCreacionDesc();
        return canales.stream()
                .map(c -> {
                    CanalDTO dto = CanalDTO.fromEntity(c);
                    dto.setMiembroCount((int) miembroRepository.countByCanalId(c.getIdCanal()));
                    permissionService.obtenerRol(c.getIdCanal(), personajeId)
                            .ifPresent(rol -> dto.setMiRol(rol.name()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<CanalDTO> listarCanalesUnidos(Integer personajeId) {
        List<Canal> canales = canalRepository.findCanalesDePersonaje(personajeId);
        return canales.stream()
                .map(c -> {
                    CanalDTO dto = CanalDTO.fromEntity(c);
                    dto.setMiembroCount((int) miembroRepository.countByCanalId(c.getIdCanal()));
                    permissionService.obtenerRol(c.getIdCanal(), personajeId)
                            .ifPresent(rol -> dto.setMiRol(rol.name()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<CanalDTO> listarCanalesPublicosNoUnidos(Integer personajeId) {
        return canalRepository.findCanalesPublicosNoUnidos(personajeId).stream()
                .map(c -> {
                    CanalDTO dto = CanalDTO.fromEntity(c);
                    dto.setMiembroCount((int) miembroRepository.countByCanalId(c.getIdCanal()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CanalDTO obtenerCanal(Integer canalId, Integer personajeId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        CanalDTO dto = CanalDTO.fromEntity(canal);
        dto.setMiembroCount((int) miembroRepository.countByCanalId(canalId));
        permissionService.obtenerRol(canalId, personajeId).ifPresent(rol -> dto.setMiRol(rol.name()));
        return dto;
    }

    @Transactional
    public CanalDTO crearCanal(CrearCanalDTO dto, Integer personajeId, boolean esAdmin) {
        if (!esAdmin) {
            long count = canalRepository.countByCreadorIdPersonaje(personajeId);
            if (count >= MAX_CANALES_POR_USUARIO) {
                throw new BadRequestException("Has alcanzado el límite de " + MAX_CANALES_POR_USUARIO + " canales creados");
            }
        }

        PerfilPersonaje creador = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        Canal canal = new Canal();
        canal.setNombre(dto.getNombre());
        canal.setDescripcion(dto.getDescripcion());
        canal.setPrivado(dto.isPrivado());
        canal.setTipo(TipoCanal.USER_CREATED);
        canal.setCreador(creador);
        canal.setFechaCreacion(new Date());

        canal = canalRepository.save(canal);

        MiembroCanal miembro = new MiembroCanal();
        miembro.setCanal(canal);
        miembro.setPersonaje(creador);
        miembro.setRol(RolCanal.OWNER);
        miembro.setFechaUnion(new Date());
        miembroRepository.save(miembro);

        CanalDTO result = CanalDTO.fromEntity(canal);
        result.setMiRol(RolCanal.OWNER.name());
        result.setMiembroCount(1);
        return result;
    }

    @Transactional
    public void unirseACanal(Integer canalId, Integer personajeId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        if (canal.isPrivado() && canal.getTipo() == TipoCanal.USER_CREATED) {
            throw new AccessDeniedException("Este canal es privado. Necesitas una invitación.");
        }

        if (miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId)) {
            throw new BadRequestException("Ya eres miembro de este canal");
        }

        if (baneoRepository.existsActivoByCanalAndPersonaje(canalId, personajeId)) {
            throw new AccessDeniedException("Estás baneado de este canal");
        }

        PerfilPersonaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        MiembroCanal miembro = new MiembroCanal();
        miembro.setCanal(canal);
        miembro.setPersonaje(personaje);
        miembro.setRol(RolCanal.MEMBER);
        miembro.setFechaUnion(new Date());
        miembroRepository.save(miembro);
    }

    @Transactional
    public void salirDeCanal(Integer canalId, Integer personajeId) {
        MiembroCanal miembro = miembroRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("No eres miembro de este canal"));

        if (miembro.getRol() == RolCanal.OWNER) {
            long otherMembers = miembroRepository.countByCanalId(canalId);
            if (otherMembers > 1) {
                throw new BadRequestException("Debes transferir la propiedad antes de salir");
            }
            canalRepository.deleteById(canalId);
        } else {
            miembroRepository.delete(miembro);
        }
    }

    @Transactional
    public void invitarMiembro(Integer canalId, Integer invitadoId, Integer solicitanteId) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.GESTIONAR_MIEMBROS);

        PerfilPersonaje invitado = personajeRepository.findById(invitadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        if (miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, invitadoId)) {
            throw new BadRequestException("El personaje ya es miembro del canal");
        }

        MiembroCanal miembro = new MiembroCanal();
        miembro.setCanal(canalRepository.findById(canalId).orElseThrow());
        miembro.setPersonaje(invitado);
        miembro.setRol(RolCanal.MEMBER);
        miembro.setInvitado(true);
        miembro.setFechaUnion(new Date());
        miembroRepository.save(miembro);
    }

    @Transactional
    public void expulsarMiembro(Integer canalId, Integer objetivoId, Integer solicitanteId) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.GESTIONAR_MIEMBROS);

        MiembroCanal objetivo = miembroRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)
                .orElseThrow(() -> new ResourceNotFoundException("El personaje no es miembro del canal"));

        if (objetivo.getRol() == RolCanal.OWNER) {
            throw new AccessDeniedException("No puedes expulsar al propietario del canal");
        }

        miembroRepository.delete(objetivo);

        messagingTemplate.convertAndSend(
                "/topic/privado." + objetivoId,
                (Object) Map.of("tipo", "EXPULSADO", "canalId", canalId));
    }

    @Transactional
    public void banearMiembro(Integer canalId, Integer objetivoId, Integer solicitanteId, String duracion) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.GESTIONAR_MIEMBROS);

        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        PerfilPersonaje objetivo = personajeRepository.findById(objetivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        PerfilPersonaje baneador = personajeRepository.findById(solicitanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante no encontrado"));

        if (objetivoId.equals(solicitanteId)) {
            throw new BadRequestException("No puedes banearte a ti mismo");
        }

        miembroRepository.findByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)
                .ifPresent(m -> {
                    if (m.getRol() == RolCanal.OWNER) {
                        throw new AccessDeniedException("No puedes banear al propietario del canal");
                    }
                    miembroRepository.delete(m);
                });

        if (baneoRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)) {
            throw new BadRequestException("El personaje ya está baneado");
        }

        BaneoCanal baneo = new BaneoCanal();
        baneo.setCanal(canal);
        baneo.setPersonaje(objetivo);
        baneo.setBaneadoPor(baneador);
        baneo.setFechaBaneo(new Date());

        if (duracion != null && !duracion.equalsIgnoreCase("PERMANENTE")) {
            long millis = switch (duracion.toUpperCase()) {
                case "1H" -> 3600000L;
                case "24H" -> 86400000L;
                case "7D" -> 604800000L;
                default -> throw new BadRequestException("Duración no válida: " + duracion);
            };
            baneo.setFechaExpiracion(new Date(System.currentTimeMillis() + millis));
        }

        baneoRepository.save(baneo);

        messagingTemplate.convertAndSend(
                "/topic/privado." + objetivoId,
                (Object) Map.of("tipo", "BANEADO", "canalId", canalId));
    }

    @Transactional
    public void cambiarRol(Integer canalId, Integer objetivoId, RolCanal nuevoRol, Integer solicitanteId) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.GESTIONAR_ROLES);

        MiembroCanal objetivo = miembroRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)
                .orElseThrow(() -> new ResourceNotFoundException("El personaje no es miembro del canal"));

        if (objetivo.getRol() == RolCanal.OWNER) {
            throw new AccessDeniedException("No puedes cambiar el rol del propietario");
        }

        if (nuevoRol == RolCanal.OWNER) {
            throw new BadRequestException("Usa la transferencia de propiedad para asignar OWNER");
        }

        miembroRepository.updateRol(canalId, objetivoId, nuevoRol);
    }

    public Page<MiembroCanalDTO> listarMiembros(Integer canalId, Integer personajeId, int page, int size) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.LEER_MENSAJES);

        return miembroRepository.findByCanalIdCanal(canalId, PageRequest.of(page, size))
                .map(m -> {
                    MiembroCanalDTO dto = MiembroCanalDTO.fromEntity(m);
                    Integer pid = m.getPersonaje().getIdPersonaje();
                    dto.setOnline(presenceService.isOnlineStrict(pid));
                    dto.setStatus(presenceService.getStatus(pid));
                    return dto;
                });
    }

    public List<MiembroCanalDTO> listarMiembrosOnline(Integer canalId, Integer personajeId) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.LEER_MENSAJES);

        return miembroRepository.findByCanalIdCanal(canalId).stream()
                .filter(m -> presenceService.isOnline(m.getPersonaje().getIdPersonaje()))
                .map(m -> {
                    MiembroCanalDTO dto = MiembroCanalDTO.fromEntity(m);
                    dto.setOnline(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarCanal(Integer canalId, Integer personajeId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        if (canal.getTipo() == TipoCanal.OFFICIAL) {
            throw new AccessDeniedException("No puedes eliminar un canal oficial");
        }

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.ELIMINAR_CANAL);
        canal.setVisible(false);
        canalRepository.save(canal);
    }
}
