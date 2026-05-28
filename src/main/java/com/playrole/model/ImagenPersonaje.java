package com.playrole.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "imagenes_personaje")
public class ImagenPersonaje implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @JoinColumn(name = "id_personaje", referencedColumnName = "id_personaje")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PerfilPersonaje idPersonaje;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Column(name = "nombre_original")
    private String nombreOriginal;

    @Column(name = "fecha_subida")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSubida;

    @Column(name = "orden")
    private Integer orden;

    public ImagenPersonaje() {
    }

    public ImagenPersonaje(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public Integer getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public PerfilPersonaje getIdPersonaje() {
        return idPersonaje;
    }

    public void setIdPersonaje(PerfilPersonaje idPersonaje) {
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idImagen != null ? idImagen.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImagenPersonaje)) {
            return false;
        }
        ImagenPersonaje other = (ImagenPersonaje) object;
        if ((this.idImagen == null && other.idImagen != null)
                || (this.idImagen != null && !this.idImagen.equals(other.idImagen))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ImagenPersonaje[ idImagen=" + idImagen + " ]";
    }
}
