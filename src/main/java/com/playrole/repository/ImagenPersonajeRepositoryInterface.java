package com.playrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playrole.model.ImagenPersonaje;

import jakarta.transaction.Transactional;

public interface ImagenPersonajeRepositoryInterface extends JpaRepository<ImagenPersonaje, Integer> {

    List<ImagenPersonaje> findByIdPersonaje_IdPersonajeOrderByOrdenAscFechaSubidaAsc(Integer idPersonaje);

    long countByIdPersonaje_IdPersonaje(Integer idPersonaje);

    @Modifying
    @Transactional
    @Query("DELETE FROM ImagenPersonaje i WHERE i.idImagen = :id")
    void deleteByIdDirect(@Param("id") Integer id);
}
