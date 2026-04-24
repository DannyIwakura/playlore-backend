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
	@Query("SELECT m FROM MensajePrivado m WHERE " +
		       "m.emisorId.userId = :idUsuario AND m.visibleEmisor = true AND m.eliminadoEmisor = false")
	List<MensajePrivado> findMensajesEnviados(@Param("idUsuario") Integer idUsuario);

    // Bandeja de entrada
    @Query("SELECT m FROM MensajePrivado m WHERE " +
    	       "m.receptorId.userId = :idUsuario AND m.visibleReceptor = true AND m.eliminadoReceptor = false")
	List<MensajePrivado> findMensajesRecibidos(@Param("idUsuario") Integer idUsuario);

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

    // Marcar como leído
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
    
    // Mensajes donde el usuario los eliminó pero existen en BD softdeletes
    @Query("SELECT m FROM MensajePrivado m WHERE " +
    	       "(m.emisorId.userId = :idUsuario AND m.visibleEmisor = false AND m.eliminadoEmisor = false) OR " +
    	       "(m.receptorId.userId = :idUsuario AND m.visibleReceptor = false AND m.eliminadoReceptor = false)")
    	List<MensajePrivado> findPapelera(@Param("idUsuario") Integer idUsuario);
    
    // Eliminar (ocultar para el usuario)
    // Evita que un mensaje se elimine y la otra parte no puede acceder a él
    @Modifying
    @Transactional
    @Query("""
           UPDATE MensajePrivado m SET 
           m.visibleEmisor = CASE WHEN m.emisorId.userId = :userId THEN false ELSE m.visibleEmisor END,
           m.visibleReceptor = CASE WHEN m.receptorId.userId = :userId THEN false ELSE m.visibleReceptor END
           WHERE m.idMensaje = :id
           """)
    void ocultarParaUsuario(@Param("id") Integer id, @Param("userId") Integer userId);
    
    // Cuando dos usuarios eliminan el mensaje, se eliminan definivitavemnte de la bd
    @Modifying
    @Transactional
    @Query("DELETE FROM MensajePrivado m WHERE m.visibleEmisor = false AND m.visibleReceptor = false")
    void eliminarMensajesHuerfanos();
}