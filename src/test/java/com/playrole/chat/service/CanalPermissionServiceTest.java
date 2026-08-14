package com.playrole.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.exception.AccessDeniedException;

class CanalPermissionServiceTest {

    private MiembroCanalRepository miembroRepository;
    private CanalPermissionService service;

    @BeforeEach
    void setUp() {
        miembroRepository = mock(MiembroCanalRepository.class);
        service = new CanalPermissionService(miembroRepository);
    }

    private void stubRol(RolCanal rol) {
        when(miembroRepository.findRolByCanalAndPersonaje(1, 10))
                .thenReturn(Optional.ofNullable(rol));
    }

    @Test
    void owner_tieneTodosLosPermisos() {
        stubRol(RolCanal.OWNER);

        for (PermisoCanal permiso : PermisoCanal.values()) {
            assertTrue(service.tienePermiso(1, 10, permiso));
        }
    }

    @Test
    void admin_gestionaRolesPeroNoEliminaCanal() {
        stubRol(RolCanal.ADMIN);

        assertTrue(service.tienePermiso(1, 10, PermisoCanal.GESTIONAR_ROLES));
        assertTrue(service.tienePermiso(1, 10, PermisoCanal.EDITAR_CANAL));
        assertTrue(service.tienePermiso(1, 10, PermisoCanal.ELIMINAR_MENSAJES_AJENOS));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.ELIMINAR_CANAL));
    }

    @Test
    void mod_silenciaPeroNoGestionaRolesNiEditaCanal() {
        stubRol(RolCanal.MOD);

        assertTrue(service.tienePermiso(1, 10, PermisoCanal.SILENCIAR));
        assertTrue(service.tienePermiso(1, 10, PermisoCanal.ELIMINAR_MENSAJES_AJENOS));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.GESTIONAR_ROLES));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.EDITAR_CANAL));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.ELIMINAR_CANAL));
    }

    @Test
    void member_soloGestionaSusPropiosMensajes() {
        stubRol(RolCanal.MEMBER);

        assertTrue(service.tienePermiso(1, 10, PermisoCanal.LEER_MENSAJES));
        assertTrue(service.tienePermiso(1, 10, PermisoCanal.ENVIAR_MENSAJES));
        assertTrue(service.tienePermiso(1, 10, PermisoCanal.EDITAR_MENSAJES_PROPIOS));
        assertTrue(service.tienePermiso(1, 10, PermisoCanal.ELIMINAR_MENSAJES_PROPIOS));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.ELIMINAR_MENSAJES_AJENOS));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.SILENCIAR));
        assertFalse(service.tienePermiso(1, 10, PermisoCanal.GESTIONAR_MIEMBROS));
    }

    @Test
    void sinRol_noTienePermisos() {
        stubRol(null);

        assertFalse(service.tienePermiso(1, 10, PermisoCanal.LEER_MENSAJES));
    }

    @Test
    void verificarPermiso_sinPermiso_lanzaAccessDenied() {
        stubRol(RolCanal.MEMBER);

        assertThrows(AccessDeniedException.class,
                () -> service.verificarPermiso(1, 10, PermisoCanal.SILENCIAR));
    }

    @Test
    void verificarPermiso_conPermiso_noLanza() {
        stubRol(RolCanal.OWNER);

        service.verificarPermiso(1, 10, PermisoCanal.SILENCIAR);
    }

    @Test
    void esMiembro_delegaEnRepositorio() {
        when(miembroRepository.existsByCanalIdCanalAndPersonajeIdPersonaje(1, 10)).thenReturn(true);

        assertTrue(service.esMiembro(1, 10));
        verify(miembroRepository).existsByCanalIdCanalAndPersonajeIdPersonaje(1, 10);
    }

    @Test
    void obtenerRol_delegaEnRepositorio() {
        stubRol(RolCanal.ADMIN);

        assertEquals(Optional.of(RolCanal.ADMIN), service.obtenerRol(1, 10));
    }
}
