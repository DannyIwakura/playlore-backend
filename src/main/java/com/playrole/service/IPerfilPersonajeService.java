package com.playrole.service;

import java.util.List;

import com.playrole.dto.PerfilPersonajeDTO;

public interface IPerfilPersonajeService {

	PerfilPersonajeDTO obtenerPersonaje(Integer id);

    List<PerfilPersonajeDTO> listarPersonajes();

    PerfilPersonajeDTO guardarPersonaje(PerfilPersonajeDTO personajeDTO);

    PerfilPersonajeDTO modificarPersonaje(Integer id, PerfilPersonajeDTO personajeDTO);

    void eliminarPersonaje(Integer id);
}