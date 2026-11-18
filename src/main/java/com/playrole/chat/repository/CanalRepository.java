package com.playrole.chat.repository;

import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.model.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CanalRepository extends JpaRepository<Canal, Integer> {

    @Query("SELECT c FROM Canal c WHERE c.visible = true " +
           "AND (c.suspendido = false OR (c.suspendidoHasta IS NOT NULL AND c.suspendidoHasta <= :ahora)) " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findVisibles(@Param("ahora") Date ahora);

    /**
     * Listado publico: excluye los privados. El staff usa findVisibles porque
     * puede ver los privados desde la lista sin invitacion.
     */
    @Query("SELECT c FROM Canal c WHERE c.visible = true AND c.privado = false " +
           "AND (c.suspendido = false OR (c.suspendidoHasta IS NOT NULL AND c.suspendidoHasta <= :ahora)) " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findVisiblesPublicos(@Param("ahora") Date ahora);

    @Query("SELECT c FROM Canal c JOIN MiembroCanal m ON m.canal.idCanal = c.idCanal " +
           "WHERE m.personaje.idPersonaje = :personajeId AND c.visible = true " +
           "AND (c.suspendido = false OR (c.suspendidoHasta IS NOT NULL AND c.suspendidoHasta <= :ahora)) " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findCanalesDePersonaje(@Param("personajeId") Integer personajeId,
                                       @Param("ahora") Date ahora);

    @Query("SELECT c FROM Canal c WHERE c.visible = true AND c.privado = false " +
           "AND (c.suspendido = false OR (c.suspendidoHasta IS NOT NULL AND c.suspendidoHasta <= :ahora)) " +
           "AND c.idCanal NOT IN (SELECT m.canal.idCanal FROM MiembroCanal m WHERE m.personaje.idPersonaje = :personajeId) " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findCanalesPublicosNoUnidos(@Param("personajeId") Integer personajeId,
                                            @Param("ahora") Date ahora);

    /**
     * Listado para staff: incluye tambien los privados a los que aun no se ha
     * unido, para que pueda entrar desde la lista sin invitacion.
     */
    @Query("SELECT c FROM Canal c WHERE c.visible = true " +
           "AND (c.suspendido = false OR (c.suspendidoHasta IS NOT NULL AND c.suspendidoHasta <= :ahora)) " +
           "AND c.idCanal NOT IN (SELECT m.canal.idCanal FROM MiembroCanal m WHERE m.personaje.idPersonaje = :personajeId) " +
           "ORDER BY c.fechaCreacion DESC")
    List<Canal> findCanalesNoUnidos(@Param("personajeId") Integer personajeId,
                                    @Param("ahora") Date ahora);

    @Query("SELECT c FROM Canal c JOIN MiembroCanal m ON m.canal.idCanal = c.idCanal " +
           "WHERE m.personaje.idPersonaje = :personajeId AND c.privado = true AND c.visible = true " +
           "AND m.rol IN :roles " +
           "AND (c.suspendido = false OR (c.suspendidoHasta IS NOT NULL AND c.suspendidoHasta <= :ahora)) " +
           "ORDER BY c.nombre ASC")
    List<Canal> findCanalesPrivadosGestionables(@Param("personajeId") Integer personajeId,
                                                @Param("roles") Collection<RolCanal> roles,
                                                @Param("ahora") Date ahora);

    long countByCreadorIdPersonaje(Integer creadorId);

    List<Canal> findByCreadorIdPersonaje(Integer creadorId);

    List<Canal> findAllByOrderByFechaCreacionDesc();

    List<Canal> findBySuspendidoTrueAndSuspendidoHastaBefore(Date ahora);
}
