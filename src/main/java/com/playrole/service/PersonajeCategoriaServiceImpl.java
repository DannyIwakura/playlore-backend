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
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;
import com.playrole.model.Usuario;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

@Service
public class PersonajeCategoriaServiceImpl implements IPersonajeCategoriaService {

 	@Autowired
    private PersonajeCategoriaRepositoryInterface personajeCategoriaRepository;

    @Autowired
    private PerfilPersonajeRepositoryInterface perfilPersonajeRepository;

    @Override
    public PersonajeCategoria guardar(PersonajeCategoria pc) {
        return personajeCategoriaRepository.save(pc);
    }

    @Override
    public Optional<PersonajeCategoriaDTO> obtenerPorId(Integer id) {
        return personajeCategoriaRepository.findById(id)
                .map(PersonajeCategoriaDTO::fromEntity);
    }
    
    @Override
    public Optional<PersonajeCategoria> obtenerEntidadPorId(Integer id) {
        return personajeCategoriaRepository.findById(id);
    }

    @Override
    public void eliminarPorId(Integer id) {
    	PersonajeCategoria pc = personajeCategoriaRepository.findById(id)
    	        .orElseThrow(() -> new ResponseStatusException(
    	            HttpStatus.NOT_FOUND,
    	            "PersonajeCategoria no encontrada"
    	        ));

    	    personajeCategoriaRepository.delete(pc);
    }

    @Override
    public List<PersonajeCategoriaDTO> obtenerTodos() {
        return personajeCategoriaRepository.findAll()
                .stream()
                .map(PersonajeCategoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonajeCategoriaDTO> obtenerPorPersonajeId(Integer idPersonaje) {
        // Verificamos que el personaje exista
        PerfilPersonaje personaje = perfilPersonajeRepository.findById(idPersonaje)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));

        return personajeCategoriaRepository.findByIdPersonajeIdPersonaje(idPersonaje)
                .stream()
                .map(PersonajeCategoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonajeCategoriaDTO> obtenerPorCategoriaId(Integer idCategoria) {
        return personajeCategoriaRepository.findByIdCategoriaIdCategoria(idCategoria)
                .stream()
                .map(PersonajeCategoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existePorCategoriaId(Integer idCategoria) {
        return personajeCategoriaRepository.existsByIdCategoriaIdCategoria(idCategoria);
    }
}
