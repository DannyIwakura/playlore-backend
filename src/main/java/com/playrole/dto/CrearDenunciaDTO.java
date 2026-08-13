package com.playrole.dto;

import com.playrole.enums.TipoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CrearDenunciaDTO {

    @NotNull(message = "El tipo de denuncia es obligatorio")
    private TipoDenuncia tipo;

    @NotNull(message = "El elemento a denunciar es obligatorio")
    private Integer tipoId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String detalle;

    public TipoDenuncia getTipo() { return tipo; }
    public void setTipo(TipoDenuncia tipo) { this.tipo = tipo; }

    public Integer getTipoId() { return tipoId; }
    public void setTipoId(Integer tipoId) { this.tipoId = tipoId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
