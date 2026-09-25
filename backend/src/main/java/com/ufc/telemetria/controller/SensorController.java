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

    private final SensorRepository repository;
    private final GoogleSheetsService googleSheetsService;

    public SensorController(SensorRepository repository, GoogleSheetsService googleSheetsService) {
        this.repository = repository;
        this.googleSheetsService = googleSheetsService;
    }

    // 1. Rota para o ESP32 enviar os dados consolidados (Payload Único - 3 Alturas + Qualidade do Ar)
    @PostMapping("/enviar")
    public ResponseEntity<LeituraSensor> receberDados(@RequestBody LeituraSensor dados) {
        
        // Log detalhado para auditar no Render
        logger.info("JSON RECEBIDO -> BMP: {}°C/{}hPa | AHT10: {}°C/{}% | AHT30: {}°C/{}% | eCO2: {} ppm | TVOC: {} ppb", 
            dados.getTemperaturaBmp(), dados.getPressao(), 
            dados.getTemperaturaAht(), dados.getUmidade(),
            dados.getTemperaturaAht30(), dados.getUmidadeAht30(),
            dados.getEco2(), dados.getTvoc());

        // Salva no Supabase
        LeituraSensor salvo = repository.save(dados);
        
        // Dispara envio assíncrono para o Google Sheets
        googleSheetsService.enviarParaSheets(salvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // 2. Rota para o Dashboard HTML
    @GetMapping("/dados")
    public ResponseEntity<List<LeituraSensor>> listarDados() {
        return ResponseEntity.ok(repository.findAll());
    }

    // 3. Rota para baixar o CSV atualizado (Todas as grandezas incluídas)
    @GetMapping("/csv")
    public ResponseEntity<String> baixarCsv() {
        List<LeituraSensor> leituras = repository.findAll();
        StringBuilder csv = new StringBuilder("ID;TEMP_BMP;PRESSAO;TEMP_AHT10;UMIDADE_AHT10;TEMP_AHT30;UMIDADE_AHT30;ECO2;TVOC;DATA_HORA\n");

        for (LeituraSensor l : leituras) {
            csv.append(l.getId()).append(";")
               .append(l.getTemperaturaBmp()).append(";")
               .append(l.getPressao()).append(";")
               .append(l.getTemperaturaAht()).append(";")
               .append(l.getUmidade()).append(";")
               .append(l.getTemperaturaAht30()).append(";")
               .append(l.getUmidadeAht30()).append(";")
               .append(l.getEco2()).append(";")
               .append(l.getTvoc()).append(";")
               .append(l.getDataHora()).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dados_telemetria_completo.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    // 4. Rota para limpar o banco
    @DeleteMapping("/limpar")
    public ResponseEntity<String> limparBanco() {
        repository.deleteAll();
        logger.warn("Banco de dados zerado via requisição HTTP.");
        return ResponseEntity.ok("Banco de dados zerado com sucesso!");
    }
}