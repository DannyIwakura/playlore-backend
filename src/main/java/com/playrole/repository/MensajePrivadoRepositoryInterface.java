package com.playrole.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.playrole.model.MensajePrivado;
import jakarta.transaction.Transactional;

public interface MensajePrivadoRepositoryInterface extends JpaRepository<MensajePrivado, Integer> {

    // Bandeja de salida
    @Query("SELECT m FROM MensajePrivado m WHERE m.emisorId.userId = :userId AND m.visibleEmisor = true AND m.archivadoEmisor = false ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findMensajesEnviados(@Param("userId") Integer userId);

    // Bandeja de entrada
    @Query("SELECT m FROM MensajePrivado m WHERE m.receptorId.userId = :userId AND m.visibleReceptor = true AND m.archivadoReceptor = false ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findMensajesRecibidos(@Param("userId") Integer userId);

    // Mensajes archivados
    @Query("SELECT m FROM MensajePrivado m WHERE " +
           "(m.emisorId.userId = :userId AND m.archivadoEmisor = true AND m.visibleEmisor = true) OR " +
           "(m.receptorId.userId = :userId AND m.archivadoReceptor = true AND m.visibleReceptor = true) " +
           "ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findArchivados(@Param("userId") Integer userId);

    // Mensajes no leídos
    @Query("SELECT m FROM MensajePrivado m WHERE m.receptorId.userId = :userId AND m.leido = false AND m.visibleReceptor = true ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findNoLeidos(@Param("userId") Integer userId);

    // Contar no leídos
    @Query("SELECT COUNT(m) FROM MensajePrivado m WHERE m.receptorId.userId = :userId AND m.leido = false AND m.visibleReceptor = true")
    Long contarNoLeidos(@Param("userId") Integer userId);

    // Marcar como leído (ESTA ES LA QUE DABA EL ERROR)
    @Modifying
    @Transactional
    @Query("UPDATE MensajePrivado m SET m.leido = true WHERE m.idMensaje = :id AND m.receptorId.userId = :userId")
    void marcarComoLeido(@Param("id") Integer id, @Param("userId") Integer userId);

    // Archivar
    @Modifying
    @Transactional
    @Query("""
           UPDATE MensajePrivado m SET 
           m.archivadoEmisor = CASE WHEN m.emisorId.userId = :userId THEN true ELSE m.archivadoEmisor END,
           m.archivadoReceptor = CASE WHEN m.receptorId.userId = :userId THEN true ELSE m.archivadoReceptor END
           WHERE m.idMensaje = :id
           """)
    void archivarParaUsuario(@Param("id") Integer id, @Param("userId") Integer userId);

    // Eliminar (Ocultar)
    @Modifying
    @Transactional
    @Query("""
           UPDATE MensajePrivado m SET 
           m.visibleEmisor = CASE WHEN m.emisorId.userId = :userId THEN false ELSE m.visibleEmisor END,
           m.visibleReceptor = CASE WHEN m.receptorId.userId = :userId THEN false ELSE m.visibleReceptor END
           WHERE m.idMensaje = :id
           """)
    void ocultarParaUsuario(@Param("id") Integer id, @Param("userId") Integer userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MensajePrivado m WHERE m.visibleEmisor = false AND m.visibleReceptor = false")
    void eliminarMensajesHuerfanos();
}