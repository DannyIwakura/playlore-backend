package com.playrole.dto;

import com.playrole.enums.AccionModeracion;
import com.playrole.model.RegistroModeracion;

import java.util.Date;

public class RegistroModeracionDTO {

    private Integer idRegistro;
    private AccionModeracion accion;
    private Integer moderadorId;
    private String moderadorNombre;
    private Integer objetivoUsuarioId;
    private String objetivoUsuarioNombre;
    private Integer objetivoPersonajeId;
    private String objetivoPersonajeNombre;
    private String motivo;
    private String detalle;
    private Date fecha;

    public static RegistroModeracionDTO fromEntity(RegistroModeracion r) {
        RegistroModeracionDTO dto = new RegistroModeracionDTO();
        dto.setIdRegistro(r.getIdRegistro());
        dto.setAccion(r.getAccion());
        dto.setModeradorId(r.getModeradorId());
        dto.setModeradorNombre(r.getModeradorNombre());
        dto.setObjetivoUsuarioId(r.getObjetivoUsuarioId());
        dto.setObjetivoUsuarioNombre(r.getObjetivoUsuarioNombre());
        dto.setObjetivoPersonajeId(r.getObjetivoPersonajeId());
        dto.setObjetivoPersonajeNombre(r.getObjetivoPersonajeNombre());
        dto.setMotivo(r.getMotivo());
        dto.setDetalle(r.getDetalle());
        dto.setFecha(r.getFecha());
        return dto;
    }

    public Integer getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Integer idRegistro) { this.idRegistro = idRegistro; }

    public AccionModeracion getAccion() { return accion; }
    public void setAccion(AccionModeracion accion) { this.accion = accion; }

    public Integer getModeradorId() { return moderadorId; }
    public void setModeradorId(Integer moderadorId) { this.moderadorId = moderadorId; }

    public String getModeradorNombre() { return moderadorNombre; }
    public void setModeradorNombre(String moderadorNombre) { this.moderadorNombre = moderadorNombre; }

    public Integer getObjetivoUsuarioId() { return objetivoUsuarioId; }
    public void setObjetivoUsuarioId(Integer objetivoUsuarioId) { this.objetivoUsuarioId = objetivoUsuarioId; }

    public String getObjetivoUsuarioNombre() { return objetivoUsuarioNombre; }
    public void setObjetivoUsuarioNombre(String objetivoUsuarioNombre) { this.objetivoUsuarioNombre = objetivoUsuarioNombre; }

    public Integer getObjetivoPersonajeId() { return objetivoPersonajeId; }
    public void setObjetivoPersonajeId(Integer objetivoPersonajeId) { this.objetivoPersonajeId = objetivoPersonajeId; }

    public String getObjetivoPersonajeNombre() { return objetivoPersonajeNombre; }
    public void setObjetivoPersonajeNombre(String objetivoPersonajeNombre) { this.objetivoPersonajeNombre = objetivoPersonajeNombre; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
}
