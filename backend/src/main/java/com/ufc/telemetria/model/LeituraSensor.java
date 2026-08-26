package com.ufc.telemetria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // Diz ao banco de dados que esta classe representa uma tabela
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperatura;
    private double pressao;
    private String sensor;

    // Campo para armazenar o momento da leitura
    private LocalDateTime dataHora;

    // Construtor vazio (obrigatório para o Spring)
    public LeituraSensor() {
    }

    // Garante que a data/hora seja capturada no exato momento da persistência no
    // banco
    @PrePersist
    protected void onCreate() {
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters manuais (para não depender de biblioteca externa agora)
    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public double getPressao() {
        return pressao;
    }

    public void setPressao(double pressao) {
        this.pressao = pressao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

}