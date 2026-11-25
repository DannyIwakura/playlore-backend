package com.playrole.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CrearAnuncioDTO {

    @NotBlank(message = "El contenido del anuncio es obligatorio")
    @Size(max = 10000, message = "El contenido no puede superar 10000 caracteres")
    private String contenido;

    /** Opcional: la UI permite publicar sin categorías. */
    private List<Integer> categoriaIds = new ArrayList<>();

    /** Opcional con JWT de usuario; se ignora con JWT de sesión de personaje. */
    private Integer personajeId;

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

    public Integer getPersonajeId() {
        return personajeId;
    }

    public void setPersonajeId(Integer personajeId) {
        this.personajeId = personajeId;
    }
}
