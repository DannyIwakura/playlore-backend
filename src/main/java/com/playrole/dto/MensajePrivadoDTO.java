package com.playrole.dto;

import java.util.Date;

import com.playrole.enums.EstadoMensaje;
import com.playrole.model.MensajePrivado;
import com.playrole.model.Usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MensajePrivadoDTO {
	private Integer id;

    @NotNull(message = "El emisor es obligatorio")
    private Integer emisorId;

    @NotNull(message = "El receptor es obligatorio")
    private Integer receptorId;

    @NotNull(message = "El contenido no puede estar vacío")
    @Size(min = 1, max = 2000, message = "El contenido debe tener entre 1 y 2000 caracteres")
    private String contenido;
    
    private String titulo;

    private EstadoMensaje estadoUsuario;

    private Date fechaEnvio;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEmisorId() { return emisorId; }
    public void setEmisorId(Integer emisorId) { this.emisorId = emisorId; }

    public Integer getReceptorId() { return receptorId; }
    public void setReceptorId(Integer receptorId) { this.receptorId = receptorId; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public EstadoMensaje getEstadoUsuario() { return estadoUsuario; }
    public void setEstadoUsuario(EstadoMensaje estadoUsuario) { this.estadoUsuario = estadoUsuario; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public static MensajePrivadoDTO fromEntity(MensajePrivado mensaje, Integer userId) {
    	MensajePrivadoDTO dto = new MensajePrivadoDTO();
        dto.setId(mensaje.getIdMensaje());
        dto.setEmisorId(mensaje.getEmisorId().getUserId());
        dto.setReceptorId(mensaje.getReceptorId().getUserId());
        dto.setContenido(mensaje.getContenido());
        dto.setTitulo(mensaje.getTitulo());
        dto.setFechaEnvio(mensaje.getFechaEnvio());

        // Determinar el estado según quién vea el mensaje
        if (mensaje.getEmisorId().getUserId().equals(userId)) {
            dto.setEstadoUsuario(mensaje.getEstadoEmisor());
        } else if (mensaje.getReceptorId().getUserId().equals(userId)) {
            dto.setEstadoUsuario(mensaje.getEstadoReceptor());
        } else {
            dto.setEstadoUsuario(null);
        }

        return dto;
    }

    public MensajePrivado toEntity(Usuario emisor, Usuario receptor) {
        MensajePrivado mensaje = new MensajePrivado();
        mensaje.setEmisorId(emisor);
        mensaje.setReceptorId(receptor);
        mensaje.setContenido(this.contenido);
        mensaje.setTitulo(this.titulo);
        mensaje.setFechaEnvio(this.fechaEnvio != null ? this.fechaEnvio : new Date());
        //por defecto ambos usuarios ven NO_LEIDO
        mensaje.setEstadoEmisor(EstadoMensaje.NO_LEIDO);
        mensaje.setEstadoReceptor(EstadoMensaje.NO_LEIDO);
        return mensaje;
    }
}
