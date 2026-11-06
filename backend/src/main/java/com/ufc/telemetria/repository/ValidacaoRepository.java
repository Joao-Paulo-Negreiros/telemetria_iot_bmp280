package com.ufc.telemetria.repository;

import com.ufc.telemetria.model.LeituraValidacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidacaoRepository extends JpaRepository<LeituraValidacao, Long> {
}