package com.playrole.dto;

import java.util.Date;

public class AmigoDTO {
	
	private Integer id;
    private String nombre;
    private String avatar;
    private Date ultimaConexion;

    public AmigoDTO() {}

    public AmigoDTO(Integer id, String nombre, String avatar, Date ultimaConexion) {
        this.id = id;
        this.nombre = nombre;
        this.avatar = avatar;
        this.ultimaConexion = ultimaConexion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Date getUltimaConexion() {
		return ultimaConexion;
	}

	public void setUltimaConexion(Date ultimaConexion) {
		this.ultimaConexion = ultimaConexion;
	}
}
