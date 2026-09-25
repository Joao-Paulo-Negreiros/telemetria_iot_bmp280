/*
 * Telemetria BIA — Firmware ESP32
 * Sensores: BMP280 + AHT10 + AHT30 + ENS160 + Cartão SD + Deep Sleep
 * Arquitetura: 2 Barramentos I2C independentes
 *   - Wire = I2C0, GPIO 21/22
 *   - I2C_Secundario = I2C1, GPIO 16/17
 * Data: 25/09/2026
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <Wire.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_AHTX0.h>
#include "ScioSense_ENS160.h"
#include <SPI.h>
#include <SD.h>
#include "esp_sleep.h"
#include "secrets.h"

// ====================================================
// CONFIGURAÇÃO DO DEEP SLEEP
// ====================================================

#define TEMPO_SONO_SEGUNDOS 300
#define uS_PARA_S 1000000ULL

// ====================================================
// BARRAMENTO I2C SECUNDÁRIO
// ====================================================
// O AHT10 fica isolado neste barramento para evitar
// conflito de endereço I2C com o AHT30.

#define I2C_SEC_SDA 16
#define I2C_SEC_SCL 17

// ====================================================
// CONTADOR DE CICLOS
// ====================================================
// Esta variável permanece na memória RTC e sobrevive
// ao Deep Sleep.

RTC_DATA_ATTR int contadorCiclos = 0;

// ====================================================
// CREDENCIAIS / SERVIDOR
// ====================================================

const char* ssid = WIFI_SSID;
const char* password = WIFI_PASSWORD;
const char* serverUrl = SERVER_URL;

// ====================================================
// OBJETOS DOS BARRAMENTOS I2C
// ====================================================

// Segundo controlador físico I2C do ESP32
TwoWire I2C_Secundario = TwoWire(1);

// ====================================================
// SENSORES
// ====================================================

// BMP280 e AHT30 utilizam o barramento principal
// Wire = GPIO 21 (SDA) / GPIO 22 (SCL)
Adafruit_BMP280 bmp;
Adafruit_AHTX0 aht30;

// ENS160 está no mesmo barramento principal
ScioSense_ENS160 ens160(ENS160_I2CADDR_1);

// AHT10 utiliza o barramento secundário
Adafruit_AHTX0 aht10;

// ====================================================
// MICROSD
// ====================================================

const int SD_CS_PIN = 4;
const char* LOG_FILE = "/dados.csv";

bool sdDisponivel = false;

// ====================================================
// GRAVAÇÃO NO CARTÃO SD
// ====================================================

void logNoSD(
  float tempBMP,
  float pressao,
  float tempAHT10,
  float umidAHT10,
  float tempAHT30,
  float umidAHT30,
  int eco2,
  int tvoc
) {

  if (!sdDisponivel) {
    return;
  }

  // Cria o arquivo caso ainda não exista
  if (!SD.exists(LOG_FILE)) {

    Serial.println("Arquivo nao existe. Criando agora...");

    File temp = SD.open(LOG_FILE, FILE_WRITE);

    if (temp) {

      temp.println(
        "ciclo;millis;tempBMP;pressao;tempAHT10;umidAHT10;tempAHT30;umidAHT30;eco2;tvoc"
      );

      temp.close();

    } else {

      Serial.println("Erro critico: Falha ao criar arquivo no SD.");

    }
  }

  // Abre o arquivo para adicionar uma nova linha
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

    arquivo.print(tempAHT10, 2);
    arquivo.print(";");

    arquivo.print(umidAHT10, 2);
    arquivo.print(";");

    arquivo.print(tempAHT30, 2);
    arquivo.print(";");

    arquivo.print(umidAHT30, 2);
    arquivo.print(";");

    arquivo.print(eco2);
    arquivo.print(";");

    arquivo.println(tvoc);

    arquivo.close();

    Serial.println("Leitura gravada no SD.");

  } else {

    Serial.println("Erro: Nao foi possivel abrir o arquivo no SD.");

  }
}

// ====================================================
// ENVIO DO PAYLOAD ÚNICO
// ====================================================
// Um ciclo = um único POST contendo todas as grandezas.

void enviarLeituraUnica(
  float tempBMP,
  float pressao,
  float tempAHT10,
  float umidAHT10,
  float tempAHT30,
  float umidAHT30,
  int eco2,
  int tvoc
) {

  // Primeiro salva localmente no SD
  // Isso garante o backup antes da tentativa HTTP.
  logNoSD(
    tempBMP,
    pressao,
    tempAHT10,
    umidAHT10,
    tempAHT30,
    umidAHT30,
    eco2,
    tvoc
  );

  // Verifica se o Wi-Fi está conectado
  if (WiFi.status() == WL_CONNECTED) {

    WiFiClientSecure client;

    // Mantido para o teste atual.
    // Futuramente pode ser substituído pela validação
    // do certificado CA do servidor.
    client.setInsecure();

    HTTPClient http;

    // Timeout de até 60 segundos para tolerar
    // eventual inicialização lenta do backend em nuvem.
    http.setTimeout(60000);

    if (!http.begin(client, serverUrl)) {

      Serial.println("Erro ao iniciar conexao HTTP.");
      return;

    }

    http.addHeader("Content-Type", "application/json");

    // ====================================================
    // MONTA O JSON
    // ====================================================

    String json = "{";

    json += "\"temperaturaBmp\":" + String(tempBMP, 2) + ",";
    json += "\"pressao\":" + String(pressao, 2) + ",";
    json += "\"temperaturaAht\":" + String(tempAHT10, 2) + ",";
    json += "\"umidade\":" + String(umidAHT10, 2) + ",";
    json += "\"temperaturaAht30\":" + String(tempAHT30, 2) + ",";
    json += "\"umidadeAht30\":" + String(umidAHT30, 2) + ",";
    json += "\"eco2\":" + String(eco2) + ",";
    json += "\"tvoc\":" + String(tvoc);

    json += "}";

    Serial.println("=====================================");
    Serial.println("Payload enviado:");
    Serial.println(json);
    Serial.println("=====================================");

    // ====================================================
    // ENVIO
    // ====================================================

    int resposta = http.POST(json);

    Serial.print("Payload Unico -> HTTPS Status: ");
    Serial.println(resposta);

    if (resposta > 0) {

      String payloadResposta = http.getString();

      Serial.print("Resposta da API: ");
      Serial.println(payloadResposta);

    } else {

      Serial.print("Falha na requisicao. Erro: ");
      Serial.println(http.errorToString(resposta).c_str());

    }

    http.end();

  } else {

    Serial.println(
      "Wi-Fi indisponivel, leitura gravada apenas no SD."
    );

  }
}

// ====================================================
// SETUP
// ====================================================

void setup() {

  Serial.begin(115200);
  delay(200);

  // ====================================================
  // CONTADOR
  // ====================================================

  contadorCiclos++;

  Serial.println();
  Serial.println("=====================================");
  Serial.print("Ciclo numero: ");
  Serial.println(contadorCiclos);

  // ====================================================
  // I2C
  // ====================================================

  // Barramento principal
  // GPIO 21 = SDA
  // GPIO 22 = SCL
  Wire.begin(21, 22);

  // Barramento secundário
  // GPIO 16 = SDA
  // GPIO 17 = SCL
  I2C_Secundario.begin(
    I2C_SEC_SDA,
    I2C_SEC_SCL
  );

  // ====================================================
  // BMP280
  // ====================================================

  if (!bmp.begin(0x76)) {

    Serial.println(
      "Erro: BMP280 nao detectado no I2C Principal!"
    );

  } else {

    Serial.println(
      "Sensor BMP280 detectado com sucesso!"
    );

  }

  // ====================================================
  // AHT30
  // ====================================================

  if (!aht30.begin(&Wire)) {

    Serial.println(
      "Erro: AHT30 nao detectado no I2C Principal!"
    );

  } else {

    Serial.println(
      "Sensor AHT30 detectado com sucesso!"
    );

  }

  // ====================================================
  // ENS160
  // ====================================================

  if (!ens160.begin()) {

    Serial.println(
      "Erro: ENS160 nao detectado no I2C Principal!"
    );

  } else {

    Serial.println(
      "Sensor ENS160 detectado com sucesso!"
    );

    // Modo de operação padrão
    ens160.setMode(ENS160_OPMODE_STD);

  }

  // ====================================================
  // AHT10
  // ====================================================

  if (!aht10.begin(&I2C_Secundario)) {

    Serial.println(
      "Erro: AHT10 nao detectado no I2C Secundario!"
    );

  } else {

    Serial.println(
      "Sensor AHT10 detectado com sucesso!"
    );

  }

  // ====================================================
  // CARTÃO SD
  // ====================================================

  SPI.begin(
    18,  // SCK
    19,  // MISO
    23,  // MOSI
    SD_CS_PIN
  );

  if (!SD.begin(SD_CS_PIN)) {

    Serial.println(
      "Aviso: Cartao SD nao detectado. Log desativado."
    );

    sdDisponivel = false;

  } else {

    sdDisponivel = true;

    Serial.println(
      "Cartao SD detectado com sucesso."
    );

  }

  // ====================================================
  // WI-FI
  // ====================================================

  WiFi.begin(ssid, password);

  Serial.print("Conectando ao Wi-Fi");

  unsigned long inicioTentativa = millis();

  while (
    WiFi.status() != WL_CONNECTED &&
    millis() - inicioTentativa < 15000
  ) {

    delay(500);
    Serial.print(".");

  }

  if (WiFi.status() == WL_CONNECTED) {

    Serial.println();
    Serial.println("Conectado ao Wi-Fi!");

  } else {

    Serial.println();
    Serial.println("Falha na conexao Wi-Fi.");

  }

  // ====================================================
  // LEITURA DO BMP280
  // ====================================================

  float tempBMP = bmp.readTemperature();

  float pressaoBMP =
    bmp.readPressure() / 100.0F;

  // ====================================================
  // LEITURA DO AHT10
  // ====================================================

  sensors_event_t hum10;
  sensors_event_t temp10;

  aht10.getEvent(
    &hum10,
    &temp10
  );

  float tempAHT10 =
    temp10.temperature;

  float umidAHT10 =
    hum10.relative_humidity;

  // ====================================================
  // LEITURA DO AHT30
  // ====================================================

  sensors_event_t hum30;
  sensors_event_t temp30;

  aht30.getEvent(
    &hum30,
    &temp30
  );

  float tempAHT30 =
    temp30.temperature;

  float umidAHT30 =
    hum30.relative_humidity;

  // ====================================================
  // LEITURA DO ENS160
  // ====================================================

  int eco2Val = 0;
  int tvocVal = 0;

  unsigned long esperaENS = millis();

  // Aguarda até 1,5 segundo para o ENS160 disponibilizar leitura
  while (
    !ens160.available() &&
    millis() - esperaENS < 1500
  ) {

    delay(50);

  }

  if (ens160.available()) {

    ens160.measure(true);

    eco2Val = ens160.geteCO2();
    tvocVal = ens160.getTVOC();

  } else {

    Serial.println(
      "Aviso: ENS160 nao disponibilizou leitura."
    );

  }

  // ====================================================
  // ENVIO E BACKUP
  // ====================================================

  enviarLeituraUnica(
    tempBMP,
    pressaoBMP,
    tempAHT10,
    umidAHT10,
    tempAHT30,
    umidAHT30,
    eco2Val,
    tvocVal
  );

  // ====================================================
  // DESLIGA WI-FI
  // ====================================================

  WiFi.disconnect(true);
  WiFi.mode(WIFI_OFF);

  // ====================================================
  // DEEP SLEEP
  // ====================================================

  Serial.print(
    "Entrando em Deep Sleep por "
  );

  Serial.print(
    TEMPO_SONO_SEGUNDOS
  );

  Serial.println(
    " segundos..."
  );

  Serial.flush();

  esp_sleep_enable_timer_wakeup(
    TEMPO_SONO_SEGUNDOS * uS_PARA_S
  );

  esp_deep_sleep_start();
}

// ====================================================
// LOOP
// ====================================================
// Fica vazio porque todo o ciclo acontece no setup().

void loop() {
}