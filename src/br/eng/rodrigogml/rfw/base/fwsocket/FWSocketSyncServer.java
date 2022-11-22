package br.eng.rodrigogml.rfw.base.fwsocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.fwsocket.listener.FWSocketSyncServerListener;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;

/**
 * Description: Classe do RFW quer permite "levantar um servidor" que aceita conex�es do tipo "requisi��o->resposta", similar a um servidor HTTP. Cada conex�o recebe uma resposta, resultado de um processamento e � finalizada.<BR>
 * Utiliza como base a transmiss�o de arquivos serializados encapsulado dentro do {@link FWSocketObjectMap}.
 *
 * @author Rodrigo Leit�o
 * @since 7.0.0 (13/11/2014)
 */
public class FWSocketSyncServer {

  /**
   * Socket de listener atual.
   */
  private ServerSocket serverSocket = null;

  /**
   * Flag que indica se o servidor est� em processo de finaliza��o, evita que LOG de erros que o socket emite enquanto est� sendo desligado
   */
  private boolean stopped = false;

  /**
   * N�mero da porta que o servidor escutar�.
   */
  private final int port;

  /**
   * Listener so FWSocket para tratamento dos eventos
   */
  private final FWSocketSyncServerListener listener;

  public FWSocketSyncServer(int port, FWSocketSyncServerListener listener) {
    this.port = port;
    this.listener = listener;
  }

  /**
   * Este m�todo para o listener completamente.<br>
   * <b>Aten��o: este m�todo n�o encerra as conex�es existentes!!!</b>
   */
  public synchronized void stopServer() throws RFWException {
    stopped = true;
    if (this.serverSocket != null) {
      if (!this.serverSocket.isClosed()) {
        try {
          this.serverSocket.close();
        } catch (Exception e) {
          throw new RFWCriticalException("Erro ao fechar o socket listener.");
        }
      }
    }
  }

  /**
   * Este m�todo Indica se o socket est� funcionando aguardo novas conex�es.
   *
   * @throws RFWException
   */
  public synchronized boolean isListening() throws RFWException {
    return this.serverSocket != null && !this.serverSocket.isClosed();
  }

  /**
   * Este m�todo inicializa o listener caso ele ainda n�o esteja operante, ou reinicia (finaliza o anterior e cria um novo) caso j� exista algum em funcionamento.<br>
   *
   * @throws RFWException
   */
  public synchronized void startServer() throws RFWException {
    // Faz o shutdown do socket anterior
    try {
      stopServer();
    } catch (Exception e) {
      // Loga a exce��o mas n�o se importa em continuar o m�todo pois este erro n�o deve atrapalhar o
      RFWLogger.logException(e);
    }
    // Cria e inicializa o novo socket
    stopped = false;
    try {
      serverSocket = new ServerSocket(this.port);
      // Dispara thread que aceitar� m�ltiplas inst�ncias de conex�o
      Thread thread = new Thread() {
        @Override
        public void run() {
          socketListenerThread();
        }
      };
      thread.setDaemon(false);
      thread.setName("FWSocket Sync Listener");
      thread.start();
    } catch (IOException e) {
      throw new RFWCriticalException("Imposs�vel inicializar o listener!", e);
    }
  }

  protected void socketListenerThread() {
    long startTime = System.currentTimeMillis(); // Tempo em que a Thread come�ou
    while (!this.serverSocket.isClosed() && this.serverSocket.isBound()) {
      try {
        // Define um time out para o accept, se ningu�m solicitar uma conex�o ser� lan�ada uma exception e aproveitamos para reiniciar o socket. �til para limpara a m�m�ria contra o BUG do socket do java, que cresce feito um gremiling
        serverSocket.setSoTimeout(60000); // Deixa um timeout de 1min para que de m�nuto em minuto fa�a a verifica��o se devemos reiniciar a thread ou n�o
        Socket clientsocket = serverSocket.accept();
        // Cria Thread Separada para gerenciar essa nvoa conex�o e n�o atrapalhar o listener
        FWSocketServerConnectionThread t = new FWSocketServerConnectionThread(clientsocket, this.listener);
        t.setDaemon(true);
        t.setName("### FWSocket Sync Client");
        t.start();
      } catch (SocketTimeoutException e) {
        // Se for exception de timeout E o socket ainda estiver OK E a thread j� estiver viva a mais de 12 Horas (12h x 60m x 60s x 1000ms = 43200000ms), forma�amos uma reinicializa��o dele para livrar mem�ria do BUG do socket que n�o para de crescer
        if (!this.serverSocket.isClosed() && this.serverSocket.isBound() && System.currentTimeMillis() - startTime > 43200000) {
          try {
            RFWLogger.logDebug("Restarting FWSocket Sync Listener");
            startServer();
            return; // Se reiniciamos, sa�mos dessa thread pois j� tem outra no lugar para ouvir o socket
          } catch (Exception e1) {
            RFWLogger.logInfo("N�o foi poss�vel reiniciar o Listener!");
            RFWLogger.logException(e1);
          }
        }
      } catch (Exception e) {
        if (!stopped) RFWLogger.logException(new RFWCriticalException("Erro ao aceitar a conex�o do cliente.", e));
      }
    }

    // Ao fim da thread tenta fechar o socket
    try {
      serverSocket.close();
    } catch (IOException e) {
      // S� loga o erro, mas n�o h� muito interesse nesta exce��o
      RFWLogger.logException(e);
    }
    RFWLogger.logDebug("FWSocket Sync Listener Thread Terminated!");
  }

}

/**
 * Description: Thread utilizada para gerenciar as conex�es de cada novo cliente que se conectar ao servidor.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.0.0 (13/11/2014)
 */
class FWSocketServerConnectionThread extends Thread {

  private final Socket clientsocket;
  private final FWSocketSyncServerListener listener;
  private final InputStreamReader reader;
  private final PrintStream writer;

  public FWSocketServerConnectionThread(Socket clientsocket, FWSocketSyncServerListener listener) throws RFWCriticalException {
    this.clientsocket = clientsocket;
    this.listener = listener;
    try {
      this.reader = new InputStreamReader(clientsocket.getInputStream(), "ISO-8859-1");
      this.writer = new PrintStream(clientsocket.getOutputStream(), true, "ISO-8859-1");
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
      this.clientsocket.setSoTimeout(300000); // 5Min - Se n�o tiver resposta em 5 minutos encerra a conex�o
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
              FWSocketObjectMap response = listener.received(tcproperties); // Processa o comando recebido do terminal e salva o retorno
              if (response != null) sendData(response);
            } catch (Exception e) {
              RFWLogger.logException(e);
              FWSocketObjectMap response = new FWSocketObjectMap(e);
              sendData(response);
            }
            // Verificamos o tempo que levou para termos a resposta pronta para ser enviada
            processtime -= System.nanoTime();
            if (processtime < -1000000000) {
              RFWLogger.logDebug("Processamento Lento!!!! Tempo total de processamento: " + (-processtime / 1000000) + "ms");
            }
            // Finalizamos o loop agora que j� respondemos
            break;
          }
        }
      }
    } catch (Exception e) {
      RFWLogger.logException(e);
      FWSocketObjectMap response = new FWSocketObjectMap(e);
      try {
        sendData(response);
      } catch (RFWException e1) {
        RFWLogger.logException(e1);
      }
    } finally {
      try {
        // Finaliza os recursos
        this.writer.flush();
        this.writer.close();
      } catch (Exception e) {
        RFWLogger.logException(e);
      }
      try {
        this.reader.close();
      } catch (IOException e) {
        RFWLogger.logException(e);
      }
      try {
        this.clientsocket.close();
      } catch (IOException e) {
        RFWLogger.logException(e);
      }
    }
  }

  private void sendData(FWSocketObjectMap properties) throws RFWException {
    if (properties != null) {
      try {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream oo = new ObjectOutputStream(out);
        oo.writeObject(properties);
        oo.flush();
        oo.reset();
        oo.close();
        final String serobj = out.toString("ISO-8859-1"); // Usa o charset ISO-8859-1 pois faz o mapeamento direto em byte, sem corrigir caracteres. Evita codificar a string para Base64 ou Hexa (que dependem de biblioteca externa)
        this.writer.print(serobj.length() + "|" + serobj + '\n');
        this.writer.flush();
      } catch (Exception e) {
        throw new RFWCriticalException("Erro ao trasferir dados pelo FWTalkerProtocol.");
      }
    }
  }
}