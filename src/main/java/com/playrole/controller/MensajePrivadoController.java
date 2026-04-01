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
import com.playrole.model.MensajePrivado;
import com.playrole.service.IMensajePrivadoService;

@RestController
@RequestMapping("/mensajes")
public class MensajePrivadoController {
	
	private final IMensajePrivadoService mensajeService;

    public MensajePrivadoController(IMensajePrivadoService mensajeService) {
        this.mensajeService = mensajeService;
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
        return mensajeService.obtenerMensajesNoLeidos(idUsuario);
    }

    @PostMapping("/enviar")
    public MensajePrivadoDTO enviarMensaje(
            @RequestParam Integer idEmisor,
            @RequestParam Integer idReceptor,
            @RequestParam String contenido) {
        return mensajeService.enviarMensaje(idEmisor, idReceptor, contenido);
    }

    @PostMapping("/filtrar-estado/{idUsuario}")
    public List<MensajePrivadoDTO> filtrarPorEstado(
            @PathVariable Integer idUsuario,
            @RequestBody List<Integer> estados) {
        return mensajeService.findByEstado(idUsuario, estados);
    }

    @PutMapping("/marcar-leido/{idMensaje}")
    public void marcarComoLeido(@PathVariable Integer idMensaje) {
        mensajeService.marcarComoLeido(idMensaje);
    }

    @DeleteMapping("/{idMensaje}")
    public void eliminarMensaje(@PathVariable Integer idMensaje) {
        mensajeService.eliminarMensaje(idMensaje);
    }
   

}
