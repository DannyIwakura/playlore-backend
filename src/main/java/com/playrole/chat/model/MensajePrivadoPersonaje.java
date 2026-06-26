package com.playrole.chat.model;

import com.playrole.model.PerfilPersonaje;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "mensajes_privados_personaje")
public class MensajePrivadoPersonaje implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje emisor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receptor_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje receptor;

    @Lob
    @Column(name = "contenido", columnDefinition = "LONGTEXT", nullable = false)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio = new Date();

    @Column(nullable = false)
    private boolean leido = false;

    @Column(name = "eliminado_emisor", nullable = false)
    private boolean eliminadoEmisor = false;

    @Column(name = "eliminado_receptor", nullable = false)
    private boolean eliminadoReceptor = false;

    public MensajePrivadoPersonaje() {}

    public Integer getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Integer idMensaje) { this.idMensaje = idMensaje; }

    public PerfilPersonaje getEmisor() { return emisor; }
    public void setEmisor(PerfilPersonaje emisor) { this.emisor = emisor; }

    public PerfilPersonaje getReceptor() { return receptor; }
    public void setReceptor(PerfilPersonaje receptor) { this.receptor = receptor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public boolean isEliminadoEmisor() { return eliminadoEmisor; }
    public void setEliminadoEmisor(boolean eliminadoEmisor) { this.eliminadoEmisor = eliminadoEmisor; }

    public boolean isEliminadoReceptor() { return eliminadoReceptor; }
    public void setEliminadoReceptor(boolean eliminadoReceptor) { this.eliminadoReceptor = eliminadoReceptor; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMensaje != null ? idMensaje.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MensajePrivadoPersonaje)) return false;
        MensajePrivadoPersonaje other = (MensajePrivadoPersonaje) object;
        return !((this.idMensaje == null && other.idMensaje != null)
                || (this.idMensaje != null && !this.idMensaje.equals(other.idMensaje)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.MensajePrivadoPersonaje[ idMensaje=" + idMensaje + " ]";
    }
}
