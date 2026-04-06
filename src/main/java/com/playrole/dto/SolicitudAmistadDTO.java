package com.playrole.dto;

import java.util.Date;

import com.playrole.enums.EstadoSolicitud;
import com.playrole.model.SolicitudAmistad;
import com.playrole.model.Usuario;

public class SolicitudAmistadDTO {

	private Integer id;
    private EstadoSolicitud estado;
    private Date fechaPeticion;
    private Date fechaRespuesta;
    private Integer emisorId;
    private String emisorNombre;
    private Integer receptorId;
    private String receptorNombre;

    public SolicitudAmistadDTO() {}

    public SolicitudAmistadDTO(Integer id, EstadoSolicitud estado, Date fechaPeticion, Date fechaRespuesta,
                               Integer emisorId, String emisorNombre, Integer receptorId, String receptorNombre) {
        this.id = id;
        this.estado = estado;
        this.fechaPeticion = fechaPeticion;
        this.fechaRespuesta = fechaRespuesta;
        this.emisorId = emisorId;
        this.emisorNombre = emisorNombre;
        this.receptorId = receptorId;
        this.receptorNombre = receptorNombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public Date getFechaPeticion() { return fechaPeticion; }
    public void setFechaPeticion(Date fechaPeticion) { this.fechaPeticion = fechaPeticion; }

    public Date getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(Date fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }

    public Integer getEmisorId() { return emisorId; }
    public void setEmisorId(Integer emisorId) { this.emisorId = emisorId; }

    public String getEmisorNombre() { return emisorNombre; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }

    public Integer getReceptorId() { return receptorId; }
    public void setReceptorId(Integer receptorId) { this.receptorId = receptorId; }

    public String getReceptorNombre() { return receptorNombre; }
    public void setReceptorNombre(String receptorNombre) { this.receptorNombre = receptorNombre; }
	
    public static SolicitudAmistadDTO fromEntity(SolicitudAmistad solicitudAmistad) {
    	
    	SolicitudAmistadDTO dto = new SolicitudAmistadDTO();
    	dto.setId(solicitudAmistad.getId());
    	dto.setEstado(solicitudAmistad.getEstado());
    	dto.setFechaPeticion(solicitudAmistad.getFechaPeticion());
    	dto.setFechaRespuesta(solicitudAmistad.getFechaRespuesta());
    	dto.setEmisorId(solicitudAmistad.getEmisorId().getUserId());
    	dto.setEmisorNombre(solicitudAmistad.getEmisorId().getNombre());
    	dto.setReceptorId(solicitudAmistad.getReceptorId().getUserId());
    	dto.setReceptorNombre(solicitudAmistad.getReceptorId().getNombre());
    	
    	return dto;
    }
    
    public SolicitudAmistad toEntity(Usuario emisor, Usuario receptor) {
    	
    	SolicitudAmistad solicitudAmistad = new SolicitudAmistad();
    	solicitudAmistad.setEmisorId(emisor);
    	solicitudAmistad.setReceptorId(receptor);
    	solicitudAmistad.setEstado(this.estado);
    	solicitudAmistad.setFechaPeticion(this.fechaPeticion);
    	solicitudAmistad.setFechaRespuesta(this.fechaRespuesta);
    	
    	return solicitudAmistad;
    }
    
}
