package com.playrole.service;

import java.util.List;

import com.playrole.dto.PerfilPersonajeAdminDTO;
import com.playrole.dto.PerfilPersonajeDTO;

public interface IPerfilPersonajeService {

	PerfilPersonajeDTO obtenerPersonaje(Integer id);

    List<PerfilPersonajeDTO> listarPersonajes();
    
    List<PerfilPersonajeDTO> listarPersonajesPorUsuario(Integer userId);

    PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO personajeDTO);

    PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO personajeDTO);

    void eliminarPersonaje(Integer id);

	PerfilPersonajeDTO modificarPersonajeAdmin(Integer id, PerfilPersonajeAdminDTO dto);
}