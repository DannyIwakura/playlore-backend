package com.playrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.model.MensajePrivado;

public interface MensajePrivadoRepositoryInterface extends JpaRepository<MensajePrivado, Long> {
	List<MensajePrivado> findByEmisorId_UserIdOrderByFechaEnvioDesc(Long userId);
    List<MensajePrivado> findByReceptorId_UserIdOrderByFechaEnvioDesc(Long userId);
    List<MensajePrivado> findByReceptorId_UserIdAndEstadoOrderByFechaEnvioDesc(Long userId, int estado);
    List<MensajePrivado> findByReceptorId_UserIdAndEstadoInOrderByFechaEnvioDesc(Long userId, List<Integer> estados);
}
