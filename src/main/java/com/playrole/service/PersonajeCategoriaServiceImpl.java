package com.playrole.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.playrole.model.PersonajeCategoria;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;

public class PersonajeCategoriaServiceImpl implements IPersonajeCategoriaService {

	@Autowired
	private PersonajeCategoriaRepositoryInterface personajeCategoriaRepository;
	
	@Override
	public PersonajeCategoria guardar(PersonajeCategoria pc) {
		return personajeCategoriaRepository.save(pc);
	}

	@Override
	public Optional<PersonajeCategoria> obtenerPorId(Integer id) {
		return personajeCategoriaRepository.findById(id);
	}

	@Override
	public void eliminarPorId(Integer id) {
		personajeCategoriaRepository.deleteById(id);
	}

	@Override
	public List<PersonajeCategoria> obtenerTodos() {
		return personajeCategoriaRepository.findAll();
	}

	@Override
	public List<PersonajeCategoria> obtenerPorPersonajeId(Integer idPersonaje) {
		return personajeCategoriaRepository.findByIdPersonajeIdPersonaje(idPersonaje);
	}

	@Override
	public List<PersonajeCategoria> obtenerPorCategoriaId(Integer idCategoria) {
		return personajeCategoriaRepository.findByIdCategoriaIdCategoria(idCategoria);
	}
}
