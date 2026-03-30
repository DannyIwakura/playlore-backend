package com.playrole.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;

public class PerfilPersonajeServiceImpl implements IPerfilPersonajeService {

	@Autowired
	private PerfilPersonajeRepositoryInterface perfilPersonajeRepository;
	
	@Override
	public List<PerfilPersonaje> obtenerPorUsuario(Integer idUsuario) {
		return perfilPersonajeRepository.findByUserId_UserId(idUsuario);
	}

	@Override
	public void crearPerfilPersonaje(PerfilPersonaje perfilPersonaje) {
		perfilPersonajeRepository.save(perfilPersonaje);
	}

	@Override
	public void modificarPerfilPersonaje(PerfilPersonaje perfilPersonaje) {
		perfilPersonajeRepository.save(perfilPersonaje);
	}

	@Override
	public void eliminarPerfilPersonaje(Integer id) {
		perfilPersonajeRepository.deleteById(id);
	}

}
