package com.playrole.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {
	
	@NotBlank(message = "El nombre de usuario no puede estar vacío")
	private String nombre;
	@NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
