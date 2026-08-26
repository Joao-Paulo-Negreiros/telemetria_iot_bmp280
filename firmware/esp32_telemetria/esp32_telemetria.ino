#include <WiFi.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Wire.h>
#include <Adafruit_BMP280.h>
#include "secrets.h"  // Credenciais locais — NÃO versionado no Git

// Credenciais da rede Wi-Fi — definidas em secrets.h (não versionado)
const char* ssid     = WIFI_SSID;
const char* password = WIFI_PASSWORD;

// Configuração do pino do sensor DS18B20 (Protocolo 1-Wire)
const int pinoSensor = 5;
OneWire oneWire(pinoSensor);
DallasTemperature sensorDS(&oneWire);

// Configuração do sensor BMP280 (Protocolo I2C - Pinos D21 e D22 padrão)
Adafruit_BMP280 bmp;

// Endereço da API Java — definido em secrets.h (não versionado)
// Se o IP mudar: edite secrets.h com o novo IPv4 (ipconfig no terminal)
const char* urlServidor = SERVER_URL;

void setup() {
  Serial.begin(115200);
  
  // Inicializa os sensores
  sensorDS.begin();

// O endereço 0x76 é o padrão para a maioria dos módulos BMP280
  if (!bmp.begin(0x76)) {
    Serial.println("Erro: Nao foi possivel encontrar o sensor BMP280!");
  }

// Inicializa a conexão com a rede Wi-Fi
  WiFi.begin(ssid, password);
  Serial.print("Conectando ao Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConectado a rede Wi-Fi!");
}

void loop() { // Só tenta executar a coleta e o envio se a placa estiver online
  if (WiFi.status() == WL_CONNECTED) {

    // BLOCO 1: LEITURA E ENVIO DO BMP280
    float temperaturaBMP = bmp.readTemperature();
    float pressaoBMP = bmp.readPressure() / 100.0F;  // Converte de Pa para hPa

    HTTPClient httpBMP;
    httpBMP.begin(urlServidor);
    httpBMP.addHeader("Content-Type", "application/json");

// Montagem manual do contrato JSON
    String jsonBMP = "{\"temperatura\": " + 
    String(temperaturaBMP) + ", \"pressao\": " +
    String(pressaoBMP) +
    ", \"sensor\": \"BMP280\"}";
    
    Serial.print("BMP280 -> Temp: ");
    Serial.print(temperaturaBMP);
    Serial.print(" *C | Pressao: ");
    Serial.print(pressaoBMP);
    Serial.println(" hPa");

// Dispara o POST para o Java e guarda a resposta (ex: 200 = Sucesso)
    int respostaBMP = httpBMP.POST(jsonBMP);
    Serial.print("HTTP BMP280: ");
    Serial.println(respostaBMP);
    httpBMP.end();

// Pausa de 500ms projetada para evitar concorrência de rede
    delay(500);

    // --- BLOCO 2: LEITURA E ENVIO DO DS18B20 ---
    sensorDS.requestTemperatures();  // Dispara o comando de leitura no barramento
    float temperaturaDS = sensorDS.getTempCByIndex(0);

    HTTPClient httpDS;
    httpDS.begin(urlServidor);
    httpDS.addHeader("Content-Type", "application/json");

// Envia pressão 0.0 para manter a integridade da tabela no banco de dados
    String jsonDS = "{\"temperatura\": " + String(temperaturaDS) + ", \"pressao\": 0.0, \"sensor\": \"DS18B20\"}";

    Serial.print("DS18B20 -> Temp: ");
    Serial.print(temperaturaDS);
    Serial.println(" *C");

    int respostaDS = httpDS.POST(jsonDS);
    Serial.print("HTTP DS18B20: ");
    Serial.println(respostaDS);
    httpDS.end();

  } else {
    Serial.println("Erro: Wi-Fi desconectado.");
  }
  // Aguarda 5 segundos antes de reiniciar o ciclo global de monitorização
  delay(5000);
}