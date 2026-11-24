package com.playrole.chat.repository;

import com.playrole.chat.model.MensajePrivadoPersonaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
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
           "m.receptor.idPersonaje = :personajeId AND m.leido = false AND m.eliminadoReceptor = false AND " +
           "NOT EXISTS (SELECT c FROM ConversacionCerrada c WHERE c.personajeId = :personajeId " +
           "            AND c.otroPersonajeId = m.emisor.idPersonaje)")
    long countNoLeidos(@Param("personajeId") Integer personajeId);

    @Query("SELECT DISTINCT " +
           "CASE WHEN m.emisor.idPersonaje = :personajeId THEN m.receptor.idPersonaje ELSE m.emisor.idPersonaje END " +
           "FROM MensajePrivadoPersonaje m WHERE " +
           "(m.emisor.idPersonaje = :personajeId AND m.eliminadoEmisor = false) OR " +
           "(m.receptor.idPersonaje = :personajeId AND m.eliminadoReceptor = false)")
    List<Integer> findContactIds(@Param("personajeId") Integer personajeId);

    /** Todos los mensajes de la pareja, incluidos los ocultos por el antiguo borrado. */
    @Query("SELECT m FROM MensajePrivadoPersonaje m WHERE " +
           "(m.emisor.idPersonaje = :p1 AND m.receptor.idPersonaje = :p2) OR " +
           "(m.emisor.idPersonaje = :p2 AND m.receptor.idPersonaje = :p1)")
    List<MensajePrivadoPersonaje> findTodosEntre(@Param("p1") Integer p1, @Param("p2") Integer p2);

    /** Comprueba si queda algun flag heredado del antiguo "eliminar conversacion". */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MensajePrivadoPersonaje m WHERE " +
           "((m.emisor.idPersonaje = :p1 AND m.receptor.idPersonaje = :p2) OR " +
           " (m.emisor.idPersonaje = :p2 AND m.receptor.idPersonaje = :p1)) AND " +
           "(m.eliminadoEmisor = true OR m.eliminadoReceptor = true)")
    boolean existsFlagsEliminadoEntre(@Param("p1") Integer p1, @Param("p2") Integer p2);

    @Modifying
    @Transactional
    @Query("DELETE FROM MensajePrivadoPersonaje m WHERE m.emisor.idPersonaje = :personajeId OR m.receptor.idPersonaje = :personajeId")
    void deleteByPersonajeId(@Param("personajeId") Integer personajeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MensajePrivadoPersonaje m WHERE m.invitacion.idInvitacion IN :invitacionIds")
    void deleteByInvitacionIds(@Param("invitacionIds") java.util.Collection<Integer> invitacionIds);
}
