package com.playrole.dto;

import java.util.Date;

import com.playrole.enums.RolUsuario;
import com.playrole.model.Usuario;

public class UsuarioDTO {
	
	private Integer userId;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private Date fechaRegistro;
    private Date ultimaConexion;
    
    public UsuarioDTO(Integer userId, String nombre, String email, RolUsuario rol, Date fechaRegistro,
    		Date ultimaConexion) {
        this.userId = userId;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.ultimaConexion = ultimaConexion;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    
    public Date getUltimaConexion() { return ultimaConexion; }
	public void setUltimaConexion(Date ultimaConexion) {this.ultimaConexion = ultimaConexion; }

	public static UsuarioDTO fromEntity(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getUserId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol(),
            usuario.getFechaRegistro(),
            usuario.getUltimaConexion()
        );
    }

    public Usuario toEntity() {
        Usuario u = new Usuario();
        u.setUserId(this.userId);
        u.setNombre(this.nombre);
        u.setEmail(this.email);
        u.setRol(this.rol);
        u.setFechaRegistro(this.fechaRegistro);
        u.setUltimaConexion(this.ultimaConexion);
        return u;
    }
}
