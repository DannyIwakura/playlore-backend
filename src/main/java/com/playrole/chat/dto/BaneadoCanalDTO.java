package com.playrole.chat.dto;

import com.playrole.chat.model.BaneoCanal;

import java.util.Date;

public class BaneadoCanalDTO {

    private Integer personajeId;
    private String personajeNombre;
    private String avatar;
    private Date fechaBaneo;
    private Date fechaExpiracion;
    private boolean activo;
    private String baneadoPorNombre;

    public static BaneadoCanalDTO fromEntity(BaneoCanal b) {
        BaneadoCanalDTO dto = new BaneadoCanalDTO();
        dto.setPersonajeId(b.getPersonaje().getIdPersonaje());
        dto.setPersonajeNombre(b.getPersonaje().getNombre());
        dto.setAvatar(b.getPersonaje().getAvatar());
        dto.setFechaBaneo(b.getFechaBaneo());
        dto.setFechaExpiracion(b.getFechaExpiracion());
        dto.setActivo(b.isActivo());
        if (b.getBaneadoPor() != null) {
            dto.setBaneadoPorNombre(b.getBaneadoPor().getNombre());
        }
        return dto;
    }

    public Integer getPersonajeId() { return personajeId; }
    public void setPersonajeId(Integer personajeId) { this.personajeId = personajeId; }

    public String getPersonajeNombre() { return personajeNombre; }
    public void setPersonajeNombre(String personajeNombre) { this.personajeNombre = personajeNombre; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Date getFechaBaneo() { return fechaBaneo; }
    public void setFechaBaneo(Date fechaBaneo) { this.fechaBaneo = fechaBaneo; }

    public Date getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(Date fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getBaneadoPorNombre() { return baneadoPorNombre; }
    public void setBaneadoPorNombre(String baneadoPorNombre) { this.baneadoPorNombre = baneadoPorNombre; }
}
