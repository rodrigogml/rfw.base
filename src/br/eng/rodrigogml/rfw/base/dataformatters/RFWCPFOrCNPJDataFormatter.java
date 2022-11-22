package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.utils.BUDocValidation;
import br.eng.rodrigogml.rfw.base.utils.BUString;

/**
 * Description: Classe que formata e valida um número de CPF ou CNPJ.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (24/06/2011)
 */
public class RFWCPFOrCNPJDataFormatter implements RFWDataFormatter<String, String> {

  private static final RFWCPFOrCNPJDataFormatter instance = new RFWCPFOrCNPJDataFormatter();

  private RFWCPFOrCNPJDataFormatter() {
  }

  public static RFWCPFOrCNPJDataFormatter getInstance() {
    return RFWCPFOrCNPJDataFormatter.instance;
  }

  @Override
  public String toPresentation(String value, Locale locale) {
    String result = "";
    if (value != null && !"".equals(value.toString().trim())) {
      result = BUString.removeNonDigits(value);

      if (result.length() == 11) {
        result = result.substring(0, 3) + "." + result.substring(3, 6) + "." + result.substring(6, 9) + "-" + result.substring(9, 11);
      } else if (result.length() == 14) {
        result = result.substring(0, 2) + "." + result.substring(2, 5) + "." + result.substring(5, 8) + "/" + result.substring(8, 12) + "-" + result.substring(12, 14);
      }
    }
    return result;
  }

  @Override
  public String toVO(String formattedvalue, Locale locale) throws RFWException {
    String result = null;
    validate(formattedvalue, locale);
    if (formattedvalue != null) {
      result = BUString.removeNonDigits(formattedvalue);
    }
    return result;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString().trim())) {
      String v = BUString.removeNonDigits((String) value);
      try {
        // Valida como CPF
        BUDocValidation.validateCPF(v);
      } catch (RFWValidationException e) {
        try {
          // Valida como CNPJ
          BUDocValidation.validateCNPJ(v.toString());
        } catch (RFWValidationException e1) {
          throw new RFWValidationException("O valor '" + value + "' não é nem um CPF nem um CNPJ válido!");
        }
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 18;
  }
}
