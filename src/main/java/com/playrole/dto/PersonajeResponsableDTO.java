package com.playrole.dto;

import com.playrole.model.PerfilPersonaje;

/**
 * Personaje elegible como responsable de un canal oficial.
 * Solo se exponen personajes de cuentas con rol ADMIN o MOD.
 */
public class PersonajeResponsableDTO {

    private Integer idPersonaje;
    private String nombre;
    private String avatar;
    private Integer userId;
    private String usuarioNombre;

    public static PersonajeResponsableDTO fromEntity(PerfilPersonaje p) {
        PersonajeResponsableDTO dto = new PersonajeResponsableDTO();
        dto.setIdPersonaje(p.getIdPersonaje());
        dto.setNombre(p.getNombre());
        dto.setAvatar(p.getAvatar());
        if (p.getUserId() != null) {
            dto.setUserId(p.getUserId().getUserId());
            dto.setUsuarioNombre(p.getUserId().getNombre());
        }
        return dto;
    }

    public Integer getIdPersonaje() { return idPersonaje; }
    public void setIdPersonaje(Integer idPersonaje) { this.idPersonaje = idPersonaje; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
}
