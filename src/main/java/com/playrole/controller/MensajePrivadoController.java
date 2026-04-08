package com.playrole.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.MensajePrivadoDTO;
import com.playrole.enums.EstadoMensaje;
import com.playrole.model.MensajePrivado;
import com.playrole.service.IMensajePrivadoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mensajes")
public class MensajePrivadoController {
	
	private final IMensajePrivadoService mensajeService;

    public MensajePrivadoController(IMensajePrivadoService mensajeService) {
        this.mensajeService = mensajeService;
    }
    
    @GetMapping("/{idMensaje}/{idUsuario}")
    public MensajePrivadoDTO obtenerMensaje(
            @PathVariable Integer idMensaje,
            @PathVariable Integer idUsuario) {

        return mensajeService.obtenerMensaje(idMensaje, idUsuario);
    }

    @GetMapping("/recibidos/{idUsuario}")
    public List<MensajePrivadoDTO> mensajesRecibidos(@PathVariable Integer idUsuario) {
        return mensajeService.mensajesRecibidos(idUsuario);
    }

    @GetMapping("/enviados/{idUsuario}")
    public List<MensajePrivadoDTO> mensajesEnviados(@PathVariable Integer idUsuario) {
        return mensajeService.mensajesEnviados(idUsuario);
    }

    @GetMapping("/no-leidos/{idUsuario}")
    public List<MensajePrivadoDTO> mensajesNoLeidos(@PathVariable Integer idUsuario) {
        return mensajeService.mensajesNoLeidos(idUsuario);
    }

    @PostMapping("/enviar")
    public MensajePrivadoDTO enviarMensaje(@RequestBody @Valid MensajePrivadoDTO mensajeDTO) {
        return mensajeService.enviarMensaje(mensajeDTO);
    }

    // NUEVO formato REST para filtrar
    @GetMapping("/recibidos/{idUsuario}/filtrar")
    public List<MensajePrivadoDTO> filtrarPorEstado(
            @PathVariable Integer idUsuario,
            @RequestParam List<EstadoMensaje> estados) {

        return mensajeService.filtrarPorEstadoRecibidos(idUsuario, estados);
    }

    @PutMapping("/marcar-leido/{idMensaje}/{idUsuario}")
    public void marcarComoLeido(
            @PathVariable Integer idMensaje,
            @PathVariable Integer idUsuario) {

        mensajeService.marcarComoLeido(idMensaje, idUsuario);
    }

    @GetMapping("/recibidos/{idUsuario}/count-no-leidos")
    public Long contarNoLeidos(@PathVariable Integer idUsuario) {
        return mensajeService.contarNoLeidos(idUsuario);
    }

    @DeleteMapping("/{idMensaje}/{idUsuario}")
    public void eliminarMensaje(
            @PathVariable Integer idMensaje,
            @PathVariable Integer idUsuario) {

        mensajeService.eliminarMensaje(idMensaje, idUsuario);
    }
}
