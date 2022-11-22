package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.utils.BUMail;

/**
 * Description: Data formatter para endereços de e-mail.<BR>
 * Esta classe não "formata" os e-mail, o valor para o VO e para a Tela passa livremente, a única eficiência desta classe está no Método de validação.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (23/06/2015)
 */
public class RFWEmailDataFormatter implements RFWDataFormatter<String, String> {

  private static RFWEmailDataFormatter instance = null;

  private RFWEmailDataFormatter() {
  }

  public static RFWEmailDataFormatter getInstance() {
    if (RFWEmailDataFormatter.instance == null) {
      RFWEmailDataFormatter.instance = new RFWEmailDataFormatter();
    }
    return RFWEmailDataFormatter.instance;
  }

  @Override
  public String toPresentation(String value, Locale locale) {
    if (value == null) return "";
    return value.toString();
  }

  @Override
  public String toVO(String formattedvalue, Locale locale) {
    return formattedvalue;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString())) {
      BUMail.validateMailAddress(value.toString());
    }
  }

  @Override
  public int getMaxlenght() {
    return 255;
  }

}
