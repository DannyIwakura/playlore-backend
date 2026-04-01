package com.playrole.model;

import java.io.Serializable;
import java.util.Date;

import com.playrole.enums.EstadoSolicitud;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "solicitud_amistades")
@NamedQueries({
    @NamedQuery(name = "SolicitudAmistad.findAll", query = "SELECT s FROM SolicitudAmistad s")})
public class SolicitudAmistad implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoSolicitud estado;
    @Column(name = "fecha_peticion")
    @Temporal(TemporalType.DATE)
    private Date fechaPeticion;
    @Column(name = "fecha_respuesta")
    @Temporal(TemporalType.DATE)
    private Date fechaRespuesta;
    @JoinColumn(name = "emisor_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario emisorId;
    @JoinColumn(name = "receptor_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario receptorId;

    public SolicitudAmistad() {
    }

    public SolicitudAmistad(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public Date getFechaPeticion() {
        return fechaPeticion;
    }

    public void setFechaPeticion(Date fechaPeticion) {
        this.fechaPeticion = fechaPeticion;
    }

    public Date getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Date fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public Usuario getEmisorId() {
        return emisorId;
    }

    public void setEmisorId(Usuario emisorId) {
        this.emisorId = emisorId;
    }

    public Usuario getReceptorId() {
        return receptorId;
    }

    public void setReceptorId(Usuario receptorId) {
        this.receptorId = receptorId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SolicitudAmistad)) {
            return false;
        }
        SolicitudAmistad other = (SolicitudAmistad) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "joptionpane.dbplaylore.SolicitudAmistad[ id=" + id + " ]";
    }
    
}