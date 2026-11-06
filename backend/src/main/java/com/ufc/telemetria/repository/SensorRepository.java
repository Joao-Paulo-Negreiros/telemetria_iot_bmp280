package com.ufc.telemetria.repository;

import com.ufc.telemetria.model.LeituraSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<LeituraSensor, Long> {
}