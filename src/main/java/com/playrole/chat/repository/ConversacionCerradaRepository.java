package com.playrole.chat.repository;

import com.playrole.chat.model.ConversacionCerrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversacionCerradaRepository extends JpaRepository<ConversacionCerrada, Integer> {

    boolean existsByPersonajeIdAndOtroPersonajeId(Integer personajeId, Integer otroPersonajeId);

    @Query("SELECT c.otroPersonajeId FROM ConversacionCerrada c WHERE c.personajeId = :personajeId")
    List<Integer> findOtroPersonajeIdsByPersonajeId(@Param("personajeId") Integer personajeId);

    void deleteByPersonajeIdAndOtroPersonajeId(Integer personajeId, Integer otroPersonajeId);

    void deleteByPersonajeIdOrOtroPersonajeId(Integer personajeId, Integer otroPersonajeId);
}
