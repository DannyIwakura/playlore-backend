package com.playrole.chat.dto;

import com.playrole.chat.model.MensajeCanal;
import com.playrole.utils.HtmlUtils;
import java.util.Date;

public class MensajeCanalDTO {

    private Integer id;
    private Integer canalId;
    private Integer personajeId;
    private String personajeNombre;
    private String personajeAvatar;
    private String contenido;
    private Date fechaEnvio;
    private boolean editado;
    private boolean esMio;
    private boolean eliminado;
    private boolean eliminadoPorModerador;

    public static MensajeCanalDTO fromEntity(MensajeCanal mensaje, Integer currentPersonajeId) {
        MensajeCanalDTO dto = new MensajeCanalDTO();
        dto.setId(mensaje.getIdMensaje());
        dto.setCanalId(mensaje.getCanal().getIdCanal());
        dto.setPersonajeId(mensaje.getPersonaje().getIdPersonaje());
        dto.setPersonajeNombre(mensaje.getPersonaje().getNombre());
        dto.setPersonajeAvatar(mensaje.getPersonaje().getAvatar());
        dto.setContenido(mensaje.getContenido());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        dto.setEditado(mensaje.isEditado());
        dto.setEsMio(mensaje.getPersonaje().getIdPersonaje().equals(currentPersonajeId));
        dto.setEliminado(mensaje.isEliminado());
        dto.setEliminadoPorModerador(mensaje.isEliminadoPorModerador());
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCanalId() { return canalId; }
    public void setCanalId(Integer canalId) { this.canalId = canalId; }

    public Integer getPersonajeId() { return personajeId; }
    public void setPersonajeId(Integer personajeId) { this.personajeId = personajeId; }

    public String getPersonajeNombre() { return personajeNombre; }
    public void setPersonajeNombre(String personajeNombre) { this.personajeNombre = personajeNombre; }

    public String getPersonajeAvatar() { return personajeAvatar; }
    public void setPersonajeAvatar(String personajeAvatar) { this.personajeAvatar = personajeAvatar; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isEditado() { return editado; }
    public void setEditado(boolean editado) { this.editado = editado; }

    public boolean isEsMio() { return esMio; }
    public void setEsMio(boolean esMio) { this.esMio = esMio; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public boolean isEliminadoPorModerador() { return eliminadoPorModerador; }
    public void setEliminadoPorModerador(boolean eliminadoPorModerador) { this.eliminadoPorModerador = eliminadoPorModerador; }
}
