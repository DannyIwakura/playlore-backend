package com.playrole.chat.dto;

import com.playrole.chat.model.Canal;
import com.playrole.chat.enums.TipoCanal;
import java.util.Date;

public class CanalDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private boolean privado;
    private boolean visible;
    private Date fechaCreacion;
    private Integer creadorId;
    private String creadorNombre;
    private String creadorAvatar;
    private Integer miembroCount;
    private String miRol;

    public static CanalDTO fromEntity(Canal canal) {
        CanalDTO dto = new CanalDTO();
        dto.setId(canal.getIdCanal());
        dto.setNombre(canal.getNombre());
        dto.setDescripcion(canal.getDescripcion());
        dto.setTipo(canal.getTipo().name());
        dto.setPrivado(canal.isPrivado());
        dto.setVisible(canal.isVisible());
        dto.setFechaCreacion(canal.getFechaCreacion());
        if (canal.getCreador() != null) {
            dto.setCreadorId(canal.getCreador().getIdPersonaje());
            dto.setCreadorNombre(canal.getCreador().getNombre());
            dto.setCreadorAvatar(canal.getCreador().getAvatar());
        }
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

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getCreadorId() { return creadorId; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }

    public String getCreadorNombre() { return creadorNombre; }
    public void setCreadorNombre(String creadorNombre) { this.creadorNombre = creadorNombre; }

    public String getCreadorAvatar() { return creadorAvatar; }
    public void setCreadorAvatar(String creadorAvatar) { this.creadorAvatar = creadorAvatar; }

    public Integer getMiembroCount() { return miembroCount; }
    public void setMiembroCount(Integer miembroCount) { this.miembroCount = miembroCount; }

    public String getMiRol() { return miRol; }
    public void setMiRol(String miRol) { this.miRol = miRol; }
}
