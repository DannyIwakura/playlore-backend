package com.playrole.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playrole.dto.AmigoDTO;
import com.playrole.dto.LoginDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.service.ISolicitudAmistadService;
import com.playrole.service.IUsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final IUsuarioService usuarioService;
	private final ISolicitudAmistadService amistadService;
	private final AuthenticationManager authenticationManager;

    public UsuarioController(IUsuarioService usuarioService,
    		ISolicitudAmistadService amistadService,
    		AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.amistadService = amistadService;
        this.authenticationManager = authenticationManager;
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
    public UsuarioDTO crearUsuario(@Valid @RequestBody UsuarioCrearDTO usuarioCrearDTO) {
        return usuarioService.guardarUsuario(usuarioCrearDTO);
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizarUsuario(@PathVariable Integer id, @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioService.modificarUsuario(id, usuarioDTO);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getNombre(),
                    loginDTO.getPassword()
                )
            );
            // Esto solo prueba que las credenciales son correctas
            return ResponseEntity.ok("Login exitoso: " + loginDTO.getNombre());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
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
