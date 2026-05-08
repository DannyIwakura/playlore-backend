package com.playrole.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;

public interface IPerfilPersonajeService {

	PerfilPersonajeDTO obtenerPersonaje(Integer id);
	
	Optional<PerfilPersonaje> obtenerEntidadPorId(Integer id);

    List<PerfilPersonajeDTO> listarPersonajes();
    
    Page<PerfilPersonajeDTO> listarPersonajesPorUsuario(Integer userId, int page, int size);
    
    Page<PerfilPersonajeDTO> buscarPersonajes(
    	    String nombre, String genero, String raza, String clase,
    	    Integer edadMin, Integer edadMax, Integer categoriaId,
    	    int page, int size);

    PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO personajeDTO, MultipartFile avatarFile);

    PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO personajeDTO, MultipartFile avatarFile);

    void eliminarPersonaje(Integer id);

	PerfilPersonajeDTO modificarPersonajeAdmin(Integer id, PerfilPersonajeAdminDTO dto);
}