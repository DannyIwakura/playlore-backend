package com.playrole.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.playrole.model.Anuncio;
import com.playrole.model.PerfilPersonaje;

/** Anuncio RP con datos vivos del personaje (sin snapshot). */
public class AnuncioDTO {

    private Integer idAnuncio;
    private String contenido;
    private boolean activo;
    private Date fechaCreacion;
    private Integer personajeId;
    private String nombrePersonaje;
    private String avatarPersonaje;
    private String genero;
    private String raza;
    private String clase;
    private Integer edadPersonaje;
    private boolean online;
    private Integer usuarioId;
    private List<AnuncioCategoriaDTO> categorias = new ArrayList<>();

    public Integer getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(Integer idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getPersonajeId() {
        return personajeId;
    }

    public void setPersonajeId(Integer personajeId) {
        this.personajeId = personajeId;
    }

    public String getNombrePersonaje() {
        return nombrePersonaje;
    }

    public void setNombrePersonaje(String nombrePersonaje) {
        this.nombrePersonaje = nombrePersonaje;
    }

    public String getAvatarPersonaje() {
        return avatarPersonaje;
    }

    public void setAvatarPersonaje(String avatarPersonaje) {
        this.avatarPersonaje = avatarPersonaje;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public Integer getEdadPersonaje() {
        return edadPersonaje;
    }

    public void setEdadPersonaje(Integer edadPersonaje) {
        this.edadPersonaje = edadPersonaje;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<AnuncioCategoriaDTO> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<AnuncioCategoriaDTO> categorias) {
        this.categorias = categorias != null ? categorias : new ArrayList<>();
    }

    public static AnuncioDTO fromEntity(Anuncio anuncio) {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setIdAnuncio(anuncio.getIdAnuncio());
        dto.setContenido(anuncio.getContenido());
        dto.setActivo(anuncio.isActivo());
        dto.setFechaCreacion(anuncio.getFechaCreacion());

        PerfilPersonaje personaje = anuncio.getPersonaje();
        if (personaje != null) {
            dto.setPersonajeId(personaje.getIdPersonaje());
            dto.setNombrePersonaje(personaje.getNombre());
            dto.setAvatarPersonaje(personaje.getAvatar());
            dto.setGenero(personaje.getGenero());
            dto.setRaza(personaje.getRaza());
            dto.setClase(personaje.getClase());
            dto.setEdadPersonaje(personaje.getEdadPersonaje());
            if (personaje.getUserId() != null) {
                dto.setUsuarioId(personaje.getUserId().getUserId());
            }
        }
        return dto;
    }
}
