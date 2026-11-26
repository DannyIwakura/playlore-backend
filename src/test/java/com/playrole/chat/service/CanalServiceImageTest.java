package com.playrole.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.playrole.chat.dto.CanalDTO;
import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.TipoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.repository.BaneoCanalRepository;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.BadRequestException;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.service.AuditoriaModeracionService;
import com.playrole.service.image.ImageProcessingService;

class CanalServiceImageTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;
    private CanalRepository canalRepository;
    private MiembroCanalRepository miembroRepository;
    private CanalPermissionService permissionService;
    private ImageProcessingService imageProcessingService;
    private CanalService service;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        canalRepository = mock(CanalRepository.class);
        miembroRepository = mock(MiembroCanalRepository.class);
        permissionService = mock(CanalPermissionService.class);
        imageProcessingService = new ImageProcessingService();

        service = new CanalService(
                canalRepository,
                miembroRepository,
                mock(BaneoCanalRepository.class),
                mock(MensajeCanalRepository.class),
                mock(PerfilPersonajeRepositoryInterface.class),
                permissionService,
                mock(PresenceService.class),
                mock(SimpMessagingTemplate.class),
                mock(PasswordEncoder.class),
                mock(AuditoriaModeracionService.class),
                mock(InvitacionCanalService.class),
                imageProcessingService
        );

        when(canalRepository.save(any(Canal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void actualizarImagen_normalizaGuardaYReemplazaAnterior() throws Exception {
        Path anterior = Paths.get(tempDir.toString(), "uploads", "canales", "viejo.png");
        Files.createDirectories(anterior.getParent());
        Files.write(anterior, pngBytes(10, 10));

        Canal canal = new Canal();
        canal.setIdCanal(1);
        canal.setNombre("Taberna");
        canal.setTipo(TipoCanal.USER_CREATED);
        canal.setImagenUrl("/uploads/canales/viejo.png");
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));
        when(miembroRepository.countByCanalId(1)).thenReturn(2L);

        MockMultipartFile file = new MockMultipartFile("imagenFile", "foto.png", "image/png", pngBytes(800, 300));
        CanalDTO dto = service.actualizarImagen(1, file, 10);

        verify(permissionService).verificarPermiso(1, 10, PermisoCanal.GESTIONAR_MIEMBROS);
        assertNotNull(dto.getImagenUrl());
        assertTrue(dto.getImagenUrl().startsWith("/uploads/canales/"));
        assertTrue(Files.notExists(anterior));

        String filename = dto.getImagenUrl().replace("/uploads/canales/", "");
        BufferedImage img = ImageIO.read(Paths.get(tempDir.toString(), "uploads", "canales", filename).toFile());
        assertEquals(400, img.getWidth());
        assertEquals(400, img.getHeight());
    }

    @Test
    void actualizarImagen_sinArchivo_lanzaBadRequest() {
        Canal canal = new Canal();
        canal.setIdCanal(1);
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));

        assertThrows(BadRequestException.class, () -> service.actualizarImagen(1, null, 10));
    }

    @Test
    void eliminarImagen_borraArchivoYLimpiaUrl() throws Exception {
        Path anterior = Paths.get(tempDir.toString(), "uploads", "canales", "viejo.png");
        Files.createDirectories(anterior.getParent());
        Files.write(anterior, pngBytes(10, 10));

        Canal canal = new Canal();
        canal.setIdCanal(1);
        canal.setNombre("Taberna");
        canal.setTipo(TipoCanal.USER_CREATED);
        canal.setImagenUrl("/uploads/canales/viejo.png");
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));
        when(miembroRepository.countByCanalId(1)).thenReturn(2L);

        CanalDTO dto = service.eliminarImagen(1, 10);

        verify(permissionService).verificarPermiso(1, 10, PermisoCanal.GESTIONAR_MIEMBROS);
        assertNull(dto.getImagenUrl());
        assertTrue(Files.notExists(anterior));
    }

    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
