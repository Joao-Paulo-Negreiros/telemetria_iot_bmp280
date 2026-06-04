package com.projeto.bia.backend_sensor.model;

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
    
    // Salva o momento da leitura
    private LocalDateTime dataHora = LocalDateTime.now();

    // Construtor vazio (obrigatório para o Spring)
    public LeituraSensor() {}

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

    public void setSensor(String sensor){
        this.sensor = sensor;
    }

}