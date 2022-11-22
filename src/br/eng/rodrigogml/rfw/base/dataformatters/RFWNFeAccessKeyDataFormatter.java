package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;

/**
 * Description: Classe que formata uma chave de acesso da NFe.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (28/09/2011)
 */
public class RFWNFeAccessKeyDataFormatter implements RFWDataFormatter<String, Object> {

  private RFWNFeAccessKeyDataFormatter() {
  }

  public static RFWNFeAccessKeyDataFormatter createInstance() {
    return new RFWNFeAccessKeyDataFormatter();
  }

  @Override
  public String toPresentation(Object value, Locale locale) {
    String result = null;
    if (value != null && !"".equals(value.toString().trim())) {
      result = value.toString().trim();
      result = result.replaceAll("[^0-9]", "");
      if (result.length() == 44) {
        result = result.substring(0, 4) + " " + result.substring(4, 8) + " " + result.substring(8, 12) + " " + result.substring(12, 16) + " " + result.substring(16, 20) + " " + result.substring(20, 24) + " " + result.substring(24, 28) + " " + result.substring(28, 32) + " " + result.substring(32, 36) + " " + result.substring(36, 40) + " " + result.substring(40, 44);
      }
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
      // Valida a chave
      String v = value.toString().replaceAll("[^0-9]", "");
      if (v.length() != 44) {
        throw new RFWValidationException("Objeto inválido para uma entrada de item de NFe.");
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 54;
  }
}
