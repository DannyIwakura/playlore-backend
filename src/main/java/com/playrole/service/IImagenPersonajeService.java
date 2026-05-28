package com.playrole.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.playrole.dto.ImagenPersonajeDTO;

public interface IImagenPersonajeService {

    List<ImagenPersonajeDTO> listarImagenes(Integer idPersonaje);

    ImagenPersonajeDTO subirImagen(Integer idPersonaje, MultipartFile imagenFile, Integer userId);

    void eliminarImagen(Integer idImagen, Integer userId);
}
