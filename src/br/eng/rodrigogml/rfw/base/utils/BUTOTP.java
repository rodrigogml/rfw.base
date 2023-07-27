package br.eng.rodrigogml.rfw.base.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.utils.RUFile;

/**
 * Description: Classe de implementa��o de autentica��o TOTP/HOTP.<br>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (20 de out de 2016)
 */
public class BUTOTP {

  /**
   * N�mero de Bits utilizado na chave secreta. Para utilizar a base 32, o padr�o � de 80bits (10bytes).
   */
  private static final int SECRET_BITS = 80;

  /**
   * Quantidade de d�gitos utilizados para a senha din�mica. Padr�o 6.
   */
  private static final int CODEDIGITS = 6;

  /**
   * Valor m�ximo (em decimal) que a senha din�mica pode ter.
   */
  private static int PASSMAXVALUE = (int) Math.pow(10, CODEDIGITS);

  /**
   * Define o tamanho da "janela" para verifica��o da senha passada. O padr�o � 3, assim verificamos a senha para o momento atual, a chave anterior e a posterior.
   */
  private static int WINDOWSIZE = 3;

  /**
   * Tempo de dura��o de cada janela. O padr�o � 30s.
   */
  private static long TIMESTEPSIZEINMILLIS = TimeUnit.SECONDS.toMillis(30);

  /**
   * Define o tipo de criptografia que ser� utilizado para calcular as chaves. O padr�o � HMAC SHA1.
   */
  private static final String HMAC_HASH_FUNCTION = "HmacSHA1";

  private BUTOTP() {
    // Contrutor privado para classe Utilit�ria/Est�tica
  }

  public static String genRandomKey() {
    // Aloca o tamanho do buffer
    byte[] buffer = new byte[SECRET_BITS / 8];

    // Enche o Buffer de bytes aleat�rios
    new Random().nextBytes(buffer);

    // Extrai os bytes para gerar a chave
    byte[] secretKey = Arrays.copyOf(buffer, SECRET_BITS / 8);
    String generatedKey = BUString.encodeBase32(secretKey);

    return generatedKey;
  }

  public static boolean authorize(String secret, int verificationCode) throws RFWException {
    return authorize(secret, verificationCode, new Date().getTime());
  }

  public static boolean authorize(String secret, int verificationCode, long time) throws RFWException {
    if (secret == null) {
      throw new RFWCriticalException("RFW_ERR_200447");
    }

    // Verifica se o c�digo de verifica��o tem o camanho correto
    if (verificationCode <= 0 || verificationCode >= PASSMAXVALUE) {
      return false;
    }

    // Realiza a valida��o
    return checkCode(secret, verificationCode, time, WINDOWSIZE);
  }

  /**
   * Este m�todo implementa o algor�timo RFC6238 para validar uma senha din�mica de acordo com o momento passado.
   *
   * @param secret Chave em base32 do usu�rio.
   * @param code Senha din�mica para valida��o.
   * @param timestamp O "tempo" (em miliseconds) para o momento em que o c�digo deve ser v�lido.
   * @param window Total de janelas que devemos considerar para considerar a senha v�lida. Entende-se por janela a cada tempo que a senha muda.
   * @return true caso a senha seja v�lida, false caso contr�rio.
   * @throws RFWException
   */
  private static boolean checkCode(String secret, long code, long timestamp, int window) throws RFWException {
    byte[] decodedKey = BUString.decodeBase32ToByte(secret);

    // Converte o tempo passado em "janelas", para saber em que "passo" da senha que estamos.
    final long timeWindow = timestamp / TIMESTEPSIZEINMILLIS;

    // De acordo com as janelas a quantidade de janelas a serem consideradas, vamos considerar metade delas "antes" da janela atual, e outra metade depois. Assim calculamos quantos passos temos de andar pra tr�s.
    int firstWindow = -((window - 1) / 2);
    int lastWindow = (window / 2);

    // Agora iteremos, desde o passo pra tr�s, at� a metade de passos pra "frente" da janela atual
    for (int windowOffset = firstWindow; windowOffset <= lastWindow; ++windowOffset) {
      // Calculamos a senha para cada janela de itera��o
      long hash = calculateCode(decodedKey, timeWindow + windowOffset);
      // Se for v�lida, j� retornamos que a autentica��o � v�lida.
      if (hash == code) return true;
    }

    // Se saiu do for sem validar a chave, retornamos que a autentica��o � inv�lida
    return false;
  }

  /**
   * Recupera o c�digo de autentica��o de uma determinada chave.
   *
   * @param secret Chave segredo conforme exporta no QRCode compat�vel com o Google Authenticator
   * @return
   * @throws RFWException
   */
  public static int getCode(String secret) throws RFWException {
    return getCode(secret, 0);
  }

  /**
   * Recupera o c�digo de autentica��o de uma determinada chave.
   *
   * @param secret Chave segredo conforme exporta no QRCode compat�vel com o Google Authenticator
   * @param offset muda a janela de autentica��o. Ex.: 1 - pega o pr�ximo c�digo em rela��o ao atual, -1 recupera o anterior em rela��o ao atual.
   * @return
   * @throws RFWException
   */
  public static int getCode(String secret, int offset) throws RFWException {
    byte[] key;
    try {
      key = BUString.decodeBase32ToByte(secret);
    } catch (Exception e) {
      key = BUString.decodeBase64ToByte(secret);
    }
    long tm = new Date().getTime() / TIMESTEPSIZEINMILLIS;

    return calculateCode(key, tm + offset);
  }

  /**
   * Calcula o c�digo de verifica��o TOTP/HOTP.
   *
   * @param key chave secreta do usu�rio.
   * @param tm janela de valida��o.
   * @return O c�digo de valida��o para a chave secreta e janela fornecida.
   * @throws RFWException
   */
  private static int calculateCode(byte[] key, long tm) throws RFWException {
    byte[] data = new byte[8]; // Array que vai guarda o "tempo da janela" convertido para big-endian.
    long value = tm;

    // Converting the instant of time from the long representation to a
    // big-endian array of bytes (RFC4226, 5.2. Description).
    // Convertemos a janela de tempo para um long utilizando a forma��o big-endian de representa��o. Cap�tulo 5.2 da RFC4226.
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }

    // Cria a especifica��o da criptografia a ser utilizada.
    SecretKeySpec signKey = new SecretKeySpec(key, HMAC_HASH_FUNCTION);

    try {
      // Utiliza o pr�prio mecanismo do java para calcular a criptografia
      Mac mac = Mac.getInstance(HMAC_HASH_FUNCTION);
      mac.init(signKey);
      byte[] hash = mac.doFinal(data);

      // Fazemos o truncamento da chave criada pela criptografia conforme capitulo 5.3 da RFC4226.
      int offset = hash[hash.length - 1] & 0xF;
      long truncatedHash = 0;
      for (int i = 0; i < 4; ++i) {
        truncatedHash <<= 8;
        truncatedHash |= (hash[offset + i] & 0xFF); // Como os bytes do java s�o "signed" (tem sinal positivo/negativo) e precisamos apenas dos bytes livres de sinal, realizamos uma opera��o bin�ria para limpar tudos os bits menos o valor que precisamos (o LSB Less Significant Bit ou algo assim).
      }

      // limpamos tudo do 32� bit para frente para garantir que temos apenas os valores v�lidos para n�o atrapalhar na pr�xima opera��o
      truncatedHash &= 0x7FFFFFFF;
      // Tiramos o m�dulo do valor obtido com o valor m�ximo poss�vel da chave para descartar todo o valor de que n�o necessitamos
      truncatedHash %= PASSMAXVALUE;

      return (int) truncatedHash; // Retornamos como INT, apesar de ter usado tudo como Long. Isso porque o java n�o tem int unsigned, s� por isso usamos int durante todo o m�todo, para que a vari�vel tivesse "espa�o" de bits suficientes para o c�lculo sem que o java estragasse o valor com o signedbit.
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RFWCriticalException("RFW_ERR_200448", new String[] { e.getMessage() }, e);
    }
  }

  /**
   *
   * @param label R�tulo de identifica��o que aparecer� na chave do usu�rio. Este parametro n�o pode conter nenhum dos caracteres ":?/", se tiver ser� removido!
   * @param secretKey Chave secreta de controle da senha
   * @return URI para ser colocada no QRCode. Ex: "otpauth://totp/user@host.com?secret=FQU3Q6FUTTBYN3QK"
   * @throws RFWException
   */
  public static String createQRContent(String label, String secretKey) throws RFWException {
    if (label == null) {
      throw new RFWValidationException("RFW_ERR_200449");
    }
    label = label.replaceAll("[\\:\\?/]", "");
    if (label.length() == 0) {
      throw new RFWValidationException("RFW_ERR_200449");
    }

    StringBuilder buff = new StringBuilder(100);
    buff.append("otpauth://totp/").append(label).append("?secret=").append(secretKey);

    return buff.toString();
  }

  public static void main2(String[] args) throws Exception {
    // for (int i = 0; i < 20; i++)
    // System.out.println(genRandomKey());
    System.out.println(BUTOTP.authorize("FQU3Q6FUTTBYN3QK", 343761));
    final String qrcode = BUTOTP.createQRContent("RFWERP (Rodrigo GML)", "FQU3Q6FUTTBYN3QK");
    System.out.println(qrcode);

    final byte[] image = BUBarCode.generateQRCode(qrcode, ErrorCorrectionLevel.M, 300, "PNG");
    RUFile.writeFileContent("C:\\t\\qrcode.png", image);
  }

  public static void main(String[] args) throws Exception {
    // otpauth://totp/FB?secret=YMMMBBQHX4O4ID7JLS7VSTJJ6PTAUEPQ&digits=6
    System.out.println(getCode("YMMMBBQHX4O4ID7JLS7VSTJJ6PTAUEPQ"));
  }

}
