package com.playrole.service;

import java.util.List;

import com.playrole.model.Usuario;

public interface IUsuarioService {
	
	Usuario obtenerUsuario(Long id);
	List<Usuario> listarUsuarios();
	void guardarUsuario(Usuario usuario);
	void modificarUsuario(Usuario usuario);
	void eliminarrUsuario(Long id);
}
