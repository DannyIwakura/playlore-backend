package com.playrole.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.AmigoDTO;
import com.playrole.dto.SolicitudAmistadDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.model.Usuario;
import com.playrole.service.ISolicitudAmistadService;
import com.playrole.service.IUsuarioService;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final IUsuarioService usuarioService;
	private final ISolicitudAmistadService amistadService;

    public UsuarioController(IUsuarioService usuarioService,
    		ISolicitudAmistadService amistadService) {
        this.usuarioService = usuarioService;
        this.amistadService = amistadService;
    }

    @GetMapping
    public List<UsuarioDTO> obtenerTodos() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtenerPorId(@PathVariable Integer id) {
        return usuarioService.obtenerUsuario(id);
    }

    @PostMapping
    public UsuarioDTO crearUsuario(@RequestBody UsuarioCrearDTO usuarioCrearDTO) {
        return usuarioService.guardarUsuario(usuarioCrearDTO);
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizarUsuario(@PathVariable Integer id, @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioService.modificarUsuario(id, usuarioDTO);
    }

    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
    }
    
    @GetMapping("/{id}/amigos")
    public List<AmigoDTO> obtenerAmigos(@PathVariable Integer id) {
        return amistadService.obtenerAmigos(id);
    }
    
    @DeleteMapping("{userId}/amigos/{amigoId}")
    public void eliminarAmistad(@PathVariable Integer userId,
                                @PathVariable Integer amigoId) {
        amistadService.eliminarAmistadEntreUsuarios(userId, amigoId);
    }
}
