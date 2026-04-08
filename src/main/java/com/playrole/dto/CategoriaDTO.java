package com.playrole.dto;

import com.playrole.enums.TipoCategoria;
import com.playrole.model.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoriaDTO {
	
	private Integer idCategoria;
	@NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;
	@Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;
	@NotNull(message = "El tipo es obligatorio")
	private TipoCategoria tipo;

    public CategoriaDTO() {}

    public CategoriaDTO(Integer idCategoria, String nombre, String descripcion,
    		TipoCategoria tipo) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }

	public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public TipoCategoria getTipo() {
		return tipo;
	}

	public void setTipo(TipoCategoria tipo) {
		this.tipo = tipo;
	}

    
    public static CategoriaDTO fromEntity(Categoria categoria) {
        return new CategoriaDTO(
            categoria.getIdCategoria(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            categoria.getTipo()
        );
    }

    public Categoria toEntity() {
        Categoria c = new Categoria();
        c.setIdCategoria(this.idCategoria);
        c.setNombre(this.nombre);
        c.setDescripcion(this.descripcion);
        c.setTipo(this.tipo);
        return c;
    }
}
