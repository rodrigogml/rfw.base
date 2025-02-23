package br.eng.rodrigogml.rfw.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.util.Charsets;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.utils.RUFile;

/**
 * Description: Utilit�rios para gerenciar arquivos, escrever, ler, etc.<br>
 *
 * @author Rodrigo Leit�o
 * @since 3.1.0 (NOV / 2009)
 * @deprecated Movida para o o RFWDeprec.Kernel com o nome de RUFile - m�todos sendo migrados pouco a pouco conforme a necessidade (Ao levar o m�todo para l�, excluir daqui).
 */
@Deprecated
public class BUFile {

  public static boolean isDirectory(String path) {
    return new File(path).isDirectory();
  }

  public static boolean isDirectory(File path) {
    return path.isDirectory();
  }

  /**
   * Retorna os arquivos de um determinado diret�rio que tenham a data de cria��o maiores ou iguais a uma determinada data.
   *
   * @param path Caminho/Diret�rio dos arquivos
   * @param timemillis Data de cria��o inicial em milisegundos (Equivalente ao System.currentTimemillis)
   * @return Arquivos criados depois da data definida
   * @throws RFWException
   */
  public static File[] getFilesNewerOrEqualThan(String path, long timemillis) throws RFWException {
    final File[] files = RUFile.getFilesFromDirectory(path);

    final LinkedList<File> newerFiles = new LinkedList<>();
    try {
      for (File file : files) {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        if (attr.creationTime().toMillis() >= timemillis) {
          newerFiles.add(file);
        }
      }
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200443");
    }
    return newerFiles.toArray(new File[0]);
  }

  public static String[] getDirectoryFromDirectory(String path) {
    File[] listOfFiles = RUFile.getFilesFromDirectory(path);

    ArrayList<String> directories = new ArrayList<>(listOfFiles.length);

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isDirectory()) {
        directories.add(listOfFiles[i].getAbsolutePath());
      }
    }

    return directories.toArray(new String[0]);
  }

  public static List<String> getFilesFromDirectoryRecursively(String path) {
    return getFilesFromDirectoryRecursively(new File(path));
  }

  public static List<String> getFilesFromDirectoryRecursively(File path) {
    LinkedList<String> files = new LinkedList<String>();

    if (path.isDirectory()) {
      for (File file : path.listFiles()) {
        if (file.isDirectory()) {
          files.addAll(getFilesFromDirectoryRecursively(file));
        } else {
          files.add(file.getAbsolutePath());
        }
      }
    }
    return files;
  }

  /**
   * Renomeia um arquivo do sistema.
   *
   * @param original Caminho do arquvio a ser renomeado
   * @param destination Caminho de destino (e nome desejado) do arquivo. Mais informa��es {@link File#renameTo(File)}.
   * @return true caso tenha renomeado com sucesso, false caso contr�rio (n�o lan�a exception).
   */
  public static boolean renameFile(String original, String destination) {
    return renameFile(new File(original), new File(destination));
  }

  /**
   * Renomeia um arquivo do sistema.
   *
   * @param original Caminho do arquvio a ser renomeado
   * @param destination Caminho de destino (e nome desejado) do arquivo. Mais informa��es {@link File#renameTo(File)}.
   * @return true caso tenha renomeado com sucesso, false caso contr�rio (n�o lan�a exception).
   */
  public static boolean renameFile(File original, File destination) {
    return original.renameTo(destination);
  }

  public static boolean deleteFile(String filename) {
    return deleteFile(new File(filename));
  }

  public static boolean deleteFile(File file) {
    if (file.exists() && file.isFile()) {
      return file.delete();
    }
    return false;
  }

  /**
   * Retorna a data e hora de cria��o de um arquivo.
   *
   * @param fileName Caminho completo ou relativo � aplica��o que leve ao arquivo.
   * @return Objeto Date criado com o timezone padr�o do sistema, que deve equivaler como do arquivo.
   * @throws RFWException
   */
  public static Date getCreationDate(String fileName) throws RFWException {
    // Se o arquivo n�o existir lan�a cr�tico, isso deve ser validado adequadamente fora da classe utilit�ria
    if (!RUFile.fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName });

    Path path = new File(fileName).toPath();
    BasicFileAttributes attr;
    try {
      attr = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200443");
    }
    return new Date(attr.creationTime().toMillis());
  }

  /**
   * Retorna a data e hora do �ltimo acesso ao arquivo.
   *
   * @param fileName Caminho completo ou relativo � aplica��o que leve ao arquivo.
   * @return Objeto Date criado com o timezone padr�o do sistema, que deve equivaler como do arquivo.
   * @throws RFWException
   */
  public static Date getLastAccessTime(String fileName) throws RFWException {
    // Se o arquivo n�o existir lan�a cr�tico, isso deve ser validado adequadamente fora da classe utilit�ria
    if (!RUFile.fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName });

    Path path = new File(fileName).toPath();
    BasicFileAttributes attr;
    try {
      attr = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200443");
    }
    return new Date(attr.lastAccessTime().toMillis());
  }

  /**
   * Retorna a data e hora da �ltima modifica��o do arquivo.
   *
   * @param fileName Caminho completo ou relativo � aplica��o que leve ao arquivo.
   * @return Objeto Date criado com o timezone padr�o do sistema, que deve equivaler como do arquivo.
   * @throws RFWException
   */
  public static Date getLastModifiedTime(String fileName) throws RFWException {
    // Se o arquivo n�o existir lan�a cr�tico, isso deve ser validado adequadamente fora da classe utilit�ria
    if (!RUFile.fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName });

    Path path = new File(fileName).toPath();
    BasicFileAttributes attr;
    try {
      attr = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200443");
    }
    return new Date(attr.lastModifiedTime().toMillis());
  }

  /**
   * Retorna se o objeto do caminho passado � um Symbolic Link.
   *
   * @param fileName Caminho completo ou relativo � aplica��o que leve ao arquivo.
   * @return Boolean indicando 'true' caso o objeto seja um SymLink, 'false' n�o seja.
   * @throws RFWException
   */
  public static boolean isSymbolicLink(String fileName) throws RFWException {
    return isSymbolicLink(new File(fileName));
  }

  /**
   * Retorna se o objeto do caminho passado � um Symbolic Link.
   *
   * @param fileName Caminho completo ou relativo � aplica��o que leve ao arquivo.
   * @return Boolean indicando 'true' caso o objeto seja um SymLink, 'false' n�o seja.
   * @throws RFWException
   */
  public static boolean isSymbolicLink(File fileName) throws RFWException {
    // Se o arquivo n�o existir lan�a cr�tico, isso deve ser validado adequadamente fora da classe utilit�ria
    if (!RUFile.fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName.getAbsolutePath() });

    Path path = fileName.toPath();
    BasicFileAttributes attr;
    try {
      attr = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200443");
    }
    return attr.isSymbolicLink();
  }

  /**
   * Este m�todo verifica a exist�ncia do caminho e, caso n�o exista ainda, o cria.<br>
   * <b>Este m�todo aceita o caminho do diret�rio sem o nome do arquivo. Caso tenha o caminho com o nome do arquivo utilize o {@link #createPathOfFile(String)}</b>
   *
   * @param pathName Caminho para o diret�rio a ser criado.
   * @throws RFWException
   */
  public static void createPath(String pathName) throws RFWException {
    File file = new File(pathName);
    file.mkdirs(); // For�a criar os diret�rios caso n�o existam
  }

  /**
   * Este m�todo verifica a exist�ncia dos diret�rios do caminho ddo arquivo passado, caso n�o exista ainda, o cria.<br>
   * <b>Este m�todo aceita o caminho completo incluindo o nome do arquivo, e garante que sua pasta seja criada. Caso tenha apenas os nomes dos diret�rios utilize o {@link #createPath(String)}</b>
   *
   * @param filePath Caminho completo do arquivo, cujo diret�rio deve ser criado.
   * @throws RFWException
   */
  public static void createPathOfFile(String filePath) throws RFWException {
    File file = new File(filePath);
    file.mkdirs(); // For�a criar os diret�rios caso n�o existam
  }

  /**
   * Extrai o nome do arquivo de um caminho completo recebido.
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFileFullName(String file) {
    File f = new File(file);
    return f.getName();
  }

  /**
   * Extrai o nome e exten��o do arquivo de um caminho completo recebido.
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFileNameAndExtension(String file) {
    return new File(file).getName();
  }

  /**
   * Extrai o caminho (diret�rio) de um caminho completo recebido. N�O RETORNA O �LTIMO SEPARADOR! Nem mesmo quando est� na raiz: Windows = "c:", no Linux = "".
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFullPath(String file) {
    return file.substring(0, file.lastIndexOf(File.separatorChar));
  }

  /**
   * Este m�todo tenta descobrir o charset correto de um arquivo. Note para identificar o charset correto ser� testado um decoder de cada charset no arquivo, e alguns deles podem dar m�ltiplos positivos, o que n�o garante uma exatid�o na detec��o.<br>
   * Teste os seguintes charsets:
   * <li>StandardCharsets.ISO_8859_1
   * <li>StandardCharsets.UTF_8
   * <li>StandardCharsets.US_ASCII
   * <li>StandardCharsets.UTF_16<br>
   *
   * @param filePath Caminho para o Arquivo a ser detectado o charset.
   * @return Charset em que o conte�do do arquivo foi validado, ou null caso nenhum Charset tenha sido identificado.
   * @throws RFWException
   */
  public static Charset detectTextFileCharset(String filePath) throws RFWException {
    Charset charset = null;

    final Charset[] charsetTest = new Charset[] { StandardCharsets.UTF_8, StandardCharsets.US_ASCII, StandardCharsets.UTF_16, StandardCharsets.ISO_8859_1 };

    found: for (Charset charsetTmp : charsetTest) {
      try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath))) {
        CharsetDecoder decoder = charsetTmp.newDecoder();
        decoder.reset();

        byte[] buffer = new byte[512];
        boolean identified = false;
        while ((input.read(buffer) != -1) && (!identified)) {
          try {
            decoder.decode(ByteBuffer.wrap(buffer));
            charset = charsetTmp;
            break found;
          } catch (CharacterCodingException e) {
            // n�o faz nada, mant�m o identified em false
            break;
          }
        }
      } catch (Exception e) {
        throw new RFWCriticalException("Falha ao l�r o conte�do do arquivo!", e);
      }
    }
    return charset;
  }

  /**
   * Exclui todos os arquivos encontrados dentro de um diret�rio.<br>
   * Mesmo que o m�todo {@link #deleteAllFilesFromPath(String, boolean)} com a op��o de symLink em false.
   *
   * @param path Caminho/Diret�rio para se excluir os arquivos (note que o diret�rio n�o � exclu�do).
   * @throws RFWException Em caso de falhas
   */
  public static void deleteAllFilesFromPath(String path) throws RFWException {
    deleteAllFilesFromPath(path, false);
  }

  /**
   * Exclui todos os arquivos encontrados dentro de um diret�rio.
   *
   * @param path Caminho/Diret�rio para se excluir os arquivos (note que o diret�rio n�o � exclu�do).
   * @param deleteSymlinks se true, for�a a exclus�o mesmo que seja um Symbolic Link e n�o um arquivo real.
   * @throws RFWException Em caso de falhas
   */
  public static void deleteAllFilesFromPath(String path, boolean deleteSymlinks) throws RFWException {
    File fPath = new File(path);
    if (!fPath.exists()) throw new RFWValidationException("O caminho '" + path + "' n�o existe!");
    if (!fPath.isDirectory()) throw new RFWValidationException("O caminho '" + path + "' n�o � um diret�rio v�lido!");
    if (!fPath.canWrite()) throw new RFWValidationException("N�o temos permiss�o para escrever no diret�rio '" + path + "'!");

    for (File file : RUFile.getFilesFromDirectory(path)) {
      if (deleteSymlinks || !isSymbolicLink(file)) deleteFile(file);
    }
  }

  /**
   * Abre um arquivo de PDF e tenta extrair o seu conte�do de texto com uma estrat�gia simples de texto. Escreve o conte�do em um arquivo tempor�rio e retorna o caminho do arquivo.
   *
   * @param filePath caminho para o arquivo PDF a ser lido
   * @return Caminho para o arquivo tempor�rio com o texto encontrado no PDF.
   * @throws RFWException
   */
  public static String scrapPDFText(String filePath) throws RFWException {
    return scrapPDFText(filePath, null);
  }

  /**
   * Abre um arquivo de PDF e tenta extrair o seu conte�do de texto com uma estrat�gia simples de texto. Escreve o conte�do em um arquivo tempor�rio e retorna o caminho do arquivo.
   *
   * @param filePath caminho para o arquivo PDF a ser lido
   * @param pageMarker Caso passada algum valor, esse valor ser� escrito no come�o de cada p�gina. Se essa marca��o contiver o texto "${0}", ele ser� substitu�do pelo n�mero da p�gina.
   * @return Caminho para o arquivo tempor�rio com o texto encontrado no PDF.
   * @throws RFWException
   */
  public static String scrapPDFText(String filePath, String pageMarker) throws RFWException {
    try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(filePath))) {
      File file = RUFile.createTemporaryFile("pdfscrap", "txt", null);
      for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
        SimpleTextExtractionStrategy extract = new SimpleTextExtractionStrategy();
        String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), extract);
        if (pageMarker != null) {
          if (i > 1) RUFile.writeFileContent(file, "\r\n", true);
          RUFile.writeFileContent(file, pageMarker.replaceAll("\\$\\{0\\}", "" + i) + "\r\n", true);
        }
        RUFile.writeFileContent(file, pageContent, true);
      }
      return file.getAbsolutePath();
    } catch (Throwable e) {
      throw new RFWCriticalException("Falha ao l�r o arquivo recebido!", new String[] { filePath }, e);
    }
  }

  /**
   * Copia um arquivo de um caminho para outro.
   *
   * @param source Caminho do arquivo de origem (arquivo)
   * @param target Caminho de destino
   * @param options Op��e de c�pia. Verifique as op��es de {@link StandardCopyOption}
   * @throws RFWException
   */
  public static void copyFile(String source, String target, CopyOption... options) throws RFWException {
    copyFile(new File(source), new File(target), options);
  }

  /**
   * Copia um arquivo de um caminho para outro.
   *
   * @param source Caminho do arquivo de origem (arquivo)
   * @param target Caminho de destino
   * @param options Op��e de c�pia. Verifique as op��es de {@link StandardCopyOption}
   * @throws RFWException
   */
  public static void copyFile(File source, File target, CopyOption... options) throws RFWException {
    copyFile(source.toPath(), target.toPath(), options);
  }

  /**
   * Copia um arquivo de um caminho para outro.
   *
   * @param source Caminho do arquivo de origem (arquivo)
   * @param target Caminho de destino
   * @param options Op��e de c�pia. Verifique as op��es de {@link StandardCopyOption}
   * @throws RFWException
   */
  public static void copyFile(Path source, Path target, CopyOption... options) throws RFWException {
    try {
      Files.copy(source, target, options);
    } catch (IOException e) {
      throw new RFWCriticalException("Falha o copiar arquivo.", e);
    }
  }

  /**
   * Tenta identificar o charset do arquivo para uma leitura mais correta.
   *
   * @param file Arquivo para identifica��o do charset.
   * @return Constante com o charset para leitura do arquivo, ou null caso a identifica��o tenha falhado.
   * @throws RFWException Lan�ado em casos de erro de leitura do arquivo, acesso, etc. N�o � lan�ado por n�o identificar o charset.
   */
  public static Charset detectCharset(File file) throws RFWException {
    try {
      String charset = UniversalDetector.detectCharset(file);
      if (charset != null) {
        if (charset.equals(Constants.CHARSET_ISO_8859_8) || charset.equals(Constants.CHARSET_ISO_8859_7) || charset.equals(Constants.CHARSET_ISO_8859_5)) {
          return StandardCharsets.ISO_8859_1;
        } else if (charset.equals(Constants.CHARSET_UTF_8)) {
          return StandardCharsets.UTF_8;
        } else if (charset.equals(Constants.CHARSET_UTF_16BE)) {
          return StandardCharsets.UTF_16BE;
        } else if (charset.equals(Constants.CHARSET_UTF_16LE)) {
          return StandardCharsets.UTF_16LE;
        } else if (charset.equals(Constants.CHARSET_US_ASCCI) || charset.equals("US-ASCII")) { // Aparentemente o UniversalDetector retorna um
          return StandardCharsets.US_ASCII;
        } else if (charset.equals(Constants.CHARSET_WINDOWS_1252)) {
          return Charsets.WINDOWS_1252;
        } else {
          RFWLogger.logImprovement("N�o foi poss�vel relacionar o charset detectado: '${0}'", new String[] { charset });
        }
      }
      return null;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao identificar o charset do arquivo!", e);
    }
  }

  /**
   * Cria um {@link BufferedReader} para l�r um arquivo. Tenta detectar o charset a ser utilizado atrav�s do m�todo {@link #detectCharset(File)}.<Br>
   * <br>
   * Sugest�o de utiliza��o:
   *
   * <pre>
   * try (BufferedReader r = BUFile.createFileBufferedReader(filePath)) {
   *   String line = null;
   *   while ((line = r.readLine()) != null) {
   *     // L�r o conte�do do arquivo
   *   }
   * }
   * </pre>
   *
   * @param filePath Caminho do arquivo para cria��o do {@link BufferedReader}.
   * @return {@link BufferedReader} pronto para a leitura do arquivo.
   * @throws RFWException
   */
  public static BufferedReader createFileBufferedReader(String filePath) throws RFWException {
    try {
      Charset charset = BUFile.detectCharset(new File(filePath));
      return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset));
    } catch (FileNotFoundException e) {
      throw new RFWCriticalException("Arquivo n�o encontrado!", e);
    }
  }

}
