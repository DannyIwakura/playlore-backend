package com.playrole.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.playrole.model.AnuncioCategoria;

public interface AnuncioCategoriaRepository extends JpaRepository<AnuncioCategoria, Integer> {

    /**
     * Las relaciones se devuelven con anuncio y categoría resueltos: el DTO lee
     * nombre/tipo de la categoría y agrupa por anuncio, así que sin los fetch
     * joins habría una consulta lazy por cada relación (N+1).
     */
    @Query("SELECT ac FROM AnuncioCategoria ac "
            + "JOIN FETCH ac.anuncio a "
            + "JOIN FETCH ac.idCategoria c "
            + "WHERE a.idAnuncio IN :anuncioIds")
    List<AnuncioCategoria> findByAnuncioIdIn(@Param("anuncioIds") Collection<Integer> anuncioIds);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM AnuncioCategoria ac WHERE ac.anuncio.idAnuncio IN :anuncioIds")
    void deleteByAnuncioIdIn(@Param("anuncioIds") Collection<Integer> anuncioIds);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM AnuncioCategoria ac WHERE ac.anuncio.idAnuncio = :anuncioId")
    void deleteByAnuncioId(@Param("anuncioId") Integer anuncioId);

    boolean existsByIdCategoriaIdCategoria(Integer idCategoria);

    /** Cascada al borrar un personaje (del anuncio dueño del personaje). */
    @Modifying
    @Transactional
    @Query("DELETE FROM AnuncioCategoria ac WHERE ac.anuncio.idAnuncio IN "
            + "(SELECT a.idAnuncio FROM Anuncio a WHERE a.personaje.idPersonaje = :personajeId)")
    void deleteByPersonajeId(@Param("personajeId") Integer personajeId);
}
