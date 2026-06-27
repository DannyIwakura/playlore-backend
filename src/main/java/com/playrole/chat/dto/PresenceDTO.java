package com.playrole.chat.dto;

public class PresenceDTO {

    private Integer personajeId;
    private String personajeNombre;
    private String personajeAvatar;
    private boolean online;
    private String status;

    public PresenceDTO() {}

    public PresenceDTO(Integer personajeId, boolean online) {
        this.personajeId = personajeId;
        this.online = online;
    }

    public Integer getPersonajeId() { return personajeId; }
    public void setPersonajeId(Integer personajeId) { this.personajeId = personajeId; }

    public String getPersonajeNombre() { return personajeNombre; }
    public void setPersonajeNombre(String personajeNombre) { this.personajeNombre = personajeNombre; }

    public String getPersonajeAvatar() { return personajeAvatar; }
    public void setPersonajeAvatar(String personajeAvatar) { this.personajeAvatar = personajeAvatar; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
