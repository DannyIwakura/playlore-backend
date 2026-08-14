package com.playrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.playrole.dto.PersonajeCategoriaDTO;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.PersonajeCategoria;
import com.playrole.model.Usuario;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;

class PersonajeCategoriaServiceImplTest {

    private PersonajeCategoriaRepositoryInterface repository;
    private ICategoriaService categoriaService;
    private IPerfilPersonajeService personajeService;
    private PersonajeCategoriaServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PersonajeCategoriaRepositoryInterface.class);
        categoriaService = mock(ICategoriaService.class);
        personajeService = mock(IPerfilPersonajeService.class);
        service = new PersonajeCategoriaServiceImpl(repository, categoriaService, personajeService);
    }

    private Categoria categoria(int id) {
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setNombre("Fantasia");
        return c;
    }

    private PerfilPersonaje personaje(int id) {
        PerfilPersonaje p = new PerfilPersonaje();
        p.setIdPersonaje(id);
        p.setNombre("Kael");
        p.setUserId(new Usuario(1));
        return p;
    }

    private PersonajeCategoriaDTO dto(Integer idCategoria, Integer idPersonaje) {
        PersonajeCategoriaDTO dto = new PersonajeCategoriaDTO();
        dto.setIdCategoria(idCategoria);
        dto.setIdPersonaje(idPersonaje);
        return dto;
    }

    @Test
    void crear_categoriaNoEncontrada_lanzaResourceNotFound() {
        when(categoriaService.obtenerEntidadPorId(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear(dto(1, 5)));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_personajeNoEncontrado_lanzaResourceNotFound() {
        when(categoriaService.obtenerEntidadPorId(1)).thenReturn(Optional.of(categoria(1)));
        when(personajeService.obtenerEntidadPorId(5)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear(dto(1, 5)));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_ok_guardaRelacionConEntidades() {
        when(categoriaService.obtenerEntidadPorId(1)).thenReturn(Optional.of(categoria(1)));
        when(personajeService.obtenerEntidadPorId(5)).thenReturn(Optional.of(personaje(5)));
        when(repository.save(any(PersonajeCategoria.class)))
                .thenAnswer(inv -> {
                    PersonajeCategoria pc = inv.getArgument(0);
                    pc.setId(7);
                    return pc;
                });

        var res = service.crear(dto(1, 5));

        assertEquals(7, res.getId());
        assertEquals(1, res.getIdCategoria());
        assertEquals(5, res.getIdPersonaje());
    }

    @Test
    void actualizar_relacionNoEncontrada_lanzaResourceNotFound() {
        when(repository.findById(9)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.actualizar(9, dto(1, 5)));
    }

    @Test
    void actualizar_cambiaCategoriaCuandoEsDistinta() {
        PersonajeCategoria existente = new PersonajeCategoria();
        existente.setId(9);
        existente.setIdCategoria(categoria(1));
        existente.setIdPersonaje(personaje(5));
        when(repository.findById(9)).thenReturn(Optional.of(existente));
        when(categoriaService.obtenerEntidadPorId(2)).thenReturn(Optional.of(categoria(2)));
        when(repository.save(any(PersonajeCategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.actualizar(9, dto(2, 5));

        assertEquals(2, res.getIdCategoria());
        verify(personajeService, never()).obtenerEntidadPorId(5);
    }

    @Test
    void actualizar_categoriaNuevaNoExiste_lanzaResourceNotFound() {
        PersonajeCategoria existente = new PersonajeCategoria();
        existente.setId(9);
        existente.setIdCategoria(categoria(1));
        existente.setIdPersonaje(personaje(5));
        when(repository.findById(9)).thenReturn(Optional.of(existente));
        when(categoriaService.obtenerEntidadPorId(2)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.actualizar(9, dto(2, 5)));
    }

    @Test
    void eliminarPorId_noExiste_lanzaResourceNotFound() {
        when(repository.existsById(9)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.eliminarPorId(9));
    }

    @Test
    void eliminarPorId_ok_borra() {
        when(repository.existsById(9)).thenReturn(true);

        service.eliminarPorId(9);

        verify(repository).deleteById(9);
    }

    @Test
    void obtenerPorPersonajeId_mapeaEntidades() {
        PersonajeCategoria pc = new PersonajeCategoria();
        pc.setId(1);
        pc.setIdCategoria(categoria(2));
        pc.setIdPersonaje(personaje(5));
        when(repository.findByIdPersonajeIdPersonaje(5)).thenReturn(List.of(pc));

        var lista = service.obtenerPorPersonajeId(5);

        assertEquals(1, lista.size());
        assertEquals("Fantasia", lista.get(0).getNombreCategoria());
        assertEquals("Kael", lista.get(0).getNombrePersonaje());
    }

    @Test
    void existePorCategoriaId_delegaEnRepositorio() {
        when(repository.existsByIdCategoriaIdCategoria(2)).thenReturn(true);

        assertTrue(service.existePorCategoriaId(2));
        verify(repository).existsByIdCategoriaIdCategoria(2);
    }

    @Test
    void obtenerTodos_mapeaEntidades() {
        when(repository.findAll()).thenReturn(List.of(new PersonajeCategoria()));

        assertEquals(1, service.obtenerTodos().size());
    }
}
