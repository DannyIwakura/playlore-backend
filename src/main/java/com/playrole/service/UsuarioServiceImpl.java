package com.playrole.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.UsuarioDTO;
import com.playrole.model.Usuario;
import com.playrole.repository.UsuarioRepositoryInterface;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private UsuarioRepositoryInterface usuarioRepositorio;

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepositorio.findAll()
                .stream()
                .map(UsuarioDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
                );
        return UsuarioDTO.fromEntity(usuario);
    }

    @Override
    public UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO) {
        // Convertir DTO a entidad y guardar
        Usuario usuarioGuardado = usuarioRepositorio.save(usuarioDTO.toEntity());
        return UsuarioDTO.fromEntity(usuarioGuardado);
    }

    @Override
    public UsuarioDTO modificarUsuario(Integer id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepositorio.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
                );

        // Actualizamos solo campos permitidos
        usuarioExistente.setNombre(usuarioDTO.getNombre());
        usuarioExistente.setEmail(usuarioDTO.getEmail());
        usuarioExistente.setRol(usuarioDTO.getRol());

        Usuario usuarioActualizado = usuarioRepositorio.save(usuarioExistente);
        return UsuarioDTO.fromEntity(usuarioActualizado);
    }

    @Override
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepositorio.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepositorio.deleteById(id);
    }
}