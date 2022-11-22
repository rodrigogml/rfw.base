package br.eng.rodrigogml.rfw.base.dataformatters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;

/**
 * Description: Formata um numero qualquer com cara de porcentagem.<br>
 * <b>Baseado no formato Float!</b>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (23/06/2011)
 */
public class RFWPercentageDataFormatter extends RFWBigDecimalDataFormatter {

  public static RFWPercentageDataFormatter createInstanceOneDigitNoSignal() {
    return new RFWPercentageDataFormatter(RFW.getRoundingMode(), 1, null, null, true);
  }

  public static RFWPercentageDataFormatter createInstanceTwoDigitNoSignal() {
    return new RFWPercentageDataFormatter(RFW.getRoundingMode(), 2, null, null, true);
  }

  public static RFWPercentageDataFormatter createInstanceMaxTwoDigitsNoSignal() {
    return new RFWPercentageDataFormatter(RFW.getRoundingMode(), 0, 2, null, null, true);
  }

  public static RFWPercentageDataFormatter createInstanceOneDigit() {
    return new RFWPercentageDataFormatter(RFW.getRoundingMode(), 1, null, null, false);
  }

  public static RFWPercentageDataFormatter createInstanceTwoDigit() {
    return new RFWPercentageDataFormatter(RFW.getRoundingMode(), 2, null, null, false);
  }

  public RFWPercentageDataFormatter(RoundingMode roundmode, Integer decimals, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    super(roundmode, decimals, minvalue, maxvalue, ignoresignal);
  }

  public RFWPercentageDataFormatter(RoundingMode roundmode, Integer decimals, Integer maxDecimals, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    super(roundmode, decimals, maxDecimals, minvalue, maxvalue, ignoresignal);
  }

  @Override
  public String toPresentation(Object value, Locale locale) {
    // Usa a mesma formatação de número e adiciona o simbolo no final
    String format = super.toPresentation(value, locale);
    if (format != null && !"".equals(format)) {
      format += "%";
    }
    return format;
  }

  @Override
  public BigDecimal toVO(String formattedvalue, Locale locale) throws RFWException {
    if (formattedvalue == null) return null;
    return super.toVO(formattedvalue.replaceAll("[\\% ]*", ""), locale);
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value)) {
      super.validate(value.toString().replaceAll("[\\% ]*", ""), locale);
    }
  }

  @Override
  public int getMaxlenght() {
    return 15;
  }

}
