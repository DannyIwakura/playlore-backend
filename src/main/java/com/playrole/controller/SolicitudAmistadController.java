package com.playrole.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.dto.CrearSolicitudAmistadDTO;
import com.playrole.dto.SolicitudAmistadDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.security.CustomUserDetails;
import com.playrole.service.ISolicitudAmistadService;

@RestController
@RequestMapping("/amistades")
public class SolicitudAmistadController {
	
	private final ISolicitudAmistadService amistadService;

	public SolicitudAmistadController(ISolicitudAmistadService amistadService) {
		this.amistadService = amistadService;
	}
	
	@PostMapping
    public SolicitudAmistadDTO enviarSolicitud(@RequestBody CrearSolicitudAmistadDTO dto,
                                               Authentication authentication) {
        Integer emisorId = obtenerUserId(authentication);
        return amistadService.enviarSolicitud(emisorId, dto.getReceptorId());
    }

    @GetMapping("/pendientes-recibidas/{idUsuario}")
    public List<SolicitudAmistadDTO> pendientesRecibidas(@PathVariable Integer idUsuario,
                                                         Authentication authentication) {
        comprobarPropietario(idUsuario, authentication);
        return amistadService.obtenerSolicitudesPendientesRecibidas(idUsuario);
    }

    @GetMapping("/pendientes-enviadas/{idUsuario}")
    public List<SolicitudAmistadDTO> pendientesEnviadas(@PathVariable Integer idUsuario,
                                                        Authentication authentication) {
        comprobarPropietario(idUsuario, authentication);
        return amistadService.obtenerSolicitudesPendientesEnviadas(idUsuario);
    }

    @PutMapping("/aceptar/{idSolicitud}")
    public SolicitudAmistadDTO aceptarSolicitud(@PathVariable Integer idSolicitud,
                                                Authentication authentication) {
        comprobarReceptor(idSolicitud, authentication);
        return amistadService.aceptarSolicitud(idSolicitud);
    }

    @PutMapping("/rechazar/{idSolicitud}")
    public SolicitudAmistadDTO rechazarSolicitud(@PathVariable Integer idSolicitud,
                                                 Authentication authentication) {
        comprobarReceptor(idSolicitud, authentication);
        return amistadService.rechazarSolicitud(idSolicitud);
    }

    @GetMapping("/comprobar-existencia/{emisorId}/{receptorId}")
    public boolean solicitudExiste(@PathVariable Integer emisorId, @PathVariable Integer receptorId,
                                   Authentication authentication) {
        comprobarPropietario(emisorId, authentication);
        return amistadService.existeSolicitud(emisorId, receptorId);
    }

    @DeleteMapping("/{id}")
    public void eliminarSolicitud(@PathVariable Integer id, Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        SolicitudAmistadDTO solicitud = amistadService.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        if (!solicitud.getEmisorId().equals(userId)) {
            throw new AccessDeniedException("Solo el emisor puede cancelar su solicitud");
        }
        amistadService.eliminarSolicitud(id);
    }

    private void comprobarPropietario(Integer idUsuario, Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        if (!userId.equals(idUsuario)) {
            throw new AccessDeniedException("No puedes consultar las solicitudes de otro usuario");
        }
    }

    private void comprobarReceptor(Integer idSolicitud, Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        SolicitudAmistadDTO solicitud = amistadService.obtenerPorId(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        if (!solicitud.getReceptorId().equals(userId)) {
            throw new AccessDeniedException("No puedes responder a una solicitud que no va dirigida a ti");
        }
    }

    private Integer obtenerUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CharacterSessionPrincipal cp) return cp.getUsuario().getUserId();
        if (principal instanceof CustomUserDetails cd) return cd.getUsuario().getUserId();
        throw new AccessDeniedException("No autenticado");
    }
}
