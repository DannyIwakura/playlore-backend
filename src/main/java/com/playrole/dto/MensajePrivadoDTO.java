package com.playrole.dto;

import java.util.Date;
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
    
    private String emisorNombre;
    private String receptorNombre;

    @NotNull(message = "El contenido no puede estar vacío")
    @Size(min = 1, max = 2000, message = "El contenido debe tener entre 1 y 2000 caracteres")
    private String contenido;
    
    private String titulo;

    private Date fechaEnvio;

    // Campos simplificados para el Frontend
    private boolean leido;
    private boolean archivado;
    private boolean esMio; // Útil para saber si el usuario actual es el emisor

    // Getters y Setters
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

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public boolean isArchivado() { return archivado; }
    public void setArchivado(boolean archivado) { this.archivado = archivado; }

    public boolean isEsMio() { return esMio; }
    public void setEsMio(boolean esMio) { this.esMio = esMio; }

    /**
     * Convierte de Entidad a DTO basado en el contexto del usuario que consulta
     */
    public static MensajePrivadoDTO fromEntity(MensajePrivado mensaje, Integer currentUserId) {
        MensajePrivadoDTO dto = new MensajePrivadoDTO();
        dto.setId(mensaje.getIdMensaje());
        dto.setEmisorId(mensaje.getEmisor().getUserId());
        dto.setReceptorId(mensaje.getReceptor().getUserId());
        dto.setEmisorNombre(mensaje.getEmisor().getNombre());
        dto.setReceptorNombre(mensaje.getReceptor().getNombre());
        dto.setContenido(mensaje.getContenido());
        dto.setTitulo(mensaje.getTitulo());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        
        // El estado de lectura es global para el mensaje
        dto.setLeido(mensaje.isLeido());

        // Lógica de contexto: ¿Quién está pidiendo este DTO?
        if (mensaje.getEmisor().getUserId().equals(currentUserId)) {
            dto.setEsMio(true);
            dto.setArchivado(mensaje.isArchivadoEmisor());
        } else {
            dto.setEsMio(false);
            dto.setArchivado(mensaje.isArchivadoReceptor());
        }

        return dto;
    }

    /**
     * Convierte de DTO a Entidad para persistencia inicial
     */
    public MensajePrivado toEntity(Usuario emisor, Usuario receptor) {
        MensajePrivado mensaje = new MensajePrivado();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setContenido(this.contenido);
        mensaje.setTitulo(this.titulo);
        mensaje.setFechaEnvio(this.fechaEnvio != null ? this.fechaEnvio : new Date());
        
        // Valores iniciales por defecto
        mensaje.setLeido(false);
        mensaje.setVisibleEmisor(true);
        mensaje.setVisibleReceptor(true);
        mensaje.setArchivadoEmisor(false);
        mensaje.setArchivadoReceptor(false);
        
        return mensaje;
    }
}