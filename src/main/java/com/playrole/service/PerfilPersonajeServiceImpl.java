package com.playrole.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.owasp.html.HtmlSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.utils.HtmlUtils;

import jakarta.validation.Valid;

@Service
public class PerfilPersonajeServiceImpl implements IPerfilPersonajeService {

	@Autowired
    private PerfilPersonajeRepositoryInterface perfilPersonajeRepository;

    @Autowired
    private UsuarioRepositoryInterface usuarioRepository;
    
    private static final String DEFAULT_AVATAR =
    	    "http://localhost:8080/api/images/AVATAR.png";
    
    @Value("${app.upload.dir}")
    private String uploadsDir;

    @Override
    public List<PerfilPersonajeDTO> listarPersonajes() {
        return perfilPersonajeRepository.findAll()
                .stream()
                .map(PerfilPersonajeDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PerfilPersonajeDTO> listarPersonajesPorUsuario(Integer userId) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Obtener personajes del usuario
        List<PerfilPersonaje> personajes = perfilPersonajeRepository.findByUserId_UserId(userId);

        // Convertir a DTO
        return personajes.stream()
                .map(PerfilPersonajeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PerfilPersonajeDTO obtenerPersonaje(Integer id) {
        PerfilPersonaje personaje = perfilPersonajeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));
        return PerfilPersonajeDTO.fromEntity(personaje);
    }
    
    @Override
    public Optional<PerfilPersonaje> obtenerEntidadPorId(Integer id) {
        return perfilPersonajeRepository.findById(id);
    }

    @Override
    public PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO dto, MultipartFile avatarFile) {
        Usuario usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Sanitizar el trasfondo para evitar XSS
        if(dto.getTrasfondo() != null) {
        	dto.setTrasfondo(HtmlUtils.sanitize(dto.getTrasfondo()));
        }
        
        PerfilPersonaje personaje = dto.toEntity(usuario);
        
        if (avatarFile != null && !avatarFile.isEmpty()) {

            validarDimensiones(avatarFile);
            validarTipoImagen(avatarFile);

            try {
                Path uploadPath = Paths.get(
                        System.getProperty("user.dir"),
                        "uploads",
                        "personajes"
                );

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String filename = UUID.randomUUID() + "_" +
                        avatarFile.getOriginalFilename();

                Path destination = uploadPath.resolve(filename);

                avatarFile.transferTo(destination.toFile());

                String avatarUrl = "/uploads/personajes/" + filename;

                personaje.setAvatar(avatarUrl);

            } catch (Exception e) {
                throw new RuntimeException("Error al subir el avatar", e);
            }

        } else {
            personaje.setAvatar(DEFAULT_AVATAR);
        }
        
        PerfilPersonaje guardado = perfilPersonajeRepository.save(personaje);
        
        
        return PerfilPersonajeDTO.fromEntity(guardado);
    }

    @Override
    public PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO dto, MultipartFile avatarFile) {
    	PerfilPersonaje existente = perfilPersonajeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));
    	
    	if(dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }

        if(dto.getEdadPersonaje() != null && dto.getEdadPersonaje() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Edad inválida");
        }

        if(dto.getAvatar() != null && !dto.getAvatar().matches("^https?://.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar debe ser una URL válida");
        }

        // Sanitizar el trasfondo para evitar XSS
        if(dto.getTrasfondo() != null) {
        	dto.setTrasfondo(HtmlUtils.sanitize(dto.getTrasfondo()));
        }
    	
        // Actualizamos solo campos que el usuario puede modificar
        if (dto.getNombre() != null) existente.setNombre(dto.getNombre());
        if (dto.getEdadPersonaje() != null) existente.setEdadPersonaje(dto.getEdadPersonaje());
        if (dto.getTrasfondo() != null) existente.setTrasfondo(dto.getTrasfondo());
        if (dto.getRaza() != null) existente.setRaza(dto.getRaza());
        if (dto.getClase() != null) existente.setClase(dto.getClase());
        
        if (avatarFile != null && !avatarFile.isEmpty()) {

            validarDimensiones(avatarFile);
            validarTipoImagen(avatarFile);

            try {
                Path uploadPath = Paths.get(
                        System.getProperty("user.dir"),
                        "uploads",
                        "personajes"
                );

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String filename = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();

                Path destination = uploadPath.resolve(filename);

                avatarFile.transferTo(destination.toFile());

                String avatarUrl = "/uploads/personajes/" + filename;

                existente.setAvatar(avatarUrl);

            } catch (Exception e) {
                throw new RuntimeException("Error al subir el avatar", e);
            }
        }

        // Siempre actualizamos la fecha de modificación
        existente.setFechaModificacion(new Date());

        PerfilPersonaje actualizado = perfilPersonajeRepository.save(existente);
        return PerfilPersonajeDTO.fromEntity(actualizado);
    }
    
    @Override
    public PerfilPersonajeDTO modificarPersonajeAdmin(Integer id, PerfilPersonajeAdminDTO dto) {
    	//buscamos el personaje
    	PerfilPersonaje existente = perfilPersonajeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));
    	
    	//cambiamos solo el estado
        if (dto.getEstado() != null) {
            existente.setEstado(dto.getEstado());
            existente.setFechaModificacion(new Date());
            perfilPersonajeRepository.save(existente);
        }

        return PerfilPersonajeDTO.fromEntity(existente);
    }

    @Override
    public void eliminarPersonaje(Integer idPersonaje) {
    	perfilPersonajeRepository.deleteByIdDirect(idPersonaje);
        System.out.println("Personaje eliminado con ID: " + idPersonaje);
    }
    
  //metodo para validad dimensiones para el avatar
    private void validarDimensiones(MultipartFile file) {

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new IllegalArgumentException("El archivo no es una imagen válida");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width > 400 || height > 400) {
                throw new InvalidImageException(
                	    "avatarFile",
                	    "El avatar no puede superar 400x400 píxeles"
                	);
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
                      || contentType.equals("image/webp");

        if (!valido) {
            throw new InvalidImageTypeException(
            	    "avatarFile",
            	    "Formato no permitido. Solo JPG, PNG y WEBP"
            	);
        }
    }
}
