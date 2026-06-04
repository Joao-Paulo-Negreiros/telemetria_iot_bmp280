package com.projeto.bia.backend_sensor.repository;

import com.projeto.bia.backend_sensor.model.LeituraSensor;
import org.springframework.data.jpa.repository.JpaRepository;

// A interface estende o JpaRepository, que já traz prontos os métodos de salvar, listar e deletar
public interface SensorRepository extends JpaRepository<LeituraSensor, Long> {
    
// O corpo fica vazio porque o Spring Boot cria os comandos básicos do banco de dados automaticamente.
// <LeituraSensor, Long> indica que este repositório gerencia a entidade LeituraSensor, 
// e que a chave primária (ID) dela é do tipo Long.
}