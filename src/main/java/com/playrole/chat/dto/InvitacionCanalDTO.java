package com.playrole.chat.dto;

import com.playrole.chat.model.InvitacionCanal;
import java.util.Date;

public class InvitacionCanalDTO {

    private Integer id;
    private Integer canalId;
    private String canalNombre;
    private String canalDescripcion;
    private String canalImagenUrl;
    private Integer invitadorId;
    private String invitadorNombre;
    private String invitadorAvatar;
    private Integer invitadoId;
    private String invitadoNombre;
    private String estado;
    private Date fechaCreacion;
    private Date fechaRespuesta;

    public static InvitacionCanalDTO fromEntity(InvitacionCanal invitacion) {
        InvitacionCanalDTO dto = new InvitacionCanalDTO();
        dto.setId(invitacion.getIdInvitacion());
        if (invitacion.getCanal() != null) {
            dto.setCanalId(invitacion.getCanal().getIdCanal());
            dto.setCanalNombre(invitacion.getCanal().getNombre());
            dto.setCanalDescripcion(invitacion.getCanal().getDescripcion());
            dto.setCanalImagenUrl(invitacion.getCanal().getImagenUrl());
        }
        if (invitacion.getInvitador() != null) {
            dto.setInvitadorId(invitacion.getInvitador().getIdPersonaje());
            dto.setInvitadorNombre(invitacion.getInvitador().getNombre());
            dto.setInvitadorAvatar(invitacion.getInvitador().getAvatar());
        }
        if (invitacion.getInvitado() != null) {
            dto.setInvitadoId(invitacion.getInvitado().getIdPersonaje());
            dto.setInvitadoNombre(invitacion.getInvitado().getNombre());
        }
        dto.setEstado(invitacion.getEstado().name());
        dto.setFechaCreacion(invitacion.getFechaCreacion());
        dto.setFechaRespuesta(invitacion.getFechaRespuesta());
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCanalId() { return canalId; }
    public void setCanalId(Integer canalId) { this.canalId = canalId; }

    public String getCanalNombre() { return canalNombre; }
    public void setCanalNombre(String canalNombre) { this.canalNombre = canalNombre; }

    public String getCanalDescripcion() { return canalDescripcion; }
    public void setCanalDescripcion(String canalDescripcion) { this.canalDescripcion = canalDescripcion; }

    public String getCanalImagenUrl() { return canalImagenUrl; }
    public void setCanalImagenUrl(String canalImagenUrl) { this.canalImagenUrl = canalImagenUrl; }

    public Integer getInvitadorId() { return invitadorId; }
    public void setInvitadorId(Integer invitadorId) { this.invitadorId = invitadorId; }

    public String getInvitadorNombre() { return invitadorNombre; }
    public void setInvitadorNombre(String invitadorNombre) { this.invitadorNombre = invitadorNombre; }

    public String getInvitadorAvatar() { return invitadorAvatar; }
    public void setInvitadorAvatar(String invitadorAvatar) { this.invitadorAvatar = invitadorAvatar; }

    public Integer getInvitadoId() { return invitadoId; }
    public void setInvitadoId(Integer invitadoId) { this.invitadoId = invitadoId; }

    public String getInvitadoNombre() { return invitadoNombre; }
    public void setInvitadoNombre(String invitadoNombre) { this.invitadoNombre = invitadoNombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(Date fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
}
