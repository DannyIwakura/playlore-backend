package com.playrole.dto;

import java.util.Date;

import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;

public class PersonajeCategoriaDTO {

	private Integer id;
    private String tipo;
    private Date fechaAdicion;

    private Integer idCategoria;
    private String nombreCategoria;

    private Integer idPersonaje;
    private String nombrePersonaje;

    public PersonajeCategoriaDTO() {}

    public PersonajeCategoriaDTO(Integer id, String tipo, Date fechaAdicion,
                                 Integer idCategoria, String nombreCategoria,
                                 Integer idPersonaje, String nombrePersonaje) {
        this.id = id;
        this.tipo = tipo;
        this.fechaAdicion = fechaAdicion;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.idPersonaje = idPersonaje;
        this.nombrePersonaje = nombrePersonaje;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getFechaAdicion() { return fechaAdicion; }
    public void setFechaAdicion(Date fechaAdicion) { this.fechaAdicion = fechaAdicion; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public Integer getIdPersonaje() { return idPersonaje; }
    public void setIdPersonaje(Integer idPersonaje) { this.idPersonaje = idPersonaje; }

    public String getNombrePersonaje() { return nombrePersonaje; }
    public void setNombrePersonaje(String nombrePersonaje) { this.nombrePersonaje = nombrePersonaje; }

    public static PersonajeCategoriaDTO fromEntity(PersonajeCategoria pc) {
        PersonajeCategoriaDTO dto = new PersonajeCategoriaDTO();
        dto.setId(pc.getId());
        dto.setTipo(pc.getTipo());
        dto.setFechaAdicion(pc.getFechaAdicion());

        if (pc.getIdCategoria() != null) {
            dto.setIdCategoria(pc.getIdCategoria().getIdCategoria());
            dto.setNombreCategoria(pc.getIdCategoria().getNombre());
        }

        if (pc.getIdPersonaje() != null) {
            dto.setIdPersonaje(pc.getIdPersonaje().getIdPersonaje());
            dto.setNombrePersonaje(pc.getIdPersonaje().getNombre());
        }

        return dto;
    }

    public PersonajeCategoria toEntity(Categoria categoria, PerfilPersonaje personaje) {
    	 PersonajeCategoria pc = new PersonajeCategoria();
    	    pc.setId(this.id);
    	    pc.setTipo(this.tipo);
    	    pc.setFechaAdicion(this.fechaAdicion != null ? this.fechaAdicion : new Date());
    	    pc.setIdCategoria(categoria);
    	    pc.setIdPersonaje(personaje);
    	    return pc;
    }

}
