package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MensajeCanal;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.dto.CrearDenunciaDTO;
import com.playrole.enums.EstadoDenuncia;
import com.playrole.enums.TipoDenuncia;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.Denuncia;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.DenunciaRepository;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;

class DenunciaServiceTest {

    private DenunciaRepository denunciaRepository;
    private MensajeCanalRepository mensajeCanalRepository;
    private PerfilPersonajeRepositoryInterface personajeRepository;
    private UsuarioRepositoryInterface usuarioRepository;
    private CanalRepository canalRepository;
    private DenunciaService service;

    @BeforeEach
    void setUp() {
        denunciaRepository = mock(DenunciaRepository.class);
        mensajeCanalRepository = mock(MensajeCanalRepository.class);
        personajeRepository = mock(PerfilPersonajeRepositoryInterface.class);
        usuarioRepository = mock(UsuarioRepositoryInterface.class);
        canalRepository = mock(CanalRepository.class);
        service = new DenunciaService(denunciaRepository, mensajeCanalRepository,
                personajeRepository, usuarioRepository, canalRepository);
    }

    private CrearDenunciaDTO dto(TipoDenuncia tipo, Integer tipoId) {
        CrearDenunciaDTO dto = new CrearDenunciaDTO();
        dto.setTipo(tipo);
        dto.setTipoId(tipoId);
        dto.setMotivo("Spam");
        return dto;
    }

    private Usuario usuario(int id) {
        Usuario u = new Usuario(id);
        u.setNombre("Usuario" + id);
        return u;
    }

    private PerfilPersonaje personaje(int id, String nombre, Usuario dueno) {
        PerfilPersonaje p = new PerfilPersonaje();
        p.setIdPersonaje(id);
        p.setNombre(nombre);
        p.setUserId(dueno);
        return p;
    }

    private Canal canal(int id, String nombre) {
        Canal c = new Canal(id);
        c.setNombre(nombre);
        return c;
    }

    private void stubSaveDevuelveDenuncia() {
        when(denunciaRepository.save(any(Denuncia.class)))
                .thenAnswer(inv -> {
                    Denuncia d = inv.getArgument(0);
                    d.setIdDenuncia(100);
                    return d;
                });
    }

    @Test
    void crearDenuncia_sinTipo_lanzaBadRequest() {
        assertThrows(BadRequestException.class, () -> service.crearDenuncia(dto(null, 1), usuario(1), null));
    }

    @Test
    void crearDenuncia_sinTipoId_lanzaBadRequest() {
        assertThrows(BadRequestException.class, () -> service.crearDenuncia(dto(TipoDenuncia.USUARIO, null), usuario(1), null));
    }

    @Test
    void crearDenuncia_denunciarseASiMismo_lanzaBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.USUARIO, 1), usuario(1), null));
    }

    @Test
    void crearDenuncia_propioPersonaje_lanzaBadRequest() {
        Usuario denunciante = usuario(1);
        PerfilPersonaje propio = personaje(5, "Kael", denunciante);

        assertThrows(BadRequestException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.PERSONAJE, 5), denunciante, propio));
    }

    @Test
    void crearDenuncia_propioCanalPorPersonaje_lanzaBadRequest() {
        Usuario denunciante = usuario(1);
        PerfilPersonaje propio = personaje(5, "Kael", denunciante);
        Canal canal = canal(7, "Sala");
        canal.setCreador(propio);
        when(canalRepository.findById(7)).thenReturn(Optional.of(canal));

        assertThrows(BadRequestException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.CANAL, 7), denunciante, propio));
    }

    @Test
    void crearDenuncia_propioCanalCreadoPorUsuario_lanzaBadRequest() {
        Usuario denunciante = usuario(1);
        Canal canal = canal(7, "Sala");
        canal.setCreadoPorUsuario(denunciante);
        when(canalRepository.findById(7)).thenReturn(Optional.of(canal));

        assertThrows(BadRequestException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.CANAL, 7), denunciante, null));
    }

    @Test
    void crearDenuncia_canalNoEncontrado_lanzaResourceNotFound() {
        when(canalRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.CANAL, 99), usuario(1), null));
    }

    @Test
    void crearDenuncia_duplicadaPendiente_lanzaBadRequest() {
        when(denunciaRepository.existsByTipoAndTipoIdAndDenuncianteUserIdAndEstado(
                TipoDenuncia.USUARIO, 3, 1, EstadoDenuncia.PENDIENTE)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.USUARIO, 3), usuario(1), null));
    }

    @Test
    void crearDenuncia_mensajeNoEncontrado_lanzaResourceNotFound() {
        when(mensajeCanalRepository.findById(55)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.MENSAJE_CANAL, 55), usuario(1), null));
    }

    @Test
    void crearDenuncia_propioMensaje_lanzaBadRequest() {
        Usuario denunciante = usuario(1);
        PerfilPersonaje autorPersonaje = personaje(5, "Kael", denunciante);
        MensajeCanal mensaje = new MensajeCanal();
        mensaje.setCanal(canal(7, "Sala"));
        mensaje.setPersonaje(autorPersonaje);
        when(mensajeCanalRepository.findById(55)).thenReturn(Optional.of(mensaje));

        assertThrows(BadRequestException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.MENSAJE_CANAL, 55), denunciante, autorPersonaje));
    }

    @Test
    void crearDenuncia_mensajeOk_guardaSnapshotYSanitiza() {
        stubSaveDevuelveDenuncia();
        Usuario denunciante = usuario(1);
        Usuario autor = usuario(2);
        PerfilPersonaje autorPersonaje = personaje(5, "Kael", autor);
        Canal canal = canal(7, "Sala");
        MensajeCanal mensaje = new MensajeCanal();
        mensaje.setCanal(canal);
        mensaje.setPersonaje(autorPersonaje);
        mensaje.setContenido("Hola <b>amigo</b>");
        when(mensajeCanalRepository.findById(55)).thenReturn(Optional.of(mensaje));

        CrearDenunciaDTO dto = dto(TipoDenuncia.MENSAJE_CANAL, 55);
        dto.setDetalle("<script>alert(1)</script>detalle");
        var res = service.crearDenuncia(dto, denunciante, null);

        assertEquals(100, res.getIdDenuncia());
        assertEquals("Hola <b>amigo</b>", res.getContenidoDenunciado());
        assertEquals("Sala", res.getCanalNombre());
        assertEquals("Kael", res.getAutorPersonajeNombre());
        assertEquals(2, res.getAutorUsuarioId());
        assertEquals("Kael", res.getObjetivoNombre());

        ArgumentCaptor<Denuncia> captor = ArgumentCaptor.forClass(Denuncia.class);
        verify(denunciaRepository).save(captor.capture());
        Denuncia guardada = captor.getValue();
        assertNotNull(guardada.getDetalle());
        org.junit.jupiter.api.Assertions.assertFalse(guardada.getDetalle().contains("script"));
    }

    @Test
    void crearDenuncia_usuarioOk() {
        stubSaveDevuelveDenuncia();
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario(3)));

        var res = service.crearDenuncia(dto(TipoDenuncia.USUARIO, 3), usuario(1), null);

        assertEquals(TipoDenuncia.USUARIO, res.getTipo());
        assertEquals("Usuario3", res.getObjetivoNombre());
    }

    @Test
    void crearDenuncia_personajeOk() {
        stubSaveDevuelveDenuncia();
        Usuario dueno = usuario(2);
        when(personajeRepository.findById(5)).thenReturn(Optional.of(personaje(5, "Kael", dueno)));

        var res = service.crearDenuncia(dto(TipoDenuncia.PERSONAJE, 5), usuario(1), null);

        assertEquals(TipoDenuncia.PERSONAJE, res.getTipo());
        assertEquals("Kael", res.getObjetivoNombre());
    }

    @Test
    void crearDenuncia_personajeNoEncontrado_lanzaResourceNotFound() {
        when(personajeRepository.findById(5)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.crearDenuncia(dto(TipoDenuncia.PERSONAJE, 5), usuario(1), null));
    }

    @Test
    void resolverDenuncia_ok_actualizaEstadoYResolutor() {
        stubSaveDevuelveDenuncia();
        Denuncia denuncia = new Denuncia();
        denuncia.setIdDenuncia(1);
        denuncia.setTipo(TipoDenuncia.USUARIO);
        denuncia.setTipoId(3);
        when(denunciaRepository.findById(1)).thenReturn(Optional.of(denuncia));
        Usuario admin = usuario(9);
        admin.setNombre("Mod");

        var res = service.resolverDenuncia(1, EstadoDenuncia.RESUELTA, "Se confirma", admin);

        assertEquals(EstadoDenuncia.RESUELTA, res.getEstado());
        assertEquals("Se confirma", res.getDecision());
        assertEquals("Mod", res.getResueltoPorNombre());
        assertNotNull(res.getFechaResolucion());
    }

    @Test
    void resolverDenuncia_noEncontrada_lanzaResourceNotFound() {
        when(denunciaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.resolverDenuncia(1, EstadoDenuncia.RESUELTA, "ok", usuario(9)));
    }

    @Test
    void listarDenuncias_enriqueceSnapshotsEnLoteSinNmasUno() {
        Denuncia d1 = new Denuncia();
        d1.setIdDenuncia(1);
        d1.setTipo(TipoDenuncia.CANAL);
        d1.setCanalId(10);
        d1.setEstado(EstadoDenuncia.PENDIENTE);

        Denuncia d2 = new Denuncia();
        d2.setIdDenuncia(2);
        d2.setTipo(TipoDenuncia.PERSONAJE);
        d2.setTipoId(20);
        d2.setEstado(EstadoDenuncia.PENDIENTE);

        Denuncia d3 = new Denuncia();
        d3.setIdDenuncia(3);
        d3.setTipo(TipoDenuncia.USUARIO);
        d3.setTipoId(30);
        d3.setEstado(EstadoDenuncia.PENDIENTE);

        Pageable pageable = PageRequest.of(0, 10);
        when(denunciaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(d1, d2, d3), pageable, 3));

        Canal sala = canal(10, "Sala");
        when(canalRepository.findAllById(anyCollection())).thenReturn(List.of(sala));
        Usuario dueno = usuario(99);
        when(personajeRepository.findAllById(anyCollection()))
                .thenReturn(List.of(personaje(20, "Kael", dueno)));
        Usuario nova = usuario(30);
        nova.setNombre("Nova");
        when(usuarioRepository.findAllById(anyCollection())).thenReturn(List.of(nova));

        var pagina = service.listarDenuncias(EstadoDenuncia.PENDIENTE, 0, 10);

        assertEquals(3, pagina.getTotalElements());
        assertEquals("Sala", pagina.getContent().get(0).getCanalNombre());
        assertEquals("Sala", pagina.getContent().get(0).getObjetivoNombre());
        assertEquals("Kael", pagina.getContent().get(1).getObjetivoNombre());
        assertEquals("Nova", pagina.getContent().get(2).getObjetivoNombre());

        verify(canalRepository, atLeastOnce()).findAllById(anyCollection());
        verify(personajeRepository).findAllById(anyCollection());
        verify(usuarioRepository).findAllById(anyCollection());
    }

    @Test
    void listarDenuncias_conSnapshotsCompletos_noConsultaRepositorios() {
        Denuncia d1 = new Denuncia();
        d1.setIdDenuncia(1);
        d1.setTipo(TipoDenuncia.CANAL);
        d1.setCanalId(10);
        d1.setCanalNombre("Sala");
        d1.setObjetivoNombre("Sala");
        d1.setEstado(EstadoDenuncia.PENDIENTE);

        Pageable pageable = PageRequest.of(0, 10);
        when(denunciaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(d1), pageable, 1));

        var pagina = service.listarDenuncias(EstadoDenuncia.PENDIENTE, 0, 10);

        assertEquals("Sala", pagina.getContent().get(0).getCanalNombre());
        verify(denunciaRepository, org.mockito.Mockito.never()).findById(any());
    }
}
