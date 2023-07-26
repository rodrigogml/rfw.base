package br.eng.rodrigogml.rfw.base.sessionmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionBackOperation;
import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionVO;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;
import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;

/**
 * Description: Classe utilizada para gerenciar as sessões de usuários através da Thread.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (13 de jul de 2018)
 */
public class SessionManager {

  /**
   * Determina se o SessionManager foi chamado para ser finalizado.
   */
  private static Boolean shutingdown = false;

  /**
   * Referência para a Thread do SessionManager.
   */
  private static Thread controlThread = null;

  /**
   * Define um prefixo que será utilizado para identificar quando a String de acesso não é o UUID da sessão, mas sim um Token utilizado para criar a sessão de acesso de uma "máquina".<br>
   * Note:
   * <li>que se este valor não for definido, o SessionManager não permitirá criar autenticação por máquina;
   * <li>Para inutilizar o acesso ao sistema via máquina defina esse atributo como "";
   * <li>Por questão de segurança esse valor só pode ser definido uma única vez.
   */
  private static String tokenPrefix = null;

  /**
   * Esta hash mantém o timeMillis() da última vez que a sessão foi utilizada. É utilizada pelo método de limpeza.<br>
   * Chave é o UUID da sessão.
   */
  private static final HashMap<String, Long> sessionsHeartBeat = new HashMap<>();

  /**
   * Cache com as sessões criadas indexadas pelo UUID da sessão.<Br>
   * Chave UUID da sessão, valor Objeto da sessão.
   */
  private static final HashMap<String, SessionVO> sessionsByUUID = new HashMap<>();

  /**
   * Cache com as sessões indexadas pela Thread à qual foi associada.<br>
   * Chave Thread associada, valor UUID da sessão.
   */
  private static final HashMap<Thread, String> sessionsByThread = new HashMap<>();

  /**
   * Cache com a ligação entre o token e o UUID da sessão.<br>
   * Chave Token de acesso, Valor UUID da sessão.
   */
  private static final HashMap<String, String> sessionsByToken = new HashMap<>();

  /**
   * Referência para a implementação da Operação de Retaguarda do sistema. Interface que provê as informações de autenticação e acessos do usuário.
   */
  private static SessionBackOperation backOperation = null;

  /**
   * Define o tempo (em segundos) que uma sessão pode viver sem ser requisitada pelo Sessionmanager. Ao ocorrer o timeout, o SessionManager descarta a sessão.<br>
   * Tempo em Segundos. Valor padrão 20 minutos.
   */
  private static long timeToLive = 1200;

  /**
   * Construtor privado para classe singleton
   */
  private SessionManager() {
  }

  /**
   * Método chamado internamente para iniciar a thread de controle das sessoes.
   */
  private static void startSessionThread() {
    SessionManager.controlThread = new Thread("### SessionManager Control Thread") {
      @Override
      public void run() {
        while (!shutingdown) {
          try { // Try para logar qualquer erro do sistema e prevenir o fim da Thread
            try {
              Thread.sleep(timeToLive * 500);
            } catch (InterruptedException e) {
            }

            // Procuramos todas as Thread registradas por Threads Mortas e as removemos
            final ArrayList<Thread> tmpThreadList = new ArrayList<>(sessionsByThread.keySet());
            for (Thread thread : tmpThreadList) {
              if (thread.getState() == State.TERMINATED) {
                sessionsByThread.remove(thread);
                RFW.pDev("[RFWSessionmanager] [REMOVED] Thread removida por estado 'TERMINATED'" + thread.getName());
              } else {
                final String uuid = sessionsByThread.get(thread);
                // Se está viva, atualizamos o tempo de vida desta sessão.
                updateCardio(uuid);
                RFW.pDev("[RFWSessionmanager] [ALIVE] Thread da sessão continua viva" + thread.getName() + " /  UUID: " + uuid);
              }
            }

            // Iteramos todas as sessões de usuários existente em busca do último momento de vida
            final ArrayList<String> tmpUUIDList = new ArrayList<>(sessionsByUUID.keySet());
            for (String uuid : tmpUUIDList) {
              // Verificamos a data do último batimento da sessão, se for maior que 10 minutos, acabamos com a sessão
              // Note que se houver uma Thread ativa para a sessão (como em um caso de processo demorado), o loop acima já atualizou o tempo para o momento atual, impedindo que a sessão não seja assassinada pq o usuário está esperando o sistema responder.
              final Long time = sessionsHeartBeat.get(uuid);
              if (System.currentTimeMillis() - time > timeToLive * 1000) {
                final SessionVO sVO = sessionsByUUID.remove(uuid);
                RFW.pDev("### [Sessionmanager] [REMOVED] Thread removida por 'TIME OUT'. UUID: " + sVO.getUUID() + " / systemID: " + sVO.getUser());
                sessionsHeartBeat.remove(uuid);
              }
            }
          } catch (Throwable t) {
            RFWLogger.logException(t);
          }
        }
      }
    };
    SessionManager.controlThread.setDaemon(true);
    SessionManager.controlThread.setPriority(Thread.MIN_PRIORITY); // Não há muita prioridade em fechar as sessões dos usuários.
    SessionManager.controlThread.start();
  }

  /**
   * Atualiza o horário do último batimento
   *
   * @param uuid UUID da sessão do usuário
   */
  private static void updateCardio(String uuid) {
    sessionsHeartBeat.put(uuid, System.currentTimeMillis());
  }

  /**
   * Permite que um usuário faça login no sistema através de usuário e senha e crie uma sessão no sistema.
   *
   * @param user Usuário para autenticação
   * @param password Senha do usuário para autenticação
   * @param locale Locale do usuário para formatação de informações regionais.
   * @return {@link SessionVO} com as informações da sessão, incluindo o UUID ({@link SessionVO#getUUID()}) que identica a sessão no {@link SessionManager}
   * @throws RFWException
   */
  public static SessionVO doLogin(String user, String password, Locale locale) throws RFWException {
    return doLogin(user, password, null, locale);
  }

  /**
   * Permite que um usuário faça login no sistema através de token e crie uma sessão no sistema.
   *
   * @param token Token de identificação e autorização do sistema.
   * @return {@link SessionVO} com as informações da sessão, incluindo o UUID ({@link SessionVO#getUUID()}) que identica a sessão no {@link SessionManager}
   * @throws RFWException
   */
  public static SessionVO doLogin(String token) throws RFWException {
    return doLogin(null, null, token, null);
  }

  /**
   * Este método concentra a operação de realizar o login internamente, aceitando o login por usuário/senha ou por token.
   *
   * @param user Usuário para o login por usuário/senha.
   * @param password Senha para o login por usuário/senha.
   * @param token Token de acesso para realizar o login por token de máquina. Só é levado em consideração se Usuário e Senha forem nulos.
   * @param locale Locale do usuário para formatação de informações regionais.
   * @return {@link SessionVO} com as informações da sessão, incluindo o UUID ({@link SessionVO#getUUID()}) que identica a sessão no {@link SessionManager}
   * @throws RFWException
   */
  private static SessionVO doLogin(String user, String password, String token, Locale locale) throws RFWException {
    if (SessionManager.controlThread == null) startSessionThread();
    PreProcess.requiredNonNull(SessionManager.backOperation, "A implementação so SessionBackOperation não foi definida no SessionManager! Inicializa o SessionManager adequadamente antes de utiliza-lo!");

    String uuid = null;
    do {
      uuid = RUGenerators.generateUUID();
    } while (sessionsByUUID.containsKey(uuid));

    SessionVO ssVO = null;
    if (user != null || password != null) {
      ssVO = SessionManager.backOperation.doLogin(user, password, locale, uuid);
    } else {
      ssVO = SessionManager.backOperation.doLogin(token, uuid);
      sessionsByToken.put(token, uuid);
    }

    if (ssVO == null) throw new RFWCriticalException("O sistema não gerou um objeto de sessão válido!");
    if (!uuid.equals(ssVO.getUUID())) throw new RFWCriticalException("O SessionVO não retornou o mesmo UUID passado na autenticação. Por favor veja a documentação do método de doLogin para entender o funcioanmento do UUID.");

    sessionsByUUID.put(uuid, ssVO);
    sessionsByThread.put(Thread.currentThread(), uuid);
    updateCardio(uuid);
    SessionManager.updateSessionVOActivity(ssVO);

    return ssVO;
  }

  /**
   * Recupera a sessão da thread atual.
   *
   * @return {@link SessionVO} associada à Thread atual.
   * @throws RFWException caso ocorra alguma exception ou se a Thread atual não for encontrada (Exception com o código "RFW_ERR_000005")
   */
  public static SessionVO getSession() throws RFWException {
    return getSession(sessionsByThread.get(Thread.currentThread()));
  }

  /**
   * Mesma operação que o método {@link #getSession()}, porém se a sessão não for encontrada retorna nulo ao invés da Exception
   *
   * @return {@link SessionVO} associada à Thread atual. Ou nulo caso a sessão não seja encontrada.
   * @throws RFWException caso ocorra alguma exception
   */
  public static SessionVO getSessionIfExists() throws RFWException {
    try {
      return getSession(sessionsByThread.get(Thread.currentThread()));
    } catch (RFWException e) {
      if (e.getExceptionCode().equals("RFW_ERR_000005")) return null;
      throw e;
    }
  }

  /**
   * Recupera a sessão pelo UUID
   *
   * @param uuid ID da sessão
   * @throws RFWException Lançado em caso de erro. Caso a sessão não exista, esteja expirada, inválida, etc. o código da exception será "RFW_ERR_000005".
   */
  public static SessionVO getSession(String uuid) throws RFWException {
    if (uuid == null) {
      throw new RFWWarningException("RFW_ERR_000005");
    }

    SessionVO sessionVO = null;
    // verifica se o valor pode ser um Token de autenticação ao invés de do UUID
    if (SessionManager.tokenPrefix != null && !"".equals(tokenPrefix) && uuid.startsWith(SessionManager.tokenPrefix)) {
      String token = uuid;

      // Se é um token de station, verificamos se já temos uma sessão criada para ele
      uuid = SessionManager.sessionsByToken.get(token);
      if (uuid != null) {
        sessionVO = SessionManager.sessionsByUUID.get(uuid);
      }

      if (sessionVO == null) {
        // Se não encontramos uma sessão válida, tentamos realizar o Login pelo Token e criar uma nova sessão.
        sessionVO = doLogin(null, null, token, null);
      }
    } else {
      // Busca pelo UUID de sessão
      sessionVO = sessionsByUUID.get(uuid);
    }
    if (sessionVO == null) {
      throw new RFWValidationException("RFW_ERR_000005");
    }

    // Atualiza o heartBeat da Sessão para mante-la viva
    updateCardio(uuid);

    return sessionVO;
  }

  /**
   * Registra uma sessão de usuário à Thread. Este método deve ser utilizado apra associar a Sessão nas Threads. Incluindo as de Login/Interceptor quanto as threads de "Fork" usadas no CRUD.
   *
   * @param thread Thread para associação da sessão
   * @param uuid Identificador da sessão
   * @throws RFWException Lançado caso ocorra algum erro ou a sessão não seja válida.
   */
  public static void attachSessionToThread(Thread thread, String uuid) throws RFWException {
    // Verificamos se a Thread já tem alguma sessão associada. Não é pertido sobreescrever a sessão.
    String threadUUID = sessionsByThread.get(thread);
    if (threadUUID != null && !uuid.equals(threadUUID)) {
      throw new RFWCriticalException("RFW_ERR_300037");
    }

    // Recupera a sessão pelo UUID, assim já verifica se está valida
    getSession(uuid);

    // Se tudo OK, associamos essa sessão na nova Thread
    sessionsByThread.put(thread, uuid);
  }

  /**
   * Remove a associação da sessão à uma determinada Thread.
   *
   * @param currentThread Thread a ter a sessão removida.
   */
  public static void cleanThread(Thread currentThread) {
    sessionsByThread.remove(currentThread);
  }

  /**
   * Finaliza o Sessionmanager. Uma vez finalizado nenhum tipo de Login será mais permitido e nenhuma sessão será autorizada.
   */
  public static final void shutdown() {
    SessionManager.shutingdown = true;
    if (SessionManager.controlThread != null) {
      SessionManager.controlThread.interrupt();
      try {
        SessionManager.controlThread.join();
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Este método simplesmente "cancela" a sessão passada. Uma vez que a sessão seja desregistrada, ela passará a dar erro de login/falha de acesso, como se já tivesse expirado. Obrigado o usuário a refazer o login.
   *
   * @param uuid Identificador único da sessão do usuário.
   */
  public static void unregisterSession(String uuid) {
    SessionVO ssVO = null;
    try {
      ssVO = getSession(uuid);
    } catch (RFWException e) {
      // se a sessão não existir não tem problema, não lança erro
    }

    if (ssVO != null) {
      sessionsByUUID.remove(ssVO.getUUID());
      sessionsHeartBeat.remove(ssVO.getUUID());
      for (Entry<Thread, String> entry : new ArrayList<>(sessionsByThread.entrySet())) {
        if (entry.getValue().equals(uuid)) sessionsByThread.remove(entry.getKey());
      }
      for (Entry<String, String> entry : new ArrayList<>(sessionsByToken.entrySet())) {
        if (entry.getValue().equals(uuid)) sessionsByToken.remove(entry.getKey());
      }
    }
  }

  /**
   * Este método simplesmente "cancela" a sessão passada.<br>
   * Uma vez que a sessão seja desregistrada, ela passará a dar erro de login/falha de acesso, como se já tivesse expirado. Obrigado o usuário a refazer o login.
   *
   * @param token Identificador único da sessão do usuário.
   */
  public static void unregisterSessionByToken(String token) {
    String uuid = sessionsByToken.get(token);
    unregisterSession(uuid);
  }

  /**
   * Este método "cancela" a sessão conforme o identificador único do usuário. Qualquer sessão cujo método {@link SessionVO#getUniqueID()} tenha um .equals() verdadeiro para o identificador passado terá a sessão finalizada.<br>
   * Uma vez que a sessão seja desregistrada, ela passará a dar erro de login/falha de acesso, como se já tivesse expirado. Obrigado o usuário a refazer o login.
   *
   * @param uniqueID Identificador único da sessão do usuário.
   */
  public static void unregisterSessionByUniqueID(String uniqueID) {
    for (Entry<String, SessionVO> entry : new ArrayList<>(sessionsByUUID.entrySet())) {
      SessionVO ssVO = entry.getValue();
      if (ssVO.getUniqueID() != null && ssVO.getUniqueID().equals(uniqueID)) {
        unregisterSession(entry.getKey());
      }
    }
  }

  /**
   * # referência para a implementação da Operação de Retaguarda do sistema. Interface que provê as informações de autenticação e acessos do usuário.
   *
   * @param backOperation the new referência para a implementação da Operação de Retaguarda do sistema
   */
  public static void setBackOperation(SessionBackOperation backOperation) throws RFWException {
    PreProcess.requiredNonNull(backOperation, "SessionBackOperation não pode ser nulo!");
    if (SessionManager.backOperation != null) throw new RFWCriticalException("Não é permitido trocar o SessionBackOperation por questões de segurança.");
    SessionManager.backOperation = backOperation;
  }

  /**
   * # define um prefixo que será utilizado para identificar quando a String de acesso não é o UUID da sessão, mas sim um Token utilizado para criar a sessão de acesso de uma "máquina".<br>
   * Note:
   * <li>que se este valor não for definido, o SessionManager não permitirá criar autenticação por máquina;
   * <li>Para inutilizar o acesso ao sistema via máquina defina esse atributo como "";
   * <li>Por questão de segurança esse valor só pode ser definido uma única vez.
   *
   * @param tokenPrefix the new define um prefixo que será utilizado para identificar quando a String de acesso não é o UUID da sessão, mas sim um Token utilizado para criar a sessão de acesso de uma "máquina"
   */
  public static void setTokenPrefix(String tokenPrefix) throws RFWException {
    PreProcess.requiredNonNull(tokenPrefix, "Prefixo de Token Inválido!");
    if (SessionManager.tokenPrefix != null) throw new RFWCriticalException("O prefixo de Token já foi definido anteriormente! Por questões de segurança ele não pode ser alterado!");
    SessionManager.tokenPrefix = tokenPrefix;
  }

  /**
   * # define o tempo (em segundos) que uma sessão pode viver sem ser requisitada pelo Sessionmanager. Ao ocorrer o timeout, o SessionManager descarta a sessão.<br>
   * Tempo em Segundos. Valor padrão 20 minutos.
   *
   * @param timeToLive the new define o tempo (em segundos) que uma sessão pode viver sem ser requisitada pelo Sessionmanager
   */
  public static void setTimeToLive(long timeToLive) throws RFWException {
    PreProcess.requiredPositive(timeToLive, "O TimeToLeave da Sessão deve ser um número positivo!");
    SessionManager.timeToLive = timeToLive;
  }

  /**
   * # define um prefixo que será utilizado para identificar quando a String de acesso não é o UUID da sessão, mas sim um Token utilizado para criar a sessão de acesso de uma "máquina".<br>
   * Note:
   * <li>que se este valor não for definido, o SessionManager não permitirá criar autenticação por máquina;
   * <li>Para inutilizar o acesso ao sistema via máquina defina esse atributo como "";
   * <li>Por questão de segurança esse valor só pode ser definido uma única vez.
   *
   * @return the define um prefixo que será utilizado para identificar quando a String de acesso não é o UUID da sessão, mas sim um Token utilizado para criar a sessão de acesso de uma "máquina"
   */
  public static String getTokenPrefix() {
    return tokenPrefix;
  }

  /**
   * # define o tempo (em segundos) que uma sessão pode viver sem ser requisitada pelo Sessionmanager. Ao ocorrer o timeout, o SessionManager descarta a sessão.<br>
   * Tempo em Segundos. Valor padrão 20 minutos.
   *
   * @return the define o tempo (em segundos) que uma sessão pode viver sem ser requisitada pelo Sessionmanager
   */
  public static long getTimeToLive() {
    return timeToLive;
  }

  /**
   * Repassa a notificação de SessionLastActiviry para o {@link SessionBackOperation}.
   *
   * @param ssVO Sessão a ser notificada.
   * @throws RFWException
   */
  public static void updateSessionVOActivity(SessionVO ssVO) throws RFWException {
    backOperation.updateSessionVOActivity(ssVO);
  }
}
