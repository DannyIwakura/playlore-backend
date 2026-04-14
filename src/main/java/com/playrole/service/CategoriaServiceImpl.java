package com.playrole.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.dto.CategoriaDTO;
import com.playrole.model.Categoria;
import com.playrole.repository.CategoríaRepositoryInterface;

@Service
public class CategoriaServiceImpl implements ICategoriaService {

	@Autowired
    private CategoríaRepositoryInterface categoriaRepository;

    @Override
    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaDTO obtenerPorId(Integer id) {
        return categoriaRepository.findById(id)
                .map(CategoriaDTO::fromEntity)
                .orElse(null);
    }
    
    @Override
    public Optional<Categoria> obtenerEntidadPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public CategoriaDTO guardar(CategoriaDTO categoriaDTO) {
        Categoria entidad = categoriaDTO.toEntity();
        Categoria guardada = categoriaRepository.save(entidad);
        return CategoriaDTO.fromEntity(guardada);
    }

    @Override
    public void eliminar(Integer id) {
        categoriaRepository.deleteById(id);
    }
}
