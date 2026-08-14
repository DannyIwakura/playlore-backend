package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.playrole.enums.RolUsuario;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.model.Usuario;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.DenunciaRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.chat.repository.SesionPersonajeRepository;
import com.playrole.repository.SolicitudAmistadRespositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

class UsuarioServiceImplTest {

    private UsuarioServiceImpl service;
    private UsuarioRepositoryInterface usuarioRepositorio;
    private PasswordEncoder passwordEncoder;
    private PerfilPersonajeRepositoryInterface personajeRepositorio;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl();
        usuarioRepositorio = mock(UsuarioRepositoryInterface.class);
        passwordEncoder = mock(PasswordEncoder.class);
        personajeRepositorio = mock(PerfilPersonajeRepositoryInterface.class);
        ReflectionTestUtils.setField(service, "usuarioRepositorio", usuarioRepositorio);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "personajeRepositorio", personajeRepositorio);
        ReflectionTestUtils.setField(service, "denunciaRepositorio", mock(DenunciaRepository.class));
        ReflectionTestUtils.setField(service, "baneoGlobalRepositorio", mock(BaneoGlobalRepository.class));
        ReflectionTestUtils.setField(service, "sesionRepositorio", mock(SesionPersonajeRepository.class));
        ReflectionTestUtils.setField(service, "solicitudRepositorio", mock(SolicitudAmistadRespositoryInterface.class));
    }

    private Usuario usuario(int id) {
        Usuario u = new Usuario();
        u.setUserId(id);
        u.setNombre("Kael");
        u.setEmail("kael@test.com");
        u.setPassword("$2a$hash-almacenado");
        u.setAvatar("/images/AVATAR.png");
        u.setRol(RolUsuario.USER);
        u.setFechaRegistro(new Date());
        u.setUltimaConexion(new Date());
        return u;
    }

    @Test
    void eliminarCuentaPropia_sinPassword_lanzaBadRequest() {
        when(usuarioRepositorio.findById(7)).thenReturn(Optional.of(usuario(7)));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.eliminarCuentaPropia(7, null));

        assertEquals("Debes introducir tu contraseña para eliminar la cuenta", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void eliminarCuentaPropia_passwordIncorrecta_lanzaAccessDenied() {
        when(usuarioRepositorio.findById(7)).thenReturn(Optional.of(usuario(7)));
        when(passwordEncoder.matches("clave-mala", "$2a$hash-almacenado")).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.eliminarCuentaPropia(7, "clave-mala"));

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    @Test
    void eliminarCuentaPropia_passwordCorrecta_eliminaCuenta() {
        when(usuarioRepositorio.findById(7)).thenReturn(Optional.of(usuario(7)));
        when(passwordEncoder.matches("clave-buena", "$2a$hash-almacenado")).thenReturn(true);
        when(personajeRepositorio.findByUserId_UserId(7)).thenReturn(List.of());

        service.eliminarCuentaPropia(7, "clave-buena");

        verify(passwordEncoder).matches("clave-buena", "$2a$hash-almacenado");
        verify(usuarioRepositorio).deleteById(7);
    }

    @Test
    void eliminarCuentaPropia_usuarioInexistente_lanzaNotFound() {
        when(usuarioRepositorio.findById(999)).thenReturn(Optional.empty());

        org.springframework.web.server.ResponseStatusException ex =
                assertThrows(org.springframework.web.server.ResponseStatusException.class,
                        () -> service.eliminarCuentaPropia(999, "clave"));

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("no encontrado"));
    }
}
