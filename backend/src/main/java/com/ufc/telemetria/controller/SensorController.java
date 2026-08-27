package com.ufc.telemetria.controller;

import com.ufc.telemetria.model.LeituraSensor;
import com.ufc.telemetria.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    private static final Logger logger = LoggerFactory.getLogger(SensorController.class);

    // Injeção de dependência recomendada via Construtor
    private final SensorRepository repository;

    // Busca a URL do application.properties (ex: google.script.url=SUA_URL_AQUI)
    // Se não achar, usa a URL padrão configurada após os dois pontos
    @Value("${google.script.url}")
    private String urlGoogleScript;

    public SensorController(SensorRepository repository) {
        this.repository = repository;
    }

    // 1. Rota para o ESP32 enviar os dados
    @PostMapping("/enviar")
    public ResponseEntity<LeituraSensor> receberDados(@RequestBody LeituraSensor dados) {

        // Salva no banco de dados
        LeituraSensor salvo = repository.save(dados);
        logger.info("Salvo no banco com ID: {} | Sensor: {}", salvo.getId(), salvo.getSensor());

        // Tenta enviar uma cópia para o Google Sheets
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LeituraSensor> request = new HttpEntity<>(salvo, headers);
            String respostaGoogle = restTemplate.postForObject(urlGoogleScript, request, String.class);

            logger.info("Enviado para o Google Sheets: {}", respostaGoogle);
        } catch (Exception e) {
            logger.error("Erro ao enviar para o Google Sheets: {}", e.getMessage());
        }

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

    // 4. Rota para limpar o banco de dados (Alterado para DeleteMapping)
    @DeleteMapping("/limpar")
    public ResponseEntity<String> limparBanco() {
        repository.deleteAll();
        logger.warn("Banco de dados zerado via requisição HTTP.");
        return ResponseEntity.ok("Banco de dados zerado com sucesso! Pronto para nova coleta.");
    }
}