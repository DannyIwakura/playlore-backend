package com.playrole.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;

public interface IUsuarioService {
	
	UsuarioDTO obtenerUsuario(Integer id);
    List<UsuarioDTO> listarUsuarios();
    UsuarioDTO guardarUsuario(UsuarioCrearDTO usuarioCrearDTO, MultipartFile avatarFile);
    UsuarioDTO modificarUsuario(Integer id, UsuarioDTO usuarioDTO);
    void eliminarUsuario(Integer id);
}
