package br.eng.rodrigogml.rfw.base.dataformatters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.eng.rodrigogml.rfw.base.utils.BUNumber;
import br.eng.rodrigogml.rfw.base.utils.RUString;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Classe utilitária que converte os valores padroes do java no formato configurado de locale do usuário, e vice-versa.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.0.0 (SET / 2009)
 */

public class LocaleConverter {

  /**
   * Políticas de definição de arrendondamento dos valores.
   */
  public static enum ROUNDPOLICY {
    /**
     * Não aplica nenhuma política de arredondamento.
     */
    NONE,

    /**
     * Arredonda para o valor que menos perde.
     */
    BESTROUND,
    /**
     * Arredonda sempre para baixo, o mesmo que truncar o valor.
     */
    ROUNDFLOOR,

    /**
     * Arrendonda sempre para cima.
     */
    ROUNDCEIL
  }

  /**
   * Recupera o simbolo que separa os decimais da parte inteira de um número, para um Locale específico.
   *
   * @param locale Localidade desejada
   * @return
   */
  public static String getDecimalSymbol(Locale locale) {
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    nf.setMinimumFractionDigits(1);
    return nf.format(1.1).replaceAll("[0-9]*", "");
  }

  /**
   * Recupera o simbolo de agrupamento de milhares de um número, para um Locale específico.
   *
   * @param locale Localidade desejada
   * @return
   */
  public static String getDigitGroupingSymbol(Locale locale) {
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);
    return nf.format(1000d).replaceAll("[0-9]*", "");
  }

  /**
   * Converte um objeto para texto chamando sei toString. Com a diferença que verifica se o objeto não é nulo, e se for não retorna a string 'null' e sim uma string vazia ''.
   *
   * @param obj Objeto a ser convertido para texto
   * @return String com o toString do objeto ou uma string vazia em caso do obj ser nulo.
   */
  public static String toString(Object obj) {
    if (obj == null) {
      return "";
    } else {
      return String.valueOf(obj);
    }
  }

  /**
   * Este método verifica o tipo de objeto e converte para uma String de acordo com sua localidade. Por exemplo, converte valores númericos usando os simbolos corretos de formatação, ou datas, etc.<br>
   * Este método é um dispatcher que dependendo do objeto recebido chama outro parser automaticamente.
   *
   * @param obj Objeto a ser convertido para a localidade correta
   * @param locale localidade do usuúrio para qual o objeto será formatado.
   * @return String com o valor do objeto no formato do locale.
   */
  public static String format(Object obj, Locale locale) {
    if (locale == null) {
      throw new NullPointerException("Locale can't be null!");
    }
    String value = "";
    if (obj instanceof Float) {
      value = formatFloat((Float) obj, locale);
    }
    return toString(value);
  }

  /**
   * Este método formata um valor Inteiro para se adequar a formatação do local.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatInteger(Integer value, Locale locale) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    return nf.format(value);
  }

  /**
   * Converte um objeto para Integer. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @return Objeto Integer com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Integer parseInteger(Object value, Locale locale) throws RFWException {
    return parseInteger(value, locale, false);
  }

  /**
   *
   * Converte um objeto para Integer. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @return Objeto Integer com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWValidationException Lançado sempre que não for possível converter a informação.
   * @throws RFWCriticalException
   */
  public static Integer parseInteger(Object value, Locale locale, boolean ignoresignal) throws RFWException {
    Integer parsedvalue = null;
    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof Integer) {
        parsedvalue = new Integer((Integer) value);
      } else if (value instanceof String) {
        // Verifica se contem apenas os caracteres esperados para evitar que nímeros com caracteres estranhos (como pontos no lugar da virgula) sejam interpretados errados
        String newvalue = (String) value;
        value = newvalue.trim();
        String groupsymbol = getDigitGroupingSymbol(locale);
        String regexp = "[+-]?[0-9]+([" + groupsymbol + "][0-9]{3})*";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000277");
        }
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");

        parsedvalue = new Integer(newvalue);
        if (negative) {
          parsedvalue = -parsedvalue;
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Este método formata um valor Long para se adequar a formatação do local.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatLong(Long value, Locale locale) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    return nf.format(value);
  }

  /**
   * Converte um objeto para Long. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @return Objeto Long com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Long parseLong(Object value, Locale locale) throws RFWException {
    return parseLong(value, locale, false);
  }

  /**
   * Converte um objeto para Long. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @return Objeto Long com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Long parseLong(Object value, Locale locale, boolean ignoresignal) throws RFWException {
    Long parsedvalue = null;
    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof Integer) {
        parsedvalue = new Long((Integer) value);
      } else if (value instanceof Long) {
        parsedvalue = new Long((Long) value);
      } else if (value instanceof String) {
        // Verifica se contem apenas os caracteres esperados para evitar que nímeros com caracteres estranhos (como pontos no lugar da virgula) sejam interpretados errados
        String newvalue = (String) value;
        value = newvalue.trim();
        String groupsymbol = getDigitGroupingSymbol(locale);
        String regexp = "[+-]?[0-9]+([" + groupsymbol + "][0-9]{3})*";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000277");
        }
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");

        parsedvalue = new Long(newvalue);
        if (negative) {
          parsedvalue = -parsedvalue;
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Este método formata um valor Float para se adequar a formatação do local. Por padrão deixa o número sempre com 2 casas decimais.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatFloat(Float value, Locale locale) {
    return formatFloat(value, locale, null);
  }

  /**
   * Este método formata um valor Float para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   */
  public static String formatFloat(Float value, Locale locale, Integer decimals) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (decimals != null) {
      nf.setMaximumFractionDigits(decimals);
      nf.setMinimumFractionDigits(decimals);
    }
    return nf.format(value);
  }

  /**
   * Este método formata um valor BigDecimal para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso não deseje só formar o número sem alterar as casas decimais.
   */
  public static String formatBigDecimal(BigDecimal value, Locale locale, Integer decimals) {
    if (value == null) {
      return "";
    }
    value = value.setScale(decimals, RFW.getRoundingMode());
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (decimals != null) {
      nf.setMaximumFractionDigits(decimals);
      nf.setMinimumFractionDigits(decimals);
    }
    return nf.format(value);
  }

  /**
   * Este método formata um valor BigDecimal para se adequar a formatação definida por parametros.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param minDecimals Quantidade de casas decimais fixas. Ou nulo caso o não deseje definir um número mínimode casas, apenas limita-las.
   * @param maxDecimals Quantidade máxima de casas decimais permitidas.
   * @param groupingUsed Define se deve ou não utilizar o caractere de agrupamento.
   */
  public static String formatBigDecimal(BigDecimal value, Locale locale, Integer minDecimals, Integer maxDecimals, boolean groupingUsed) {
    if (value == null) {
      return "";
    }
    value = value.setScale(maxDecimals, RFW.getRoundingMode());
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (maxDecimals != null) {
      nf.setMaximumFractionDigits(maxDecimals);
    }
    if (minDecimals != null) {
      nf.setMinimumFractionDigits(minDecimals);
    }
    nf.setGroupingUsed(groupingUsed);
    return nf.format(value);
  }

  /**
   * Este método formata um valor BigDecimal para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param minDecimals Quantidade de casas decimais fixas. Ou nulo caso o não deseje definir um número mínimode casas, apenas limita-las.
   * @param maxDecimals Quantidade máxima de casas decimais permitidas.
   */
  public static String formatBigDecimal(BigDecimal value, Locale locale, Integer minDecimals, Integer maxDecimals) {
    if (value == null) {
      return "";
    }
    value = value.setScale(maxDecimals, RFW.getRoundingMode());
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (maxDecimals != null) {
      nf.setMaximumFractionDigits(maxDecimals);
    }
    if (minDecimals != null) {
      nf.setMinimumFractionDigits(minDecimals);
    }
    return nf.format(value);
  }

  /**
   * Este método formata um valor BigDecimal para se adequar a formatação do local.<Br>
   * Este método mantém a quantidade de números decimais existente no BigDecimal
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatBigDecimal(BigDecimal value, Locale locale) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    nf.setMaximumFractionDigits(value.scale());
    nf.setMinimumFractionDigits(value.scale());
    return nf.format(value);
  }

  /**
   * Este método formata um valor Float para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param mindecimals Quantidade mínima de casas decimais.
   * @param maxdecimals Quantidade máxima de casas decimais.
   */
  public static String formatFloat(Float value, Locale locale, Integer mindecimals, Integer maxdecimals) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (mindecimals != null) {
      nf.setMinimumFractionDigits(mindecimals);
    }
    if (maxdecimals != null) {
      nf.setMaximumFractionDigits(maxdecimals);
    }
    return nf.format(value);
  }

  /**
   * Converte um objeto para Float. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   * <b>Atenção:</b> De acordo com o tamanho do número a precisão dos decimais poderão ser perdidas pelo float! Caso isso não seja desejado use Double.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @return Objeto Float com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Float parseFloat(Object value, Locale locale) throws RFWException {
    return parseFloat(value, locale, false, null);
  }

  public static BigDecimal parseBigDecimal(Object value, Locale locale) throws RFWException {
    return parseBigDecimal(value, locale, false, null, RFW.getRoundingMode());
  }

  /**
   * Converte um objeto para Float. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   * <b>Atenção:</b> De acordo com o tamanho do número a precisão dos decimais poderão ser perdidas pelo float! Caso isso não seja desejado use Double.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto Float com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Float parseFloat(Object value, Locale locale, boolean ignoresignal, Integer decimals) throws RFWException {
    return parseFloat(value, locale, ignoresignal, decimals, ROUNDPOLICY.NONE);
  }

  public static BigDecimal parseBigDecimal(Object value, Locale locale, boolean ignoresignal, Integer decimals) throws RFWException {
    return parseBigDecimal(value, locale, ignoresignal, decimals, RFW.getRoundingMode());
  }

  /**
   * Converte um objeto para BigDecimal. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   * <b>Atenção:</b> De acordo com o tamanho do número a precisão dos decimais poderão ser perdidas pelo float! Caso isso não seja desejado use Double.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto BigDecimal com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static BigDecimal parseBigDecimal(Object value, Locale locale, boolean ignoresignal, Integer decimals, RoundingMode roundingmode) throws RFWException {
    BigDecimal parsedvalue = null;

    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof BigDecimal) {
        parsedvalue = (BigDecimal) value;
      } else if (value instanceof Double) {
        parsedvalue = new BigDecimal((Double) value);
      } else if (value instanceof String) {
        String newvalue = (String) value;
        String groupsymbol = getDigitGroupingSymbol(locale);
        String decimalsymbol = getDecimalSymbol(locale);
        String regexp = "[+-]?[0-9]*([" + groupsymbol + "]*[0-9]+)*?[" + decimalsymbol + "]?[0-9]*";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000276");
        }
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");
        newvalue = RUString.replaceAll(newvalue, decimalsymbol, ".");
        try {
          parsedvalue = new BigDecimal(newvalue);
        } catch (NumberFormatException e) {
          throw new RFWValidationException("RFW_ERR_000488", new String[] { "" + value });
        }
        if (decimals != null) {
          parsedvalue = parsedvalue.setScale(decimals, roundingmode);
        }
        if (negative) {
          parsedvalue = parsedvalue.negate();
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Este método formata um valor Double para se adequar a formatação do local. Por padrão deixa o número sempre com 2 casas decimais.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatDouble(Double value, Locale locale) {
    return formatDouble(value, locale, null);
  }

  /**
   * Este método formata um valor Double para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   */
  public static String formatDouble(Double value, Locale locale, Integer decimals) {
    return formatDouble(value, locale, decimals, true);
  }

  /**
   * Este método formata um valor Double para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @param groupingused Define se o número deve usar o agrupador de milhares Ex: Caso true 1000L = "1.000"; Caso false 1000L => "1000";
   */
  public static String formatDouble(Double value, Locale locale, Integer decimals, boolean groupingused) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    nf.setGroupingUsed(groupingused);
    if (decimals != null) {
      nf.setMaximumFractionDigits(decimals);
      nf.setMinimumFractionDigits(decimals);
    }
    return nf.format(value);
  }

  /**
   * Converte um objeto para Double. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @return Objeto Double com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Double parseDouble(Object value, Locale locale) throws RFWException {
    return parseDouble(value, locale, false, null);
  }

  /**
   * Converte um objeto para Double. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto Double com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Double parseDouble(Object value, Locale locale, boolean ignoresignal, Integer decimals) throws RFWException {
    return parseDouble(value, locale, ignoresignal, decimals, ROUNDPOLICY.BESTROUND);
  }

  /**
   * Converte um objeto para Double. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto Double com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Double parseDouble(Object value, Locale locale, boolean ignoresignal, Integer decimals, ROUNDPOLICY roundpolicy) throws RFWException {
    Double parsedvalue = null;

    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof Float) {
        parsedvalue = new Double((Float) value);
      } else if (value instanceof Double) {
        parsedvalue = new Double((Double) value);
      } else if (value instanceof String) {
        String newvalue = ((String) value).trim();
        String groupsymbol = getDigitGroupingSymbol(locale);
        String decimalsymbol = getDecimalSymbol(locale);
        String regexp = "[+-]?[0-9]*([" + groupsymbol + "]*[0-9]+)*?[" + decimalsymbol + "]?[0-9]*";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000276");
        }
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");
        newvalue = RUString.replaceAll(newvalue, decimalsymbol, ".");
        if (!"".equals(newvalue)) {
          parsedvalue = new Double(newvalue);
          if (decimals != null) {
            if (ROUNDPOLICY.BESTROUND.equals(roundpolicy)) {
              parsedvalue = BUNumber.round(parsedvalue, decimals);
            } else if (ROUNDPOLICY.ROUNDCEIL.equals(roundpolicy)) {
              parsedvalue = BUNumber.roundceil(parsedvalue, decimals);
            } else if (ROUNDPOLICY.ROUNDFLOOR.equals(roundpolicy)) {
              parsedvalue = BUNumber.roundfloor(parsedvalue, decimals);
            } else if (ROUNDPOLICY.NONE.equals(roundpolicy)) {
              // Não faz nada deixa o número sem arredondamento
            }
          }
          if (negative) {
            parsedvalue = -parsedvalue;
          }
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Converte um objeto para Float. Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto Double com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Float parseFloat(Object value, Locale locale, boolean ignoresignal, Integer decimals, ROUNDPOLICY roundpolicy) throws RFWException {
    Float parsedvalue = null;

    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof Float) {
        parsedvalue = new Float((Float) value);
      } else if (value instanceof Double) {
        parsedvalue = new Float((Double) value);
      } else if (value instanceof String) {
        String newvalue = (String) value;
        String groupsymbol = getDigitGroupingSymbol(locale);
        String decimalsymbol = getDecimalSymbol(locale);
        String regexp = "[+-]?[0-9]*([" + groupsymbol + "]*[0-9]+)*?[" + decimalsymbol + "]?[0-9]*";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000276");
        }
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");
        newvalue = RUString.replaceAll(newvalue, decimalsymbol, ".");
        parsedvalue = new Float(newvalue);
        if (decimals != null) {
          if (ROUNDPOLICY.BESTROUND.equals(roundpolicy)) {
            parsedvalue = BUNumber.round(parsedvalue, decimals);
          } else if (ROUNDPOLICY.ROUNDCEIL.equals(roundpolicy)) {
            parsedvalue = BUNumber.roundceil(parsedvalue, decimals);
          } else if (ROUNDPOLICY.ROUNDFLOOR.equals(roundpolicy)) {
            parsedvalue = BUNumber.roundfloor(parsedvalue, decimals);
          } else if (ROUNDPOLICY.NONE.equals(roundpolicy)) {
            // Não faz nada deixa o número sem arredondamento
          }
        }
        if (negative) {
          parsedvalue = -parsedvalue;
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Este mítodo formata um valor currency para se adequar a formatação do local. Por padrão deixa o número sempre com 2 casas decimais.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatCurrency(Number value, Locale locale) {
    return formatCurrency(value, locale, null);
  }

  /**
   * Este método formata um valor currency (Double) para se adequar a formatação do local.<Br>
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   */
  public static String formatCurrency(Number value, Locale locale, Integer decimals) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (decimals != null) {
      nf.setMaximumFractionDigits(decimals);
      nf.setMinimumFractionDigits(decimals);
    } else {
      nf.setMaximumFractionDigits(2);
      nf.setMinimumFractionDigits(2);
    }
    return nf.format(value);
  }

  /**
   * Converte um objeto para Currency (Double). Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   * Por padrão deixa apenas 2 casas deciamis.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @return Objeto Double com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWValidationException Lançado sempre que não for possível converter a informação.
   * @throws RFWCriticalException
   */
  public static Double parseCurrency(Object value, Locale locale) throws RFWValidationException, RFWCriticalException {
    return parseCurrency(value, locale, false, null);
  }

  /**
   * Converte um objeto para Currency (Double). Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto Double com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWCriticalException
   */
  public static Double parseCurrency(Object value, Locale locale, boolean ignoresignal, Integer decimals) throws RFWValidationException, RFWCriticalException {
    Double parsedvalue = null;

    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof Float) {
        parsedvalue = new Double((Float) value);
      } else if (value instanceof Double) {
        parsedvalue = new Double((Double) value);
      } else if (value instanceof String) {
        String newvalue = (String) value;
        String groupsymbol = getDigitGroupingSymbol(locale);
        String decimalsymbol = getDecimalSymbol(locale);
        String regexp = "[+-]?[0-9]*([" + groupsymbol + "]*[0-9]+)*?[" + decimalsymbol + "]?[0-9]*";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000276");
        }
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");
        newvalue = RUString.replaceAll(newvalue, decimalsymbol, ".");
        parsedvalue = new Double(newvalue);
        if (decimals != null) {
          double decimalratio = Math.pow(10, decimals);
          parsedvalue = Math.round(parsedvalue.doubleValue() * decimalratio) / decimalratio;
        }
        if (negative) {
          parsedvalue = -parsedvalue;
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Este método formata um valor Percentage (BigDecimal) para se adequar a formatação do local. Por padrão deixa o número sempre com 1 casas decimais.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatPercentage(BigDecimal value, Locale locale) {
    return formatPercentage(value, locale, null);
  }

  /**
   * Este método formata um valor Percentage (BigDecimal) para se adequar a formatação do local. Por padrão deixa o número sempre com 1 casas decimais.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   */
  public static String formatPercentage(BigDecimal value, Locale locale, Integer decimals) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (decimals != null) {
      nf.setMaximumFractionDigits(decimals);
      nf.setMinimumFractionDigits(decimals);
    }
    return nf.format(value) + "%";
  }

  /**
   * Este método formata um valor Percentage (Float) para se adequar a formatação do local. Por padrão deixa o número sempre com 1 casas decimais.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatPercentage(Float value, Locale locale) {
    return formatPercentage(value, locale, null);
  }

  /**
   * Este método formata um valor Percentage (Float) para se adequar a formatação do local.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   */
  public static String formatPercentage(Float value, Locale locale, Integer decimals) {
    return formatPercentage(new Double(value), locale, decimals);
  }

  /**
   * Este método formata um valor Percentage para se adequar a formatação do local.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatPercentage(Integer value, Locale locale) {
    return formatPercentage(new Double(value), locale, 0);
  }

  /**
   * Este método formata um valor Percentage para se adequar a formatação do local.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatPercentage(Long value, Locale locale) {
    return formatPercentage(new Double(value), locale, 0);
  }

  /**
   * Este método formata um valor Percentage para se adequar a formatação do local. Por padrão deixa o número sempre com 1 casas decimais.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   */
  public static String formatPercentage(Double value, Locale locale) {
    return formatPercentage(value, locale, null);
  }

  /**
   * Este método formata um valor Percentage (Float) para se adequar a formatação do local.<Br>
   * Usar valor em porcentagem 100% é 100F. 1% é 1F.
   *
   * @param value Objeto a ser formatado
   * @param locale String com o valor formatado ou uma string vazia caso value seja nulo.
   * @param decimals Quantidade de casas decimais. Ou nulo caso o valor recebido seja nulo ou vazio.
   */
  public static String formatPercentage(Double value, Locale locale, Integer decimals) {
    if (value == null) {
      return "";
    }
    NumberFormat nf = NumberFormat.getNumberInstance(locale);
    if (decimals != null) {
      nf.setMaximumFractionDigits(decimals);
      nf.setMinimumFractionDigits(decimals);
    }
    return nf.format(value) + "%";
  }

  /**
   * Converte um objeto para Percentage (Float). Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @return Objeto Float com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Double parsePercentage(Object value, Locale locale) throws RFWException {
    return parsePercentage(value, locale, false, null);
  }

  /**
   * Converte um objeto para Percentage (Float). Caso seja uma String usa o locale para recuperar os simbolos da localidade. Caso seja outro objeto tenta fazer o parser da melhor maneira possível.<br>
   *
   * @param value Valor a ser formatado.
   * @param locale Localidade a ser usada para recuperar os simbolos.
   * @param ignoresignal ignora o sinal, retornando sempre um valor positivo.
   * @param decimals numero de casas decimais que se deseja ter o número arredondado. Nulo para manter quantas casas decimais forem possíveis.
   * @return Objeto Float com o valor parseado. Ou nulo caso o valor recebido seja nulo ou vazio.
   * @throws RFWException
   */
  public static Double parsePercentage(Object value, Locale locale, boolean ignoresignal, Integer decimals) throws RFWException {
    Double parsedvalue = null;

    if (value != null && !"".equals(value.toString().trim())) {
      if (value instanceof Float) {
        parsedvalue = new Double((Float) value);
      } else if (value instanceof Double) {
        parsedvalue = new Double((Double) value);
      } else if (value instanceof String) {
        String newvalue = (String) value;
        String groupsymbol = getDigitGroupingSymbol(locale);
        String decimalsymbol = getDecimalSymbol(locale);
        String regexp = "[+-]?[0-9]*([" + groupsymbol + "]*[0-9]+)*?[" + decimalsymbol + "]?[0-9]*[%]?";
        if (!newvalue.matches(regexp)) {
          throw new RFWValidationException("RFW_ERR_000276");
        }
        boolean negative = !ignoresignal && newvalue.indexOf("-") != -1;
        newvalue = RUString.replaceAll(newvalue, groupsymbol, "");
        newvalue = RUString.replaceAll(newvalue, "-", "");
        newvalue = RUString.replaceAll(newvalue, "+", "");
        newvalue = RUString.replaceAll(newvalue, "%", "");
        newvalue = RUString.replaceAll(newvalue, decimalsymbol, ".");
        if (!"".equals(newvalue)) {
          parsedvalue = new Double(newvalue);
          if (decimals != null) {
            double decimalratio = Math.pow(10, decimals);
            parsedvalue = (Math.round(parsedvalue.floatValue() * decimalratio)) / decimalratio;
          }
          if (negative) {
            parsedvalue = -parsedvalue;
          }
        } else {
          parsedvalue = null;
        }
      } else {
        throw new RFWCriticalException("RFW_ERR_000212", new String[] { value.getClass().toString() });
      }
    }
    return parsedvalue;
  }

  /**
   * Tabela de conversão de valores:
   * <ul>
   * <li>KB Kilobyte 1,024 Bytes</li>
   * <li>MB Megabyte 1,048,576 Bytes</li>
   * <li>GB Gigabyte 1,073,741,824 Bytes | One billion Bytes</li>
   * <li>TB Terrabyte 1024 GB, 1,048,576 MB, 8,388,608 KB, 1,099,511,627,776 Bytes and 8,796,093,022,208 bits.</li>
   * <li>PB Pettabyte 1024 TB, 1,048,576 GB, 1,073,741,824 MB, 1,099,511,627,776 KB, 1,125,899,906,842,624 Bytes and 9,007,199,254,740,992 bits.</li>
   * <li>EB Exabyte 1024 PB, 1,048,576 TB, 1,073,741,824 GB, 1,099,511,627,776 MB, 1,125,899,906,842,624 KB, 1,152,921,504,606,846,976 Bytes and 9,223,372,036,854,775,808 bits.</li>
   * <li>ZB Zettabyte 1024 EB, 1,048,576 PB, 1,073,741,824 TB, 1,099,511,627,776 GB, 1,125,899,906,842,624 MB, 1,152,921,504,606,846,976 KB, 1,180,591,620,717,411,303,424 Bytes and 9,444,732,965,739,290,427,392 bits</li>
   * <li>YB Yottabyte 1024 ZB, 1,048,576 EB, 1,073,741,824 PB, 1,099,511,627,776 TB, 1,125,899,906,842,624 GB, 1,152,921,504,606,846,976 MB, 1,180,591,620,717,411,303,424 KB 1,208,925,819,614,629,174,706,176 Bytes and 9,671,406,556,917,033,397,649,408 bits</li>
   * </ul>
   *
   * @param bytes
   * @param locale
   * @param decimals
   * @return String formatada com as informações dos bytes.
   * @throws NullPointerException
   */
  public static String formatBytesSize(Long bytes, Locale locale, Integer decimals) {
    if (bytes == null) {
      throw new NullPointerException("Impossível formatar valor nulo!");
    }

    int pow = 0;
    double newbytes = bytes;
    while (newbytes >= 1024 && pow < 8) {
      newbytes = newbytes / 1024;
      pow++;
    }

    String size = formatDouble(newbytes, locale, decimals);
    if (pow == 0) {
      size += "Bytes";
    } else if (pow == 1) {
      size += "KB";
    } else if (pow == 2) {
      size += "MB";
    } else if (pow == 3) {
      size += "GB";
    } else if (pow == 4) {
      size += "TB";
    } else if (pow == 5) {
      size += "PB";
    } else if (pow == 6) {
      size += "EB";
    } else if (pow == 7) {
      size += "ZB";
    } else if (pow == 8) {
      size += "YB";
    }
    return size;
  }

  /**
   * Transforma unidade de medida em outra, formatando com apenas duas casas decimais.<br>
   * Unidades de medida:<br>
   * <li>0 - Bytes</li>
   * <li>1 - KB Kilobyte 1,024 Bytes</li>
   * <li>2 - MB Megabyte 1,048,576 Bytes</li>
   * <li>3 - GB Gigabyte 1,073,741,824 Bytes | One billion Bytes</li>
   * <li>4 - TB Terrabyte 1024 GB, 1,048,576 MB, 8,388,608 KB, 1,099,511,627,776 Bytes and 8,796,093,022,208 bits.</li>
   * <li>5 - PB Pettabyte 1024 TB, 1,048,576 GB, 1,073,741,824 MB, 1,099,511,627,776 KB, 1,125,899,906,842,624 Bytes and 9,007,199,254,740,992 bits.</li>
   * <li>6 - EB Exabyte 1024 PB, 1,048,576 TB, 1,073,741,824 GB, 1,099,511,627,776 MB, 1,125,899,906,842,624 KB, 1,152,921,504,606,846,976 Bytes and 9,223,372,036,854,775,808 bits.</li>
   * <li>7 - ZB Zettabyte 1024 EB, 1,048,576 PB, 1,073,741,824 TB, 1,099,511,627,776 GB, 1,125,899,906,842,624 MB, 1,152,921,504,606,846,976 KB, 1,180,591,620,717,411,303,424 Bytes and 9,444,732,965,739,290,427,392 bits</li>
   * <li>8 - YB Yottabyte 1024 ZB, 1,048,576 EB, 1,073,741,824 PB, 1,099,511,627,776 TB, 1,125,899,906,842,624 GB, 1,152,921,504,606,846,976 MB, 1,180,591,620,717,411,303,424 KB 1,208,925,819,614,629,174,706,176 Bytes and 9,671,406,556,917,033,397,649,408 bits</li>
   *
   * @param size tamanho a ser convertido
   * @param entryDimension Unidade de medida original.
   * @param exitDimension Unidade de Medida final.
   * @throws RFWException
   */
  public static BigDecimal convertByteUnits(BigDecimal size, int entryDimension, int decimals, int exitDimension) throws RFWException {
    // Calcula o fator de utilizando o próprio índice passado
    BigDecimal entryFactor = new BigDecimal(1024).pow(entryDimension);
    BigDecimal exitFactor = new BigDecimal(1024).pow(exitDimension);

    // Calcula o fator de conversão dividindo a dimensão de entrada pela dimensão de saída
    BigDecimal cf = entryFactor.divide(exitFactor);
    // Calcula o novo tamanho
    return size.multiply(cf).setScale(decimals, RFW.getRoundingMode());
  }

  /**
   * Formata a parte de data (sem o time) para a notação usada no Brasil (dd/MM/yyyy HH:mm:ss)
   *
   * @param date A data a ser formatada.
   * @return Uma string contendo a data com a formatação aplicada.
   */
  public static String formatDateTime(Date date) {
    return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
  }

  /**
   * Formata a parte de data (sem o time) para a notação usada no Brasil (dd/MM/yyyy)
   *
   * @param date A data a ser formatada.
   * @return Uma string contendo a data com a formatação aplicada.
   */
  public static String formatDate(Date date) {
    return new SimpleDateFormat("dd/MM/yyyy").format(date);
  }

  /**
   * Este método faz o parser de datas no formato "dd/MM/yyyy" para {@link Date}.
   *
   * @param formatteddate
   * @return
   * @throws RFWException
   */
  public static Date parseDate_dd_MM_yyyy(String formatteddate) throws RFWException {
    try {
      return new SimpleDateFormat("dd/MM/yyyy").parse(formatteddate);
    } catch (ParseException e) {
      throw new RFWValidationException("RFW_ERR_100098", new String[] { formatteddate }, e);
    }
  }
}
