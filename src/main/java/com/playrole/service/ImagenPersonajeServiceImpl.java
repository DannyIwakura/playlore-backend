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
import com.playrole.model.ImagenPersonaje;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.ImagenPersonajeRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.service.image.ImageProcessingService;
import com.playrole.service.image.ImageVariant;
import com.playrole.service.image.ProcessedImage;

@Service
public class ImagenPersonajeServiceImpl implements IImagenPersonajeService {

    private static final int MAX_IMAGENES = 30;

    private final ImagenPersonajeRepositoryInterface imagenRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final ImageProcessingService imageProcessingService;

    public ImagenPersonajeServiceImpl(
            ImagenPersonajeRepositoryInterface imagenRepository,
            PerfilPersonajeRepositoryInterface personajeRepository,
            ImageProcessingService imageProcessingService) {
        this.imagenRepository = imagenRepository;
        this.personajeRepository = personajeRepository;
        this.imageProcessingService = imageProcessingService;
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

        try {
            ProcessedImage processed = imageProcessingService.process(imagenFile, ImageVariant.CHARACTER_GALLERY, "imagenFile");
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "galeria");
            Files.createDirectories(uploadPath);

            String filename = UUID.randomUUID() + "." + processed.extension();
            Path destination = uploadPath.resolve(filename);
            Files.write(destination, processed.bytes());

            String url = "/uploads/galeria/" + filename;

            ImagenPersonaje entity = new ImagenPersonaje();
            entity.setIdPersonaje(personaje);
            entity.setUrl(url);
            entity.setNombreOriginal(imagenFile.getOriginalFilename());
            entity.setFechaSubida(new Date());
            entity.setOrden((int) count);

            ImagenPersonaje guardado = imagenRepository.save(entity);
            return ImagenPersonajeDTO.fromEntity(guardado);

        } catch (RuntimeException e) {
            throw e;
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
            org.slf4j.LoggerFactory.getLogger(ImagenPersonajeServiceImpl.class)
                    .warn("No se pudo eliminar archivo de imagen: {}", e.getMessage());
        }

        imagenRepository.deleteByIdDirect(idImagen);
    }


}
