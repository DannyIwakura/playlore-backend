package com.playrole.model;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "Mensajes_Privados")
public class MensajePrivado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Lob
    @Column(name = "contenido", columnDefinition = "LONGTEXT")
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio = new Date();
    
    @Column(name = "leido", nullable = false)
    private boolean leido = false;

    @Column(name = "visible_emisor", nullable = false)
    private boolean visibleEmisor = true;

    @Column(name = "visible_receptor", nullable = false)
    private boolean visibleReceptor = true;

    @Column(name = "archivado_emisor", nullable = false)
    private boolean archivadoEmisor = false;

    @Column(name = "archivado_receptor", nullable = false)
    private boolean archivadoReceptor = false;
    
    @Column(name = "eliminado_emisor", nullable = false)
    private boolean eliminadoEmisor = false;

    @Column(name = "eliminado_receptor", nullable = false)
    private boolean eliminadoReceptor = false;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "emisor_id", referencedColumnName = "user_id")
    private Usuario emisorId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "receptor_id", referencedColumnName = "user_id")
    private Usuario receptorId;

    public MensajePrivado() {}

    public Integer getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Integer idMensaje) { this.idMensaje = idMensaje; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public boolean isVisibleEmisor() { return visibleEmisor; }
    public void setVisibleEmisor(boolean visibleEmisor) { this.visibleEmisor = visibleEmisor; }

    public boolean isVisibleReceptor() { return visibleReceptor; }
    public void setVisibleReceptor(boolean visibleReceptor) { this.visibleReceptor = visibleReceptor; }

    public boolean isArchivadoEmisor() { return archivadoEmisor; }
    public void setArchivadoEmisor(boolean archivadoEmisor) { this.archivadoEmisor = archivadoEmisor; }

    public boolean isArchivadoReceptor() { return archivadoReceptor; }
    public void setArchivadoReceptor(boolean archivadoReceptor) { this.archivadoReceptor = archivadoReceptor; }
    
    public boolean isEliminadoEmisor() { return eliminadoEmisor; }
    public void setEliminadoEmisor(boolean eliminadoEmisor) { this.eliminadoEmisor = eliminadoEmisor; }
    
    public boolean isEliminadoReceptor() { return eliminadoReceptor; }
    public void setEliminadoReceptor(boolean eliminadoReceptor) { this.eliminadoReceptor = eliminadoReceptor; }

    
    public Usuario getEmisor() { return emisorId; }
    public void setEmisor(Usuario emisor) { this.emisorId = emisor; }

    public Usuario getReceptor() { return receptorId; }
    public void setReceptor(Usuario receptor) { this.receptorId = receptor; }

    // hashCode, equals y toString (basados en idMensaje)
    @Override
    public int hashCode() {
        return (idMensaje != null ? idMensaje.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MensajePrivado)) return false;
        MensajePrivado other = (MensajePrivado) object;
        return !((this.idMensaje == null && other.idMensaje != null) || 
                 (this.idMensaje != null && !this.idMensaje.equals(other.idMensaje)));
    }
}