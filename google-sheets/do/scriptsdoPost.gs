function doPost(e) {
  try {
    // 1. Cria uma nova aba limpa para o formato largo (evita misturar os dados)
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var sheet = ss.getSheetByName("Test lareb 3");
    
    if (!sheet) {
      sheet = ss.insertSheet("BIA_PayloadUnico");
      sheet.appendRow(["Data/Hora", "Temp BMP280 (°C)", "Pressão (hPa)", "Temp AHT10 (°C)", "Umidade (%)"]);
    }
    
    // 2. Transforma o JSON do payload consolidado em objeto
    var dadosRecebidos = JSON.parse(e.postData.contents);
    
    // 3. Extrai as 4 grandezas (os nomes devem bater EXATAMENTE com o LeituraSensor.java)
    var tempBmp = dadosRecebidos.temperaturaBmp;
    var pressao = dadosRecebidos.pressao;
    var tempAht = dadosRecebidos.temperaturaAht;
    var umidade = dadosRecebidos.umidade;
    
    // 4. Marcação de data
    var dataHoraAtual = new Date();
    
    // 5. Salva a linha consolidada (1 ciclo = 1 linha completa)
    sheet.appendRow([dataHoraAtual, tempBmp, pressao, tempAht, umidade]);
    
    return ContentService.createTextOutput("Sucesso! Ciclo gravado completo.")
      .setMimeType(ContentService.MimeType.TEXT);
    
  } catch(erro) {
    return ContentService.createTextOutput("Erro no script: " + erro.toString())
      .setMimeType(ContentService.MimeType.TEXT);
  }
}