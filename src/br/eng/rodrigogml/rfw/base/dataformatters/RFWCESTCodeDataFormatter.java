package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.utils.RUString;

/**
 * Description: Classe que formata e valida um código CEST, de acordo com a quantidade de dígitos fornecida no campo.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (14 de dez de 2016)
 */
public class RFWCESTCodeDataFormatter implements RFWDataFormatter<String, Object> {

  public RFWCESTCodeDataFormatter() {
  }

  @Override
  public String toPresentation(Object value, Locale locale) {
    String result = "";
    if (value != null && !"".equals(value.toString().trim())) {
      result = value.toString().trim();
      result = result.replaceAll("[^0-9]", "");
      RUString.completeUntilLengthLeft("0", result, 7);
      result = result.substring(0, 2) + "." + result.substring(2, 5) + "." + result.substring(5, 7);
    }
    return result;
  }

  @Override
  public Object toVO(String formattedvalue, Locale locale) {
    String result = null;
    if (formattedvalue != null) {
      result = formattedvalue.replaceAll("[^0-9]", "");
    }
    return result;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString().trim())) {
      String v = value.toString().replaceAll("[^0-9]", "");
      // Verifica se tem tamanho Correto depois que só sobrarem números - exige que tenha os 8 números e não completa números a esquerda para evitar que ao "esquecer" um número durante a digitação, o erro seja detectado.
      if (v.length() != 7) {
        throw new RFWValidationException("Não é um código CEST válido!");
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 9;
  }

}
