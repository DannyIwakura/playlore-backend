package com.playrole.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.playrole.chat.enums.PermisoCanal;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MensajeCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;

class CanalMensajeServiceTest {

    private MensajeCanalRepository mensajeRepository;
    private CanalRepository canalRepository;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private CanalPermissionService permissionService;
    private SimpMessagingTemplate messagingTemplate;
    private BaneoGlobalRepository baneoGlobalRepository;
    private MiembroCanalRepository miembroRepository;
    private CanalMensajeService service;

    @BeforeEach
    void setUp() {
        mensajeRepository = mock(MensajeCanalRepository.class);
        canalRepository = mock(CanalRepository.class);
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        permissionService = mock(CanalPermissionService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        baneoGlobalRepository = mock(BaneoGlobalRepository.class);
        miembroRepository = mock(MiembroCanalRepository.class);
        service = new CanalMensajeService(mensajeRepository, canalRepository, personajeRepository,
                permissionService, messagingTemplate, baneoGlobalRepository, miembroRepository);
    }

    private MensajeCanal mensaje(int id, String contenido, PerfilPersonaje autor) {
        Canal canal = new Canal(1);
        canal.setNombre("Sala");
        MensajeCanal m = new MensajeCanal();
        m.setIdMensaje(id);
        m.setCanal(canal);
        m.setPersonaje(autor);
        m.setContenido(contenido);
        m.setFechaEnvio(new Date());
        return m;
    }

    @Test
    void buscarMensajes_escapaComodinesDeLike() {
        PerfilPersonaje autor = new PerfilPersonaje();
        autor.setIdPersonaje(10);
        autor.setNombre("Kael");
        autor.setUserId(new Usuario(1));
        when(mensajeRepository.buscarEnCanal(eq(1), any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mensaje(5, "oferta 100%", autor))));

        var pagina = service.buscarMensajes(1, 10, "100%", 0, 10);

        ArgumentCaptor<String> termino = ArgumentCaptor.forClass(String.class);
        verify(mensajeRepository).buscarEnCanal(eq(1), termino.capture(), any(Pageable.class));
        assertEquals("100!%", termino.getValue());

        assertEquals(1, pagina.getTotalElements());
        assertEquals("Kael", pagina.getContent().get(0).getPersonajeNombre());
        assertTrue(pagina.getContent().get(0).isEsMio());
    }

    @Test
    void buscarMensajes_escapaGuionBajoExclamacionYOrdenaPorFechaDesc() {
        when(mensajeRepository.buscarEnCanal(eq(1), any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.buscarMensajes(1, 10, "50%_off!", 0, 10);

        ArgumentCaptor<String> termino = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(mensajeRepository).buscarEnCanal(eq(1), termino.capture(), pageable.capture());
        assertEquals("50!%!_off!!", termino.getValue());
        assertEquals(Sort.Direction.DESC,
                pageable.getValue().getSort().getOrderFor("fechaEnvio").getDirection());
    }

    @Test
    void buscarMensajes_conQNulaUsaTerminoVacio() {
        when(mensajeRepository.buscarEnCanal(eq(1), any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.buscarMensajes(1, 10, null, 0, 10);

        ArgumentCaptor<String> termino = ArgumentCaptor.forClass(String.class);
        verify(mensajeRepository).buscarEnCanal(eq(1), termino.capture(), any(Pageable.class));
        assertEquals("", termino.getValue());
    }

    @Test
    void buscarMensajes_exigePermisoDeLectura() {
        when(mensajeRepository.buscarEnCanal(eq(1), any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.buscarMensajes(1, 10, "hola", 0, 10);

        verify(permissionService).verificarPermiso(1, 10, PermisoCanal.LEER_MENSAJES);
    }

    @Test
    void obtenerMensajes_delegaConOrdenDesc() {
        PerfilPersonaje autor = new PerfilPersonaje();
        autor.setIdPersonaje(10);
        autor.setNombre("Kael");
        autor.setUserId(new Usuario(1));
        when(mensajeRepository.findMensajesByCanal(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mensaje(5, "hola", autor)), PageRequest.of(0, 50), 1));

        var pagina = service.obtenerMensajes(1, 10, 0, 50);

        assertEquals(1, pagina.getTotalElements());
        assertEquals(5, pagina.getContent().get(0).getId());
    }
}
