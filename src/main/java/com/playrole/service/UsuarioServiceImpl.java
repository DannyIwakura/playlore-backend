package com.playrole.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.enums.RolUsuario;
import com.playrole.model.Usuario;
import com.playrole.repository.UsuarioRepositoryInterface;

import jakarta.validation.Valid;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private UsuarioRepositoryInterface usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public UsuarioDTO guardarUsuario(@Valid UsuarioCrearDTO usuarioCrearDTO) {
    	//comprobar si ya existe un usuario con ese email
        boolean existe = usuarioRepositorio.existsByEmail(usuarioCrearDTO.getEmail());
        if (existe) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }

        //convertir DTO a entidad
        Usuario usuario = usuarioCrearDTO.toEntity();

        //cifrar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuarioCrearDTO.getPassword()));

        //asignar rol y fecha de registro
        usuario.setRol(RolUsuario.USER);
        usuario.setFechaRegistro(new Date());
        usuario.setUltimaConexion(new Date());

        //guardar en base de datos
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);

        //devolver DTO
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