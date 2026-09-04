package com.ufc.telemetria.controller;

import com.ufc.telemetria.model.LeituraSensor;
import com.ufc.telemetria.repository.SensorRepository;
import com.ufc.telemetria.service.GoogleSheetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    private static final Logger logger = LoggerFactory.getLogger(SensorController.class);

    // Injeção de dependência recomendada via Construtor
    private final SensorRepository repository;
    private final GoogleSheetsService googleSheetsService;

    public SensorController(SensorRepository repository, GoogleSheetsService googleSheetsService) {
        this.repository = repository;
        this.googleSheetsService = googleSheetsService;
    }

    // 1. Rota para o ESP32 enviar os dados
    @PostMapping("/enviar")
    public ResponseEntity<LeituraSensor> receberDados(@RequestBody LeituraSensor dados) {

        // Salva no banco de dados
        LeituraSensor salvo = repository.save(dados);
        logger.info("Salvo no banco com ID: {} | Sensor: {}", salvo.getId(), salvo.getSensor());

        // Dispara envio assíncrono para o Google Sheets (não bloqueia o retorno 201)
        googleSheetsService.enviarParaSheets(salvo);

        // Retorna status 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
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
    @DeleteMapping("/limpar")
    public ResponseEntity<String> limparBanco() {
        repository.deleteAll();
        logger.warn("Banco de dados zerado via requisição HTTP.");
        return ResponseEntity.ok("Banco de dados zerado com sucesso! Pronto para nova coleta.");
    }
}