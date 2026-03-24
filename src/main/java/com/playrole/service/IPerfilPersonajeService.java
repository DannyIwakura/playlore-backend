package com.playrole.service;

import java.util.List;

import com.playrole.model.PerfilPersonaje;

public interface IPerfilPersonajeService {

	List<PerfilPersonaje> obtenerPorUsuario(Long idUsuario);
	void crearPerfilPersonaje(PerfilPersonaje perfilPersonaje);
	void modificarPerfilPersonaje(PerfilPersonaje perfilPersonaje);
	void eliminarPerfilPersonaje(Long id);
}
