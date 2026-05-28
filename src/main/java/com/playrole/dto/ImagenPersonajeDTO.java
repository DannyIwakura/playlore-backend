package com.playrole.dto;

import java.util.Date;

import com.playrole.model.ImagenPersonaje;

public class ImagenPersonajeDTO {

    private Integer idImagen;
    private Integer idPersonaje;
    private String url;
    private String nombreOriginal;
    private Date fechaSubida;
    private Integer orden;

    public Integer getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public Integer getIdPersonaje() {
        return idPersonaje;
    }

    public void setIdPersonaje(Integer idPersonaje) {
        this.idPersonaje = idPersonaje;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public Date getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(Date fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public static ImagenPersonajeDTO fromEntity(ImagenPersonaje entity) {
        ImagenPersonajeDTO dto = new ImagenPersonajeDTO();
        dto.setIdImagen(entity.getIdImagen());
        dto.setIdPersonaje(entity.getIdPersonaje().getIdPersonaje());
        dto.setUrl(entity.getUrl());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setFechaSubida(entity.getFechaSubida());
        dto.setOrden(entity.getOrden());
        return dto;
    }
}
