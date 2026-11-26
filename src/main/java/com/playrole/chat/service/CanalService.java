package com.playrole.chat.service;

import com.playrole.chat.dto.BaneadoCanalDTO;
import com.playrole.chat.dto.CanalDTO;
import com.playrole.chat.dto.CrearCanalDTO;
import com.playrole.chat.dto.EditarCanalDTO;
import com.playrole.chat.dto.MiembroCanalDTO;
import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.enums.TipoCanal;
import com.playrole.chat.model.BaneoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.repository.BaneoCanalRepository;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.enums.AccionModeracion;
import com.playrole.enums.RolUsuario;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.service.AuditoriaModeracionService;
import com.playrole.service.image.ImageProcessingService;
import com.playrole.service.image.ImageVariant;
import com.playrole.service.image.ProcessedImage;
import com.playrole.utils.ModeracionUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CanalService {

    private static final int MAX_CANALES_POR_USUARIO = 10;

    private final CanalRepository canalRepository;
    private final MiembroCanalRepository miembroRepository;
    private final BaneoCanalRepository baneoRepository;
    private final MensajeCanalRepository mensajeRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final CanalPermissionService permissionService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaModeracionService auditoriaService;
    private final InvitacionCanalService invitacionService;
    private final ImageProcessingService imageProcessingService;

    public CanalService(CanalRepository canalRepository,
                        MiembroCanalRepository miembroRepository,
                        BaneoCanalRepository baneoRepository,
                        MensajeCanalRepository mensajeRepository,
                        PerfilPersonajeRepositoryInterface personajeRepository,
                        CanalPermissionService permissionService,
                        PresenceService presenceService,
                        SimpMessagingTemplate messagingTemplate,
                        PasswordEncoder passwordEncoder,
                        AuditoriaModeracionService auditoriaService,
                        InvitacionCanalService invitacionService,
                        ImageProcessingService imageProcessingService) {
        this.canalRepository = canalRepository;
        this.miembroRepository = miembroRepository;
        this.baneoRepository = baneoRepository;
        this.mensajeRepository = mensajeRepository;
        this.personajeRepository = personajeRepository;
        this.permissionService = permissionService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
        this.invitacionService = invitacionService;
        this.imageProcessingService = imageProcessingService;
    }

    public List<CanalDTO> listarCanalesDisponibles(Integer personajeId, boolean esStaff) {
        List<Canal> canales = esStaff
                ? canalRepository.findVisibles(new Date())
                : canalRepository.findVisiblesPublicos(new Date());
        return construirDTOs(canales, personajeId, true);
    }

    public List<CanalDTO> listarCanalesUnidos(Integer personajeId) {
        List<Canal> canales = canalRepository.findCanalesDePersonaje(personajeId, new Date());
        return construirDTOs(canales, personajeId, true);
    }

    public List<CanalDTO> listarCanalesPublicosNoUnidos(Integer personajeId, boolean esStaff) {
        List<Canal> canales = esStaff
                ? canalRepository.findCanalesNoUnidos(personajeId, new Date())
                : canalRepository.findCanalesPublicosNoUnidos(personajeId, new Date());
        return construirDTOs(canales, personajeId, false);
    }

    /**
     * Canales privados en los que el personaje puede gestionar miembros
     * (dueno, admin o mod del canal). Es la lista que se ofrece al invitar.
     */
    public List<CanalDTO> listarCanalesPrivadosGestionables(Integer personajeId) {
        List<Canal> canales = canalRepository.findCanalesPrivadosGestionables(
                personajeId,
                List.of(RolCanal.OWNER, RolCanal.ADMIN, RolCanal.MOD),
                new Date());
        return construirDTOs(canales, personajeId, true);
    }

    private List<CanalDTO> construirDTOs(List<Canal> canales, Integer personajeId, boolean incluirMiRol) {
        if (canales.isEmpty()) return List.of();

        List<Integer> canalIds = canales.stream().map(Canal::getIdCanal).collect(Collectors.toList());
        Map<Integer, Long> conteos = miembroRepository.countByCanalIn(canalIds).stream()
                .collect(Collectors.toMap(MiembroCanalRepository.CountByCanal::getCanalId,
                        MiembroCanalRepository.CountByCanal::getTotal));
        Map<Integer, RolCanal> roles = incluirMiRol
                ? miembroRepository.findRolesPorCanales(canalIds, personajeId).stream()
                        .collect(Collectors.toMap(MiembroCanalRepository.RolPorCanal::getCanalId,
                                MiembroCanalRepository.RolPorCanal::getRol))
                : Map.of();

        return canales.stream()
                .map(c -> {
                    CanalDTO dto = CanalDTO.fromEntity(c);
                    dto.setMiembroCount(conteos.getOrDefault(c.getIdCanal(), 0L).intValue());
                    if (incluirMiRol) {
                        RolCanal rol = roles.get(c.getIdCanal());
                        if (rol != null) dto.setMiRol(rol.name());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CanalDTO obtenerCanal(Integer canalId, Integer personajeId, boolean esStaff) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        if (canal.isSuspendidoActivo()) {
            throw new AccessDeniedException("Este canal está suspendido por moderación");
        }

        if (canal.isPrivado() && !esStaff
                && !miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId)) {
            throw new AccessDeniedException("Este canal es privado. Necesitas una invitación.");
        }

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
    public CanalDTO editarCanal(Integer canalId, EditarCanalDTO dto, Integer personajeId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.EDITAR_CANAL);

        if (canal.getTipo() == TipoCanal.OFFICIAL && dto.isPrivado()) {
            throw new BadRequestException("Un canal oficial debe ser público. Quita primero la marca de oficial.");
        }

        canal.setNombre(dto.getNombre());
        canal.setDescripcion(dto.getDescripcion());
        canal.setPrivado(canal.getTipo() == TipoCanal.OFFICIAL ? false : dto.isPrivado());
        canal = canalRepository.save(canal);

        CanalDTO result = CanalDTO.fromEntity(canal);
        result.setMiembroCount((int) miembroRepository.countByCanalId(canalId));
        permissionService.obtenerRol(canalId, personajeId).ifPresent(rol -> result.setMiRol(rol.name()));
        return result;
    }

    @Transactional
    public void unirseACanal(Integer canalId, Integer personajeId, boolean esStaff, Usuario usuarioSolicitante) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        if (canal.isSuspendidoActivo()) {
            throw new AccessDeniedException("Este canal está suspendido por moderación");
        }

        if (canal.isPrivado() && !esStaff) {
            throw new AccessDeniedException("Este canal es privado. Necesitas una invitación.");
        }

        if (miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId)) {
            throw new BadRequestException("Ya eres miembro de este canal");
        }

        // El staff no queda bloqueado por baneos antiguos del canal.
        if (!esStaff && baneoRepository.existsActivoByCanalAndPersonaje(canalId, personajeId)) {
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

        // Solo se audita el acceso del staff a un privado sin invitacion.
        if (esStaff && canal.isPrivado()) {
            auditoriaService.registrar(AccionModeracion.CANAL_ACCESO_MODERADOR, usuarioSolicitante, canal,
                    "Acceso sin invitación",
                    "El personaje " + personaje.getNombre() + " (#" + personajeId + ") entró como miembro");
        }
    }

    @Transactional
    public void salirDeCanal(Integer canalId, Integer personajeId) {
        MiembroCanal miembro = miembroRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("No eres miembro de este canal"));

        if (miembro.getRol() == RolCanal.OWNER) {
            throw new BadRequestException("El dueño del canal no puede salir. Usa el panel de administración para transferir o eliminar el canal");
        }

        miembroRepository.delete(miembro);
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

        if (esStaff(objetivo)) {
            throw new AccessDeniedException("No puedes banear a un personaje de una cuenta de moderación");
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
            baneo.setFechaExpiracion(ModeracionUtils.calcularExpiracion(duracion));
        }

        baneoRepository.save(baneo);

        messagingTemplate.convertAndSend(
                "/topic/privado." + objetivoId,
                (Object) Map.of("tipo", "BANEADO", "canalId", canalId));
    }

    @Transactional
    public void desbanearMiembro(Integer canalId, Integer objetivoId, Integer solicitanteId) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.GESTIONAR_MIEMBROS);

        BaneoCanal baneo = baneoRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)
                .orElseThrow(() -> new ResourceNotFoundException("El personaje no está baneado de este canal"));

        baneoRepository.delete(baneo);

        messagingTemplate.convertAndSend(
                "/topic/privado." + objetivoId,
                (Object) Map.of("tipo", "DESBANEADO", "canalId", canalId));
    }

    public List<BaneadoCanalDTO> listarBaneados(Integer canalId, Integer personajeId) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.GESTIONAR_MIEMBROS);

        return baneoRepository.findByCanalIdCanalOrderByFechaBaneoDesc(canalId).stream()
                .map(BaneadoCanalDTO::fromEntity)
                .collect(Collectors.toList());
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

    @Transactional
    public void silenciarMiembro(Integer canalId, Integer objetivoId, Integer solicitanteId, String duracion) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.SILENCIAR);

        if (objetivoId.equals(solicitanteId)) {
            throw new BadRequestException("No puedes silenciarte a ti mismo");
        }

        MiembroCanal objetivo = miembroRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)
                .orElseThrow(() -> new ResourceNotFoundException("El personaje no es miembro del canal"));

        if (objetivo.getRol() != RolCanal.MEMBER) {
            throw new AccessDeniedException("Solo puedes silenciar a miembros del canal");
        }

        if (esStaff(objetivo.getPersonaje())) {
            throw new AccessDeniedException("No puedes silenciar a un personaje de una cuenta de moderación");
        }

        Date hasta = ModeracionUtils.calcularExpiracion(duracion);
        if (hasta == null) {
            throw new BadRequestException("Debes indicar una duración para el silenciamiento");
        }

        objetivo.setSilenciadoHasta(hasta);
        miembroRepository.save(objetivo);

        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        messagingTemplate.convertAndSend(
                "/topic/privado." + objetivoId,
                (Object) Map.of("tipo", "SILENCIADO", "canalId", canalId,
                        "canalNombre", canal.getNombre(), "silenciadoHasta", hasta.getTime()));
    }

    @Transactional
    public void desilenciarMiembro(Integer canalId, Integer objetivoId, Integer solicitanteId) {
        permissionService.verificarPermiso(canalId, solicitanteId, PermisoCanal.SILENCIAR);

        MiembroCanal objetivo = miembroRepository
                .findByCanalIdCanalAndPersonajeIdPersonaje(canalId, objetivoId)
                .orElseThrow(() -> new ResourceNotFoundException("El personaje no es miembro del canal"));

        objetivo.setSilenciadoHasta(null);
        miembroRepository.save(objetivo);

        messagingTemplate.convertAndSend(
                "/topic/privado." + objetivoId,
                (Object) Map.of("tipo", "DESILENCIADO", "canalId", canalId));
    }

    public Page<MiembroCanalDTO> listarMiembros(Integer canalId, Integer personajeId, int page, int size) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.LEER_MENSAJES);

        Page<MiembroCanal> pagina = miembroRepository.findByCanalIdCanal(canalId, PageRequest.of(page, size));
        Map<Integer, Boolean> staff = mapaStaff(pagina.getContent());

        return pagina.map(m -> {
            MiembroCanalDTO dto = MiembroCanalDTO.fromEntity(m);
            Integer pid = m.getPersonaje().getIdPersonaje();
            dto.setOnline(presenceService.isOnline(pid));
            dto.setStatus(presenceService.getStatus(pid));
            dto.setEsStaff(staff.getOrDefault(pid, false));
            return dto;
        });
    }

    public List<MiembroCanalDTO> listarMiembrosOnline(Integer canalId, Integer personajeId) {
        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.LEER_MENSAJES);

        List<MiembroCanal> miembros = miembroRepository.findByCanalIdCanal(canalId);
        Map<Integer, Boolean> staff = mapaStaff(miembros);

        return miembros.stream()
                .filter(m -> presenceService.isOnline(m.getPersonaje().getIdPersonaje()))
                .map(m -> {
                    MiembroCanalDTO dto = MiembroCanalDTO.fromEntity(m);
                    dto.setOnline(true);
                    dto.setEsStaff(staff.getOrDefault(m.getPersonaje().getIdPersonaje(), false));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Consulta batch del rol de cuenta para marcar al staff en los listados de
     * miembros. Evita un SELECT por cada personaje.
     */
    private Map<Integer, Boolean> mapaStaff(List<MiembroCanal> miembros) {
        if (miembros.isEmpty()) return Map.of();

        List<Integer> personajeIds = miembros.stream()
                .map(m -> m.getPersonaje().getIdPersonaje())
                .collect(Collectors.toList());

        return personajeRepository.findRolesUsuarioPorPersonajes(personajeIds).stream()
                .collect(Collectors.toMap(
                        PerfilPersonajeRepositoryInterface.RolUsuarioPorPersonaje::getPersonajeId,
                        r -> r.getRol() == RolUsuario.ADMIN || r.getRol() == RolUsuario.MOD));
    }

    private boolean esStaff(PerfilPersonaje personaje) {
        RolUsuario rol = personaje.getUserId() != null ? personaje.getUserId().getRol() : null;
        return rol == RolUsuario.ADMIN || rol == RolUsuario.MOD;
    }

    @Transactional
    public void eliminarCanal(Integer canalId, Integer personajeId, String password) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        if (canal.getTipo() == TipoCanal.OFFICIAL) {
            throw new AccessDeniedException("No puedes eliminar un canal oficial");
        }

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.ELIMINAR_CANAL);

        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Debes introducir tu contraseña para eliminar el canal");
        }

        PerfilPersonaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        String passwordCifrada = personaje.getUserId().getPassword();
        if (!passwordEncoder.matches(password, passwordCifrada)) {
            throw new BadRequestException("La contraseña no es correcta");
        }

        List<Integer> miembroIds = borrarCanalFisico(canal);

        for (Integer miembroId : miembroIds) {
            messagingTemplate.convertAndSend(
                    "/topic/privado." + miembroId,
                    (Object) Map.of("tipo", "CANAL_ELIMINADO", "canalId", canalId));
        }
    }

    /**
     * Borrado fisico compartido por el dueño y por moderacion. Quien llame
     * debe haber validado antes los permisos y la contraseña (si aplica).
     * Devuelve los ids de los personajes que eran miembros para notificarles.
     */
    public List<Integer> borrarCanalFisico(Canal canal) {
        Integer canalId = canal.getIdCanal();
        List<MiembroCanal> miembros = miembroRepository.findByCanalIdCanal(canalId);
        List<Integer> miembroIds = miembros.stream()
                .map(m -> m.getPersonaje().getIdPersonaje())
                .collect(Collectors.toList());

        if (canal.getImagenUrl() != null) {
            eliminarArchivoImagen(canal.getImagenUrl());
        }

        // Las invitaciones y sus mensajes privados apuntan al canal: se borran
        // antes para no dejar claves foraneas colgando.
        invitacionService.eliminarInvitacionesDeCanal(canalId);

        baneoRepository.deleteByCanalId(canalId);
        mensajeRepository.deleteByCanalId(canalId);
        miembroRepository.deleteAll(miembros);
        canalRepository.delete(canal);

        return miembroIds;
    }

    @Transactional
    public CanalDTO actualizarImagen(Integer canalId, MultipartFile imagenFile, Integer personajeId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.GESTIONAR_MIEMBROS);

        if (imagenFile == null || imagenFile.isEmpty()) {
            throw new BadRequestException("No se ha proporcionado una imagen");
        }

        canal = guardarImagenCanal(canal, imagenFile);

        CanalDTO dto = CanalDTO.fromEntity(canal);
        dto.setMiembroCount((int) miembroRepository.countByCanalId(canalId));
        permissionService.obtenerRol(canalId, personajeId).ifPresent(rol -> dto.setMiRol(rol.name()));
        return dto;
    }

    /**
     * Valida y guarda la imagen de un canal. No comprueba permisos: quien llame
     * debe haberlos validado antes (dueño o moderación).
     */
    @Transactional
    public Canal guardarImagenCanal(Canal canal, MultipartFile imagenFile) {
        if (imagenFile == null || imagenFile.isEmpty()) {
            throw new BadRequestException("No se ha proporcionado una imagen");
        }

        ProcessedImage processed = imageProcessingService.process(imagenFile, ImageVariant.CHANNEL_IMAGE, "imagenFile");

        String anterior = canal.getImagenUrl();
        try {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "canales");
            Files.createDirectories(uploadPath);

            String filename = UUID.randomUUID() + "." + processed.extension();
            Path destination = uploadPath.resolve(filename);
            Files.write(destination, processed.bytes());

            canal.setImagenUrl("/uploads/canales/" + filename);
            Canal guardado = canalRepository.save(canal);
            if (anterior != null) {
                eliminarArchivoImagen(anterior);
            }
            return guardado;
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen del canal", e);
        }
    }

    @Transactional
    public CanalDTO eliminarImagen(Integer canalId, Integer personajeId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));

        permissionService.verificarPermiso(canalId, personajeId, PermisoCanal.GESTIONAR_MIEMBROS);

        if (canal.getImagenUrl() != null) {
            eliminarArchivoImagen(canal.getImagenUrl());
            canal.setImagenUrl(null);
            canal = canalRepository.save(canal);
        }

        CanalDTO dto = CanalDTO.fromEntity(canal);
        dto.setMiembroCount((int) miembroRepository.countByCanalId(canalId));
        permissionService.obtenerRol(canalId, personajeId).ifPresent(rol -> dto.setMiRol(rol.name()));
        return dto;
    }

    private void eliminarArchivoImagen(String imageUrl) {
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", "canales", filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(CanalService.class)
                    .warn("No se pudo eliminar archivo de imagen: {}", e.getMessage());
        }
    }


}
