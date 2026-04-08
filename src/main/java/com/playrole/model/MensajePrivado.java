package com.playrole.model;

import java.io.Serializable;
import java.util.Date;

import com.playrole.enums.EstadoMensaje;

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
@Table(name = "mensajes_privados")
public class MensajePrivado implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_mensaje")
    private Integer idMensaje;
	
	@Column(nullable = false, length = 255)
	private String titulo;

    @Column(name = "contenido")
    private String contenido;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "estado_emisor")
    private EstadoMensaje estadoEmisor = EstadoMensaje.NO_LEIDO;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "estado_receptor")
    private EstadoMensaje estadoReceptor = EstadoMensaje.NO_LEIDO;

    @Column(name = "fecha_envio")
    @Temporal(TemporalType.DATE)
    private Date fechaEnvio;

    @JoinColumn(name = "emisor_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario emisorId;

    @JoinColumn(name = "receptor_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario receptorId;

    public MensajePrivado() {
    }

    public MensajePrivado(Integer idMensaje) {
        this.idMensaje = idMensaje;
    }

    public Integer getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Integer idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public EstadoMensaje getEstadoEmisor() {
		return estadoEmisor;
	}

	public void setEstadoEmisor(EstadoMensaje estadoEmisor) {
		this.estadoEmisor = estadoEmisor;
	}

	public EstadoMensaje getEstadoReceptor() {
		return estadoReceptor;
	}

	public void setEstadoReceptor(EstadoMensaje estadoReceptor) {
		this.estadoReceptor = estadoReceptor;
	}

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
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
        hash += (idMensaje != null ? idMensaje.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MensajePrivado)) {
            return false;
        }
        MensajePrivado other = (MensajePrivado) object;
        if ((this.idMensaje == null && other.idMensaje != null) || (this.idMensaje != null && !this.idMensaje.equals(other.idMensaje))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "joptionpane.dbplaylore.MensajePrivado[ idMensaje=" + idMensaje + " ]";
    }
    
}
