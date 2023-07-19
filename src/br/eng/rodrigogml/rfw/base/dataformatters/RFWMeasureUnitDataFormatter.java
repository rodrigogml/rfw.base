package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler.MeasureUnit;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Data Formatter para trocar o valor da {@link MeasureUnit} para o valor do Bundle.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (20 de dez de 2019)
 */
public class RFWMeasureUnitDataFormatter implements RFWDataFormatter<String, MeasureUnit> {

  @Override
  public String toPresentation(MeasureUnit value, Locale locale) throws RFWException {
    if (value == null) return "";
    return RFWBundle.get(value);
  }

  @Override
  public MeasureUnit toVO(String formattedvalue, Locale locale) throws RFWException {
    return MeasureRuler.valueOf(formattedvalue);
  }

  @Override
  public int getMaxlenght() {
    return 500;
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    try {
      if (value instanceof String) {
        MeasureRuler.valueOf((String) value);
        return; // se não deu erro, não lança a exception
      }
    } catch (Exception e) {
    }
    throw new RFWValidationException("O valor não pode ser convertido em uma MeasureUnit válida!");
  }

}
