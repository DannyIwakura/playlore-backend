package com.playrole.chat.dto;

import com.playrole.chat.model.MiembroCanal;
import java.util.Date;

public class MiembroCanalDTO {

    private Integer id;
    private Integer personajeId;
    private String personajeNombre;
    private String personajeAvatar;
    private String rol;
    private Date fechaUnion;
    private boolean online;

    public static MiembroCanalDTO fromEntity(MiembroCanal miembro) {
        MiembroCanalDTO dto = new MiembroCanalDTO();
        dto.setId(miembro.getIdMiembro());
        dto.setPersonajeId(miembro.getPersonaje().getIdPersonaje());
        dto.setPersonajeNombre(miembro.getPersonaje().getNombre());
        dto.setPersonajeAvatar(miembro.getPersonaje().getAvatar());
        dto.setRol(miembro.getRol().name());
        dto.setFechaUnion(miembro.getFechaUnion());
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPersonajeId() { return personajeId; }
    public void setPersonajeId(Integer personajeId) { this.personajeId = personajeId; }

    public String getPersonajeNombre() { return personajeNombre; }
    public void setPersonajeNombre(String personajeNombre) { this.personajeNombre = personajeNombre; }

    public String getPersonajeAvatar() { return personajeAvatar; }
    public void setPersonajeAvatar(String personajeAvatar) { this.personajeAvatar = personajeAvatar; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Date getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(Date fechaUnion) { this.fechaUnion = fechaUnion; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}
