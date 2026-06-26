package com.playrole.chat.model;

import com.playrole.model.PerfilPersonaje;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "mensajes_canal")
public class MensajeCanal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canal_id", referencedColumnName = "id_canal")
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personaje_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje personaje;

    @Lob
    @Column(name = "contenido", columnDefinition = "LONGTEXT", nullable = false)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio = new Date();

    @Column(nullable = false)
    private boolean editado = false;

    @Column(name = "fecha_edicion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEdicion;

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado = false;

    @Column(name = "eliminado_por_moderador", nullable = false)
    private boolean eliminadoPorModerador = false;

    public MensajeCanal() {}

    public Integer getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Integer idMensaje) { this.idMensaje = idMensaje; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public PerfilPersonaje getPersonaje() { return personaje; }
    public void setPersonaje(PerfilPersonaje personaje) { this.personaje = personaje; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isEditado() { return editado; }
    public void setEditado(boolean editado) { this.editado = editado; }

    public Date getFechaEdicion() { return fechaEdicion; }
    public void setFechaEdicion(Date fechaEdicion) { this.fechaEdicion = fechaEdicion; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public boolean isEliminadoPorModerador() { return eliminadoPorModerador; }
    public void setEliminadoPorModerador(boolean eliminadoPorModerador) { this.eliminadoPorModerador = eliminadoPorModerador; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMensaje != null ? idMensaje.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MensajeCanal)) return false;
        MensajeCanal other = (MensajeCanal) object;
        return !((this.idMensaje == null && other.idMensaje != null)
                || (this.idMensaje != null && !this.idMensaje.equals(other.idMensaje)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.MensajeCanal[ idMensaje=" + idMensaje + " ]";
    }
}
