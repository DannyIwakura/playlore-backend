package com.playrole.repository;

import com.playrole.model.RegistroModeracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroModeracionRepository extends JpaRepository<RegistroModeracion, Integer> {

    List<RegistroModeracion> findAllByOrderByFechaDesc();
}
