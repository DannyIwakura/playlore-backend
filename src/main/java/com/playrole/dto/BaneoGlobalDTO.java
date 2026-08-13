package com.playrole.dto;

import com.playrole.model.BaneoGlobal;

import java.util.Date;

public class BaneoGlobalDTO {

    private Integer idBaneoGlobal;
    private String tipo;
    private Integer idObjetivo;
    private String nombreObjetivo;
    private String motivo;
    private String baneadoPorNombre;
    private Date fechaBaneo;
    private Date fechaExpiracion;
    private boolean activo;

    public static BaneoGlobalDTO fromEntity(BaneoGlobal b) {
        BaneoGlobalDTO dto = new BaneoGlobalDTO();
        dto.setIdBaneoGlobal(b.getIdBaneoGlobal());
        dto.setMotivo(b.getMotivo());
        dto.setFechaBaneo(b.getFechaBaneo());
        dto.setFechaExpiracion(b.getFechaExpiracion());
        dto.setActivo(b.isActivo());
        if (b.getBaneadoPor() != null) {
            dto.setBaneadoPorNombre(b.getBaneadoPor().getNombre());
        }
        if (b.getUsuario() != null) {
            dto.setTipo("USUARIO");
            dto.setIdObjetivo(b.getUsuario().getUserId());
            dto.setNombreObjetivo(b.getUsuario().getNombre());
        } else if (b.getPersonaje() != null) {
            dto.setTipo("PERSONAJE");
            dto.setIdObjetivo(b.getPersonaje().getIdPersonaje());
            dto.setNombreObjetivo(b.getPersonaje().getNombre());
        }
        return dto;
    }

    public Integer getIdBaneoGlobal() { return idBaneoGlobal; }
    public void setIdBaneoGlobal(Integer idBaneoGlobal) { this.idBaneoGlobal = idBaneoGlobal; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getIdObjetivo() { return idObjetivo; }
    public void setIdObjetivo(Integer idObjetivo) { this.idObjetivo = idObjetivo; }

    public String getNombreObjetivo() { return nombreObjetivo; }
    public void setNombreObjetivo(String nombreObjetivo) { this.nombreObjetivo = nombreObjetivo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getBaneadoPorNombre() { return baneadoPorNombre; }
    public void setBaneadoPorNombre(String baneadoPorNombre) { this.baneadoPorNombre = baneadoPorNombre; }

    public Date getFechaBaneo() { return fechaBaneo; }
    public void setFechaBaneo(Date fechaBaneo) { this.fechaBaneo = fechaBaneo; }

    public Date getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(Date fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
