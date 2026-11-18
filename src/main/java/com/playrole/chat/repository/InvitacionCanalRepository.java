package com.playrole.chat.repository;

import com.playrole.chat.enums.EstadoInvitacion;
import com.playrole.chat.model.InvitacionCanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface InvitacionCanalRepository extends JpaRepository<InvitacionCanal, Integer> {

    Optional<InvitacionCanal> findFirstByCanalIdCanalAndInvitadoIdPersonajeAndEstado(
            Integer canalId, Integer invitadoId, EstadoInvitacion estado);

    boolean existsByCanalIdCanalAndInvitadoIdPersonajeAndEstado(
            Integer canalId, Integer invitadoId, EstadoInvitacion estado);

    List<InvitacionCanal> findByInvitadoIdPersonajeAndEstadoOrderByFechaCreacionDesc(
            Integer invitadoId, EstadoInvitacion estado);

    List<InvitacionCanal> findByCanalIdCanal(Integer canalId);

    @Modifying
    @Transactional
    @Query("DELETE FROM InvitacionCanal i WHERE i.canal.idCanal = :canalId")
    void deleteByCanalId(@Param("canalId") Integer canalId);

    @Modifying
    @Transactional
    @Query("DELETE FROM InvitacionCanal i WHERE i.invitado.idPersonaje = :personajeId " +
           "OR i.invitador.idPersonaje = :personajeId")
    void deleteByPersonajeId(@Param("personajeId") Integer personajeId);
}
