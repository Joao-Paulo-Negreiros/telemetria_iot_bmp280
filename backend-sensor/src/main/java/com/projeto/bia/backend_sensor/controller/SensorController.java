package com.projeto.bia.backend_sensor.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.projeto.bia.backend_sensor.model.LeituraSensor;
import com.projeto.bia.backend_sensor.repository.SensorRepository;   
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // Liberado para o frontend HTML consumir qualquer rota
@RestController   
@RequestMapping("/api/sensor")
public class SensorController {

    @Autowired
    private SensorRepository repository; 

    // 1. Rota para o ESP32 enviar os dados
    @PostMapping("/enviar")
    public LeituraSensor receberDados(@RequestBody LeituraSensor dados) {
        LeituraSensor salvo = repository.save(dados);
        System.out.println("Salvo no banco com ID: " + salvo.getId() + " | Sensor: " + salvo.getSensor());
        return salvo; 
    }

    // 2. Rota para o Dashboard HTML ler os dados para o gráfico
    @GetMapping("/dados")
    public ResponseEntity<List<LeituraSensor>> listarDados() {
        return ResponseEntity.ok(repository.findAll());
    }

    // 3. Rota para baixar o CSV atualizado
    @GetMapping("/csv")
    public ResponseEntity<String> baixarCsv() {
        List<LeituraSensor> leituras = repository.findAll();

        StringBuilder csv = new StringBuilder("ID;TEMPERATURA;PRESSAO;SENSOR;DATA_HORA\n");

        for (LeituraSensor l : leituras) {
            csv.append(l.getId()).append(";")
               .append(l.getTemperatura()).append(";")
               .append(l.getPressao()).append(";")
               .append(l.getSensor()).append(";")
               .append(l.getDataHora()).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dados_telemetria.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    // 4. Rota para limpar o banco de dados
    @GetMapping("/limpar")
    public ResponseEntity<String> limparBanco() {
        repository.deleteAll();
        return ResponseEntity.ok("Banco de dados zerado com sucesso! Pronto para nova coleta.");
    }
} 