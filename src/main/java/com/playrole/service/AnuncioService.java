package com.playrole.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.playrole.dto.ActualizarAnuncioDTO;
import com.playrole.dto.AnuncioDTO;
import com.playrole.dto.AnunciosRespuestaDTO;
import com.playrole.dto.CambiarActivoDTO;
import com.playrole.dto.CrearAnuncioDTO;

public interface AnuncioService {

    AnuncioDTO crear(CrearAnuncioDTO dto, Authentication authentication);

    AnuncioDTO editar(Integer idAnuncio, ActualizarAnuncioDTO dto, Authentication authentication);

    AnuncioDTO cambiarActivo(Integer idAnuncio, CambiarActivoDTO dto, Authentication authentication);

    void eliminar(Integer idAnuncio, Authentication authentication);

    /** Listado público: nunca incluye anuncios de la cuenta autenticada. */
    AnunciosRespuestaDTO listar(Integer usuarioId, String nombre, String genero, String raza,
            String clase, Integer edadMin, Integer edadMax, List<Integer> categoriaIds,
            boolean soloOnline, List<Integer> excluirIds, int size);

    /** Todos los anuncios (activos e inactivos) de la cuenta. */
    List<AnuncioDTO> misAnuncios(Integer usuarioId);

    /** Solo anuncios activos del personaje. */
    List<AnuncioDTO> porPersonaje(Integer idPersonaje);

    boolean existeCategoriaEnAnuncios(Integer idCategoria);
}
