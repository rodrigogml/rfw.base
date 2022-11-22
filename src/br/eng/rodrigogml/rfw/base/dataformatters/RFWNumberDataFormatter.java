package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.dataformatters.LocaleConverter.ROUNDPOLICY;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;

/**
 * Description: Classe padrão formatadora de números. Permite e trata algumas configurações<br>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (23/06/2011)
 */
public abstract class RFWNumberDataFormatter implements RFWDataFormatter<String, Object> {

  /**
   * Define a política de arredondamento do número.
   */
  protected ROUNDPOLICY roundpolicy = null;

  /**
   * Valor mínimo aceito.
   */
  protected Double minvalue = null;

  /**
   * Valor máximo aceito.
   */
  protected Double maxvalue = null;

  /**
   * número de dígitos fracionários.
   */
  protected Integer decimals = null;

  /**
   * Caso true, retornará sempre um valor positivo, simplesmente ignorando e arrancando o sinal. Como se fosse o módulo (ou valor absoluto) do número.<br>
   */
  protected Boolean ignoresignal = null;

  /**
   * Define se usaremos agrupamento de milhares ou não.
   */
  protected boolean groupingused = true;

  public RFWNumberDataFormatter(ROUNDPOLICY roundpolicy, Integer decimals, Double minvalue, Double maxvalue, Boolean ignoresignal) {
    this(roundpolicy, decimals, minvalue, maxvalue, ignoresignal, true);
  }

  public RFWNumberDataFormatter(ROUNDPOLICY roundpolicy, Integer decimals, Double minvalue, Double maxvalue, Boolean ignoresignal, boolean groupingused) {
    this.roundpolicy = roundpolicy;
    this.decimals = decimals;
    this.minvalue = minvalue;
    this.maxvalue = maxvalue;
    this.ignoresignal = ignoresignal;
    this.groupingused = groupingused;
  }

  @Override
  public String toPresentation(Object value, Locale locale) {
    String result = "";
    if (value != null && !"".equals(value.toString().trim())) {
      Double db = new Double("" + value);
      if (this.ignoresignal) db = Math.abs(db);
      result = LocaleConverter.formatDouble(db, locale, this.decimals, this.groupingused);
    }
    return result;
  }

  @Override
  public Object toVO(String formattedvalue, Locale locale) throws RFWException {
    return processValue(formattedvalue, locale);
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value.toString().trim())) {
      Double nv = processValue(value.toString(), locale);
      // Verifica range de valores
      if (this.minvalue != null && nv.doubleValue() < this.minvalue) {
        throw new RFWValidationException("RFW_ERR_300055", new String[] { "" + LocaleConverter.formatDouble(this.minvalue, locale, this.decimals) });
      } else if (this.maxvalue != null && nv.doubleValue() > this.maxvalue) {
        throw new RFWValidationException("RFW_ERR_300056", new String[] { "" + LocaleConverter.formatDouble(this.maxvalue, locale, this.decimals) });
      }
    }
  }

  /**
   * Faz as modificações necessárias ao número, como remover o sinal e arredondamentos. não faz validações, apenas correções.
   *
   * @param value
   * @return
   * @throws RFWException
   */
  protected Double processValue(String value, Locale locale) throws RFWException {
    // Verifica se é objeto numérico tentando convertelo em double
    Double nv = null;
    if (value != null && !"".equals(value.trim())) {
      try {
        nv = LocaleConverter.parseDouble(value.trim(), locale, ignoresignal, decimals, this.roundpolicy);
      } catch (RFWException e) {
        throw new RFWValidationException("RFW_ERR_300054");
      }
    }
    return nv;
  }

  @Override
  public int getMaxlenght() {
    if (this.maxvalue != null) {
      return ("" + this.maxvalue.longValue()).length();
    } else {
      return ("" + Double.MAX_VALUE).length();
    }
  }
}
