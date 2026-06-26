package com.playrole.chat.model;

import com.playrole.chat.enums.RolCanal;
import com.playrole.model.PerfilPersonaje;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "miembros_canal",
       uniqueConstraints = @UniqueConstraint(columnNames = {"canal_id", "personaje_id"}))
public class MiembroCanal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_miembro")
    private Integer idMiembro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canal_id", referencedColumnName = "id_canal")
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personaje_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje personaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolCanal rol;

    @Column(name = "fecha_union", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaUnion = new Date();

    @Column(name = "silenciado_hasta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date silenciadoHasta;

    @Column(name = "invitado_por", nullable = false)
    private boolean invitado = false;

    public MiembroCanal() {}

    public Integer getIdMiembro() { return idMiembro; }
    public void setIdMiembro(Integer idMiembro) { this.idMiembro = idMiembro; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public PerfilPersonaje getPersonaje() { return personaje; }
    public void setPersonaje(PerfilPersonaje personaje) { this.personaje = personaje; }

    public RolCanal getRol() { return rol; }
    public void setRol(RolCanal rol) { this.rol = rol; }

    public Date getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(Date fechaUnion) { this.fechaUnion = fechaUnion; }

    public Date getSilenciadoHasta() { return silenciadoHasta; }
    public void setSilenciadoHasta(Date silenciadoHasta) { this.silenciadoHasta = silenciadoHasta; }

    public boolean isInvitado() { return invitado; }
    public void setInvitado(boolean invitado) { this.invitado = invitado; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMiembro != null ? idMiembro.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MiembroCanal)) return false;
        MiembroCanal other = (MiembroCanal) object;
        return !((this.idMiembro == null && other.idMiembro != null)
                || (this.idMiembro != null && !this.idMiembro.equals(other.idMiembro)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.MiembroCanal[ idMiembro=" + idMiembro + " ]";
    }
}
