package com.playrole.chat.repository;

import com.playrole.chat.model.BaneoCanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BaneoCanalRepository extends JpaRepository<BaneoCanal, Integer> {

    Optional<BaneoCanal> findByCanalIdCanalAndPersonajeIdPersonaje(Integer canalId, Integer personajeId);

    List<BaneoCanal> findByCanalIdCanalOrderByFechaBaneoDesc(Integer canalId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM BaneoCanal b " +
           "WHERE b.canal.idCanal = :canalId AND b.personaje.idPersonaje = :personajeId " +
           "AND (b.fechaExpiracion IS NULL OR b.fechaExpiracion > CURRENT_TIMESTAMP)")
    boolean existsActivoByCanalAndPersonaje(@Param("canalId") Integer canalId,
                                            @Param("personajeId") Integer personajeId);

    boolean existsByCanalIdCanalAndPersonajeIdPersonaje(Integer canalId, Integer personajeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BaneoCanal b WHERE b.canal.idCanal = :canalId")
    void deleteByCanalId(@Param("canalId") Integer canalId);
}
