package br.eng.rodrigogml.rfw.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWWarningException;

/**
 * Description: Classe utilitária responsável por dispobilizar classes de tratamento e manipulação de arquivos compactados do tipo ZIP.<BR>
 *
 * @author Rodrigo Leitão
 * @since 5.1.0 (11/10/2013)
 */
public class BUZip {

  private BUZip() {
  }

  /**
   * Este método retorna os nomes dos arquivos contidos no arquivo .zip.<br>
   * Retorna uma lista de String contendo seu nome completo, isto é, caminho completo incluindo as pastas dentro do arquivo.<br>
   * <br>
   * Por exemplo, imagine um arquivo zip que tenha duas pastas: <b>"A"</b> e <b>"B"</b>, dentro da pasta <b>"A"</b> exista dois arquivos: <b>"1.txt"</b> e <b>"2.txt"</b>, já na pasta <b>"B"</b> existe apenas o arquivo <b>"Cantarola.mp3"</b>. Neste caso os elementos retornados na lista serão:<br>
   * <ul>
   * <li>"A/1.txt"
   * <li>"A/2.txt"
   * <li>"B/Cantarola.mp3"
   * </ul>
   * <br>
   *
   * @param content conteúdo do arquivo em bytes.
   * @return uma lista vazia caso o arquivo ZIP não contenha nenhuma entra, ou a lista de elementos na ordem em que foram encontrados no ZIP.
   * @throws RFWException Lançado caso ocorra algum problema tentando ler o arquivo .zip
   */
  public static List<String> getZipEntries(final byte[] content) throws RFWException {
    return getZipEntries(new ByteArrayInputStream(content));
  }

  /**
   * Este método retorna os nomes dos arquivos contidos no arquivo .zip.<br>
   * Retorna uma lista de String contendo seu nome completo, isto é, caminho completo incluindo as pastas dentro do arquivo.<br>
   * <br>
   * Por exemplo, imagine um arquivo zip que tenha duas pastas: <b>"A"</b> e <b>"B"</b>, dentro da pasta <b>"A"</b> exista dois arquivos: <b>"1.txt"</b> e <b>"2.txt"</b>, já na pasta <b>"B"</b> existe apenas o arquivo <b>"Cantarola.mp3"</b>. Neste caso os elementos retornados na lista serão:<br>
   * <ul>
   * <li>"A/1.txt"
   * <li>"A/2.txt"
   * <li>"B/Cantarola.mp3"
   * </ul>
   * <br>
   *
   * @param stream InputStream do Arquivo .zip
   * @return uma lista vazia caso o arquivo ZIP não contenha nenhuma entra, ou a lista de elementos na ordem em que foram encontrados no ZIP.
   * @throws RFWException Lançado caso ocorra algum problema tentando ler o arquivo .zip
   */
  public static List<String> getZipEntries(InputStream stream) throws RFWException {
    final List<String> entries = new ArrayList<>();
    try (ZipInputStream zip = new ZipInputStream(stream)) {
      ZipEntry ze = null;
      while ((ze = zip.getNextEntry()) != null) {
        entries.add(ze.getName());
      }
      zip.closeEntry();
    } catch (IOException e) {
      throw new RFWWarningException("RFW_ERR_200078", e);
    }
    return entries;
  }

  /**
   * Extrai uma entrada de dentro do arquivo ZIP para um arquivo específico.
   *
   * @param zipContent Conteúdo do arquivo zip.
   * @param entryName Entrada a ser extraída. Incluí o caminho completo, ex:
   *          <ul>
   *          <li>"A/1.txt"
   *          <li>"A/2.txt"
   *          <li>"B/Cantarola.mp3"
   *          </ul>
   *          <br>
   * @param outputFile Arquivo destino do conteúdo extraído. Pode ser um arquivo temporário. Incluír o nome do arquivo, não apenas o diretório.
   * @throws RFWException
   */
  public static void extractZipEntry(InputStream zipContent, final String entryName, File outputFile) throws RFWException {
    try (ZipInputStream zip = new ZipInputStream(zipContent)) {
      ZipEntry ze = null;
      while ((ze = zip.getNextEntry()) != null) {
        if (ze.getName().equals(entryName)) {
          break;
        }
      }
      if (ze == null) {
        throw new RFWValidationException("RFW_ERR_200079");
      }
      if (ze.isDirectory()) {
        throw new RFWValidationException("RFW_ERR_200080");
      }

      // Tudo OK? Extrai o arquivo para o ByteArrayOutputStream
      try (FileOutputStream buffer = new FileOutputStream(outputFile)) {
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = zip.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
      }
    } catch (IOException e) {
      throw new RFWWarningException("RFW_ERR_200078", e);
    }
  }

  public static byte[] decompressGZIP(byte[] compressedData) throws RFWException {
    try {
      ByteArrayInputStream bytein = new ByteArrayInputStream(compressedData);
      GZIPInputStream gzin = new GZIPInputStream(bytein);
      ByteArrayOutputStream byteout = new ByteArrayOutputStream();

      int res = 0;
      byte buf[] = new byte[1024];
      while ((res = gzin.read(buf, 0, buf.length)) != -1) {
        byteout.write(buf, 0, res);
      }
      return byteout.toByteArray();
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200422", e);
    }
  }

  /**
   * Cria um arquivo ZIP com o conteúdo de outros arquivos dentro.
   *
   * @param zipFileName Nome do arquivo ZIP temporário para ser criado em uma pasta temporária. Ex: "CargaBalanca.zip"
   * @param compressionLevel Nível de compressão do ZIP de 0-9, sendo 0 menos comprimido e 9 o máximo de compressão.
   * @param content array bi-dimensional onde o primeiro index indica os arquivos a serem adicionados, enquanto que o segundo index deve ter tamanho 2:
   *          <li>No index 0 uma {@link String} com o nome do arquivo que será colocado dentro do ZIP,
   *          <li>No index 1 uma das opções:
   *          <ul>
   *          <li>{@link InputStream} com o conteúdo do arquivo.
   *          <li>{@link String} com o caminho para uma arquivo temporário.
   * @return Caminho para o arquivo temporário criado com o resultado da operação
   * @throws RFWException
   */
  public static String createNewZipFile(String zipFileName, int compressionLevel, Object[][] content) throws RFWException {
    File file = BUFile.createFileInTemporaryPath(zipFileName, StandardCharsets.ISO_8859_1);
    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
      zip.setMethod(ZipOutputStream.DEFLATED);
      zip.setLevel(compressionLevel);

      // Itera todo o conteúdo para adicionar dentro do ZIP
      for (int i = 0; i < content.length; i++) {
        if (content[i] != null && content[i][1] != null) {
          if (content[i][1] instanceof InputStream) {
            try (InputStream fileContent = (InputStream) content[i][1]) {
              // Cria o "arquivo" dentro do zip
              zip.putNextEntry(new ZipEntry((String) content[i][0]));
              // Escreve o conteúdo do arquivo dentro do zip
              BUIO.copy(fileContent, zip, false);
              zip.closeEntry();
            }
          } else if (content[i][1] instanceof String) {
            try (FileInputStream fileContent = new FileInputStream((String) content[i][1])) {
              zip.putNextEntry(new ZipEntry((String) content[i][0]));
              BUIO.copy(fileContent, zip, false);
              zip.closeEntry();
            }
          } else {
            throw new RFWCriticalException("Conteúdo inexperado para ser colocado dentro do arquivo ZIP!", new String[] { content[i][1].getClass().getCanonicalName() });
          }
        }
      }
    } catch (Throwable e) {
      throw new RFWCriticalException("Falha ao criar o arquivo ZIP!", e);
    }
    return file.getAbsolutePath();
  }
}
