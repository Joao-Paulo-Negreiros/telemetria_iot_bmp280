package com.ufc.telemetria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Novo formato largo: colunas dedicadas por grandeza
    private Double temperaturaBmp;
    private Double pressao;
    private Double temperaturaAht;
    private Double umidade;

    private LocalDateTime dataHora;

    public LeituraSensor() {
    }

    @PrePersist
    protected void onCreate() {
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTemperaturaBmp() {
        return temperaturaBmp;
    }

    public void setTemperaturaBmp(Double temperaturaBmp) {
        this.temperaturaBmp = temperaturaBmp;
    }

    public Double getPressao() {
        return pressao;
    }

    public void setPressao(Double pressao) {
        this.pressao = pressao;
    }

    public Double getTemperaturaAht() {
        return temperaturaAht;
    }

    public void setTemperaturaAht(Double temperaturaAht) {
        this.temperaturaAht = temperaturaAht;
    }

    public Double getUmidade() {
        return umidade;
    }

    public void setUmidade(Double umidade) {
        this.umidade = umidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}