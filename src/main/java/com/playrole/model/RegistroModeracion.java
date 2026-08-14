package com.playrole.model;

import com.playrole.enums.AccionModeracion;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Registro inmutable de acciones de moderación. Guarda snapshots (id + nombre)
 * del moderador y del objetivo en vez de claves foráneas, de forma que el
 * registro de auditoría sobrevive a la eliminación de usuarios o personajes.
 */
@Entity
@Table(name = "registros_moderacion")
public class RegistroModeracion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 30)
    private AccionModeracion accion;

    @Column(name = "moderador_id")
    private Integer moderadorId;

    @Column(name = "moderador_nombre", length = 255)
    private String moderadorNombre;

    @Column(name = "objetivo_usuario_id")
    private Integer objetivoUsuarioId;

    @Column(name = "objetivo_usuario_nombre", length = 255)
    private String objetivoUsuarioNombre;

    @Column(name = "objetivo_personaje_id")
    private Integer objetivoPersonajeId;

    @Column(name = "objetivo_personaje_nombre", length = 255)
    private String objetivoPersonajeNombre;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha = new Date();

    public RegistroModeracion() {}

    public Integer getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Integer idRegistro) { this.idRegistro = idRegistro; }

    public AccionModeracion getAccion() { return accion; }
    public void setAccion(AccionModeracion accion) { this.accion = accion; }

    public Integer getModeradorId() { return moderadorId; }
    public void setModeradorId(Integer moderadorId) { this.moderadorId = moderadorId; }

    public String getModeradorNombre() { return moderadorNombre; }
    public void setModeradorNombre(String moderadorNombre) { this.moderadorNombre = moderadorNombre; }

    public Integer getObjetivoUsuarioId() { return objetivoUsuarioId; }
    public void setObjetivoUsuarioId(Integer objetivoUsuarioId) { this.objetivoUsuarioId = objetivoUsuarioId; }

    public String getObjetivoUsuarioNombre() { return objetivoUsuarioNombre; }
    public void setObjetivoUsuarioNombre(String objetivoUsuarioNombre) { this.objetivoUsuarioNombre = objetivoUsuarioNombre; }

    public Integer getObjetivoPersonajeId() { return objetivoPersonajeId; }
    public void setObjetivoPersonajeId(Integer objetivoPersonajeId) { this.objetivoPersonajeId = objetivoPersonajeId; }

    public String getObjetivoPersonajeNombre() { return objetivoPersonajeNombre; }
    public void setObjetivoPersonajeNombre(String objetivoPersonajeNombre) { this.objetivoPersonajeNombre = objetivoPersonajeNombre; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRegistro != null ? idRegistro.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RegistroModeracion)) return false;
        RegistroModeracion other = (RegistroModeracion) object;
        return !((this.idRegistro == null && other.idRegistro != null)
                || (this.idRegistro != null && !this.idRegistro.equals(other.idRegistro)));
    }

    @Override
    public String toString() {
        return "com.playrole.model.RegistroModeracion[ idRegistro=" + idRegistro + " ]";
    }
}
