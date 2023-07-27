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
 * Description: Classe de implementação de autenticação TOTP/HOTP.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (20 de out de 2016)
 */
public class BUTOTP {

  /**
   * Número de Bits utilizado na chave secreta. Para utilizar a base 32, o padrão é de 80bits (10bytes).
   */
  private static final int SECRET_BITS = 80;

  /**
   * Quantidade de dígitos utilizados para a senha dinâmica. Padrão 6.
   */
  private static final int CODEDIGITS = 6;

  /**
   * Valor máximo (em decimal) que a senha dinâmica pode ter.
   */
  private static int PASSMAXVALUE = (int) Math.pow(10, CODEDIGITS);

  /**
   * Define o tamanho da "janela" para verificação da senha passada. O padrão é 3, assim verificamos a senha para o momento atual, a chave anterior e a posterior.
   */
  private static int WINDOWSIZE = 3;

  /**
   * Tempo de duração de cada janela. O padrão é 30s.
   */
  private static long TIMESTEPSIZEINMILLIS = TimeUnit.SECONDS.toMillis(30);

  /**
   * Define o tipo de criptografia que será utilizado para calcular as chaves. O padrão é HMAC SHA1.
   */
  private static final String HMAC_HASH_FUNCTION = "HmacSHA1";

  private BUTOTP() {
    // Contrutor privado para classe Utilitária/Estática
  }

  public static String genRandomKey() {
    // Aloca o tamanho do buffer
    byte[] buffer = new byte[SECRET_BITS / 8];

    // Enche o Buffer de bytes aleatórios
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

    // Verifica se o código de verificação tem o camanho correto
    if (verificationCode <= 0 || verificationCode >= PASSMAXVALUE) {
      return false;
    }

    // Realiza a validação
    return checkCode(secret, verificationCode, time, WINDOWSIZE);
  }

  /**
   * Este método implementa o algorítimo RFC6238 para validar uma senha dinâmica de acordo com o momento passado.
   *
   * @param secret Chave em base32 do usuário.
   * @param code Senha dinâmica para validação.
   * @param timestamp O "tempo" (em miliseconds) para o momento em que o código deve ser válido.
   * @param window Total de janelas que devemos considerar para considerar a senha válida. Entende-se por janela a cada tempo que a senha muda.
   * @return true caso a senha seja válida, false caso contrário.
   * @throws RFWException
   */
  private static boolean checkCode(String secret, long code, long timestamp, int window) throws RFWException {
    byte[] decodedKey = BUString.decodeBase32ToByte(secret);

    // Converte o tempo passado em "janelas", para saber em que "passo" da senha que estamos.
    final long timeWindow = timestamp / TIMESTEPSIZEINMILLIS;

    // De acordo com as janelas a quantidade de janelas a serem consideradas, vamos considerar metade delas "antes" da janela atual, e outra metade depois. Assim calculamos quantos passos temos de andar pra trás.
    int firstWindow = -((window - 1) / 2);
    int lastWindow = (window / 2);

    // Agora iteremos, desde o passo pra trás, até a metade de passos pra "frente" da janela atual
    for (int windowOffset = firstWindow; windowOffset <= lastWindow; ++windowOffset) {
      // Calculamos a senha para cada janela de iteração
      long hash = calculateCode(decodedKey, timeWindow + windowOffset);
      // Se for válida, já retornamos que a autenticação é válida.
      if (hash == code) return true;
    }

    // Se saiu do for sem validar a chave, retornamos que a autenticação é inválida
    return false;
  }

  /**
   * Recupera o código de autenticação de uma determinada chave.
   *
   * @param secret Chave segredo conforme exporta no QRCode compatível com o Google Authenticator
   * @return
   * @throws RFWException
   */
  public static int getCode(String secret) throws RFWException {
    return getCode(secret, 0);
  }

  /**
   * Recupera o código de autenticação de uma determinada chave.
   *
   * @param secret Chave segredo conforme exporta no QRCode compatível com o Google Authenticator
   * @param offset muda a janela de autenticação. Ex.: 1 - pega o próximo código em relação ao atual, -1 recupera o anterior em relação ao atual.
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
   * Calcula o código de verificação TOTP/HOTP.
   *
   * @param key chave secreta do usuário.
   * @param tm janela de validação.
   * @return O código de validação para a chave secreta e janela fornecida.
   * @throws RFWException
   */
  private static int calculateCode(byte[] key, long tm) throws RFWException {
    byte[] data = new byte[8]; // Array que vai guarda o "tempo da janela" convertido para big-endian.
    long value = tm;

    // Converting the instant of time from the long representation to a
    // big-endian array of bytes (RFC4226, 5.2. Description).
    // Convertemos a janela de tempo para um long utilizando a formação big-endian de representação. Capítulo 5.2 da RFC4226.
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }

    // Cria a especificação da criptografia a ser utilizada.
    SecretKeySpec signKey = new SecretKeySpec(key, HMAC_HASH_FUNCTION);

    try {
      // Utiliza o próprio mecanismo do java para calcular a criptografia
      Mac mac = Mac.getInstance(HMAC_HASH_FUNCTION);
      mac.init(signKey);
      byte[] hash = mac.doFinal(data);

      // Fazemos o truncamento da chave criada pela criptografia conforme capitulo 5.3 da RFC4226.
      int offset = hash[hash.length - 1] & 0xF;
      long truncatedHash = 0;
      for (int i = 0; i < 4; ++i) {
        truncatedHash <<= 8;
        truncatedHash |= (hash[offset + i] & 0xFF); // Como os bytes do java são "signed" (tem sinal positivo/negativo) e precisamos apenas dos bytes livres de sinal, realizamos uma operação binária para limpar tudos os bits menos o valor que precisamos (o LSB Less Significant Bit ou algo assim).
      }

      // limpamos tudo do 32° bit para frente para garantir que temos apenas os valores válidos para não atrapalhar na próxima operação
      truncatedHash &= 0x7FFFFFFF;
      // Tiramos o módulo do valor obtido com o valor máximo possível da chave para descartar todo o valor de que não necessitamos
      truncatedHash %= PASSMAXVALUE;

      return (int) truncatedHash; // Retornamos como INT, apesar de ter usado tudo como Long. Isso porque o java não tem int unsigned, só por isso usamos int durante todo o método, para que a variável tivesse "espaço" de bits suficientes para o cálculo sem que o java estragasse o valor com o signedbit.
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RFWCriticalException("RFW_ERR_200448", new String[] { e.getMessage() }, e);
    }
  }

  /**
   *
   * @param label Rótulo de identificação que aparecerá na chave do usuário. Este parametro não pode conter nenhum dos caracteres ":?/", se tiver será removido!
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
