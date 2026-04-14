package com.playrole.service;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.model.PerfilPersonaje;

public interface IPerfilPersonajeService {

	PerfilPersonajeDTO obtenerPersonaje(Integer id);
	
	Optional<PerfilPersonaje> obtenerEntidadPorId(Integer id);

    List<PerfilPersonajeDTO> listarPersonajes();
    
    List<PerfilPersonajeDTO> listarPersonajesPorUsuario(Integer userId);

    PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO personajeDTO, MultipartFile avatarFile);

    PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO personajeDTO, MultipartFile avatarFile);

    void eliminarPersonaje(Integer id);

	PerfilPersonajeDTO modificarPersonajeAdmin(Integer id, PerfilPersonajeAdminDTO dto);
}