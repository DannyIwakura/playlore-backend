package com.playrole.dto;

import java.util.Date;

import com.playrole.enums.RolUsuario;
import com.playrole.model.Usuario;

public class UsuarioCrearDTO {

	private String nombre;
    private String email;
    private String password;
    private RolUsuario rol;
    private Date fechaRegistro;

    public UsuarioCrearDTO() {}

    public UsuarioCrearDTO(String nombre, String email, String password, RolUsuario rol, Date fechaRegistro) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Usuario toEntity() {
        Usuario u = new Usuario();
        u.setNombre(this.nombre);
        u.setEmail(this.email);
        u.setPassword(this.password);
        u.setRol(this.rol);
        u.setFechaRegistro(this.fechaRegistro);
        return u;
    }
	
}
