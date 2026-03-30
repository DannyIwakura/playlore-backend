package com.playrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playrole.model.MensajePrivado;

public interface MensajePrivadoRepositoryInterface extends JpaRepository<MensajePrivado, Integer> {
	List<MensajePrivado> findByEmisorId_UserIdOrderByFechaEnvioDesc(Integer userId);
    List<MensajePrivado> findByReceptorId_UserIdOrderByFechaEnvioDesc(Integer userId);
    List<MensajePrivado> findByReceptorId_UserIdAndEstadoOrderByFechaEnvioDesc(Integer userId, int estado);
    List<MensajePrivado> findByReceptorId_UserIdAndEstadoInOrderByFechaEnvioDesc(Integer userId, List<Integer> estados);
}
