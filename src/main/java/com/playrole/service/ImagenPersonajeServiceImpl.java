package com.playrole.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.ImagenPersonajeDTO;
import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.model.ImagenPersonaje;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.ImagenPersonajeRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;

@Service
public class ImagenPersonajeServiceImpl implements IImagenPersonajeService {

    private static final int MAX_IMAGENES = 30;
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;

    private final ImagenPersonajeRepositoryInterface imagenRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;

    public ImagenPersonajeServiceImpl(
            ImagenPersonajeRepositoryInterface imagenRepository,
            PerfilPersonajeRepositoryInterface personajeRepository) {
        this.imagenRepository = imagenRepository;
        this.personajeRepository = personajeRepository;
    }

    @Override
    public List<ImagenPersonajeDTO> listarImagenes(Integer idPersonaje) {
        return imagenRepository
                .findByIdPersonaje_IdPersonajeOrderByOrdenAscFechaSubidaAsc(idPersonaje)
                .stream()
                .map(ImagenPersonajeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ImagenPersonajeDTO subirImagen(Integer idPersonaje, MultipartFile imagenFile, Integer userId) {
        PerfilPersonaje personaje = personajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));

        if (!personaje.getUserId().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para subir imágenes a este personaje");
        }

        long count = imagenRepository.countByIdPersonaje_IdPersonaje(idPersonaje);
        if (count >= MAX_IMAGENES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Límite de " + MAX_IMAGENES + " imágenes alcanzado");
        }

        if (imagenFile == null || imagenFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes seleccionar una imagen");
        }

        validarTipoImagen(imagenFile);
        validarDimensiones(imagenFile);

        try {
            Path uploadPath = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    "galeria");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID() + "_" + imagenFile.getOriginalFilename();
            Path destination = uploadPath.resolve(filename);
            imagenFile.transferTo(destination.toFile());

            String url = "/uploads/galeria/" + filename;

            ImagenPersonaje entity = new ImagenPersonaje();
            entity.setIdPersonaje(personaje);
            entity.setUrl(url);
            entity.setNombreOriginal(imagenFile.getOriginalFilename());
            entity.setFechaSubida(new Date());
            entity.setOrden((int) count);

            ImagenPersonaje guardado = imagenRepository.save(entity);
            return ImagenPersonajeDTO.fromEntity(guardado);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir la imagen", e);
        }
    }

    @Override
    public void eliminarImagen(Integer idImagen, Integer userId) {
        ImagenPersonaje imagen = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada"));

        if (!imagen.getIdPersonaje().getUserId().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para eliminar esta imagen");
        }

        try {
            Path filePath = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    "galeria",
                    imagen.getUrl().replace("/uploads/galeria/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Si no se puede eliminar el archivo, continuamos
        }

        imagenRepository.deleteByIdDirect(idImagen);
    }

    private void validarDimensiones(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("El archivo no es una imagen válida");
            }
            int width = image.getWidth();
            int height = image.getHeight();
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                throw new InvalidImageException(
                        "imagenFile",
                        "La imagen no puede superar " + MAX_WIDTH + "x" + MAX_HEIGHT + " píxeles");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen", e);
        }
    }

    private void validarTipoImagen(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("No se puede determinar el tipo de archivo");
        }
        boolean valido = contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp")
                || contentType.equals("image/gif");
        if (!valido) {
            throw new InvalidImageTypeException(
                    "imagenFile",
                    "Formato no permitido. Solo JPG, PNG, WEBP y GIF");
        }
    }
}
