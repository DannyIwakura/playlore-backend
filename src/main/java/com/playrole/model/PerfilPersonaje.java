package com.playrole.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "perfiles_personajes")
@NamedQueries({
    @NamedQuery(name = "PerfilPersonaje.findAll", query = "SELECT p FROM PerfilPersonaje p")})
public class PerfilPersonaje implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_personaje")
    private Integer idPersonaje;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "edad_personaje")
    private Integer edadPersonaje;
    @Column(name = "avatar")
    private String avatar;
    @Column(name = "estado")
    private Integer estado;
    @Column(name = "trasfondo")
    private String trasfondo;
    @Column(name = "raza")
    private String raza;
    @Column(name = "clase")
    private String clase;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.DATE)
    private Date fechaCreacion;
    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.DATE)
    private Date fechaModificacion;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Usuario userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idPersonaje", fetch = FetchType.EAGER)
    private List<PersonajeCategoria> personajeCategoriaList;

    public PerfilPersonaje() {
    }

    public PerfilPersonaje(Integer idPersonaje) {
        this.idPersonaje = idPersonaje;
    }

    public PerfilPersonaje(Integer idPersonaje, String nombre) {
        this.idPersonaje = idPersonaje;
        this.nombre = nombre;
    }

    public Integer getIdPersonaje() {
        return idPersonaje;
    }

    public void setIdPersonaje(Integer idPersonaje) {
        this.idPersonaje = idPersonaje;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEdadPersonaje() {
        return edadPersonaje;
    }

    public void setEdadPersonaje(Integer edadPersonaje) {
        this.edadPersonaje = edadPersonaje;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public String getTrasfondo() {
        return trasfondo;
    }

    public void setTrasfondo(String trasfondo) {
        this.trasfondo = trasfondo;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Usuario getUserId() {
        return userId;
    }

    public void setUserId(Usuario userId) {
        this.userId = userId;
    }

    public List<PersonajeCategoria> getPersonajeCategoriaList() {
        return personajeCategoriaList;
    }

    public void setPersonajeCategoriaList(List<PersonajeCategoria> personajeCategoriaList) {
        this.personajeCategoriaList = personajeCategoriaList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPersonaje != null ? idPersonaje.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PerfilPersonaje)) {
            return false;
        }
        PerfilPersonaje other = (PerfilPersonaje) object;
        if ((this.idPersonaje == null && other.idPersonaje != null) || (this.idPersonaje != null && !this.idPersonaje.equals(other.idPersonaje))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "joptionpane.dbplaylore.PerfilPersonaje[ idPersonaje=" + idPersonaje + " ]";
    }
    
}