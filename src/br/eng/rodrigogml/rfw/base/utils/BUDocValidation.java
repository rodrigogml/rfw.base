package br.eng.rodrigogml.rfw.base.utils;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;

/**
 * Description: Classe de utilidades de manipula��o do CNPJ.<br>
 *
 * @author Rodrigo Leit�o
 * @since 3.1.0 (NOV / 2009)
 * @version 4.1.0 (24/06/2011) - rodrigogml - Nome alterado de DOcumentValidationUtils para ficar no padr�o do Framework.
 */
public class BUDocValidation {

  /**
   * Tipos de C�digo de Barras de boleto e outros t�tulos banc�rio.
   *
   */
  public enum BankBillCodeType {
    /**
     * C�digo de barras do boleto.
     */
    BOLETO_BARCODE,
    /**
     * Linha digit�vel do boleto.
     */
    BOLETO_NUMERICCODE,
    /**
     * C�digo de barras de uma guia de arrecada��o.
     */
    SERVICE_BARCODE,
    /**
     * Linha digit�vel de uma guia de arrecada��o.
     */
    SERVICE_NUMERICCODE
  }

  // TODO RGML Rever os c�digos de erros e colocar no bundle reconfigurado.

  /**
   * Classe est�tica, denecess�rio instancia-la.
   */
  private BUDocValidation() {
  }

  /**
   * Valida a Instri��o estadual de acordo com o estado passado.<br>
   * Este m�todo nada mais � do que um delegate para o m�todo correto de valida da IE de acordo com a UF passada no parametro acronym.
   *
   * @param ie IE a ser validada.
   * @param acronym UF (2 letras) do estado que validar� a IE. Ex: 'SP', 'RJ', 'MG', etc.
   * @throws RFWValidationException
   */
  public static void validateIE(String ie, String acronym) throws RFWValidationException {
    if (ie == null || acronym == null) {
      throw new RFWValidationException("RFW_ERR_200304");
    }
    // Prepara a UF para compara��o
    acronym = acronym.toUpperCase();
    switch (acronym) {
      case "SP":
        validateIEonSP(ie);
        break;
      case "AC":
        validateIEonAC(ie);
        break;
      case "AL":
        validateIEonAL(ie);
        break;
      case "AP":
        validateIEonAP(ie);
        break;
      case "AM":
        validateIEonAM(ie);
        break;
      case "BA":
        validateIEonBA(ie);
        break;
      case "CE":
        validateIEonCE(ie);
        break;
      case "DF":
        validateIEonDF(ie);
        break;
      case "ES":
        validateIEonES(ie);
        break;
      case "GO":
        validateIEonGO(ie);
        break;
      case "MA":
        validateIEonMA(ie);
        break;
      case "MT":
        validateIEonMT(ie);
        break;
      case "MS":
        validateIEonMS(ie);
        break;
      case "MG":
        validateIEonMG(ie);
        break;
      case "PA":
        validateIEonPA(ie);
        break;
      case "PB":
        validateIEonPB(ie);
        break;
      case "PR":
        validateIEonPR(ie);
        break;
      case "PE":
        validateIEonPE(ie);
        break;
      case "PI":
        validateIEonPI(ie);
        break;
      case "RJ":
        validateIEonRJ(ie);
        break;
      case "RN":
        validateIEonRN(ie);
        break;
      case "RS":
        validateIEonRS(ie);
        break;
      case "RO":
        validateIEonRO(ie);
        break;
      case "RR":
        validateIEonRR(ie);
        break;
      case "SC":
        validateIEonSC(ie);
        break;
      case "SE":
        validateIEonSE(ie);
        break;
      case "TO":
        validateIEonTO(ie);
        break;
      default:
        throw new RFWValidationException("RFW_ERR_200305");
    }

  }

  /**
   * Valida se pe uma UF v�lida para o Brasil.<br>
   * Este m�todo simplesmente verifica se a string recebida � uma das UF do Brasil, considerando apenas 2 letras e ignorando o case.
   *
   * @param uf UFa ser validada.
   * @throws RFWValidationException
   */
  public static void validateUF(String uf) throws RFWException {
    if (uf == null) {
      throw new RFWValidationException("RFW_ERR_200417");
    }
    // Prepara a UF para compara��o
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
   * Este m�todo tenta validar a IE (Inscri��o Estadual) em qualquer estado do pa�s. Caso em qualquer um deles o valor seja v�lido este m�todo aceitar� o valor.<br>
   * As descri��es de das valida��es dos estados podem ser encontradas no link: http://www.sintegra.gov.br/insc_est.html
   *
   * @param ie
   * @throws RFWValidationException Lan�ar� a exce��o caso o valor n�o seja um IR v�lido em nenhum estado.
   */
  public static void validateIE(String ie) throws RFWValidationException {
    try {
      validateIEonSP(ie);
    } catch (Exception e) {
      try {
        validateIEonMG(ie);
      } catch (Exception e2) {
        try {
          validateIEonRJ(ie);
        } catch (Exception e3) {
          try {
            validateIEonAP(ie);
          } catch (Exception e4) {
            try {
              validateIEonAM(ie);
            } catch (Exception e5) {
              try {
                validateIEonBA(ie);
              } catch (Exception e51) {
                try {
                  validateIEonCE(ie);
                } catch (Exception e6) {
                  try {
                    validateIEonDF(ie);
                  } catch (Exception e7) {
                    try {
                      validateIEonES(ie);
                    } catch (Exception e8) {
                      try {
                        validateIEonGO(ie);
                      } catch (Exception e81) {
                        try {
                          validateIEonMA(ie);
                        } catch (Exception e9) {
                          try {
                            validateIEonMT(ie);
                          } catch (Exception e10) {
                            try {
                              validateIEonMS(ie);
                            } catch (Exception e11) {
                              try {
                                validateIEonAC(ie);
                              } catch (Exception e12) {
                                try {
                                  validateIEonPA(ie);
                                } catch (Exception e13) {
                                  try {
                                    validateIEonPB(ie);
                                  } catch (Exception e14) {
                                    try {
                                      validateIEonPR(ie);
                                    } catch (Exception e15) {
                                      try {
                                        validateIEonPE(ie);
                                      } catch (Exception e16) {
                                        try {
                                          validateIEonPI(ie);
                                        } catch (Exception e17) {
                                          try {
                                            validateIEonAL(ie);
                                          } catch (Exception e18) {
                                            try {
                                              validateIEonRN(ie);
                                            } catch (Exception e19) {
                                              try {
                                                validateIEonRS(ie);
                                              } catch (Exception e20) {
                                                try {
                                                  validateIEonRO(ie);
                                                } catch (Exception e21) {
                                                  try {
                                                    validateIEonRR(ie);
                                                  } catch (Exception e22) {
                                                    try {
                                                      validateIEonSC(ie);
                                                    } catch (Exception e23) {
                                                      try {
                                                        validateIEonSE(ie);
                                                      } catch (Exception e24) {
                                                        try {
                                                          validateIEonTO(ie);
                                                        } catch (Exception e25) {
                                                          throw new RFWValidationException("RFW_ERR_200298", new String[] { ie });
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Acre.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonAC(String ie) throws RFWValidationException {
    if (ie == null || !ie.matches("[0-9]{13}")) {
      throw new RFWValidationException("RFW_ERR_210010");
    }

    // valida os dois primeiros digitos - devem ser iguais a 01
    for (int i = 0; i < 2; i++) {
      if (Integer.parseInt(String.valueOf(ie.charAt(i))) != i) {
        throw new RFWValidationException("RFW_ERR_210012");
      }
    }

    int soma = 0;
    int pesoInicial = 4;
    int pesoFinal = 9;
    int d1 = 0; // primeiro digito verificador
    int d2 = 0; // segundo digito verificador

    // calcula o primeiro digito
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 3) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicial;
        pesoInicial--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFinal;
        pesoFinal--;
      }
    }
    d1 = 11 - (soma % 11);
    if (d1 == 10 || d1 == 11) {
      d1 = 0;
    }

    // calcula o segundo digito
    soma = d1 * 2;
    pesoInicial = 5;
    pesoFinal = 9;
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 4) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicial;
        pesoInicial--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFinal;
        pesoFinal--;
      }
    }

    d2 = 11 - (soma % 11);
    if (d2 == 10 || d2 == 11) {
      d2 = 0;
    }

    // valida os digitos verificadores
    String dv = d1 + "" + d2;
    if (!dv.equals(ie.substring(ie.length() - 2, ie.length()))) {
      throw new RFWValidationException("RFW_ERR_210011");
    }

  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Alagoas.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonAL(String ie) throws RFWValidationException {
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210013");
    }

    // valida os dois primeiros d�gitos - deve ser iguais a 24
    if (!ie.substring(0, 2).equals("24")) {
      throw new RFWValidationException("RFW_ERR_210014");
    }

    // valida o terceiro d�gito - deve ser 0,3,5,7,8
    int[] digits = { 0, 3, 5, 7, 8 };
    boolean check = false;
    for (int i = 0; i < digits.length; i++) {
      if (Integer.parseInt(String.valueOf(ie.charAt(2))) == digits[i]) {
        check = true;
        break;
      }
    }
    if (!check) {
      throw new RFWValidationException("RFW_ERR_210015");
    }

    // calcula o d�gito verificador
    int soma = 0;
    int peso = 9;
    int d = 0; // d�gito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }
    d = ((soma * 10) % 11);
    if (d == 10) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210015");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Amap�.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonAP(String ie) throws RFWValidationException {
    // valida quantida de d�gito
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210016");
    }

    // verifica os dois primeiros d�gito - deve ser igual 03
    if (!ie.substring(0, 2).equals("03")) {
      throw new RFWValidationException("RFW_ERR_210017");
    }

    // calcula o d�gito verificador
    int d1 = -1;
    int soma = -1;
    int peso = 9;

    // configura o valor do digito verificador e da soma de acordo com faixa das inscri��es
    long x = Long.parseLong(ie.substring(0, ie.length() - 1)); // x = inscri��o estadual sem o d�gito verificador
    if (x >= 3017001L && x <= 3019022L) {
      d1 = 1;
      soma = 9;
    } else if (x >= 3000001L && x <= 3017000L) {
      d1 = 0;
      soma = 5;
    } else if (x >= 3019023L) {
      d1 = 0;
      soma = 0;
    }

    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    int d = 11 - ((soma % 11)); // d = armazena o digito verificador ap�s c�lculo
    if (d == 10) {
      d = 0;
    } else if (d == 11) {
      d = d1;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210018");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Amazonas.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonAM(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210019");
    }

    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    if (soma < 11) {
      d = 11 - soma;
    } else if ((soma % 11) <= 1) {
      d = 0;
    } else {
      d = 11 - (soma % 11);
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210020");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Bahia.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonBA(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{8}") && !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210021");
    }

    // C�lculo do m�dulo de acordo com o primeiro d�gito da IE
    int modulo = 10;
    int firstDigit = Integer.parseInt(String.valueOf(ie.charAt(ie.length() == 8 ? 0 : 1)));
    if (firstDigit == 6 || firstDigit == 7 || firstDigit == 9) modulo = 11;

    // C�lculo do segundo d�gito
    int d2 = -1; // segundo d�gito verificador
    int soma = 0;
    int peso = ie.length() == 8 ? 7 : 8;
    for (int i = 0; i < ie.length() - 2; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    int resto = soma % modulo;

    if (resto == 0 || (modulo == 11 && resto == 1)) {
      d2 = 0;
    } else {
      d2 = modulo - resto;
    }

    // Calculo do primeiro digito
    int d1 = -1; // primeiro digito verificador
    soma = d2 * 2;
    peso = ie.length() == 8 ? 8 : 9;
    for (int i = 0; i < ie.length() - 2; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    resto = soma % modulo;

    if (resto == 0 || (modulo == 11 && resto == 1)) {
      d1 = 0;
    } else {
      d1 = modulo - resto;
    }

    // valida os digitos verificadores
    String dv = d1 + "" + d2;
    if (!dv.equals(ie.substring(ie.length() - 2, ie.length()))) {
      throw new RFWValidationException("RFW_ERR_210022");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Cear�.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonCE(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210023");
    }

    // C�lculo do d�gito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // d�gito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if (d == 10 || d == 11) {
      d = 0;
    }
    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210024");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Distrito Federal.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonDF(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{13}")) {
      throw new RFWValidationException("RFW_ERR_210025");
    }

    // C�lculo do primeiro d�gito verificador
    int soma = 0;
    int pesoInicio = 4;
    int pesoFim = 9;
    int d1 = -1; // primeiro d�gito verificador
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 3) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
        pesoInicio--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
        pesoFim--;
      }
    }

    d1 = 11 - (soma % 11);
    if (d1 == 11 || d1 == 10) {
      d1 = 0;
    }

    // C�lculo do segundo d�gito verificador
    soma = d1 * 2;
    pesoInicio = 5;
    pesoFim = 9;
    int d2 = -1; // segundo d�gito verificador
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 4) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
        pesoInicio--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
        pesoFim--;
      }
    }

    d2 = 11 - (soma % 11);
    if (d2 == 11 || d2 == 10) {
      d2 = 0;
    }

    // valida os digitos verificadores
    String dv = d1 + "" + d2;
    if (!dv.equals(ie.substring(ie.length() - 2, ie.length()))) {
      throw new RFWValidationException("RFW_ERR_210026");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Esp�rito Santo.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonES(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210027");
    }

    // C�lculo do d�gito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // d�gito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    int resto = soma % 11;
    if (resto < 2) {
      d = 0;
    } else if (resto > 1) {
      d = 11 - resto;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210028");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Goi�s.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonGO(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210029");
    }

    // valida os dois primeiros d�gitos
    if (!"10".equals(ie.substring(0, 2))) {
      if (!"11".equals(ie.substring(0, 2))) {
        if (!"15".equals(ie.substring(0, 2))) {
          throw new RFWValidationException("RFW_ERR_210030");
        }
      }
    }

    // Quando a inscri��o for 11094402 o d�gito verificador pode ser zero (0) e pode ser um (1);
    if (ie.substring(0, ie.length() - 1).equals("11094402")) {
      if (!ie.substring(ie.length() - 1, ie.length()).equals("0")) {
        if (!ie.substring(ie.length() - 1, ie.length()).equals("1")) {
          throw new RFWValidationException("RFW_ERR_210031");
        }
      }
    } else {

      // C�lculo do d�gito verificador
      int soma = 0;
      int peso = 9;
      int d = -1; // d�gito verificador
      for (int i = 0; i < ie.length() - 1; i++) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
        peso--;
      }

      int resto = soma % 11;
      long faixaInicio = 10103105;
      long faixaFim = 10119997;
      long insc = Long.parseLong(ie.substring(0, ie.length() - 1));
      if (resto == 0) {
        d = 0;
      } else if (resto == 1) {
        if (insc >= faixaInicio && insc <= faixaFim) {
          d = 1;
        } else {
          d = 0;
        }
      } else if (resto != 0 && resto != 1) {
        d = 11 - resto;
      }

      // valida o digito verificador
      String dv = d + "";
      if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
        throw new RFWValidationException("RFW_ERR_210031");
      }
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Maranh�o.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonMA(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210032");
    }

    // valida os dois primeiros digitos
    if (!ie.substring(0, 2).equals("12")) {
      throw new RFWValidationException("RFW_ERR_210033");
    }

    // C�lculo do d�gito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // d�gito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if ((soma % 11) == 0 || (soma % 11) == 1) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210034");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Mato Grosso.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonMT(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{11}")) {
      throw new RFWValidationException("RFW_ERR_210035");
    }

    // Calcula o digito verificador
    int soma = 0;
    int pesoInicial = 3;
    int pesoFinal = 9;
    int d = -1;

    for (int i = 0; i < ie.length() - 1; i++) {
      if (i < 2) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicial;
        pesoInicial--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFinal;
        pesoFinal--;
      }
    }

    d = 11 - (soma % 11);
    if ((soma % 11) == 0 || (soma % 11) == 1) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210036");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Matro Grosso do Sul.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonMS(String ie) throws RFWValidationException {
    // valida quantida de d�gitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210037");
    }

    // valida os dois primeiros digitos
    if (!ie.substring(0, 2).equals("28")) {
      throw new RFWValidationException("RFW_ERR_210038");
    }

    // Calcula o digito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    int resto = soma % 11;
    int result = 11 - resto;
    if (resto == 0) {
      d = 0;
    } else if (resto > 0) {
      if (result > 9) {
        d = 0;
      } else if (result < 10) {
        d = result;
      }
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210039");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Minas Gerais.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonMG(String ie) throws RFWValidationException {
    /*
     * FORMATO GERAL: A1A2A3B1B2B3B4B5B6C1C2D1D2 Onde: A= C�digo do Munic�pio B= N�mero da inscri��o C= N�mero de ordem do estabelecimento D= D�gitos de controle
     */

    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{13}")) {
      throw new RFWValidationException("RFW_ERR_210040");
    }

    // iguala a casas para o calculo
    // em inserir o algarismo zero "0" imediatamente ap�s o n�mero de c�digo do munic�pio,
    // desprezando-se os d�gitos de controle.
    String str = "";
    for (int i = 0; i < ie.length() - 2; i++) {
      if (Character.isDigit(ie.charAt(i))) {
        if (i == 3) {
          str += "0";
          str += ie.charAt(i);
        } else {
          str += ie.charAt(i);
        }
      }
    }

    // Calculo do primeiro digito verificador
    int soma = 0;
    int pesoInicio = 1;
    int pesoFim = 2;
    int d1 = -1; // primeiro d�gito verificador
    for (int i = 0; i < str.length(); i++) {
      if (i % 2 == 0) {
        int x = Integer.parseInt(String.valueOf(str.charAt(i))) * pesoInicio;
        String strX = Integer.toString(x);
        for (int j = 0; j < strX.length(); j++) {
          soma += Integer.parseInt(String.valueOf(strX.charAt(j)));
        }
      } else {
        int y = Integer.parseInt(String.valueOf(str.charAt(i))) * pesoFim;
        String strY = Integer.toString(y);
        for (int j = 0; j < strY.length(); j++) {
          soma += Integer.parseInt(String.valueOf(strY.charAt(j)));
        }
      }
    }

    int dezenaExata = soma;
    while (dezenaExata % 10 != 0) {
      dezenaExata++;
    }
    d1 = dezenaExata - soma; // resultado - primeiro digito verificador

    // Calculo do segundo digito verificador
    soma = d1 * 2;
    pesoInicio = 3;
    pesoFim = 11;
    int d2 = -1;
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 2) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
        pesoInicio--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
        pesoFim--;
      }
    }

    d2 = 11 - (soma % 11); // resultado - segundo digito verificador
    if ((soma % 11 == 0) || (soma % 11 == 1)) {
      d2 = 0;
    }

    // valida os digitos verificadores
    String dv = d1 + "" + d2;
    if (!dv.equals(ie.substring(ie.length() - 2, ie.length()))) {
      throw new RFWValidationException("RFW_ERR_210041");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Par�.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonPA(String ie) throws RFWValidationException {
    // valida quantidade de d�gitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210042");
    }

    // valida os dois primeiros digitos
    if (!ie.substring(0, 2).equals("15")) {
      throw new RFWValidationException("RFW_ERR_210043");
    }

    // Calcula o digito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // d�gito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if ((soma % 11) == 0 || (soma % 11) == 1) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210044");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Para�ba.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonPB(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210045");
    }

    // Calcula o digito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if (d == 10 || d == 11) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210046");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Paran�.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonPR(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{10}")) {
      throw new RFWValidationException("RFW_ERR_210047");
    }

    // c�lculo do primeiro digito
    int soma = 0;
    int pesoInicio = 3;
    int pesoFim = 7;
    int d1 = -1; // digito verificador
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 2) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
        pesoInicio--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
        pesoFim--;
      }
    }

    d1 = 11 - (soma % 11);
    if ((soma % 11) == 0 || (soma % 11) == 1) {
      d1 = 0;
    }

    // c�lculo do segundo digito
    soma = d1 * 2;
    pesoInicio = 4;
    pesoFim = 7;
    int d2 = -1; // segundo digito
    for (int i = 0; i < ie.length() - 2; i++) {
      if (i < 3) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
        pesoInicio--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
        pesoFim--;
      }
    }

    d2 = 11 - (soma % 11);
    if ((soma % 11) == 0 || (soma % 11) == 1) {
      d2 = 0;
    }

    // valida os digitos verificadores
    String dv = d1 + "" + d2;
    if (!dv.equals(ie.substring(ie.length() - 2, ie.length()))) {
      throw new RFWValidationException("RFW_ERR_210048");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Pernambuco.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonPE(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{14}") && !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210049");
    } else if (ie.matches("[0-9]{9}")) {
      // Se � com 9 d�gitos, ent�o faz a valida��o abaixo copiada do site da Sintegra
      long[] numero = new long[9];

      for (int i = 0; i < 7; i++) {
        numero[i] = (ie.charAt(i) - 48);
      }
      // *** O primeiro digito verificador do Numero de Inscricao Estadual ******
      long soma1 = 0;

      for (int i = 0; i < 7; i++) {
        soma1 += numero[i] * (8 - i);
      }
      long resto1 = soma1 % 11;
      if (resto1 == 0 || resto1 == 1) {
        numero[7] = 0;
      } else {
        numero[7] = 11 - resto1;
      }
      long soma2 = (numero[7] * 2);
      for (int i = 0; i < 7; i++) {
        soma2 += numero[i] * (9 - i);
      }

      long resto2 = soma2 % 11;

      if (resto2 == 0 || resto2 == 1) {
        numero[8] = 0;
      } else {
        numero[8] = 11 - resto2;
      }
      String dv = "" + numero[7] + numero[8];
      if (!ie.substring(ie.length() - 2, ie.length()).equals(dv)) {
        throw new RFWValidationException("RFW_ERR_210050");
      }

    } else {
      // Sen�o, � a de 14 d�gitos

      // C�lculo do d�gito verificador
      int soma = 0;
      int pesoInicio = 5;
      int pesoFim = 9;
      int d = -1; // d�gito verificador

      for (int i = 0; i < ie.length() - 1; i++) {
        if (i < 5) {
          soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
          pesoInicio--;
        } else {
          soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
          pesoFim--;
        }
      }

      d = 11 - (soma % 11);
      if (d > 9) {
        d -= 10;
      }

      // valida o digito verificador
      String dv = d + "";
      if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
        throw new RFWValidationException("RFW_ERR_210050");
      }
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Piau�.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonPI(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210051");
    }

    // Calculo do digito verficador
    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if (d == 11 || d == 10) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210052");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Rio de Janeiro.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonRJ(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{8}")) {
      throw new RFWValidationException("RFW_ERR_210053");
    }

    // Calculo do digito verficador
    int soma = 0;
    int peso = 7;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      if (i == 0) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * 2;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
        peso--;
      }
    }

    d = 11 - (soma % 11);
    if ((soma % 11) <= 1) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210054");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Rio Grande do Norte.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonRN(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{10}") && !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210055");
    }

    // valida os dois primeiros digitos
    if (!ie.substring(0, 2).equals("20")) {
      throw new RFWValidationException("RFW_ERR_210056");
    }

    // calcula o digito para inscri��o de 9 d?gitos
    if (ie.length() == 9) {
      int soma = 0;
      int peso = 9;
      int d = -1; // digito verificador
      for (int i = 0; i < ie.length() - 1; i++) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
        peso--;
      }

      d = ((soma * 10) % 11);
      if (d == 10) {
        d = 0;
      }

      // valida o digito verificador
      String dv = d + "";
      if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
        throw new RFWValidationException("RFW_ERR_210057");
      }
    } else {
      int soma = 0;
      int peso = 10;
      int d = -1; // digito verificador
      for (int i = 0; i < ie.length() - 1; i++) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
        peso--;
      }
      d = ((soma * 10) % 11);
      if (d == 10) {
        d = 0;
      }

      // valida o digito verificador
      String dv = d + "";
      if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
        throw new RFWValidationException("RFW_ERR_210057");
      }
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Rio Grande do Sul.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonRS(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{10}")) {
      throw new RFWValidationException("RFW_ERR_210058");
    }

    // Calculo do difito verificador
    int soma = Integer.parseInt(String.valueOf(ie.charAt(0))) * 2;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 1; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if (d == 10 || d == 11) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210059");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Rond�nia.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonRO(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{14}")) {
      throw new RFWValidationException("RFW_ERR_210060");
    }

    // Calculo do digito verificador
    int soma = 0;
    int pesoInicio = 6;
    int pesoFim = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      if (i < 5) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoInicio;
        pesoInicio--;
      } else {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * pesoFim;
        pesoFim--;
      }
    }

    d = 11 - (soma % 11);
    if (d == 11 || d == 10) {
      d -= 10;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210061");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Roraima.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonRR(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210062");
    }

    // valida os dois primeiros digitos
    if (!ie.substring(0, 2).equals("24")) {
      throw new RFWValidationException("RFW_ERR_210063");
    }

    int soma = 0;
    int peso = 1;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso++;
    }

    d = soma % 9;

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210064");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Santa Catarina.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonSC(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210065");
    }

    // Calculo do difito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if ((soma % 11) == 0 || (soma % 11) == 1) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210066");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do S�o Paulo.<br>
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonSP(String ie) throws RFWValidationException {
    if (ie == null || !ie.matches("[0-9]{12}")) {
      throw new RFWValidationException("RFW_ERR_200299");
    }

    int[] base = new int[] { 1, 3, 4, 5, 6, 7, 8, 10 }; // Array com os pesos

    // Divide os d�vigitos do IE recebido
    String[] digits = ie.split("|");

    // Calculamos o Primeiro DV
    long sum = 0; // Soma acumulada do primeiro DV
    for (int i = 0; i < base.length; i++) {
      sum += base[i] * new Long(digits[i]);
    }
    // Obtem o m�dulo de 11 do resultado
    String mod = "" + (sum % 11);
    // o primeiro DV � o d�gito mais a direita do resultado
    char dv = mod.charAt(mod.length() - 1);

    // Validamos o primeiro DV antes de testar o segundo, afinal, se j� falou no primeiro o IE n�o � v�lido, pra que perder clocks calculando o segundo.
    if (ie.charAt(8) != dv) throw new RFWValidationException("RFW_ERR_200300");

    // Tudo OK, procedemos para o c�lculo do 2�DV - Praticamente a mesma l�gica do anterior, s� mudamos os pesos
    base = new int[] { 3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2 }; // Nova base de pesos
    sum = 0; // Soma acumulada do primeiro DV
    for (int i = 0; i < base.length; i++) {
      sum += base[i] * new Long(digits[i]);
    }
    // Obtem o m�dulo de 11 do resultado
    mod = "" + (sum % 11);
    // o primeiro DV � o d�gito mais a direita do resultado
    dv = mod.charAt(mod.length() - 1);

    // Validamos o primeiro DV antes de testar o segundo, afinal, se j� falou no primeiro o IE n�o � v�lido, pra que perder clocks calculando o segundo.
    if (ie.charAt(11) != dv) throw new RFWValidationException("RFW_ERR_200300");
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Sergipe.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonSE(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_210067");
    }

    // calculo do digito verificador
    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
      peso--;
    }

    d = 11 - (soma % 11);
    if (d == 11 || d == 11 || d == 10) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210068");
    }
  }

  /**
   * Valida se o valor entrado � uma IE (Inscri��o Estadual) v�lida de acordo com a valida��o do estado do Tocantins.
   *
   * @param ie
   * @throws RFWValidationException
   */
  public static void validateIEonTO(String ie) throws RFWValidationException {
    // valida quantida de digitos
    if (ie == null || !ie.matches("[0-9]{9}") && !ie.matches("[0-9]{11}")) {
      throw new RFWValidationException("RFW_ERR_210069");
    } else if (ie.length() == 9) {
      ie = ie.substring(0, 2) + "02" + ie.substring(2);
    }

    int soma = 0;
    int peso = 9;
    int d = -1; // digito verificador
    for (int i = 0; i < ie.length() - 1; i++) {
      if (i != 2 && i != 3) {
        soma += Integer.parseInt(String.valueOf(ie.charAt(i))) * peso;
        peso--;
      }
    }
    d = 11 - (soma % 11);
    if ((soma % 11) < 2) {
      d = 0;
    }

    // valida o digito verificador
    String dv = d + "";
    if (!ie.substring(ie.length() - 1, ie.length()).equals(dv)) {
      throw new RFWValidationException("RFW_ERR_210070");
    }
  }

  /**
   * Valida um n�mero de CNPJ (Cadastro Nacional de Pessoa Jur�dica).
   *
   * @param cnpj n�mero do cnpj apenas n�meros (sem pontos, tra�os, etc.) incluindo o d�gito verificador.
   * @throws RFWValidationException lan�ado para qualquer erro que seja encontrado na valida��o do CNPJ.
   */
  public static void validateCNPJ(String cnpj) throws RFWValidationException {
    if (cnpj == null) {
      throw new RFWValidationException("RFW_ERR_200011", new String[] { cnpj });
    }
    // Verifica se o CNPJ tem 14 algarismos
    if (!cnpj.matches("[0-9]{14}")) {
      throw new RFWValidationException("RFW_ERR_200012", new String[] { cnpj });
    }
    // Vericamos se os n�meros que definem a matriz ou n�mero de filial n�o est�o zerados
    if (Integer.parseInt(cnpj.substring(8, 12)) == 0) {
      throw new RFWValidationException("RFW_ERR_200013", new String[] { cnpj });
    }
    // Verifica se os n�meros de cadastro n�o est�o zerados
    if (Integer.parseInt(cnpj.substring(0, 8)) == 0) {
      throw new RFWValidationException("RFW_ERR_200014", new String[] { cnpj });
    }
    // Verifica se os digitos verificadores conferem
    if (!cnpj.substring(12, 14).equals(BUDVCalc.calcCNPJValidateDigit(cnpj.substring(0, 12)))) {
      throw new RFWValidationException("RFW_ERR_200016", new String[] { cnpj });
    }
  }

  public static void validateCPF(String cpf) throws RFWValidationException {
    if (cpf == null) {
      throw new RFWValidationException("RFW_ERR_200017", new String[] { cpf });
    }
    // Verifica se o CNPJ tem 11 algarismos
    if (!cpf.matches("[0-9]{11}")) {
      throw new RFWValidationException("RFW_ERR_200018", new String[] { cpf });
    }
    // Verifica se os n�meros de cadastro n�o est�o zerados
    if (Integer.parseInt(cpf.substring(0, 9)) == 0) {
      throw new RFWValidationException("RFW_ERR_200019", new String[] { cpf });
    }
    // Verifica se os digitos verificadores conferem
    if (!cpf.substring(9, 11).equals(BUDVCalc.calcCPFValidateDigit(cpf.substring(0, 9)))) {
      throw new RFWValidationException("RFW_ERR_200021", new String[] { cpf });
    }
  }

  public static void validateCPFOrCNPJ(String cpfOrCnpj) throws RFWValidationException {
    if (cpfOrCnpj == null) {
      throw new RFWValidationException("RFW_ERR_200018", new String[] { cpfOrCnpj });
    }
    // Verifica se o CNPJ tem 11 algarismos
    if (!cpfOrCnpj.matches("[0-9]{11}") && !cpfOrCnpj.matches("[0-9]{14}")) {
      throw new RFWValidationException("RFW_ERR_200419", new String[] { cpfOrCnpj });
    }
    // Verifica se os n�meros de cadastro n�o est�o zerados
    if ((cpfOrCnpj.length() == 11 && Integer.parseInt(cpfOrCnpj.substring(0, 9)) == 0) || (cpfOrCnpj.length() == 14 && Integer.parseInt(cpfOrCnpj.substring(0, 9)) == 0)) {
      throw new RFWValidationException("RFW_ERR_200420", new String[] { cpfOrCnpj });
    }
    // Verifica se os digitos verificadores conferem
    if ((cpfOrCnpj.length() == 11 && !cpfOrCnpj.substring(9, 11).equals(BUDVCalc.calcCPFValidateDigit(cpfOrCnpj.substring(0, 9)))) || (cpfOrCnpj.length() == 14 && !cpfOrCnpj.substring(12, 14).equals(BUDVCalc.calcCNPJValidateDigit(cpfOrCnpj.substring(0, 12))))) {
      throw new RFWValidationException("RFW_ERR_200021", new String[] { cpfOrCnpj });
    }
  }

  /**
   * Verifica se o c�digo de barras � um c�digo de boleto v�lido. <br>
   * O m�todo remove quaisquer caracteres que n�o sejam d�gitos do c�digo de barras antes de validar.
   *
   * @param codebar O c�digo de barras a ser verificado.
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

    // Se o c�digo do banco n�o est� zerado
    if (codebar.substring(0, 3) == "000") {
      throw new RFWValidationException("RFW_ERR_210057");
    }

    // Se o campo de moeda � 9, pois n�o conhecemos outro valor
    if (codebar.charAt(3) != '9') {
      throw new RFWValidationException("RFW_ERR_210063");
    }
  }

  /**
   * Verifica se a linha num�rica (linha digit�vel) � uma linha de boleto v�lida. <br>
   * O m�todo remove quaisquer caracteres que n�o sejam d�gitos do c�digo de barras antes de validar.
   *
   * @param numericLine the numeric line
   * @throws RFWException the RFW exception
   */
  public static void isBoletoNumericCodeValid(String numericLine) throws RFWException {
    numericLine = RUString.removeNonDigits(numericLine);

    // Se o tamanho est� correto
    if (!(numericLine.length() == 33 || (numericLine.length() > 33 && numericLine.length() <= 36 && numericLine.substring(33) == "0000") || numericLine.length() >= 37 && numericLine.length() <= 47)) {
      throw new RFWValidationException("RFW_ERR_210055");
    }

    // Se possui c�digo do banco preenchido
    if (numericLine.substring(0, 3) == "000") {
      throw new RFWValidationException("RFW_ERR_210057");
    }

    // Se o c�digo da moeda � v�lido
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

    // O DV � calculado com base no c�digo de barras e n�o no c�digo num�rico. Por isso primeiro vamos converter o c�digo num�rico no c�digo de barras e depois calcular o DV
    String codeBar = convertNumericCodeToBarCode(numericLine);
    String codeBarWithoutDV = codeBar.substring(0, 4) + codeBar.substring(5);
    if (numericLine.charAt(32) != BUDVCalc.calcMod11(codeBarWithoutDV).charAt(0)) {
      throw new RFWValidationException("RFW_ERR_210058");
    }
  }

  /**
   * Converte o c�digo num�rico para o c�digo de barras.<br>
   * Este m�todo espera receber um c�digo num�rico v�lido. N�O CALCULA O DV, copia ele do c�digo num�rico. N�O VALIDA O RESULTADO!
   *
   * @param numericCode A linha num�rica a ser convertida.
   * @return O c�digo de barras convertido.
   * @throws RFWException
   */
  public static String convertNumericCodeToBarCode(String numericCode) throws RFWException {
    numericCode = RUString.removeNonDigits(numericCode);

    String barCode = null;
    final BankBillCodeType type = getCodeType(numericCode, false);
    if (type == null) throw new RFWValidationException("C�digo Num�rico Inv�lido ou n�o conhecido pelo RFW!");

    switch (type) {
      case SERVICE_NUMERICCODE: {
        barCode = numericCode.substring(0, 11);
        barCode += numericCode.substring(12, 23);
        barCode += numericCode.substring(24, 35);
        barCode += numericCode.substring(36, 47);
      }
        break;
      case BOLETO_NUMERICCODE: {
        // Completa com zeros o c�digo caso ele n�o esteja no tamaho correto para facilitar a convers�o
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
        // N�o converte neste m�todo
        barCode = numericCode;
        break;
    }
    return barCode;
  }

  /**
   * Identifica qual � o tipo de {@code BankBillCodeType} que uma string representa.<br>
   * ESTE M�TODO FAZ A VALIDA��O DOS DVs PARA GARANTIR QUE A NUMERA��O EST� SENDO CORRETAMENTE INTERPRETADA
   *
   * @param code O c�digo a ser verificado.
   * @return Um {@code BankBillCodeType} que identifica o c�digo passado ao m�todo, OU nulo caso o c�digo n�o seja reconhecido/v�lido
   * @throws RFWException
   */
  public static BankBillCodeType getCodeType(String code) throws RFWException {
    return getCodeType(code, true);
  }

  /**
   * Recupera o tipo do c�digo de barra baseado no tamanho e estrutura do c�digo.
   *
   * @param code C�digo a ser definido.
   * @param test Caso true, o m�todo testa a estrutura e DV para garantir que � o c�digo, caso false considera apenas o tamanho. (Em alguns casos n�o testa para evitar o loop infinito entre m�todos).
   * @return the code type
   * @throws RFWException
   */
  private static BankBillCodeType getCodeType(String code, boolean test) throws RFWException {
    code = RUString.removeNonDigits(code);
    if (code.length() == 44) { // Provavelmente � c�digo de barras
      if (code.startsWith("8")) { // Provavelmente c�digo de arrecada��o
        try {
          if (test) isServiceBarCodeValid(code);
          return BankBillCodeType.SERVICE_BARCODE;
        } catch (RFWValidationException e) {
        }
      }
      // Se n�o come�a com 8, ou se n�o era v�lido, tentamos como boleto
      try {
        if (test) BUDocValidation.isBoletoBarCodeValid(code);
        return BankBillCodeType.BOLETO_BARCODE;
      } catch (RFWValidationException e) {
      }
    }
    // Se o tamanho n�o � 44, ou se falhou em validar como c�digo de barras, vamos processar como representa��o num�rica
    // A primeira � a representa��o de arrecada��o, que � sempre fixa e � a maior
    if (code.length() == 48) {
      try {
        if (test) isServiceNumericCodeValid(code);
        return BankBillCodeType.SERVICE_NUMERICCODE;
      } catch (RFWValidationException e) {
      }
    } else if (code.length() >= 33 && code.length() <= 47) {
      // Se o tamanho est� entre os tamanhos que a representa��o num�rica do boleto pode ter, validamos para verificar
      try {
        if (test) isBoletoNumericCodeValid(code);
        return BankBillCodeType.BOLETO_NUMERICCODE;
      } catch (RFWValidationException e) {
      }
    }
    return null;
  }

  /**
   * Verifica se o c�digo de barras � um c�digo de guia de arrecada��o/servi�os v�lido. <br>
   * O m�todo remove quaisquer caracteres que n�o sejam d�gitos do c�digo de barras antes de validar.
   *
   * @param codeBar O c�digo de barras a ser verificado.
   * @throws RFWException
   */
  public static void isServiceBarCodeValid(String codeBar) throws RFWException {
    codeBar = RUString.removeNonDigits(codeBar);

    // Tamanho exato deve ser de 44 algarismos
    if (codeBar.length() != 44) {
      throw new RFWValidationException("O tamanho do c�digo n�o � v�lido para uma guia de arrecada��o/servi�o.", new String[] { "44" });
    }

    // O primeiro d�gito deve ser "8"
    if (codeBar.charAt(0) != '8') {
      throw new RFWValidationException("O c�digo de barras da guia de arrecada��o deve sempre come�ar com 8.");
    }

    // Removendo o DV da string
    String codeToCheck = codeBar.substring(0, 3) + codeBar.substring(4);

    // Identificador do valor deve ser:
    // 6 (Valor a ser cobrado efetivamente em reais) - com d�gito verificador calculado pelo m�dulo 10
    // 7 (Quantidade de Moeda) - com d�gito verificador calculado pelo m�dulo 10
    // 8 (Valor a ser cobrado efetivamente em reais) - com d�gito verificador calculado pelo m�dulo 11
    // 9 (Quantidade de Moeda) - com d�gito verificador calculado pelo m�dulo 11
    if (codeBar.charAt(2) != '6' && codeBar.charAt(2) != '7' && codeBar.charAt(2) != '8' && codeBar.charAt(2) != '9') {
      throw new RFWValidationException("O c�digo da moeda do c�digo de barras n�o � v�lido, ou n�o reconhecido pelo RFW.");
    }

    // Se o campo 'segmento' possui um valor v�lido, ou seja, diferente de zero e 8 (os �nicos valores inv�lidos atualmente)
    if (codeBar.charAt(1) == '0' || codeBar.charAt(1) == '8') {
      throw new RFWValidationException("RFW_ERR_210065");
    }

    // O DV deve estar correto
    if ((codeBar.charAt(2) == '6' || codeBar.charAt(2) == '7') && codeBar.charAt(3) != BUDVCalc.calcMod10(codeToCheck).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do c�digo de barras n�o est� correto.");
    } else if ((codeBar.charAt(2) == '8' || codeBar.charAt(2) == '9')) {
      if (codeBar.charAt(1) == '5' || codeBar.charAt(1) == '1') { // 5 - Guias Governo // 1 - Prefeituras
        if (codeBar.charAt(3) != BUDVCalc.calcMod11ForServiceGovernment(codeToCheck).charAt(0)) {
          throw new RFWValidationException("O d�gito verificador do c�digo de barras n�o est� correto.");
        }
      } else if (codeBar.charAt(3) != BUDVCalc.calcMod11(codeToCheck).charAt(0)) {
        throw new RFWValidationException("O d�gito verificador do c�digo de barras n�o est� correto.");
      }
    }

  }

  /**
   * Verifica se a linha num�rica (linha digit�vel) � uma linha de guia de arrecada��o/servi�os v�lida. <br>
   * O m�todo remove quaisquer caracteres que n�o sejam d�gitos do c�digo de barras antes de validar.
   *
   * @param numericCode the numeric code
   * @throws RFWException
   */
  public static void isServiceNumericCodeValid(String numericCode) throws RFWException {
    numericCode = RUString.removeNonDigits(numericCode);

    // Tamanho exato de 48 algarismos
    if (numericCode.length() != 48) {
      throw new RFWValidationException("O tamanho do c�digo num�rico n�o � v�lido para uma conta servi�o/guia de arrecada��o.", new String[] { "48" });
    }

    // Como o a representa��o num�rica � o pr�prio c�digo de barras, s� acrescido de um DV a cada 11 posi��es, vamos remover elas e fazer a valida��o do c�digo de barras, depois s� validamos os DVs.
    // Assim as valida��es ficam todas concentradas s� no m�todo do c�digo de barras.
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
      if (barCode.charAt(1) == '5' || barCode.charAt(1) == '1') { // 5 - Guias de Arrecada��o Governo / 1 - Prefeituras
        modCalc = 3;
      } else {
        modCalc = 2;
      }
    } else {
      throw new RFWValidationException("C�digo do Identificador desconhecido pelo RFW!");
    }

    // Validar DV do primeiro bloco
    if (modCalc == 1 && numericCode.charAt(11) != BUDVCalc.calcMod10(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 2 && numericCode.charAt(11) != BUDVCalc.calcMod11(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 3 && numericCode.charAt(11) != BUDVCalc.calcMod11ForServiceGovernment(firstBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    }

    // Validar DV do segundo bloco
    if (modCalc == 1 && numericCode.charAt(23) != BUDVCalc.calcMod10(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 2 && numericCode.charAt(23) != BUDVCalc.calcMod11(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 3 && numericCode.charAt(23) != BUDVCalc.calcMod11ForServiceGovernment(secondBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    }

    // Validar DV do terceiro bloco
    if (modCalc == 1 && numericCode.charAt(35) != BUDVCalc.calcMod10(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 2 && numericCode.charAt(35) != BUDVCalc.calcMod11(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 3 && numericCode.charAt(35) != BUDVCalc.calcMod11ForServiceGovernment(thirdBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    }

    // Validar DV do terceiro bloco
    if (modCalc == 1 && numericCode.charAt(47) != BUDVCalc.calcMod10(fourthBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 2 && numericCode.charAt(47) != BUDVCalc.calcMod11(fourthBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    } else if (modCalc == 3 && numericCode.charAt(47) != BUDVCalc.calcMod11ForServiceGovernment(fourthBlockCode).charAt(0)) {
      throw new RFWValidationException("O d�gito verificador do bloco 1 � inv�lido!");
    }

  }

  /**
   * Este m�todo recebe um c�digo, podendo ser um c�digo de barras ou uma representa��o num�rica (numericCode) e verifica se � v�lida. O m�todo tenta reconhecer entre todos os tipos de c�digo conhecido pelo Framework.
   *
   * @param code C�digo e ser validado.
   * @throws RFWException
   */
  public static void isCodeValid(String code) throws RFWException {
    code = RUString.removeNonDigits(code);
    final BankBillCodeType type = getCodeType(code);
    if (type == null) throw new RFWValidationException("C�digo inv�lido ou n�o reconhecido pelo RFW!");
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
   * Converte o c�digo de barras para o c�digo num�rico.<br>
   * Este m�todo espera receber um c�digo de barras v�lido. N�O CALCULA O DV GERAL (s� os intermedi�rios pois n�o existem no C�digo de Barras), copia ele do c�digo recebido. N�O VALIDA O RESULTADO!
   *
   * @param barCode C�digo de Barras ser convertido.
   * @return O c�digo de barras convertido em c�digo num�rico.
   * @throws RFWException
   */
  public static String convertBarCodeToNumericCode(String barCode) throws RFWException {
    barCode = RUString.removeNonDigits(barCode);

    String numericCode = null;
    final BankBillCodeType type = getCodeType(barCode);
    if (type == null) throw new RFWValidationException("C�digo de Barras Inv�lido ou n�o conhecido pelo RFW!");

    switch (type) {
      case SERVICE_BARCODE: {

        // Verifica qual Mod utilizar de acordo com o campo 'identificador do valor'
        int modCalc; // 1 M�dulo de 10, 2-M�dulo de 11, 3-Mod11 para Guias de Arreca��o do Governo
        if (barCode.charAt(2) == '6' || barCode.charAt(2) == '7') {
          modCalc = 1;
        } else if (barCode.charAt(2) == '8' || barCode.charAt(2) == '9') {
          if (barCode.charAt(1) == '5' || barCode.charAt(1) == '1') { // 5 - Guias de Arrecada��o Governo / 1 - Prefeituras
            modCalc = 3;
          } else {
            modCalc = 2;
          }
        } else {
          throw new RFWValidationException("C�digo do Identificador desconhecido pelo RFW!");
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
        // Completa com zeros o c�digo caso ele n�o esteja no tamaho correto para facilitar a convers�o
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
        // N�o converte neste m�todo
        numericCode = barCode;
        break;
    }
    return numericCode;
  }

}
