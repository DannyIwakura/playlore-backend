package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.model.ImagenPersonaje;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.ImagenPersonajeRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;

class ImagenPersonajeServiceImplTest {

    @TempDir
    Path tempDir;

    private ImagenPersonajeRepositoryInterface imagenRepository;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private ImagenPersonajeServiceImpl service;
    private String userDirOriginal;

    @BeforeEach
    void setUp() {
        imagenRepository = mock(ImagenPersonajeRepositoryInterface.class);
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        service = new ImagenPersonajeServiceImpl(imagenRepository, personajeRepository);
        userDirOriginal = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", userDirOriginal);
    }

    private PerfilPersonaje personajeDelUsuario(Integer usuarioId) {
        PerfilPersonaje p = new PerfilPersonaje();
        p.setIdPersonaje(5);
        p.setNombre("Kael");
        p.setUserId(new Usuario(usuarioId));
        return p;
    }

    private byte[] pngBytes(int width, int height) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private MockMultipartFile png(int width, int height) throws Exception {
        return new MockMultipartFile("imagenFile", "foto.png", "image/png", pngBytes(width, height));
    }

    @Test
    void subirImagen_personajeNoEncontrado_devuelve404() {
        when(personajeRepository.findById(5)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> service.subirImagen(5, png(10, 10), 1));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void subirImagen_noEsElDueno_devuelve403() throws Exception {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));

        var ex = assertThrows(ResponseStatusException.class, () -> service.subirImagen(5, png(10, 10), 999));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void subirImagen_limiteAlcanzado_devuelve400() throws Exception {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));
        when(imagenRepository.countByIdPersonaje_IdPersonaje(5)).thenReturn(30L);

        var ex = assertThrows(ResponseStatusException.class, () -> service.subirImagen(5, png(10, 10), 1));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void subirImagen_archivoVacio_devuelve400() {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));
        when(imagenRepository.countByIdPersonaje_IdPersonaje(5)).thenReturn(0L);
        MockMultipartFile vacio = new MockMultipartFile("imagenFile", "foto.png", "image/png", new byte[0]);

        var ex = assertThrows(ResponseStatusException.class, () -> service.subirImagen(5, vacio, 1));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void subirImagen_contentTypeNoPermitido_lanzaInvalidImageType() throws Exception {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));
        when(imagenRepository.countByIdPersonaje_IdPersonaje(5)).thenReturn(0L);
        MockMultipartFile falso = new MockMultipartFile("imagenFile", "foto.txt", "text/plain", pngBytes(10, 10));

        assertThrows(InvalidImageTypeException.class, () -> service.subirImagen(5, falso, 1));
    }

    @Test
    void subirImagen_magicBytesNoCoinciden_lanzaInvalidImageType() throws Exception {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));
        when(imagenRepository.countByIdPersonaje_IdPersonaje(5)).thenReturn(0L);
        MockMultipartFile suplantado = new MockMultipartFile(
                "imagenFile", "foto.png", "image/png", "no soy una imagen real".getBytes());

        assertThrows(InvalidImageTypeException.class, () -> service.subirImagen(5, suplantado, 1));
    }

    @Test
    void subirImagen_dimensionesExcesivas_lanzaInvalidImage() throws Exception {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));
        when(imagenRepository.countByIdPersonaje_IdPersonaje(5)).thenReturn(0L);

        assertThrows(InvalidImageException.class, () -> service.subirImagen(5, png(2000, 100), 1));
    }

    @Test
    void subirImagen_ok_guardaYDevuelveDto() throws Exception {
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personajeDelUsuario(1)));
        when(imagenRepository.countByIdPersonaje_IdPersonaje(5)).thenReturn(0L);
        when(imagenRepository.save(any(ImagenPersonaje.class)))
                .thenAnswer(inv -> {
                    ImagenPersonaje img = inv.getArgument(0);
                    img.setIdImagen(1);
                    return img;
                });

        var res = service.subirImagen(5, png(100, 50), 1);

        assertEquals(1, res.getIdImagen());
        assertEquals("foto.png", res.getNombreOriginal());
        assertTrue(res.getUrl().startsWith("/uploads/galeria/"));
        assertEquals(0, res.getOrden());
        verify(imagenRepository).save(any(ImagenPersonaje.class));
    }

    @Test
    void eliminarImagen_noEncontrada_devuelve404() {
        when(imagenRepository.findById(1)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> service.eliminarImagen(1, 1));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void eliminarImagen_noEsElDueno_devuelve403() {
        ImagenPersonaje imagen = new ImagenPersonaje();
        imagen.setIdImagen(1);
        imagen.setIdPersonaje(personajeDelUsuario(1));
        when(imagenRepository.findById(1)).thenReturn(Optional.of(imagen));

        var ex = assertThrows(ResponseStatusException.class, () -> service.eliminarImagen(1, 999));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void eliminarImagen_ok_borraArchivoYRegistro() throws Exception {
        Path galeria = Paths.get(tempDir.toString(), "uploads", "galeria");
        Files.createDirectories(galeria);
        Path archivo = galeria.resolve("foto.jpg");
        Files.write(archivo, new byte[]{1, 2, 3});

        ImagenPersonaje imagen = new ImagenPersonaje();
        imagen.setIdImagen(1);
        imagen.setIdPersonaje(personajeDelUsuario(1));
        imagen.setUrl("/uploads/galeria/foto.jpg");
        when(imagenRepository.findById(1)).thenReturn(Optional.of(imagen));

        service.eliminarImagen(1, 1);

        assertFalse(Files.exists(archivo));
        verify(imagenRepository).deleteByIdDirect(1);
    }

    @Test
    void eliminarImagen_archivoInexistente_noLanza() {
        ImagenPersonaje imagen = new ImagenPersonaje();
        imagen.setIdImagen(1);
        imagen.setIdPersonaje(personajeDelUsuario(1));
        imagen.setUrl("/uploads/galeria/no-existe.jpg");
        when(imagenRepository.findById(1)).thenReturn(Optional.of(imagen));

        service.eliminarImagen(1, 1);

        verify(imagenRepository).deleteByIdDirect(1);
    }
}
