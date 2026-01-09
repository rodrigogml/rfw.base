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
 * Description: Classe do Framework que cria um socket e conecta em um FWSocketSyncServer abstrai a rotina de conexão de um socket. Simplificando o acesso ao envio de dados e recebimento.<BR>
 *
 * @author Rodrigo Leitão
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
      writer = new PrintStream(clientsocket.getOutputStream(), true, "UTF-8");
      // Serializamos e enviamos o objeto
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final ObjectOutputStream oo = new ObjectOutputStream(out);
      oo.writeObject(map);
      oo.flush();
      oo.close();
      String serobj = out.toString("UTF-8"); // Usa o charset ISO-8859-1 pois faz o mapeamento direto em byte, sem corrigir caracteres. Evita codificar a string para Base64 ou Hexa (que dependem de biblioteca externa)
      writer.print(serobj.length() + "|" + serobj + '\n');
      writer.flush();
      // Depois que enviou o dado, ficamos aguardando pela resposta
      reader = new InputStreamReader(clientsocket.getInputStream(), "UTF-8");
      final StringBuilder cmdbuff = new StringBuilder(); // Guarda os pedoaços dos comandos que chegarem
      int expectedlength = -1;
      char[] c = new char[1024]; // Bytes a serem lidos por vêz
      int readbytes = -1;
      while ((readbytes = reader.read(c)) > 0) {
        // Colocamos o conteúdo no buffer
        cmdbuff.append(c, 0, readbytes);
        // Verifica se pelo menos já temos o primeiro pipe na string(o que indica que temos o tamanho completo do comando esperado, caso contrário vamos apenas anexando no buffer até ter o mínimo de informaçõ necessária para processar
        final int firstpipe = cmdbuff.indexOf("|");
        if (firstpipe > 0) {
          // Recuperamos o tamanho esperado do comando - definido pelos números entre o começo do comando e o primeiro pipe
          expectedlength = Integer.parseInt(cmdbuff.substring(0, firstpipe));
          // Interpretamos se o comando montado tem o tamanho esperado
          // >> Lembre-se que o tamanho descrito no começo e o primeiro pipe, assim como o \n do final, não devem ser considerados para "bater" o tamanho esperado, pos isso devem ser descontados do tamanho do cmdbuffer (ou somados no tamanho do expected)
          final int fullexpected = expectedlength + (firstpipe + 1) + 1; // Tamanho total entre o experado e o cabeçalho + '\n' do final
          if (cmdbuff.length() >= fullexpected) {
            String serialobj = cmdbuff.substring(firstpipe + 1, fullexpected - 1); // Pula o tamanho esperado e o primeiro pipe, e subtrai 1 do tamanho total para remover o '\n' do final
            // Limpamos do Buffer apenas a informação sendo processada agora. Em casos de receber um comando colado em outro, temos de manter no buffer os comandos ainda não processados
            cmdbuff.delete(0, fullexpected);
            // Desserializa o objeto
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialobj.getBytes("UTF-8")));
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
    // Valida se a resposta recebida é um erro, se for, lançamos ele como se fosse uma continuação do "throw" do server
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
