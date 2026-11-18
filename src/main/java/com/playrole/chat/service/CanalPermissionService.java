package com.playrole.chat.service;

import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.ResourceNotFoundException;
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
                    PermisoCanal.GESTIONAR_ROLES, PermisoCanal.SILENCIAR,
                    PermisoCanal.EDITAR_CANAL),
            RolCanal.MOD, EnumSet.of(
                    PermisoCanal.LEER_MENSAJES, PermisoCanal.ENVIAR_MENSAJES,
                    PermisoCanal.EDITAR_MENSAJES_PROPIOS, PermisoCanal.ELIMINAR_MENSAJES_PROPIOS,
                    PermisoCanal.ELIMINAR_MENSAJES_AJENOS, PermisoCanal.GESTIONAR_MIEMBROS,
                    PermisoCanal.SILENCIAR),
            RolCanal.MEMBER, EnumSet.of(
                    PermisoCanal.LEER_MENSAJES, PermisoCanal.ENVIAR_MENSAJES,
                    PermisoCanal.EDITAR_MENSAJES_PROPIOS, PermisoCanal.ELIMINAR_MENSAJES_PROPIOS));

    private final MiembroCanalRepository miembroRepository;
    private final CanalRepository canalRepository;

    public CanalPermissionService(MiembroCanalRepository miembroRepository,
                                  CanalRepository canalRepository) {
        this.miembroRepository = miembroRepository;
        this.canalRepository = canalRepository;
    }

    public boolean tienePermiso(Integer canalId, Integer personajeId, PermisoCanal permiso) {
        Optional<RolCanal> rol = miembroRepository.findRolByCanalAndPersonaje(canalId, personajeId);
        return rol.map(r -> ROL_PERMISOS.getOrDefault(r, EnumSet.noneOf(PermisoCanal.class)).contains(permiso))
                  .orElse(false);
    }

    public void verificarCanalActivo(Integer canalId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));
        if (canal.isSuspendidoActivo()) {
            throw new AccessDeniedException("Este canal está suspendido por moderación"
                    + (canal.getSuspendidoHasta() != null
                        ? " hasta " + canal.getSuspendidoHasta()
                        : ""));
        }
    }

    public void verificarPermiso(Integer canalId, Integer personajeId, PermisoCanal permiso) {
        verificarCanalActivo(canalId);
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
