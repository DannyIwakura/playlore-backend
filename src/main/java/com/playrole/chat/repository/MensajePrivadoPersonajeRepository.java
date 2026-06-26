package com.playrole.chat.repository;

import com.playrole.chat.model.MensajePrivadoPersonaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajePrivadoPersonajeRepository extends JpaRepository<MensajePrivadoPersonaje, Integer> {

    @Query("SELECT m FROM MensajePrivadoPersonaje m WHERE " +
           "(m.emisor.idPersonaje = :personajeId AND m.eliminadoEmisor = false) OR " +
           "(m.receptor.idPersonaje = :personajeId AND m.eliminadoReceptor = false) " +
           "ORDER BY m.fechaEnvio DESC")
    List<MensajePrivadoPersonaje> findConversaciones(@Param("personajeId") Integer personajeId);

    @Query("SELECT m FROM MensajePrivadoPersonaje m WHERE " +
           "((m.emisor.idPersonaje = :personajeId1 AND m.receptor.idPersonaje = :personajeId2 AND m.eliminadoEmisor = false) OR " +
           "(m.emisor.idPersonaje = :personajeId2 AND m.receptor.idPersonaje = :personajeId1 AND m.eliminadoReceptor = false)) " +
           "ORDER BY m.fechaEnvio ASC")
    List<MensajePrivadoPersonaje> findConversacionEntre(
            @Param("personajeId1") Integer personajeId1,
            @Param("personajeId2") Integer personajeId2);

    @Query("SELECT COUNT(m) FROM MensajePrivadoPersonaje m WHERE " +
           "m.receptor.idPersonaje = :personajeId AND m.leido = false AND m.eliminadoReceptor = false")
    long countNoLeidos(@Param("personajeId") Integer personajeId);

    @Query("SELECT DISTINCT " +
           "CASE WHEN m.emisor.idPersonaje = :personajeId THEN m.receptor.idPersonaje ELSE m.emisor.idPersonaje END " +
           "FROM MensajePrivadoPersonaje m WHERE " +
           "(m.emisor.idPersonaje = :personajeId AND m.eliminadoEmisor = false) OR " +
           "(m.receptor.idPersonaje = :personajeId AND m.eliminadoReceptor = false)")
    List<Integer> findContactIds(@Param("personajeId") Integer personajeId);
}
