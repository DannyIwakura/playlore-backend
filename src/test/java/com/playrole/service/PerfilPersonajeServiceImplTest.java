package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.AnuncioCategoriaRepository;
import com.playrole.repository.AnuncioRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.service.image.ImageProcessingService;

class PerfilPersonajeServiceImplTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;
    private PerfilPersonajeServiceImpl service;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private UsuarioRepositoryInterface usuarioRepository;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        service = new PerfilPersonajeServiceImpl();
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        usuarioRepository = mock(UsuarioRepositoryInterface.class);
        ReflectionTestUtils.setField(service, "perfilPersonajeRepository", personajeRepository);
        ReflectionTestUtils.setField(service, "anuncioRepository", mock(AnuncioRepository.class));
        ReflectionTestUtils.setField(service, "anuncioCategoriaRepository", mock(AnuncioCategoriaRepository.class));
        ReflectionTestUtils.setField(service, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(service, "imageProcessingService", new ImageProcessingService());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void guardarPersonaje_conAvatarGrande_normalizaA400() throws Exception {
        Usuario usuario = new Usuario(7);
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));
        when(personajeRepository.save(any(PerfilPersonaje.class))).thenAnswer(inv -> {
            PerfilPersonaje p = inv.getArgument(0);
            p.setIdPersonaje(1);
            p.setFechaCreacion(new Date());
            return p;
        });

        PerfilPersonajeDTO dto = new PerfilPersonajeDTO();
        dto.setNombre("Kael");
        dto.setUserId(7);

        MockMultipartFile avatar = new MockMultipartFile("avatarFile", "foto.png", "image/png", pngBytes(900, 500));

        PerfilPersonajeDTO res = service.guardarPersonaje(dto, avatar);

        assertTrue(res.getAvatar().startsWith("/uploads/personajes/"));
        String filename = res.getAvatar().replace("/uploads/personajes/", "");
        Path guardado = Paths.get(tempDir.toString(), "uploads", "personajes", filename);
        BufferedImage img = ImageIO.read(guardado.toFile());
        assertEquals(400, img.getWidth());
        assertEquals(400, img.getHeight());
    }

    @Test
    void modificarPersonaje_conAvatarNuevo_reemplazaYBorraAnterior() throws Exception {
        Path anterior = Paths.get(tempDir.toString(), "uploads", "personajes", "viejo.png");
        Files.createDirectories(anterior.getParent());
        Files.write(anterior, pngBytes(10, 10));

        PerfilPersonaje existente = new PerfilPersonaje();
        existente.setIdPersonaje(5);
        existente.setNombre("Kael");
        existente.setAvatar("/uploads/personajes/viejo.png");
        existente.setUserId(new Usuario(7));
        existente.setFechaCreacion(new Date());

        when(personajeRepository.findById(5)).thenReturn(Optional.of(existente));
        when(personajeRepository.save(any(PerfilPersonaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PerfilPersonajeDTO dto = new PerfilPersonajeDTO();
        dto.setNombre("Kael");
        dto.setUserId(7);

        MockMultipartFile avatar = new MockMultipartFile("avatarFile", "nuevo.png", "image/png", pngBytes(800, 800));
        PerfilPersonajeDTO res = service.modificarPersonaje(5, dto, avatar);

        assertTrue(res.getAvatar().startsWith("/uploads/personajes/"));
        assertTrue(!res.getAvatar().equals("/uploads/personajes/viejo.png"));
        assertTrue(Files.notExists(anterior));
    }

    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
