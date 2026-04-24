package com.playrole.service;

import java.util.List;
import java.util.Optional;

import com.playrole.dto.AmigoDTO;
import com.playrole.dto.SolicitudAmistadDTO;

public interface ISolicitudAmistadService {

    SolicitudAmistadDTO enviarSolicitud(Integer emisorId, Integer receptorId);

    List<SolicitudAmistadDTO> obtenerSolicitudesPendientesRecibidas(Integer userId);
    
    List<SolicitudAmistadDTO> obtenerSolicitudesPendientesEnviadas(Integer userId);

    List<SolicitudAmistadDTO> obtenerSolicitudesPendientes(Integer userId);

    SolicitudAmistadDTO aceptarSolicitud(Integer idSolicitud);

    boolean existeSolicitud(Integer emisorId, Integer receptorId);
    
    public List<AmigoDTO> obtenerAmigos(Integer userId);

    Optional<SolicitudAmistadDTO> obtenerPorId(Integer id);
    
    void eliminarSolicitud(Integer idSolicitud);
    
    public void eliminarAmistadEntreUsuarios(Integer userId1, Integer userId2);
}
