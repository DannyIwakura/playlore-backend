package com.playrole.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.owasp.html.HtmlSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import com.playrole.repository.AnuncioCategoriaRepository;
import com.playrole.repository.AnuncioRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.specification.PerfilPersonajeSpec;
import com.playrole.service.image.ImageProcessingService;
import com.playrole.service.image.ImageVariant;
import com.playrole.service.image.ProcessedImage;
import com.playrole.utils.HtmlUtils;

import jakarta.validation.Valid;

@Service
public class PerfilPersonajeServiceImpl implements IPerfilPersonajeService {

	@Autowired
    private PerfilPersonajeRepositoryInterface perfilPersonajeRepository;

    @Autowired
    private AnuncioRepository anuncioRepository;

    @Autowired
    private AnuncioCategoriaRepository anuncioCategoriaRepository;

    @Autowired
    private UsuarioRepositoryInterface usuarioRepository;

    @Autowired
    private ImageProcessingService imageProcessingService;
    
    private static final String DEFAULT_AVATAR =
    	    com.playrole.utils.AppConstants.AVATAR_POR_DEFECTO;
    
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
    public Page<PerfilPersonajeDTO> listarPersonajesPorUsuario(Integer userId, int page, int size) {
        usuarioRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Pageable pageable = PageRequest.of(page, size);

        return perfilPersonajeRepository.findByUserId_UserId(userId, pageable)
                .map(PerfilPersonajeDTO::fromEntity);
    }
    
    @Override
    public Page<PerfilPersonajeDTO> buscarPersonajes(
            String nombre, String genero, String raza, String clase,
            Integer edadMin, Integer edadMax, Integer categoriaId,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Specification<PerfilPersonaje> spec = PerfilPersonajeSpec.filtrar(
            nombre, genero, raza, clase, edadMin, edadMax, categoriaId);
        
        return perfilPersonajeRepository.findAll(spec, pageable)
                .map(PerfilPersonajeDTO::fromEntity);
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
            ProcessedImage processed = imageProcessingService.process(avatarFile, ImageVariant.CHARACTER_AVATAR, "avatarFile");
            personaje.setAvatar(guardarImagenProcesada(processed, "personajes"));
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
            String anterior = existente.getAvatar();
            ProcessedImage processed = imageProcessingService.process(avatarFile, ImageVariant.CHARACTER_AVATAR, "avatarFile");
            existente.setAvatar(guardarImagenProcesada(processed, "personajes"));
            eliminarAvatarAnterior(anterior);
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
        // Los anuncios (y sus categorías) se borran antes que el personaje.
        anuncioCategoriaRepository.deleteByPersonajeId(idPersonaje);
        anuncioRepository.deleteByPersonajeId(idPersonaje);
    	perfilPersonajeRepository.deleteByIdDirect(idPersonaje);
    }
    
    private String guardarImagenProcesada(ProcessedImage processed, String carpeta) {
        try {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", carpeta);
            Files.createDirectories(uploadPath);
            String filename = UUID.randomUUID() + "." + processed.extension();
            Path destination = uploadPath.resolve(filename);
            Files.write(destination, processed.bytes());
            return "/uploads/" + carpeta + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    private void eliminarAvatarAnterior(String anterior) {
        if (anterior == null || anterior.equals(DEFAULT_AVATAR) || !anterior.startsWith("/uploads/")) {
            return;
        }
        try {
            String relative = anterior.substring("/uploads/".length());
            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", relative);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // No bloqueamos la actualización si falla la limpieza del archivo anterior.
        }
    }
}
