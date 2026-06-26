package com.playrole.chat.service;

import com.playrole.chat.auth.SessionJwtUtils;
import com.playrole.chat.dto.SesionPersonajeDTO;
import com.playrole.chat.model.SesionPersonaje;
import com.playrole.chat.repository.SesionPersonajeRepository;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SesionPersonajeService {

    private static final int MAX_SESIONES_ACTIVAS = 5;

    private final SesionPersonajeRepository sesionRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final SessionJwtUtils sessionJwtUtils;

    public SesionPersonajeService(SesionPersonajeRepository sesionRepository,
                                   UsuarioRepositoryInterface usuarioRepository,
                                   PerfilPersonajeRepositoryInterface personajeRepository,
                                   SessionJwtUtils sessionJwtUtils) {
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.personajeRepository = personajeRepository;
        this.sessionJwtUtils = sessionJwtUtils;
    }

    @Transactional
    public SesionPersonajeDTO iniciarSesion(Integer usuarioId, Integer personajeId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PerfilPersonaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));

        if (!personaje.getUserId().getUserId().equals(usuarioId)) {
            throw new BadRequestException("Este personaje no te pertenece");
        }

        long sesionesActivas = sesionRepository.countActivasByUsuario(usuarioId);
        if (sesionesActivas >= MAX_SESIONES_ACTIVAS) {
            throw new BadRequestException("Has alcanzado el límite de " + MAX_SESIONES_ACTIVAS
                    + " sesiones activas. Cierra otra sesión primero.");
        }

        String token = sessionJwtUtils.generarToken(
                usuarioId, personajeId, personaje.getNombre(),
                personaje.getAvatar(), usuario.getRol().name());

        SesionPersonaje sesion = new SesionPersonaje();
        sesion.setUsuario(usuario);
        sesion.setPersonaje(personaje);
        sesion.setTokenJwt(token);
        sesion.setFechaInicio(new Date());
        sesion.setUltimaActividad(new Date());
        sesion.setActiva(true);

        sesion = sesionRepository.save(sesion);

        return toDTO(sesion);
    }

    @Transactional
    public void cerrarSesion(String tokenJwt) {
        SesionPersonaje sesion = sesionRepository.findByTokenJwtAndActivaTrue(tokenJwt)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        sesion.setActiva(false);
        sesionRepository.save(sesion);
    }

    @Transactional
    public void cerrarTodasSesiones(Integer usuarioId) {
        List<SesionPersonaje> sesiones = sesionRepository.findActivasByUsuario(usuarioId);
        for (SesionPersonaje sesion : sesiones) {
            sesion.setActiva(false);
        }
        sesionRepository.saveAll(sesiones);
    }

    public List<SesionPersonajeDTO> sesionesActivas(Integer usuarioId) {
        return sesionRepository.findActivasByUsuario(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SesionPersonajeDTO toDTO(SesionPersonaje sesion) {
        SesionPersonajeDTO dto = new SesionPersonajeDTO();
        dto.setIdSesion(sesion.getIdSesion());
        dto.setPersonajeId(sesion.getPersonaje().getIdPersonaje());
        dto.setPersonajeNombre(sesion.getPersonaje().getNombre());
        dto.setPersonajeAvatar(sesion.getPersonaje().getAvatar());
        dto.setUsuarioId(sesion.getUsuario().getUserId());
        dto.setTokenJwt(sesion.getTokenJwt());
        dto.setFechaInicio(sesion.getFechaInicio());
        dto.setUltimaActividad(sesion.getUltimaActividad());
        return dto;
    }
}
