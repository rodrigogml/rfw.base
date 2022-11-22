package br.eng.rodrigogml.rfw.base.dataformatters;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.eng.rodrigogml.rfw.base.RFW;

/**
 * Description: Formata um numero qualquer como valor monetário.<br>
 * Atualmente este formatador não tem diferença alguma para o padrão de números. A existencia desta classe é apenas para que já fiquem todos os campos centralizados, para uma eventual mudança futura na formação de valores monetários.
 *
 * @author Rodrigo Leitão
 * @since 5.1.0 (20/10/2012)
 */
public class RFWCurrencyBigDecimalDataFormatter extends RFWBigDecimalDataFormatter {

  public static RFWCurrencyBigDecimalDataFormatter createInstance() {
    return new RFWCurrencyBigDecimalDataFormatter(RFW.getRoundingMode(), null, null, false);
  }

  public static RFWCurrencyBigDecimalDataFormatter createInstancePositive() {
    return new RFWCurrencyBigDecimalDataFormatter(RFW.getRoundingMode(), null, null, true);
  }

  public RFWCurrencyBigDecimalDataFormatter(RoundingMode roundingmode, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    this(roundingmode, 2, minvalue, maxvalue, ignoresignal);
  }

  public RFWCurrencyBigDecimalDataFormatter(RoundingMode roundingmode, Integer decimals, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    super(roundingmode, decimals, minvalue, maxvalue, ignoresignal);
  }

  public RFWCurrencyBigDecimalDataFormatter(RoundingMode roundingmode, Integer decimals, Integer maxDecimals, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    super(roundingmode, decimals, maxDecimals, minvalue, maxvalue, ignoresignal);
  }

}
