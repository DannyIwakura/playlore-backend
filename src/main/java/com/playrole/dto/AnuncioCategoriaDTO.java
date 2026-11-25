package com.playrole.dto;

import com.playrole.enums.TipoCategoria;
import com.playrole.model.AnuncioCategoria;

/** Categoría de un anuncio: id del catálogo, nombre y tipo. */
public class AnuncioCategoriaDTO {

    private Integer idCategoria;
    private String nombre;
    private TipoCategoria tipo;

    public AnuncioCategoriaDTO() {
    }

    public AnuncioCategoriaDTO(Integer idCategoria, String nombre, TipoCategoria tipo) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
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

    public TipoCategoria getTipo() {
        return tipo;
    }

    public void setTipo(TipoCategoria tipo) {
        this.tipo = tipo;
    }

    public static AnuncioCategoriaDTO fromEntity(AnuncioCategoria relacion) {
        return new AnuncioCategoriaDTO(
                relacion.getIdCategoria() != null ? relacion.getIdCategoria().getIdCategoria() : null,
                relacion.getIdCategoria() != null ? relacion.getIdCategoria().getNombre() : null,
                relacion.getIdCategoria() != null ? relacion.getIdCategoria().getTipo() : null);
    }
}
