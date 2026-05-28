package com.playrole.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.AmigoDTO;
import com.playrole.dto.LoginDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.security.CustomUserDetails;
import com.playrole.service.CaptchaService;
import com.playrole.service.ISolicitudAmistadService;
import com.playrole.service.IUsuarioService;
import com.playrole.utils.JwtUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final IUsuarioService usuarioService;
	private final ISolicitudAmistadService amistadService;
	private final AuthenticationManager authenticationManager;
	private final CaptchaService captchaService;
	@Autowired
	private JwtUtils jwtUtils;

    public UsuarioController(IUsuarioService usuarioService,
    		ISolicitudAmistadService amistadService,
    		AuthenticationManager authenticationManager,
    		CaptchaService captchaService) {
        this.usuarioService = usuarioService;
        this.amistadService = amistadService;
        this.authenticationManager = authenticationManager;
        this.captchaService = captchaService;
    }

    @GetMapping
    public Page<UsuarioDTO> obtenerTodos(
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size ) {
        return usuarioService.listarUsuarios(page, size);
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtenerPorId(@PathVariable Integer id) {
        return usuarioService.obtenerUsuario(id);
    }

    @GetMapping("/buscar")
    public ResponseEntity<UsuarioDTO> buscarPorNombre(@RequestParam String nombre) {
        return usuarioService.buscarPorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioDTO crearUsuario(
            @RequestPart("usuario") @Valid UsuarioCrearDTO usuarioCrearDTO,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        captchaService.verify(usuarioCrearDTO.getCaptchaToken());
        return usuarioService.guardarUsuario(usuarioCrearDTO, avatarFile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioDTO> modificarUsuario(
            @PathVariable Integer id,
            @RequestPart("usuario") UsuarioDTO usuarioDTO,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {

        UsuarioDTO actualizado = usuarioService.modificarUsuario(id, usuarioDTO, avatarFile);
        return ResponseEntity.ok(actualizado);
    }
    
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> cambiarRol(
            @PathVariable Integer id,
            @RequestParam String rol) {

        UsuarioDTO actualizado = usuarioService.cambiarRol(id, rol);
        return ResponseEntity.ok(actualizado);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        captchaService.verify(loginDTO.getCaptchaToken());
    	try {
    		//comprobamos las credenciales
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getNombre(),
                    loginDTO.getPassword()
                )
            );
            //cargamos los detalles del usuario, id, rol...
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            //generamos el token que contendra toda esa info desponible en el front
            String token = jwtUtils.generarToken(userDetails);
            
            //actilizamos la fecha de la ultima conexion
            usuarioService.actualizarUltimaConexion(loginDTO);
            
            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas");
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
