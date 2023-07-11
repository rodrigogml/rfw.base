package br.eng.rodrigogml.rfw.base.utils;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;

/**
 * Description: Classe utilit�ria para concentrar o c�lculo de D�givos Verificadores (DV).<br>
 * �til j� que na maioria dos casos de c�lculos de DV s�o usados uma pequena cole��o de algor�timos, como Mod10 e Mod11.
 *
 * @author Rodrigo Leit�o
 * @since 4.0.0 (28/01/2011)
 */
public class BUDVCalc {

  private BUDVCalc() {
  }

  /**
   * Calcula um d�gito verificador usando m�dulo de 10.<br>
   * Usado para calcular o DV dos blocos da linha digit�vel de um Boleto.
   *
   * @param value valor contendo apenas d�gitos para que seja calculado o DV.
   * @return String contendo apenas 1 caracter que ser� o DV.
   * @throws RFWValidationException Lan�ado caso o valor n�o tenha apenas n�meros, ou seja um valor nulo/vazio.
   */
  public static String calcMod10(String value) throws RFWValidationException {
    if (value == null || !value.matches("[0-9]+")) {
      throw new RFWValidationException("RFW_ERR_200001");
    }
    int factor = 2;
    int counter = 0;
    for (int i = value.length() - 1; i >= 0; i--) {
      String tmpval = "" + value.charAt(i);

      int mult = factor * Integer.parseInt(tmpval);
      do {
        counter += (mult % 10);
        mult = mult / 10;
      } while (mult > 0);

      if (factor == 2) {
        factor = 1;
      } else {
        factor = 2;
      }
    }
    int tmod = counter % 10;
    if (tmod == 0) {
      return "0";
    } else {
      return "" + (10 - tmod);
    }
  }

  /**
   * Calcula um d�gito verificador usando m�dulo de 11, com uma base de 2 � 9.<br>
   * Multiplicando cada n�mero do valor passado pelos n�meros da base (2, 3,..., 9) e somando, ao final obt�m o m�dulo da d�vis�o por 11, e subtrai de 11. Caso o resultado do c�lculo seja igual a 11 (resto 0) ou 10 (resto 1) o DV ser� 0.<br>
   * <br>
   * <b>Casos conhecidos que usam esta valida��o:</b>
   * <ul>
   * <li>Chave de Acesso da NFe</li>
   * <li>DV Geral do Boleto de Cobran�a</li>
   * </ul>
   *
   * @param value valor contendo apenas d�gitos para que seja calculado o DV.
   * @return String contendo apenas 1 caracter que ser� o DV.
   * @throws RFWValidationException Lan�ado caso o valor n�o tenha apenas n�meros, ou seja um valor nulo/vazio.
   */
  public static String calcMod11(String value) throws RFWException {
    value = RUString.removeNonDigits(value);
    if (value == null || !value.matches("[0-9]+")) {
      throw new RFWValidationException("RFW_ERR_200001");
    }

    int[] base = { 2, 3, 4, 5, 6, 7, 8, 9 };
    String[] digits = value.split("|");

    long sum = 0;
    int basecount = 0;
    for (int i = digits.length - 1; i >= 0; i--) {
      sum += base[basecount] * new Long(digits[i]);
      basecount++;
      basecount = (basecount % base.length);
    }
    long mod = 11 - (sum % 11);
    if (mod >= 10) {
      mod = 1;
    }

    return "" + mod;
  }

  /**
   * Calcula um d�gito verificador usando m�dulo de 11 APENAS PARA GUIAS DE ARRECA��O DO GOVERNO / BOLETOS DE SERVI�O, com uma base de 2 � 9.<br>
   * Realiza a mesma opera��o que o m�todo {@link #calcMod11(String)} com a diferen�a que quando c�lculo do DV d� >=10, o m�todo original retorna 1 e este m�todo retorna 0.<br>
   * <br>
   * <b>Casos conhecidos que usam esta valida��o:</b>
   * <ul>
   * <li>Guia de FGTS</li>
   * <li>Guia de GPS</li>
   * </ul>
   *
   * @param value valor contendo apenas d�gitos para que seja calculado o DV.
   * @return String contendo apenas 1 caracter que ser� o DV.
   * @throws RFWValidationException Lan�ado caso o valor n�o tenha apenas n�meros, ou seja um valor nulo/vazio.
   */
  public static String calcMod11ForServiceGovernment(String value) throws RFWException {
    value = RUString.removeNonDigits(value);
    if (value == null || !value.matches("[0-9]+")) {
      throw new RFWValidationException("RFW_ERR_200001");
    }

    int[] base = { 2, 3, 4, 5, 6, 7, 8, 9 };
    String[] digits = value.split("|");

    long sum = 0;
    int basecount = 0;
    for (int i = digits.length - 1; i >= 0; i--) {
      sum += base[basecount] * new Long(digits[i]);
      basecount++;
      basecount = (basecount % base.length);
    }
    long mod = 11 - (sum % 11);
    if (mod >= 10) {
      mod = 0;
    }

    return "" + mod;
  }

  /**
   * Este m�todo calcula o d�gito verificador usado no CNPJ. Calcula por m�dulo de 11 o primeiro d�gito verificador, e depois calcula o segundo digito verificador tamb�m com m�dulo de 11, mas com a matriz multiplicadora deslocada, come�ando em 3.
   *
   * @param cnpj 12 algarismos que comp�e o CNPJ, incluindo os 4 que indica o n�mero da filial.
   * @return
   * @throws RFWValidationException
   */
  public static String calcCNPJValidateDigit(String cnpj) throws RFWValidationException {
    // Verifica se o CNPJ tem 12 algarismos (sem os dois d�gitos verificadores)
    if (!cnpj.matches("[0-9]{12}")) {
      throw new RFWValidationException("RFW_ERR_200015");
    }
    char[] dig = cnpj.toCharArray();
    int[] mat = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
    int[] mat2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3 };

    // Calculo do DV1
    long tot = 0;
    for (int i = 0; i < dig.length; i++) {
      tot += Integer.parseInt("" + dig[i]) * mat[i];
    }
    int mod = (int) (tot % 11);
    int dv1 = 0;
    if (mod >= 2) {
      dv1 = (11 - mod);
    }
    // Calculo do DV2
    tot = 0;
    for (int i = 0; i < dig.length; i++) {
      tot += Integer.parseInt("" + dig[i]) * mat2[i];
    }
    tot += dv1 * 2;
    mod = (int) (tot % 11);
    int dv2 = 0;
    if (mod >= 2) {
      dv2 = (11 - mod);
    }
    return "" + dv1 + dv2;
  }

  /**
   * Este m�todo calcula o d�gito verificador usado no CPF.<br>
   * Calcula por m�dulo de 11 o primeiro d�gito verificador, e depois calcula o segundo digito verificador tamb�m com m�dulo de 11, mas com a matriz multiplicadora deslocada, come�ando em 3.
   *
   * @param cpf 9 algarismos que comp�e o CPF.
   * @return
   * @throws RFWValidationException
   */
  public static String calcCPFValidateDigit(String cpf) throws RFWValidationException {
    // Verifica se o CNPJ tem 13 algarismos (sem os dois d�gitos verificadores)
    if (!cpf.matches("[0-9]{9}")) {
      throw new RFWValidationException("RFW_ERR_200020");
    }
    char[] dig = cpf.toCharArray();
    int[] mat = { 10, 9, 8, 7, 6, 5, 4, 3, 2 };
    int[] mat2 = { 11, 10, 9, 8, 7, 6, 5, 4, 3 };

    // Calculo do DV1
    long tot = 0;
    for (int i = 0; i < dig.length; i++) {
      tot += Integer.parseInt("" + dig[i]) * mat[i];
    }
    int mod = (int) (tot % 11);
    int dv1 = 0;
    if (mod >= 2) {
      dv1 = (11 - mod);
    }
    // Calculo do DV2
    tot = 0;
    for (int i = 0; i < dig.length; i++) {
      tot += Integer.parseInt("" + dig[i]) * mat2[i];
    }
    tot += dv1 * 2;
    mod = (int) (tot % 11);
    int dv2 = 0;
    if (mod >= 2) {
      dv2 = (11 - mod);
    }
    return "" + dv1 + dv2;
  }

  /**
   * Calcula o d�gito verificador para o c�digo de barras EAN. Funciona para EAN8 e EAN13.
   *
   * @param code c�digo que deveser� ser validado, note que para o EAN8 devem ser passados apenas 7 digitos, j� no EAN13 devem ser passados apenas 12.
   * @return �ltimo digito do c�digo de barra que valida a numera��o.
   *
   * @throws RFWException
   */
  public static String calcEANValidateDigit(String code) throws RFWValidationException {
    int n1 = 0;
    int n0 = 0;
    // Valida se o c�digo n�o � de tamanho inv�lido
    if (code == null || (code.length() != 7 && code.length() != 12)) {
      throw new RFWValidationException("RFW_ERR_200058");
    }
    for (int i = 0; i < code.length(); i += 2) {
      n1 += new Integer(code.substring(i, i + 1));
      n0 += new Integer(code.substring(i + 1, i + 2));
    }
    n0 = n0 * 3;
    return "" + (10 - ((n1 + n0) % 10));
  }
}
