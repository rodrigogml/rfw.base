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

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWWarningException;

/**
 * Description: Classe utilitária utilizada para geração de código de barras e similares..<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (20/06/2015)
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
 */
@Deprecated
public final class BUBarCode {

  /**
   * Construtor privado para previdir a instancia da classe utilitária.
   */
  private BUBarCode() {
  }

  /**
   * Gerador de QRCode em image.<br>
   * Mais informações sobre o QRCode em https://en.wikipedia.org/wiki/QR_code.
   *
   * @param content Define o conteúdo a ser codificado dentro do QRCode.
   * @param errorcorrectionlevel Nível de correção de dados.
   * @param size Tamanho em pixel da imagem a ser gerada.
   * @param fileType Filetype suportados para exportação. O FileType é o mesmo argumento passado para o javax.imageio.ImageIO. Os tipos suportados podem ser encontrados em http://docs.oracle.com/javase/7/docs/api/javax/imageio/package-summary.html. Tipos usados com mais frequência são: "PNG", "JPEG", "BMP" e "GIF".
   * @return byte array com o conteúdo da imagem/arquivo.
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
   * Identico ao método {@link #readQRCode(InputStream, Map)}, mas não passa nenhum parâmetro de configuração.
   *
   * @param stream conteúdo da imagem a ser interpretada.
   * @return conteúdo resgatado da imagem.
   * @throws RFWException
   */
  public static String readQRCode(InputStream stream) throws RFWException {
    return readQRCode(stream, null);
  }

  /**
   * Aceita um Stream com o conteúdo de uma imagem (JPEG, GIF, BMP, PNG) que contenha um qrcode qualquer e tenta interpretar seu conteúdo e retorna-lo.
   *
   * @param stream conteúdo da imagem a ser interpretada.
   * @param hash Configurações de decodificação do QRCode.
   * @return conteúdo resgatado da imagem.
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
   * Aceita um Stream com o conteúdo de uma imagem (JPEG, GIF, BMP, PNG) que contenha um código de barras 1D/2D qualquer e tenta interpretar seu conteúdo e retorna-lo.<br>
   * Os códigos de barras suprotados até o momento são:
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
   * @param stream conteúdo da imagem a ser interpretada.
   * @param hash Configurações de decodificação do código de barras.
   * @return conteúdo resgatado da imagem.
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
}
