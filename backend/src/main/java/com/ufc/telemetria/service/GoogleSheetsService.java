package com.ufc.telemetria.service;

import com.ufc.telemetria.model.LeituraSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleSheetsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.script.url}")
    private String urlGoogleScript;

    @Async
    public void enviarParaSheets(LeituraSensor leitura) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LeituraSensor> request = new HttpEntity<>(leitura, headers);

            String resposta = restTemplate.postForObject(urlGoogleScript, request, String.class);
            logger.info("Enviado para o Google Sheets: {}", resposta);
        } catch (Exception e) {
            logger.error("Erro ao enviar para o Google Sheets: {}", e.getMessage());
        }
    }
}
