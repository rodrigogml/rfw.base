package br.eng.rodrigogml.rfw.base.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.BaseEncoding;

import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;

/**
 * Description: Classe com métodos úteis para tratamentos e manipulação de String.<br>
 *
 * @author Rodrigo Leitão
 * @since 1.0.0 (AGO / 2007)
 * @version 4.1.0 (23/06/2011) - rodrigogml - Nome alterado de StringUtils, para ficar no padrão do sistema.
 * @deprecated Movido para RFW.Kernel na classe RUString
 */
@Deprecated
public class BUString {

  /**
   * Array com os digitos: 0-9.
   */
  public static final char[] digits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

  /**
   * Array com os caracteres: a-z, A-Z e 0-9.
   */
  public static final char[] simplechars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

  /**
   * Array com a maioria dos caracteres "comuns".
   */
  public static final char[] allchars = new char[] { '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '¡', '¢', '£', '¤', '¥', '¦', '§', '¨', '©', 'ª', '«', '¬', '­', '®', '¯', '°', '±', '²', '³', '´', 'µ', '¶', '·', '¸', '¹', 'º', '»', '¼', '½', '¾', '¿', 'À', 'Á', 'Â', 'Ã', 'Ä', 'Å', 'Æ', 'Ç', 'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï', 'Ð', 'Ñ', 'Ò', 'Ó', 'Ô', 'Õ', 'Ö', '×', 'Ø', 'Ù', 'Ú', 'Û', 'Ü', 'Ý', 'Þ', 'ß', 'à', 'á', 'â', 'ã', 'ä', 'å', 'æ', 'ç', 'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö', '÷', 'ø', 'ù', 'ú', 'û', 'ü', 'ý', 'þ', 'ÿ', 'Œ', 'œ', 'Š', 'š', 'Ÿ', 'Ž',
      'ž', 'ƒ' };

  /*
   * Referencia os métodos de normalização de string para cada um dos jdks, para evitar de se fazer diversas reflexões em cada vez que se usa o método de remover acentos.
   */
  private static Method normalizerJDK5 = null; // Salva o método que será usado na normalização da string no JDK5
  private static Method normalizerJDK6 = null;// Salva o método que será usado na normalização da string no JDK6
  private static Object normalizerJDK6form = null; // Salva o form necessário para o normalizer do jdk6
  private static Boolean unknownormalizer = null; // Salva se o método de normalização é desconhecido, null não procurado ainda, true desconhecido (usa modo manual), false conhecido

  /**
   * Remove a acentuação de um texto passado. Incluindo 'ç' por 'c', maiúsculas e minúscas (preservando a captalização da letra).
   *
   * @param text String que terá seus acentos removidos.
   * @return String sem caracteres acentuados, trocados pelos seus correspondentes.
   */
  public static String removeAccents(String text) {
    // Verifica se conhece o método de normalização
    if (unknownormalizer == null) { // Se ainda não foi procurado, procura
      try {
        // Tenta Compatibilidade com JDK 6 - Evita o Import para evitar erros de compilação e execução
        // Recupera a Classe do Normalizer
        Class<?> normalizer = Class.forName("java.text.Normalizer");
        // Encontra a classe do Form
        Class<?> normalizerform = Class.forName("java.text.Normalizer$Form");
        // Encontra e enum NFD
        normalizerJDK6form = null;
        for (int i = 0; i < normalizerform.getEnumConstants().length; i++) {
          if ("NFD".equals(normalizerform.getEnumConstants()[i].toString())) {
            normalizerJDK6form = normalizerform.getEnumConstants()[i];
            break;
          }
        }
        normalizerJDK6 = normalizer.getMethod("normalize", new Class[] { CharSequence.class, normalizerform });
        unknownormalizer = Boolean.FALSE;
        return ((String) normalizerJDK6.invoke(null, new Object[] { text, normalizerJDK6form })).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
      } catch (Exception ex) {
        try {
          // Compatibilidade com JDK 5 - Evita o Import para evitar erros de compilação e execução
          Class<?> normalizerC = Class.forName("sun.text.Normalizer");
          normalizerJDK5 = normalizerC.getMethod("decompose", new Class[] { String.class, boolean.class, int.class });
          unknownormalizer = Boolean.FALSE;
          return ((String) normalizerJDK5.invoke(null, new Object[] { text, false, 0 })).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception ex2) {
          // Salva como modo manual, normalizador desconhecido
          unknownormalizer = Boolean.TRUE;
          text = text.replaceAll("[áàãâä]", "a");
          text = text.replaceAll("[éèêë]", "e");
          text = text.replaceAll("[íìîï]", "i");
          text = text.replaceAll("[óòõôö]", "o");
          text = text.replaceAll("[úùûü]", "u");
          text = text.replaceAll("[ç]", "c");
          text = text.replaceAll("[ñ]", "n");
          text = text.replaceAll("[ÁÀÃÂÄ]", "A");
          text = text.replaceAll("[ÉÈÊË]", "E");
          text = text.replaceAll("[ÍÌÎÏ]", "I");
          text = text.replaceAll("[ÓÒÕÔÖ]", "O");
          text = text.replaceAll("[ÚÙÛÜ]", "U");
          text = text.replaceAll("[Ñ]", "N");
          return text;
        }
      }
    } else if (unknownormalizer) {
      text = text.replaceAll("[áàãâä]", "a");
      text = text.replaceAll("[éèêë]", "e");
      text = text.replaceAll("[íìîï]", "i");
      text = text.replaceAll("[óòõôö]", "o");
      text = text.replaceAll("[úùûü]", "u");
      text = text.replaceAll("[ç]", "c");
      text = text.replaceAll("[ñ]", "n");
      text = text.replaceAll("[ÁÀÃÂÄ]", "A");
      text = text.replaceAll("[ÉÈÊË]", "E");
      text = text.replaceAll("[ÍÌÎÏ]", "I");
      text = text.replaceAll("[ÓÒÕÔÖ]", "O");
      text = text.replaceAll("[ÚÙÛÜ]", "U");
      text = text.replaceAll("[Ñ]", "N");
      return text;
    } else if (!unknownormalizer) {
      if (normalizerJDK6 != null) {
        try {
          return ((String) normalizerJDK6.invoke(null, new Object[] { text, normalizerJDK6form })).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("Error while normalizing string with JDK6 compatible method!");
        }
      } else if (normalizerJDK5 != null) {
        try {
          return ((String) normalizerJDK5.invoke(null, new Object[] { text, false, 0 })).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("Error while normalizing string with JDK5 compatible method!");
        }
      }
    }
    return null;
  }

  /**
   * Remove os caracteres inválidospara UTF-8. Trocando letras acentuadas por suas correspondentes sem acentos, e outros caracteres inválidos pelo caractere '?'.
   *
   * @param text Texto a ser processado.
   * @return Texto processado.
   * @throws RFWException Lançado caso ocorra alguma falha em processar o texto.
   */
  public static String removeNonUTF8(String text) throws RFWException {
    try {
      CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPLACE);
      decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
      CharBuffer parsed = decoder.decode(ByteBuffer.wrap(text.getBytes("UTF-8")));
      return parsed.toString();
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200451", new String[] { text }, e);
    }
  }

  /**
   * Remove todos os caracteres que não compõe os primeiros 128 caracteres da tabela UTF-8 pelo texto passado.
   *
   * @param text Texto a ser tratado.
   * @return Texto sem os caracteres fora dos primeiros caracteres da tabela UTF-8.
   * @throws RFWException
   */
  public static String removeNonUTF8BaseCaracters(String text) throws RFWException {
    return replaceNonUTF8BaseCaracters(text, "");
  }

  /**
   * Substitui todos os caracteres que não compõe os primeiros 128 caracteres da tabela UTF-8 pelo texto passado.
   *
   * @param text Texto a ser tratado.
   * @param replacement Valor que substituirá os caracteres removidos
   * @return Texto tratado.
   * @throws RFWException
   */
  public static String replaceNonUTF8BaseCaracters(String text, String replacement) throws RFWException {
    return text.replaceAll("[^\\u0000-\\u007E]", replacement);
  }

  /**
   * Substitui todas as ocorrencias de 'oldvalue' por 'newvalue' no texto de 'value'. No entanto este método diferencia maiúsculas, minúsculas, acentos, etc.
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuirá oldvalue.
   * @return
   */
  public static String replaceAll(String text, String oldvalue, String newvalue) {
    return replaceAll(text, oldvalue, newvalue, Boolean.TRUE, Boolean.TRUE);
  }

  /**
   * Substitui o texto recursivamente até que o texto não sofra mais alterações, isto é, o texto será procurado do inicio ao fim pela substituição quantas vezes for necessárias até que seja feita uma busca completa e nada seja encontrado.<br>
   * <b>ATENÇÂO:</b> Pode gerar StackOverflow facilmente se substituimos um texto por outro que contém o valor sendo procurado!<Br>
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuirá oldvalue.
   * @return
   */
  public static String replaceAllRecursively(String text, String oldvalue, String newvalue) {
    String oldtext = text;
    text = replaceAll(text, oldvalue, newvalue);
    while (!oldtext.equals(text)) {
      oldtext = text;
      text = replaceAll(text, oldvalue, newvalue);
    }
    return text;
  }

  /**
   * Substitui o texto recursivamente até que o texto não sofra mais alterações, isto é, o texto será procurado do inicio ao fim pela substituição quantas vezes for necessárias até que seja feita uma busca completa e nada seja encontrado.<br>
   * <b>ATENÇÂO:</b> Pode gerar StackOverflow facilmente se substituimos um texto por outro que contém o valor sendo procurado!<Br>
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuirá oldvalue.
   * @param distinctaccents true distingue acentos, false ignora acentos
   * @param distinctcase true distingue letras maiusculas de minusculas, false ignora case das letras.
   * @return
   */
  public static String replaceAllRecursively(String text, String oldvalue, String newvalue, Boolean distinctaccents, Boolean distinctcase) {
    String oldtext = text;
    text = replaceAll(text, oldvalue, newvalue, distinctaccents, distinctcase);
    while (!oldtext.equals(text)) {
      oldtext = text;
      text = replaceAll(text, oldvalue, newvalue, distinctaccents, distinctcase);
    }
    return text;
  }

  /**
   * Substitui todas as ocorrencias de 'oldvalue' por 'newvalue' no texto de 'value'.<Br>
   * De acordo com as definições passadas, ele ignora acentos e case de letras.
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuirá oldvalue.
   * @param distinctaccents true distingue acentos, false ignora acentos
   * @param distinctcase true distingue letras maiusculas de minusculas, false ignora case das letras.
   * @return
   */
  public static String replaceAll(String text, String oldvalue, String newvalue, Boolean distinctaccents, Boolean distinctcase) {
    if (oldvalue.equals("")) {
      throw new IllegalArgumentException("Old value must have content.");
    }

    String ntext = text;
    String noldvalue = oldvalue;
    if (!distinctaccents) {
      ntext = removeAccents(ntext);
      noldvalue = removeAccents(noldvalue);
    }
    if (!distinctcase) {
      ntext = ntext.toUpperCase();
      noldvalue = noldvalue.toUpperCase();
    }

    // Com os parametros corrigos (tirados os acentos se for o caso, ou em maiusculas se for o caso) verificamos as ocorrencias
    StringBuilder buff = new StringBuilder();

    int startIdx = 0;
    int idxOld = 0;
    while ((idxOld = ntext.indexOf(noldvalue, startIdx)) >= 0) {
      // grab a part of aInput which does not include aOldPattern
      buff.append(text.substring(startIdx, idxOld));
      // add aNewPattern to take place of aOldPattern
      buff.append(newvalue);

      // reset the startIdx to just after the current match, to see
      // if there are any further matches
      startIdx = idxOld + noldvalue.length();
    }
    // the final chunk will go to the end of aInput
    buff.append(text.substring(startIdx));

    return buff.toString();
  }

  /**
   * Converte o objeto que contém o valor da enumeração (a própria enumeração) em "chave".<br>
   * O mesmo que o método <code>getEnumKey()</code>, exceto pelo acrescimo do valor da enumeração ao final.
   *
   * @param value enum desejado.
   * @return String com da chave.
   */
  public static String getEnumContainer(Enum<?> value) {
    String enumcontainer = null;
    if (value != null) {
      enumcontainer = value.getDeclaringClass().getCanonicalName();
    }
    return enumcontainer;
  }

  /**
   * Recupera a parte direita de uma string.
   *
   * @param value String original
   * @param length tamanho da parte que se deseja.
   * @return Retorna uma string do tamanho desejado contendo a parte direita da string. Retorna a string original caso o tamanho solicitado seja maior ou igual ao tamanho da original. Retorna null caso a string original seja nula.
   */
  public static String right(String value, int length) {
    String ret = null;
    if (value != null) {
      if (value.length() <= length) {
        ret = value;
      } else if (length <= 0) {
        ret = "";
      } else {
        ret = value.substring(value.length() - length, value.length());
      }
    }
    return ret;
  }

  /**
   * Recupera a parte esquerda de uma string.
   *
   * @param value String original
   * @param length tamanho da parte que se deseja.
   * @return Retorna uma string do tamanho desejado contendo a parte esquerda da string. Retorna a string original caso o tamanho solicitado seja maior ou igual ao tamanho da original. Retorna null caso a string original seja nula.
   */
  public static String left(String value, int length) {
    String ret = null;
    if (value != null) {
      if (value.length() <= length) {
        ret = value;
      } else if (length <= 0) {
        ret = "";
      } else {
        ret = value.substring(0, length);
      }
    }
    return ret;
  }

  /**
   * Conta quantas vezes um caracter aparece numa string.
   *
   * @param value String original
   * @param length tamanho da parte que se deseja.
   * @return Retorna uma string do tamanho desejado contendo a parte esquerda da string. Retorna a string original caso o tamanho solicitado seja maior ou igual ao tamanho da original. Retorna null caso a string original seja nula.
   */
  public static int count(String value, char delim) {
    int countChars = 0;
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) == delim) {
        countChars++;
      }
    }
    return countChars;
  }

  public static int count(StringBuilder value, char delim) {
    int countChars = 0;
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) == delim) {
        countChars++;
      }
    }
    return countChars;
  }

  /**
   * Conta quantas linhas existem em uma String. Em outras palavras conta quantas quebras de linha foram encontradas
   *
   * @param value Texto com as quebras de linhas para contar
   * @return
   */
  public static int countLines(String value) {
    return count(value, '\n');
  }

  /**
   * Conta quantas linhas existem em uma String. Em outras palavras conta quantas quebras de linha foram encontradas
   *
   * @param value Texto com as quebras de linhas para contar
   * @return
   */
  public static int countLines(StringBuilder value) {
    return count(value, '\n');
  }

  /**
   * Gera uma String qualquer no padrão: [A-Za-z0-9]{length}
   *
   * @param length tamanho exato da String desejada
   * @return
   */
  public static String genString(int length) {
    StringBuilder buf = new StringBuilder(length);
    while (buf.length() < length) {
      buf.append(BUString.simplechars[(int) (Math.random() * BUString.simplechars.length)]);
    }
    return buf.toString();
  }

  /**
   * Gera uma String qualquer no padrão: [0-9]{length}
   *
   * @param length tamanho da String desejada
   * @return
   */
  public static String genStringDigits(int length) {
    StringBuilder buf = new StringBuilder(length);
    while (buf.length() < length) {
      buf.append(BUString.digits[(int) (BUString.digits.length - (Math.random() * 10))]);
    }
    return buf.toString();
  }

  /**
   * Cria uma String com n repetições de uma determinada cadeira de caracteres (ou caracter simples).
   *
   * @param repeats Número de repetições na String final.
   * @param base Conteúdo a ser repetido na String.
   * @return String montada conforme as definições. Com tamanho total = repeats * base.length();
   */
  public static String genString(int repeats, String base) {
    return new String(new char[repeats]).replaceAll("\0", base);
  }

  /**
   * Este método recebe um valor string e quebra em linhas com o tamanho máximo definido. Este método quebrará as linhas somente nos espaços em branco entre as palavras, não quebra as palavras no meio.
   *
   * @param content Conteúdo a ser quebrado em linhas
   * @param maxlength tamanho máximo de cada linha.
   * @return Array de String com todas as linhas criadas.
   */
  public static String[] breakLineInBlankSpaces(String content, int maxlength) {
    final LinkedList<String> lines = new LinkedList<>();

    String[] blines = content.split("\\ ");
    final StringBuilder b = new StringBuilder(maxlength);
    for (int i = 0; i < blines.length; i++) {
      // Verifica se ainda cabe na mesmoa linha
      if (b.length() + blines[i].length() + 1 <= maxlength) { // O +1 refere-se ao espaço que será adicionado entre o conteúdo do buffer e a nova palavra
        b.append(" ").append(blines[i]);
      } else {
        lines.add(b.toString());
        b.delete(0, b.length());
        b.append(blines[i]);
      }
    }
    // Ao acabar, verificamose se temos conteúdo no buff e passamos e acrescentamos à lista, caso contrário perdemos a última linha
    if (b.length() > 0) lines.add(b.toString());
    String[] a = new String[lines.size()];
    return lines.toArray(a);
  }

  /**
   * Método utilizado para converter um byte array de base 64 em uma String para Hexadecimal.<br>
   * Este método é utilizado por exemplo para receber o bytearray do campo DigestValue do XML da NFe/NFCe (cuja base é 64), e converte para uma representação Hexadecimal. Essa representação Hexa é utilizada na geração da URL no QRCode da NFCe.
   *
   * @param bytearray
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHexFromBase64(byte[] bytearray) throws RFWException {
    return BUString.toHex(Base64.getEncoder().encodeToString(bytearray));
  }

  /**
   * Método utilizado extrair o byte array de base 64 a partir de uma string que represente um valor em hexa.<br>
   * Faz o procedimento contrário ao {@link #toHexFromBase64(byte[])}<br>
   *
   * @param bytearray
   * @param hexstring
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static byte[] fromHexToByteArrayBase64(String hexstring) throws RFWException {
    return Base64.getDecoder().decode(BUString.fromHexToByteArray(hexstring));
  }

  /**
   * Método utilizado para converter uma String para Hexadecimal.<br>
   * Este método utiliza o CharSet Padrão do ambiente.
   *
   * @param value Valor a ser convertido
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHex(String value) throws RFWException {
    return toHex(value.getBytes(/* YOUR_CHARSET? */));
  }

  /**
   * Método utilizado para converter uma String para Hexadecimal.<br>
   * Este método permite identificar o charset usado para decodificar a String.
   *
   * @param value Valor a ser convertido
   * @param charset Charset para decodificação da String
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHex(String value, Charset charset) throws RFWException {
    return toHex(value.getBytes(charset));
  }

  /**
   * Método utilizado para converter um array de bytes para Hexadecimal.<br>
   *
   * @param bytes cadeia de bytes a ser convertido para uma String representando o valor Hexadecimal
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHex(byte[] bytes) throws RFWException {
    return String.format("%040x", new BigInteger(1, bytes));
  }

  /**
   * Este método recebe uma string representando valores em hexa e retorna os valores em um array de bytes.
   *
   * @param hexstring String representando um valor hexa
   * @return array de bytes com os mesmos valores representados em hexa na string.
   * @throws RFWException
   */
  public static byte[] fromHexToByteArray(String hexstring) throws RFWException {
    // Valida a String recebida se só tem caracteres em Hexa
    if (!hexstring.matches("[0-9A-Fa-f]*")) {
      throw new RFWValidationException("RFW_ERR_200362");
    }
    return new BigInteger(hexstring, 16).toByteArray();
  }

  /**
   * Método utilizado para converter uma string de valor hexadecimal para uma String. Utiliza os bytes dos valores hexa decimal para converter em String utilizando o charset padrão do sistema.
   *
   * @param hexstring String com valores em hexa
   * @return String montada usando os bytes do valor hexa com o charset padrão do sistema.
   * @throws RFWException
   */
  public static String fromHexToString(String hexstring) throws RFWException {
    // Valida a String recebida se só tem caracteres em Hexa
    if (!hexstring.matches("[0-9A-Fa-f]*")) {
      throw new RFWValidationException("RFW_ERR_200362");
    }
    return new String(new BigInteger(hexstring, 16).toByteArray());
  }

  /**
   * Calcula a Hash SHA1 de uma String.
   *
   * @param value Valor a ter a Hash calculada.
   * @return Valor em Hexa calculado com o algorítimo de SHA1.
   * @throws RFWException
   */
  public static String calcSHA1(String value) throws RFWException {
    try {
      MessageDigest cript = MessageDigest.getInstance("SHA-1");
      cript.reset();
      cript.update(value.getBytes());
      return toHex(cript.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new RFWCriticalException("RFW_ERR_200307", e);
    }
  }

  /**
   * Calcula a Hash SHA1 de uma String.
   *
   * @param value Valor a ter a Hash calculada.
   * @param charset Defineo charset do valor, usado para converter corretamente em bytes.
   * @return Valor em Hexa calculado com o algorítimo de SHA1.
   * @throws RFWException
   */
  public static String calcSHA1(String value, String charset) throws RFWException {
    try {
      MessageDigest cript = MessageDigest.getInstance("SHA-1");
      cript.reset();
      cript.update(value.getBytes(charset));
      return toHex(cript.digest());
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200307", e);
    }
  }

  /**
   * Este método recebe um valor inteiro e o converte para letras no padrão de colunas do Excel.<br>
   * <b>Por exemplo 1 -> A, 2 -> B, ..., 26 -> Z, 27 -> AA, 28 -> AB, ...</b>
   *
   * @param value valor numérico a ser convertido.
   * @return Letras equivalendo o valor convertido.
   */
  public static String convertToExcelColumnLetters(long value) {
    final StringBuilder buff = new StringBuilder();
    value = value - 1; // Corrige valor de value para começar em 0, já que a definição do método diz que o primeiro valor é 1 e não 0.
    while (value > -1) {
      int mod = (int) (value % 26);
      // Converte o valor para o char adequado
      buff.insert(0, Character.toChars(mod + 65)[0]);
      value = value / 26 - 1;
    }
    return buff.toString();
  }

  /**
   * Este método realiza o "escape" dos carateres que atrapalham o parser do do XML. Não faz escape de todos os caracteres acentuados e fora da tabela padrão, simplesmente faz escape dos seguintes caracteres que atrapalham a estrutura do XML:<br>
   * <li>< - &amp;lt;</li>
   * <li>> - &amp;gt;</li>
   * <li>& - &amp;amp;</li>
   * <li>“ - &amp;quot;</li>
   * <li>‘ - &amp;#39;</li>
   *
   * @param clientname
   * @return
   */
  public static String escapeXML(String text) {
    text = text.replaceAll("\\<", "&lt;");
    text = text.replaceAll("\\>", "&gt;");
    text = text.replaceAll("\\&", "&amp;");
    text = text.replaceAll("\\\"", "&quot;");
    text = text.replaceAll("\\'", "&#39;");
    return text;
  }

  /**
   * Método utilizado para "escapar" os caracteres especiais em HTML.<Br>
   *
   * @param text
   */
  public static String escapeHTML(String text) {
    text = text.replaceAll("\\&", "&amp;");
    text = text.replaceAll("\\¥", "&yen;");
    text = text.replaceAll("\\Ý", "&Yacute;");
    text = text.replaceAll("\\ý", "&yacute;");
    text = text.replaceAll("\\Ü", "&Uuml;");
    text = text.replaceAll("\\ü", "&uuml;");
    text = text.replaceAll("\\¨", "&uml;");
    text = text.replaceAll("\\Ù", "&Ugrave;");
    text = text.replaceAll("\\ù", "&ugrave;");
    text = text.replaceAll("\\Û", "&Ucirc;");
    text = text.replaceAll("\\û", "&ucirc;");
    text = text.replaceAll("\\Ú", "&Uacute;");
    text = text.replaceAll("\\ú", "&uacute;");
    text = text.replaceAll("\\×", "&times;");
    text = text.replaceAll("\\Þ", "&THORN;");
    text = text.replaceAll("\\þ", "&thorn;");
    text = text.replaceAll("\\ß", "&szlig;");
    text = text.replaceAll("\\³", "&sup3;");
    text = text.replaceAll("\\²", "&sup2;");
    text = text.replaceAll("\\¹", "&sup1;");
    text = text.replaceAll("\\§", "&sect;");
    text = text.replaceAll("\\®", "&reg;");
    text = text.replaceAll("\\»", "&raquo;");
    text = text.replaceAll("\\\"", "&quot;");
    text = text.replaceAll("\\£", "&pound;");
    text = text.replaceAll("\\±", "&plusmn;");
    text = text.replaceAll("\\¶", "&para;");
    text = text.replaceAll("\\Ö", "&Ouml;");
    text = text.replaceAll("\\ö", "&ouml;");
    text = text.replaceAll("\\Õ", "&Otilde;");
    text = text.replaceAll("\\õ", "&otilde;");
    text = text.replaceAll("\\Ø", "&Oslash;");
    text = text.replaceAll("\\ø", "&oslash;");
    text = text.replaceAll("\\º", "&ordm;");
    text = text.replaceAll("\\ª", "&ordf;");
    text = text.replaceAll("\\Ò", "&Ograve;");
    text = text.replaceAll("\\ò", "&ograve;");
    text = text.replaceAll("\\Ô", "&Ocirc;");
    text = text.replaceAll("\\ô", "&ocirc;");
    text = text.replaceAll("\\Ó", "&Oacute;");
    text = text.replaceAll("\\ó", "&oacute;");
    text = text.replaceAll("\\Ñ", "&Ntilde;");
    text = text.replaceAll("\\ñ", "&ntilde;");
    text = text.replaceAll("\\¬", "&not;");
    text = text.replaceAll("\\·", "&middot;");
    text = text.replaceAll("\\µ", "&micro;");
    text = text.replaceAll("\\¯", "&macr;");
    text = text.replaceAll("\\<", "&lt;");
    text = text.replaceAll("\\Ï", "&Iuml;");
    text = text.replaceAll("\\ï", "&iuml;");
    text = text.replaceAll("\\¿", "&iquest;");
    text = text.replaceAll("\\Ì", "&Igrave;");
    text = text.replaceAll("\\ì", "&igrave;");
    text = text.replaceAll("\\¡", "&iexcl;");
    text = text.replaceAll("\\Î", "&Icirc;");
    text = text.replaceAll("\\î", "&icirc;");
    text = text.replaceAll("\\Í", "&Iacute;");
    text = text.replaceAll("\\í", "&iacute;");
    text = text.replaceAll("\\>", "&gt;");
    text = text.replaceAll("\\¾", "&frac34;");
    text = text.replaceAll("\\¼", "&frac14;");
    text = text.replaceAll("\\½", "&frac12;");
    text = text.replaceAll("\\€", "&euro;");
    text = text.replaceAll("\\Ë", "&Euml;");
    text = text.replaceAll("\\ë", "&euml;");
    text = text.replaceAll("\\Ð", "&ETH;");
    text = text.replaceAll("\\ð", "&eth;");
    text = text.replaceAll("\\È", "&Egrave;");
    text = text.replaceAll("\\è", "&egrave;");
    text = text.replaceAll("\\Ê", "&Ecirc;");
    text = text.replaceAll("\\ê", "&ecirc;");
    text = text.replaceAll("\\É", "&Eacute;");
    text = text.replaceAll("\\é", "&eacute;");
    text = text.replaceAll("\\÷", "&divide;");
    text = text.replaceAll("\\°", "&deg;");
    text = text.replaceAll("\\¤", "&curren;");
    text = text.replaceAll("\\©", "&copy;");
    text = text.replaceAll("\\¢", "&cent;");
    text = text.replaceAll("\\¸", "&cedil;");
    text = text.replaceAll("\\Ç", "&Ccedil;");
    text = text.replaceAll("\\ç", "&ccedil;");
    text = text.replaceAll("\\¦", "&brvbar;");
    text = text.replaceAll("\\Ä", "&Auml;");
    text = text.replaceAll("\\ä", "&auml;");
    text = text.replaceAll("\\Ã", "&Atilde;");
    text = text.replaceAll("\\ã", "&atilde;");
    text = text.replaceAll("\\Å", "&Aring;");
    text = text.replaceAll("\\å", "&aring;");
    text = text.replaceAll("\\À", "&Agrave;");
    text = text.replaceAll("\\à", "&agrave;");
    text = text.replaceAll("\\Æ", "&AElig;");
    text = text.replaceAll("\\æ", "&aelig;");
    text = text.replaceAll("\\´", "&acute;");
    text = text.replaceAll("\\Â", "&Acirc;");
    text = text.replaceAll("\\â", "&acirc;");
    text = text.replaceAll("\\Á", "&Aacute;");
    text = text.replaceAll("\\á", "&aacute;");
    return text;
  }

  /**
   * Método utilizado para remover o "escapar" os caracteres especiais em HTML.<Br>
   *
   * @param text
   */
  public static String unescapeHTML(String text) {
    text = text.replaceAll("&yen;", "\\¥");
    text = text.replaceAll("&Yacute;", "\\Ý");
    text = text.replaceAll("&yacute;", "\\ý");
    text = text.replaceAll("&Uuml;", "\\Ü");
    text = text.replaceAll("&uuml;", "\\ü");
    text = text.replaceAll("&uml;", "\\¨");
    text = text.replaceAll("&Ugrave;", "\\Ù");
    text = text.replaceAll("&ugrave;", "\\ù");
    text = text.replaceAll("&Ucirc;", "\\Û");
    text = text.replaceAll("&ucirc;", "\\û");
    text = text.replaceAll("&Uacute;", "\\Ú");
    text = text.replaceAll("&uacute;", "\\ú");
    text = text.replaceAll("&times;", "\\×");
    text = text.replaceAll("&THORN;", "\\Þ");
    text = text.replaceAll("&thorn;", "\\þ");
    text = text.replaceAll("&szlig;", "\\ß");
    text = text.replaceAll("&sup3;", "\\³");
    text = text.replaceAll("&sup2;", "\\²");
    text = text.replaceAll("&sup1;", "\\¹");
    text = text.replaceAll("&sect;", "\\§");
    text = text.replaceAll("&reg;", "\\®");
    text = text.replaceAll("&raquo;", "\\»");
    text = text.replaceAll("\\\"", "&quot;");
    text = text.replaceAll("&pound;", "\\£");
    text = text.replaceAll("&plusmn;", "\\±");
    text = text.replaceAll("&para;", "\\¶");
    text = text.replaceAll("&Ouml;", "\\Ö");
    text = text.replaceAll("&ouml;", "\\ö");
    text = text.replaceAll("&Otilde;", "\\Õ");
    text = text.replaceAll("&otilde;", "\\õ");
    text = text.replaceAll("&Oslash;", "\\Ø");
    text = text.replaceAll("&oslash;", "\\ø");
    text = text.replaceAll("&ordm;", "\\º");
    text = text.replaceAll("&ordf;", "\\ª");
    text = text.replaceAll("&Ograve;", "\\Ò");
    text = text.replaceAll("&ograve;", "\\ò");
    text = text.replaceAll("&Ocirc;", "\\Ô");
    text = text.replaceAll("&ocirc;", "\\ô");
    text = text.replaceAll("&Oacute;", "\\Ó");
    text = text.replaceAll("&oacute;", "\\ó");
    text = text.replaceAll("&Ntilde;", "\\Ñ");
    text = text.replaceAll("&ntilde;", "\\ñ");
    text = text.replaceAll("&not;", "\\¬");
    text = text.replaceAll("&middot;", "\\·");
    text = text.replaceAll("&micro;", "\\µ");
    text = text.replaceAll("&macr;", "\\¯");
    text = text.replaceAll("&lt;", "\\<");
    text = text.replaceAll("&Iuml;", "\\Ï");
    text = text.replaceAll("&iuml;", "\\ï");
    text = text.replaceAll("&iquest;", "\\¿");
    text = text.replaceAll("&Igrave;", "\\Ì");
    text = text.replaceAll("&igrave;", "\\ì");
    text = text.replaceAll("&iexcl;", "\\¡");
    text = text.replaceAll("&Icirc;", "\\Î");
    text = text.replaceAll("&icirc;", "\\î");
    text = text.replaceAll("&Iacute;", "\\Í");
    text = text.replaceAll("&iacute;", "\\í");
    text = text.replaceAll("&gt;", "\\>");
    text = text.replaceAll("&frac34;", "\\¾");
    text = text.replaceAll("&frac14;", "\\¼");
    text = text.replaceAll("&frac12;", "\\½");
    text = text.replaceAll("&euro;", "\\€");
    text = text.replaceAll("&Euml;", "\\Ë");
    text = text.replaceAll("&euml;", "\\ë");
    text = text.replaceAll("&ETH;", "\\Ð");
    text = text.replaceAll("&eth;", "\\ð");
    text = text.replaceAll("&Egrave;", "\\È");
    text = text.replaceAll("&egrave;", "\\è");
    text = text.replaceAll("&Ecirc;", "\\Ê");
    text = text.replaceAll("&ecirc;", "\\ê");
    text = text.replaceAll("&Eacute;", "\\É");
    text = text.replaceAll("&eacute;", "\\é");
    text = text.replaceAll("&divide;", "\\÷");
    text = text.replaceAll("&deg;", "\\°");
    text = text.replaceAll("&curren;", "\\¤");
    text = text.replaceAll("&copy;", "\\©");
    text = text.replaceAll("&cent;", "\\¢");
    text = text.replaceAll("&cedil;", "\\¸");
    text = text.replaceAll("&Ccedil;", "\\Ç");
    text = text.replaceAll("&ccedil;", "\\ç");
    text = text.replaceAll("&brvbar;", "\\¦");
    text = text.replaceAll("&Auml;", "\\Ä");
    text = text.replaceAll("&auml;", "\\ä");
    text = text.replaceAll("&Atilde;", "\\Ã");
    text = text.replaceAll("&atilde;", "\\ã");
    text = text.replaceAll("&Aring;", "\\Å");
    text = text.replaceAll("&aring;", "\\å");
    text = text.replaceAll("&Agrave;", "\\À");
    text = text.replaceAll("&agrave;", "\\à");
    text = text.replaceAll("&AElig;", "\\Æ");
    text = text.replaceAll("&aelig;", "\\æ");
    text = text.replaceAll("&acute;", "\\´");
    text = text.replaceAll("&Acirc;", "\\Â");
    text = text.replaceAll("&acirc;", "\\â");
    text = text.replaceAll("&Aacute;", "\\Á");
    text = text.replaceAll("&aacute;", "\\á");
    text = text.replaceAll("&amp;", "\\&");
    return text;
  }

  /**
   * Este método obtem uma string e a converte em um pattern RegExp para realizar Matches em Strings.<br>
   * O propósito deste método é auxiliar o desenvolvedor a aplicar as mesmas mascaras (do SQL) utilizadas atualmente nos campos de filtros que populam o RFWMO, em uma Expressão Regular que possa ser utilizada para filtrar lista de valores em String, sem consulta no banco de dados.<br>
   * As máscaras são: % - para qualquer caracter em qualquer quantidade e _ para 1 único caracter qualquer.
   *
   * @param value Texto escrito pelo usuário com as mascaras escrita acima
   * @return String com a expressão regular equivalente a ser usada em cada "String".matches() para saber se o valor é equivalente com o filtro do usuário.
   */
  public static String convertFieldMaskToRegExpPattern(String value) {
    if (value != null) {
      // Primeiro fazemos o Quota de toda a expressão para evitar problemas
      value = "\\Q" + value + "\\E";
      // Troca os filtros, lembrando que antes de cada filtros temos que encerrar e recomeçar o "quote" ou a expressão não vai considerar nem estes comandos
      value = value.replaceAll("\\%", "\\\\E.*\\\\Q");
      value = value.replaceAll("\\_", "\\\\E.\\\\Q");
    }
    return value;
  }

  /**
   * Recebe um valor em BigDecimal e o escreve por extenso. Se passado algum valor com mais de 2 casas decimais o valor será arredondado.
   *
   * @param value Valor a ser transformado por extenso.
   * @return String com o texto do valor por extenso em Reais, escrito em Português brasileiro.
   */
  public static String currencyToExtense_BrazilianReal_BrazilianPortuguese(BigDecimal value) {
    // Garante que teremos apenas duas casas decimais
    value = value.setScale(2, RFW.getRoundingMode());

    final StringBuilder buff = new StringBuilder();

    // Separa a parte inteira e os centavos do valor
    BigDecimal[] splitValue = BUNumber.extractIntegerAndFractionPart(value);

    // Recuperar o extenso da parte inteira
    buff.append(valueToExtense_BrazilianPortuguese(splitValue[0]));
    // Anexa a moeda
    if (splitValue[0].compareTo(BigDecimal.ONE) == 0) {
      buff.append(" Real");
    } else {
      buff.append(" Reais");
    }

    // Só processa os centavos se ele existir, se estiver zerado não escreve "zero centavos"
    if (splitValue[1].compareTo(BigDecimal.ZERO) != 0) {
      // Recupera o extendo da parte dos centavos
      buff.append(" e ").append(valueToExtense_BrazilianPortuguese(splitValue[1].multiply(new BigDecimal("100"))));
      // Anexa a palavra centavos se existir
      if (splitValue[1].compareTo(BigDecimal.ONE) == 0) {
        buff.append(" centavo");
      } else {
        buff.append(" centavos");
      }
    }

    return buff.toString();
  }

  /**
   * Escreve um valor por extenso. Apesar de aceitar um BigDecimal por causa do tamanho dos números, os valores fracionários serão simplesmente ignorados.
   *
   * @param value Valor a ser transformado por extenso.
   * @return String com o valor por extenso em Português Brasileiro.
   */
  public static Object valueToExtense_BrazilianPortuguese(BigDecimal value) {
    final StringBuilder buff = new StringBuilder();
    final BigDecimal BIGTHOUSAND = new BigDecimal("1000");

    // Garante que os decimais serão ignorados
    value = value.setScale(0, RoundingMode.FLOOR);

    // Se o valor é zero já retorna logo, não sai tentando calcular e esrever para não escrever errado. Esse é o único número em que "zero" é escrito
    if (value.compareTo(BigDecimal.ZERO) == 0) {
      return "zero";
    }

    // Quebra o valor em cada milhar para ir compondo o valor
    int pow = 0;
    while (value.compareTo(BigDecimal.ZERO) > 0) {
      long hundreds = value.remainder(BIGTHOUSAND).longValue();
      value = value.divide(BIGTHOUSAND, 0, RoundingMode.FLOOR);

      if (hundreds > 0) {
        // Decopõe o número em unidades, dezens e centenas para criar o texto
        int uvalue = (int) (hundreds % 10);
        int dvalue = (int) ((hundreds / 10f) % 10);
        int cvalue = (int) ((hundreds / 100f) % 10);

        String ctext = null;
        String dtext = null;
        String utext = null;

        if (hundreds == 100) {
          ctext = "cem";
        } else {
          if (cvalue == 1) {
            if (dvalue > 0 || uvalue > 0) {
              ctext = "cento";
            } else {
              ctext = "cem";
            }
          } else if (cvalue == 2) {
            ctext = "duzentos";
          } else if (cvalue == 3) {
            ctext = "trezentos";
          } else if (cvalue == 4) {
            ctext = "quatrocentos";
          } else if (cvalue == 5) {
            ctext = "quinhentos";
          } else if (cvalue == 6) {
            ctext = "seiscentos";
          } else if (cvalue == 7) {
            ctext = "setecentos";
          } else if (cvalue == 8) {
            ctext = "oitocentos";
          } else if (cvalue == 9) {
            ctext = "novecentos";
          }

          // Verifica o texto das dezenas
          if (dvalue == 1) {
            if (uvalue == 0) {
              dtext = "dez";
            } else if (uvalue == 1) {
              dtext = "onze";
            } else if (uvalue == 2) {
              dtext = "doze";
            } else if (uvalue == 3) {
              dtext = "treze";
            } else if (uvalue == 4) {
              dtext = "quatorze";
            } else if (uvalue == 5) {
              dtext = "quinze";
            } else if (uvalue == 6) {
              dtext = "dezesseis";
            } else if (uvalue == 7) {
              dtext = "dezessete";
            } else if (uvalue == 8) {
              dtext = "dezoito";
            } else if (uvalue == 9) {
              dtext = "dezenove";
            }
          } else {
            // Se não tem nome específico para o conjunto dezena e unidade, separamos em dezena e unidade
            if (dvalue == 2) {
              dtext = "vinte";
            } else if (dvalue == 3) {
              dtext = "trinta";
            } else if (dvalue == 4) {
              dtext = "quarenta";
            } else if (dvalue == 5) {
              dtext = "cinquenta";
            } else if (dvalue == 6) {
              dtext = "sessenta";
            } else if (dvalue == 7) {
              dtext = "setenta";
            } else if (dvalue == 8) {
              dtext = "oitenta";
            } else if (dvalue == 9) {
              dtext = "noventa";
            }
            // Texto das unidades
            if (uvalue == 1) {
              utext = "um";
            } else if (uvalue == 2) {
              utext = "dois";
            } else if (uvalue == 3) {
              utext = "três";
            } else if (uvalue == 4) {
              utext = "quatro";
            } else if (uvalue == 5) {
              utext = "cinco";
            } else if (uvalue == 6) {
              utext = "seis";
            } else if (uvalue == 7) {
              utext = "sete";
            } else if (uvalue == 8) {
              utext = "oito";
            } else if (uvalue == 9) {
              utext = "nove";
            }
          }
        }

        String text = ctext;
        if (dtext != null) {
          if (text != null) {
            text = text + " e " + dtext;
          } else {
            text = dtext;
          }
        }
        if (utext != null) {
          if (text != null) {
            text = text + " e " + utext;
          } else {
            text = utext;
          }
        }

        // Depois que o número está pronto, verificamos em que casa de milhar estamos para anexar o valor
        switch (pow) {
          case 0:
            // Não há nada, só o número mesmo
            break;
          case 1:
            text += " mil";
            break;
          case 2:
            text += (hundreds == 1 ? " milhão" : " milhões");
            break;
          case 3:
            text += (hundreds == 1 ? " bilhão" : " bilhões");
            break;
          case 4:
            text += (hundreds == 1 ? " trilhão" : " trilhões");
            break;
          case 5:
            text += (hundreds == 1 ? " quatrilhão" : " quatrilhões");
            break;
          case 6:
            text += (hundreds == 1 ? " quintilhão" : " quintilhões");
            break;
          case 7:
            text += (hundreds == 1 ? " sextilhão" : " sextilhões");
            break;
          case 8:
            text += (hundreds == 1 ? " setilhão" : " setilhões");
            break;
          case 9:
            text += (hundreds == 1 ? " octilhão" : " octilhões");
            break;
          case 10:
            text += (hundreds == 1 ? " nonilhão" : " nonilhões");
            break;
          default:
            break;
        }
        if (buff.length() > 0) buff.insert(0, "e ");
        buff.insert(0, text + ' ');
      }
      pow++;
    }
    return buff.toString().trim();
  }

  /**
   * Este método decodifica uma string codificada em base 64.
   *
   * @param encodedContent String codificada
   * @return String decodificada
   */
  public static String decodeBase64(String encodedContent) {
    return new String(Base64.getMimeDecoder().decode(encodedContent));
  }

  /**
   * Este método decodifica uma string codificada em base 64.
   *
   * @param encodedContent String codificada
   * @return String decodificada
   * @throws UnsupportedEncodingException
   */
  public static String decodeBase64(String encodedContent, String charset) throws RFWException {
    try {
      return new String(Base64.getMimeDecoder().decode(encodedContent), charset);
    } catch (UnsupportedEncodingException e) {
      throw new RFWCriticalException("Charset inválido: '" + charset + "'!");
    }
  }

  /**
   * Este método decodifica uma string codificada em base 64.
   *
   * @param encodedContent String codificada
   * @return String decodificada
   */
  public static byte[] decodeBase64ToByte(String encodedContent) {
    return Base64.getMimeDecoder().decode(encodedContent);
  }

  /**
   * Este método codifica uma string em base 64.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase64(String content) {
    return new String(Base64.getMimeEncoder().encodeToString(content.getBytes()));
  }

  /**
   * Este método codifica um array de bytes em base 64.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase64(byte[] content) {
    return new String(Base64.getMimeEncoder().encodeToString(content));
  }

  /**
   * Este método codifica um array de bytes em base 32.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase32(byte[] content) {
    // Estamos usando o Google Guava (já presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra opção seria utilizar o Apache Commons, mas este ainda não está presente no RFWDeprec. No futuro quem sabe ter a própria implementação
    return BaseEncoding.base32().encode(content);
  }

  /**
   * Este método codifica uma String em base 32.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase32(String content) {
    // Estamos usando o Google Guava (já presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra opção seria utilizar o Apache Commons, mas este ainda não está presente no RFWDeprec. No futuro quem sabe ter a própria implementação
    return BaseEncoding.base32().encode(content.getBytes());
  }

  /**
   * Este método decodifica uma String em base 32.
   *
   * @param content String para codificada.
   * @return String codificada
   */
  public static String decodeBase32(String content) {
    // Estamos usando o Google Guava (já presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra opção seria utilizar o Apache Commons, mas este ainda não está presente no RFWDeprec. No futuro quem sabe ter a própria implementação
    return new String(BaseEncoding.base32().decode(content));
  }

  /**
   * Este método decodifica uma String em base 32.
   *
   * @param content String para codificada.
   * @return String codificada
   */
  public static byte[] decodeBase32ToByte(String content) {
    // Estamos usando o Google Guava (já presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra opção seria utilizar o Apache Commons, mas este ainda não está presente no RFWDeprec. No futuro quem sabe ter a própria implementação
    return BaseEncoding.base32().decode(content);
  }

  /**
   * Escreve uma String de trás para frente.
   *
   * @param content - Conteúdo para ser invertido.
   * @return String invertida
   */
  public static String invert(String content) {
    return new StringBuilder(content).reverse().toString();
  }

  /**
   * Extraí o conteúdo de uma String que seja compatrível com uma expressão regular. O conteúdo retornado é o conteúdo dentro do primeiro grupo.<br>
   * Para definir um grupo basta colocar o conteúdo que deseja retornar dentro de parenteses '(' e ')'. O primeiro parenteses encontrado define o primeiro grupo. Para que o parenteses seja parte da expressão, e não definição do grupo utilize duas barras "\\" antes do '(' para "escape" do comando: "\\(".<br>
   *
   *
   * @param text Texto de onde o valor deverá ser extraído.
   * @param regExp Expressão regular que define o bloco a ser recuperado.
   *
   * @return Conteúdo que combina com a expressão regular, extraído do texto principal. NULO caso o conteúdo não seja encontrado.
   * @throws RFWException
   */
  public static String extract(String text, String regExp) throws RFWException {
    return extract(text, regExp, 1);
  }

  /**
   * Extraí o conteúdo de uma String que seja compatrível com uma expressão regular. O conteúdo retornado é o conteúdo dentro do grupo definido em groupID.<br>
   * Para definir um grupo basta colocar o conteúdo que deseja retornar dentro de parenteses '(' e ')'. O primeiro parenteses encontrado define o primeiro grupo. Para que o parenteses seja parte da expressão, e não definição do grupo utilize duas barras "\\" antes do '(' para "escape" do comando: "\\(".<br>
   *
   *
   * @param text Texto de onde o valor deverá ser extraído.
   * @param regExp Expressão regular que define o bloco a ser recuperado. Veja a documentação de {@link Pattern}
   * @param groupID Contador do grupo a ser retornado.
   *
   * @return Conteúdo que combina com a expressão regular, extraído do texto principal. NULO caso o conteúdo não seja encontrado.
   * @throws RFWException
   */
  public static String extract(String text, String regExp, int groupID) throws RFWException {
    Pattern pattern = Pattern.compile(regExp);
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group(groupID);
    }
    return null;
  }

  /**
   * Recupera os Bytes da String no padrão UTF-8. Embora seja uma chamada simples, já trata a exception que é muito inconveniente de ficar no código o tempo todo, sendo que o encoding já está fixo.
   *
   * @param s Texto String que será convertido para bytes.
   * @return array de bytes no padrão UTF-8 com o conteúdo da String
   * @throws RFWException
   */
  public static byte[] getUTF8Bytes(String s) throws RFWException {
    try {
      return s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RFWCriticalException("RFW_ERR_200474", new String[] { "UTF-8" });
    }
  }

  /**
   * Recupera a String a partir dos Bytes no padrão UTF-8. Embora seja uma chamada simples, já trata a exception que é muito inconveniente de ficar no código o tempo todo, sendo que o encoding já está fixo.
   *
   * @param b array de bytes utilizados para montar a String no padrão UTF-8.
   * @return String montada a partir dos dados do Array, utilizando o padrão UTF-8.
   * @throws RFWException
   */
  public static String getUTF8Bytes(byte[] b) throws RFWException {
    try {
      return new String(b, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RFWCriticalException("RFW_ERR_200474", new String[] { "UTF-8" });
    }
  }

  /**
   * Remove da String tudo o que não for dígitos.<br>
   * Método para remover pontuação de valores numéridos como CPF, CNPJ, Representações Numéricas de Códigos de Barras, CEP, etc.<Br>
   *
   * @param value Valor a ter os "não números" estripados
   * @return String apenas com os números/dígitos recebidos.
   * @throws RFWException
   */
  public static String removeNonDigits(String value) {
    if (value == null) return null;
    return value.replaceAll("\\D+", "");
  }

  /**
   * Remove zeros a esquerda no começo da String.<br>
   * Ex: <br>
   * <ul>
   * <li>null -> null</li>
   * <li>"0001234" -> "1234"</li>
   * <li>"00012340000" -> "12340000"</li>
   * <li>" 00012340000" -> " 00012340000" (Note que começa com um espaço e não com zeros)</li>
   * <li>"000000000000000000" -> "" (Retorna String vazia e não null)</li>
   * </ul>
   *
   * @param string texto a ter os zeros do começo da String removidos.
   * @return Mesma String sem os zeros do começo da String.
   */
  public static String removeLeadingZeros(String string) {
    if (string == null) return null;
    return string.replaceFirst("^0*", "");
  }

  /**
   * Método auxiliar para separar os campos de uma linha de arquivo CSV (Comma Separated Value), considerando as Aspas e Escaped Characteres.<br>
   * <br>
   *
   * No arquivo CSV, para "escapar as vírgulas" o campo deve ser envolto por: " (aspas). Por exemplo:
   * <li>"Meu CSV tem , no meio do texto", campo 2, etc. -> 0=Meu CSV tem , no meio do texto; 1=campo2; 2=etc.<br>
   * <br>
   * para escapar uma aspas dentro de um campo, é necessário duplica-la:
   * <li>"Meu CSV tem "","" no meio do texto", campo 2, etc. -> 0=Meu CSV tem "," no meio do texto; 1=campo2; 2=etc.<br>
   * <br>
   *
   * @param line linha do arquivo para se realizar o parse.
   * @return
   * @throws RFWException
   */
  public static String[] parseCSVLine(String line) throws RFWException {
    return parseCSVLine(line, ',', '"');
  }

  /**
   * Método auxiliar para separar os campos de uma linha de arquivo CSV (Comma Separated Value), considerando as Aspas e Escaped Characteres.<br>
   * <br>
   *
   * No arquivo CSV, para "escapar as vírgulas" o campo deve ser envolto por: " (aspas). Por exemplo:
   * <li>"Meu CSV tem , no meio do texto", campo 2, etc. -> 0=Meu CSV tem , no meio do texto; 1=campo2; 2=etc.<br>
   * <br>
   * para escapar uma aspas dentro de um campo, é necessário duplica-la:
   * <li>"Meu CSV tem "","" no meio do texto", campo 2, etc. -> 0=Meu CSV tem "," no meio do texto; 1=campo2; 2=etc.<br>
   * <br>
   *
   * @param line linha do arquivo para se realizar o parse.
   * @param separators Separados utilizado entre os campos. Normalmente ',' ou '|', ou '\\t'
   * @param customQuote Define as aspas sendo utilizadas no arquivo. Normalmente '"'.
   * @return
   * @throws RFWException
   */
  public static String[] parseCSVLine(String line, char separators, char customQuote) throws RFWException {
    LinkedList<String> result = new LinkedList<>();

    if (line == null || line.isEmpty()) {
      return new String[0];
    }

    StringBuffer curVal = new StringBuffer();
    boolean inQuotes = false;
    boolean startCollectChar = false;
    boolean doubleQuotesInColumn = false;

    char[] chars = line.toCharArray();

    for (char ch : chars) {
      if (inQuotes) {
        startCollectChar = true;
        if (ch == customQuote) {
          inQuotes = false;
          doubleQuotesInColumn = false;
        } else {
          if (ch == '"') {
            if (!doubleQuotesInColumn) {
              curVal.append(ch);
              doubleQuotesInColumn = true;
            }
          } else {
            curVal.append(ch);
          }
        }
      } else {
        if (ch == customQuote) {
          inQuotes = true;
          if (chars[0] != '"' && customQuote == '\"') {
            curVal.append('"');
          }
          if (startCollectChar) {
            curVal.append('"');
          }
        } else if (ch == separators) {
          result.add(curVal.toString());
          curVal = new StringBuffer();
          startCollectChar = false;
        } else if (ch == '\r') {
          // ignora \r
          continue;
        } else if (ch == '\n') {
          // Finaliza a linha no \n!
          break;
        } else {
          curVal.append(ch);
        }
      }

    }

    result.add(curVal.toString());

    return result.toArray(new String[0]);
  }

  /**
   * Concatena Strings colocando ", " entre elas caso a primeira String seja diferente de null e de "".
   *
   * @param string1 Primeiro valor a ser concatenado
   * @param string2 Segundo valor a ser concatenado
   * @return Nunca retorna nulo, retorna o conteúdo de String1 e String2 separados por virgula caso ambos tenham valor válido. Sendo algum nulo ou vazio, retorna apena o valor do outro. Sendo ambos nulos ou vazios, retorna "".
   */
  public static String appendWithComma(String string1, String string2) {
    string1 = PreProcess.processStringToNull(string1);
    string2 = PreProcess.processStringToNull(string2);

    if (string1 == null && string2 == null) {
      return "";
    }

    if (string1 == null) return string2;
    if (string2 == null) return string1;

    return string1 + ", " + string2;
  }

  public static void validateEqualsString(String expected, String actual) throws RFWException {
    int i = 0;
    for (; i < expected.length(); i++) {
      if (i >= actual.length()) {
        throw new RFWValidationException("O valor do texto 'Atual' chegou ao fim na posição '${0}' quando era esperado o caracter '${1}'.", new String[] { "" + i, "" + expected.charAt(i) });
      }
      if (expected.charAt(i) != actual.charAt(i)) {
        String part1 = expected.substring(Math.max(0, i - 5), Math.min(expected.length(), i + 5));
        String part2 = actual.substring(Math.max(0, i - 5), Math.min(actual.length(), i + 5));
        throw new RFWValidationException("O valor do texto está diferente na posição '${0}'. Esperavamos '${1}' e encontramos '${2}'.", new String[] { "" + i, part1, part2 });
      }
    }
    if (actual.length() > expected.length()) {
      String part2 = actual.substring(i, Math.min(actual.length(), i + 10));
      throw new RFWValidationException("O texto esperado chegou ao fim na posição '${0}' mas o valor atual continua com o conteúdo '${1}...'.", new String[] { "" + i, part2 });
    }
  }

  /**
   * Realiza o substring na melhor maneira possível sem lançar qualquer exception ou retornar nulo.
   *
   * @param value Valor String para ser cortado. Se nulo, retorna "".
   * @param startIndex Posição inicial para iniciar o corte. Se menor que 0, será ajutada para 0. Se maior que o tamanho da String força o retorno de "".
   * @param finalIndex Posição final para finalizar o corte. Se maior que o tamanho da String, será ajutado para o tamanho máximo da String. Se finalIndex <= startIndex retorna "".
   * @return Corte possível conforme parâmetros definidos.
   */
  public static String subString(String value, int startIndex, int finalIndex) {
    if (value == null) return "";
    if (startIndex < 0) startIndex = 0;
    if (startIndex > value.length()) return "";
    if (finalIndex > value.length()) finalIndex = value.length();
    if (finalIndex - startIndex <= 0) return "";
    return value.substring(startIndex, finalIndex);
  }

  /**
   * Procura a extrai uma data no formato dd/MM/yyyy dentro de uma String<br>
   * <b>Atenção:</b> Este método não valida a data, só procura uma ocorrência que esteja no formado de data e a retorna.<br>
   * No momento o método verifica se a quantidade de dias é consizentes com o mês passado (meses de 30/31 e 29 dias). Valida se o ano começa com 19xx ou 20xx quando houver a necessidade de trabalhar com os anos 21xx este método precisa de atualização.
   *
   * @param text Conteúdo de Texto para procurar
   * @param groupID valor começando em 1 para encontrar a primeira ocorrência, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso não.
   */
  public static String extractDateDDMMYYYY(String text, int groupID) {
    try {
      // "(?:(?:0[1-9]|1[0-9]|2[0-9]|3[0-1])/(?:01|03|05|07|08|10|12)/(19|20)[0-9]{2})"; // Meses com 31 dias
      // "(?:(?:0[1-9]|1[0-9]|2[0-9]|30)/(?:04|06|09|11)/(19|20)[0-9]{2})"; // Meses com 30 dias
      // "(?:(?:0[1-9]|1[0-9]|2[0-9])/(?:02)/(19|20)[0-9]{2})"; // Fevereiro com 29 dias
      String regExp = "((?:(?:0[1-9]|1[0-9]|2[0-9]|3[0-1])/(?:01|03|05|07|08|10|12)/(19|20)[0-9]{2})|(?:(?:0[1-9]|1[0-9]|2[0-9]|30)/(?:04|06|09|11)/(19|20)[0-9]{2})|(?:(?:0[1-9]|1[0-9]|2[0-9])/(?:02)/(19|20)[0-9]{2}))";
      return BUString.extract(text, regExp, groupID);
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }

  /**
   * Procura a extrai uma data no formato MM/yyyy dentro de uma String<br>
   * <b>Atenção:</b> Este método não valida a data, só procura uma ocorrência que esteja no formado de data e a retorna.<br>
   * Valida se o ano começa com 19xx ou 20xx quando houver a necessidade de trabalhar com os anos 21xx este método precisa de atualização.
   *
   * @param text Conteúdo de Texto para procurar
   * @param groupID valor começando em 1 para encontrar a primeira ocorrência, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso não.
   */
  public static String extractDateMMYYYY(String text, int groupID) {
    try {
      String regExp = "(?:^|[^/])((?:0[0-9]|1[0-2])/(?:(19|20)[0-9]{2}))(?:$|[^/])";
      return BUString.extract(text, regExp, groupID);
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }

  /**
   * Procura a extrai uma hora no formato hh:mm:ss dentro de uma String<br>
   * <b>Atenção:</b> Este método não valida o horário, só procura uma ocorrência que esteja no formado de data e a retorna.<br>
   * No momento o método verifica se o formato é condizente com de 0 à 23h, de 0 à 59min, e 0 à 59s.
   *
   * @param text Conteúdo de Texto para procurar
   * @param groupID valor começando em 1 para encontrar a primeira ocorrência, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso não.
   */
  public static String extractTimeHHMMSSS(String text, int groupID) {
    try {
      String regExp = "((?:[01][0-9]|2[0-3])\\:(?:[0-5][0-9])\\:(?:[0-5][0-9]))";
      // String regExp = "((?:(?:0[1-9]|1[0-9]|2[0-9]|3[0-1])/(?:01|03|05|07|08|10|12)/(19|20)[0-9]{2})|(?:(?:0[1-9]|1[0-9]|2[0-9]|30)/(?:04|06|09|11)/(19|20)[0-9]{2})|(?:(?:0[1-9]|1[0-9]|2[0-9])/(?:02)/(19|20)[0-9]{2}))";
      return BUString.extract(text, regExp, groupID);
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }

  /**
   * Procura uma sequência de números com uma quantidade certa de dígitos.<br>
   * Não extrai uma sequência de números de dentro de uma sequência maior (checa se está no começo/fim do conteúdo ou cercado por espaços)
   *
   * @param text Conteúdo de Texto para Procurar
   * @param digitsCount Total de dígitos esperados na sequência
   * @param groupID valor começando em 1 para encontrar a primeira ocorrência, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso não.
   */
  public static String extractCodes(String text, int digitsCount, int groupID) {
    try {
      String regExp = "(?:^|[ ])([0-9]{4})(?:$|[ ])";
      return BUString.extract(text, regExp, groupID);
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }

  /**
   * Procura uma String em um formato de CNPJ (todo pontuado, pacialmente ou mesmo só uma sequência dom os 14 dígitos) no texto passado.<Br>
   * <b>Atenção:</b> Este método não valida se o conteúdo é válido, apenas procura o padrão que possa ser considerado um CNPJ. Para validar utilize {@link BUDocValidation#validateCNPJ(String)}.
   *
   * @param text Texto a ser procurado. Número da ocorrência.
   * @param groupID valor começando em 1 para encontrar a primeira ocorrência, e sequencialmente.
   * @return
   */
  public static String extracCNPJ(String text, int groupID) {
    try {
      String regExp = "(?:^|[^\\d])([0-9]{2}\\.?[0-9]{3}\\.?[0-9]{3}/?[0-9]{4}\\-?[0-9]{2})(?:$|[^\\d])";
      return BUString.extract(text, regExp, groupID);
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }

  /**
   * Extrai valores numéricos do texto. Aceita que os milhares dos números estejam separados por pontos e os demais com virgula.<br>
   * Este método não considera valores se não tiver a virgula.<br>
   *
   *
   * @param text
   * @param groupID
   * @return
   */
  public static String extractDecimalValues(String text, int groupID, boolean useCommaToDecimal) {
    try {
      String decimals = null;
      String thousands = null;
      if (useCommaToDecimal) {
        decimals = ",";
        thousands = "\\.";
      } else {
        decimals = "\\.";
        thousands = ",";
      }
      String regExp = "(?:^|[^0-9" + decimals + "])([0-9]{1,3}(?:[" + thousands + "]?[0-9]{3})*[" + decimals + "][0-9]+)(?:$|[^0-9" + decimals + "])";
      String t = BUString.extract(text, regExp, groupID);
      return t;
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }

  /**
   * Extrai de sequência de texto que possa ser uma representação numérica de um código de barras de contas de serviço/consumo.<br>
   * <b>Atenção:</b> Este método não valida a código, só procura uma ocorrência que esteja no formado e a retorna. A validação pode ser feita com {@link BUDocValidation#isServiceNumericCodeValid(String)}<br>
   *
   * @param text
   * @param groupID
   * @return
   */
  public static String extractServiceNumericCode(String text, int groupID) {
    try {
      String regExp = "(?:^|[^0-9])((?:[0-9]{11}[ \\.-]*[0-9][ \\.-]*){4})(?:$|[^0-9])";
      return BUString.extract(text, regExp, groupID);
    } catch (RFWException e) {
      RFWLogger.logException(e);
    }
    return null;
  }
}
