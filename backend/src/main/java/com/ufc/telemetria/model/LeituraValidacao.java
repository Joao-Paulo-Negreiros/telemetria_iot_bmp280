
package com.ufc.telemetria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "leituras_validacao")
public class LeituraValidacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double temperaturaBmp;
    private Double pressao;

    private Double temperaturaAht;
    private Double umidade;

    private Double temperaturaAht30;
    private Double umidadeAht30;

    private Integer eco2;
    private Integer tvoc;

    private LocalDateTime dataHora;

    public LeituraValidacao() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now(
                    ZoneId.of("America/Fortaleza")
            );
        }
    }

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
