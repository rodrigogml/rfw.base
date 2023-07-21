package br.eng.rodrigogml.rfw.base.utils;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.utils.RUString;

/**
 * Description: Classe de utilidades de manipulação do CNPJ.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.1.0 (NOV / 2009)
 * @version 4.1.0 (24/06/2011) - rodrigogml - Nome alterado de DOcumentValidationUtils para ficar no padrão do Framework.
 */
public class BUDocValidation {

  /**
   * Tipos de Código de Barras de boleto e outros títulos bancário.
   *
   */
  public enum BankBillCodeType {
    /**
     * Código de barras do boleto.
     */
    BOLETO_BARCODE,
    /**
     * Linha digitável do boleto.
     */
    BOLETO_NUMERICCODE,
    /**
     * Código de barras de uma guia de arrecadação.
     */
    SERVICE_BARCODE,
    /**
     * Linha digitável de uma guia de arrecadação.
     */
    SERVICE_NUMERICCODE
  }

  // TODO RGML Rever os códigos de erros e colocar no bundle reconfigurado.

  /**
   * Classe estática, denecessário instancia-la.
   */
  private BUDocValidation() {
  }

  /**
   * Valida se pe uma UF válida para o Brasil.<br>
   * Este método simplesmente verifica se a string recebida é uma das UF do Brasil, considerando apenas 2 letras e ignorando o case.
   *
   * @param uf UFa ser validada.
   * @throws RFWValidationException
   */
  public static void validateUF(String uf) throws RFWException {
    if (uf == null) {
      throw new RFWValidationException("RFW_ERR_200417");
    }
    // Prepara a UF para comparação
    uf = uf.toUpperCase();
    switch (uf) {
      case "SP":
      case "AC":
      case "AL":
      case "AP":
      case "AM":
      case "BA":
      case "CE":
      case "DF":
      case "ES":
      case "GO":
      case "MA":
      case "MT":
      case "MS":
      case "MG":
      case "PA":
      case "PB":
      case "PR":
      case "PE":
      case "PI":
      case "RJ":
      case "RN":
      case "RS":
      case "RO":
      case "RR":
      case "SC":
      case "SE":
      case "TO":
        break;
      default:
        throw new RFWValidationException("RFW_ERR_200417");
    }

  }

  /**
   * Verifica se o código de barras é um código de boleto válido. <br>
   * O método remove quaisquer caracteres que não sejam dígitos do código de barras antes de validar.
   *
   * @param codebar O código de barras a ser verificado.
   * @throws RFWException
   */
  public static void isBoletoBarCodeValid(String codebar) throws RFWException {
    codebar = RUString.removeNonDigits(codebar);

    // Tamanho exato de 44 algarismos
    if (codebar.length() != 44) {
      throw new RFWValidationException("RFW_ERR_210054", new String[] { "44" });
    }

    // Removendo o DV da string
    String codeToCheck = codebar.substring(0, 4) + codebar.substring(5);
    // O DV tem que estar correto
    if (codebar.charAt(4) != BUDVCalc.calcMod11(codeToCheck).charAt(0)) {
      throw new RFWValidationException("RFW_ERR_210058");
    }

    // Se o código do banco não está zerado
    if (codebar.substring(0, 3) == "000") {
      throw new RFWValidationException("RFW_ERR_210057");
    }

    // Se o campo de moeda é 9, pois não conhecemos outro valor
    if (codebar.charAt(3) != '9') {
      throw new RFWValidationException("RFW_ERR_210063");
    }
  }

  /**
   * Verifica se a linha numérica (linha digitável) é uma linha de boleto válida. <br>
   * O método remove quaisquer caracteres que não sejam dígitos do código de barras antes de validar.
   *
   * @param numericLine the numeric line
   * @throws RFWException the RFWDeprec exception
   */
  public static void isBoletoNumericCodeValid(String numericLine) throws RFWException {
    numericLine = RUString.removeNonDigits(numericLine);

    // Se o tamanho está correto
    if (!(numericLine.length() == 33 || (numericLine.length() > 33 && numericLine.length() <= 36 && numericLine.substring(33) == "0000") || numericLine.length() >= 37 && numericLine.length() <= 47)) {
      throw new RFWValidationException("RFW_ERR_210055");
    }

    // Se possui código do banco preenchido
    if (numericLine.substring(0, 3) == "000") {
      throw new RFWValidationException("RFW_ERR_210057");
    }

    // Se o código da moeda é válido
    if (numericLine.charAt(3) != '9') {
      throw new RFWValidationException("RFW_ERR_210063");
    }

    // DV do primeiro bloco
    String firstBlockCode = numericLine.substring(0, 9);
    if (numericLine.charAt(9) != BUDVCalc.calcMod10(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("RFW_ERR_210060");
    }

    // DV do segundo bloco
    String secondBlockCode = numericLine.substring(10, 20);
    if (numericLine.charAt(20) != BUDVCalc.calcMod10(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("RFW_ERR_210061");
    }

    // DV do terceiro bloco
    String thirdBlockCode = numericLine.substring(21, 31);
    if (numericLine.charAt(31) != BUDVCalc.calcMod10(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("RFW_ERR_210062");
    }

    // O DV é calculado com base no código de barras e não no código numérico. Por isso primeiro vamos converter o código numérico no código de barras e depois calcular o DV
    String codeBar = convertNumericCodeToBarCode(numericLine);
    String codeBarWithoutDV = codeBar.substring(0, 4) + codeBar.substring(5);
    if (numericLine.charAt(32) != BUDVCalc.calcMod11(codeBarWithoutDV).charAt(0)) {
      throw new RFWValidationException("RFW_ERR_210058");
    }
  }

  /**
   * Converte o código numérico para o código de barras.<br>
   * Este método espera receber um código numérico válido. NÃO CALCULA O DV, copia ele do código numérico. NÃO VALIDA O RESULTADO!
   *
   * @param numericCode A linha numérica a ser convertida.
   * @return O código de barras convertido.
   * @throws RFWException
   */
  public static String convertNumericCodeToBarCode(String numericCode) throws RFWException {
    numericCode = RUString.removeNonDigits(numericCode);

    String barCode = null;
    final BankBillCodeType type = getCodeType(numericCode, false);
    if (type == null) throw new RFWValidationException("Código Numérico Inválido ou não conhecido pelo RFWDeprec!");

    switch (type) {
      case SERVICE_NUMERICCODE: {
        barCode = numericCode.substring(0, 11);
        barCode += numericCode.substring(12, 23);
        barCode += numericCode.substring(24, 35);
        barCode += numericCode.substring(36, 47);
      }
        break;
      case BOLETO_NUMERICCODE: {
        // Completa com zeros o código caso ele não esteja no tamaho correto para facilitar a conversão
        numericCode = RUString.completeUntilLengthRight("0", numericCode, 47);

        barCode = numericCode.substring(0, 4);
        barCode += numericCode.substring(32, 33);
        barCode += numericCode.substring(33, 47);
        barCode += numericCode.substring(4, 9);
        barCode += numericCode.substring(10, 20);
        barCode += numericCode.substring(21, 31);
      }
        break;
      case BOLETO_BARCODE:
      case SERVICE_BARCODE:
        // Não converte neste método
        barCode = numericCode;
        break;
    }
    return barCode;
  }

  /**
   * Identifica qual é o tipo de {@code BankBillCodeType} que uma string representa.<br>
   * ESTE MÉTODO FAZ A VALIDAÇÃO DOS DVs PARA GARANTIR QUE A NUMERAÇÃO ESTÁ SENDO CORRETAMENTE INTERPRETADA
   *
   * @param code O código a ser verificado.
   * @return Um {@code BankBillCodeType} que identifica o código passado ao método, OU nulo caso o código não seja reconhecido/válido
   * @throws RFWException
   */
  public static BankBillCodeType getCodeType(String code) throws RFWException {
    return getCodeType(code, true);
  }

  /**
   * Recupera o tipo do código de barra baseado no tamanho e estrutura do código.
   *
   * @param code Código a ser definido.
   * @param test Caso true, o método testa a estrutura e DV para garantir que é o código, caso false considera apenas o tamanho. (Em alguns casos não testa para evitar o loop infinito entre métodos).
   * @return the code type
   * @throws RFWException
   */
  private static BankBillCodeType getCodeType(String code, boolean test) throws RFWException {
    code = RUString.removeNonDigits(code);
    if (code.length() == 44) { // Provavelmente é código de barras
      if (code.startsWith("8")) { // Provavelmente código de arrecadação
        try {
          if (test) isServiceBarCodeValid(code);
          return BankBillCodeType.SERVICE_BARCODE;
        } catch (RFWValidationException e) {
        }
      }
      // Se não começa com 8, ou se não era válido, tentamos como boleto
      try {
        if (test) BUDocValidation.isBoletoBarCodeValid(code);
        return BankBillCodeType.BOLETO_BARCODE;
      } catch (RFWValidationException e) {
      }
    }
    // Se o tamanho não é 44, ou se falhou em validar como código de barras, vamos processar como representação numérica
    // A primeira é a representação de arrecadação, que é sempre fixa e é a maior
    if (code.length() == 48) {
      try {
        if (test) isServiceNumericCodeValid(code);
        return BankBillCodeType.SERVICE_NUMERICCODE;
      } catch (RFWValidationException e) {
      }
    } else if (code.length() >= 33 && code.length() <= 47) {
      // Se o tamanho está entre os tamanhos que a representação numérica do boleto pode ter, validamos para verificar
      try {
        if (test) isBoletoNumericCodeValid(code);
        return BankBillCodeType.BOLETO_NUMERICCODE;
      } catch (RFWValidationException e) {
      }
    }
    return null;
  }

  /**
   * Verifica se o código de barras é um código de guia de arrecadação/serviços válido. <br>
   * O método remove quaisquer caracteres que não sejam dígitos do código de barras antes de validar.
   *
   * @param codeBar O código de barras a ser verificado.
   * @throws RFWException
   */
  public static void isServiceBarCodeValid(String codeBar) throws RFWException {
    codeBar = RUString.removeNonDigits(codeBar);

    // Tamanho exato deve ser de 44 algarismos
    if (codeBar.length() != 44) {
      throw new RFWValidationException("O tamanho do código não é válido para uma guia de arrecadação/serviço.", new String[] { "44" });
    }

    // O primeiro dígito deve ser "8"
    if (codeBar.charAt(0) != '8') {
      throw new RFWValidationException("O código de barras da guia de arrecadação deve sempre começar com 8.");
    }

    // Removendo o DV da string
    String codeToCheck = codeBar.substring(0, 3) + codeBar.substring(4);

    // Identificador do valor deve ser:
    // 6 (Valor a ser cobrado efetivamente em reais) - com dígito verificador calculado pelo módulo 10
    // 7 (Quantidade de Moeda) - com dígito verificador calculado pelo módulo 10
    // 8 (Valor a ser cobrado efetivamente em reais) - com dígito verificador calculado pelo módulo 11
    // 9 (Quantidade de Moeda) - com dígito verificador calculado pelo módulo 11
    if (codeBar.charAt(2) != '6' && codeBar.charAt(2) != '7' && codeBar.charAt(2) != '8' && codeBar.charAt(2) != '9') {
      throw new RFWValidationException("O código da moeda do código de barras não é válido, ou não reconhecido pelo RFWDeprec.");
    }

    // Se o campo 'segmento' possui um valor válido, ou seja, diferente de zero e 8 (os únicos valores inválidos atualmente)
    if (codeBar.charAt(1) == '0' || codeBar.charAt(1) == '8') {
      throw new RFWValidationException("RFW_ERR_210065");
    }

    // O DV deve estar correto
    if ((codeBar.charAt(2) == '6' || codeBar.charAt(2) == '7') && codeBar.charAt(3) != BUDVCalc.calcMod10(codeToCheck).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do código de barras não está correto.");
    } else if ((codeBar.charAt(2) == '8' || codeBar.charAt(2) == '9')) {
      if (codeBar.charAt(1) == '5' || codeBar.charAt(1) == '1') { // 5 - Guias Governo // 1 - Prefeituras
        if (codeBar.charAt(3) != BUDVCalc.calcMod11ForServiceGovernment(codeToCheck).charAt(0)) {
          throw new RFWValidationException("O dígito verificador do código de barras não está correto.");
        }
      } else if (codeBar.charAt(3) != BUDVCalc.calcMod11(codeToCheck).charAt(0)) {
        throw new RFWValidationException("O dígito verificador do código de barras não está correto.");
      }
    }

  }

  /**
   * Verifica se a linha numérica (linha digitável) é uma linha de guia de arrecadação/serviços válida. <br>
   * O método remove quaisquer caracteres que não sejam dígitos do código de barras antes de validar.
   *
   * @param numericCode the numeric code
   * @throws RFWException
   */
  public static void isServiceNumericCodeValid(String numericCode) throws RFWException {
    numericCode = RUString.removeNonDigits(numericCode);

    // Tamanho exato de 48 algarismos
    if (numericCode.length() != 48) {
      throw new RFWValidationException("O tamanho do código numérico não é válido para uma conta serviço/guia de arrecadação.", new String[] { "48" });
    }

    // Como o a representação numérica é o próprio código de barras, só acrescido de um DV a cada 11 posições, vamos remover elas e fazer a validação do código de barras, depois só validamos os DVs.
    // Assim as validações ficam todas concentradas só no método do código de barras.
    String firstBlockCode = numericCode.substring(0, 11);
    String secondBlockCode = numericCode.substring(12, 23);
    String thirdBlockCode = numericCode.substring(24, 35);
    String fourthBlockCode = numericCode.substring(36, 47);

    String barCode = firstBlockCode + secondBlockCode + thirdBlockCode + fourthBlockCode;
    isServiceBarCodeValid(barCode);

    int modCalc;
    if (barCode.charAt(2) == '6' || barCode.charAt(2) == '7') {
      modCalc = 1;
    } else if (barCode.charAt(2) == '8' || barCode.charAt(2) == '9') {
      if (barCode.charAt(1) == '5' || barCode.charAt(1) == '1') { // 5 - Guias de Arrecadação Governo / 1 - Prefeituras
        modCalc = 3;
      } else {
        modCalc = 2;
      }
    } else {
      throw new RFWValidationException("Código do Identificador desconhecido pelo RFWDeprec!");
    }

    // Validar DV do primeiro bloco
    if (modCalc == 1 && numericCode.charAt(11) != BUDVCalc.calcMod10(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 2 && numericCode.charAt(11) != BUDVCalc.calcMod11(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 3 && numericCode.charAt(11) != BUDVCalc.calcMod11ForServiceGovernment(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    }

    // Validar DV do segundo bloco
    if (modCalc == 1 && numericCode.charAt(23) != BUDVCalc.calcMod10(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 2 && numericCode.charAt(23) != BUDVCalc.calcMod11(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 3 && numericCode.charAt(23) != BUDVCalc.calcMod11ForServiceGovernment(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    }

    // Validar DV do terceiro bloco
    if (modCalc == 1 && numericCode.charAt(35) != BUDVCalc.calcMod10(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 2 && numericCode.charAt(35) != BUDVCalc.calcMod11(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 3 && numericCode.charAt(35) != BUDVCalc.calcMod11ForServiceGovernment(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    }

    // Validar DV do terceiro bloco
    if (modCalc == 1 && numericCode.charAt(47) != BUDVCalc.calcMod10(fourthBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 2 && numericCode.charAt(47) != BUDVCalc.calcMod11(fourthBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    } else if (modCalc == 3 && numericCode.charAt(47) != BUDVCalc.calcMod11ForServiceGovernment(fourthBlockCode).charAt(0)) {
      throw new RFWValidationException("O dígito verificador do bloco 1 é inválido!");
    }

  }

  /**
   * Este método recebe um código, podendo ser um código de barras ou uma representação numérica (numericCode) e verifica se é válida. O método tenta reconhecer entre todos os tipos de código conhecido pelo Framework.
   *
   * @param code Código e ser validado.
   * @throws RFWException
   */
  public static void isCodeValid(String code) throws RFWException {
    code = RUString.removeNonDigits(code);
    final BankBillCodeType type = getCodeType(code);
    if (type == null) throw new RFWValidationException("Código inválido ou não reconhecido pelo RFWDeprec!");
    switch (type) {
      case BOLETO_BARCODE:
        BUDocValidation.isBoletoBarCodeValid(code);
        break;
      case BOLETO_NUMERICCODE:
        isBoletoNumericCodeValid(code);
        break;
      case SERVICE_BARCODE:
        isServiceBarCodeValid(code);
        break;
      case SERVICE_NUMERICCODE:
        isServiceNumericCodeValid(code);
        break;
    }
  }

  /**
   * Converte o código de barras para o código numérico.<br>
   * Este método espera receber um código de barras válido. NÃO CALCULA O DV GERAL (só os intermediários pois não existem no Código de Barras), copia ele do código recebido. NÃO VALIDA O RESULTADO!
   *
   * @param barCode Código de Barras ser convertido.
   * @return O código de barras convertido em código numérico.
   * @throws RFWException
   */
  public static String convertBarCodeToNumericCode(String barCode) throws RFWException {
    barCode = RUString.removeNonDigits(barCode);

    String numericCode = null;
    final BankBillCodeType type = getCodeType(barCode);
    if (type == null) throw new RFWValidationException("Código de Barras Inválido ou não conhecido pelo RFWDeprec!");

    switch (type) {
      case SERVICE_BARCODE: {

        // Verifica qual Mod utilizar de acordo com o campo 'identificador do valor'
        int modCalc; // 1 Módulo de 10, 2-Módulo de 11, 3-Mod11 para Guias de Arrecação do Governo
        if (barCode.charAt(2) == '6' || barCode.charAt(2) == '7') {
          modCalc = 1;
        } else if (barCode.charAt(2) == '8' || barCode.charAt(2) == '9') {
          if (barCode.charAt(1) == '5' || barCode.charAt(1) == '1') { // 5 - Guias de Arrecadação Governo / 1 - Prefeituras
            modCalc = 3;
          } else {
            modCalc = 2;
          }
        } else {
          throw new RFWValidationException("Código do Identificador desconhecido pelo RFWDeprec!");
        }

        numericCode = "";

        String newBlock = barCode.substring(0, 11);
        if (modCalc == 1) {
          numericCode += newBlock + BUDVCalc.calcMod10(newBlock);
        } else if (modCalc == 2) {
          numericCode += newBlock + BUDVCalc.calcMod11(newBlock);
        } else {
          numericCode += newBlock + BUDVCalc.calcMod11ForServiceGovernment(newBlock);
        }

        newBlock = barCode.substring(11, 22);
        if (modCalc == 1) {
          numericCode += newBlock + BUDVCalc.calcMod10(newBlock);
        } else if (modCalc == 2) {
          numericCode += newBlock + BUDVCalc.calcMod11(newBlock);
        } else {
          numericCode += newBlock + BUDVCalc.calcMod11ForServiceGovernment(newBlock);
        }

        newBlock = barCode.substring(22, 33);
        if (modCalc == 1) {
          numericCode += newBlock + BUDVCalc.calcMod10(newBlock);
        } else if (modCalc == 2) {
          numericCode += newBlock + BUDVCalc.calcMod11(newBlock);
        } else {
          numericCode += newBlock + BUDVCalc.calcMod11ForServiceGovernment(newBlock);
        }

        newBlock = barCode.substring(33, 44);
        if (modCalc == 1) {
          numericCode += newBlock + BUDVCalc.calcMod10(newBlock);
        } else if (modCalc == 2) {
          numericCode += newBlock + BUDVCalc.calcMod11(newBlock);
        } else {
          numericCode += newBlock + BUDVCalc.calcMod11ForServiceGovernment(newBlock);
        }
      }
        break;
      case BOLETO_BARCODE: {
        // Completa com zeros o código caso ele não esteja no tamaho correto para facilitar a conversão
        barCode = RUString.completeUntilLengthRight("0", barCode, 47);

        String block = null;
        // Bloco 1
        block = barCode.substring(0, 4);
        block += barCode.substring(19, 24);
        numericCode = block + BUDVCalc.calcMod10(block);
        // Bloco 2
        block = barCode.substring(24, 34);
        numericCode += block + BUDVCalc.calcMod10(block);
        // Bloco 3
        block = barCode.substring(34, 44);
        numericCode += block + BUDVCalc.calcMod10(block);
        // DV Geral
        numericCode += barCode.substring(4, 5);
        // Vencimento e Valor
        numericCode += barCode.substring(5, 19);
      }
        break;
      case BOLETO_NUMERICCODE:
      case SERVICE_NUMERICCODE:
        // Não converte neste método
        numericCode = barCode;
        break;
    }
    return numericCode;
  }

}
