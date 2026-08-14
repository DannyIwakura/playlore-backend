package com.playrole.repository;

import com.playrole.enums.EstadoDenuncia;
import com.playrole.model.Denuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.Collection;

public interface DenunciaRepository extends JpaRepository<Denuncia, Integer>, JpaSpecificationExecutor<Denuncia> {

    Page<Denuncia> findByEstado(EstadoDenuncia estado, Pageable pageable);

    boolean existsByTipoAndTipoIdAndDenuncianteUserIdAndEstado(
            com.playrole.enums.TipoDenuncia tipo, Integer tipoId, Integer denuncianteUserId, EstadoDenuncia estado);

    @Modifying
    @Transactional
    @Query("DELETE FROM Denuncia d WHERE d.denunciante.userId = :usuarioId")
    void deleteByDenuncianteUsuario(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Denuncia d WHERE d.denunciantePersonaje.idPersonaje IN :personajeIds")
    void deleteByDenunciantePersonajes(@Param("personajeIds") Collection<Integer> personajeIds);

    @Modifying
    @Transactional
    @Query("UPDATE Denuncia d SET d.resueltoPor = NULL WHERE d.resueltoPor.userId = :usuarioId")
    void desvincularResueltoPor(@Param("usuarioId") Integer usuarioId);
}
