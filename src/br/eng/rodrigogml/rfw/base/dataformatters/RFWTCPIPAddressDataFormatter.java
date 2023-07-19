package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Data formatter para IPs IPv4.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (15/06/2015)
 */
public class RFWTCPIPAddressDataFormatter implements RFWDataFormatter<String, Object> {

  private static RFWTCPIPAddressDataFormatter instance = null;

  private RFWTCPIPAddressDataFormatter() {
  }

  public static RFWTCPIPAddressDataFormatter getInstance() {
    if (RFWTCPIPAddressDataFormatter.instance == null) {
      RFWTCPIPAddressDataFormatter.instance = new RFWTCPIPAddressDataFormatter();
    }
    return RFWTCPIPAddressDataFormatter.instance;
  }

  @Override
  public String toPresentation(Object value, Locale locale) throws RFWException {
    if (value == null) return "";
    return value.toString();
  }

  @Override
  public String toVO(String formattedvalue, Locale locale) throws RFWException {
    if ("".equals(formattedvalue)) return null;
    // Quebramos de acordo com os pontos, convertemos para Integer e reconcatemaos para garantir que não ficamos com nenhum 0 não significativo em cada um dos valores.
    final String[] parts = formattedvalue.split("\\.");
    return Integer.parseInt(parts[0]) + "." + Integer.parseInt(parts[1]) + "." + Integer.parseInt(parts[2]) + "." + Integer.parseInt(parts[3]);
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    // Contrata o valor contra uma expressão regular que verifica digito por digito do IP
    if (value != null && !"".equals(value)) {
      String patter255 = "(([0])|([1][0-9]{0,2})|([2](([0-4]?[0-9])|([5][0-5]))?)|([3-9][0-9]?)){1,}"; // Patter que aceita os números de 0 à 255 sem zeros não significativos em cada bloco. Aceita os blocos com 0 e 255 pois não há como ter certeza que são inválidos sem conhecer a mascara de rede. Por exemplo, em uma mascara 255.255.128.0 o último bloco pode conter o valor 0 ou
                                                                                                       // 255 e ser
      // completamente válido.
      if (!value.toString().matches(patter255 + "\\." + patter255 + "\\." + patter255 + "\\." + patter255)) {
        throw new RFWValidationException("RFW_ERR_200306");
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 15;
  }

}
