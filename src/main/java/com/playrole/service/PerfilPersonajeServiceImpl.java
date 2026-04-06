package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.owasp.html.HtmlSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.utils.HtmlUtils;

@Service
public class PerfilPersonajeServiceImpl implements IPerfilPersonajeService {

	@Autowired
    private PerfilPersonajeRepositoryInterface perfilPersonajeRepository;

    @Autowired
    private UsuarioRepositoryInterface usuarioRepository;

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
    public PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        
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
        
        PerfilPersonaje personaje = dto.toEntity(usuario);
        PerfilPersonaje guardado = perfilPersonajeRepository.save(personaje);
        return PerfilPersonajeDTO.fromEntity(guardado);
    }

    @Override
    public PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO dto) {
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
        if (dto.getAvatar() != null) existente.setAvatar(dto.getAvatar());
        if (dto.getTrasfondo() != null) existente.setTrasfondo(dto.getTrasfondo());
        if (dto.getRaza() != null) existente.setRaza(dto.getRaza());
        if (dto.getClase() != null) existente.setClase(dto.getClase());

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

}
