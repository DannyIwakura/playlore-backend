package com.playrole.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Anuncio RP. La autoría es del personaje (FK viva: nombre/avatar se leen del
 * personaje al listar), pero el límite de activos se cuenta por cuenta.
 */
@Entity
@Table(name = "anuncios")
public class Anuncio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_anuncio")
    private Integer idAnuncio;

    @Basic(optional = false)
    @Column(name = "contenido", columnDefinition = "LONGTEXT")
    private String contenido;

    @Basic(optional = false)
    @Column(name = "activo", nullable = false)
    private boolean activo = false;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    // EAGER deliberado: los listados necesitan los datos del personaje y así
    // evitamos una consulta por anuncio. El usuario se resuelve en lote en el service.
    @JoinColumn(name = "personaje_id", referencedColumnName = "id_personaje")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private PerfilPersonaje personaje;

    @OneToMany(mappedBy = "anuncio", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<AnuncioCategoria> anuncioCategoriaList;

    public Anuncio() {
    }

    public Anuncio(Integer idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

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

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public PerfilPersonaje getPersonaje() {
        return personaje;
    }

    public void setPersonaje(PerfilPersonaje personaje) {
        this.personaje = personaje;
    }

    public List<AnuncioCategoria> getAnuncioCategoriaList() {
        return anuncioCategoriaList;
    }

    public void setAnuncioCategoriaList(List<AnuncioCategoria> anuncioCategoriaList) {
        this.anuncioCategoriaList = anuncioCategoriaList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idAnuncio != null ? idAnuncio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Anuncio)) {
            return false;
        }
        Anuncio other = (Anuncio) object;
        return !((this.idAnuncio == null && other.idAnuncio != null)
                || (this.idAnuncio != null && !this.idAnuncio.equals(other.idAnuncio)));
    }

    @Override
    public String toString() {
        return "com.playrole.model.Anuncio[ idAnuncio=" + idAnuncio + " ]";
    }
}
