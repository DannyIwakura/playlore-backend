package com.playrole.dto;

import java.util.Date;

import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;
import com.playrole.model.Usuario;

public class PerfilPersonajeDTO {
	private Integer idPersonaje;
    private String nombre;
    private Integer edadPersonaje;
    private String avatar;
    private Integer estado;
    private String trasfondo;
    private String raza;
    private String clase;
    private Date fechaCreacion;
    private Date fechaModificacion;
    private Integer userId;

    public Integer getIdPersonaje() { return idPersonaje; }
    public void setIdPersonaje(Integer idPersonaje) { this.idPersonaje = idPersonaje; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEdadPersonaje() { return edadPersonaje; }
    public void setEdadPersonaje(Integer edadPersonaje) { this.edadPersonaje = edadPersonaje; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getEstado() { return estado; }
    public void setEstado(Integer estado) { this.estado = estado; }
    public String getTrasfondo() { return trasfondo; }
    public void setTrasfondo(String trasfondo) { this.trasfondo = trasfondo; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getClase() { return clase; }
    public void setClase(String clase) { this.clase = clase; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public static PerfilPersonajeDTO fromEntity(PerfilPersonaje personaje) {
        PerfilPersonajeDTO dto = new PerfilPersonajeDTO();
        dto.setIdPersonaje(personaje.getIdPersonaje());
        dto.setNombre(personaje.getNombre());
        dto.setEdadPersonaje(personaje.getEdadPersonaje());
        dto.setAvatar(personaje.getAvatar());
        dto.setEstado(personaje.getEstado());
        dto.setTrasfondo(personaje.getTrasfondo());
        dto.setRaza(personaje.getRaza());
        dto.setClase(personaje.getClase());
        dto.setFechaCreacion(personaje.getFechaCreacion());
        dto.setFechaModificacion(personaje.getFechaModificacion());
        dto.setUserId(personaje.getUserId() != null ? personaje.getUserId().getUserId() : null);
        return dto;
    }

    public PerfilPersonaje toEntity(Usuario usuario) {
        PerfilPersonaje personaje = new PerfilPersonaje();
        personaje.setIdPersonaje(this.idPersonaje);
        personaje.setNombre(this.nombre);
        personaje.setEdadPersonaje(this.edadPersonaje);
        personaje.setAvatar(this.avatar);
        personaje.setEstado(this.estado);
        personaje.setTrasfondo(this.trasfondo);
        personaje.setRaza(this.raza);
        personaje.setClase(this.clase);
        personaje.setFechaCreacion(this.fechaCreacion != null ? this.fechaCreacion : new Date());
        personaje.setFechaModificacion(new Date());
        personaje.setUserId(usuario); // pasar la entidad Usuario ya obtenida
        return personaje;
    }
}
