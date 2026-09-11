package com.playrole.controller;

import java.util.List;
import java.util.Map;

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

import jakarta.servlet.http.HttpServletRequest;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.dto.AmigoDTO;
import com.playrole.dto.GoogleLoginDTO;
import com.playrole.dto.LoginDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.TooManyRequestsException;
import com.playrole.security.CustomUserDetails;
import com.playrole.service.CaptchaService;
import com.playrole.service.BaneoGlobalService;
import com.playrole.service.GoogleAuthService;
import com.playrole.service.ISolicitudAmistadService;
import com.playrole.service.IUsuarioService;
import com.playrole.service.LoginAttemptService;
import com.playrole.utils.JwtUtils;
import com.playrole.utils.AuthUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final IUsuarioService usuarioService;
	private final ISolicitudAmistadService amistadService;
	private final AuthenticationManager authenticationManager;
	private final CaptchaService captchaService;
	private final GoogleAuthService googleAuthService;
	private final LoginAttemptService loginAttemptService;
	private final BaneoGlobalService baneoGlobalService;
	@Autowired
	private JwtUtils jwtUtils;

    public UsuarioController(IUsuarioService usuarioService,
    		ISolicitudAmistadService amistadService,
    		AuthenticationManager authenticationManager,
    		CaptchaService captchaService,
    		GoogleAuthService googleAuthService,
    		LoginAttemptService loginAttemptService,
    		BaneoGlobalService baneoGlobalService) {
        this.usuarioService = usuarioService;
        this.amistadService = amistadService;
        this.authenticationManager = authenticationManager;
        this.captchaService = captchaService;
        this.googleAuthService = googleAuthService;
        this.loginAttemptService = loginAttemptService;
        this.baneoGlobalService = baneoGlobalService;
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
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            HttpServletRequest request) {
        String key = "registro:" + obtenerIpCliente(request);
        if (loginAttemptService.estaBloqueado(key)) {
            throw new TooManyRequestsException("Demasiados registros desde esta IP. Espera unos minutos.");
        }
        captchaService.verify(usuarioCrearDTO.getCaptchaToken());
        return usuarioService.guardarUsuario(usuarioCrearDTO, avatarFile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioDTO> modificarUsuario(
            @PathVariable Integer id,
            @RequestPart("usuario") UsuarioDTO usuarioDTO,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            Authentication authentication) {

        Integer userId = obtenerUserId(authentication);
        if (!id.equals(userId) && !esAdmin(authentication)) {
            throw new AccessDeniedException("No puedes modificar el perfil de otro usuario");
        }

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
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String key = loginDTO.getNombre().toLowerCase() + ":" + obtenerIpCliente(request);
        if (loginAttemptService.estaBloqueado(key)) {
            throw new TooManyRequestsException("Demasiados intentos fallidos. Espera 15 minutos.");
        }
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

            //comprobamos si el usuario está baneado globalmente
            baneoGlobalService.verificarUsuarioNoBaneado(userDetails.getUsuario().getUserId());

            //generamos el token que contendra toda esa info desponible en el front
            String token = jwtUtils.generarToken(userDetails);
            
            //actilizamos la fecha de la ultima conexion
            usuarioService.actualizarUltimaConexion(loginDTO);

            loginAttemptService.limpiar(key);
            
            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            loginAttemptService.registrarIntentoFallido(key);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas");
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<String> googleLogin(@Valid @RequestBody GoogleLoginDTO googleLoginDTO,
                                              HttpServletRequest request) {
        String key = "google:" + obtenerIpCliente(request);
        if (loginAttemptService.estaBloqueado(key)) {
            throw new TooManyRequestsException("Demasiados intentos desde esta IP. Espera unos minutos.");
        }
        try {
            String token = googleAuthService.loginConCredential(googleLoginDTO.getCredential());
            loginAttemptService.limpiar(key);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            loginAttemptService.registrarIntentoFallido(key);
            throw e;
        }
    }

    @PutMapping("/{id}/ultima-conexion")
    public void actualizarUltimaConexion(@PathVariable Integer id, Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        if (!id.equals(userId)) {
            throw new AccessDeniedException("No puedes actualizar la conexión de otro usuario");
        }
        usuarioService.actualizarUltimaConexion(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Integer id,
                                @RequestBody(required = false) Map<String, String> body,
                                Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        if (id.equals(userId)) {
            String password = body == null ? null : body.get("password");
            usuarioService.eliminarCuentaPropia(id, password);
            return;
        }
        if (!esAdmin(authentication)) {
            throw new AccessDeniedException("No puedes eliminar la cuenta de otro usuario");
        }
        usuarioService.eliminarUsuario(id);
    }
    
    @GetMapping("/{id}/amigos")
    public List<AmigoDTO> obtenerAmigos(@PathVariable Integer id, Authentication authentication) {
        Integer userId = obtenerUserId(authentication);
        if (!id.equals(userId)) {
            throw new AccessDeniedException("No puedes ver los amigos de otro usuario");
        }
        return amistadService.obtenerAmigos(id);
    }
    
    @DeleteMapping("{userId}/amigos/{amigoId}")
    public void eliminarAmistad(@PathVariable Integer userId,
                                @PathVariable Integer amigoId,
                                Authentication authentication) {
        Integer autenticado = obtenerUserId(authentication);
        if (!userId.equals(autenticado)) {
            throw new AccessDeniedException("No puedes eliminar las amistades de otro usuario");
        }
        amistadService.eliminarAmistadEntreUsuarios(userId, amigoId);
    }

    private boolean esAdmin(Authentication authentication) {
        return AuthUtils.esAdmin(authentication);
    }

    private Integer obtenerUserId(Authentication authentication) {
        return AuthUtils.obtenerUserId(authentication);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
