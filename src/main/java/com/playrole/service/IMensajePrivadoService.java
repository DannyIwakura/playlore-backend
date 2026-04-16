package com.playrole.service;

import java.util.List;
import com.playrole.dto.MensajePrivadoDTO;

public interface IMensajePrivadoService {
    
    // Obtiene un mensaje específico adaptado a la vista del usuario (currentUserId)
    MensajePrivadoDTO obtenerMensaje(Integer idMensaje, Integer idUsuario);
    
    // Bandeja de Entrada (Visibles y No archivados)
    List<MensajePrivadoDTO> mensajesRecibidos(Integer idUsuario);

    // Bandeja de Salida (Visibles y No archivados)
    List<MensajePrivadoDTO> mensajesEnviados(Integer idUsuario);

    // Mensajes marcados como archivados para ese usuario
    List<MensajePrivadoDTO> mensajesArchivados(Integer idUsuario);

    // Solo los que el receptor no ha abierto
    List<MensajePrivadoDTO> mensajesNoLeidos(Integer idUsuario);

    // Creación de nuevo mensaje
    MensajePrivadoDTO enviarMensaje(MensajePrivadoDTO mensajeDTO);

    // Reemplaza filtrarPorEstadoRecibidos: Filtra por lectura o archivo de forma sencilla
    List<MensajePrivadoDTO> listarPorCriterio(Integer idUsuario, boolean soloLeidos, boolean soloArchivados);

    Long contarNoLeidos(Integer idUsuario);

    void marcarComoLeido(Integer idMensaje, Integer idUsuario);

    // Mueve el mensaje a archivado
    void archivarMensaje(Integer idMensaje, Integer idUsuario);
    
    // Mueve el mensaje a la papelera
    List<MensajePrivadoDTO> mensajesPapelera(Integer idUsuario);

    // Cambia el flag de visibilidad a false para el usuario (Borrado lógico)
    void eliminarMensaje(Integer idMensaje, Integer idUsuario);
    
    //Eliminar si los el emisor y receptor los ha borrado de su papelera
    void eliminarDefinitivamente(Integer idMensaje, Integer idUsuario);
}