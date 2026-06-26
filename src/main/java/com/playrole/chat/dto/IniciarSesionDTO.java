package com.playrole.chat.dto;

import jakarta.validation.constraints.NotNull;

public class IniciarSesionDTO {

    @NotNull(message = "El ID del personaje es obligatorio")
    private Integer personajeId;

    public Integer getPersonajeId() { return personajeId; }
    public void setPersonajeId(Integer personajeId) { this.personajeId = personajeId; }
}
