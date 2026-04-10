package com.playrole.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.playrole.enums.RolUsuario;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "usuarios")
@NamedQueries({
    @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM Usuario u")})
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    private Integer userId;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "avatar")
    private String avatar;
    @Basic(optional = false)
    @Column(name = "password")
    private String password;
    @Basic(optional = false)
    @Column(name = "email")
    private String email;
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private RolUsuario rol;
    @Basic(optional = false)
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.DATE)
    private Date fechaRegistro;
    @Temporal(TemporalType.TIMESTAMP)
    @Basic(optional = false)
    @Column(name = "ultima_conexion")
    private Date ultimaConexion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId", fetch = FetchType.LAZY)
    private List<PerfilPersonaje> perfilPersonajeList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "emisorId", fetch = FetchType.EAGER)
    private List<SolicitudAmistad> solicitudAmistadList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receptorId", fetch = FetchType.EAGER)
    private List<SolicitudAmistad> solicitudAmistadList1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "emisorId", fetch = FetchType.EAGER)
    private List<MensajePrivado> mensajePrivadoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receptorId", fetch = FetchType.EAGER)
    private List<MensajePrivado> mensajePrivadoList1;

    public Usuario() {
    }

    public Usuario(Integer userId) {
        this.userId = userId;
    }

    public Usuario(Integer userId, String nombre, String avatar, String password, String email, RolUsuario rol, Date fechaRegistro,
    		Date ultimaConexion) {
        this.userId = userId;
        this.nombre = nombre;
        this.avatar = avatar;
        this.password = password;
        this.email = email;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.ultimaConexion = ultimaConexion;
    }

	public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public Date getUltimaConexion() {
		return ultimaConexion;
	}

	public void setUltimaConexion(Date ultimaConexion) {
		this.ultimaConexion = ultimaConexion;
	}

    public List<PerfilPersonaje> getPerfilPersonajeList() {
        return perfilPersonajeList;
    }

    public void setPerfilPersonajeList(List<PerfilPersonaje> perfilPersonajeList) {
        this.perfilPersonajeList = perfilPersonajeList;
    }

    public List<SolicitudAmistad> getSolicitudAmistadList() {
        return solicitudAmistadList;
    }

    public void setSolicitudAmistadList(List<SolicitudAmistad> solicitudAmistadList) {
        this.solicitudAmistadList = solicitudAmistadList;
    }

    public List<SolicitudAmistad> getSolicitudAmistadList1() {
        return solicitudAmistadList1;
    }

    public void setSolicitudAmistadList1(List<SolicitudAmistad> solicitudAmistadList1) {
        this.solicitudAmistadList1 = solicitudAmistadList1;
    }

    public List<MensajePrivado> getMensajePrivadoList() {
        return mensajePrivadoList;
    }

    public void setMensajePrivadoList(List<MensajePrivado> mensajePrivadoList) {
        this.mensajePrivadoList = mensajePrivadoList;
    }

    public List<MensajePrivado> getMensajePrivadoList1() {
        return mensajePrivadoList1;
    }

    public void setMensajePrivadoList1(List<MensajePrivado> mensajePrivadoList1) {
        this.mensajePrivadoList1 = mensajePrivadoList1;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "joptionpane.dbplaylore.Usuario[ userId=" + userId + " ]";
    }
    
}
