package com.playrole.repository;

import com.playrole.enums.EstadoDenuncia;
import com.playrole.model.Denuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DenunciaRepository extends JpaRepository<Denuncia, Integer>, JpaSpecificationExecutor<Denuncia> {

    Page<Denuncia> findByEstado(EstadoDenuncia estado, Pageable pageable);

    boolean existsByTipoAndTipoIdAndDenuncianteUserIdAndEstado(
            com.playrole.enums.TipoDenuncia tipo, Integer tipoId, Integer denuncianteUserId, EstadoDenuncia estado);
}
