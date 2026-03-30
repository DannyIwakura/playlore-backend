package com.playrole.service;

import java.util.List;

import com.playrole.model.PerfilPersonaje;

public interface IPerfilPersonajeService {

	List<PerfilPersonaje> obtenerPorUsuario(Integer idUsuario);
	void crearPerfilPersonaje(PerfilPersonaje perfilPersonaje);
	void modificarPerfilPersonaje(PerfilPersonaje perfilPersonaje);
	void eliminarPerfilPersonaje(Integer id);
}
