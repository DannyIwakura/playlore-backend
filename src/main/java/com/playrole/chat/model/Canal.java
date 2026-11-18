package com.playrole.chat.model;

import com.playrole.chat.enums.TipoCanal;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "canales")
public class Canal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canal")
    private Integer idCanal;

    @Basic(optional = false)
    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoCanal tipo;

    @Column(name = "es_privado", nullable = false)
    private boolean privado = false;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    @Column(name = "fecha_creacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", referencedColumnName = "id_personaje")
    private PerfilPersonaje creador;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario", referencedColumnName = "user_id")
    private Usuario creadoPorUsuario;

    @Column(name = "suspendido", nullable = false)
    private boolean suspendido = false;

    @Column(name = "suspendido_hasta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date suspendidoHasta;

    @Column(name = "fecha_suspension")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSuspension;

    @Column(name = "suspendido_por_id")
    private Integer suspendidoPorId;

    @Column(name = "suspendido_por_nombre", length = 255)
    private String suspendidoPorNombre;

    @Column(name = "motivo_suspension", length = 255)
    private String motivoSuspension;

    public Canal() {}

    public Canal(Integer idCanal) {
        this.idCanal = idCanal;
    }

    public Integer getIdCanal() { return idCanal; }
    public void setIdCanal(Integer idCanal) { this.idCanal = idCanal; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoCanal getTipo() { return tipo; }
    public void setTipo(TipoCanal tipo) { this.tipo = tipo; }

    public boolean isPrivado() { return privado; }
    public void setPrivado(boolean privado) { this.privado = privado; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public PerfilPersonaje getCreador() { return creador; }
    public void setCreador(PerfilPersonaje creador) { this.creador = creador; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Usuario getCreadoPorUsuario() { return creadoPorUsuario; }
    public void setCreadoPorUsuario(Usuario creadoPorUsuario) { this.creadoPorUsuario = creadoPorUsuario; }

    public boolean isSuspendido() { return suspendido; }
    public void setSuspendido(boolean suspendido) { this.suspendido = suspendido; }

    public Date getSuspendidoHasta() { return suspendidoHasta; }
    public void setSuspendidoHasta(Date suspendidoHasta) { this.suspendidoHasta = suspendidoHasta; }

    public Date getFechaSuspension() { return fechaSuspension; }
    public void setFechaSuspension(Date fechaSuspension) { this.fechaSuspension = fechaSuspension; }

    public Integer getSuspendidoPorId() { return suspendidoPorId; }
    public void setSuspendidoPorId(Integer suspendidoPorId) { this.suspendidoPorId = suspendidoPorId; }

    public String getSuspendidoPorNombre() { return suspendidoPorNombre; }
    public void setSuspendidoPorNombre(String suspendidoPorNombre) { this.suspendidoPorNombre = suspendidoPorNombre; }

    public String getMotivoSuspension() { return motivoSuspension; }
    public void setMotivoSuspension(String motivoSuspension) { this.motivoSuspension = motivoSuspension; }

    /**
     * Una suspension es efectiva mientras siga marcada y no haya vencido.
     * El scheduler limpia las vencidas, pero las consultas y los permisos
     * usan esta comprobacion para no depender del desfase de la tarea.
     */
    public boolean isSuspendidoActivo() {
        return suspendido && (suspendidoHasta == null || suspendidoHasta.after(new Date()));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idCanal != null ? idCanal.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Canal)) return false;
        Canal other = (Canal) object;
        return !((this.idCanal == null && other.idCanal != null)
                || (this.idCanal != null && !this.idCanal.equals(other.idCanal)));
    }

    @Override
    public String toString() {
        return "com.playrole.chat.model.Canal[ idCanal=" + idCanal + " ]";
    }
}
