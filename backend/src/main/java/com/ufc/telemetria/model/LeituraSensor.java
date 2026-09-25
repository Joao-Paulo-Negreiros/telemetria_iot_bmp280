package com.ufc.telemetria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Altura 1: BMP280
    private Double temperaturaBmp;
    private Double pressao;

    // Altura 2: AHT10 (Barramento I2C Secundário)
    private Double temperaturaAht;
    private Double umidade;

    // Altura 3: AHT21 (Barramento I2C Principal)
    private Double temperaturaAht21;
    private Double umidadeAht21;

    // Qualidade do ar: ENS160
    private Integer eco2;
    private Integer tvoc;

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

    public Double getTemperaturaAht21() {
        return temperaturaAht21;
    }

    public void setTemperaturaAht21(Double temperaturaAht21) {
        this.temperaturaAht21 = temperaturaAht21;
    }

    public Double getUmidadeAht21() {
        return umidadeAht21;
    }

    public void setUmidadeAht21(Double umidadeAht21) {
        this.umidadeAht21 = umidadeAht21;
    }

    public Integer getEco2() {
        return eco2;
    }

    public void setEco2(Integer eco2) {
        this.eco2 = eco2;
    }

    public Integer getTvoc() {
        return tvoc;
    }

    public void setTvoc(Integer tvoc) {
        this.tvoc = tvoc;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}