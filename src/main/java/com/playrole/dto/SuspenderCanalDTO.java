package com.playrole.dto;

public class SuspenderCanalDTO {

    // 1H, 24H, 7D, 30D o INDEFINIDA
    private String duracion = "24H";

    private String motivo;

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
