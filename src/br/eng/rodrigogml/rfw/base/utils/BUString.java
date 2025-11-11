package br.eng.rodrigogml.rfw.base.utils;

/**
 * Description: Classe com métodos úteis para tratamentos e manipulação de String.<br>
 *
 * @author Rodrigo Leitão
 * @since 1.0.0 (AGO / 2007)
 * @version 4.1.0 (23/06/2011) - rodrigogml - Nome alterado de StringUtils, para ficar no padrão do sistema.
 * @deprecated Movido para RFW.Kernel na classe RUString
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
 */
@Deprecated
public class BUString {

  // /**
  // * Array com a maioria dos caracteres "comuns".
  // */
  // // public static final char[] allchars = new char[] { '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd',
  // // 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '¡', '¢', '£', '¤', '¥', '¦', '§', '¨', '©', 'ª', '«', '¬', '­', '®', '¯', '°', '±', '²', '³', '´', 'µ', '¶', '·', '¸', '¹', 'º', '»', '¼', '½', '¾', '¿', 'À', 'Á', 'Â', 'Ã', 'Ä', 'Å', 'Æ', 'Ç', 'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï', 'Ð', 'Ñ', 'Ò', 'Ó', 'Ô',
  // 'Õ',
  // // 'Ö', '×', 'Ø', 'Ù', 'Ú', 'Û', 'Ü', 'Ý', 'Þ', 'ß', 'à', 'á', 'â', 'ã', 'ä', 'å', 'æ', 'ç', 'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö', '÷', 'ø', 'ù', 'ú', 'û', 'ü', 'ý', 'þ', 'ÿ', 'Œ', 'œ', 'Š', 'š', 'Ÿ', 'Ž',
  // // 'ž','ƒ'
  // // };
  //
  // /**
  // * Remove todos os caracteres que não compõe os primeiros 128 caracteres da tabela UTF-8 pelo texto passado.
  // *
  // * @param text Texto a ser tratado.
  // * @return Texto sem os caracteres fora dos primeiros caracteres da tabela UTF-8.
  // * @throws RFWException
  // */
  // public static String removeNonUTF8BaseCaracters(String text) throws RFWException {
  // return replaceNonUTF8BaseCaracters(text, "");
  // }
  //
  // /**
  // * Substitui todos os caracteres que não compõe os primeiros 128 caracteres da tabela UTF-8 pelo texto passado.
  // *
  // * @param text Texto a ser tratado.
  // * @param replacement Valor que substituirá os caracteres removidos
  // * @return Texto tratado.
  // * @throws RFWException
  // */
  // public static String replaceNonUTF8BaseCaracters(String text, String replacement) throws RFWException {
  // return text.replaceAll("[^\\u0000-\\u007E]", replacement);
  // }
  //
  // /**
  // * Este método recebe um valor string e quebra em linhas com o tamanho máximo definido. Este método quebrará as linhas somente nos espaços em branco entre as palavras, não quebra as palavras no meio.
  // *
  // * @param content Conteúdo a ser quebrado em linhas
  // * @param maxlength tamanho máximo de cada linha.
  // * @return Array de String com todas as linhas criadas.
  // */
  // public static String[] breakLineInBlankSpaces(String content, int maxlength) {
  // final LinkedList<String> lines = new LinkedList<>();
  //
  // String[] blines = content.split("\\ ");
  // final StringBuilder b = new StringBuilder(maxlength);
  // for (int i = 0; i < blines.length; i++) {
  // // Verifica se ainda cabe na mesmoa linha
  // if (b.length() + blines[i].length() + 1 <= maxlength) { // O +1 refere-se ao espaço que será adicionado entre o conteúdo do buffer e a nova palavra
  // b.append(" ").append(blines[i]);
  // } else {
  // lines.add(b.toString());
  // b.delete(0, b.length());
  // b.append(blines[i]);
  // }
  // }
  // // Ao acabar, verificamose se temos conteúdo no buff e passamos e acrescentamos à lista, caso contrário perdemos a última linha
  // if (b.length() > 0) lines.add(b.toString());
  // String[] a = new String[lines.size()];
  // return lines.toArray(a);
  // }
  //
  // /**
  // * Método utilizado extrair o byte array de base 64 a partir de uma string que represente um valor em hexa.<br>
  // * Faz o procedimento contrário ao {@link #toHexFromBase64(byte[])}<br>
  // *
  // * @param bytearray
  // * @param hexstring
  // * @return String com o valor em HexaDecimal com as letras em lowercase.
  // */
  // public static byte[] fromHexToByteArrayBase64(String hexstring) throws RFWException {
  // return Base64.getDecoder().decode(BUString.fromHexToByteArray(hexstring));
  // }
  //
  // /**
  // * Método utilizado para converter uma String para Hexadecimal.<br>
  // * Este método utiliza o CharSet Padrão do ambiente.
  // *
  // * @param value Valor a ser convertido
  // * @return String com o valor em HexaDecimal com as letras em lowercase.
  // */
  // public static String toHex(String value) throws RFWException {
  // return toHex(value.getBytes(/* YOUR_CHARSET? */));
  // }
  //
  // /**
  // * Método utilizado para converter uma String para Hexadecimal.<br>
  // * Este método permite identificar o charset usado para decodificar a String.
  // *
  // * @param value Valor a ser convertido
  // * @param charset Charset para decodificação da String
  // * @return String com o valor em HexaDecimal com as letras em lowercase.
  // */
  // public static String toHex(String value, Charset charset) throws RFWException {
  // return toHex(value.getBytes(charset));
  // }
  //
  // /**
  // * Método utilizado para converter um array de bytes para Hexadecimal.<br>
  // *
  // * @param bytes cadeia de bytes a ser convertido para uma String representando o valor Hexadecimal
  // * @return String com o valor em HexaDecimal com as letras em lowercase.
  // */
  // public static String toHex(byte[] bytes) throws RFWException {
  // return String.format("%040x", new BigInteger(1, bytes));
  // }
  //
  // /**
  // * Este método recebe uma string representando valores em hexa e retorna os valores em um array de bytes.
  // *
  // * @param hexstring String representando um valor hexa
  // * @return array de bytes com os mesmos valores representados em hexa na string.
  // * @throws RFWException
  // */
  // public static byte[] fromHexToByteArray(String hexstring) throws RFWException {
  // // Valida a String recebida se só tem caracteres em Hexa
  // if (!hexstring.matches("[0-9A-Fa-f]*")) {
  // throw new RFWValidationException("RFW_ERR_200362");
  // }
  // return new BigInteger(hexstring, 16).toByteArray();
  // }
  //
  // /**
  // * Método utilizado para converter uma string de valor hexadecimal para uma String. Utiliza os bytes dos valores hexa decimal para converter em String utilizando o charset padrão do sistema.
  // *
  // * @param hexstring String com valores em hexa
  // * @return String montada usando os bytes do valor hexa com o charset padrão do sistema.
  // * @throws RFWException
  // */
  // public static String fromHexToString(String hexstring) throws RFWException {
  // // Valida a String recebida se só tem caracteres em Hexa
  // if (!hexstring.matches("[0-9A-Fa-f]*")) {
  // throw new RFWValidationException("RFW_ERR_200362");
  // }
  // return new String(new BigInteger(hexstring, 16).toByteArray());
  // }
  //
  // /**
  // * Calcula a Hash SHA1 de uma String.
  // *
  // * @param value Valor a ter a Hash calculada.
  // * @return Valor em Hexa calculado com o algorítimo de SHA1.
  // * @throws RFWException
  // */
  // public static String calcSHA1(String value) throws RFWException {
  // try {
  // MessageDigest cript = MessageDigest.getInstance("SHA-1");
  // cript.reset();
  // cript.update(value.getBytes());
  // return toHex(cript.digest());
  // } catch (NoSuchAlgorithmException e) {
  // throw new RFWCriticalException("RFW_ERR_200307", e);
  // }
  // }
  //
  // /**
  // * Calcula a Hash SHA1 de uma String.
  // *
  // * @param value Valor a ter a Hash calculada.
  // * @param charset Defineo charset do valor, usado para converter corretamente em bytes.
  // * @return Valor em Hexa calculado com o algorítimo de SHA1.
  // * @throws RFWException
  // */
  // public static String calcSHA1(String value, String charset) throws RFWException {
  // try {
  // MessageDigest cript = MessageDigest.getInstance("SHA-1");
  // cript.reset();
  // cript.update(value.getBytes(charset));
  // return toHex(cript.digest());
  // } catch (Exception e) {
  // throw new RFWCriticalException("RFW_ERR_200307", e);
  // }
  // }
  //
  // /**
  // * Escreve uma String de trás para frente.
  // *
  // * @param content - Conteúdo para ser invertido.
  // * @return String invertida
  // */
  // public static String invert(String content) {
  // return new StringBuilder(content).reverse().toString();
  // }

}
