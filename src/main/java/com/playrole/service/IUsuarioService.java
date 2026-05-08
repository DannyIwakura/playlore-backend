package com.playrole.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.LoginDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;

public interface IUsuarioService {
	
	UsuarioDTO obtenerUsuario(Integer id);
	public Optional<UsuarioDTO> buscarPorNombre(String nombre);
	Page<UsuarioDTO> listarUsuarios(int pagina, int size);
    UsuarioDTO guardarUsuario(UsuarioCrearDTO usuarioCrearDTO, MultipartFile avatarFile);
    UsuarioDTO modificarUsuario(Integer id, UsuarioDTO usuarioDTO, MultipartFile avatarFile);
    void actualizarUltimaConexion(LoginDTO loginDTO);
    void eliminarUsuario(Integer id);
}
