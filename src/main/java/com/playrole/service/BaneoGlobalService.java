package com.playrole.service;

import com.playrole.chat.service.SesionPersonajeService;
import com.playrole.dto.BaneoGlobalDTO;
import com.playrole.dto.CrearBaneoDTO;
import com.playrole.enums.AccionModeracion;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.BaneoGlobal;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.utils.ModeracionUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaneoGlobalService {

    private final BaneoGlobalRepository baneoRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final SesionPersonajeService sesionPersonajeService;
    private final AuditoriaModeracionService auditoriaService;

    public BaneoGlobalService(BaneoGlobalRepository baneoRepository,
                              UsuarioRepositoryInterface usuarioRepository,
                              PerfilPersonajeRepositoryInterface personajeRepository,
                              SesionPersonajeService sesionPersonajeService,
                              AuditoriaModeracionService auditoriaService) {
        this.baneoRepository = baneoRepository;
        this.usuarioRepository = usuarioRepository;
        this.personajeRepository = personajeRepository;
        this.sesionPersonajeService = sesionPersonajeService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public BaneoGlobalDTO banear(CrearBaneoDTO dto, Usuario admin) {
        if (dto.getId() == null) {
            throw new BadRequestException("El id del objetivo es obligatorio");
        }
        if (!"USUARIO".equals(dto.getTipo()) && !"PERSONAJE".equals(dto.getTipo())) {
            throw new BadRequestException("El tipo debe ser USUARIO o PERSONAJE");
        }

        if ("USUARIO".equals(dto.getTipo())) {
            Usuario objetivo = usuarioRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            if (objetivo.getUserId().equals(admin.getUserId())) {
                throw new BadRequestException("No puedes banearte a ti mismo");
            }
            if (objetivo.getRol().name().equals("ADMIN")) {
                throw new BadRequestException("No puedes banear a un administrador");
            }
            if (baneoRepository.existsActivoByUsuario(objetivo.getUserId())) {
                throw new BadRequestException("El usuario ya está baneado");
            }

            BaneoGlobal baneo = new BaneoGlobal();
            baneo.setUsuario(objetivo);
            baneo.setBaneadoPor(admin);
            baneo.setMotivo(dto.getMotivo());
            baneo.setFechaBaneo(new Date());
            baneo.setFechaExpiracion(calcularExpiracion(dto.getDuracion()));
            baneo = baneoRepository.save(baneo);

            sesionPersonajeService.cerrarTodasSesiones(objetivo.getUserId());
            auditoriaService.registrar(AccionModeracion.BANEO_CUENTA, admin, objetivo, null,
                    dto.getMotivo(), dto.getDuracion() != null ? "Duración: " + dto.getDuracion() : "Permanente");
            return BaneoGlobalDTO.fromEntity(baneo);
        } else {
            PerfilPersonaje objetivo = personajeRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));
            if (baneoRepository.existsActivoByPersonaje(objetivo.getIdPersonaje())) {
                throw new BadRequestException("El personaje ya está baneado");
            }

            BaneoGlobal baneo = new BaneoGlobal();
            baneo.setPersonaje(objetivo);
            baneo.setBaneadoPor(admin);
            baneo.setMotivo(dto.getMotivo());
            baneo.setFechaBaneo(new Date());
            baneo.setFechaExpiracion(calcularExpiracion(dto.getDuracion()));
            baneo = baneoRepository.save(baneo);

            sesionPersonajeService.cerrarSesionesDePersonaje(objetivo.getIdPersonaje());
            auditoriaService.registrar(AccionModeracion.BANEO_PERSONAJE, admin, null, objetivo,
                    dto.getMotivo(), dto.getDuracion() != null ? "Duración: " + dto.getDuracion() : "Permanente");
            return BaneoGlobalDTO.fromEntity(baneo);
        }
    }

    @Transactional
    public void desbanear(Integer baneoId, Usuario moderador) {
        BaneoGlobal baneo = baneoRepository.findById(baneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Baneo no encontrado"));
        baneoRepository.delete(baneo);
        auditoriaService.registrar(AccionModeracion.DESBANEO, moderador,
                baneo.getUsuario(), baneo.getPersonaje(), baneo.getMotivo(),
                "Baneo #" + baneoId + " eliminado");
    }

    public List<BaneoGlobalDTO> listar() {
        return baneoRepository.findAllByOrderByFechaBaneoDesc().stream()
                .map(BaneoGlobalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void verificarUsuarioNoBaneado(Integer usuarioId) {
        if (usuarioId != null && baneoRepository.existsActivoByUsuario(usuarioId)) {
            throw new AccessDeniedException("Tu cuenta ha sido suspendida. Contacta con el soporte.");
        }
    }

    public void verificarNoBaneado(Integer usuarioId, Integer personajeId) {
        if (usuarioId != null && baneoRepository.existsActivoByUsuarioOPersonaje(usuarioId, personajeId)) {
            throw new AccessDeniedException("No puedes realizar esta acción: tu cuenta o personaje está suspendido.");
        }
    }

    private Date calcularExpiracion(String duracion) {
        return ModeracionUtils.calcularExpiracion(duracion);
    }
}
