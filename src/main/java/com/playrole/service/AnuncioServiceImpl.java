package com.playrole.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playrole.chat.auth.CharacterSessionPrincipal;
import com.playrole.chat.service.PresenceService;
import com.playrole.dto.ActualizarAnuncioDTO;
import com.playrole.dto.AnuncioCategoriaDTO;
import com.playrole.dto.AnuncioDTO;
import com.playrole.dto.AnunciosRespuestaDTO;
import com.playrole.dto.CambiarActivoDTO;
import com.playrole.dto.CrearAnuncioDTO;
import com.playrole.exception.AccessDeniedException;
import com.playrole.exception.BadRequestException;
import com.playrole.exception.ResourceNotFoundException;
import com.playrole.model.Anuncio;
import com.playrole.model.AnuncioCategoria;
import com.playrole.model.Categoria;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.AnuncioCategoriaRepository;
import com.playrole.repository.AnuncioRepository;
import com.playrole.repository.CategoríaRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.specification.AnuncioSpec;
import com.playrole.utils.AuthUtils;
import com.playrole.utils.HtmlUtils;

@Service
public class AnuncioServiceImpl implements AnuncioService {

    static final int MAX_ACTIVOS_POR_CUENTA = 3;
    static final int LOTE_POR_DEFECTO = 9;
    static final int LOTE_MAXIMO = 20;
    static final int MAX_EXCLUIDOS = 100;
    private static final int MAX_CONTENIDO = 10_000;

    private final AnuncioRepository anuncioRepository;
    private final AnuncioCategoriaRepository anuncioCategoriaRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final CategoríaRepositoryInterface categoriaRepository;
    private final PresenceService presenceService;

    public AnuncioServiceImpl(AnuncioRepository anuncioRepository,
            AnuncioCategoriaRepository anuncioCategoriaRepository,
            PerfilPersonajeRepositoryInterface personajeRepository,
            CategoríaRepositoryInterface categoriaRepository,
            PresenceService presenceService) {
        this.anuncioRepository = anuncioRepository;
        this.anuncioCategoriaRepository = anuncioCategoriaRepository;
        this.personajeRepository = personajeRepository;
        this.categoriaRepository = categoriaRepository;
        this.presenceService = presenceService;
    }

    @Override
    @Transactional
    public AnuncioDTO crear(CrearAnuncioDTO dto, Authentication authentication) {
        // Resolver (y autorizar) el personaje antes de validar el resto evita
        // filtrar información de categorías a quien no puede publicar allí.
        PerfilPersonaje personaje = resolverPersonaje(dto.getPersonajeId(), authentication);
        String contenido = validarContenido(dto.getContenido());
        List<Categoria> categorias = validarCategorias(dto.getCategoriaIds());

        Anuncio anuncio = new Anuncio();
        anuncio.setContenido(contenido);
        anuncio.setActivo(false); // todo anuncio nace inactivo
        anuncio.setPersonaje(personaje);
        anuncio.setFechaCreacion(new Date());
        anuncio.setFechaModificacion(new Date());

        Anuncio guardado = anuncioRepository.save(anuncio);
        List<AnuncioCategoria> relaciones = guardarCategorias(guardado, categorias);
        return aDTO(guardado, relaciones);
    }

    @Override
    @Transactional
    public AnuncioDTO editar(Integer idAnuncio, ActualizarAnuncioDTO dto, Authentication authentication) {
        Anuncio anuncio = obtenerAnuncio(idAnuncio);
        verificarPropiedad(anuncio.getPersonaje(), authentication);

        String contenido = validarContenido(dto.getContenido());
        List<Categoria> categorias = validarCategorias(dto.getCategoriaIds());

        anuncioCategoriaRepository.deleteByAnuncioId(idAnuncio);
        List<AnuncioCategoria> relaciones = guardarCategorias(anuncio, categorias);
        anuncio.setContenido(contenido);
        anuncio.setFechaModificacion(new Date());

        Anuncio actualizado = anuncioRepository.save(anuncio);
        return aDTO(actualizado, relaciones);
    }

    @Override
    @Transactional
    public AnuncioDTO cambiarActivo(Integer idAnuncio, CambiarActivoDTO dto, Authentication authentication) {
        if (dto.getActivo() == null) {
            throw new BadRequestException("El estado activo es obligatorio");
        }

        Anuncio anuncio = obtenerAnuncio(idAnuncio);
        verificarPropiedad(anuncio.getPersonaje(), authentication);

        boolean destino = dto.getActivo();
        if (anuncio.isActivo() == destino) {
            return aDTO(anuncio, categoriasDe(idAnuncio)); // idempotente
        }

        if (destino) {
            Integer usuarioId = anuncio.getPersonaje().getUserId() != null
                    ? anuncio.getPersonaje().getUserId().getUserId()
                    : null;
            long activos = usuarioId != null ? anuncioRepository.countActivosByUsuario(usuarioId) : 0L;
            if (activos >= MAX_ACTIVOS_POR_CUENTA) {
                throw new BadRequestException("Solo puedes tener 3 anuncios activos por cuenta. "
                        + "Desactiva uno antes de activar este.");
            }
        }

        anuncio.setActivo(destino);
        anuncio.setFechaModificacion(new Date());
        Anuncio actualizado = anuncioRepository.save(anuncio);
        return aDTO(actualizado, categoriasDe(idAnuncio));
    }

    @Override
    @Transactional
    public void eliminar(Integer idAnuncio, Authentication authentication) {
        Anuncio anuncio = obtenerAnuncio(idAnuncio);
        verificarPropiedad(anuncio.getPersonaje(), authentication);

        anuncioCategoriaRepository.deleteByAnuncioId(idAnuncio);
        anuncioRepository.delete(anuncio);
    }

    @Override
    @Transactional(readOnly = true)
    public AnunciosRespuestaDTO listar(Integer usuarioId, String nombre, String genero,
            String raza, String clase, Integer edadMin, Integer edadMax,
            List<Integer> categoriaIds, boolean soloOnline,
            List<Integer> excluirIds, int size) {

        List<Integer> excluidos = limitar(excluirIds, MAX_EXCLUIDOS);
        Specification<Anuncio> spec = AnuncioSpec.filtrar(
                usuarioId, nombre, genero, raza, clase, edadMin, edadMax,
                categoriaIds, excluidos);
        List<Anuncio> candidatos = new ArrayList<>(anuncioRepository.findAll(spec));

        // La presencia se resuelve siempre en un solo lote: aunque el filtro
        // «solo conectados» esté apagado, el DTO debe informar quién está online.
        Map<Integer, Boolean> presencia = presenciaDe(candidatos);
        List<Anuncio> filtrados = candidatos;
        if (soloOnline) {
            filtrados = candidatos.stream()
                    .filter(a -> Boolean.TRUE.equals(
                            presencia.get(a.getPersonaje().getIdPersonaje())))
                    .collect(Collectors.toList());
        }

        // Orden aleatorio en cada petición y lote de hasta `size` (9 por defecto).
        Collections.shuffle(filtrados);
        int tamanio = Math.min(Math.max(size, 1), LOTE_MAXIMO);
        boolean hayMas = filtrados.size() > tamanio;
        List<Anuncio> lote = filtrados.subList(0, Math.min(tamanio, filtrados.size()));

        return new AnunciosRespuestaDTO(aDTOs(lote, presencia), hayMas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnuncioDTO> misAnuncios(Integer usuarioId) {
        List<Anuncio> anuncios = anuncioRepository.findByUsuarioId(usuarioId);
        return aDTOs(anuncios, presenciaDe(anuncios));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnuncioDTO> porPersonaje(Integer idPersonaje) {
        List<Anuncio> activos = anuncioRepository.findByPersonajeId(idPersonaje).stream()
                .filter(Anuncio::isActivo)
                .collect(Collectors.toList());
        return aDTOs(activos, presenciaDe(activos));
    }

    @Override
    public boolean existeCategoriaEnAnuncios(Integer idCategoria) {
        return anuncioCategoriaRepository.existsByIdCategoriaIdCategoria(idCategoria);
    }

    private Anuncio obtenerAnuncio(Integer idAnuncio) {
        return anuncioRepository.findById(idAnuncio)
                .orElseThrow(() -> new ResourceNotFoundException("Anuncio no encontrado"));
    }

    /**
     * Con JWT de usuario se exige `personajeId` en el body; con JWT de sesión de
     * personaje se usa el personaje de la sesión (se ignore lo que envíe el body).
     */
    private PerfilPersonaje resolverPersonaje(Integer personajeIdBody, Authentication authentication) {
        Integer personajeId;
        if (authentication != null && authentication.getPrincipal() instanceof CharacterSessionPrincipal cp) {
            personajeId = cp.getPersonajeId();
        } else {
            if (personajeIdBody == null) {
                throw new BadRequestException("Debes indicar el personaje para publicar el anuncio");
            }
            personajeId = personajeIdBody;
        }

        PerfilPersonaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado"));
        verificarPropiedad(personaje, authentication);
        return personaje;
    }

    private void verificarPropiedad(PerfilPersonaje personaje, Authentication authentication) {
        Integer userId = AuthUtils.obtenerUserId(authentication);
        boolean esPropio = personaje.getUserId() != null
                && userId.equals(personaje.getUserId().getUserId());
        if (!esPropio && !AuthUtils.esModerador(authentication)) {
            throw new AccessDeniedException("No puedes gestionar anuncios de otro personaje");
        }
    }

    private String validarContenido(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new BadRequestException("El contenido del anuncio es obligatorio");
        }
        if (contenido.length() > MAX_CONTENIDO) {
            throw new BadRequestException("El contenido no puede superar 10000 caracteres");
        }
        String sanitizado = HtmlUtils.sanitize(contenido);
        if (sanitizado == null || sanitizado.isBlank()) {
            throw new BadRequestException("El contenido del anuncio no es válido");
        }
        return sanitizado;
    }

    /**
     * Las categorías son opcionales (la UI las marca como tal). Si vienen,
     * validamos que existan y que no se repitan; si no vienen, no hay nada que
     * guardar ni que consultar.
     */
    private List<Categoria> validarCategorias(List<Integer> categoriaIds) {
        if (categoriaIds == null || categoriaIds.isEmpty()) {
            return List.of();
        }
        Set<Integer> ids = new LinkedHashSet<>();
        for (Integer id : categoriaIds) {
            if (id == null) {
                throw new BadRequestException("Las categorías seleccionadas no son válidas");
            }
            if (!ids.add(id)) {
                throw new BadRequestException("Las categorías seleccionadas no deben repetirse");
            }
        }

        Map<Integer, Categoria> encontradas = categoriaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Categoria::getIdCategoria, Function.identity()));
        if (encontradas.size() != ids.size()) {
            throw new ResourceNotFoundException("Alguna categoría seleccionada no existe");
        }
        return ids.stream().map(encontradas::get).collect(Collectors.toList());
    }

    private List<AnuncioCategoria> guardarCategorias(Anuncio anuncio, List<Categoria> categorias) {
        if (categorias.isEmpty()) {
            return List.of();
        }
        List<AnuncioCategoria> relaciones = categorias.stream().map(categoria -> {
            AnuncioCategoria relacion = new AnuncioCategoria();
            relacion.setAnuncio(anuncio);
            relacion.setIdCategoria(categoria);
            relacion.setFechaAdicion(new Date());
            return relacion;
        }).collect(Collectors.toList());
        return new ArrayList<>(anuncioCategoriaRepository.saveAll(relaciones));
    }

    private List<AnuncioCategoria> categoriasDe(Integer idAnuncio) {
        return anuncioCategoriaRepository.findByAnuncioIdIn(List.of(idAnuncio));
    }

    /** DTO de una sola operación: evita consultar lo que ya tenemos en memoria. */
    private AnuncioDTO aDTO(Anuncio anuncio, List<AnuncioCategoria> relaciones) {
        AnuncioDTO dto = AnuncioDTO.fromEntity(anuncio);
        dto.setCategorias(relaciones == null ? List.of()
                : relaciones.stream().map(AnuncioCategoriaDTO::fromEntity).collect(Collectors.toList()));
        return dto;
    }

    /** Presencia en lote para los personajes de una lista de anuncios. */
    private Map<Integer, Boolean> presenciaDe(List<Anuncio> anuncios) {
        if (anuncios.isEmpty()) {
            return Map.of();
        }
        Set<Integer> personajeIds = anuncios.stream()
                .map(a -> a.getPersonaje().getIdPersonaje())
                .collect(Collectors.toSet());
        Map<Integer, Boolean> estados = presenceService.getOnlineStatusForCharacters(personajeIds);
        return estados == null ? Map.of() : estados;
    }

    /**
     * Enriquecimiento en lote: usuario y categorías se resuelven con una consulta
     * cada uno, nunca una por anuncio.
     */
    private List<AnuncioDTO> aDTOs(List<Anuncio> anuncios, Map<Integer, Boolean> presencia) {
        if (anuncios.isEmpty()) {
            return List.of();
        }

        List<Integer> ids = anuncios.stream()
                .map(Anuncio::getIdAnuncio)
                .collect(Collectors.toList());
        Map<Integer, Integer> usuarioIds = anuncioRepository.findUsuarioIdsByAnuncioIds(ids).stream()
                .collect(Collectors.toMap(AnuncioRepository.AnuncioUsuarioId::getAnuncioId,
                        AnuncioRepository.AnuncioUsuarioId::getUsuarioId));
        Map<Integer, List<AnuncioCategoriaDTO>> categorias = anuncioCategoriaRepository
                .findByAnuncioIdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        ac -> ac.getAnuncio().getIdAnuncio(),
                        Collectors.mapping(AnuncioCategoriaDTO::fromEntity, Collectors.toList())));

        return anuncios.stream().map(anuncio -> {
            AnuncioDTO dto = AnuncioDTO.fromEntity(anuncio);
            dto.setUsuarioId(usuarioIds.getOrDefault(anuncio.getIdAnuncio(), dto.getUsuarioId()));
            dto.setCategorias(categorias.getOrDefault(anuncio.getIdAnuncio(), List.of()));
            dto.setOnline(Boolean.TRUE.equals(presencia.get(dto.getPersonajeId())));
            return dto;
        }).collect(Collectors.toList());
    }

    private static List<Integer> limitar(List<Integer> ids, int maximo) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.size() <= maximo ? ids : new ArrayList<>(ids.subList(0, maximo));
    }
}
