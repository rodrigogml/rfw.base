package br.eng.rodrigogml.rfw.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
import java.util.Timer;
import java.util.TimerTask;

import org.apache.pdfbox.util.Charsets;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import br.eng.rodrigogml.rfw.base.dataformatters.LocaleConverter;
import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;

/**
 * Description: Utilitários para gerenciar arquivos, escrever, ler, etc.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.1.0 (NOV / 2009)
 */

public class BUFile {

  public static boolean isDirectory(String path) {
    return new File(path).isDirectory();
  }

  public static boolean isDirectory(File path) {
    return path.isDirectory();
  }

  public static String[] getFileNamesFromDirectory(String path) {
    File[] listOfFiles = getFilesFromDirectory(path);

    ArrayList<String> files = new ArrayList<>(listOfFiles.length);

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        files.add(listOfFiles[i].getAbsolutePath());
      }
    }

    return files.toArray(new String[0]);
  }

  /**
   * Retorna os arquivos de um determinado diretório que tenham a data de criação maiores ou iguais a uma determinada data.
   *
   * @param path Caminho/Diretório dos arquivos
   * @param timemillis Data de criação inicial em milisegundos (Equivalente ao System.currentTimemillis)
   * @return Arquivos criados depois da data definida
   * @throws RFWException
   */
  public static File[] getFilesNewerOrEqualThan(String path, long timemillis) throws RFWException {
    final File[] files = getFilesFromDirectory(path);

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

  public static File[] getFilesFromDirectory(String path) {
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    return listOfFiles;
  }

  public static String[] getDirectoryFromDirectory(String path) {
    File[] listOfFiles = getFilesFromDirectory(path);

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

  public static boolean fileExists(String filename) {
    return fileExists(new File(filename));
  }

  public static boolean fileExists(File file) {
    return file.exists();
  }

  public static String readFileContentToString(String filename) throws RFWException {
    File file = new File(filename);
    return readFileContentToString(file);
  }

  public static String readFileContentToString(File file) throws RFWException {
    return new String(readFileContent(file));
  }

  public static String readFileContentToString(String filename, String charset) throws RFWException {
    File file = new File(filename);
    return readFileContentToString(file, charset);
  }

  public static String readFileContentToString(String filename, Charset charset) throws RFWException {
    File file = new File(filename);
    return readFileContentToString(file, charset);
  }

  public static String readFileContentToString(File file, Charset charset) throws RFWException {
    return new String(readFileContent(file), charset);
  }

  public static String readFileContentToString(File file, String charset) throws RFWException {
    try {
      return new String(readFileContent(file), charset);
    } catch (UnsupportedEncodingException e) {
      throw new RFWCriticalException("RFW_ERR_200332");
    }
  }

  public static byte[] readFileContent(String filename) throws RFWException {
    File file = new File(filename);
    return readFileContent(file);
  }

  public static byte[] readFileContent(File file) throws RFWException {
    if (!file.exists()) {
      throw new RFWValidationException("RFW_ERR_200059", new String[] { file.getPath() });
    }
    if (!file.canRead()) {
      throw new RFWValidationException("RFW_ERR_200060", new String[] { file.getPath() });
    }
    FileInputStream fileinput = null;
    try {
      fileinput = new FileInputStream(file);
      if (file.length() > Integer.MAX_VALUE) {
        throw new RFWValidationException("RFW_ERR_200063", new String[] { LocaleConverter.formatBytesSize(file.length(), null, 1) });
      }
      return BUIO.readToByteArray(fileinput);
    } catch (FileNotFoundException e) {
      throw new RFWValidationException("RFW_ERR_200061", new String[] { file.getPath() });
    } finally {
      if (fileinput != null) {
        try {
          fileinput.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Escreve/Anexa o conteúdo de texto em um arquivo utilizando o {@link StandardCharsets#UTF_8}
   *
   * @param fileName Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo
   * @param append Indica se devemos anexar o conteúdo no conteúdo já existente do arquivo (true) ou se devemos sobreescrever o conteúdo atual (false).
   * @param charset Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @throws RFWException
   */
  public static void writeFileContent(String filename, String filecontent, boolean append) throws RFWException {
    writeFileContent(new File(filename), filecontent, append, StandardCharsets.UTF_8);
  }

  /**
   * Escreve/Anexa o conteúdo de texto em um arquivo.
   *
   * @param fileName Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo
   * @param append Indica se devemos anexar o conteúdo no conteúdo já existente do arquivo (true) ou se devemos sobreescrever o conteúdo atual (false).
   * @param charset Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @throws RFWException
   */
  public static void writeFileContent(String fileName, String filecontent, boolean append, Charset charset) throws RFWException {
    writeFileContent(new File(fileName), filecontent, append, charset);
  }

  public static void writeFileContent(String filename, String filecontent) throws RFWException {
    writeFileContent(new File(filename), filecontent);
  }

  public static void writeFileContent(String filename, byte[] filecontent) throws RFWException {
    writeFileContent(filename, filecontent, false);
  }

  public static void writeFileContent(String filename, byte[] filecontent, boolean append) throws RFWException {
    FileOutputStream o = null;
    try {
      o = new FileOutputStream(filename, append);
    } catch (IOException ex) {
      throw new RFWWarningException("Não foi possível abrir o aquivo para escrita: '${0}'", new String[] { filename }, ex);
    }
    try {
      o.write(filecontent);
    } catch (IOException ex) {
      throw new RFWWarningException("RFWERP_000065", new String[] { filename });
    } finally {
      if (o != null) {
        try {
          o.flush();
          o.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Escreve o conteúdo de texto em um arquivo existente utilizando {@link StandardCharsets#UTF_8}.
   *
   * @param file Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo
   * @throws RFWException
   */
  public static void writeFileContent(File file, String filecontent) throws RFWException {
    writeFileContent(file, filecontent, false, StandardCharsets.UTF_8);
  }

  /**
   * Escreve o conteúdo de texto em um arquivo existente.
   *
   * @param file Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo
   * @param charset Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @throws RFWException
   */
  public static void writeFileContent(File file, String filecontent, Charset charset) throws RFWException {
    writeFileContent(file, filecontent, false, charset);
  }

  /**
   * Escreve o conteúdo de texto em um arquivo.
   *
   * @param file Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo
   * @param append Indica se devemos anexar o conteúdo no conteúdo já existente do arquivo (true) ou se devemos sobreescrever o conteúdo atual (false).
   * @param charset Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @throws RFWException
   */
  public static void writeFileContent(File file, String fileContent, boolean append, Charset charset) throws RFWException {
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, append), charset)) {
      writer.write(fileContent);
    } catch (Throwable e) {
      throw new RFWWarningException("Não foi possível abrir o aquivo para escrita: '${0}'", new String[] { file.getAbsolutePath() }, e);
    }
  }

  /**
   * Escreve o conteúdo de texto em um arquivo utilizando o {@link StandardCharsets#UTF_8}.
   *
   * @param file Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo.
   * @param append Indica se devemos anexar o conteúdo no conteúdo já existente do arquivo (true) ou se devemos sobreescrever o conteúdo atual (false).
   * @throws RFWException
   */
  public static void writeFileContent(File file, String fileContent, boolean append) throws RFWException {
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8)) {
      writer.write(fileContent);
    } catch (Throwable e) {
      throw new RFWWarningException("Não foi possível abrir o aquivo para escrita: '${0}'", new String[] { file.getAbsolutePath() }, e);
    }
  }

  /**
   * Escreve o conteúdo de texto em um arquivo de forma binária.
   *
   * @param file Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo.
   * @param append Indica se devemos anexar o conteúdo no conteúdo já existente do arquivo (true) ou se devemos sobreescrever o conteúdo atual (false).
   * @throws RFWException
   */
  public static void writeFileContent(File file, byte[] fileContent, boolean append) throws RFWException {
    try (FileOutputStream writer = new FileOutputStream(file, append)) {
      writer.write(fileContent);
    } catch (Throwable e) {
      throw new RFWWarningException("Não foi possível abrir o aquivo para escrita: '${0}'", new String[] { file.getAbsolutePath() }, e);
    }
  }

  /**
   * Escreve o conteúdo de texto em um arquivo de forma binária.
   *
   * @param file Arquivo para Escrita
   * @param fileContent Conteúdo a ser escrito dentro do arquivo.
   * @throws RFWException
   */
  public static void writeFileContent(File file, byte[] fileContent) throws RFWException {
    try (FileOutputStream writer = new FileOutputStream(file)) {
      writer.write(fileContent);
    } catch (Throwable e) {
      throw new RFWWarningException("Não foi possível abrir o aquivo para escrita: '${0}'", new String[] { file.getAbsolutePath() }, e);
    }
  }

  /**
   * Escreve o conteúdo de um arquivo em uma pasta temporária com opção de exclusão.
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param fileContent Conteúdo a ser escrito dentro do arquivo.
   * @param delayToDelete define um tempo em milisegundos antes de excluir o arquivo. Se passado um valor negativo, o arquivo será excluindo quando a VM for finalizada (desde que não finalize abortando por erro ou fechamento forçado pelo SO). Se a aplicação finalizar antes do tempo passado, o arquivo é excluído no fechamento da aplicação.
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException
   */
  public static File writeFileContentInTemporaryPathWithDelete(String fileName, byte[] fileContent, long delayToDelete) throws RFWException {
    File file = BUFile.createFileInTemporaryPathWithDelete(fileName, delayToDelete); // Exclui em 5 minutos
    BUFile.writeFileContent(file, fileContent);
    return file;
  }

  /**
   * Renomeia um arquivo do sistema.
   *
   * @param original Caminho do arquvio a ser renomeado
   * @param destination Caminho de destino (e nome desejado) do arquivo. Mais informações {@link File#renameTo(File)}.
   * @return true caso tenha renomeado com sucesso, false caso contrário (não lança exception).
   */
  public static boolean renameFile(String original, String destination) {
    return renameFile(new File(original), new File(destination));
  }

  /**
   * Renomeia um arquivo do sistema.
   *
   * @param original Caminho do arquvio a ser renomeado
   * @param destination Caminho de destino (e nome desejado) do arquivo. Mais informações {@link File#renameTo(File)}.
   * @return true caso tenha renomeado com sucesso, false caso contrário (não lança exception).
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
   * Cria um arquivo temporário (na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();<br>
   * Utiliza o Charset {@link StandardCharsets#UTF_8}.
   *
   * @param filename Nome do Arquivo
   * @param extension Extensão do arquivo. Não passar o "." somente a extenção. Ex: "txt", "log", etc...
   * @param content Contúdo do arquivo.
   * @return Objeto File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createTemporaryFile(String filename, String extension, String content) throws RFWException {
    try {
      File temp = File.createTempFile(filename, "." + extension);
      if (content != null) BUFile.writeFileContent(temp, content, StandardCharsets.UTF_8);
      return temp;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao escrever o arquivo temporário!", new String[] { filename, extension }, e);
    }
  }

  /**
   * Cria um arquivo temporário (na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param filename Nome do Arquivo
   * @param extension Extensão do arquivo. Não passar o "." somente a extenção. Ex: "txt", "log", etc...
   * @param content Contúdo do arquivo.
   * @param charset Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @return Objeto File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createTemporaryFile(String filename, String extension, String content, Charset charset) throws RFWException {
    try {
      File temp = File.createTempFile(filename, "." + extension);
      if (content != null) BUFile.writeFileContent(temp, content, charset);
      return temp;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao escrever o arquivo temporário!", new String[] { filename, extension }, e);
    }
  }

  /**
   * Equivalente ao método {@link #createFileInTemporaryPath(String, String, Charset)} passando como charset {@link StandardCharsets#UTF_8} Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <b>A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPath(String fileName) throws RFWException {
    return createFileInTemporaryPath(fileName, null, StandardCharsets.UTF_8);
  }

  /**
   * Equivalente ao método {@link #createFileInTemporaryPath(String, String, Charset)} passando como charset {@link StandardCharsets#UTF_8} Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <br>
   * A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param delayToDelete define um tempo em milisegundos antes de excluir o arquivo. Se passado um valor negativo, o arquivo será excluindo quando a VM for finalizada (desde que não finalize abortando por erro ou fechamento forçado pelo SO). Se a aplicação finalizar antes do tempo passado, o arquivo é excluído no fechamento da aplicação.
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPathWithDelete(String fileName, long delayToDelete) throws RFWException {
    File file = createFileInTemporaryPath(fileName, null, StandardCharsets.UTF_8);
    if (delayToDelete >= 0) {
      Timer t = new Timer("BUFile Delete Temporary File", true);
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          file.delete();
        }
      }, delayToDelete);
    }
    file.deleteOnExit();
    return file;
  }

  /**
   * Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <b>A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param content Contúdo do arquivo a ser escrito, ou null caso não deseje efetivamente criar o arquivo ainda.
   * @param charSet Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @param delayToDelete define um tempo em milisegundos antes de excluir o arquivo. Se passado um valor negativo, o arquivo será excluindo quando a VM for finalizada (desde que não finalize abortando por erro ou fechamento forçado pelo SO). Se a aplicação finalizar antes do tempo passado, o arquivo é excluído no fechamento da aplicação.
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPathWithDelete(String fileName, String content, Charset charSet, long delayToDelete) throws RFWException {
    try {
      String mix = RUString.genString(4) + System.currentTimeMillis();
      Path path = Files.createTempDirectory(mix);
      String fullpath = path.toString();
      if (!File.separator.equals(fullpath.substring(fullpath.length() - 1, fullpath.length()))) {
        fullpath += File.separator;
      }
      File file = new File(fullpath + fileName);
      if (content != null) BUFile.writeFileContent(file, content, charSet);

      if (delayToDelete >= 0) {
        Timer t = new Timer("BUFile Delete Temporary File", true);
        t.schedule(new TimerTask() {
          @Override
          public void run() {
            file.delete();
          }
        }, delayToDelete);
      }
      file.deleteOnExit();

      return file;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao escrever o arquivo temporário!", new String[] { fileName }, e);
    }
  }

  /**
   * Equivalente ao método {@link #createFileInTemporaryPath(String, String, Charset)} passando como charset {@link StandardCharsets#UTF_8} Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <b>A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param charSet Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPath(String fileName, Charset charSet) throws RFWException {
    return createFileInTemporaryPath(fileName, null, charSet);
  }

  /**
   * Equivalente ao método {@link #createFileInTemporaryPath(String, String, Charset)} passando como charset {@link StandardCharsets#UTF_8} Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <b>A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param content Contúdo do arquivo a ser escrito, ou null caso não deseje efetivamente criar o arquivo ainda.
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPath(String fileName, String content) throws RFWException {
    return createFileInTemporaryPath(fileName, content, StandardCharsets.UTF_8);
  }

  /**
   * Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <b>A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param content Contúdo do arquivo a ser escrito, ou null caso não deseje efetivamente criar o arquivo ainda.
   * @param charSet Charset a ser utilizado na escrita do arquivo. {@link StandardCharsets}
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPath(String fileName, String content, Charset charSet) throws RFWException {
    try {
      String mix = RUString.genString(4) + System.currentTimeMillis();
      Path path = Files.createTempDirectory(mix);
      String fullpath = path.toString();
      if (!File.separator.equals(fullpath.substring(fullpath.length() - 1, fullpath.length()))) {
        fullpath += File.separator;
      }
      File temp = new File(fullpath + fileName);
      if (content != null) BUFile.writeFileContent(temp, content, charSet);
      return temp;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao escrever o arquivo temporário!", new String[] { fileName }, e);
    }
  }

  /**
   * Cria um arquivo em uma pasta temporária (em uma subpasta na pastas temporária de acordo com o sistema) e escreve o conteúdo no arquivo.<br>
   * <b>A diferença entre este método e o {@link #createTemporaryFile(String, String, String, Charset)} é que o nome do arquivo continuará sendo o que o usuário passou, só a pasta (caminho) é que receberá valores aleatórios para evitar arquivos com o mesmo nome. Para obter o caminho do arquivo utilize o File retornado e seus métodos. O caminho completo para o arquivo pode ser recuperado com
   * file.getAbsolutePath(); já o caminho incluindo o arquivo pode ser obtido com file.getAbsoluteFile();
   *
   * @param fileName Nome do Arquivo com a extensão. Ex.: "meuarquivo.txt"
   * @param content Contúdo do arquivo a ser escrito, ou null caso não deseje efetivamente criar o arquivo ainda.
   * @return File represetando o arquivo, usado para escrever o conteúdo.
   * @throws RFWException Em caso de falha durante a execução.
   */
  public static File createFileInTemporaryPath(String fileName, byte[] content) throws RFWException {
    try {
      String mix = RUString.genString(4) + System.currentTimeMillis();
      Path path = Files.createTempDirectory(mix);
      String fullpath = path.toString();
      if (!File.separator.equals(fullpath.substring(fullpath.length() - 1, fullpath.length()))) {
        fullpath += File.separator;
      }
      File temp = new File(fullpath + fileName);
      if (content != null) BUFile.writeFileContent(temp, content, false);
      return temp;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao escrever o arquivo temporário!", new String[] { fileName }, e);
    }
  }

  /**
   * Retorna a data e hora de criação de um arquivo.
   *
   * @param fileName Caminho completo ou relativo à aplicação que leve ao arquivo.
   * @return Objeto Date criado com o timezone padrão do sistema, que deve equivaler como do arquivo.
   * @throws RFWException
   */
  public static Date getCreationDate(String fileName) throws RFWException {
    // Se o arquivo não existir lança crítico, isso deve ser validado adequadamente fora da classe utilitária
    if (!fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName });

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
   * Retorna a data e hora do último acesso ao arquivo.
   *
   * @param fileName Caminho completo ou relativo à aplicação que leve ao arquivo.
   * @return Objeto Date criado com o timezone padrão do sistema, que deve equivaler como do arquivo.
   * @throws RFWException
   */
  public static Date getLastAccessTime(String fileName) throws RFWException {
    // Se o arquivo não existir lança crítico, isso deve ser validado adequadamente fora da classe utilitária
    if (!fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName });

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
   * Retorna a data e hora da última modificação do arquivo.
   *
   * @param fileName Caminho completo ou relativo à aplicação que leve ao arquivo.
   * @return Objeto Date criado com o timezone padrão do sistema, que deve equivaler como do arquivo.
   * @throws RFWException
   */
  public static Date getLastModifiedTime(String fileName) throws RFWException {
    // Se o arquivo não existir lança crítico, isso deve ser validado adequadamente fora da classe utilitária
    if (!fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName });

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
   * Retorna se o objeto do caminho passado é um Symbolic Link.
   *
   * @param fileName Caminho completo ou relativo à aplicação que leve ao arquivo.
   * @return Boolean indicando 'true' caso o objeto seja um SymLink, 'false' não seja.
   * @throws RFWException
   */
  public static boolean isSymbolicLink(String fileName) throws RFWException {
    return isSymbolicLink(new File(fileName));
  }

  /**
   * Retorna se o objeto do caminho passado é um Symbolic Link.
   *
   * @param fileName Caminho completo ou relativo à aplicação que leve ao arquivo.
   * @return Boolean indicando 'true' caso o objeto seja um SymLink, 'false' não seja.
   * @throws RFWException
   */
  public static boolean isSymbolicLink(File fileName) throws RFWException {
    // Se o arquivo não existir lança crítico, isso deve ser validado adequadamente fora da classe utilitária
    if (!fileExists(fileName)) throw new RFWCriticalException("RFW_ERR_200444", new String[] { fileName.getAbsolutePath() });

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
   * Este método verifica a existência do caminho e, caso não exista ainda, o cria.<br>
   * <b>Este método aceita o caminho do diretório sem o nome do arquivo. Caso tenha o caminho com o nome do arquivo utilize o {@link #createPathOfFile(String)}</b>
   *
   * @param pathName Caminho para o diretório a ser criado.
   * @throws RFWException
   */
  public static void createPath(String pathName) throws RFWException {
    File file = new File(pathName);
    file.mkdirs(); // Força criar os diretórios caso não existam
  }

  /**
   * Este método verifica a existência dos diretórios do caminho ddo arquivo passado, caso não exista ainda, o cria.<br>
   * <b>Este método aceita o caminho completo incluindo o nome do arquivo, e garante que sua pasta seja criada. Caso tenha apenas os nomes dos diretórios utilize o {@link #createPath(String)}</b>
   *
   * @param filePath Caminho completo do arquivo, cujo diretório deve ser criado.
   * @throws RFWException
   */
  public static void createPathOfFile(String filePath) throws RFWException {
    File file = new File(filePath);
    file.mkdirs(); // Força criar os diretórios caso não existam
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
   * Extrai o nome do arquivo de um caminho completo recebido. NÃO RETORNA A EXTENSÃO DO ARQUIVO!
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFileName(String file) {
    final String name = new File(file).getName();
    return name.substring(0, name.lastIndexOf('.'));
  }

  /**
   * Extrai o nome e extenção do arquivo de um caminho completo recebido.
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFileNameAndExtension(String file) {
    return new File(file).getName();
  }

  /**
   * Extrai o caminho (diretório) de um caminho completo recebido. NÃO RETORNA O ÚLTIMO SEPARADOR! Nem mesmo quando está na raiz: Windows = "c:", no Linux = "".
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFullPath(String file) {
    return file.substring(0, file.lastIndexOf(File.separatorChar));
  }

  /**
   * Extrai o nome do arquivo de um caminho completo recebido.
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFileExtension(File file) {
    return extractFileExtension(file.getAbsolutePath());
  }

  /**
   * Extrai o nome do arquivo de um caminho completo recebido.
   *
   * @param file Caminho com o nome do arquivo
   * @return
   */
  public static String extractFileExtension(String file) {
    return file.substring(file.lastIndexOf('.') + 1);
  }

  /**
   * Este método tenta descobrir o charset correto de um arquivo. Note para identificar o charset correto será testado um decoder de cada charset no arquivo, e alguns deles podem dar múltiplos positivos, o que não garante uma exatidão na detecção.<br>
   * Teste os seguintes charsets:
   * <li>StandardCharsets.ISO_8859_1
   * <li>StandardCharsets.UTF_8
   * <li>StandardCharsets.US_ASCII
   * <li>StandardCharsets.UTF_16<br>
   *
   * @param filePath Caminho para o Arquivo a ser detectado o charset.
   * @return Charset em que o conteúdo do arquivo foi validado, ou null caso nenhum Charset tenha sido identificado.
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
            // não faz nada, mantém o identified em false
            break;
          }
        }
      } catch (Exception e) {
        throw new RFWCriticalException("Falha ao lêr o conteúdo do arquivo!", e);
      }
    }
    return charset;
  }

  /**
   * Exclui todos os arquivos encontrados dentro de um diretório.<br>
   * Mesmo que o método {@link #deleteAllFilesFromPath(String, boolean)} com a opção de symLink em false.
   *
   * @param path Caminho/Diretório para se excluir os arquivos (note que o diretório não é excluído).
   * @throws RFWException Em caso de falhas
   */
  public static void deleteAllFilesFromPath(String path) throws RFWException {
    deleteAllFilesFromPath(path, false);
  }

  /**
   * Exclui todos os arquivos encontrados dentro de um diretório.
   *
   * @param path Caminho/Diretório para se excluir os arquivos (note que o diretório não é excluído).
   * @param deleteSymlinks se true, força a exclusão mesmo que seja um Symbolic Link e não um arquivo real.
   * @throws RFWException Em caso de falhas
   */
  public static void deleteAllFilesFromPath(String path, boolean deleteSymlinks) throws RFWException {
    File fPath = new File(path);
    if (!fPath.exists()) throw new RFWValidationException("O caminho '" + path + "' não existe!");
    if (!fPath.isDirectory()) throw new RFWValidationException("O caminho '" + path + "' não é um diretório válido!");
    if (!fPath.canWrite()) throw new RFWValidationException("Não temos permissão para escrever no diretório '" + path + "'!");

    for (File file : getFilesFromDirectory(path)) {
      if (deleteSymlinks || !isSymbolicLink(file)) deleteFile(file);
    }
  }

  /**
   * Abre um arquivo de PDF e tenta extrair o seu conteúdo de texto com uma estratégia simples de texto. Escreve o conteúdo em um arquivo temporário e retorna o caminho do arquivo.
   *
   * @param filePath caminho para o arquivo PDF a ser lido
   * @return Caminho para o arquivo temporário com o texto encontrado no PDF.
   * @throws RFWException
   */
  public static String scrapPDFText(String filePath) throws RFWException {
    return scrapPDFText(filePath, null);
  }

  /**
   * Abre um arquivo de PDF e tenta extrair o seu conteúdo de texto com uma estratégia simples de texto. Escreve o conteúdo em um arquivo temporário e retorna o caminho do arquivo.
   *
   * @param filePath caminho para o arquivo PDF a ser lido
   * @param pageMarker Caso passada algum valor, esse valor será escrito no começo de cada página. Se essa marcação contiver o texto "${0}", ele será substituído pelo número da página.
   * @return Caminho para o arquivo temporário com o texto encontrado no PDF.
   * @throws RFWException
   */
  public static String scrapPDFText(String filePath, String pageMarker) throws RFWException {
    try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(filePath))) {
      File file = BUFile.createTemporaryFile("pdfscrap", "txt", null);
      for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
        SimpleTextExtractionStrategy extract = new SimpleTextExtractionStrategy();
        String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), extract);
        if (pageMarker != null) {
          if (i > 1) BUFile.writeFileContent(file, "\r\n", true);
          BUFile.writeFileContent(file, pageMarker.replaceAll("\\$\\{0\\}", "" + i) + "\r\n", true);
        }
        BUFile.writeFileContent(file, pageContent, true);
      }
      return file.getAbsolutePath();
    } catch (Throwable e) {
      throw new RFWCriticalException("Falha ao lêr o arquivo recebido!", new String[] { filePath }, e);
    }
  }

  /**
   * Copia um arquivo de um caminho para outro.
   *
   * @param source Caminho do arquivo de origem (arquivo)
   * @param target Caminho de destino
   * @param options Opçõe de cópia. Verifique as opções de {@link StandardCopyOption}
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
   * @param options Opçõe de cópia. Verifique as opções de {@link StandardCopyOption}
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
   * @param options Opçõe de cópia. Verifique as opções de {@link StandardCopyOption}
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
   * @param file Arquivo para identificação do charset.
   * @return Constante com o charset para leitura do arquivo, ou null caso a identificação tenha falhado.
   * @throws RFWException Lançado em casos de erro de leitura do arquivo, acesso, etc. Não é lançado por não identificar o charset.
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
          RFWLogger.logImprovement("O arquivo BUFile não foi capaz de correlacionar o charset '" + charset + "'.");
        }
      }
      return null;
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao identificar o charset do arquivo!", e);
    }
  }

  /**
   * Cria um {@link BufferedReader} para lêr um arquivo. Tenta detectar o charset a ser utilizado através do método {@link #detectCharset(File)}.<Br>
   * <br>
   * Sugestão de utilização:
   *
   * <pre>
   * try (BufferedReader r = BUFile.createFileBufferedReader(filePath)) {
   *   String line = null;
   *   while ((line = r.readLine()) != null) {
   *     // Lêr o conteúdo do arquivo
   *   }
   * }
   * </pre>
   *
   * @param filePath Caminho do arquivo para criação do {@link BufferedReader}.
   * @return {@link BufferedReader} pronto para a leitura do arquivo.
   * @throws RFWException
   */
  public static BufferedReader createFileBufferedReader(String filePath) throws RFWException {
    try {
      Charset charset = BUFile.detectCharset(new File(filePath));
      return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset));
    } catch (FileNotFoundException e) {
      throw new RFWCriticalException("Arquivo não encontrado!", e);
    }
  }

}
