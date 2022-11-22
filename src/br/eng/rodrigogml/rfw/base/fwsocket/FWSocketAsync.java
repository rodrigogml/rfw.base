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
 * Description: Classe do FW quer permite "levantar um servidor" ou iniciar o client, atrav�s de conex�es do tipo "full-duplex".<BR>
 * Um servidor deve ser levantado, o qual aguardar� que uma conex�o seja feita. Quem faz a conex�o � chamado de client.<br>
 * Uma vez conectado se estabelece dois "canais" de convers�o, um usado para enviar comandos, outro para receber comandos. Consequentemente os canais s�o assincronos, isto �, o comando � enviado mas como a respsota v�m em outro canal ela n�o � recebida na mesma thread do comando enviado.
 *
 * @author Rodrigo Leit�o
 * @since 7.0.0 (13/11/2014)
 */
public class FWSocketAsync {

  /**
   * Socket usado quando a classe levanta um servidor para aguarda conex�o (servidora).
   */
  private ServerSocket serversocket = null;

  /**
   * Socket usado quando a classe � usada para conectar-se a um servidor (client).
   */
  private Socket clientsocket = null;

  /**
   * Salva a refer�ncia do writer do socket, usada para enviar os comandos, quando a classe � usada como client.
   */
  private PrintStream clientwriter = null;

  /**
   * Hash utilizada para guardar os objetos writer de quando a classe � utilizada como server. Cada chave � um Long gerado durante o processo de cria��o do socket.
   */
  private HashMap<Long, PrintStream> clientwriterhash = new HashMap<>();

  /**
   * Endere�o de host ou ip do servidor ao qual a classe deve se conectar.
   */
  private String host = null;

  /**
   * N�mero da porta que o servidor escutar�, ou que o client se conectar�.
   */
  private int port = -1;

  /**
   * Listener so FWSocket para tratamento dos eventos
   */
  private FWSocketAsyncServerListener listener = null;

  /**
   * Cria uma nova inst�ncia do {@link FWSocketAsync}.
   */
  public FWSocketAsync() {
  }

  /**
   * Este m�todo para o listener completamente.<br>
   * <b>Aten��o: este m�todo n�o encerra as conex�es existentes!!!</b>
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
   * Este m�todo inicializa o listener caso ele ainda n�o esteja operante, ou reinicia (finaliza o anterior e cria um novo) caso j� exista algum em funcionamento.<br>
   *
   * @throws RFWException
   */
  public synchronized void startServer(int port, FWSocketAsyncServerListener listener) throws RFWException {
    if (clientsocket != null) {
      throw new RFWWarningException("Imposs�vel criar servidor se a classe est� sendo usada como client!");
    }

    this.host = null;
    this.port = port;
    this.listener = listener;

    // Faz o shutdown do socket anterior
    try {
      stopServer();
    } catch (Exception e) {
      // Loga a exce��o mas n�o se importa em continuar o m�todo pois este erro n�o deve atrapalhar o
      RFWLogger.logException(e);
    }
    // Cria e inicializa o novo socket
    try {
      serversocket = new ServerSocket(this.port);
      // Dispara thread que aceitar� m�ltiplas inst�ncias de conex�o
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
      throw new RFWCriticalException("Imposs�vel inicializar o listener!", e);
    }
  }

  /**
   * Inicia a classe como cliente, conectando-se a algum servidor
   *
   * @param host Host ou IP de conex�o do servidor.
   * @param port Porta de conex�o do servidor.
   * @param listener Listener utilizado para receber os comandos vindos do servidor.
   * @throws RFWException
   */
  public synchronized void connect(String host, int port, FWSocketAsyncServerListener listener) throws RFWException {
    if (this.serversocket != null) {
      throw new RFWWarningException("Imposs�vel conectar-se a um servidor quando a classe est� sendo usada como server!");
    }

    this.host = host;
    this.port = port;
    this.listener = listener;

    // Desconecta alguma conex�o anterior
    try {
      disconnect();
    } catch (Exception e) {
      // Loga a exce��o mas n�o se importa em continuar o m�todo pois este erro n�o deve atrapalhar o
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
   * Encerra a conex�o com o servidor.
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
        // Gera identificador �nico para este client
        Long clientid = System.nanoTime();
        while (this.clientwriterhash.containsKey(clientid)) {
          clientid = System.nanoTime();
        }
        // Cria Thread Separada para gerenciar essa nvoa conex�o e n�o atrapalhar o listener
        this.clientwriter = new PrintStream(clientsocket.getOutputStream(), true, "ISO-8859-1");
        FWSocketAsyncConnectionThread t = new FWSocketAsyncConnectionThread(clientid, clientsocket, this.listener, this);
        t.setDaemon(true);
        t.setName("FWSocket ClientListener");
        t.start();
      } catch (Exception e) {
        RFWLogger.logException(new RFWCriticalException("Erro ao aceitar a conex�o do cliente.", e));
      }
    }
    // Ao fim da thread tenta fechar o socket
    try {
      serversocket.close();
    } catch (IOException e) {
      // S� loga o erro, mas n�o h� muito interesse nesta exce��o
      RFWLogger.logException(e);
    }
    RFWLogger.logDebug("[RichTerminalListener] Listener Thread Terminated!");
  }

  /**
   * M�todo usado para enviar dados para o servidor, quando esta classe � usada como client.
   *
   * @param properties Container a ser enviado.
   * @throws RFWException
   */
  public void sendDataToServer(FWSocketObjectMap properties) throws RFWException {
    if (clientwriter == null || clientsocket == null || !clientsocket.isConnected()) {
      throw new RFWValidationException("Cliente n�o conectado para enviar comando!");
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
   * M�todo usado para enviar dados para algum client conectado, quando esta classe � usada como server.
   *
   * @param clientid Identificador do cliente.
   * @param properties container dos dados a serem enviados.
   *
   * @throws RFWException
   */
  public void sendDataToClient(Long clientid, FWSocketObjectMap properties) throws RFWException {
    final PrintStream writer = this.clientwriterhash.get(clientid);
    if (writer == null) {
      throw new RFWValidationException("Imposs�vel encontra o writer para o client ID: ${0}!", new String[] { "" + clientid });
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
 * Description: Thread utilizada para gerenciar as conex�es de cada novo cliente que se conectar ao servidor.<BR>
 *
 * @author Rodrigo Leit�o
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
      throw new RFWCriticalException("Erro ao iniciar streams de comunica��o do FWSocket!");
    }
  }

  @Override
  public void run() {
    // Cria Leitor e Escritor no formato de String que manter� os bytes exatamente como precisamos atrav�s do socket, e assim nos permite trabalhar diretamente com strings.

    long processtime = 0L; // Mant�m a hora em que come�amos a processar o comando para calcular o tempo que levamos para enviar a resposta para o servidor. (nanotime)
    final StringBuilder cmdbuff = new StringBuilder(); // Guarda os pedoa�os dos comandos que chegarem
    int expectedlenght = -1;
    char[] c = new char[1024]; // Bytes a serem lidos por v�z
    int readbytes = -1;

    // L� o Input enquanto poss�vel
    try {
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
            // Desserializa o objeto
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialobj.getBytes("ISO-8859-1")));
            FWSocketObjectMap tcproperties = (FWSocketObjectMap) input.readObject();
            processtime = System.nanoTime();
            try {
              listener.received(clientid, tcproperties); // Processa o comando recebido do terminal e salva o retorno
            } catch (Exception e) {
              // Ignora qualquer exception vinda do listener para n�o matar o socket
            }
            // Verificamos o tempo que levou para termos a resposta pronta para ser enviada
            processtime -= System.nanoTime();
            if (processtime < -200000000) {
              RFWLogger.logDebug("Processamento Lento!!!! Tempo total de processamento: " + (-processtime));
            }
            // Finalizamos o loop agora que j� respondemos
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