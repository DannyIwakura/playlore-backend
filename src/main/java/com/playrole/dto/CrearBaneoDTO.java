package com.playrole.dto;

public class CrearBaneoDTO {

    // "USUARIO" o "PERSONAJE"
    private String tipo;

    private Integer id;

    private String duracion = "PERMANENTE";

    private String motivo;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
