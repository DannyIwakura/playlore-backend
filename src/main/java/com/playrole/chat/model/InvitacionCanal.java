package com.playrole.chat.model;

import com.playrole.chat.enums.EstadoInvitacion;
import com.playrole.model.PerfilPersonaje;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Invitacion de un personaje a un canal. Se guarda como entidad propia para
 * conocer su estado (pendiente, aceptada o rechazada) aunque el mensaje
 * privado que la transporta se elimine de la conversacion.
 */
@Entity
@Table(name = "invitaciones_canal",
       indexes = @Index(name = "idx_invitacion_invitado_estado",
               columnList = "invitado_id, estado"))
public class InvitacionCanal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_invitacion")
    private Integer idInvitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canal_id", referencedColumnName = "id_canal")
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitado_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje invitado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitador_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje invitador;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion = new Date();

    @Column(name = "fecha_respuesta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRespuesta;

    public InvitacionCanal() {}

    public Integer getIdInvitacion() { return idInvitacion; }
    public void setIdInvitacion(Integer idInvitacion) { this.idInvitacion = idInvitacion; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public PerfilPersonaje getInvitado() { return invitado; }
    public void setInvitado(PerfilPersonaje invitado) { this.invitado = invitado; }

    public PerfilPersonaje getInvitador() { return invitador; }
    public void setInvitador(PerfilPersonaje invitador) { this.invitador = invitador; }

    public EstadoInvitacion getEstado() { return estado; }
    public void setEstado(EstadoInvitacion estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(Date fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idInvitacion != null ? idInvitacion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof InvitacionCanal)) return false;
        InvitacionCanal other = (InvitacionCanal) object;
        return !((this.idInvitacion == null && other.idInvitacion != null)
                || (this.idInvitacion != null && !this.idInvitacion.equals(other.idInvitacion)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.InvitacionCanal[ idInvitacion=" + idInvitacion + " ]";
    }
}
