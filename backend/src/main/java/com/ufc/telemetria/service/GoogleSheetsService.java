package com.ufc.telemetria.service;

import com.ufc.telemetria.model.LeituraValidacao;
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

import java.util.LinkedHashMap;
import java.util.Map;

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

    @Async
    public void enviarValidacaoParaSheets(LeituraValidacao leitura) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("tipo", "validacao");
            payload.put("temperaturaBmp", leitura.getTemperaturaBmp());
            payload.put("pressao", leitura.getPressao());
            payload.put("temperaturaAht", leitura.getTemperaturaAht());
            payload.put("umidade", leitura.getUmidade());
            payload.put("temperaturaAht30", leitura.getTemperaturaAht30());
            payload.put("umidadeAht30", leitura.getUmidadeAht30());
            payload.put("eco2", leitura.getEco2());
            payload.put("tvoc", leitura.getTvoc());

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(payload, headers);

            String resposta = restTemplate.postForObject(
                    urlGoogleScript,
                    request,
                    String.class
            );

            logger.info("Validação enviada para o Google Sheets: {}", resposta);

        } catch (Exception e) {
            logger.error("Erro ao enviar validação para o Google Sheets: {}", e.getMessage());
        }
    }
}
