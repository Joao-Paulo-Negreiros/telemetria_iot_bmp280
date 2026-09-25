function doPost(e) {

  try {

    // ====================================================
    // PLANILHA
    // ====================================================

    var ss = SpreadsheetApp.getActiveSpreadsheet();

    var nomeAba = "casa amarela";

    var sheet = ss.getSheetByName(nomeAba);

    // ====================================================
    // CABEÇALHO
    // ====================================================

    var cabecalho = [
      "Data/Hora",
      "Temp BMP280 (°C)",
      "Pressão (hPa)",
      "Temp AHT10 (°C)",
      "Umidade AHT10 (%)",
      "Temp AHT30 (°C)",
      "Umidade AHT30 (%)",
      "eCO2 (ppm)",
      "TVOC (ppb)"
    ];

    // Cria a aba caso ela não exista
    if (!sheet) {
      sheet = ss.insertSheet(nomeAba);
    }

    // Garante que a primeira linha tenha o cabeçalho correto.
    // Isso também corrige a planilha que já existia
    // com "AHT21" escrito no cabeçalho.
    sheet
      .getRange(1, 1, 1, cabecalho.length)
      .setValues([cabecalho]);

    // ====================================================
    // LEITURA DO JSON
    // ====================================================

    var dadosRecebidos =
      JSON.parse(e.postData.contents);

    // ====================================================
    // BMP280
    // ====================================================

    var tempBmp =
      dadosRecebidos.temperaturaBmp;

    var pressao =
      dadosRecebidos.pressao;

    // ====================================================
    // AHT10
    // ====================================================

    var tempAht10 =
      dadosRecebidos.temperaturaAht;

    var umidadeAht10 =
      dadosRecebidos.umidade;

    // ====================================================
    // AHT30
    // ====================================================

    var tempAht30 =
      dadosRecebidos.temperaturaAht30;

    var umidadeAht30 =
      dadosRecebidos.umidadeAht30;

    // ====================================================
    // ENS160
    // ====================================================

    var eco2 =
      dadosRecebidos.eco2;

    var tvoc =
      dadosRecebidos.tvoc;

    // ====================================================
    // DATA / HORA
    // ====================================================

    var dataHoraAtual =
      new Date();

    // ====================================================
    // SALVAMENTO
    // ====================================================
    // Um ciclo = uma única linha com todas as grandezas.

    sheet.appendRow([

      dataHoraAtual,

      tempBmp,

      pressao,

      tempAht10,

      umidadeAht10,

      tempAht30,

      umidadeAht30,

      eco2,

      tvoc

    ]);

    // ====================================================
    // RESPOSTA
    // ====================================================

    return ContentService
      .createTextOutput(
        "Sucesso! Ciclo gravado completo."
      )
      .setMimeType(
        ContentService.MimeType.TEXT
      );

  } catch (erro) {

    return ContentService
      .createTextOutput(
        "Erro no script: " + erro.toString()
      )
      .setMimeType(
        ContentService.MimeType.TEXT
      );
  }
}