package com.playrole.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.playrole.dto.CategoriaDTO;
import com.playrole.model.Categoria;
import com.playrole.repository.CategoríaRepositoryInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Service
public class CategoriaServiceImpl implements ICategoriaService {

	@Autowired
    private CategoríaRepositoryInterface categoriaRepository;

	@Override
	public Page<CategoriaDTO> obtenerTodas(int page, int size) {

	    Page<Categoria> categoriasPage =
	            categoriaRepository.findAll(PageRequest.of(page, size));

	    List<CategoriaDTO> categoriasDTO =
	            categoriasPage.getContent()
	                    .stream()
	                    .map(CategoriaDTO::fromEntity)
	                    .collect(Collectors.toList());

	    return new PageImpl<>(
	            categoriasDTO,
	            categoriasPage.getPageable(),
	            categoriasPage.getTotalElements()
	    );
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
