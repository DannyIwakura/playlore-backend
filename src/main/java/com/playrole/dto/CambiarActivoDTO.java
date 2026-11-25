package com.playrole.dto;

import jakarta.validation.constraints.NotNull;

public class CambiarActivoDTO {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
