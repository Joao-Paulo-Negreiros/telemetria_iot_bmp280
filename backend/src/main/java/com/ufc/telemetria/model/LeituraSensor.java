package com.ufc.telemetria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;
import java.time.ZoneId;

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

    // Altura 3: AHT30 (Barramento I2C Principal)
    private Double temperaturaAht30;
    private Double umidadeAht30;

    // Qualidade do ar: ENS160
    private Integer eco2;
    private Integer tvoc;

    private LocalDateTime dataHora;

    public LeituraSensor() {
    }

    @PrePersist
    protected void onCreate() {
        this.dataHora = LocalDateTime.now(
                ZoneId.of("America/Fortaleza")
        );
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

    public Double getTemperaturaAht30() {
        return temperaturaAht30;
    }

    public void setTemperaturaAht30(Double temperaturaAht30) {
        this.temperaturaAht30 = temperaturaAht30;
    }

    public Double getUmidadeAht30() {
        return umidadeAht30;
    }

    public void setUmidadeAht30(Double umidadeAht30) {
        this.umidadeAht30 = umidadeAht30;
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