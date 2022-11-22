package br.eng.rodrigogml.rfw.base.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWWarningException;

/**
 * Description: Classe utilit�ria utilizada para gera��o de c�digo de barras e similares..<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (20/06/2015)
 */
public final class BUBarCode {

  /**
   * Construtor privado para previdir a instancia da classe utilit�ria.
   */
  private BUBarCode() {
  }

  /**
   * Gerador de QRCode em image.<br>
   * Mais informa��es sobre o QRCode em https://en.wikipedia.org/wiki/QR_code.
   *
   * @param content Define o conte�do a ser codificado dentro do QRCode.
   * @param errorcorrectionlevel N�vel de corre��o de dados.
   * @param size Tamanho em pixel da imagem a ser gerada.
   * @param fileType Filetype suportados para exporta��o. O FileType � o mesmo argumento passado para o javax.imageio.ImageIO. Os tipos suportados podem ser encontrados em http://docs.oracle.com/javase/7/docs/api/javax/imageio/package-summary.html. Tipos usados com mais frequ�ncia s�o: "PNG", "JPEG", "BMP" e "GIF".
   * @return byte array com o conte�do da imagem/arquivo.
   * @throws RFWException
   */
  public static byte[] generateQRCode(String content, ErrorCorrectionLevel errorcorrectionlevel, int size, String fileType) throws RFWException {
    // Valida os parametros antes
    if (content == null || "".equals(content)) {
      throw new RFWValidationException("RFW_ERR_200309");
    }
    if (size < 10) {
      throw new RFWValidationException("RFW_ERR_200310");
    }
    if (errorcorrectionlevel == null) {
      throw new RFWValidationException("RFW_ERR_200311");
    }
    if (fileType == null || "".equals(fileType)) {
      throw new RFWValidationException("RFW_ERR_200312");
    }
    // Tenta gerar o QRCode
    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
    try {
      Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
      hintMap.put(EncodeHintType.ERROR_CORRECTION, errorcorrectionlevel);
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);
      int width = byteMatrix.getWidth();
      BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
      image.createGraphics();

      Graphics2D graphics = (Graphics2D) image.getGraphics();
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, width, width);
      graphics.setColor(Color.BLACK);

      for (int i = 0; i < width; i++) {
        for (int j = 0; j < width; j++) {
          if (byteMatrix.get(i, j)) {
            graphics.fillRect(i, j, 1, 1);
          }
        }
      }
      ImageIO.write(image, fileType, outstream);
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200308", e);
    } finally {
      try {
        outstream.flush();
        outstream.close();
      } catch (Exception e) {
      }
    }
    return outstream.toByteArray();
  }

  /**
   * Identico ao m�todo {@link #readQRCode(InputStream, Map)}, mas n�o passa nenhum par�metro de configura��o.
   *
   * @param stream conte�do da imagem a ser interpretada.
   * @return conte�do resgatado da imagem.
   * @throws RFWException
   */
  public static String readQRCode(InputStream stream) throws RFWException {
    return readQRCode(stream, null);
  }

  /**
   * Aceita um Stream com o conte�do de uma imagem (JPEG, GIF, BMP, PNG) que contenha um qrcode qualquer e tenta interpretar seu conte�do e retorna-lo.
   *
   * @param stream conte�do da imagem a ser interpretada.
   * @param hash Configura��es de decodifica��o do QRCode.
   * @return conte�do resgatado da imagem.
   * @throws RFWException
   */
  public static String readQRCode(InputStream stream, Map<DecodeHintType, ?> hash) throws RFWException {
    try {
      BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(stream))));
      Result qrCodeResult = new QRCodeReader().decode(binaryBitmap, hash);
      return qrCodeResult.getText();
    } catch (Exception e) {
      throw new RFWWarningException("RFW_ERR_200313");
    }
  }

  /**
   * Aceita um Stream com o conte�do de uma imagem (JPEG, GIF, BMP, PNG) que contenha um c�digo de barras 1D/2D qualquer e tenta interpretar seu conte�do e retorna-lo.<br>
   * Os c�digos de barras suprotados at� o momento s�o:
   * <ul>
   * <li>1D product
   * <ul>
   * <li>UPC-A
   * <li>UPC-E
   * <li>EAN-8
   * <li>EAN-13
   * </ul>
   * <li>1D industrial
   * <ul>
   * <li>Code 39
   * <li>Code 93
   * <li>Code 128
   * <li>Codabar
   * <li>ITF
   * <li>RSS-14
   * <li>RSS-Expanded
   * </ul>
   * <li>2D
   * <ul>
   * <li>QR Code
   * <li>Data Matrix
   * <li>Aztec (beta)
   * <li>PDF 417 (beta)
   * </ul>
   * </ul>
   *
   * @param stream conte�do da imagem a ser interpretada.
   * @param hash Configura��es de decodifica��o do c�digo de barras.
   * @return conte�do resgatado da imagem.
   * @throws RFWException
   */
  public static String readMultiFormat(InputStream stream, Map<DecodeHintType, ?> hash) throws RFWException {
    try {
      BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(stream))));
      Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hash);
      return qrCodeResult.getText();
    } catch (Exception e) {
      throw new RFWWarningException("RFW_ERR_200313");
    }
  }

  /**
   * Valida se o c�digo de barras GTIN � v�lido. Funciona para GTIN8, GTIN12, GTIN13 e GTIN14.<br>
   * Caso o valor passado seja nulo, resultar� em NullPointerException para evitar que erros de programa��o em passar o valor sejam acobertados por uma "preven��o" interna do m�todo.
   *
   * @param fullCodeBar C�digo de Barra completo, incluindo o d�vido verificador
   * @return true se for um c�digo v�lido, false caso contr�rio
   */
  public static boolean isGTINCodeBarValid(String fullCodeBar) {
    boolean ret = false;
    if (fullCodeBar.length() == 8 || fullCodeBar.length() == 12 || fullCodeBar.length() == 13 || fullCodeBar.length() == 14) {
      if (fullCodeBar.matches("[0-9]*")) {
        int impSum = 0;
        // PS: Nas itera��es n�o consideramos o �ltimo n�mero apra os c�lculos poide deve ser o DV
        for (int i = fullCodeBar.length() - 2; i >= 0; i -= 2) { // itera os n�meros nas posi��es impares
          impSum += Integer.parseInt(fullCodeBar.substring(i, i + 1));
        }
        impSum *= 3; // Multiplicamos o resultado por 3
        for (int i = fullCodeBar.length() - 3; i >= 0; i -= 2) { // soma os n�meros nas pori��es pares
          impSum += Integer.parseInt(fullCodeBar.substring(i, i + 1));
        }
        // Verificamos o n�mero que "falta" para chegar no pr�ximo m�ltiplo de 10
        int dv = (10 - (impSum % 10)) % 10; // <- O segundo m�dulo garante que quando o resultado do primeiro m�dulo der 0, o DV n�o resulta em 10, e sim em 0 como deve ser.

        // Verificamos se � v�lido
        return fullCodeBar.substring(fullCodeBar.length() - 1, fullCodeBar.length()).equals("" + dv);
      }
    }
    return ret;
  }
}
