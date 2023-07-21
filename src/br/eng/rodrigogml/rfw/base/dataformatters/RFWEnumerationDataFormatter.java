package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.kernel.dataformatters.RFWDataFormatter;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWRunTimeException;

/**
 * Description: Data Formatter para trocar o valor de uma enumeration para o valor do Bundle.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (20 de dez de 2019)
 */
public class RFWEnumerationDataFormatter implements RFWDataFormatter<String, Enum<?>> {

  @Override
  public String toPresentation(Enum<?> value, Locale locale) throws RFWException {
    return RFWBundle.get(value);
  }

  @Override
  public Enum<?> toVO(String formattedvalue, Locale locale) throws RFWException {
    throw new RFWRunTimeException("O RFWEnumerationDataFormatter não foi implementado para realizar o parser, apenas para formatar para relatórios e campos de exibição (como Grids, etc.)");
  }

  @Override
  public int getMaxlenght() {
    return 500;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    throw new RFWRunTimeException("O RFWEnumerationDataFormatter não foi implementado para realizar o parser, apenas para formatar para relatórios e campos de exibição (como Grids, etc.)");

  }

}
