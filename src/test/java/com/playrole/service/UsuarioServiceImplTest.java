package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
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
import com.playrole.service.image.ImageProcessingService;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;

class UsuarioServiceImplTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;
    private UsuarioServiceImpl service;
    private UsuarioRepositoryInterface usuarioRepositorio;
    private PasswordEncoder passwordEncoder;
    private PerfilPersonajeRepositoryInterface personajeRepositorio;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

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
        ReflectionTestUtils.setField(service, "entityManager", mock(EntityManager.class));
        ReflectionTestUtils.setField(service, "imageProcessingService", new ImageProcessingService());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
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

    @Test
    void guardarUsuario_conAvatarGrande_normalizaYGuarda() throws Exception {
        when(usuarioRepositorio.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("hash");
        when(usuarioRepositorio.save(org.mockito.ArgumentMatchers.any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setUserId(1);
            return u;
        });

        UsuarioCrearDTO dto = new UsuarioCrearDTO();
        dto.setNombre("Nuevo");
        dto.setEmail("nuevo@test.com");
        dto.setPassword("Password1!");

        MockMultipartFile avatar = new MockMultipartFile("avatarFile", "foto.png", "image/png", pngBytes(500, 300));

        UsuarioDTO res = service.guardarUsuario(dto, avatar);

        assertTrue(res.getAvatar().startsWith("/uploads/avatars/"));
        String filename = res.getAvatar().replace("/uploads/avatars/", "");
        Path guardado = Paths.get(tempDir.toString(), "uploads", "avatars", filename);
        assertTrue(Files.exists(guardado));
        BufferedImage img = ImageIO.read(guardado.toFile());
        assertEquals(200, img.getWidth());
        assertEquals(200, img.getHeight());
    }

    @Test
    void modificarUsuario_conAvatarNuevo_reemplazaYBorraAnterior() throws Exception {
        Path anterior = Paths.get(tempDir.toString(), "uploads", "avatars", "viejo.png");
        Files.createDirectories(anterior.getParent());
        Files.write(anterior, pngBytes(10, 10));

        Usuario existente = usuario(7);
        existente.setAvatar("/uploads/avatars/viejo.png");
        when(usuarioRepositorio.findById(7)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.save(org.mockito.ArgumentMatchers.any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO dto = new UsuarioDTO(7, "Kael", "kael@test.com", "/images/AVATAR.png", RolUsuario.USER, new Date(), new Date());

        MockMultipartFile avatar = new MockMultipartFile("avatarFile", "nuevo.png", "image/png", pngBytes(500, 500));
        UsuarioDTO res = service.modificarUsuario(7, dto, avatar);

        assertTrue(res.getAvatar().startsWith("/uploads/avatars/"));
        assertTrue(!res.getAvatar().equals("/uploads/avatars/viejo.png"));
        assertTrue(Files.notExists(anterior));
    }

    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
