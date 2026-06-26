package com.playrole.chat.dto;

import java.util.Date;

public class SesionPersonajeDTO {

    private Integer idSesion;
    private Integer personajeId;
    private String personajeNombre;
    private String personajeAvatar;
    private Integer usuarioId;
    private String tokenJwt;
    private Date fechaInicio;
    private Date ultimaActividad;

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }

    public Integer getPersonajeId() { return personajeId; }
    public void setPersonajeId(Integer personajeId) { this.personajeId = personajeId; }

    public String getPersonajeNombre() { return personajeNombre; }
    public void setPersonajeNombre(String personajeNombre) { this.personajeNombre = personajeNombre; }

    public String getPersonajeAvatar() { return personajeAvatar; }
    public void setPersonajeAvatar(String personajeAvatar) { this.personajeAvatar = personajeAvatar; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getTokenJwt() { return tokenJwt; }
    public void setTokenJwt(String tokenJwt) { this.tokenJwt = tokenJwt; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(Date ultimaActividad) { this.ultimaActividad = ultimaActividad; }
}
