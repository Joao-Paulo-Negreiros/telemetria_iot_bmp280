/*
 * Telemetria BIA — Firmware ESP32 (BMP280 + AHT10 + SD + Deep Sleep)
 * Atualizado: Payload Único + HTTPS (Nuvem)  21/09/2026
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <Wire.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_AHTX0.h>
#include <SPI.h>
#include <SD.h>
#include "esp_sleep.h"
#include "secrets.h" 

#define TEMPO_SONO_SEGUNDOS 300
#define uS_PARA_S 1000000ULL

RTC_DATA_ATTR int contadorCiclos = 0;

const char* ssid        = WIFI_SSID;
const char* password    = WIFI_PASSWORD;
const char* urlServidor = SERVER_URL;

Adafruit_BMP280 bmp;
Adafruit_AHTX0 aht;

const int SD_CS_PIN = 4;
const char* LOG_FILE = "/dados.csv";
bool sdDisponivel = false;

// Atualizado para gravar as 4 grandezas na mesma linha
void logNoSD(float tempBMP, float pressao, float tempAHT, float umidade) {
  if (!sdDisponivel) return;

  if (!SD.exists(LOG_FILE)) {
    Serial.println("Arquivo nao existe. Criando agora...");
    File temp = SD.open(LOG_FILE, FILE_WRITE);
    if (temp) {
      temp.println("ciclo;millis;tempBMP;pressao;tempAHT;umidade");
      temp.close();
    } else {
      Serial.println("Erro critico: Falha ao forcar criacao do arquivo.");
    }
  }

  File arquivo = SD.open(LOG_FILE, FILE_APPEND);
  if (arquivo) {
    arquivo.print(contadorCiclos);
    arquivo.print(";");
    arquivo.print(millis());
    arquivo.print(";");
    arquivo.print(tempBMP, 2);
    arquivo.print(";");
    arquivo.print(pressao, 2);
    arquivo.print(";");
    arquivo.print(tempAHT, 2);
    arquivo.print(";");
    arquivo.println(umidade, 2);
    arquivo.close();
  }
}

// Dispara um único POST contendo as 4 grandezas via HTTPS
void enviarLeituraUnica(float tempBMP, float pressao, float tempAHT, float umidade) {
  logNoSD(tempBMP, pressao, tempAHT, umidade);

  if (WiFi.status() == WL_CONNECTED) {
    // 1. Cria o cliente Wi-Fi com suporte a SSL/TLS (HTTPS)
    WiFiClientSecure client;
    
    // 2. Ignora a validação restrita do certificado do servidor (Ideal para projetos acadêmicos)
    client.setInsecure(); 
    
    HTTPClient http;
    
    // 3. Inicia a conexão HTTP passando o cliente seguro e a URL
    http.begin(client, urlServidor); 
    http.addHeader("Content-Type", "application/json");

    // Montando o JSON no formato largo
    String json = "{";
    json += "\"temperaturaBmp\":" + String(tempBMP, 2) + ",";
    json += "\"pressao\":" + String(pressao, 2) + ",";
    json += "\"temperaturaAht\":" + String(tempAHT, 2) + ",";
    json += "\"umidade\":" + String(umidade, 2);
    json += "}";

    int resposta = http.POST(json);
    Serial.print("Payload Unico -> HTTPS Status: ");
    Serial.println(resposta);
    http.end();
  } else {
    Serial.println("Wi-Fi indisponivel, leitura salva apenas no SD.");
  }
}

void setup() {
  Serial.begin(115200);
  delay(200);

  contadorCiclos++;
  Serial.println("=====================================");
  Serial.print("Ciclo numero: ");
  Serial.println(contadorCiclos);

  Wire.begin(21, 22);

  if (!bmp.begin(0x76)) {
    Serial.println("Erro: Nao foi possivel encontrar o sensor BMP280!");
  }
  
  if (!aht.begin()) {
    Serial.println("Erro: Nao foi possivel encontrar o sensor AHT10!");
  } else {
    Serial.println("Sensor AHT10 detectado com sucesso!");
  }

  SPI.begin(18, 19, 23, SD_CS_PIN);
  if (!SD.begin(SD_CS_PIN)) {
    Serial.println("Aviso: cartao SD nao detectado. Log local desativado.");
    sdDisponivel = false;
  } else {
    sdDisponivel = true;
    Serial.println("Cartao SD detectado.");
    if (!SD.exists(LOG_FILE)) {
      File arquivo = SD.open(LOG_FILE, FILE_WRITE);
      if (arquivo) {
        arquivo.println("ciclo;millis;tempBMP;pressao;tempAHT;umidade");
        arquivo.close();
      }
    }
  }

  WiFi.begin(ssid, password);
  Serial.print("Conectando ao Wi-Fi");
  unsigned long inicioTentativa = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - inicioTentativa < 15000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nConectado a rede Wi-Fi!");
  } else {
    Serial.println("\nNao foi possivel conectar ao Wi-Fi.");
  }

  // --- LEITURA CONSOLIDADA DOS SENSORES ---
  
  // 1. Lê BMP280
  float temperaturaBMP = bmp.readTemperature();
  float pressaoBMP = bmp.readPressure() / 100.0F;
  
  // Respiro de 100ms no barramento I2C para evitar colisão
  delay(100); 

  // 2. Lê AHT10
  sensors_event_t eventHum, eventTemp;
  aht.getEvent(&eventHum, &eventTemp);
  float temperaturaAHT = eventTemp.temperature;
  float umidadeAHT = eventHum.relative_humidity;

  // 3. Envia e grava tudo de uma vez
  enviarLeituraUnica(temperaturaBMP, pressaoBMP, temperaturaAHT, umidadeAHT);

  // --- FIM DA LEITURA ---

  WiFi.disconnect(true);
  WiFi.mode(WIFI_OFF);

  Serial.print("Entrando em Deep Sleep por ");
  Serial.print(TEMPO_SONO_SEGUNDOS);
  Serial.println(" segundos...");
  Serial.flush();

  esp_sleep_enable_timer_wakeup(TEMPO_SONO_SEGUNDOS * uS_PARA_S);
  esp_deep_sleep_start();
}

void loop() {}