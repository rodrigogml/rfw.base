package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.utils.BUDocValidation;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.utils.RUString;

/**
 * Description: Classe que formata e valida um número de CPF.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (24/06/2011)
 */
public class RFWCPFDataFormatter implements RFWDataFormatter<String, String> {

  private static final RFWCPFDataFormatter instance = new RFWCPFDataFormatter();

  private RFWCPFDataFormatter() {
  }

  public static RFWCPFDataFormatter getInstance() {
    return RFWCPFDataFormatter.instance;
  }

  @Override
  public String toPresentation(String value, Locale locale) {
    String result = "";
    if (value != null && !"".equals(value.toString().trim())) {
      result = value.toString().trim();
      result = result.replaceAll("[^0-9]", "");
      RUString.completeUntilLengthLeft("0", result, 11);
      if (result.length() == 11) {
        result = result.substring(0, 3) + "." + result.substring(3, 6) + "." + result.substring(6, 9) + "-" + result.substring(9, 11);
      }
    }
    return result;
  }

  @Override
  public String toVO(String formattedvalue, Locale locale) {
    String result = null;
    if (formattedvalue != null) {
      result = formattedvalue.replaceAll("[^0-9]", "");
    }
    return result;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString().trim())) {
      try {
        // Valida o CPF
        String v = value.toString().replaceAll("[^0-9]", "");
        BUDocValidation.validateCPF(v);
      } catch (RFWValidationException e) {
        throw new RFWValidationException("RFW_ERR_300053");
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 14;
  }

}
