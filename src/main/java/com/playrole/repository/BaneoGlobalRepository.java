package com.playrole.repository;

import com.playrole.model.BaneoGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

public interface BaneoGlobalRepository extends JpaRepository<BaneoGlobal, Integer> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM BaneoGlobal b " +
           "WHERE b.usuario.userId = :usuarioId AND b.personaje IS NULL " +
           "AND (b.fechaExpiracion IS NULL OR b.fechaExpiracion > CURRENT_TIMESTAMP)")
    boolean existsActivoByUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM BaneoGlobal b " +
           "WHERE b.personaje.idPersonaje = :personajeId " +
           "AND (b.fechaExpiracion IS NULL OR b.fechaExpiracion > CURRENT_TIMESTAMP)")
    boolean existsActivoByPersonaje(@Param("personajeId") Integer personajeId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM BaneoGlobal b " +
           "WHERE ((b.usuario.userId = :usuarioId AND b.personaje IS NULL) " +
           "OR b.personaje.idPersonaje = :personajeId) " +
           "AND (b.fechaExpiracion IS NULL OR b.fechaExpiracion > CURRENT_TIMESTAMP)")
    boolean existsActivoByUsuarioOPersonaje(@Param("usuarioId") Integer usuarioId,
                                            @Param("personajeId") Integer personajeId);

    List<BaneoGlobal> findAllByOrderByFechaBaneoDesc();

    @Modifying
    @Transactional
    @Query("DELETE FROM BaneoGlobal b WHERE b.usuario.userId = :usuarioId OR b.baneadoPor.userId = :usuarioId")
    void deleteByUsuario(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BaneoGlobal b WHERE b.personaje.idPersonaje IN :personajeIds")
    void deleteByPersonajes(@Param("personajeIds") Collection<Integer> personajeIds);
}
