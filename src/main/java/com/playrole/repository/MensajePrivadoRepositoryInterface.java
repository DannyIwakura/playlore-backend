package com.playrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.enums.EstadoMensaje;
import com.playrole.model.MensajePrivado;

import jakarta.transaction.Transactional;

public interface MensajePrivadoRepositoryInterface extends JpaRepository<MensajePrivado, Integer> {
	//mensajes enviados por un usuario que no estén "eliminados" para el emisor
    @Query("SELECT m FROM MensajePrivado m WHERE m.emisorId.userId = :userId AND m.estadoEmisor <> com.playrole.enums.EstadoMensaje.EN_PAPELERA ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findMensajesEnviados(@Param("userId") Integer userId);

    //mensajes recibidos por un usuario que no estén "eliminados" para el receptor
    @Query("SELECT m FROM MensajePrivado m WHERE m.receptorId.userId = :userId AND m.estadoReceptor <> com.playrole.enums.EstadoMensaje.EN_PAPELERA ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findMensajesRecibidos(@Param("userId") Integer userId);

    //mensajes no leídos por un usuario (receptor)
    @Query("SELECT m FROM MensajePrivado m WHERE m.receptorId.userId = :userId AND m.estadoReceptor = com.playrole.enums.EstadoMensaje.NO_LEIDO ORDER BY m.fechaEnvio DESC")
    List<MensajePrivado> findNoLeidos(@Param("userId") Integer userId);

    @Query("""
    		SELECT m FROM MensajePrivado m 
    		WHERE m.receptorId.userId = :userId
    		AND m.estadoReceptor IN :estados
    		AND m.estadoReceptor <> com.playrole.enums.EstadoMensaje.EN_PAPELERA
    		ORDER BY m.fechaEnvio DESC
    		""")
    		List<MensajePrivado> filtrarPorEstadoRecibidos(
    		        @Param("userId") Integer userId,
    		        @Param("estados") List<EstadoMensaje> estados);
    
    //contar mensajes no leídos
    @Query("SELECT COUNT(m) FROM MensajePrivado m WHERE m.receptorId.userId = :userId AND m.estadoReceptor = com.playrole.enums.EstadoMensaje.NO_LEIDO")
    Long contarNoLeidos(@Param("userId") Integer userId);

    //marcar como leído para un receptor
    @Modifying
    @Transactional
    @Query("UPDATE MensajePrivado m SET m.estadoReceptor = com.playrole.enums.EstadoMensaje.LEIDO WHERE m.idMensaje = :id AND m.receptorId.userId = :userId")
    void marcarComoLeido(@Param("id") Integer id, @Param("userId") Integer userId);

    //eliminar mensaje para un usuario (pone EN_PAPELERA en su estado)
    @Modifying
    @Transactional
    @Query("UPDATE MensajePrivado m SET " +
           "m.estadoEmisor = CASE WHEN m.emisorId.userId = :userId THEN com.playrole.enums.EstadoMensaje.EN_PAPELERA ELSE m.estadoEmisor END, " +
           "m.estadoReceptor = CASE WHEN m.receptorId.userId = :userId THEN com.playrole.enums.EstadoMensaje.EN_PAPELERA ELSE m.estadoReceptor END " +
           "WHERE m.idMensaje = :id")
    void eliminarParaUsuario(@Param("id") Integer id, @Param("userId") Integer userId);
}
