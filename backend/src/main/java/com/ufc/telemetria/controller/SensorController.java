
package com.ufc.telemetria.controller;

import com.ufc.telemetria.model.LeituraSensor;
import com.ufc.telemetria.model.LeituraValidacao;
import com.ufc.telemetria.repository.SensorRepository;
import com.ufc.telemetria.repository.ValidacaoRepository;
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

    private static final Logger logger =
            LoggerFactory.getLogger(SensorController.class);

    private final SensorRepository repository;
    private final ValidacaoRepository validacaoRepository;
    private final GoogleSheetsService googleSheetsService;

    public SensorController(
            SensorRepository repository,
            ValidacaoRepository validacaoRepository,
            GoogleSheetsService googleSheetsService) {

        this.repository = repository;
        this.validacaoRepository = validacaoRepository;
        this.googleSheetsService = googleSheetsService;
    }

    // =========================================================
    // 1. FLUXO HISTORICO
    // =========================================================

    @PostMapping("/enviar")
    public ResponseEntity<LeituraSensor> receberDados(
            @RequestBody LeituraSensor dados) {

        logger.info(
                "LEITURA HISTORICA -> BMP: {}°C/{}hPa | AHT10: {}°C/{}% | AHT30: {}°C/{}% | eCO2: {} ppm | TVOC: {} ppb",
                dados.getTemperaturaBmp(),
                dados.getPressao(),
                dados.getTemperaturaAht(),
                dados.getUmidade(),
                dados.getTemperaturaAht30(),
                dados.getUmidadeAht30(),
                dados.getEco2(),
                dados.getTvoc()
        );

        LeituraSensor salvo = repository.save(dados);

        googleSheetsService.enviarParaSheets(salvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/dados")
    public ResponseEntity<List<LeituraSensor>> listarDados() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/csv")
    public ResponseEntity<String> baixarCsv() {

        List<LeituraSensor> leituras = repository.findAll();

        StringBuilder csv = new StringBuilder(
                "ID;TEMP_BMP;PRESSAO;TEMP_AHT10;UMIDADE_AHT10;TEMP_AHT30;UMIDADE_AHT30;ECO2;TVOC;DATA_HORA\n"
        );

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
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"dados_telemetria_completo.csv\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    @DeleteMapping("/limpar")
    public ResponseEntity<String> limparBanco() {

        repository.deleteAll();

        logger.warn("Banco historico zerado via requisicao HTTP.");

        return ResponseEntity.ok("Banco de dados zerado com sucesso!");
    }

    // =========================================================
    // 2. FLUXO DE VALIDACAO
    // =========================================================

    @PostMapping("/validacao")
    public ResponseEntity<LeituraValidacao> receberDadosValidacao(
            @RequestBody LeituraValidacao dados) {

        logger.info(
                "LEITURA DE VALIDACAO -> BMP: {}°C/{}hPa | AHT10: {}°C/{}% | AHT30: {}°C/{}% | eCO2: {} ppm | TVOC: {} ppb",
                dados.getTemperaturaBmp(),
                dados.getPressao(),
                dados.getTemperaturaAht(),
                dados.getUmidade(),
                dados.getTemperaturaAht30(),
                dados.getUmidadeAht30(),
                dados.getEco2(),
                dados.getTvoc()
        );

        LeituraValidacao salvo = validacaoRepository.save(dados);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/validacao/dados")
    public ResponseEntity<List<LeituraValidacao>> listarDadosValidacao() {

        return ResponseEntity.ok(
                validacaoRepository.findAll()
        );
    }

    @GetMapping("/validacao/csv")
    public ResponseEntity<String> baixarCsvValidacao() {

        List<LeituraValidacao> leituras =
                validacaoRepository.findAll();

        StringBuilder csv = new StringBuilder(
                "ID;TEMP_BMP;PRESSAO;TEMP_AHT10;UMIDADE_AHT10;TEMP_AHT30;UMIDADE_AHT30;ECO2;TVOC;DATA_HORA\n"
        );

        for (LeituraValidacao l : leituras) {

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
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"dados_validacao.csv\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }
}
