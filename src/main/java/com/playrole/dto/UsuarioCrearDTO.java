package com.playrole.dto;

import com.playrole.model.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioCrearDTO {
	
	@NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
	@Pattern(
		    regexp = "^[a-zA-Z0-9_]+$",
		    message = "El nombre solo puede contener letras, números y guiones bajos"
		)
	private String nombre;
	@NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
	@Size(max = 255, message = "El email no puede superar 255 caracteres")
    private String email;
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Pattern(
    	    regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
    	    message = "La contraseña debe contener al menos una letra y un número"
    	)
    private String password;

    public UsuarioCrearDTO() {}

    public UsuarioCrearDTO(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Usuario toEntity() {
        Usuario u = new Usuario();
        u.setNombre(this.nombre);
        u.setEmail(this.email);
        u.setPassword(this.password);
        return u;
    }
	
}
