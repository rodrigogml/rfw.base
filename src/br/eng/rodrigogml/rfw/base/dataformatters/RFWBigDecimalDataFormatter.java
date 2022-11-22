package br.eng.rodrigogml.rfw.base.dataformatters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;

/**
 * Description: Classe usada para formatar valores BigDecimal.<BR>
 * Esta classe é muito similar (praticamente uma sósia) da {@link RFWNumberDataFormatter}, só não pudemos estender aquela classe por o processamento dela é todo baseado em Double, o que continuaria causando os problemas de arredondamento evitados na implementação do BigDecimal.
 *
 * @author Rodrigo Leitão
 * @since 5.1.0 (20/10/2012)
 */
public class RFWBigDecimalDataFormatter implements RFWDataFormatter<String, Object> {

  public static RFWBigDecimalDataFormatter createInstanceMaxTreeDigitsNoSignal() {
    return new RFWBigDecimalDataFormatter(RFW.getRoundingMode(), 0, 3, null, null, true);
  }

  public static RFWBigDecimalDataFormatter createInstanceMaxTwoDigitsNoSignal() {
    return new RFWBigDecimalDataFormatter(RFW.getRoundingMode(), 0, 2, null, null, true);
  }

  public static RFWBigDecimalDataFormatter createInstanceTreeDigitsNoSignal() {
    return new RFWBigDecimalDataFormatter(RFW.getRoundingMode(), 3, null, null, true);
  }

  public static RFWBigDecimalDataFormatter createInstanceTwoDigitsNoSignal() {
    return new RFWBigDecimalDataFormatter(RFW.getRoundingMode(), 2, null, null, true);
  }

  public static RFWBigDecimalDataFormatter createInstanceOneDigitNoSignal() {
    return new RFWBigDecimalDataFormatter(RFW.getRoundingMode(), 1, null, null, true);
  }

  /**
   * número de dígitos fracionários.
   */
  private Integer decimals = null;

  /**
   * número máximo de dígitos fracionários.
   */
  private Integer maxDecimals = null;

  /**
   * Define a política de arredondamento do número.
   */
  private RoundingMode roundingmode = null;

  /**
   * Caso true, retornará sempre um valor positivo, simplesmente ignorando e arrancando o sinal. Como se fosse o módulo (ou valor absoluto) do número.<br>
   */
  private Boolean ignoresignal = null;

  /**
   * Valor mínimo aceito.
   */
  private BigDecimal minvalue = null;

  /**
   * Valor máximo aceito.
   */
  private BigDecimal maxvalue = null;

  public RFWBigDecimalDataFormatter(RoundingMode roundingmode, Integer decimals, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    this(roundingmode, decimals, decimals, minvalue, maxvalue, ignoresignal);
  }

  public RFWBigDecimalDataFormatter(RoundingMode roundingmode, Integer decimals, Integer maxDecimals, BigDecimal minvalue, BigDecimal maxvalue, Boolean ignoresignal) {
    this.roundingmode = roundingmode;
    this.decimals = decimals;
    this.maxDecimals = maxDecimals;
    this.minvalue = minvalue;
    this.maxvalue = maxvalue;
    this.ignoresignal = ignoresignal;
  }

  @Override
  public String toPresentation(Object value, Locale locale) {
    String result = "";
    if (value != null && !"".equals(value.toString().trim())) {
      BigDecimal bigvalue = null;
      if (value instanceof BigDecimal) {
        bigvalue = (BigDecimal) value;
      } else {
        bigvalue = new BigDecimal(value.toString());
      }

      if (this.maxDecimals != null) {
        bigvalue = bigvalue.setScale(this.maxDecimals, this.roundingmode);
      } else {
        bigvalue = bigvalue.setScale(this.decimals, this.roundingmode);
      }
      if (this.maxDecimals != null) {
        result = LocaleConverter.formatBigDecimal(bigvalue, locale, this.decimals, this.maxDecimals);
      } else {
        result = LocaleConverter.formatBigDecimal(bigvalue, locale, this.decimals);
      }
    }
    return result;
  }

  @Override
  public BigDecimal toVO(String formattedvalue, Locale locale) throws RFWException {
    return processValue(formattedvalue, locale);
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString().trim())) {
      BigDecimal nv = processValue(value.toString(), locale);
      // Verifica range de valores
      if (this.minvalue != null && this.minvalue.compareTo(nv) >= 0) {
        throw new RFWValidationException("RFW_ERR_200031", new String[] { value.toString() });
      } else if (this.maxvalue != null && this.maxvalue.compareTo(nv) <= 0) {
        throw new RFWValidationException("RFW_ERR_200032", new String[] { value.toString() });
      }
    }
  }

  @Override
  public int getMaxlenght() {
    return 25;
  }

  /**
   * Faz as modificações necessárias ao número, como remover o sinal e arredondamentos. não faz validações, apenas correções.
   *
   * @param value
   * @return
   * @throws RFWException
   */
  protected BigDecimal processValue(String value, Locale locale) throws RFWException {
    // Verifica se é objeto numérico tentando convertelo em double
    BigDecimal nv = null;
    if (value != null && !"".equals(value)) {
      try {
        if (this.maxDecimals != null) {
          nv = LocaleConverter.parseBigDecimal(value, locale, ignoresignal, maxDecimals, this.roundingmode);
        } else {
          nv = LocaleConverter.parseBigDecimal(value, locale, ignoresignal, decimals, this.roundingmode);
        }
      } catch (RFWException e) {
        throw e;
      } catch (Exception e) {
        throw new RFWValidationException("RFW_ERR_200030", e);
      }
    }
    return nv;
  }
}
