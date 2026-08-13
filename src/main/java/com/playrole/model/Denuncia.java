package com.playrole.model;

import com.playrole.enums.EstadoDenuncia;
import com.playrole.enums.TipoDenuncia;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "denuncias")
public class Denuncia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_denuncia")
    private Integer idDenuncia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denunciante_id", referencedColumnName = "user_id")
    private Usuario denunciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denunciante_personaje_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje denunciantePersonaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoDenuncia tipo;

    @Column(name = "tipo_id", nullable = false)
    private Integer tipoId;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "detalle", columnDefinition = "LONGTEXT")
    private String detalle;

    // Snapshot para denuncias de MENSAJE_CANAL (sobrevive al borrado del mensaje)
    @Column(name = "contenido_denunciado", columnDefinition = "LONGTEXT")
    private String contenidoDenunciado;

    @Column(name = "canal_id")
    private Integer canalId;

    @Column(name = "autor_personaje_id")
    private Integer autorPersonajeId;

    // Snapshots: copian el nombre del denunciado y del canal en el momento de
    // la denuncia, de forma que la información sobrevive aunque se borre el objetivo.
    @Column(name = "objetivo_nombre", length = 255)
    private String objetivoNombre;

    @Column(name = "autor_personaje_nombre", length = 255)
    private String autorPersonajeNombre;

    @Column(name = "autor_usuario_id")
    private Integer autorUsuarioId;

    @Column(name = "autor_usuario_nombre", length = 255)
    private String autorUsuarioNombre;

    @Column(name = "canal_nombre", length = 255)
    private String canalNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDenuncia estado = EstadoDenuncia.PENDIENTE;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por", referencedColumnName = "user_id")
    private Usuario resueltoPor;

    @Column(name = "fecha_resolucion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaResolucion;

    @Column(name = "decision", length = 255)
    private String decision;

    public Denuncia() {}

    public Integer getIdDenuncia() { return idDenuncia; }
    public void setIdDenuncia(Integer idDenuncia) { this.idDenuncia = idDenuncia; }

    public Usuario getDenunciante() { return denunciante; }
    public void setDenunciante(Usuario denunciante) { this.denunciante = denunciante; }

    public PerfilPersonaje getDenunciantePersonaje() { return denunciantePersonaje; }
    public void setDenunciantePersonaje(PerfilPersonaje denunciantePersonaje) { this.denunciantePersonaje = denunciantePersonaje; }

    public TipoDenuncia getTipo() { return tipo; }
    public void setTipo(TipoDenuncia tipo) { this.tipo = tipo; }

    public Integer getTipoId() { return tipoId; }
    public void setTipoId(Integer tipoId) { this.tipoId = tipoId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getContenidoDenunciado() { return contenidoDenunciado; }
    public void setContenidoDenunciado(String contenidoDenunciado) { this.contenidoDenunciado = contenidoDenunciado; }

    public Integer getCanalId() { return canalId; }
    public void setCanalId(Integer canalId) { this.canalId = canalId; }

    public Integer getAutorPersonajeId() { return autorPersonajeId; }
    public void setAutorPersonajeId(Integer autorPersonajeId) { this.autorPersonajeId = autorPersonajeId; }

    public String getObjetivoNombre() { return objetivoNombre; }
    public void setObjetivoNombre(String objetivoNombre) { this.objetivoNombre = objetivoNombre; }

    public String getAutorPersonajeNombre() { return autorPersonajeNombre; }
    public void setAutorPersonajeNombre(String autorPersonajeNombre) { this.autorPersonajeNombre = autorPersonajeNombre; }

    public Integer getAutorUsuarioId() { return autorUsuarioId; }
    public void setAutorUsuarioId(Integer autorUsuarioId) { this.autorUsuarioId = autorUsuarioId; }

    public String getAutorUsuarioNombre() { return autorUsuarioNombre; }
    public void setAutorUsuarioNombre(String autorUsuarioNombre) { this.autorUsuarioNombre = autorUsuarioNombre; }

    public String getCanalNombre() { return canalNombre; }
    public void setCanalNombre(String canalNombre) { this.canalNombre = canalNombre; }

    public EstadoDenuncia getEstado() { return estado; }
    public void setEstado(EstadoDenuncia estado) { this.estado = estado; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Usuario getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(Usuario resueltoPor) { this.resueltoPor = resueltoPor; }

    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDenuncia != null ? idDenuncia.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Denuncia)) return false;
        Denuncia other = (Denuncia) object;
        return !((this.idDenuncia == null && other.idDenuncia != null)
                || (this.idDenuncia != null && !this.idDenuncia.equals(other.idDenuncia)));
    }

    @Override
    public String toString() {
        return "com.playrole.model.Denuncia[ idDenuncia=" + idDenuncia + " ]";
    }
}
