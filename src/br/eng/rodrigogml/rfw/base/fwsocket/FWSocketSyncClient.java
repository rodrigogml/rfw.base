package br.eng.rodrigogml.rfw.base.fwsocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Classe do Framework que cria um socket e conecta em um FWSocketSyncServer abstrai a rotina de conex�o de um socket. Simplificando o acesso ao envio de dados e recebimento.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.0.0 (13/11/2014)
 */
public class FWSocketSyncClient implements Serializable {

  private static final long serialVersionUID = -2133834936147544361L;

  private final String host;
  private final int port;

  public FWSocketSyncClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  private Socket connect() throws Exception {
    InetAddress serverAddr = InetAddress.getByName(host);
    Socket clientsocket = new Socket(serverAddr, port);
    clientsocket.setKeepAlive(true);
    return clientsocket;
  }

  public synchronized FWSocketObjectMap sendData(FWSocketObjectMap map) throws RFWException {
    PrintStream writer = null;
    Socket clientsocket = null;
    InputStreamReader reader = null;
    FWSocketObjectMap response = null;
    try {
      // Conectamos para obter o socket
      clientsocket = connect();
      writer = new PrintStream(clientsocket.getOutputStream(), true, "ISO-8859-1");
      // Serializamos e enviamos o objeto
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final ObjectOutputStream oo = new ObjectOutputStream(out);
      oo.writeObject(map);
      oo.flush();
      oo.close();
      String serobj = out.toString("ISO-8859-1"); // Usa o charset ISO-8859-1 pois faz o mapeamento direto em byte, sem corrigir caracteres. Evita codificar a string para Base64 ou Hexa (que dependem de biblioteca externa)
      writer.print(serobj.length() + "|" + serobj + '\n');
      writer.flush();
      // Depois que enviou o dado, ficamos aguardando pela resposta
      reader = new InputStreamReader(clientsocket.getInputStream(), "ISO-8859-1");
      final StringBuilder cmdbuff = new StringBuilder(); // Guarda os pedoa�os dos comandos que chegarem
      int expectedlenght = -1;
      char[] c = new char[1024]; // Bytes a serem lidos por v�z
      int readbytes = -1;
      while ((readbytes = reader.read(c)) > 0) {
        // Colocamos o conte�do no buffer
        cmdbuff.append(c, 0, readbytes);
        // Verifica se pelo menos j� temos o primeiro pipe na string(o que indica que temos o tamanho completo do comando esperado, caso contr�rio vamos apenas anexando no buffer at� ter o m�nimo de informa�� necess�ria para processar
        final int firstpipe = cmdbuff.indexOf("|");
        if (firstpipe > 0) {
          // Recuperamos o tamanho esperado do comando - definido pelos n�meros entre o come�o do comando e o primeiro pipe
          expectedlenght = Integer.parseInt(cmdbuff.substring(0, firstpipe));
          // Interpretamos se o comando montado tem o tamanho esperado
          // >> Lembre-se que o tamanho descrito no come�o e o primeiro pipe, assim como o \n do final, n�o devem ser considerados para "bater" o tamanho esperado, pos isso devem ser descontados do tamanho do cmdbuffer (ou somados no tamanho do expected)
          final int fullexpected = expectedlenght + (firstpipe + 1) + 1; // Tamanho total entre o experado e o cabe�alho + '\n' do final
          if (cmdbuff.length() >= fullexpected) {
            String serialobj = cmdbuff.substring(firstpipe + 1, fullexpected - 1); // Pula o tamanho esperado e o primeiro pipe, e subtrai 1 do tamanho total para remover o '\n' do final
            // Limpamos do Buffer apenas a informa��o sendo processada agora. Em casos de receber um comando colado em outro, temos de manter no buffer os comandos ainda n�o processados
            cmdbuff.delete(0, fullexpected);
            // Desserializa o objeto
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialobj.getBytes("ISO-8859-1")));
            response = (FWSocketObjectMap) input.readObject();
            break;
          }
        }
      }
    } catch (RFWException e) {
      throw e;
    } catch (Exception e) {
      if ((e instanceof ConnectException) && "Connection refused: connect".equals(e.getMessage())) {
        throw new RFWValidationException("Falha ao conectar no servidor: ${0}", new String[] { e.getMessage() }, e);
      } else if ((e instanceof ConnectException) && "Connection timed out: connect".equals(e.getMessage())) {
        throw new RFWValidationException("Falha ao conectar no servidor: ${0}", new String[] { e.getMessage() }, e);
      } else if ((e instanceof SocketException) && "Connection reset".equals(e.getMessage())) {
        throw new RFWValidationException("Falha ao conectar no servidor: ${0}", new String[] { e.getMessage() }, e);
      } else {
        throw new RFWCriticalException("Falha ao conectar no servidor: ${0}", new String[] { e.getMessage() }, e);
      }
    } finally {
      if (writer != null) {
        writer.flush();
        writer.close();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
        }
      }
    }
    // Valida se a resposta recebida � um erro, se for, lan�amos ele como se fosse uma continua��o do "throw" do server
    if (response != null) {
      Exception ex = (Exception) response.get(FWSocketObjectMap.PROPERTY_EXCEPTION);
      if (ex != null) {
        if (ex instanceof RFWException) {
          throw (RFWException) ex;
        } else {
          throw new RFWCriticalException("Falha ao executar comando no servidor!", ex);
        }
      }
    }
    return response;
  }
}
