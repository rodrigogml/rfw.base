package br.eng.rodrigogml.rfw.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;

/**
 * Description: Classe utilit�ria com m�todos de IO.<br>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (8 de mai de 2019)
 */
public class BUIO {

  /**
   * Construtor privado para classe utilit�ria.
   */
  private BUIO() {
  }

  /**
   * Escreve o conte�do lido de um InputStream em um OutputStream.<Br>
   * Este m�todo invoca o .close() de ambos os streams ao finalizar a c�pia.
   *
   * @param in InputStream para leitura dos dados.
   * @param out OutputStream para escrita dos dados.
   * @param closeStreams se true, o m�todo chama as fun��es .close() dos streams, caso false os streams s�o deixados em aberto ap�s a c�pia. �til quando vamos compiar multiplos conte�dos ou se os streams j� foram criandos dentro de blocos try() que executam o fechamento.
   * @throws RFWException Lan�ado caso n�o seja poss�vel manipular algum dos Streams.
   */
  public static void copy(InputStream in, OutputStream out, boolean closeStreams) throws RFWException {
    try {
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao copiar conte�do do InputStream para o OutputStream.", e);
    } finally {
      try {
        if (closeStreams) out.close();
      } catch (IOException e) {
      }
      try {
        if (closeStreams) in.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Este m�todo lan�a uma execu��o java baseada na estrutura atual (se estamos em um jar ou executando classes em pasta aberta como nos momentos de development), e lan�a uma nova execu��o.<br>
   * Note que este m�todo N�O FOR�A A FINALIZA��O DESTA APLICA��O, algo como System.exit(0). Isso pq depois que este m�todo � chamado pode ser necess�rio ainda executar algum processo de finaliza��o. <br>
   * <br>
   * ATEN��O ESTE M�TODO PRECISA DE MAIS ATEN��O E ALGUNS REPAROS... N�O FUNCIONA BEM TODOS OS CEN�RIOS, Ex; execu��o em JAR, execu��o dentor e fora do eclipse, etc...
   *
   * @param executableClass
   * @throws RFWException
   */
  public static void restartApplication(Class<?> executableClass) throws RFWException {
    try {
      final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
      final File currentJar = new File(executableClass.getProtectionDomain().getCodeSource().getLocation().toURI());

      if (currentJar.getName().endsWith(".jar")) {
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
      } else {
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-classpath");
        String classPath = currentJar.getPath();
        if (RFW.isDevelopmentEnvironment()) {
          // Inclu�mos a pasta de libs na pasta anterior a de classes, que de forma geral dos projetos do RFW para aplica��es, � onde os jars de depend�ncias s�o copiados
          classPath += File.pathSeparator + currentJar.getPath() + File.separator + ".." + File.separator + "libs" + File.separator + "*";
        }
        command.add(classPath);
        command.add(executableClass.getCanonicalName());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
      }
    } catch (Throwable t) {
      throw new RFWCriticalException("Falha ao montar o comando para reiniciar a aplica��o.");
    }
  }

  /**
   * L� o conte�do de um inputStream para um array de Bytes.
   *
   * @param in InputStream pronto para ser lido
   * @return array de bytes com o conte�do lido do InputStream
   * @throws RFWException
   */
  public static byte[] readToByteArray(InputStream in) throws RFWException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] tmpbytes = new byte[4096];
      int c = 0;
      while ((c = in.read(tmpbytes)) > 0) {
        out.write(tmpbytes, 0, c);
      }
      out.flush();
      return out.toByteArray();
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao l�r o conte�do do InputStream!", e);
    }
  }

  /**
   * L� o conte�do de um InputStream para uma String utilizando o Charset UTF_8.
   *
   * @param in Stream com o conte�do para ser lido.
   * @return String montada com os Bytes do InputStream convertidos utilizadno o UTF_8.
   * @throws RFWException
   */
  public static String readToString(InputStream in) throws RFWException {
    return new String(BUIO.readToByteArray(in), StandardCharsets.UTF_8);
  }

  /**
   * L� o conte�do de um InputStream para uma String utilizando o Charset desejado.
   *
   * @param in Stream com o conte�do para ser lido.
   * @param charset que ser� utilizado para converter os bytes em caracteres.
   * @return String montada com os Bytes do InputStream convertidos utilizadno o Charset passado.
   * @throws RFWException
   */
  public static String readToString(InputStream in, Charset charset) throws RFWException {
    return new String(BUIO.readToByteArray(in), charset);
  }
}
