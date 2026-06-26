package com.playrole.chat.auth;

import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import java.io.Serializable;

public class CharacterSessionPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Usuario usuario;
    private final PerfilPersonaje personaje;
    private final Integer sesionId;
    private final String tokenJwt;

    public CharacterSessionPrincipal(Usuario usuario, PerfilPersonaje personaje, Integer sesionId, String tokenJwt) {
        this.usuario = usuario;
        this.personaje = personaje;
        this.sesionId = sesionId;
        this.tokenJwt = tokenJwt;
    }

    public Usuario getUsuario() { return usuario; }
    public PerfilPersonaje getPersonaje() { return personaje; }
    public Integer getSesionId() { return sesionId; }
    public String getTokenJwt() { return tokenJwt; }

    public Integer getUsuarioId() { return usuario.getUserId(); }
    public Integer getPersonajeId() { return personaje.getIdPersonaje(); }
    public String getPersonajeNombre() { return personaje.getNombre(); }
    public String getPersonajeAvatar() { return personaje.getAvatar(); }
    public String getUsuarioRole() { return usuario.getRol().name(); }
}
