package com.playrole.chat.service;

import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CanalPermissionService {

    private static final Map<RolCanal, Set<PermisoCanal>> ROL_PERMISOS = Map.of(
            RolCanal.OWNER, EnumSet.allOf(PermisoCanal.class),
            RolCanal.ADMIN, EnumSet.of(
                    PermisoCanal.LEER_MENSAJES, PermisoCanal.ENVIAR_MENSAJES,
                    PermisoCanal.EDITAR_MENSAJES_PROPIOS, PermisoCanal.ELIMINAR_MENSAJES_PROPIOS,
                    PermisoCanal.ELIMINAR_MENSAJES_AJENOS, PermisoCanal.GESTIONAR_MIEMBROS,
                    PermisoCanal.GESTIONAR_ROLES, PermisoCanal.SILENCIAR),
            RolCanal.MOD, EnumSet.of(
                    PermisoCanal.LEER_MENSAJES, PermisoCanal.ENVIAR_MENSAJES,
                    PermisoCanal.EDITAR_MENSAJES_PROPIOS, PermisoCanal.ELIMINAR_MENSAJES_PROPIOS,
                    PermisoCanal.ELIMINAR_MENSAJES_AJENOS, PermisoCanal.SILENCIAR),
            RolCanal.MEMBER, EnumSet.of(
                    PermisoCanal.LEER_MENSAJES, PermisoCanal.ENVIAR_MENSAJES,
                    PermisoCanal.EDITAR_MENSAJES_PROPIOS, PermisoCanal.ELIMINAR_MENSAJES_PROPIOS));

    private final MiembroCanalRepository miembroRepository;

    public CanalPermissionService(MiembroCanalRepository miembroRepository) {
        this.miembroRepository = miembroRepository;
    }

    public boolean tienePermiso(Integer canalId, Integer personajeId, PermisoCanal permiso) {
        Optional<RolCanal> rol = miembroRepository.findRolByCanalAndPersonaje(canalId, personajeId);
        return rol.map(r -> ROL_PERMISOS.getOrDefault(r, EnumSet.noneOf(PermisoCanal.class)).contains(permiso))
                  .orElse(false);
    }

    public void verificarPermiso(Integer canalId, Integer personajeId, PermisoCanal permiso) {
        if (!tienePermiso(canalId, personajeId, permiso)) {
            throw new AccessDeniedException("No tienes permiso para realizar esta acción en el canal");
        }
    }

    public boolean esMiembro(Integer canalId, Integer personajeId) {
        return miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(canalId, personajeId);
    }

    public Optional<RolCanal> obtenerRol(Integer canalId, Integer personajeId) {
        return miembroRepository.findRolByCanalAndPersonaje(canalId, personajeId);
    }
}
