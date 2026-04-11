package com.playrole.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;

public interface IPerfilPersonajeService {

	PerfilPersonajeDTO obtenerPersonaje(Integer id);

    List<PerfilPersonajeDTO> listarPersonajes();
    
    List<PerfilPersonajeDTO> listarPersonajesPorUsuario(Integer userId);

    PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO personajeDTO, MultipartFile avatarFile);

    PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO personajeDTO, MultipartFile avatarFile);

    void eliminarPersonaje(Integer id);

	PerfilPersonajeDTO modificarPersonajeAdmin(Integer id, PerfilPersonajeAdminDTO dto);
}