package com.playrole.service;

import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.enums.TipoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.chat.service.CanalService;
import com.playrole.dto.CanalModeracionDTO;
import com.playrole.dto.CrearCanalOficialDTO;
import com.playrole.dto.EliminarCanalModeracionDTO;
import com.playrole.dto.RestaurarCanalDTO;
import com.playrole.dto.SuspenderCanalDTO;
import com.playrole.enums.AccionModeracion;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.utils.HtmlUtils;
import com.playrole.utils.ModeracionUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Moderacion global de canales. Usa el rol de cuenta (ADMIN/MOD), no el rol
 * dentro del canal: un moderador puede actuar sobre canales de los que no es
 * miembro. Suspender oculta y bloquea el canal conservando sus datos; eliminar
 * borra fisicamente igual que el flujo del dueño.
 */
@Service
public class CanalModeracionService {

    private static final Logger log = LoggerFactory.getLogger(CanalModeracionService.class);
    private static final int MAX_MOTIVO = 255;

    private final CanalRepository canalRepository;
    private final MiembroCanalRepository miembroRepository;
    private final CanalService canalService;
    private final AuditoriaModeracionService auditoriaService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PerfilPersonajeRepositoryInterface personajeRepository;

    public CanalModeracionService(CanalRepository canalRepository,
                                  MiembroCanalRepository miembroRepository,
                                  CanalService canalService,
                                  AuditoriaModeracionService auditoriaService,
                                  SimpMessagingTemplate messagingTemplate,
                                  PerfilPersonajeRepositoryInterface personajeRepository) {
        this.canalRepository = canalRepository;
        this.miembroRepository = miembroRepository;
        this.canalService = canalService;
        this.auditoriaService = auditoriaService;
        this.messagingTemplate = messagingTemplate;
        this.personajeRepository = personajeRepository;
    }

    public List<CanalModeracionDTO> listar() {
        List<Canal> canales = canalRepository.findAllByOrderByFechaCreacionDesc();
        if (canales.isEmpty()) return List.of();

        List<Integer> ids = canales.stream().map(Canal::getIdCanal).collect(Collectors.toList());
        Map<Integer, Long> conteos = miembroRepository.countByCanalIn(ids).stream()
                .collect(Collectors.toMap(MiembroCanalRepository.CountByCanal::getCanalId,
                        MiembroCanalRepository.CountByCanal::getTotal));

        return canales.stream()
                .map(c -> CanalModeracionDTO.fromEntity(c, conteos.getOrDefault(c.getIdCanal(), 0L)))
                .collect(Collectors.toList());
    }

    public CanalModeracionDTO obtener(Integer canalId) {
        Canal canal = buscarCanal(canalId);
        return CanalModeracionDTO.fromEntity(canal, miembroRepository.countByCanalId(canalId));
    }

    @Transactional
    public CanalModeracionDTO suspender(Integer canalId, SuspenderCanalDTO dto, Usuario moderador) {
        Canal canal = buscarCanal(canalId);

        if (canal.isSuspendidoActivo()) {
            throw new BadRequestException("El canal ya está suspendido");
        }

        String motivo = validarMotivo(dto != null ? dto.getMotivo() : null);
        String duracion = dto != null ? dto.getDuracion() : null;
        Date hasta = ModeracionUtils.calcularExpiracionSuspension(duracion);

        canal.setSuspendido(true);
        canal.setSuspendidoHasta(hasta);
        canal.setFechaSuspension(new Date());
        canal.setSuspendidoPorId(moderador.getUserId());
        canal.setSuspendidoPorNombre(moderador.getNombre());
        canal.setMotivoSuspension(motivo);
        canal = canalRepository.save(canal);

        notificarMiembros(canal, "CANAL_SUSPENDIDO", hasta, motivo);
        auditoriaService.registrar(AccionModeracion.CANAL_SUSPENDIDO, moderador, canal, motivo,
                "Duración: " + (hasta != null ? "hasta " + hasta : "indefinida"));

        return CanalModeracionDTO.fromEntity(canal, miembroRepository.countByCanalId(canalId));
    }

    @Transactional
    public CanalModeracionDTO restaurar(Integer canalId, RestaurarCanalDTO dto, Usuario moderador) {
        Canal canal = buscarCanal(canalId);

        if (!canal.isSuspendido()) {
            throw new BadRequestException("El canal no está suspendido");
        }

        limpiarSuspension(canal);
        canal = canalRepository.save(canal);

        notificarMiembros(canal, "CANAL_RESTAURADO", null, null);
        String motivo = dto != null ? HtmlUtils.sanitize(dto.getMotivo()) : null;
        auditoriaService.registrar(AccionModeracion.CANAL_RESTAURADO, moderador, canal, motivo,
                "Suspensión levantada manualmente");

        return CanalModeracionDTO.fromEntity(canal, miembroRepository.countByCanalId(canalId));
    }

    @Transactional
    public void eliminar(Integer canalId, EliminarCanalModeracionDTO dto, Usuario admin) {
        Canal canal = buscarCanal(canalId);

        if (canal.getTipo() == TipoCanal.OFFICIAL) {
            throw new BadRequestException("No se puede eliminar un canal oficial. Quita primero la marca de oficial.");
        }

        String motivo = validarMotivo(dto != null ? dto.getMotivo() : null);

        Integer canalIdFinal = canal.getIdCanal();
        List<Integer> miembroIds = canalService.borrarCanalFisico(canal);

        for (Integer miembroId : miembroIds) {
            messagingTemplate.convertAndSend(
                    "/topic/privado." + miembroId,
                    (Object) Map.of("tipo", "CANAL_ELIMINADO", "canalId", canalIdFinal));
        }

        auditoriaService.registrar(AccionModeracion.CANAL_ELIMINADO, admin, canal, motivo,
                "Canales eliminados definitivamente. Miembros notificados: " + miembroIds.size());
    }

    /**
     * Crea un canal oficial con un personaje responsable que queda como OWNER.
     * El canal nace publico y la imagen es opcional.
     */
    @Transactional
    public CanalModeracionDTO crearOficial(CrearCanalOficialDTO dto, MultipartFile imagenFile, Usuario admin) {
        String nombre = HtmlUtils.sanitize(dto.getNombre() != null ? dto.getNombre().trim() : null);
        if (nombre == null || nombre.isBlank()) {
            throw new BadRequestException("El nombre del canal es obligatorio");
        }

        PerfilPersonaje responsable = personajeRepository.findById(dto.getPersonajeResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Personaje responsable no encontrado"));

        Canal canal = new Canal();
        canal.setNombre(nombre);
        canal.setDescripcion(HtmlUtils.sanitize(dto.getDescripcion()));
        canal.setTipo(TipoCanal.OFFICIAL);
        canal.setPrivado(false);
        canal.setCreador(responsable);
        canal.setFechaCreacion(new Date());
        canal = canalRepository.save(canal);

        if (imagenFile != null && !imagenFile.isEmpty()) {
            canal = canalService.guardarImagenCanal(canal, imagenFile);
        }

        MiembroCanal miembro = new MiembroCanal();
        miembro.setCanal(canal);
        miembro.setPersonaje(responsable);
        miembro.setRol(RolCanal.OWNER);
        miembro.setFechaUnion(new Date());
        miembroRepository.save(miembro);

        auditoriaService.registrar(AccionModeracion.CANAL_OFICIAL_CREADO, admin, canal,
                "Canal oficial creado",
                "Responsable: " + responsable.getNombre() + " (#" + responsable.getIdPersonaje() + ")");

        return CanalModeracionDTO.fromEntity(canal, 1L);
    }

    /**
     * Promueve un canal existente a oficial. Un oficial es siempre publico, asi
     * que la marca fuerza privado = false.
     */
    @Transactional
    public CanalModeracionDTO marcarOficial(Integer canalId, Usuario admin) {
        Canal canal = buscarCanal(canalId);

        if (canal.getTipo() == TipoCanal.OFFICIAL) {
            throw new BadRequestException("El canal ya es oficial");
        }

        canal.setTipo(TipoCanal.OFFICIAL);
        canal.setPrivado(false);
        canal = canalRepository.save(canal);

        auditoriaService.registrar(AccionModeracion.CANAL_MARCADO_OFICIAL, admin, canal,
                "Canal marcado como oficial", "El canal pasa a ser público");

        return CanalModeracionDTO.fromEntity(canal, miembroRepository.countByCanalId(canalId));
    }

    /**
     * Retira la marca de oficial. El canal sigue siendo publico; el dueño decide
     * despues si quiere hacerlo privado.
     */
    @Transactional
    public CanalModeracionDTO desmarcarOficial(Integer canalId, Usuario admin) {
        Canal canal = buscarCanal(canalId);

        if (canal.getTipo() != TipoCanal.OFFICIAL) {
            throw new BadRequestException("El canal no es oficial");
        }

        canal.setTipo(TipoCanal.USER_CREATED);
        canal = canalRepository.save(canal);

        auditoriaService.registrar(AccionModeracion.CANAL_DESMARCADO_OFICIAL, admin, canal,
                "Canal desmarcado como oficial", "El canal sigue público; el dueño decide si lo hace privado");

        return CanalModeracionDTO.fromEntity(canal, miembroRepository.countByCanalId(canalId));
    }

    /**
     * Restaura las suspensiones temporales vencidas aunque nadie haya abierto
     * el listado. Las indefinidas (sin fecha) se quedan hasta restauracion manual.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void restaurarSuspensionesExpiradas() {
        List<Canal> expirados = canalRepository.findBySuspendidoTrueAndSuspendidoHastaBefore(new Date());
        if (expirados.isEmpty()) return;

        for (Canal canal : expirados) {
            limpiarSuspension(canal);
            canalRepository.save(canal);
            notificarMiembros(canal, "CANAL_RESTAURADO", null, null);
            auditoriaService.registrar(AccionModeracion.CANAL_RESTAURADO, null, canal,
                    "Suspensión expirada", "Restauración automática al vencer el plazo");
        }

        log.info("Restaurados {} canales con suspensión expirada", expirados.size());
    }

    private Canal buscarCanal(Integer canalId) {
        return canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));
    }

    private void limpiarSuspension(Canal canal) {
        canal.setSuspendido(false);
        canal.setSuspendidoHasta(null);
        canal.setFechaSuspension(null);
        canal.setSuspendidoPorId(null);
        canal.setSuspendidoPorNombre(null);
        canal.setMotivoSuspension(null);
    }

    private String validarMotivo(String motivo) {
        String limpio = HtmlUtils.sanitize(motivo != null ? motivo.trim() : null);
        if (limpio == null || limpio.isBlank()) {
            throw new BadRequestException("El motivo es obligatorio");
        }
        if (limpio.length() > MAX_MOTIVO) {
            throw new BadRequestException("El motivo no puede superar " + MAX_MOTIVO + " caracteres");
        }
        return limpio;
    }

    private void notificarMiembros(Canal canal, String tipo, Date hasta, String motivo) {
        List<MiembroCanal> miembros = miembroRepository.findByCanalIdCanal(canal.getIdCanal());
        for (MiembroCanal miembro : miembros) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("tipo", tipo);
            payload.put("canalId", canal.getIdCanal());
            payload.put("canalNombre", canal.getNombre());
            payload.put("hasta", hasta);
            payload.put("motivo", motivo);
            messagingTemplate.convertAndSend(
                    "/topic/privado." + miembro.getPersonaje().getIdPersonaje(),
                    (Object) payload);
        }
    }
}
