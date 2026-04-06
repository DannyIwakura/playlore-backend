package com.playrole.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.AmigoDTO;
import com.playrole.dto.CrearSolicitudAmistadDTO;
import com.playrole.dto.SolicitudAmistadDTO;
import com.playrole.service.ISolicitudAmistadService;

@RestController
@RequestMapping("/amistades")
public class SolicitudAmistadController {
	
	private final ISolicitudAmistadService amistadService;

	public SolicitudAmistadController(ISolicitudAmistadService amistadService) {
		this.amistadService = amistadService;
	}
	
	@PostMapping
    public SolicitudAmistadDTO enviarSolicitud(@RequestBody CrearSolicitudAmistadDTO dto) {
        return amistadService.enviarSolicitud(dto.getEmisorId(), dto.getReceptorId());
    }

    @GetMapping("/pendientes-recibidas/{idUsuario}")
    public List<SolicitudAmistadDTO> pendientesRecibidas(@PathVariable Integer idUsuario) {
        return amistadService.obtenerSolicitudesPendientesRecibidas(idUsuario);
    }

    @GetMapping("/pendientes-enviadas/{idUsuario}")
    public List<SolicitudAmistadDTO> pendientesEnviadas(@PathVariable Integer idUsuario) {
        return amistadService.obtenerSolicitudesPendientesEnviadas(idUsuario);
    }

    @PutMapping("/aceptar/{idSolicitud}")
    public SolicitudAmistadDTO aceptarSolicitud(@PathVariable Integer idSolicitud) {
        return amistadService.aceptarSolicitud(idSolicitud);
    }

    @PutMapping("/rechazar/{idSolicitud}")
    public SolicitudAmistadDTO rechazarSolicitud(@PathVariable Integer idSolicitud) {
        return amistadService.rechazarSolicitud(idSolicitud);
    }

    @GetMapping("/comprobar-existencia/{emisorId}/{receptorId}")
    public boolean solicitudExiste(@PathVariable Integer emisorId, @PathVariable Integer receptorId) {
        return amistadService.existeSolicitud(emisorId, receptorId);
    }

    @DeleteMapping("/{id}")
    public void eliminarSolicitud(@PathVariable Integer id) {
        amistadService.eliminarSolicitud(id);
    }
}
