package com.playrole.chat.model;

import com.playrole.model.PerfilPersonaje;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "baneos_canal")
public class BaneoCanal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_baneo")
    private Integer idBaneo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canal_id", referencedColumnName = "id_canal")
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personaje_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje personaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baneado_por_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje baneadoPor;

    @Column(name = "fecha_baneo", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBaneo = new Date();

    @Column(name = "fecha_expiracion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaExpiracion;

    public BaneoCanal() {}

    public Integer getIdBaneo() { return idBaneo; }
    public void setIdBaneo(Integer idBaneo) { this.idBaneo = idBaneo; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public PerfilPersonaje getPersonaje() { return personaje; }
    public void setPersonaje(PerfilPersonaje personaje) { this.personaje = personaje; }

    public PerfilPersonaje getBaneadoPor() { return baneadoPor; }
    public void setBaneadoPor(PerfilPersonaje baneadoPor) { this.baneadoPor = baneadoPor; }

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
        hash += (idBaneo != null ? idBaneo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BaneoCanal)) return false;
        BaneoCanal other = (BaneoCanal) object;
        return !((this.idBaneo == null && other.idBaneo != null)
                || (this.idBaneo != null && !this.idBaneo.equals(other.idBaneo)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.BaneoCanal[ idBaneo=" + idBaneo + " ]";
    }
}
