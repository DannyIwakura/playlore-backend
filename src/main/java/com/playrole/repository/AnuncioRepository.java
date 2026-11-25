package com.playrole.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.model.Anuncio;

import jakarta.transaction.Transactional;

public interface AnuncioRepository extends JpaRepository<Anuncio, Integer>,
        JpaSpecificationExecutor<Anuncio> {

    /** Activos de TODA la cuenta (no del personaje), para el límite de 3. */
    @Query("SELECT COUNT(a) FROM Anuncio a WHERE a.activo = true "
            + "AND a.personaje.userId.userId = :usuarioId")
    long countActivosByUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT a FROM Anuncio a WHERE a.personaje.idPersonaje = :personajeId "
            + "ORDER BY a.fechaCreacion DESC")
    List<Anuncio> findByPersonajeId(@Param("personajeId") Integer personajeId);

    @Query("SELECT a FROM Anuncio a WHERE a.personaje.userId.userId = :usuarioId "
            + "ORDER BY a.fechaCreacion DESC")
    List<Anuncio> findByUsuarioId(@Param("usuarioId") Integer usuarioId);

    /**
     * Usuario propietario en lote: evita recorrer a.personaje.userId uno a uno
     * al construir los DTO del listado.
     */
    @Query("SELECT a.idAnuncio AS anuncioId, a.personaje.userId.userId AS usuarioId "
            + "FROM Anuncio a WHERE a.idAnuncio IN :anuncioIds")
    List<AnuncioUsuarioId> findUsuarioIdsByAnuncioIds(@Param("anuncioIds") Collection<Integer> anuncioIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM Anuncio a WHERE a.personaje.idPersonaje = :personajeId")
    void deleteByPersonajeId(@Param("personajeId") Integer personajeId);

    interface AnuncioUsuarioId {
        Integer getAnuncioId();

        Integer getUsuarioId();
    }
}
