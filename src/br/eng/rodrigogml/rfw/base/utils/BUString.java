package br.eng.rodrigogml.rfw.base.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;
import br.eng.rodrigogml.rfw.kernel.utils.RUDocValidation;

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
   * Array com a maioria dos caracteres "comuns".
   */
  public static final char[] allchars = new char[] { '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�', '�',
      '�', '�' };

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
   * <b>Aten��o:</b> Este m�todo n�o valida se o conte�do � v�lido, apenas procura o padr�o que possa ser considerado um CNPJ. Para validar utilize {@link RUDocValidation#validateCNPJ(String)}.
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
   * <b>Aten��o:</b> Este m�todo n�o valida a c�digo, s� procura uma ocorr�ncia que esteja no formado e a retorna. A valida��o pode ser feita com {@link RUDocValidation#isServiceNumericCodeValid(String)}<br>
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
