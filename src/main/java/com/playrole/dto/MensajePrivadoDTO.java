package com.playrole.dto;

import java.util.Date;

import com.playrole.enums.EstadoMensaje;
import com.playrole.model.MensajePrivado;
import com.playrole.model.Usuario;

public class MensajePrivadoDTO {
	private Integer id;
    private Integer emisorId;
    private Integer receptorId;
    private String contenido;
    private EstadoMensaje estado;
    private Date fechaEnvio;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getEmisorId() { return emisorId; }
    public void setEmisorId(Integer emisorId) { this.emisorId = emisorId; }
    public Integer getReceptorId() { return receptorId; }
    public void setReceptorId(Integer receptorId) { this.receptorId = receptorId; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public EstadoMensaje getEstado() { return estado; }
    public void setEstado(EstadoMensaje estado) { this.estado = estado; }
    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public static MensajePrivadoDTO fromEntity(MensajePrivado mensaje) {
        MensajePrivadoDTO dto = new MensajePrivadoDTO();
        dto.setId(mensaje.getIdMensaje());
        dto.setEmisorId(mensaje.getEmisorId().getUserId());
        dto.setReceptorId(mensaje.getReceptorId().getUserId());
        dto.setContenido(mensaje.getContenido());
        dto.setEstado(mensaje.getEstado());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        return dto;
    }

    public MensajePrivado toEntity(Usuario emisor, Usuario receptor) {
        MensajePrivado mensaje = new MensajePrivado();
        mensaje.setEmisorId(emisor);
        mensaje.setReceptorId(receptor);
        mensaje.setContenido(this.contenido);
        mensaje.setEstado(this.estado != null ? this.estado : EstadoMensaje.NO_LEIDO);
        mensaje.setFechaEnvio(this.fechaEnvio != null ? this.fechaEnvio : new Date());
        return mensaje;
    }
    
}
