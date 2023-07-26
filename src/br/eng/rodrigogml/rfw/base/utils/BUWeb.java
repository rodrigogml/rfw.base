package br.eng.rodrigogml.rfw.base.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobMonitor;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.dataformatters.LocaleConverter;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;

/**
 * Description: Classe utilit�ria com m�todos pertinentes a manipula��o de informa��o na WEB.<br>
 *
 * @author Rodrigo Leit�o
 * @since 7.4.0 (7 de set de 2017)
 */
public class BUWeb {

  /**
   * Construtor privado de classe utilit�ria
   */
  private BUWeb() {
  }

  /**
   * Este m�todo tem a fun��o de abrir uma conex�o http no endere�o desejado e retornar o conte�do recuperado em formato de String.<br>
   * Note que este m�todo funciona tando quando para pegar o conte�do de p�ginas quando para obter o conte�do de arquivos de texto. Arquivos bin�rios � melhor obter o conte�do atrav�s do m�todo {@link #getURLContent(String)}.
   *
   * @param address Endere�o HTTP a ser recuperado
   * @return Conte�do retornado atrav�z da conex�o em formato String.
   *
   */
  public static String getURLContentOnString(String address) throws RFWException {
    return new String(getURLContent(address));
  }

  /**
   * Este m�todo tem a fun��o de abrir uma conex�o http no endere�o desejado e retornar o conte�do recuperado em formato de String.<br>
   * Note que este m�todo funciona tando quando para pegar o conte�do de p�ginas quando para obter o conte�do de arquivos de texto. Arquivos bin�rios � melhor obter o conte�do atrav�s do m�todo {@link #getURLContent(String)}.
   *
   * @param address Endere�o HTTP a ser recuperado
   * @param charset
   * @return Conte�do retornado atrav�z da conex�o em formato String.
   *
   */
  public static String getURLContentOnString(String address, Charset charset) throws RFWException {
    return new String(getURLContent(address), charset);
  }

  /**
   * Este m�todo tem a fun��o de abrir uma conex�o http no endere�o desejado e retornar o conte�do recuperado em byte array.<br>
   * Este m�todo funciona melhor para baixar arquivos ou conte�do bin�rio. Para baixar conte�do de p�ginas e arquivos de texto experimente o {@link #getURLContentOnString(String)}
   *
   * @param address Endere�o HTTP a ser recuperado
   * @return Conte�do retornado atrav�z da conex�o em um byte Array.
   */
  public static byte[] getURLContent(String address) throws RFWException {
    // try {//address = "http://images.creativeink.com.br/BL000322-2-VM_900.jpg";
    // URL url = new URL(address);
    // URLConnection conn = url.openConnection();
    // conn.setConnectTimeout(5000);
    // conn.setReadTimeout(5000);
    // conn.connect();
    // try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    // byte dataBuffer[] = new byte[1024];
    // int bytesRead;
    // while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
    // out.write(dataBuffer, 0, bytesRead);
    // }
    // return out.toByteArray();
    // } catch (IOException e) {
    // throw new RFWCriticalException("Falha ao realizar a conxe�o ou l�r os dados da mesma.", new String[] { address }, e);
    // }
    // } catch (IOException e1) {
    // throw new RFWCriticalException("URL inv�lida para download!", new String[] { address }, e1);
    // }

    try {
      int tries = 0;
      while (tries < 100) { // Evita um loop infinito de tentativas, incluindo redirecionamentos
        URL obj = new URL(address);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(5000); // Define um timeout de conex�o para n�o esperar para sempre
        con.setReadTimeout(5000); // Define um timeout de espera por resposta para n�o esperar para sempre
        con.setRequestMethod("GET"); // normalmente n�o � necess�rio pois GET � o padr�o
        con.setRequestProperty("User-Agent", "Mozilla/5.0"); // Configura��es da requisi��o

        int responseCode = con.getResponseCode(); // Recupera o c�digo de retorno
        RFWLogger.logDebug("Response Code : " + responseCode);
        if (responseCode == 302) { // 302 Movido Temporariamente - https://pt.wikipedia.org/wiki/HTTP_302
          address = con.getHeaderField("Location");
          RFWLogger.logDebug("Redirecionando requisi��o para o endere�o: " + address);
        } else if (responseCode == 200) { // 200 OK
          try (BufferedInputStream in = new BufferedInputStream(con.getInputStream()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
              out.write(dataBuffer, 0, bytesRead);
            }
            return out.toByteArray();
          } catch (IOException e) {
            throw new RFWCriticalException("Falha ao realizar a conxe�o ou l�r os dados da mesma.", new String[] { address }, e);
          }
        } else {
          throw new RFWCriticalException("Recebido do endere�o '" + address + "' o c�digo '" + responseCode + "', que � desconhecido pelo RFWDeprec!");
        }
        tries++;
      }
      throw new RFWWarningException("N�o foi poss�vel recuperar o conte�do da URL: " + address);
    } catch (MalformedURLException e) {
      throw new RFWCriticalException("URL inv�lida para download!", new String[] { address }, e);
    } catch (ProtocolException e) {
      throw new RFWCriticalException("Falha de protocolo para baixar o conte�do!", new String[] { address }, e);
    } catch (SocketTimeoutException e) {
      throw new RFWCriticalException("Expirado o tempo de espera (timeout) de retorno do site.", new String[] { address }, e);
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao l�r o conte�do do site!", new String[] { address }, e);
    }

  }

  /**
   * Este m�todo tem a fun��o de abrir uma conex�o http no endere�o desejado e retornar o conte�do recuperado para um arquivo tempor�rio.<br>
   * Este m�todo funciona melhor para baixar arquivos ou conte�do bin�rio. Para baixar conte�do de p�ginas e arquivos de texto experimente o {@link #getURLContentOnString(String)}<br>
   * <br>
   * <b>ATEN��O:</B> Este m�todo permite conectar por HTTPS, no entando ele n�o valida se o certificado recebido � v�lido e/ou confi�vel. Simplesmente aceita.
   *
   * @param address Endere�o HTTP a ser recuperado
   * @param maxSizeBytesUpload Tamanho m�ximo do arquivo para ser baixado. Se o arquivo for maior que o valor em bytes passado aqui, lan�amos exce��o de valida��o. Para deixar sem limite passe nulo. Note que se n�o obtivermos o tamanho do arquivo antes, a exception ser� lan�ada s� depois de baixar a quantidade de bytes passada aqui.
   * @param jobStatus Suprote para o servi�o {@link JobMonitor}.
   * @return Conte�do retornado atrav�z da conex�o em um byte Array.
   */
  public static File getURLContentOnTemporaryFile(String address, Long maxSizeBytesUpload, JobStatus jobStatus) throws RFWException {
    try {
      int tries = 0;
      while (tries < 100) { // Evita um loop infinito de tentativas, incluindo redirecionamentos
        if (jobStatus != null) {
          jobStatus.setProgressMessage("Abrindo Conex�o...");
          jobStatus.checkInterrupt();
        }

        URL obj = new URL(address);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        if (con instanceof HttpsURLConnection) {
          // Permite o override de aceita��o dos certificados conhecidos pelo RFWDeprec
          BUCert.configureSSLCertificatesOnConnection((HttpsURLConnection) con);
        }
        con.setConnectTimeout(5000); // Define um timeout de conex�o para n�o esperar para sempre
        con.setReadTimeout(5000); // Define um timeout de espera por resposta para n�o esperar para sempre
        con.setRequestMethod("GET"); // normalmente n�o � necess�rio pois GET � o padr�o
        con.setRequestProperty("User-Agent", "Mozilla/5.0"); // Configura��es da requisi��o

        int responseCode = con.getResponseCode(); // Recupera o c�digo de retorno
        if (responseCode == 302) { // 302 Movido Temporariamente - https://pt.wikipedia.org/wiki/HTTP_302
          address = con.getHeaderField("Location");
          if (jobStatus != null) jobStatus.setProgressMessage("Conte�do Redirecionado para o endere�o: " + address);
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
          }
        } else if (responseCode == 200) { // 200 OK
          if (jobStatus != null) jobStatus.setProgressMessage("Conte�do Encontrado!");

          String disposition = con.getHeaderField("Content-Disposition");
          // String contentType = con.getContentType();
          int contentLength = con.getContentLength();

          if (contentLength > 0) {
            if (maxSizeBytesUpload != null && contentLength > maxSizeBytesUpload) new RFWValidationException("O arquivo n�o pode ser baixado por que � maior que o m�ximo permitido: " + LocaleConverter.formatBytesSize(maxSizeBytesUpload, RFW.getLocale(), 1) + ".");
            if (jobStatus != null) {
              jobStatus.setIndeterminate(false);
              jobStatus.setParam("size", contentLength);
            }
          }

          // Tenta retirar o nome do arquivo do Header...
          String fileName = null;
          if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) fileName = disposition.substring(index + 10, disposition.length() - 1);
          }
          // Tenta tirar da URL
          if (fileName == null) fileName = address.substring(address.lastIndexOf("/") + 1, address.length());
          // Se n�o deixa no nome padr�o
          if (fileName == null) fileName = "downloadedFile";
          if (jobStatus != null) jobStatus.setParam("filename", fileName);

          File tmpFile = BUFile.createFileInTemporaryPath(fileName);
          long totalRead = 0;
          if (jobStatus != null) jobStatus.setProgressMessage("Baixando...");
          try (InputStream in = con.getInputStream(); FileOutputStream out = new FileOutputStream(tmpFile)) {
            byte dataBuffer[] = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
              out.write(dataBuffer, 0, bytesRead);
              totalRead += bytesRead;

              if (jobStatus != null) {
                jobStatus.checkInterrupt();
                if (contentLength > 0) {
                  jobStatus.setProgress((double) totalRead / contentLength);
                }
              }
              if (contentLength <= 0) {
                // Se for maior que zero j� foi validado, n�o precisamos ficar revalidando
                if (maxSizeBytesUpload != null && totalRead > maxSizeBytesUpload) throw new RFWValidationException("O arquivo n�o pode ser baixado por que � maior que o m�ximo permitido: " + LocaleConverter.formatBytesSize(maxSizeBytesUpload, RFW.getLocale(), 1) + ".");
              }
            }
            if (jobStatus != null) jobStatus.setProgressMessage("Finalizado!");
            return tmpFile;
          } catch (IOException e) {
            throw new RFWCriticalException("Falha ao realizar a conxe�o ou l�r os dados da mesma.", new String[] { address }, e);
          }
        } else {
          throw new RFWCriticalException("Recebido do endere�o '" + address + "' o c�digo '" + responseCode + "', que � desconhecido pelo RFWDeprec!");
        }
        tries++;
      }
      throw new RFWWarningException("N�o foi poss�vel recuperar o conte�do da URL: " + address);
    } catch (

    MalformedURLException e) {
      throw new RFWCriticalException("URL inv�lida para download!", new String[] { address }, e);
    } catch (ProtocolException e) {
      throw new RFWCriticalException("Falha de protocolo para baixar o conte�do!", new String[] { address }, e);
    } catch (SocketTimeoutException e) {
      throw new RFWCriticalException("Expirado o tempo de espera (timeout) de retorno do site.", new String[] { address }, e);
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao l�r o conte�do do site!", new String[] { address }, e);
    }
  }

}
