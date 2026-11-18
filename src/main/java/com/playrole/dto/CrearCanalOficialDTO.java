package com.playrole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un canal oficial desde el panel de administracion.
 * El personaje responsable queda como OWNER del canal; la imagen es opcional.
 */
public class CrearCanalOficialDTO {

    @NotBlank(message = "El nombre del canal es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "Debes elegir un personaje responsable")
    private Integer personajeResponsableId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getPersonajeResponsableId() { return personajeResponsableId; }
    public void setPersonajeResponsableId(Integer personajeResponsableId) { this.personajeResponsableId = personajeResponsableId; }
}
