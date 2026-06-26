package com.playrole.chat.repository;

import com.playrole.chat.model.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CanalRepository extends JpaRepository<Canal, Integer> {

    List<Canal> findByVisibleTrueOrderByFechaCreacionDesc();

    @Query("SELECT c FROM Canal c JOIN MiembroCanal m ON m.canal.idCanal = c.idCanal " +
           "WHERE m.personaje.idPersonaje = :personajeId AND c.visible = true " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findCanalesDePersonaje(@Param("personajeId") Integer personajeId);

    @Query("SELECT c FROM Canal c WHERE c.visible = true AND c.privado = false " +
           "AND c.idCanal NOT IN (SELECT m.canal.idCanal FROM MiembroCanal m WHERE m.personaje.idPersonaje = :personajeId) " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findCanalesPublicosNoUnidos(@Param("personajeId") Integer personajeId);

    long countByCreadorIdPersonaje(Integer creadorId);
}
