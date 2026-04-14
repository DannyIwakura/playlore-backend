package com.playrole.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.PerfilPersonajeDTO;
import com.playrole.dto.PersonajeCategoriaDTO;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

import jakarta.transaction.Transactional;

@Service
public class PersonajeCategoriaServiceImpl implements IPersonajeCategoriaService {

    private final PersonajeCategoriaRepositoryInterface repository;
    private final ICategoriaService categoriaService;
    private final IPerfilPersonajeService personajeService;

    public PersonajeCategoriaServiceImpl(PersonajeCategoriaRepositoryInterface repository,
                                         ICategoriaService categoriaService,
                                         IPerfilPersonajeService personajeService) {
        this.repository = repository;
        this.categoriaService = categoriaService;
        this.personajeService = personajeService;
    }

    @Override
    public List<PersonajeCategoriaDTO> obtenerTodos() {
        return repository.findAll().stream()
                .map(PersonajeCategoriaDTO::fromEntity)
                .toList();
    }

    @Override
    public Optional<PersonajeCategoriaDTO> obtenerPorId(Integer id) {
        return repository.findById(id).map(PersonajeCategoriaDTO::fromEntity);
    }

    @Override
    public List<PersonajeCategoriaDTO> obtenerPorPersonajeId(Integer idPersonaje) {
        return repository.findByIdPersonajeIdPersonaje(idPersonaje).stream()
                .map(PersonajeCategoriaDTO::fromEntity)
                .toList();
    }
    
    @Override
    public boolean existePorCategoriaId(Integer idCategoria) {
        return repository.existsByIdCategoriaIdCategoria(idCategoria);
    }

    @Override
    @Transactional
    public PersonajeCategoriaDTO crear(PersonajeCategoriaDTO dto) {
        // Obtenemos entidades reales para persistir la relación
        Categoria cat = categoriaService.obtenerEntidadPorId(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        PerfilPersonaje per = personajeService.obtenerEntidadPorId(dto.getIdPersonaje())
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        // Usamos el método toEntity de tu DTO
        PersonajeCategoria nuevaRelacion = dto.toEntity(cat, per);
        
        // Si no viene fecha, la entidad en el DTO ya pone 'new Date()'
        return PersonajeCategoriaDTO.fromEntity(repository.save(nuevaRelacion));
    }

    @Override
    @Transactional
    public PersonajeCategoriaDTO actualizar(Integer id, PersonajeCategoriaDTO dto) {
        PersonajeCategoria existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relación no encontrada"));

        // 1. Actualizar Fecha
        if (dto.getFechaAdicion() != null) {
            existente.setFechaAdicion(dto.getFechaAdicion());
        }

        // 2. Actualizar Categoría si el ID enviado es distinto al actual
        if (dto.getIdCategoria() != null && !dto.getIdCategoria().equals(existente.getIdCategoria().getIdCategoria())) {
            Categoria nuevaCat = categoriaService.obtenerEntidadPorId(dto.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            existente.setIdCategoria(nuevaCat);
        }

        // 3. Actualizar Personaje si el ID enviado es distinto al actual
        if (dto.getIdPersonaje() != null && !dto.getIdPersonaje().equals(existente.getIdPersonaje().getIdPersonaje())) {
            PerfilPersonaje nuevoPer = personajeService.obtenerEntidadPorId(dto.getIdPersonaje())
                    .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));
            existente.setIdPersonaje(nuevoPer);
        }

        return PersonajeCategoriaDTO.fromEntity(repository.save(existente));
    }

    @Override
    @Transactional
    public void eliminarPorId(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("El ID no existe");
        }
        repository.deleteById(id);
    }

    @Override
    public List<PersonajeCategoriaDTO> obtenerPorCategoriaId(Integer idCategoria) {
        // 1. Buscamos en el repositorio usando el método que ya tienes definido
        List<PersonajeCategoria> entidades = repository.findByIdCategoriaIdCategoria(idCategoria);
        
        // 2. Convertimos la lista de entidades a una lista de DTOs
        return entidades.stream()
                .map(PersonajeCategoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }
}