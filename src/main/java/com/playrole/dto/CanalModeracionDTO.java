package com.playrole.dto;

import com.playrole.chat.model.Canal;

import java.util.Date;

public class CanalModeracionDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private boolean privado;
    private boolean visible;
    private String imagenUrl;
    private Date fechaCreacion;
    private Integer creadorId;
    private String creadorNombre;
    private Integer creadoPorUsuarioId;
    private String creadoPorUsuarioNombre;
    private Integer miembroCount;
    private boolean suspendido;
    private boolean suspendidoActivo;
    private Date suspendidoHasta;
    private Date fechaSuspension;
    private Integer suspendidoPorId;
    private String suspendidoPorNombre;
    private String motivoSuspension;

    public static CanalModeracionDTO fromEntity(Canal canal, long miembroCount) {
        CanalModeracionDTO dto = new CanalModeracionDTO();
        dto.setId(canal.getIdCanal());
        dto.setNombre(canal.getNombre());
        dto.setDescripcion(canal.getDescripcion());
        dto.setTipo(canal.getTipo() != null ? canal.getTipo().name() : null);
        dto.setPrivado(canal.isPrivado());
        dto.setVisible(canal.isVisible());
        dto.setImagenUrl(canal.getImagenUrl());
        dto.setFechaCreacion(canal.getFechaCreacion());
        dto.setMiembroCount((int) miembroCount);
        if (canal.getCreador() != null) {
            dto.setCreadorId(canal.getCreador().getIdPersonaje());
            dto.setCreadorNombre(canal.getCreador().getNombre());
        }
        if (canal.getCreadoPorUsuario() != null) {
            dto.setCreadoPorUsuarioId(canal.getCreadoPorUsuario().getUserId());
            dto.setCreadoPorUsuarioNombre(canal.getCreadoPorUsuario().getNombre());
        }
        dto.setSuspendido(canal.isSuspendido());
        dto.setSuspendidoActivo(canal.isSuspendidoActivo());
        dto.setSuspendidoHasta(canal.getSuspendidoHasta());
        dto.setFechaSuspension(canal.getFechaSuspension());
        dto.setSuspendidoPorId(canal.getSuspendidoPorId());
        dto.setSuspendidoPorNombre(canal.getSuspendidoPorNombre());
        dto.setMotivoSuspension(canal.getMotivoSuspension());
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isPrivado() { return privado; }
    public void setPrivado(boolean privado) { this.privado = privado; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getCreadorId() { return creadorId; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }

    public String getCreadorNombre() { return creadorNombre; }
    public void setCreadorNombre(String creadorNombre) { this.creadorNombre = creadorNombre; }

    public Integer getCreadoPorUsuarioId() { return creadoPorUsuarioId; }
    public void setCreadoPorUsuarioId(Integer creadoPorUsuarioId) { this.creadoPorUsuarioId = creadoPorUsuarioId; }

    public String getCreadoPorUsuarioNombre() { return creadoPorUsuarioNombre; }
    public void setCreadoPorUsuarioNombre(String creadoPorUsuarioNombre) { this.creadoPorUsuarioNombre = creadoPorUsuarioNombre; }

    public Integer getMiembroCount() { return miembroCount; }
    public void setMiembroCount(Integer miembroCount) { this.miembroCount = miembroCount; }

    public boolean isSuspendido() { return suspendido; }
    public void setSuspendido(boolean suspendido) { this.suspendido = suspendido; }

    public boolean isSuspendidoActivo() { return suspendidoActivo; }
    public void setSuspendidoActivo(boolean suspendidoActivo) { this.suspendidoActivo = suspendidoActivo; }

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
}
