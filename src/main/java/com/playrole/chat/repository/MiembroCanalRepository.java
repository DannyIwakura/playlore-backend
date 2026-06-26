package com.playrole.chat.repository;

import com.playrole.chat.enums.RolCanal;
import com.playrole.chat.model.MiembroCanal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface MiembroCanalRepository extends JpaRepository<MiembroCanal, Integer> {

    List<MiembroCanal> findByCanalIdCanal(Integer canalId);

    @Query(value = "SELECT * FROM miembros_canal WHERE canal_id = :canalId " +
           "ORDER BY CASE rol WHEN 'OWNER' THEN 0 WHEN 'MOD' THEN 1 WHEN 'ADMIN' THEN 2 ELSE 3 END",
           countQuery = "SELECT COUNT(*) FROM miembros_canal WHERE canal_id = :canalId",
           nativeQuery = true)
    Page<MiembroCanal> findByCanalIdCanal(@Param("canalId") Integer canalId, Pageable pageable);

    Optional<MiembroCanal> findByCanalIdCanalAndPersonajeIdPersonaje(Integer canalId, Integer personajeId);

    @Query("SELECT COUNT(m) FROM MiembroCanal m WHERE m.canal.idCanal = :canalId")
    long countByCanalId(@Param("canalId") Integer canalId);

    boolean existsByCanalIdCanalAndPersonajeIdPersonaje(Integer canalId, Integer personajeId);

    @Query("SELECT m.rol FROM MiembroCanal m WHERE m.canal.idCanal = :canalId AND m.personaje.idPersonaje = :personajeId")
    Optional<RolCanal> findRolByCanalAndPersonaje(@Param("canalId") Integer canalId, @Param("personajeId") Integer personajeId);

    @Modifying
    @Transactional
    @Query("UPDATE MiembroCanal m SET m.rol = :rol WHERE m.canal.idCanal = :canalId AND m.personaje.idPersonaje = :personajeId")
    void updateRol(@Param("canalId") Integer canalId, @Param("personajeId") Integer personajeId, @Param("rol") RolCanal rol);

    @Modifying
    @Transactional
    @Query("DELETE FROM MiembroCanal m WHERE m.canal.idCanal = :canalId AND m.personaje.idPersonaje = :personajeId")
    void deleteByCanalAndPersonaje(@Param("canalId") Integer canalId, @Param("personajeId") Integer personajeId);

    List<MiembroCanal> findByPersonajeIdPersonaje(Integer personajeId);
}
