package br.eng.rodrigogml.rfw.base.utils;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Classe utilit�ria para concentrar o c�lculo de D�givos Verificadores (DV).<br>
 * �til j� que na maioria dos casos de c�lculos de DV s�o usados uma pequena cole��o de algor�timos, como Mod10 e Mod11.
 *
 * @author Rodrigo Leit�o
 * @since 4.0.0 (28/01/2011)
 * @deprecated Classe em substitui��o pela RFWDVCalc
 */
@Deprecated
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
      throw new RFWValidationException("RFW_000058");
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
