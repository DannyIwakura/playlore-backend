package com.playrole.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.playrole.model.Usuario;
import com.playrole.repository.UsuarioRepositoryInterface;

public class UsuarioServiceImpl implements IUsuarioService {
	
	@Autowired
	private UsuarioRepositoryInterface usuarioRepositorio;

	@Override
	public List<Usuario> listarUsuarios() {
		return this.usuarioRepositorio.findAll();
	}

	@Override
	public Usuario obtenerUsuario(Long id) {
		return usuarioRepositorio.findById(id)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	}

	@Override
	public void guardarUsuario(Usuario usuario) {
		usuarioRepositorio.save(usuario);
	}

	@Override
	public void modificarUsuario(Usuario usuario) {
		usuarioRepositorio.save(usuario);
	}

	@Override
	public void eliminarrUsuario(Long id) {
		usuarioRepositorio.deleteById(id);
	}
}
