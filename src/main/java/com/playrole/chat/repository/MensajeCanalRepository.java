package com.playrole.chat.repository;

import com.playrole.chat.model.MensajeCanal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;

public interface MensajeCanalRepository extends JpaRepository<MensajeCanal, Integer> {

    @Query("SELECT m FROM MensajeCanal m WHERE m.canal.idCanal = :canalId " +
           "ORDER BY m.fechaEnvio DESC")
    Page<MensajeCanal> findMensajesByCanal(@Param("canalId") Integer canalId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM MensajeCanal m WHERE m.canal.idCanal = :canalId")
    void deleteByCanalId(@Param("canalId") Integer canalId);
}
