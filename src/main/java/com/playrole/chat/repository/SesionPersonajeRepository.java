package com.playrole.chat.repository;

import com.playrole.chat.model.SesionPersonaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SesionPersonajeRepository extends JpaRepository<SesionPersonaje, Integer> {

    @Query("SELECT s FROM SesionPersonaje s WHERE s.usuario.userId = :usuarioId AND s.activa = true")
    List<SesionPersonaje> findActivasByUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(s) FROM SesionPersonaje s WHERE s.usuario.userId = :usuarioId AND s.activa = true")
    long countActivasByUsuario(@Param("usuarioId") Integer usuarioId);

    Optional<SesionPersonaje> findByTokenJwtAndActivaTrue(String tokenJwt);

    @Query("SELECT s FROM SesionPersonaje s WHERE s.personaje.idPersonaje = :personajeId AND s.activa = true")
    List<SesionPersonaje> findActivasByPersonaje(@Param("personajeId") Integer personajeId);
}
