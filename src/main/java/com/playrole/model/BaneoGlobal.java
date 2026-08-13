package com.playrole.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "baneos_globales")
public class BaneoGlobal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_baneo_global")
    private Integer idBaneoGlobal;

    // Si el ban es de toda la cuenta, usuario != null y personaje == null.
    // Si el ban es solo de un personaje, personaje != null y usuario == null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "user_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje personaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baneado_por_id", referencedColumnName = "user_id")
    private Usuario baneadoPor;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "fecha_baneo", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBaneo = new Date();

    @Column(name = "fecha_expiracion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaExpiracion;

    public BaneoGlobal() {}

    public Integer getIdBaneoGlobal() { return idBaneoGlobal; }
    public void setIdBaneoGlobal(Integer idBaneoGlobal) { this.idBaneoGlobal = idBaneoGlobal; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public PerfilPersonaje getPersonaje() { return personaje; }
    public void setPersonaje(PerfilPersonaje personaje) { this.personaje = personaje; }

    public Usuario getBaneadoPor() { return baneadoPor; }
    public void setBaneadoPor(Usuario baneadoPor) { this.baneadoPor = baneadoPor; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Date getFechaBaneo() { return fechaBaneo; }
    public void setFechaBaneo(Date fechaBaneo) { this.fechaBaneo = fechaBaneo; }

    public Date getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(Date fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public boolean isActivo() {
        return fechaExpiracion == null || fechaExpiracion.after(new Date());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idBaneoGlobal != null ? idBaneoGlobal.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BaneoGlobal)) return false;
        BaneoGlobal other = (BaneoGlobal) object;
        return !((this.idBaneoGlobal == null && other.idBaneoGlobal != null)
                || (this.idBaneoGlobal != null && !this.idBaneoGlobal.equals(other.idBaneoGlobal)));
    }

    @Override
    public String toString() {
        return "com.playrole.model.BaneoGlobal[ idBaneoGlobal=" + idBaneoGlobal + " ]";
    }
}
