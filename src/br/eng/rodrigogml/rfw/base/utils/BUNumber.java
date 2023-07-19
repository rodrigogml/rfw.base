package br.eng.rodrigogml.rfw.base.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe de métodos utilitários para tratamento de números.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.1.0 (NOV / 2009)
 * @version 4.1.0 (23/06/2011) - rodrigogml - Trocou o nome, antes NumberUtils, para ficar no padrão do Framework.
 */
public class BUNumber {

  /**
   * Este método arredonda <b>para o lado mais próximo</b> números decimais (double) com um número máximo de casas (decimals).
   *
   * @param value valor decimal a ser arredondado
   * @param decimals número de casas máximo
   * @return
   */
  public static Double round(double value, int decimals) {
    double factor = Math.pow(10, decimals);
    double result = Math.round(value * factor) / factor;
    return result;
  }

  /**
   * Este método arredonda <b>para baixo</b> números decimais (double) com um número máximo de casas (decimals).
   *
   * @param value valor decimal a ser arredondado
   * @param decimals número de casas máximo
   * @return
   */
  public static Double roundfloor(double value, int decimals) {
    double factor = Math.pow(10, decimals);
    double result = Math.floor(value * factor) / factor;
    return result;
  }

  /**
   * Este método arredonda <b>para cima</b> números decimais (double) com um número máximo de casas (decimals).
   *
   * @param value valor decimal a ser arredondado
   * @param decimals número de casas máximo
   * @return
   */
  public static Double roundceil(double value, int decimals) {
    double factor = Math.pow(10, decimals);
    double result = Math.ceil(value * factor) / factor;
    return result;
  }

  /**
   * Este método arredonda <b>para o lado mais próximo</b> números decimais (float) com um número máximo de casas (decimals).
   *
   * @param value valor decimal a ser arredondado
   * @param decimals número de casas máximo
   * @return
   */
  public static Float round(float value, int decimals) {
    double factor = Math.pow(10, decimals);
    float result = (float) (Math.round(value * factor) / factor);
    return result;
  }

  /**
   * Este método arredonda <b>para baixo</b> números decimais (float) com um número máximo de casas (decimals).
   *
   * @param value valor decimal a ser arredondado
   * @param decimals número de casas máximo
   * @return
   */
  public static Float roundfloor(float value, int decimals) {
    double factor = Math.pow(10, decimals);
    float result = (float) (Math.floor(value * factor) / factor);
    return result;
  }

  /**
   * Este método arredonda <b>para cima</b> números decimais (float) com um número máximo de casas (decimals).
   *
   * @param value valor decimal a ser arredondado
   * @param decimals número de casas máximo
   * @return
   */
  public static Float roundceil(float value, int decimals) {
    double factor = Math.pow(10, decimals);
    float result = (float) (Math.ceil(value * factor) / factor);
    return result;
  }

  /**
   * Remove os zeros não significativos (depois do ponto), formata o divisor de decimal de acordo com o Locale. Para garantir o uso do "." no resultado final utilize o Locale.ENGLISH.<br>
   * Não coloca separados de milhares.<br>
   * Deixa no Máximo 2 dígitos, isto é, depois da virgula remove os zeros não significativos e arredonda os dígitos se passarem de 2.
   *
   * @param number
   * @return
   */
  public static String removeNonSignificantZeros(BigDecimal number, Locale locale) {
    DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(locale));
    return df.format(number);
  }

  /**
   * Remove os zeros não significativos (depois do ponto), formata o divisor de decimal de acordo com o Locale. Para garantir o uso do "." no resultado final utilize o Locale.ENGLISH.<br>
   * Não coloca separados de milhares.
   *
   * @param number
   * @return
   */
  public static String removeNonSignificantZeros(BigDecimal number, Locale locale, int maxdecimals) {
    String pattern = RUString.completeUntilLengthRight("#", "0.", maxdecimals + 2);
    DecimalFormat df = new DecimalFormat(pattern, new DecimalFormatSymbols(locale));
    return df.format(number);
  }

  /**
   * Extrai a parte inteira e a parte decimal de um BigDecimal passado.
   *
   * @param value BigDecimal a ser dividido entre as duas partes
   * @return Array com as duas partes. Index 0 = Parte inteira; Index 1 = parte Fracionária
   */
  public static BigDecimal[] extractIntegerAndFractionPart(BigDecimal value) {
    final BigDecimal fraction = value.remainder(BigDecimal.ONE);
    BigDecimal integer = value.subtract(fraction);
    return new BigDecimal[] { integer, fraction };
  }

  /**
   * Este método abstrai os cálculos que temos de fazer para gerar um número inteiro aleatório. Simplificando o código com uma chamada onde simplismente passamos os valores iniciais e final.<br>
   * <B>ATENÇÃO: </b> os valores passados são inclusivos, isto é, os valores passados podem ser retornados como resultado da função.
   *
   * @param min
   * @param max
   * @return número aleatório entre os valores min e max, inclusive.
   */
  public static int randomInt(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  /**
   * Converte um array de Bytes para uma String no formato "[01]*", sendo a posição '0' do array impresso mais a esquerda.
   *
   * @param bytes Bytes que serão convertidos para uma String Binária.
   * @return Strig com os valores dos bytes em binário. Posição 0 do array = 8bits mais a esquerda, posição 1 = próximos 8 bits, e assim por diante
   */
  public static String byteArrayToBinaryString(byte[] bytes) {
    StringBuilder buff = new StringBuilder();
    for (int j = 0; j < bytes.length; j++) {
      buff.append(String.format("%8s", Integer.toBinaryString(bytes[j] & 0xFF)).replace(' ', '0'));
    }
    return buff.toString();
  }

  /**
   * Converte um Bytes para uma String no formato "[01]*", sendo a posição '0' do array impresso mais a esquerda.
   *
   * @param bytes Bytes que serão convertidos para uma String Binária.
   * @return Strig com os valores dos bytes em binário. Posição 0 do array = 8bits mais a esquerda, posição 1 = próximos 8 bits, e assim por diante
   */
  public static String byteToBinaryString(byte bt) {
    return String.format("%8s", Integer.toBinaryString(bt & 0xFF)).replace(' ', '0');
  }

  /**
   * Transforma uma sequencia de bytes no formato String (Ex: "11101110111011101110111011101110") em um array de bytes.<br>
   * Faz o processo inverso do {@link #byteArrayToBinaryString(byte[])}
   *
   * @param bytes String com a sequência de bits para ser transformada em bytes.
   * @return Array de bytes que representam os bytes (cada 8 bits) recebido. O tamanho do array será de 1/8 o tamanho da String.
   * @throws RFWException
   */
  public static byte[] binaryStringToByteArray(String bytes) throws RFWException {
    if (bytes.length() % 8 != 0) throw new RFWCriticalException("O valor binário não tem múltiplos de 8 bits! (Len: " + bytes.length() + ") - " + bytes);

    byte[] array = new byte[bytes.length() / 8];
    for (int i = 0; i < array.length; i++) {
      String bits = bytes.substring(8 * i, 8 * (i + 1));
      array[i] = (byte) Integer.parseUnsignedInt(bits, 2);
    }
    return array;
  }

  /**
   * Este método obtem um array de bytes e converte para uma string, utilizando os caretes que represemtam os bytes.
   *
   * @param bytes Array de bytes para serem transformados em HEX.
   *
   * @return String contendo os valores do array transformados em dígitos Hexadecimal.
   */
  public static String byteArrayToHexString(byte[] bytes) {
    char[] hexArray = "0123456789ABCDEF".toCharArray();
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  /**
   * Converte uma String (utilizando os bytes dos caractres como valores Hex) para um bytearray com valores Hexa.<br>
   * Ex: Se entrar: "3132333435363738393031323334353637383930" -> o byte array resultante monta a String "12345678901234567890" se new String(retByte[])
   *
   * @param s
   * @return
   */
  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  /**
   * Este método cria uma sequência numérica aleatória.
   *
   * @param length Tamanho/Quantida de Dígitos da sequência
   */
  public static String generateNumericSequence(int length) {
    String seq = "";
    while (seq.length() != length) {
      seq += ("" + Math.random()).replaceAll("\\.", "");
      if (seq.length() > length) {
        // Obtem os dígitos mais a direita pois eles se alteram mais do que os primeiros gerados pelo Math.random()
        seq = seq.substring(seq.length() - length, seq.length());
      }
    }
    return seq;
  }

  /**
   * Retorna o maior número entre os valores passados.
   *
   * @param numbers números inteiros para retornar o maior
   * @return O menor número encontrado. Retorna nulo caso não receba nenhum número, array nulo, ou array só com valores nulos.
   */
  public static Integer max(Integer... numbers) {
    Integer v = null;
    if (numbers != null) {
      for (Integer i : numbers) {
        if (i != null && (v == null || v < i)) {
          v = i;
        }
      }
    }
    return v;
  }

  /**
   * Garante que o BigDecimal terá no mínimo a escala desejada.
   *
   * @param value Valor para ter a escala corrigida
   * @param minScale Quantidade de dígitos mínimos na escala.
   * @return Retorna nulo se value for nulo. Retorna o value caso já tenha uma precisão maior ou igual ao minScale definido. Retorna o mesmo value com a a precisão aumentada para para o minScale caso a precisão atual seja menor que minScale.
   */
  public static BigDecimal minScale(BigDecimal value, int minScale) {
    if (value == null) return null;
    if (value.scale() < minScale) return value.setScale(minScale);
    return value;
  }

}