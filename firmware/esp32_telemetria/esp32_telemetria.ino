/*
 * Telemetria BIA — Firmware ESP32 (PRINCIPAL)
 * ---------------------------------------------------------------------
 * Versão de produção: Deep Sleep via RTC timer + backup local em cartão SD.
 * Substitui a versão anterior baseada em delay() no loop().
 *
 * COMPORTAMENTO: o ESP32 acorda, lê os sensores, grava no SD, envia via
 * Wi-Fi/HTTP, desliga o rádio e volta a dormir. Todo o ciclo roda dentro
 * do setup() — o loop() fica vazio, pois o Deep Sleep reinicia a placa
 * a cada ciclo.
 *
 * PINAGEM:
 *   BMP280  (I2C)     -> SDA = GPIO 21, SCL = GPIO 22
 *   DS18B20 (1-Wire)  -> GPIO 5
 *   Módulo SD (SPI)   -> CS = GPIO 4, SCK = GPIO 18, MISO = GPIO 19, MOSI = GPIO 23
 *
 * Requer secrets.h na mesma pasta (WIFI_SSID, WIFI_PASSWORD, SERVER_URL).
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Wire.h>
#include <Adafruit_BMP280.h>
#include <SPI.h>
#include <SD.h>
#include "esp_sleep.h"
#include "secrets.h"

// ⏱️ TEMPO DE SONO — 600s (10 min), conforme arquitetura definida do projeto.
// Para testes rápidos, pode reduzir temporariamente (ex: 30), mas volte para
// 600 antes de deixar rodando de forma contínua.
#define TEMPO_SONO_SEGUNDOS 600
#define uS_PARA_S 1000000ULL

// Contador de ciclos — sobrevive ao Deep Sleep (memória RTC)
RTC_DATA_ATTR int contadorCiclos = 0;

const char* ssid        = WIFI_SSID;
const char* password    = WIFI_PASSWORD;
const char* urlServidor = SERVER_URL;

const int pinoSensor = 5;
OneWire oneWire(pinoSensor);
DallasTemperature sensorDS(&oneWire);
Adafruit_BMP280 bmp;

const int SD_CS_PIN = 4;
const char* LOG_FILE = "/telemetria_log.csv";
bool sdDisponivel = false;

void logNoSD(const String& sensor, float temperatura, float pressao) {
  if (!sdDisponivel) return;

  File arquivo = SD.open(LOG_FILE, FILE_APPEND);
  if (!arquivo) {
    Serial.println("Erro: nao foi possivel abrir o arquivo de log no SD.");
    return;
  }

  arquivo.print(contadorCiclos);
  arquivo.print(";");
  arquivo.print(millis());
  arquivo.print(";");
  arquivo.print(sensor);
  arquivo.print(";");
  arquivo.print(temperatura, 2);
  arquivo.print(";");
  arquivo.println(pressao, 2);

  arquivo.close();
}

void enviarLeitura(const char* nomeSensor, float temperatura, float pressao) {
  logNoSD(nomeSensor, temperatura, pressao);

  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(urlServidor);
    http.addHeader("Content-Type", "application/json");

    String json = "{\"temperatura\": " + String(temperatura) +
                   ", \"pressao\": " + String(pressao) +
                   ", \"sensor\": \"" + nomeSensor + "\"}";

    int resposta = http.POST(json);
    Serial.print(nomeSensor);
    Serial.print(" -> HTTP: ");
    Serial.println(resposta);
    http.end();
  } else {
    Serial.print(nomeSensor);
    Serial.println(" -> Wi-Fi indisponivel, leitura salva apenas no SD.");
  }
}

void setup() {
  Serial.begin(115200);
  delay(200);

  contadorCiclos++;
  Serial.println("=====================================");
  Serial.print("Ciclo numero: ");
  Serial.println(contadorCiclos);

  esp_sleep_wakeup_cause_t motivo = esp_sleep_get_wakeup_cause();
  if (motivo == ESP_SLEEP_WAKEUP_TIMER) {
    Serial.println("Acordou por temporizador (Deep Sleep).");
  } else {
    Serial.println("Boot inicial (nao foi wake de Deep Sleep).");
  }

  // Sensores
  sensorDS.begin();
  if (!bmp.begin(0x76)) {
    Serial.println("Erro: Nao foi possivel encontrar o sensor BMP280!");
  }

  // Cartão SD
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
        arquivo.println("ciclo;millis;sensor;temperatura;pressao");
        arquivo.close();
      }
    }
  }

  // Wi-Fi
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
    Serial.println("\nNao foi possivel conectar ao Wi-Fi em 15s. Seguindo apenas com log no SD.");
  }

  // Leituras e envio
  float temperaturaBMP = bmp.readTemperature();
  float pressaoBMP = bmp.readPressure() / 100.0F;
  enviarLeitura("BMP280", temperaturaBMP, pressaoBMP);

  delay(500);

  sensorDS.requestTemperatures();
  float temperaturaDS = sensorDS.getTempCByIndex(0);
  enviarLeitura("DS18B20", temperaturaDS, 0.0);

  // Desliga o radio explicitamente antes de dormir
  WiFi.disconnect(true);
  WiFi.mode(WIFI_OFF);

  Serial.print("Entrando em Deep Sleep por ");
  Serial.print(TEMPO_SONO_SEGUNDOS);
  Serial.println(" segundos...");
  Serial.flush();

  esp_sleep_enable_timer_wakeup(TEMPO_SONO_SEGUNDOS * uS_PARA_S);
  esp_deep_sleep_start();
}

void loop() {
  // Vazio de propósito: toda a lógica roda uma vez por ciclo dentro do setup()
}