package com.playrole.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.playrole.model.Categoria;
import com.playrole.repository.CategoríaRepositoryInterface;

public class CategoriaServiceImpl implements ICategoriaService {

	@Autowired
	private CategoríaRepositoryInterface categoriaRepository;
	
	
	@Override
	public List<Categoria> obtenerTodas() {
		return categoriaRepository.findAll();
	}

	@Override
	public Categoria obtenerPorId(Integer id) {
        Optional<Categoria> opt = categoriaRepository.findById(id);
        return opt.orElse(null);
    }

	@Override
	public Categoria guardar(Categoria categoria) {
		return categoriaRepository.save(categoria);
	}

	@Override
	public void eliminar(Integer id) {
		categoriaRepository.deleteById(id);
	}

	@Override
	public List<Categoria> obtenerPorTipo(String tipo) {
		return categoriaRepository.findDistinctByPersonajeCategoriaListTipo(tipo);
	}
}
