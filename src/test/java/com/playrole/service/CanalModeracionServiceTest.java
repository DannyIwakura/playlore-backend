package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.chat.service.CanalService;
import com.playrole.dto.EliminarCanalModeracionDTO;
import com.playrole.dto.RestaurarCanalDTO;
import com.playrole.dto.SuspenderCanalDTO;
import com.playrole.enums.AccionModeracion;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.BadRequestException;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;

class CanalModeracionServiceTest {

    private CanalRepository canalRepository;
    private MiembroCanalRepository miembroRepository;
    private CanalService canalService;
    private AuditoriaModeracionService auditoriaService;
    private SimpMessagingTemplate messagingTemplate;
    private CanalModeracionService service;

    @BeforeEach
    void setUp() {
        canalRepository = mock(CanalRepository.class);
        miembroRepository = mock(MiembroCanalRepository.class);
        canalService = mock(CanalService.class);
        auditoriaService = mock(AuditoriaModeracionService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        service = new CanalModeracionService(canalRepository, miembroRepository,
                canalService, auditoriaService, messagingTemplate);

        when(canalRepository.save(any(Canal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario moderador(int id) {
        Usuario u = new Usuario(id);
        u.setNombre("Moderador" + id);
        u.setRol(RolUsuario.MOD);
        return u;
    }

    private Canal canal(int id) {
        Canal c = new Canal();
        c.setIdCanal(id);
        c.setNombre("Canal" + id);
        return c;
    }

    private MiembroCanal miembro(int personajeId) {
        PerfilPersonaje p = new PerfilPersonaje();
        p.setIdPersonaje(personajeId);
        MiembroCanal m = new MiembroCanal();
        m.setPersonaje(p);
        return m;
    }

    private SuspenderCanalDTO suspension(String duracion, String motivo) {
        SuspenderCanalDTO dto = new SuspenderCanalDTO();
        dto.setDuracion(duracion);
        dto.setMotivo(motivo);
        return dto;
    }

    @Test
    void suspender_marcaCanalNotificaYAudita() {
        Canal canal = canal(1);
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));
        when(miembroRepository.findByCanalIdCanal(1)).thenReturn(List.of(miembro(10), miembro(11)));
        when(miembroRepository.countByCanalId(1)).thenReturn(2L);

        Usuario mod = moderador(7);
        var dto = service.suspender(1, suspension("24H", "Contenido ilegal"), mod);

        assertTrue(canal.isSuspendido());
        assertNotNull(canal.getSuspendidoHasta());
        assertEquals(7, canal.getSuspendidoPorId());
        assertEquals("Moderador7", canal.getSuspendidoPorNombre());
        assertEquals("Contenido ilegal", canal.getMotivoSuspension());
        assertTrue(dto.isSuspendidoActivo());
        assertEquals(2, dto.getMiembroCount());

        verify(messagingTemplate).convertAndSend(eq("/topic/privado.10"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/privado.11"), any(Object.class));
        verify(auditoriaService).registrar(eq(AccionModeracion.CANAL_SUSPENDIDO), eq(mod),
                eq(canal), eq("Contenido ilegal"), anyString());
    }

    @Test
    void suspender_indefinida_noFijaVencimiento() {
        Canal canal = canal(1);
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));
        when(miembroRepository.findByCanalIdCanal(1)).thenReturn(List.of());

        service.suspender(1, suspension("INDEFINIDA", "Revisión"), moderador(7));

        assertTrue(canal.isSuspendido());
        assertNull(canal.getSuspendidoHasta());
        assertTrue(canal.isSuspendidoActivo());
    }

    @Test
    void suspender_sinMotivo_lanzaBadRequest() {
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal(1)));

        assertThrows(BadRequestException.class,
                () -> service.suspender(1, suspension("24H", "   "), moderador(7)));
    }

    @Test
    void suspender_canalYaSuspendido_lanzaBadRequest() {
        Canal canal = canal(1);
        canal.setSuspendido(true);
        canal.setSuspendidoHasta(new Date(System.currentTimeMillis() + 60_000));
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));

        assertThrows(BadRequestException.class,
                () -> service.suspender(1, suspension("24H", "Spam"), moderador(7)));
    }

    @Test
    void suspender_duracionInvalida_lanzaBadRequest() {
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal(1)));

        assertThrows(BadRequestException.class,
                () -> service.suspender(1, suspension("9999H", "Spam"), moderador(7)));
    }

    @Test
    void restaurar_limpiaSuspensionNotificaYAudita() {
        Canal canal = canal(1);
        canal.setSuspendido(true);
        canal.setSuspendidoHasta(new Date(System.currentTimeMillis() + 60_000));
        canal.setFechaSuspension(new Date());
        canal.setSuspendidoPorId(7);
        canal.setSuspendidoPorNombre("Moderador7");
        canal.setMotivoSuspension("Spam");
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));
        when(miembroRepository.findByCanalIdCanal(1)).thenReturn(List.of(miembro(10)));

        RestaurarCanalDTO dto = new RestaurarCanalDTO();
        dto.setMotivo("Revisado");
        Usuario mod = moderador(8);

        service.restaurar(1, dto, mod);

        assertFalse(canal.isSuspendido());
        assertNull(canal.getSuspendidoHasta());
        assertNull(canal.getFechaSuspension());
        assertNull(canal.getSuspendidoPorId());
        assertNull(canal.getSuspendidoPorNombre());
        assertNull(canal.getMotivoSuspension());

        verify(messagingTemplate).convertAndSend(eq("/topic/privado.10"), any(Object.class));
        verify(auditoriaService).registrar(eq(AccionModeracion.CANAL_RESTAURADO), eq(mod),
                eq(canal), eq("Revisado"), anyString());
    }

    @Test
    void restaurar_canalNoSuspendido_lanzaBadRequest() {
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal(1)));

        assertThrows(BadRequestException.class,
                () -> service.restaurar(1, new RestaurarCanalDTO(), moderador(7)));
    }

    @Test
    void eliminar_borraYNotificaMiembros() {
        Canal canal = canal(1);
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));
        when(canalService.borrarCanalFisico(canal)).thenReturn(List.of(10, 11));

        EliminarCanalModeracionDTO dto = new EliminarCanalModeracionDTO();
        dto.setMotivo("Contenido ilegal");
        Usuario admin = moderador(1);

        service.eliminar(1, dto, admin);

        verify(canalService).borrarCanalFisico(canal);
        verify(messagingTemplate).convertAndSend(eq("/topic/privado.10"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/privado.11"), any(Object.class));
        verify(auditoriaService).registrar(eq(AccionModeracion.CANAL_ELIMINADO), eq(admin),
                eq(canal), eq("Contenido ilegal"), anyString());
    }

    @Test
    void eliminar_sinMotivo_noBorraNada() {
        Canal canal = canal(1);
        when(canalRepository.findById(1)).thenReturn(Optional.of(canal));

        assertThrows(BadRequestException.class,
                () -> service.eliminar(1, new EliminarCanalModeracionDTO(), moderador(1)));

        verify(canalService, never()).borrarCanalFisico(any());
    }

    @Test
    void restaurarSuspensionesExpiradas_restauraYAuditaComoSistema() {
        Canal canal = canal(1);
        canal.setSuspendido(true);
        canal.setSuspendidoHasta(new Date(System.currentTimeMillis() - 60_000));
        canal.setMotivoSuspension("Spam");
        when(canalRepository.findBySuspendidoTrueAndSuspendidoHastaBefore(any(Date.class)))
                .thenReturn(List.of(canal));
        when(miembroRepository.findByCanalIdCanal(1)).thenReturn(List.of());

        service.restaurarSuspensionesExpiradas();

        assertFalse(canal.isSuspendido());
        assertNull(canal.getSuspendidoHasta());
        verify(auditoriaService).registrar(eq(AccionModeracion.CANAL_RESTAURADO), isNull(),
                eq(canal), eq("Suspensión expirada"), anyString());
    }

    @Test
    void listar_calculaContadoresEnLote() {
        when(canalRepository.findAllByOrderByFechaCreacionDesc())
                .thenReturn(List.of(canal(1), canal(2)));

        MiembroCanalRepository.CountByCanal conteo = mock(MiembroCanalRepository.CountByCanal.class);
        when(conteo.getCanalId()).thenReturn(1);
        when(conteo.getTotal()).thenReturn(5L);
        when(miembroRepository.countByCanalIn(any())).thenReturn(List.of(conteo));

        var lista = service.listar();

        assertEquals(2, lista.size());
        assertEquals(5, lista.get(0).getMiembroCount());
        assertEquals(0, lista.get(1).getMiembroCount());
    }
}
