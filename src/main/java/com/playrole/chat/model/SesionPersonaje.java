package com.playrole.chat.model;

import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "sesiones_personaje")
public class SesionPersonaje implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Integer idSesion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personaje_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje personaje;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", referencedColumnName = "user_id")
    private Usuario usuario;

    @Column(name = "token_jwt", columnDefinition = "VARCHAR(500)", unique = true)
    private String tokenJwt;

    @Column(name = "fecha_inicio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio = new Date();

    @Column(name = "ultima_actividad")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimaActividad;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "ip_address")
    private String ipAddress;

    public SesionPersonaje() {}

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }

    public PerfilPersonaje getPersonaje() { return personaje; }
    public void setPersonaje(PerfilPersonaje personaje) { this.personaje = personaje; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTokenJwt() { return tokenJwt; }
    public void setTokenJwt(String tokenJwt) { this.tokenJwt = tokenJwt; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(Date ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSesion != null ? idSesion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SesionPersonaje)) return false;
        SesionPersonaje other = (SesionPersonaje) object;
        return !((this.idSesion == null && other.idSesion != null)
                || (this.idSesion != null && !this.idSesion.equals(other.idSesion)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.SesionPersonaje[ idSesion=" + idSesion + " ]";
    }
}
