package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

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

        PerfilPersonaje personaje = dto.toEntity(usuario);
        PerfilPersonaje guardado = perfilPersonajeRepository.save(personaje);
        return PerfilPersonajeDTO.fromEntity(guardado);
    }

    @Override
    public PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO dto) {
        PerfilPersonaje existente = perfilPersonajeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));

        // Actualizamos solo campos permitidos
        if (dto.getNombre() != null) existente.setNombre(dto.getNombre());
        if (dto.getEdadPersonaje() != null) existente.setEdadPersonaje(dto.getEdadPersonaje());
        if (dto.getAvatar() != null) existente.setAvatar(dto.getAvatar());
        if (dto.getEstado() != null) existente.setEstado(dto.getEstado());
        if (dto.getTrasfondo() != null) existente.setTrasfondo(dto.getTrasfondo());
        if (dto.getRaza() != null) existente.setRaza(dto.getRaza());
        if (dto.getClase() != null) existente.setClase(dto.getClase());
        existente.setFechaModificacion(new Date());

        PerfilPersonaje actualizado = perfilPersonajeRepository.save(existente);
        return PerfilPersonajeDTO.fromEntity(actualizado);
    }

    @Override
    public void eliminarPersonaje(Integer idPersonaje) {
    	perfilPersonajeRepository.deleteByIdDirect(idPersonaje);
        System.out.println("Personaje eliminado con ID: " + idPersonaje);
    }

}
