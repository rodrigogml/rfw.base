package br.eng.rodrigogml.rfw.base.utils;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Classe utilitária para concentrar o cálculo de Dígivos Verificadores (DV).<br>
 * Útil já que na maioria dos casos de cálculos de DV são usados uma pequena coleção de algorítimos, como Mod10 e Mod11.
 *
 * @author Rodrigo Leitão
 * @since 4.0.0 (28/01/2011)
 * @deprecated Classe em substituição pela RFWDVCalc
 */
@Deprecated
public class BUDVCalc {

  private BUDVCalc() {
  }

  /**
   * Calcula um dígito verificador usando módulo de 10.<br>
   * Usado para calcular o DV dos blocos da linha digitável de um Boleto.
   *
   * @param value valor contendo apenas dígitos para que seja calculado o DV.
   * @return String contendo apenas 1 caracter que será o DV.
   * @throws RFWValidationException Lançado caso o valor não tenha apenas números, ou seja um valor nulo/vazio.
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
   * Calcula o dígito verificador para o código de barras EAN. Funciona para EAN8 e EAN13.
   *
   * @param code código que deveserá ser validado, note que para o EAN8 devem ser passados apenas 7 digitos, já no EAN13 devem ser passados apenas 12.
   * @return Último digito do código de barra que valida a numeração.
   *
   * @throws RFWException
   */
  public static String calcEANValidateDigit(String code) throws RFWValidationException {
    int n1 = 0;
    int n0 = 0;
    // Valida se o código não é de tamanho inválido
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
