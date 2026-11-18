package com.playrole.chat.dto;

import com.playrole.chat.model.MensajePrivadoPersonaje;
import java.util.Date;

public class MensajePrivadoPersonajeDTO {

    private Integer id;
    private Integer emisorId;
    private Integer receptorId;
    private String emisorNombre;
    private String receptorNombre;
    private String emisorAvatar;
    private String receptorAvatar;
    private String contenido;
    private Date fechaEnvio;
    private boolean leido;
    private boolean esMio;
    private InvitacionCanalDTO invitacion;

    public static MensajePrivadoPersonajeDTO fromEntity(MensajePrivadoPersonaje msg, Integer currentPersonajeId) {
        MensajePrivadoPersonajeDTO dto = new MensajePrivadoPersonajeDTO();
        dto.setId(msg.getIdMensaje());
        dto.setEmisorId(msg.getEmisor().getIdPersonaje());
        dto.setReceptorId(msg.getReceptor().getIdPersonaje());
        dto.setEmisorNombre(msg.getEmisor().getNombre());
        dto.setReceptorNombre(msg.getReceptor().getNombre());
        dto.setEmisorAvatar(msg.getEmisor().getAvatar());
        dto.setReceptorAvatar(msg.getReceptor().getAvatar());
        dto.setContenido(msg.getContenido());
        dto.setFechaEnvio(msg.getFechaEnvio());
        dto.setLeido(msg.isLeido());
        dto.setEsMio(msg.getEmisor().getIdPersonaje().equals(currentPersonajeId));
        if (msg.getInvitacion() != null) {
            dto.setInvitacion(InvitacionCanalDTO.fromEntity(msg.getInvitacion()));
        }
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEmisorId() { return emisorId; }
    public void setEmisorId(Integer emisorId) { this.emisorId = emisorId; }

    public Integer getReceptorId() { return receptorId; }
    public void setReceptorId(Integer receptorId) { this.receptorId = receptorId; }

    public String getEmisorNombre() { return emisorNombre; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }

    public String getReceptorNombre() { return receptorNombre; }
    public void setReceptorNombre(String receptorNombre) { this.receptorNombre = receptorNombre; }

    public String getEmisorAvatar() { return emisorAvatar; }
    public void setEmisorAvatar(String emisorAvatar) { this.emisorAvatar = emisorAvatar; }

    public String getReceptorAvatar() { return receptorAvatar; }
    public void setReceptorAvatar(String receptorAvatar) { this.receptorAvatar = receptorAvatar; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public boolean isEsMio() { return esMio; }
    public void setEsMio(boolean esMio) { this.esMio = esMio; }

    public InvitacionCanalDTO getInvitacion() { return invitacion; }
    public void setInvitacion(InvitacionCanalDTO invitacion) { this.invitacion = invitacion; }
}
