package com.playrole.service;

import java.util.List;

import com.playrole.dto.UsuarioDTO;

public interface IUsuarioService {
	
	UsuarioDTO obtenerUsuario(Integer id);
    List<UsuarioDTO> listarUsuarios();
    UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO);
    UsuarioDTO modificarUsuario(Integer id, UsuarioDTO usuarioDTO);
    void eliminarUsuario(Integer id);
}
