package com.playrole.service;

import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MensajeCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.dto.CrearDenunciaDTO;
import com.playrole.dto.DenunciaDTO;
import com.playrole.enums.EstadoDenuncia;
import com.playrole.enums.TipoDenuncia;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.Denuncia;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.DenunciaRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.utils.HtmlUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final MensajeCanalRepository mensajeCanalRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final CanalRepository canalRepository;

    public DenunciaService(DenunciaRepository denunciaRepository,
                           MensajeCanalRepository mensajeCanalRepository,
                           PerfilPersonajeRepositoryInterface personajeRepository,
                           UsuarioRepositoryInterface usuarioRepository,
                           CanalRepository canalRepository) {
        this.denunciaRepository = denunciaRepository;
        this.mensajeCanalRepository = mensajeCanalRepository;
        this.personajeRepository = personajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.canalRepository = canalRepository;
    }

    @Transactional
    public DenunciaDTO crearDenuncia(CrearDenunciaDTO dto, Usuario denunciante, PerfilPersonaje denunciantePersonaje) {
        if (dto.getTipo() == null || dto.getTipoId() == null) {
            throw new BadRequestException("Tipo y elemento a denunciar son obligatorios");
        }

        if (dto.getTipo() == TipoDenuncia.USUARIO && denunciante.getUserId().equals(dto.getTipoId())) {
            throw new BadRequestException("No puedes denunciarte a ti mismo");
        }
        if (dto.getTipo() == TipoDenuncia.PERSONAJE
                && denunciantePersonaje != null
                && denunciantePersonaje.getIdPersonaje().equals(dto.getTipoId())) {
            throw new BadRequestException("No puedes denunciar tu propio personaje");
        }
        if (dto.getTipo() == TipoDenuncia.CANAL) {
            Canal canal = canalRepository.findById(dto.getTipoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));
            if (denunciantePersonaje != null && canal.getCreador() != null
                    && canal.getCreador().getIdPersonaje().equals(denunciantePersonaje.getIdPersonaje())) {
                throw new BadRequestException("No puedes denunciar tu propio canal");
            }
            if (canal.getCreadoPorUsuario() != null
                    && canal.getCreadoPorUsuario().getUserId().equals(denunciante.getUserId())) {
                throw new BadRequestException("No puedes denunciar tu propio canal");
            }
        }

        if (denunciaRepository.existsByTipoAndTipoIdAndDenuncianteUserIdAndEstado(
                dto.getTipo(), dto.getTipoId(), denunciante.getUserId(), EstadoDenuncia.PENDIENTE)) {
            throw new BadRequestException("Ya has denunciado este elemento y está pendiente de revisión");
        }

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(denunciante);
        denuncia.setDenunciantePersonaje(denunciantePersonaje);
        denuncia.setTipo(dto.getTipo());
        denuncia.setTipoId(dto.getTipoId());
        denuncia.setMotivo(dto.getMotivo());
        denuncia.setDetalle(HtmlUtils.sanitize(dto.getDetalle()));

        if (dto.getTipo() == TipoDenuncia.MENSAJE_CANAL) {
            MensajeCanal mensaje = mensajeCanalRepository.findById(dto.getTipoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));
            if (denunciantePersonaje != null
                    && mensaje.getPersonaje().getIdPersonaje().equals(denunciantePersonaje.getIdPersonaje())) {
                throw new BadRequestException("No puedes denunciar tu propio mensaje");
            }
            denuncia.setContenidoDenunciado(HtmlUtils.sanitize(mensaje.getContenido()));
            denuncia.setCanalId(mensaje.getCanal().getIdCanal());
            denuncia.setCanalNombre(mensaje.getCanal().getNombre());
            denuncia.setAutorPersonajeId(mensaje.getPersonaje().getIdPersonaje());
            denuncia.setAutorPersonajeNombre(mensaje.getPersonaje().getNombre());
            denuncia.setObjetivoNombre(mensaje.getPersonaje().getNombre());
            if (mensaje.getPersonaje().getUserId() != null) {
                denuncia.setAutorUsuarioId(mensaje.getPersonaje().getUserId().getUserId());
                denuncia.setAutorUsuarioNombre(mensaje.getPersonaje().getUserId().getNombre());
            }
        } else if (dto.getTipo() == TipoDenuncia.PERSONAJE) {
            PerfilPersonaje objetivo = personajeRepository.findById(dto.getTipoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));
            denuncia.setObjetivoNombre(objetivo.getNombre());
            if (objetivo.getUserId() != null) {
                denuncia.setAutorUsuarioId(objetivo.getUserId().getUserId());
                denuncia.setAutorUsuarioNombre(objetivo.getUserId().getNombre());
            }
        } else if (dto.getTipo() == TipoDenuncia.USUARIO) {
            Usuario objetivo = usuarioRepository.findById(dto.getTipoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            denuncia.setObjetivoNombre(objetivo.getNombre());
        } else if (dto.getTipo() == TipoDenuncia.CANAL) {
            Canal canal = canalRepository.findById(dto.getTipoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));
            denuncia.setCanalId(canal.getIdCanal());
            denuncia.setCanalNombre(canal.getNombre());
            denuncia.setObjetivoNombre(canal.getNombre());
        }

        return toDTO(denunciaRepository.save(denuncia));
    }

    public Page<DenunciaDTO> listarDenuncias(EstadoDenuncia estado, int page, int size) {
        return listarDenuncias(estado, null, null, null, null, null, null, page, size);
    }

    public Page<DenunciaDTO> listarDenuncias(EstadoDenuncia estado, TipoDenuncia tipo,
                                             LocalDate desde, LocalDate hasta,
                                             String objetivo, String denunciante, String resueltoPor,
                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Denuncia> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("estado"), estado));
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"),
                        Date.from(desde.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            }
            if (hasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"),
                        Date.from(hasta.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant())));
            }
            if (StringUtils.hasText(objetivo)) {
                predicates.add(cb.like(cb.lower(root.get("objetivoNombre")),
                        "%" + objetivo.toLowerCase(Locale.ROOT) + "%"));
            }
            if (StringUtils.hasText(denunciante)) {
                Join<Denuncia, Usuario> join = root.join("denunciante");
                predicates.add(cb.like(cb.lower(join.get("nombre")),
                        "%" + denunciante.toLowerCase(Locale.ROOT) + "%"));
            }
            if (StringUtils.hasText(resueltoPor)) {
                Join<Denuncia, Usuario> join = root.join("resueltoPor", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(join.get("nombre")),
                        "%" + resueltoPor.toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return denunciaRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional
    public DenunciaDTO resolverDenuncia(Integer id, EstadoDenuncia estado, String decision, Usuario admin) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));
        denuncia.setEstado(estado);
        denuncia.setDecision(decision);
        denuncia.setResueltoPor(admin);
        denuncia.setFechaResolucion(new Date());
        return toDTO(denunciaRepository.save(denuncia));
    }

    private DenunciaDTO toDTO(Denuncia d) {
        DenunciaDTO dto = new DenunciaDTO();
        dto.setIdDenuncia(d.getIdDenuncia());
        dto.setTipo(d.getTipo());
        dto.setTipoId(d.getTipoId());
        dto.setMotivo(d.getMotivo());
        dto.setDetalle(d.getDetalle());
        dto.setContenidoDenunciado(d.getContenidoDenunciado());
        dto.setCanalId(d.getCanalId());
        dto.setAutorPersonajeId(d.getAutorPersonajeId());

        dto.setCanalNombre(d.getCanalNombre());
        if (dto.getCanalNombre() == null && d.getCanalId() != null) {
            canalRepository.findById(d.getCanalId())
                    .ifPresent(c -> dto.setCanalNombre(c.getNombre()));
        }

        dto.setAutorPersonajeNombre(d.getAutorPersonajeNombre());
        if (dto.getAutorPersonajeNombre() == null && d.getAutorPersonajeId() != null) {
            personajeRepository.findById(d.getAutorPersonajeId())
                    .ifPresent(p -> dto.setAutorPersonajeNombre(p.getNombre()));
        }

        dto.setAutorUsuarioId(d.getAutorUsuarioId());
        dto.setAutorUsuarioNombre(d.getAutorUsuarioNombre());
        if (dto.getAutorUsuarioId() != null && dto.getAutorUsuarioNombre() == null) {
            usuarioRepository.findById(d.getAutorUsuarioId())
                    .ifPresent(u -> dto.setAutorUsuarioNombre(u.getNombre()));
        }

        dto.setObjetivoNombre(d.getObjetivoNombre());
        if (dto.getObjetivoNombre() == null) {
            if (d.getTipo() == TipoDenuncia.USUARIO) {
                usuarioRepository.findById(d.getTipoId())
                        .ifPresent(u -> dto.setObjetivoNombre(u.getNombre()));
            } else if (d.getTipo() == TipoDenuncia.PERSONAJE) {
                personajeRepository.findById(d.getTipoId())
                        .ifPresent(p -> dto.setObjetivoNombre(p.getNombre()));
            } else if (d.getTipo() == TipoDenuncia.CANAL && d.getCanalId() != null) {
                canalRepository.findById(d.getCanalId())
                        .ifPresent(c -> dto.setObjetivoNombre(c.getNombre()));
            } else {
                dto.setObjetivoNombre(dto.getAutorPersonajeNombre());
            }
        }

        dto.setEstado(d.getEstado());
        dto.setFecha(d.getFecha());
        if (d.getDenunciante() != null) {
            dto.setDenuncianteUserId(d.getDenunciante().getUserId());
            dto.setDenuncianteNombre(d.getDenunciante().getNombre());
        }
        if (d.getDenunciantePersonaje() != null) {
            dto.setDenunciantePersonajeId(d.getDenunciantePersonaje().getIdPersonaje());
            dto.setDenunciantePersonajeNombre(d.getDenunciantePersonaje().getNombre());
        }
        if (d.getResueltoPor() != null) {
            dto.setResueltoPorNombre(d.getResueltoPor().getNombre());
        }
        dto.setDecision(d.getDecision());
        dto.setFechaResolucion(d.getFechaResolucion());
        return dto;
    }
}
