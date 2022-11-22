package br.eng.rodrigogml.rfw.base.fwsocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.base.fwsocket.listener.FWSocketAsyncServerListener;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;

/**
 * Description: Classe do FW quer permite "levantar um servidor" ou iniciar o client, através de conexões do tipo "full-duplex".<BR>
 * Um servidor deve ser levantado, o qual aguardará que uma conexão seja feita. Quem faz a conexão é chamado de client.<br>
 * Uma vez conectado se estabelece dois "canais" de conversão, um usado para enviar comandos, outro para receber comandos. Consequentemente os canais são assincronos, isto é, o comando é enviado mas como a respsota vêm em outro canal ela não é recebida na mesma thread do comando enviado.
 *
 * @author Rodrigo Leitão
 * @since 7.0.0 (13/11/2014)
 */
public class FWSocketAsync {

  /**
   * Socket usado quando a classe levanta um servidor para aguarda conexão (servidora).
   */
  private ServerSocket serversocket = null;

  /**
   * Socket usado quando a classe é usada para conectar-se a um servidor (client).
   */
  private Socket clientsocket = null;

  /**
   * Salva a referência do writer do socket, usada para enviar os comandos, quando a classe é usada como client.
   */
  private PrintStream clientwriter = null;

  /**
   * Hash utilizada para guardar os objetos writer de quando a classe é utilizada como server. Cada chave é um Long gerado durante o processo de criação do socket.
   */
  private HashMap<Long, PrintStream> clientwriterhash = new HashMap<>();

  /**
   * Endereço de host ou ip do servidor ao qual a classe deve se conectar.
   */
  private String host = null;

  /**
   * Número da porta que o servidor escutará, ou que o client se conectará.
   */
  private int port = -1;

  /**
   * Listener so FWSocket para tratamento dos eventos
   */
  private FWSocketAsyncServerListener listener = null;

  /**
   * Cria uma nova instância do {@link FWSocketAsync}.
   */
  public FWSocketAsync() {
  }

  /**
   * Este método para o listener completamente.<br>
   * <b>Atenção: este método não encerra as conexões existentes!!!</b>
   */
  public synchronized void stopServer() throws RFWException {
    if (this.serversocket != null) {
      if (!this.serversocket.isClosed()) {
        try {
          this.serversocket.close();
        } catch (Exception e) {
          throw new RFWCriticalException("Erro ao fechar o socket listener.");
        } finally {
          this.serversocket = null;
        }
      } else {
        this.serversocket = null;
      }
      this.host = null;
      this.port = -1;
      this.listener = null;
    }
  }

  /**
   * Este método inicializa o listener caso ele ainda não esteja operante, ou reinicia (finaliza o anterior e cria um novo) caso já exista algum em funcionamento.<br>
   *
   * @throws RFWException
   */
  public synchronized void startServer(int port, FWSocketAsyncServerListener listener) throws RFWException {
    if (clientsocket != null) {
      throw new RFWWarningException("Impossível criar servidor se a classe está sendo usada como client!");
    }

    this.host = null;
    this.port = port;
    this.listener = listener;

    // Faz o shutdown do socket anterior
    try {
      stopServer();
    } catch (Exception e) {
      // Loga a exceção mas não se importa em continuar o método pois este erro não deve atrapalhar o
      RFWLogger.logException(e);
    }
    // Cria e inicializa o novo socket
    try {
      serversocket = new ServerSocket(this.port);
      // Dispara thread que aceitará múltiplas instâncias de conexão
      Thread thread = new Thread() {
        @Override
        public void run() {
          serverSocketListenerThread();
        }
      };
      thread.setDaemon(false);
      thread.setName("FWSocket Async Listener");
      thread.start();
    } catch (IOException e) {
      throw new RFWCriticalException("Impossível inicializar o listener!", e);
    }
  }

  /**
   * Inicia a classe como cliente, conectando-se a algum servidor
   *
   * @param host Host ou IP de conexão do servidor.
   * @param port Porta de conexão do servidor.
   * @param listener Listener utilizado para receber os comandos vindos do servidor.
   * @throws RFWException
   */
  public synchronized void connect(String host, int port, FWSocketAsyncServerListener listener) throws RFWException {
    if (this.serversocket != null) {
      throw new RFWWarningException("Impossível conectar-se a um servidor quando a classe está sendo usada como server!");
    }

    this.host = host;
    this.port = port;
    this.listener = listener;

    // Desconecta alguma conexão anterior
    try {
      disconnect();
    } catch (Exception e) {
      // Loga a exceção mas não se importa em continuar o método pois este erro não deve atrapalhar o
      RFWLogger.logException(e);
    }

    try {
      // Cria e inicializa o socket cliente
      InetAddress serverAddr = InetAddress.getByName(host);
      this.clientsocket = new Socket(serverAddr, port);
      clientsocket.setKeepAlive(true);

      this.clientwriter = new PrintStream(clientsocket.getOutputStream(), true, "ISO-8859-1");
      FWSocketAsyncConnectionThread t = new FWSocketAsyncConnectionThread(null, clientsocket, this.listener, this);
      t.setDaemon(true);
      t.setName("FWSocket ClientListener");
      t.start();
    } catch (Exception e) {
      throw new RFWWarningException("Erro ao conectar: '${0}'", new String[] { e.getMessage() }, e);
    }
  }

  /**
   * Encerra a conexão com o servidor.
   *
   * @throws RFWException
   */
  public synchronized void disconnect() throws RFWException {
    if (this.clientsocket != null) {
      if (!this.clientsocket.isClosed()) {
        try {
          this.clientsocket.close();
        } catch (Exception e) {
          throw new RFWCriticalException("Erro ao fechar socket.");
        } finally {
          this.clientsocket = null;
        }
      } else {
        this.clientsocket = null;
      }
      this.host = null;
      this.port = -1;
      this.listener = null;
      if (this.clientwriter != null) {
        try {
          this.clientwriter.flush();
        } catch (Exception e) {
        }
        try {
          this.clientwriter.close();
        } catch (Exception e) {
        }
      }
    }
  }

  protected synchronized void serverSocketListenerThread() {
    while (!this.serversocket.isClosed() && this.serversocket.isBound()) {
      try {
        Socket clientsocket = serversocket.accept();
        // Gera identificador único para este client
        Long clientid = System.nanoTime();
        while (this.clientwriterhash.containsKey(clientid)) {
          clientid = System.nanoTime();
        }
        // Cria Thread Separada para gerenciar essa nvoa conexão e não atrapalhar o listener
        this.clientwriter = new PrintStream(clientsocket.getOutputStream(), true, "ISO-8859-1");
        FWSocketAsyncConnectionThread t = new FWSocketAsyncConnectionThread(clientid, clientsocket, this.listener, this);
        t.setDaemon(true);
        t.setName("FWSocket ClientListener");
        t.start();
      } catch (Exception e) {
        RFWLogger.logException(new RFWCriticalException("Erro ao aceitar a conexão do cliente.", e));
      }
    }
    // Ao fim da thread tenta fechar o socket
    try {
      serversocket.close();
    } catch (IOException e) {
      // Só loga o erro, mas não há muito interesse nesta exceção
      RFWLogger.logException(e);
    }
    RFWLogger.logDebug("[RichTerminalListener] Listener Thread Terminated!");
  }

  /**
   * Método usado para enviar dados para o servidor, quando esta classe é usada como client.
   *
   * @param properties Container a ser enviado.
   * @throws RFWException
   */
  public void sendDataToServer(FWSocketObjectMap properties) throws RFWException {
    if (clientwriter == null || clientsocket == null || !clientsocket.isConnected()) {
      throw new RFWValidationException("Cliente não conectado para enviar comando!");
    }
    if (properties != null) {
      try {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream oo = new ObjectOutputStream(out);
        oo.writeObject(properties);
        oo.flush();
        oo.reset();
        oo.close();
        final String serobj = out.toString("ISO-8859-1"); // Usa o charset ISO-8859-1 pois faz o mapeamento direto em byte, sem corrigir caracteres. Evita codificar a string para Base64 ou Hexa (que dependem de biblioteca externa)
        this.clientwriter.print(serobj.length() + "|" + serobj + '\n');
        this.clientwriter.flush();
      } catch (Exception e) {
        throw new RFWCriticalException("Erro ao trasferir dados pelo FWTalkerProtocol.");
      }
    }
  }

  /**
   * Método usado para enviar dados para algum client conectado, quando esta classe é usada como server.
   *
   * @param clientid Identificador do cliente.
   * @param properties container dos dados a serem enviados.
   *
   * @throws RFWException
   */
  public void sendDataToClient(Long clientid, FWSocketObjectMap properties) throws RFWException {
    final PrintStream writer = this.clientwriterhash.get(clientid);
    if (writer == null) {
      throw new RFWValidationException("Impossível encontra o writer para o client ID: ${0}!", new String[] { "" + clientid });
    }
    if (properties != null) {
      try {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream oo = new ObjectOutputStream(out);
        oo.writeObject(properties);
        oo.flush();
        oo.reset();
        oo.close();
        final String serobj = out.toString("ISO-8859-1"); // Usa o charset ISO-8859-1 pois faz o mapeamento direto em byte, sem corrigir caracteres. Evita codificar a string para Base64 ou Hexa (que dependem de biblioteca externa)
        writer.print(serobj.length() + "|" + serobj + '\n');
        writer.flush();
      } catch (Exception e) {
        throw new RFWCriticalException("Erro ao trasferir dados pelo FWTalkerProtocol.");
      }
    }
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

}

/**
 * Description: Thread utilizada para gerenciar as conexões de cada novo cliente que se conectar ao servidor.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.0.0 (13/11/2014)
 */
class FWSocketAsyncConnectionThread extends Thread {

  private final FWSocketAsyncServerListener listener;
  private final InputStreamReader reader;
  private final FWSocketAsync fwSocket;
  private final Long clientid;

  public FWSocketAsyncConnectionThread(Long clientid, Socket clientsocket, FWSocketAsyncServerListener listener, FWSocketAsync fwSocket) throws RFWCriticalException {
    this.clientid = clientid;
    this.listener = listener;
    this.fwSocket = fwSocket;
    try {
      this.reader = new InputStreamReader(clientsocket.getInputStream(), "ISO-8859-1");
    } catch (Exception e) {
      throw new RFWCriticalException("Erro ao iniciar streams de comunicação do FWSocket!");
    }
  }

  @Override
  public void run() {
    // Cria Leitor e Escritor no formato de String que manterá os bytes exatamente como precisamos através do socket, e assim nos permite trabalhar diretamente com strings.

    long processtime = 0L; // Mantém a hora em que começamos a processar o comando para calcular o tempo que levamos para enviar a resposta para o servidor. (nanotime)
    final StringBuilder cmdbuff = new StringBuilder(); // Guarda os pedoaços dos comandos que chegarem
    int expectedlenght = -1;
    char[] c = new char[1024]; // Bytes a serem lidos por vêz
    int readbytes = -1;

    // Lê o Input enquanto possível
    try {
      while ((readbytes = reader.read(c)) > 0) {
        // Colocamos o conteúdo no buffer
        cmdbuff.append(c, 0, readbytes);
        // Verifica se pelo menos já temos o primeiro pipe na string(o que indica que temos o tamanho completo do comando esperado, caso contrário vamos apenas anexando no buffer até ter o mínimo de informaçõ necessária para processar
        final int firstpipe = cmdbuff.indexOf("|");
        if (firstpipe > 0) {
          // Recuperamos o tamanho esperado do comando - definido pelos números entre o começo do comando e o primeiro pipe
          expectedlenght = Integer.parseInt(cmdbuff.substring(0, firstpipe));

          // Interpretamos se o comando montado tem o tamanho esperado
          // >> Lembre-se que o tamanho descrito no começo e o primeiro pipe, assim como o \n do final, não devem ser considerados para "bater" o tamanho esperado, pos isso devem ser descontados do tamanho do cmdbuffer (ou somados no tamanho do expected)
          final int fullexpected = expectedlenght + (firstpipe + 1) + 1; // Tamanho total entre o experado e o cabeçalho + '\n' do final
          if (cmdbuff.length() >= fullexpected) {
            String serialobj = cmdbuff.substring(firstpipe + 1, fullexpected - 1); // Pula o tamanho esperado e o primeiro pipe, e subtrai 1 do tamanho total para remover o '\n' do final
            // Desserializa o objeto
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialobj.getBytes("ISO-8859-1")));
            FWSocketObjectMap tcproperties = (FWSocketObjectMap) input.readObject();
            processtime = System.nanoTime();
            try {
              listener.received(clientid, tcproperties); // Processa o comando recebido do terminal e salva o retorno
            } catch (Exception e) {
              // Ignora qualquer exception vinda do listener para não matar o socket
            }
            // Verificamos o tempo que levou para termos a resposta pronta para ser enviada
            processtime -= System.nanoTime();
            if (processtime < -200000000) {
              RFWLogger.logDebug("Processamento Lento!!!! Tempo total de processamento: " + (-processtime));
            }
            // Finalizamos o loop agora que já respondemos
            break;
          }
        }
      }
    } catch (Exception e) {
    } finally {
      try {
        this.reader.close();
      } catch (Exception e) {
      }
      try {
        this.fwSocket.disconnect();
      } catch (RFWException e) {
      }
    }
  }
}