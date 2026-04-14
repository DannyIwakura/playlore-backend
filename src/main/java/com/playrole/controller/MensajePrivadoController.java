package com.playrole.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.playrole.dto.MensajePrivadoDTO;
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

    @GetMapping("/archivados/{idUsuario}")
    public List<MensajePrivadoDTO> mensajesArchivados(@PathVariable Integer idUsuario) {
        return mensajeService.mensajesArchivados(idUsuario);
    }

    @GetMapping("/no-leidos/{idUsuario}")
    public List<MensajePrivadoDTO> mensajesNoLeidos(@PathVariable Integer idUsuario) {
        return mensajeService.mensajesNoLeidos(idUsuario);
    }

    @PostMapping("/enviar")
    public MensajePrivadoDTO enviarMensaje(@RequestBody @Valid MensajePrivadoDTO mensajeDTO) {
        return mensajeService.enviarMensaje(mensajeDTO);
    }

    // Filtrado simplificado basado en booleanos
    @GetMapping("/recibidos/{idUsuario}/filtrar")
    public List<MensajePrivadoDTO> filtrar(
            @PathVariable Integer idUsuario,
            @RequestParam(defaultValue = "false") boolean soloLeidos,
            @RequestParam(defaultValue = "false") boolean soloArchivados) {
        return mensajeService.listarPorCriterio(idUsuario, soloLeidos, soloArchivados);
    }

    @PutMapping("/marcar-leido/{idMensaje}/{idUsuario}")
    public void marcarComoLeido(
            @PathVariable Integer idMensaje,
            @PathVariable Integer idUsuario) {
        mensajeService.marcarComoLeido(idMensaje, idUsuario);
    }

    @PutMapping("/archivar/{idMensaje}/{idUsuario}")
    public void archivarMensaje(
            @PathVariable Integer idMensaje,
            @PathVariable Integer idUsuario) {
        mensajeService.archivarMensaje(idMensaje, idUsuario);
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