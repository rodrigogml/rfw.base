package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.utils.BUDocValidation;
import br.eng.rodrigogml.rfw.base.utils.RUString;

/**
 * Description: Classe que formata e valida um número de CNPJ.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (23/06/2011)
 */
public class RFWCNPJDataFormatter implements RFWDataFormatter<String, String> {

  private RFWCNPJDataFormatter() {
  }

  public static RFWCNPJDataFormatter createInstance() {
    return new RFWCNPJDataFormatter();
  }

  @Override
  public String toPresentation(String value, Locale locale) {
    String result = "";
    if (value != null && !"".equals(value.toString().trim())) {
      result = RUString.removeNonDigits(value);
      if (result.length() == 14) {
        result = result.substring(0, 2) + "." + result.substring(2, 5) + "." + result.substring(5, 8) + "/" + result.substring(8, 12) + "-" + result.substring(12, 14);
      }
    }
    return result;
  }

  @Override
  public String toVO(String formattedvalue, Locale locale) {
    String result = null;
    if (formattedvalue != null) {
      result = RUString.removeNonDigits(formattedvalue);
    }
    return result;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString().trim())) {
      try {
        // Valida o CNPJ
        value = value.toString().replaceAll("[^0-9]", "");
        BUDocValidation.validateCNPJ(value.toString());
      } catch (RFWValidationException e) {
        throw new RFWValidationException("RFW_ERR_300049");
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 18;
  }
}
