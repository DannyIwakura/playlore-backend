package com.playrole.dto;

import com.playrole.model.Categoria;

public class CategoriaDTO {
	
	private Integer idCategoria;
    private String nombre;
    private String descripcion;

    public CategoriaDTO() {}

    public CategoriaDTO(Integer idCategoria, String nombre, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
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
    
    public static CategoriaDTO fromEntity(Categoria categoria) {
        return new CategoriaDTO(
            categoria.getIdCategoria(),
            categoria.getNombre(),
            categoria.getDescripcion()
        );
    }

    public Categoria toEntity() {
        Categoria c = new Categoria();
        c.setIdCategoria(this.idCategoria);
        c.setNombre(this.nombre);
        c.setDescripcion(this.descripcion);
        return c;
    }

}
