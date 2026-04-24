package com.playrole.dto;

public class AmigoDTO {
	
	private Integer id;
    private String nombre;
    private String avatar;

    public AmigoDTO() {}

    public AmigoDTO(Integer id, String nombre, String avatar) {
        this.id = id;
        this.nombre = nombre;
        this.avatar = avatar;
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
}
