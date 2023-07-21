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
 * Description: Classe com m�todos �teis para tratamentos e manipula��o de String.<br>
 *
 * @author Rodrigo Leit�o
 * @since 1.0.0 (AGO / 2007)
 * @version 4.1.0 (23/06/2011) - rodrigogml - Nome alterado de StringUtils, para ficar no padr�o do sistema.
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
  public static final char[] allchars = new char[] { '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�',
      '�', '�' };

  /*
   * Referencia os m�todos de normaliza��o de string para cada um dos jdks, para evitar de se fazer diversas reflex�es em cada vez que se usa o m�todo de remover acentos.
   */
  private static Method normalizerJDK5 = null; // Salva o m�todo que ser� usado na normaliza��o da string no JDK5
  private static Method normalizerJDK6 = null;// Salva o m�todo que ser� usado na normaliza��o da string no JDK6
  private static Object normalizerJDK6form = null; // Salva o form necess�rio para o normalizer do jdk6
  private static Boolean unknownormalizer = null; // Salva se o m�todo de normaliza��o � desconhecido, null n�o procurado ainda, true desconhecido (usa modo manual), false conhecido

  /**
   * Remove a acentua��o de um texto passado. Incluindo '�' por 'c', mai�sculas e min�scas (preservando a captaliza��o da letra).
   *
   * @param text String que ter� seus acentos removidos.
   * @return String sem caracteres acentuados, trocados pelos seus correspondentes.
   */
  public static String removeAccents(String text) {
    // Verifica se conhece o m�todo de normaliza��o
    if (unknownormalizer == null) { // Se ainda n�o foi procurado, procura
      try {
        // Tenta Compatibilidade com JDK 6 - Evita o Import para evitar erros de compila��o e execu��o
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
          // Compatibilidade com JDK 5 - Evita o Import para evitar erros de compila��o e execu��o
          Class<?> normalizerC = Class.forName("sun.text.Normalizer");
          normalizerJDK5 = normalizerC.getMethod("decompose", new Class[] { String.class, boolean.class, int.class });
          unknownormalizer = Boolean.FALSE;
          return ((String) normalizerJDK5.invoke(null, new Object[] { text, false, 0 })).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception ex2) {
          // Salva como modo manual, normalizador desconhecido
          unknownormalizer = Boolean.TRUE;
          text = text.replaceAll("[�����]", "a");
          text = text.replaceAll("[����]", "e");
          text = text.replaceAll("[����]", "i");
          text = text.replaceAll("[�����]", "o");
          text = text.replaceAll("[����]", "u");
          text = text.replaceAll("[�]", "c");
          text = text.replaceAll("[�]", "n");
          text = text.replaceAll("[�����]", "A");
          text = text.replaceAll("[����]", "E");
          text = text.replaceAll("[����]", "I");
          text = text.replaceAll("[�����]", "O");
          text = text.replaceAll("[����]", "U");
          text = text.replaceAll("[�]", "N");
          return text;
        }
      }
    } else if (unknownormalizer) {
      text = text.replaceAll("[�����]", "a");
      text = text.replaceAll("[����]", "e");
      text = text.replaceAll("[����]", "i");
      text = text.replaceAll("[�����]", "o");
      text = text.replaceAll("[����]", "u");
      text = text.replaceAll("[�]", "c");
      text = text.replaceAll("[�]", "n");
      text = text.replaceAll("[�����]", "A");
      text = text.replaceAll("[����]", "E");
      text = text.replaceAll("[����]", "I");
      text = text.replaceAll("[�����]", "O");
      text = text.replaceAll("[����]", "U");
      text = text.replaceAll("[�]", "N");
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
   * Remove os caracteres inv�lidospara UTF-8. Trocando letras acentuadas por suas correspondentes sem acentos, e outros caracteres inv�lidos pelo caractere '?'.
   *
   * @param text Texto a ser processado.
   * @return Texto processado.
   * @throws RFWException Lan�ado caso ocorra alguma falha em processar o texto.
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
   * Remove todos os caracteres que n�o comp�e os primeiros 128 caracteres da tabela UTF-8 pelo texto passado.
   *
   * @param text Texto a ser tratado.
   * @return Texto sem os caracteres fora dos primeiros caracteres da tabela UTF-8.
   * @throws RFWException
   */
  public static String removeNonUTF8BaseCaracters(String text) throws RFWException {
    return replaceNonUTF8BaseCaracters(text, "");
  }

  /**
   * Substitui todos os caracteres que n�o comp�e os primeiros 128 caracteres da tabela UTF-8 pelo texto passado.
   *
   * @param text Texto a ser tratado.
   * @param replacement Valor que substituir� os caracteres removidos
   * @return Texto tratado.
   * @throws RFWException
   */
  public static String replaceNonUTF8BaseCaracters(String text, String replacement) throws RFWException {
    return text.replaceAll("[^\\u0000-\\u007E]", replacement);
  }

  /**
   * Substitui todas as ocorrencias de 'oldvalue' por 'newvalue' no texto de 'value'. No entanto este m�todo diferencia mai�sculas, min�sculas, acentos, etc.
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuir� oldvalue.
   * @return
   */
  public static String replaceAll(String text, String oldvalue, String newvalue) {
    return replaceAll(text, oldvalue, newvalue, Boolean.TRUE, Boolean.TRUE);
  }

  /**
   * Substitui o texto recursivamente at� que o texto n�o sofra mais altera��es, isto �, o texto ser� procurado do inicio ao fim pela substitui��o quantas vezes for necess�rias at� que seja feita uma busca completa e nada seja encontrado.<br>
   * <b>ATEN��O:</b> Pode gerar StackOverflow facilmente se substituimos um texto por outro que cont�m o valor sendo procurado!<Br>
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuir� oldvalue.
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
   * Substitui o texto recursivamente at� que o texto n�o sofra mais altera��es, isto �, o texto ser� procurado do inicio ao fim pela substitui��o quantas vezes for necess�rias at� que seja feita uma busca completa e nada seja encontrado.<br>
   * <b>ATEN��O:</b> Pode gerar StackOverflow facilmente se substituimos um texto por outro que cont�m o valor sendo procurado!<Br>
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuir� oldvalue.
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
   * De acordo com as defini��es passadas, ele ignora acentos e case de letras.
   *
   * @param text texto a ser manipulado
   * @param oldvalue valor a ser procurado e substituido.
   * @param newvalue valor que substiuir� oldvalue.
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
   * Converte o objeto que cont�m o valor da enumera��o (a pr�pria enumera��o) em "chave".<br>
   * O mesmo que o m�todo <code>getEnumKey()</code>, exceto pelo acrescimo do valor da enumera��o ao final.
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
   * Gera uma String qualquer no padr�o: [A-Za-z0-9]{length}
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
   * Gera uma String qualquer no padr�o: [0-9]{length}
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
   * Cria uma String com n repeti��es de uma determinada cadeira de caracteres (ou caracter simples).
   *
   * @param repeats N�mero de repeti��es na String final.
   * @param base Conte�do a ser repetido na String.
   * @return String montada conforme as defini��es. Com tamanho total = repeats * base.length();
   */
  public static String genString(int repeats, String base) {
    return new String(new char[repeats]).replaceAll("\0", base);
  }

  /**
   * Este m�todo recebe um valor string e quebra em linhas com o tamanho m�ximo definido. Este m�todo quebrar� as linhas somente nos espa�os em branco entre as palavras, n�o quebra as palavras no meio.
   *
   * @param content Conte�do a ser quebrado em linhas
   * @param maxlength tamanho m�ximo de cada linha.
   * @return Array de String com todas as linhas criadas.
   */
  public static String[] breakLineInBlankSpaces(String content, int maxlength) {
    final LinkedList<String> lines = new LinkedList<>();

    String[] blines = content.split("\\ ");
    final StringBuilder b = new StringBuilder(maxlength);
    for (int i = 0; i < blines.length; i++) {
      // Verifica se ainda cabe na mesmoa linha
      if (b.length() + blines[i].length() + 1 <= maxlength) { // O +1 refere-se ao espa�o que ser� adicionado entre o conte�do do buffer e a nova palavra
        b.append(" ").append(blines[i]);
      } else {
        lines.add(b.toString());
        b.delete(0, b.length());
        b.append(blines[i]);
      }
    }
    // Ao acabar, verificamose se temos conte�do no buff e passamos e acrescentamos � lista, caso contr�rio perdemos a �ltima linha
    if (b.length() > 0) lines.add(b.toString());
    String[] a = new String[lines.size()];
    return lines.toArray(a);
  }

  /**
   * M�todo utilizado para converter um byte array de base 64 em uma String para Hexadecimal.<br>
   * Este m�todo � utilizado por exemplo para receber o bytearray do campo DigestValue do XML da NFe/NFCe (cuja base � 64), e converte para uma representa��o Hexadecimal. Essa representa��o Hexa � utilizada na gera��o da URL no QRCode da NFCe.
   *
   * @param bytearray
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHexFromBase64(byte[] bytearray) throws RFWException {
    return BUString.toHex(Base64.getEncoder().encodeToString(bytearray));
  }

  /**
   * M�todo utilizado extrair o byte array de base 64 a partir de uma string que represente um valor em hexa.<br>
   * Faz o procedimento contr�rio ao {@link #toHexFromBase64(byte[])}<br>
   *
   * @param bytearray
   * @param hexstring
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static byte[] fromHexToByteArrayBase64(String hexstring) throws RFWException {
    return Base64.getDecoder().decode(BUString.fromHexToByteArray(hexstring));
  }

  /**
   * M�todo utilizado para converter uma String para Hexadecimal.<br>
   * Este m�todo utiliza o CharSet Padr�o do ambiente.
   *
   * @param value Valor a ser convertido
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHex(String value) throws RFWException {
    return toHex(value.getBytes(/* YOUR_CHARSET? */));
  }

  /**
   * M�todo utilizado para converter uma String para Hexadecimal.<br>
   * Este m�todo permite identificar o charset usado para decodificar a String.
   *
   * @param value Valor a ser convertido
   * @param charset Charset para decodifica��o da String
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHex(String value, Charset charset) throws RFWException {
    return toHex(value.getBytes(charset));
  }

  /**
   * M�todo utilizado para converter um array de bytes para Hexadecimal.<br>
   *
   * @param bytes cadeia de bytes a ser convertido para uma String representando o valor Hexadecimal
   * @return String com o valor em HexaDecimal com as letras em lowercase.
   */
  public static String toHex(byte[] bytes) throws RFWException {
    return String.format("%040x", new BigInteger(1, bytes));
  }

  /**
   * Este m�todo recebe uma string representando valores em hexa e retorna os valores em um array de bytes.
   *
   * @param hexstring String representando um valor hexa
   * @return array de bytes com os mesmos valores representados em hexa na string.
   * @throws RFWException
   */
  public static byte[] fromHexToByteArray(String hexstring) throws RFWException {
    // Valida a String recebida se s� tem caracteres em Hexa
    if (!hexstring.matches("[0-9A-Fa-f]*")) {
      throw new RFWValidationException("RFW_ERR_200362");
    }
    return new BigInteger(hexstring, 16).toByteArray();
  }

  /**
   * M�todo utilizado para converter uma string de valor hexadecimal para uma String. Utiliza os bytes dos valores hexa decimal para converter em String utilizando o charset padr�o do sistema.
   *
   * @param hexstring String com valores em hexa
   * @return String montada usando os bytes do valor hexa com o charset padr�o do sistema.
   * @throws RFWException
   */
  public static String fromHexToString(String hexstring) throws RFWException {
    // Valida a String recebida se s� tem caracteres em Hexa
    if (!hexstring.matches("[0-9A-Fa-f]*")) {
      throw new RFWValidationException("RFW_ERR_200362");
    }
    return new String(new BigInteger(hexstring, 16).toByteArray());
  }

  /**
   * Calcula a Hash SHA1 de uma String.
   *
   * @param value Valor a ter a Hash calculada.
   * @return Valor em Hexa calculado com o algor�timo de SHA1.
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
   * @return Valor em Hexa calculado com o algor�timo de SHA1.
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
   * Este m�todo recebe um valor inteiro e o converte para letras no padr�o de colunas do Excel.<br>
   * <b>Por exemplo 1 -> A, 2 -> B, ..., 26 -> Z, 27 -> AA, 28 -> AB, ...</b>
   *
   * @param value valor num�rico a ser convertido.
   * @return Letras equivalendo o valor convertido.
   */
  public static String convertToExcelColumnLetters(long value) {
    final StringBuilder buff = new StringBuilder();
    value = value - 1; // Corrige valor de value para come�ar em 0, j� que a defini��o do m�todo diz que o primeiro valor � 1 e n�o 0.
    while (value > -1) {
      int mod = (int) (value % 26);
      // Converte o valor para o char adequado
      buff.insert(0, Character.toChars(mod + 65)[0]);
      value = value / 26 - 1;
    }
    return buff.toString();
  }

  /**
   * Este m�todo realiza o "escape" dos carateres que atrapalham o parser do do XML. N�o faz escape de todos os caracteres acentuados e fora da tabela padr�o, simplesmente faz escape dos seguintes caracteres que atrapalham a estrutura do XML:<br>
   * <li>< - &amp;lt;</li>
   * <li>> - &amp;gt;</li>
   * <li>& - &amp;amp;</li>
   * <li>� - &amp;quot;</li>
   * <li>� - &amp;#39;</li>
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
   * M�todo utilizado para "escapar" os caracteres especiais em HTML.<Br>
   *
   * @param text
   */
  public static String escapeHTML(String text) {
    text = text.replaceAll("\\&", "&amp;");
    text = text.replaceAll("\\�", "&yen;");
    text = text.replaceAll("\\�", "&Yacute;");
    text = text.replaceAll("\\�", "&yacute;");
    text = text.replaceAll("\\�", "&Uuml;");
    text = text.replaceAll("\\�", "&uuml;");
    text = text.replaceAll("\\�", "&uml;");
    text = text.replaceAll("\\�", "&Ugrave;");
    text = text.replaceAll("\\�", "&ugrave;");
    text = text.replaceAll("\\�", "&Ucirc;");
    text = text.replaceAll("\\�", "&ucirc;");
    text = text.replaceAll("\\�", "&Uacute;");
    text = text.replaceAll("\\�", "&uacute;");
    text = text.replaceAll("\\�", "&times;");
    text = text.replaceAll("\\�", "&THORN;");
    text = text.replaceAll("\\�", "&thorn;");
    text = text.replaceAll("\\�", "&szlig;");
    text = text.replaceAll("\\�", "&sup3;");
    text = text.replaceAll("\\�", "&sup2;");
    text = text.replaceAll("\\�", "&sup1;");
    text = text.replaceAll("\\�", "&sect;");
    text = text.replaceAll("\\�", "&reg;");
    text = text.replaceAll("\\�", "&raquo;");
    text = text.replaceAll("\\\"", "&quot;");
    text = text.replaceAll("\\�", "&pound;");
    text = text.replaceAll("\\�", "&plusmn;");
    text = text.replaceAll("\\�", "&para;");
    text = text.replaceAll("\\�", "&Ouml;");
    text = text.replaceAll("\\�", "&ouml;");
    text = text.replaceAll("\\�", "&Otilde;");
    text = text.replaceAll("\\�", "&otilde;");
    text = text.replaceAll("\\�", "&Oslash;");
    text = text.replaceAll("\\�", "&oslash;");
    text = text.replaceAll("\\�", "&ordm;");
    text = text.replaceAll("\\�", "&ordf;");
    text = text.replaceAll("\\�", "&Ograve;");
    text = text.replaceAll("\\�", "&ograve;");
    text = text.replaceAll("\\�", "&Ocirc;");
    text = text.replaceAll("\\�", "&ocirc;");
    text = text.replaceAll("\\�", "&Oacute;");
    text = text.replaceAll("\\�", "&oacute;");
    text = text.replaceAll("\\�", "&Ntilde;");
    text = text.replaceAll("\\�", "&ntilde;");
    text = text.replaceAll("\\�", "&not;");
    text = text.replaceAll("\\�", "&middot;");
    text = text.replaceAll("\\�", "&micro;");
    text = text.replaceAll("\\�", "&macr;");
    text = text.replaceAll("\\<", "&lt;");
    text = text.replaceAll("\\�", "&Iuml;");
    text = text.replaceAll("\\�", "&iuml;");
    text = text.replaceAll("\\�", "&iquest;");
    text = text.replaceAll("\\�", "&Igrave;");
    text = text.replaceAll("\\�", "&igrave;");
    text = text.replaceAll("\\�", "&iexcl;");
    text = text.replaceAll("\\�", "&Icirc;");
    text = text.replaceAll("\\�", "&icirc;");
    text = text.replaceAll("\\�", "&Iacute;");
    text = text.replaceAll("\\�", "&iacute;");
    text = text.replaceAll("\\>", "&gt;");
    text = text.replaceAll("\\�", "&frac34;");
    text = text.replaceAll("\\�", "&frac14;");
    text = text.replaceAll("\\�", "&frac12;");
    text = text.replaceAll("\\�", "&euro;");
    text = text.replaceAll("\\�", "&Euml;");
    text = text.replaceAll("\\�", "&euml;");
    text = text.replaceAll("\\�", "&ETH;");
    text = text.replaceAll("\\�", "&eth;");
    text = text.replaceAll("\\�", "&Egrave;");
    text = text.replaceAll("\\�", "&egrave;");
    text = text.replaceAll("\\�", "&Ecirc;");
    text = text.replaceAll("\\�", "&ecirc;");
    text = text.replaceAll("\\�", "&Eacute;");
    text = text.replaceAll("\\�", "&eacute;");
    text = text.replaceAll("\\�", "&divide;");
    text = text.replaceAll("\\�", "&deg;");
    text = text.replaceAll("\\�", "&curren;");
    text = text.replaceAll("\\�", "&copy;");
    text = text.replaceAll("\\�", "&cent;");
    text = text.replaceAll("\\�", "&cedil;");
    text = text.replaceAll("\\�", "&Ccedil;");
    text = text.replaceAll("\\�", "&ccedil;");
    text = text.replaceAll("\\�", "&brvbar;");
    text = text.replaceAll("\\�", "&Auml;");
    text = text.replaceAll("\\�", "&auml;");
    text = text.replaceAll("\\�", "&Atilde;");
    text = text.replaceAll("\\�", "&atilde;");
    text = text.replaceAll("\\�", "&Aring;");
    text = text.replaceAll("\\�", "&aring;");
    text = text.replaceAll("\\�", "&Agrave;");
    text = text.replaceAll("\\�", "&agrave;");
    text = text.replaceAll("\\�", "&AElig;");
    text = text.replaceAll("\\�", "&aelig;");
    text = text.replaceAll("\\�", "&acute;");
    text = text.replaceAll("\\�", "&Acirc;");
    text = text.replaceAll("\\�", "&acirc;");
    text = text.replaceAll("\\�", "&Aacute;");
    text = text.replaceAll("\\�", "&aacute;");
    return text;
  }

  /**
   * M�todo utilizado para remover o "escapar" os caracteres especiais em HTML.<Br>
   *
   * @param text
   */
  public static String unescapeHTML(String text) {
    text = text.replaceAll("&yen;", "\\�");
    text = text.replaceAll("&Yacute;", "\\�");
    text = text.replaceAll("&yacute;", "\\�");
    text = text.replaceAll("&Uuml;", "\\�");
    text = text.replaceAll("&uuml;", "\\�");
    text = text.replaceAll("&uml;", "\\�");
    text = text.replaceAll("&Ugrave;", "\\�");
    text = text.replaceAll("&ugrave;", "\\�");
    text = text.replaceAll("&Ucirc;", "\\�");
    text = text.replaceAll("&ucirc;", "\\�");
    text = text.replaceAll("&Uacute;", "\\�");
    text = text.replaceAll("&uacute;", "\\�");
    text = text.replaceAll("&times;", "\\�");
    text = text.replaceAll("&THORN;", "\\�");
    text = text.replaceAll("&thorn;", "\\�");
    text = text.replaceAll("&szlig;", "\\�");
    text = text.replaceAll("&sup3;", "\\�");
    text = text.replaceAll("&sup2;", "\\�");
    text = text.replaceAll("&sup1;", "\\�");
    text = text.replaceAll("&sect;", "\\�");
    text = text.replaceAll("&reg;", "\\�");
    text = text.replaceAll("&raquo;", "\\�");
    text = text.replaceAll("\\\"", "&quot;");
    text = text.replaceAll("&pound;", "\\�");
    text = text.replaceAll("&plusmn;", "\\�");
    text = text.replaceAll("&para;", "\\�");
    text = text.replaceAll("&Ouml;", "\\�");
    text = text.replaceAll("&ouml;", "\\�");
    text = text.replaceAll("&Otilde;", "\\�");
    text = text.replaceAll("&otilde;", "\\�");
    text = text.replaceAll("&Oslash;", "\\�");
    text = text.replaceAll("&oslash;", "\\�");
    text = text.replaceAll("&ordm;", "\\�");
    text = text.replaceAll("&ordf;", "\\�");
    text = text.replaceAll("&Ograve;", "\\�");
    text = text.replaceAll("&ograve;", "\\�");
    text = text.replaceAll("&Ocirc;", "\\�");
    text = text.replaceAll("&ocirc;", "\\�");
    text = text.replaceAll("&Oacute;", "\\�");
    text = text.replaceAll("&oacute;", "\\�");
    text = text.replaceAll("&Ntilde;", "\\�");
    text = text.replaceAll("&ntilde;", "\\�");
    text = text.replaceAll("&not;", "\\�");
    text = text.replaceAll("&middot;", "\\�");
    text = text.replaceAll("&micro;", "\\�");
    text = text.replaceAll("&macr;", "\\�");
    text = text.replaceAll("&lt;", "\\<");
    text = text.replaceAll("&Iuml;", "\\�");
    text = text.replaceAll("&iuml;", "\\�");
    text = text.replaceAll("&iquest;", "\\�");
    text = text.replaceAll("&Igrave;", "\\�");
    text = text.replaceAll("&igrave;", "\\�");
    text = text.replaceAll("&iexcl;", "\\�");
    text = text.replaceAll("&Icirc;", "\\�");
    text = text.replaceAll("&icirc;", "\\�");
    text = text.replaceAll("&Iacute;", "\\�");
    text = text.replaceAll("&iacute;", "\\�");
    text = text.replaceAll("&gt;", "\\>");
    text = text.replaceAll("&frac34;", "\\�");
    text = text.replaceAll("&frac14;", "\\�");
    text = text.replaceAll("&frac12;", "\\�");
    text = text.replaceAll("&euro;", "\\�");
    text = text.replaceAll("&Euml;", "\\�");
    text = text.replaceAll("&euml;", "\\�");
    text = text.replaceAll("&ETH;", "\\�");
    text = text.replaceAll("&eth;", "\\�");
    text = text.replaceAll("&Egrave;", "\\�");
    text = text.replaceAll("&egrave;", "\\�");
    text = text.replaceAll("&Ecirc;", "\\�");
    text = text.replaceAll("&ecirc;", "\\�");
    text = text.replaceAll("&Eacute;", "\\�");
    text = text.replaceAll("&eacute;", "\\�");
    text = text.replaceAll("&divide;", "\\�");
    text = text.replaceAll("&deg;", "\\�");
    text = text.replaceAll("&curren;", "\\�");
    text = text.replaceAll("&copy;", "\\�");
    text = text.replaceAll("&cent;", "\\�");
    text = text.replaceAll("&cedil;", "\\�");
    text = text.replaceAll("&Ccedil;", "\\�");
    text = text.replaceAll("&ccedil;", "\\�");
    text = text.replaceAll("&brvbar;", "\\�");
    text = text.replaceAll("&Auml;", "\\�");
    text = text.replaceAll("&auml;", "\\�");
    text = text.replaceAll("&Atilde;", "\\�");
    text = text.replaceAll("&atilde;", "\\�");
    text = text.replaceAll("&Aring;", "\\�");
    text = text.replaceAll("&aring;", "\\�");
    text = text.replaceAll("&Agrave;", "\\�");
    text = text.replaceAll("&agrave;", "\\�");
    text = text.replaceAll("&AElig;", "\\�");
    text = text.replaceAll("&aelig;", "\\�");
    text = text.replaceAll("&acute;", "\\�");
    text = text.replaceAll("&Acirc;", "\\�");
    text = text.replaceAll("&acirc;", "\\�");
    text = text.replaceAll("&Aacute;", "\\�");
    text = text.replaceAll("&aacute;", "\\�");
    text = text.replaceAll("&amp;", "\\&");
    return text;
  }

  /**
   * Este m�todo obtem uma string e a converte em um pattern RegExp para realizar Matches em Strings.<br>
   * O prop�sito deste m�todo � auxiliar o desenvolvedor a aplicar as mesmas mascaras (do SQL) utilizadas atualmente nos campos de filtros que populam o RFWMO, em uma Express�o Regular que possa ser utilizada para filtrar lista de valores em String, sem consulta no banco de dados.<br>
   * As m�scaras s�o: % - para qualquer caracter em qualquer quantidade e _ para 1 �nico caracter qualquer.
   *
   * @param value Texto escrito pelo usu�rio com as mascaras escrita acima
   * @return String com a express�o regular equivalente a ser usada em cada "String".matches() para saber se o valor � equivalente com o filtro do usu�rio.
   */
  public static String convertFieldMaskToRegExpPattern(String value) {
    if (value != null) {
      // Primeiro fazemos o Quota de toda a express�o para evitar problemas
      value = "\\Q" + value + "\\E";
      // Troca os filtros, lembrando que antes de cada filtros temos que encerrar e recome�ar o "quote" ou a express�o n�o vai considerar nem estes comandos
      value = value.replaceAll("\\%", "\\\\E.*\\\\Q");
      value = value.replaceAll("\\_", "\\\\E.\\\\Q");
    }
    return value;
  }

  /**
   * Recebe um valor em BigDecimal e o escreve por extenso. Se passado algum valor com mais de 2 casas decimais o valor ser� arredondado.
   *
   * @param value Valor a ser transformado por extenso.
   * @return String com o texto do valor por extenso em Reais, escrito em Portugu�s brasileiro.
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

    // S� processa os centavos se ele existir, se estiver zerado n�o escreve "zero centavos"
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
   * Escreve um valor por extenso. Apesar de aceitar um BigDecimal por causa do tamanho dos n�meros, os valores fracion�rios ser�o simplesmente ignorados.
   *
   * @param value Valor a ser transformado por extenso.
   * @return String com o valor por extenso em Portugu�s Brasileiro.
   */
  public static Object valueToExtense_BrazilianPortuguese(BigDecimal value) {
    final StringBuilder buff = new StringBuilder();
    final BigDecimal BIGTHOUSAND = new BigDecimal("1000");

    // Garante que os decimais ser�o ignorados
    value = value.setScale(0, RoundingMode.FLOOR);

    // Se o valor � zero j� retorna logo, n�o sai tentando calcular e esrever para n�o escrever errado. Esse � o �nico n�mero em que "zero" � escrito
    if (value.compareTo(BigDecimal.ZERO) == 0) {
      return "zero";
    }

    // Quebra o valor em cada milhar para ir compondo o valor
    int pow = 0;
    while (value.compareTo(BigDecimal.ZERO) > 0) {
      long hundreds = value.remainder(BIGTHOUSAND).longValue();
      value = value.divide(BIGTHOUSAND, 0, RoundingMode.FLOOR);

      if (hundreds > 0) {
        // Decop�e o n�mero em unidades, dezens e centenas para criar o texto
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
            // Se n�o tem nome espec�fico para o conjunto dezena e unidade, separamos em dezena e unidade
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
              utext = "tr�s";
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

        // Depois que o n�mero est� pronto, verificamos em que casa de milhar estamos para anexar o valor
        switch (pow) {
          case 0:
            // N�o h� nada, s� o n�mero mesmo
            break;
          case 1:
            text += " mil";
            break;
          case 2:
            text += (hundreds == 1 ? " milh�o" : " milh�es");
            break;
          case 3:
            text += (hundreds == 1 ? " bilh�o" : " bilh�es");
            break;
          case 4:
            text += (hundreds == 1 ? " trilh�o" : " trilh�es");
            break;
          case 5:
            text += (hundreds == 1 ? " quatrilh�o" : " quatrilh�es");
            break;
          case 6:
            text += (hundreds == 1 ? " quintilh�o" : " quintilh�es");
            break;
          case 7:
            text += (hundreds == 1 ? " sextilh�o" : " sextilh�es");
            break;
          case 8:
            text += (hundreds == 1 ? " setilh�o" : " setilh�es");
            break;
          case 9:
            text += (hundreds == 1 ? " octilh�o" : " octilh�es");
            break;
          case 10:
            text += (hundreds == 1 ? " nonilh�o" : " nonilh�es");
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
   * Este m�todo decodifica uma string codificada em base 64.
   *
   * @param encodedContent String codificada
   * @return String decodificada
   */
  public static String decodeBase64(String encodedContent) {
    return new String(Base64.getMimeDecoder().decode(encodedContent));
  }

  /**
   * Este m�todo decodifica uma string codificada em base 64.
   *
   * @param encodedContent String codificada
   * @return String decodificada
   * @throws UnsupportedEncodingException
   */
  public static String decodeBase64(String encodedContent, String charset) throws RFWException {
    try {
      return new String(Base64.getMimeDecoder().decode(encodedContent), charset);
    } catch (UnsupportedEncodingException e) {
      throw new RFWCriticalException("Charset inv�lido: '" + charset + "'!");
    }
  }

  /**
   * Este m�todo decodifica uma string codificada em base 64.
   *
   * @param encodedContent String codificada
   * @return String decodificada
   */
  public static byte[] decodeBase64ToByte(String encodedContent) {
    return Base64.getMimeDecoder().decode(encodedContent);
  }

  /**
   * Este m�todo codifica uma string em base 64.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase64(String content) {
    return new String(Base64.getMimeEncoder().encodeToString(content.getBytes()));
  }

  /**
   * Este m�todo codifica um array de bytes em base 64.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase64(byte[] content) {
    return new String(Base64.getMimeEncoder().encodeToString(content));
  }

  /**
   * Este m�todo codifica um array de bytes em base 32.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase32(byte[] content) {
    // Estamos usando o Google Guava (j� presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra op��o seria utilizar o Apache Commons, mas este ainda n�o est� presente no RFWDeprec. No futuro quem sabe ter a pr�pria implementa��o
    return BaseEncoding.base32().encode(content);
  }

  /**
   * Este m�todo codifica uma String em base 32.
   *
   * @param content String para ser codificada.
   * @return String codificada
   */
  public static String encodeBase32(String content) {
    // Estamos usando o Google Guava (j� presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra op��o seria utilizar o Apache Commons, mas este ainda n�o est� presente no RFWDeprec. No futuro quem sabe ter a pr�pria implementa��o
    return BaseEncoding.base32().encode(content.getBytes());
  }

  /**
   * Este m�todo decodifica uma String em base 32.
   *
   * @param content String para codificada.
   * @return String codificada
   */
  public static String decodeBase32(String content) {
    // Estamos usando o Google Guava (j� presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra op��o seria utilizar o Apache Commons, mas este ainda n�o est� presente no RFWDeprec. No futuro quem sabe ter a pr�pria implementa��o
    return new String(BaseEncoding.base32().decode(content));
  }

  /**
   * Este m�todo decodifica uma String em base 32.
   *
   * @param content String para codificada.
   * @return String codificada
   */
  public static byte[] decodeBase32ToByte(String content) {
    // Estamos usando o Google Guava (j� presente no RFWDeprec por conta do Vaadin e outras bibliotecas) Outra op��o seria utilizar o Apache Commons, mas este ainda n�o est� presente no RFWDeprec. No futuro quem sabe ter a pr�pria implementa��o
    return BaseEncoding.base32().decode(content);
  }

  /**
   * Escreve uma String de tr�s para frente.
   *
   * @param content - Conte�do para ser invertido.
   * @return String invertida
   */
  public static String invert(String content) {
    return new StringBuilder(content).reverse().toString();
  }

  /**
   * Extra� o conte�do de uma String que seja compatr�vel com uma express�o regular. O conte�do retornado � o conte�do dentro do primeiro grupo.<br>
   * Para definir um grupo basta colocar o conte�do que deseja retornar dentro de parenteses '(' e ')'. O primeiro parenteses encontrado define o primeiro grupo. Para que o parenteses seja parte da express�o, e n�o defini��o do grupo utilize duas barras "\\" antes do '(' para "escape" do comando: "\\(".<br>
   *
   *
   * @param text Texto de onde o valor dever� ser extra�do.
   * @param regExp Express�o regular que define o bloco a ser recuperado.
   *
   * @return Conte�do que combina com a express�o regular, extra�do do texto principal. NULO caso o conte�do n�o seja encontrado.
   * @throws RFWException
   */
  public static String extract(String text, String regExp) throws RFWException {
    return extract(text, regExp, 1);
  }

  /**
   * Extra� o conte�do de uma String que seja compatr�vel com uma express�o regular. O conte�do retornado � o conte�do dentro do grupo definido em groupID.<br>
   * Para definir um grupo basta colocar o conte�do que deseja retornar dentro de parenteses '(' e ')'. O primeiro parenteses encontrado define o primeiro grupo. Para que o parenteses seja parte da express�o, e n�o defini��o do grupo utilize duas barras "\\" antes do '(' para "escape" do comando: "\\(".<br>
   *
   *
   * @param text Texto de onde o valor dever� ser extra�do.
   * @param regExp Express�o regular que define o bloco a ser recuperado. Veja a documenta��o de {@link Pattern}
   * @param groupID Contador do grupo a ser retornado.
   *
   * @return Conte�do que combina com a express�o regular, extra�do do texto principal. NULO caso o conte�do n�o seja encontrado.
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
   * Recupera os Bytes da String no padr�o UTF-8. Embora seja uma chamada simples, j� trata a exception que � muito inconveniente de ficar no c�digo o tempo todo, sendo que o encoding j� est� fixo.
   *
   * @param s Texto String que ser� convertido para bytes.
   * @return array de bytes no padr�o UTF-8 com o conte�do da String
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
   * Recupera a String a partir dos Bytes no padr�o UTF-8. Embora seja uma chamada simples, j� trata a exception que � muito inconveniente de ficar no c�digo o tempo todo, sendo que o encoding j� est� fixo.
   *
   * @param b array de bytes utilizados para montar a String no padr�o UTF-8.
   * @return String montada a partir dos dados do Array, utilizando o padr�o UTF-8.
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
   * Remove da String tudo o que n�o for d�gitos.<br>
   * M�todo para remover pontua��o de valores num�ridos como CPF, CNPJ, Representa��es Num�ricas de C�digos de Barras, CEP, etc.<Br>
   *
   * @param value Valor a ter os "n�o n�meros" estripados
   * @return String apenas com os n�meros/d�gitos recebidos.
   * @throws RFWException
   */
  public static String removeNonDigits(String value) {
    if (value == null) return null;
    return value.replaceAll("\\D+", "");
  }

  /**
   * Remove zeros a esquerda no come�o da String.<br>
   * Ex: <br>
   * <ul>
   * <li>null -> null</li>
   * <li>"0001234" -> "1234"</li>
   * <li>"00012340000" -> "12340000"</li>
   * <li>" 00012340000" -> " 00012340000" (Note que come�a com um espa�o e n�o com zeros)</li>
   * <li>"000000000000000000" -> "" (Retorna String vazia e n�o null)</li>
   * </ul>
   *
   * @param string texto a ter os zeros do come�o da String removidos.
   * @return Mesma String sem os zeros do come�o da String.
   */
  public static String removeLeadingZeros(String string) {
    if (string == null) return null;
    return string.replaceFirst("^0*", "");
  }

  /**
   * M�todo auxiliar para separar os campos de uma linha de arquivo CSV (Comma Separated Value), considerando as Aspas e Escaped Characteres.<br>
   * <br>
   *
   * No arquivo CSV, para "escapar as v�rgulas" o campo deve ser envolto por: " (aspas). Por exemplo:
   * <li>"Meu CSV tem , no meio do texto", campo 2, etc. -> 0=Meu CSV tem , no meio do texto; 1=campo2; 2=etc.<br>
   * <br>
   * para escapar uma aspas dentro de um campo, � necess�rio duplica-la:
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
   * M�todo auxiliar para separar os campos de uma linha de arquivo CSV (Comma Separated Value), considerando as Aspas e Escaped Characteres.<br>
   * <br>
   *
   * No arquivo CSV, para "escapar as v�rgulas" o campo deve ser envolto por: " (aspas). Por exemplo:
   * <li>"Meu CSV tem , no meio do texto", campo 2, etc. -> 0=Meu CSV tem , no meio do texto; 1=campo2; 2=etc.<br>
   * <br>
   * para escapar uma aspas dentro de um campo, � necess�rio duplica-la:
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
   * @return Nunca retorna nulo, retorna o conte�do de String1 e String2 separados por virgula caso ambos tenham valor v�lido. Sendo algum nulo ou vazio, retorna apena o valor do outro. Sendo ambos nulos ou vazios, retorna "".
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
        throw new RFWValidationException("O valor do texto 'Atual' chegou ao fim na posi��o '${0}' quando era esperado o caracter '${1}'.", new String[] { "" + i, "" + expected.charAt(i) });
      }
      if (expected.charAt(i) != actual.charAt(i)) {
        String part1 = expected.substring(Math.max(0, i - 5), Math.min(expected.length(), i + 5));
        String part2 = actual.substring(Math.max(0, i - 5), Math.min(actual.length(), i + 5));
        throw new RFWValidationException("O valor do texto est� diferente na posi��o '${0}'. Esperavamos '${1}' e encontramos '${2}'.", new String[] { "" + i, part1, part2 });
      }
    }
    if (actual.length() > expected.length()) {
      String part2 = actual.substring(i, Math.min(actual.length(), i + 10));
      throw new RFWValidationException("O texto esperado chegou ao fim na posi��o '${0}' mas o valor atual continua com o conte�do '${1}...'.", new String[] { "" + i, part2 });
    }
  }

  /**
   * Realiza o substring na melhor maneira poss�vel sem lan�ar qualquer exception ou retornar nulo.
   *
   * @param value Valor String para ser cortado. Se nulo, retorna "".
   * @param startIndex Posi��o inicial para iniciar o corte. Se menor que 0, ser� ajutada para 0. Se maior que o tamanho da String for�a o retorno de "".
   * @param finalIndex Posi��o final para finalizar o corte. Se maior que o tamanho da String, ser� ajutado para o tamanho m�ximo da String. Se finalIndex <= startIndex retorna "".
   * @return Corte poss�vel conforme par�metros definidos.
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
   * <b>Aten��o:</b> Este m�todo n�o valida a data, s� procura uma ocorr�ncia que esteja no formado de data e a retorna.<br>
   * No momento o m�todo verifica se a quantidade de dias � consizentes com o m�s passado (meses de 30/31 e 29 dias). Valida se o ano come�a com 19xx ou 20xx quando houver a necessidade de trabalhar com os anos 21xx este m�todo precisa de atualiza��o.
   *
   * @param text Conte�do de Texto para procurar
   * @param groupID valor come�ando em 1 para encontrar a primeira ocorr�ncia, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso n�o.
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
   * <b>Aten��o:</b> Este m�todo n�o valida a data, s� procura uma ocorr�ncia que esteja no formado de data e a retorna.<br>
   * Valida se o ano come�a com 19xx ou 20xx quando houver a necessidade de trabalhar com os anos 21xx este m�todo precisa de atualiza��o.
   *
   * @param text Conte�do de Texto para procurar
   * @param groupID valor come�ando em 1 para encontrar a primeira ocorr�ncia, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso n�o.
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
   * <b>Aten��o:</b> Este m�todo n�o valida o hor�rio, s� procura uma ocorr�ncia que esteja no formado de data e a retorna.<br>
   * No momento o m�todo verifica se o formato � condizente com de 0 � 23h, de 0 � 59min, e 0 � 59s.
   *
   * @param text Conte�do de Texto para procurar
   * @param groupID valor come�ando em 1 para encontrar a primeira ocorr�ncia, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso n�o.
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
   * Procura uma sequ�ncia de n�meros com uma quantidade certa de d�gitos.<br>
   * N�o extrai uma sequ�ncia de n�meros de dentro de uma sequ�ncia maior (checa se est� no come�o/fim do conte�do ou cercado por espa�os)
   *
   * @param text Conte�do de Texto para Procurar
   * @param digitsCount Total de d�gitos esperados na sequ�ncia
   * @param groupID valor come�ando em 1 para encontrar a primeira ocorr�ncia, e sequencialmente.
   * @return Objeto encontrado, ou nulo caso n�o.
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
   * Procura uma String em um formato de CNPJ (todo pontuado, pacialmente ou mesmo s� uma sequ�ncia dom os 14 d�gitos) no texto passado.<Br>
   * <b>Aten��o:</b> Este m�todo n�o valida se o conte�do � v�lido, apenas procura o padr�o que possa ser considerado um CNPJ. Para validar utilize {@link BUDocValidation#validateCNPJ(String)}.
   *
   * @param text Texto a ser procurado. N�mero da ocorr�ncia.
   * @param groupID valor come�ando em 1 para encontrar a primeira ocorr�ncia, e sequencialmente.
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
   * Extrai valores num�ricos do texto. Aceita que os milhares dos n�meros estejam separados por pontos e os demais com virgula.<br>
   * Este m�todo n�o considera valores se n�o tiver a virgula.<br>
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
   * Extrai de sequ�ncia de texto que possa ser uma representa��o num�rica de um c�digo de barras de contas de servi�o/consumo.<br>
   * <b>Aten��o:</b> Este m�todo n�o valida a c�digo, s� procura uma ocorr�ncia que esteja no formado e a retorna. A valida��o pode ser feita com {@link BUDocValidation#isServiceNumericCodeValid(String)}<br>
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
