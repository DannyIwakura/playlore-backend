package com.playrole.service;

import java.util.List;
import java.util.Optional;

import com.playrole.model.SolicitudAmistad;

public interface ISolicitudAmistadService {

    SolicitudAmistad enviarSolicitud(Integer emisorId, Integer receptorId);

    List<SolicitudAmistad> obtenerSolicitudesEnviadas(Integer userId);

    List<SolicitudAmistad> obtenerSolicitudesRecibidas(Integer userId);

    List<SolicitudAmistad> obtenerSolicitudesPendientes(Integer userId);

    SolicitudAmistad aceptarSolicitud(Integer idSolicitud);

    SolicitudAmistad rechazarSolicitud(Integer idSolicitud);

    boolean existeSolicitud(Integer emisorId, Integer receptorId);

    List<SolicitudAmistad> obtenerAmistades(Integer userId);

    Optional<SolicitudAmistad> obtenerPorId(Integer id);
}
