package com.playrole.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActualizarAnuncioDTO {

    @NotBlank(message = "El contenido del anuncio es obligatorio")
    @Size(max = 10000, message = "El contenido no puede superar 10000 caracteres")
    private String contenido;

    /** Opcional: la UI permite guardar sin categorías. */
    private List<Integer> categoriaIds = new ArrayList<>();

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public List<Integer> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(List<Integer> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }
}
