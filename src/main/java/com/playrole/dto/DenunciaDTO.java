package com.playrole.dto;

import com.playrole.enums.EstadoDenuncia;
import com.playrole.enums.TipoDenuncia;

import java.util.Date;

public class DenunciaDTO {

    private Integer idDenuncia;
    private TipoDenuncia tipo;
    private Integer tipoId;
    private String motivo;
    private String detalle;
    private String contenidoDenunciado;
    private Integer canalId;
    private Integer autorPersonajeId;
    private String autorPersonajeNombre;
    private String objetivoNombre;
    private Integer autorUsuarioId;
    private String autorUsuarioNombre;
    private String canalNombre;
    private EstadoDenuncia estado;
    private Date fecha;
    private Integer denuncianteUserId;
    private String denuncianteNombre;
    private Integer denunciantePersonajeId;
    private String denunciantePersonajeNombre;
    private String resueltoPorNombre;
    private String decision;
    private Date fechaResolucion;

    public Integer getIdDenuncia() { return idDenuncia; }
    public void setIdDenuncia(Integer idDenuncia) { this.idDenuncia = idDenuncia; }

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

    public String getAutorPersonajeNombre() { return autorPersonajeNombre; }
    public void setAutorPersonajeNombre(String autorPersonajeNombre) { this.autorPersonajeNombre = autorPersonajeNombre; }

    public String getObjetivoNombre() { return objetivoNombre; }
    public void setObjetivoNombre(String objetivoNombre) { this.objetivoNombre = objetivoNombre; }

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

    public Integer getDenuncianteUserId() { return denuncianteUserId; }
    public void setDenuncianteUserId(Integer denuncianteUserId) { this.denuncianteUserId = denuncianteUserId; }

    public String getDenuncianteNombre() { return denuncianteNombre; }
    public void setDenuncianteNombre(String denuncianteNombre) { this.denuncianteNombre = denuncianteNombre; }

    public Integer getDenunciantePersonajeId() { return denunciantePersonajeId; }
    public void setDenunciantePersonajeId(Integer denunciantePersonajeId) { this.denunciantePersonajeId = denunciantePersonajeId; }

    public String getDenunciantePersonajeNombre() { return denunciantePersonajeNombre; }
    public void setDenunciantePersonajeNombre(String denunciantePersonajeNombre) { this.denunciantePersonajeNombre = denunciantePersonajeNombre; }

    public String getResueltoPorNombre() { return resueltoPorNombre; }
    public void setResueltoPorNombre(String resueltoPorNombre) { this.resueltoPorNombre = resueltoPorNombre; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
