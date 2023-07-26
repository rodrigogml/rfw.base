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
 * Description: Classe utilizada para gerenciar as sess�es de usu�rios atrav�s da Thread.<br>
 *
 * @author Rodrigo Leit�o
 * @since 10.0.0 (13 de jul de 2018)
 */
public class SessionManager {

  /**
   * Determina se o SessionManager foi chamado para ser finalizado.
   */
  private static Boolean shutingdown = false;

  /**
   * Refer�ncia para a Thread do SessionManager.
   */
  private static Thread controlThread = null;

  /**
   * Define um prefixo que ser� utilizado para identificar quando a String de acesso n�o � o UUID da sess�o, mas sim um Token utilizado para criar a sess�o de acesso de uma "m�quina".<br>
   * Note:
   * <li>que se este valor n�o for definido, o SessionManager n�o permitir� criar autentica��o por m�quina;
   * <li>Para inutilizar o acesso ao sistema via m�quina defina esse atributo como "";
   * <li>Por quest�o de seguran�a esse valor s� pode ser definido uma �nica vez.
   */
  private static String tokenPrefix = null;

  /**
   * Esta hash mant�m o timeMillis() da �ltima vez que a sess�o foi utilizada. � utilizada pelo m�todo de limpeza.<br>
   * Chave � o UUID da sess�o.
   */
  private static final HashMap<String, Long> sessionsHeartBeat = new HashMap<>();

  /**
   * Cache com as sess�es criadas indexadas pelo UUID da sess�o.<Br>
   * Chave UUID da sess�o, valor Objeto da sess�o.
   */
  private static final HashMap<String, SessionVO> sessionsByUUID = new HashMap<>();

  /**
   * Cache com as sess�es indexadas pela Thread � qual foi associada.<br>
   * Chave Thread associada, valor UUID da sess�o.
   */
  private static final HashMap<Thread, String> sessionsByThread = new HashMap<>();

  /**
   * Cache com a liga��o entre o token e o UUID da sess�o.<br>
   * Chave Token de acesso, Valor UUID da sess�o.
   */
  private static final HashMap<String, String> sessionsByToken = new HashMap<>();

  /**
   * Refer�ncia para a implementa��o da Opera��o de Retaguarda do sistema. Interface que prov� as informa��es de autentica��o e acessos do usu�rio.
   */
  private static SessionBackOperation backOperation = null;

  /**
   * Define o tempo (em segundos) que uma sess�o pode viver sem ser requisitada pelo Sessionmanager. Ao ocorrer o timeout, o SessionManager descarta a sess�o.<br>
   * Tempo em Segundos. Valor padr�o 20 minutos.
   */
  private static long timeToLive = 1200;

  /**
   * Construtor privado para classe singleton
   */
  private SessionManager() {
  }

  /**
   * M�todo chamado internamente para iniciar a thread de controle das sessoes.
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
                // Se est� viva, atualizamos o tempo de vida desta sess�o.
                updateCardio(uuid);
                RFW.pDev("[RFWSessionmanager] [ALIVE] Thread da sess�o continua viva" + thread.getName() + " /  UUID: " + uuid);
              }
            }

            // Iteramos todas as sess�es de usu�rios existente em busca do �ltimo momento de vida
            final ArrayList<String> tmpUUIDList = new ArrayList<>(sessionsByUUID.keySet());
            for (String uuid : tmpUUIDList) {
              // Verificamos a data do �ltimo batimento da sess�o, se for maior que 10 minutos, acabamos com a sess�o
              // Note que se houver uma Thread ativa para a sess�o (como em um caso de processo demorado), o loop acima j� atualizou o tempo para o momento atual, impedindo que a sess�o n�o seja assassinada pq o usu�rio est� esperando o sistema responder.
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
    SessionManager.controlThread.setPriority(Thread.MIN_PRIORITY); // N�o h� muita prioridade em fechar as sess�es dos usu�rios.
    SessionManager.controlThread.start();
  }

  /**
   * Atualiza o hor�rio do �ltimo batimento
   *
   * @param uuid UUID da sess�o do usu�rio
   */
  private static void updateCardio(String uuid) {
    sessionsHeartBeat.put(uuid, System.currentTimeMillis());
  }

  /**
   * Permite que um usu�rio fa�a login no sistema atrav�s de usu�rio e senha e crie uma sess�o no sistema.
   *
   * @param user Usu�rio para autentica��o
   * @param password Senha do usu�rio para autentica��o
   * @param locale Locale do usu�rio para formata��o de informa��es regionais.
   * @return {@link SessionVO} com as informa��es da sess�o, incluindo o UUID ({@link SessionVO#getUUID()}) que identica a sess�o no {@link SessionManager}
   * @throws RFWException
   */
  public static SessionVO doLogin(String user, String password, Locale locale) throws RFWException {
    return doLogin(user, password, null, locale);
  }

  /**
   * Permite que um usu�rio fa�a login no sistema atrav�s de token e crie uma sess�o no sistema.
   *
   * @param token Token de identifica��o e autoriza��o do sistema.
   * @return {@link SessionVO} com as informa��es da sess�o, incluindo o UUID ({@link SessionVO#getUUID()}) que identica a sess�o no {@link SessionManager}
   * @throws RFWException
   */
  public static SessionVO doLogin(String token) throws RFWException {
    return doLogin(null, null, token, null);
  }

  /**
   * Este m�todo concentra a opera��o de realizar o login internamente, aceitando o login por usu�rio/senha ou por token.
   *
   * @param user Usu�rio para o login por usu�rio/senha.
   * @param password Senha para o login por usu�rio/senha.
   * @param token Token de acesso para realizar o login por token de m�quina. S� � levado em considera��o se Usu�rio e Senha forem nulos.
   * @param locale Locale do usu�rio para formata��o de informa��es regionais.
   * @return {@link SessionVO} com as informa��es da sess�o, incluindo o UUID ({@link SessionVO#getUUID()}) que identica a sess�o no {@link SessionManager}
   * @throws RFWException
   */
  private static SessionVO doLogin(String user, String password, String token, Locale locale) throws RFWException {
    if (SessionManager.controlThread == null) startSessionThread();
    PreProcess.requiredNonNull(SessionManager.backOperation, "A implementa��o so SessionBackOperation n�o foi definida no SessionManager! Inicializa o SessionManager adequadamente antes de utiliza-lo!");

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

    if (ssVO == null) throw new RFWCriticalException("O sistema n�o gerou um objeto de sess�o v�lido!");
    if (!uuid.equals(ssVO.getUUID())) throw new RFWCriticalException("O SessionVO n�o retornou o mesmo UUID passado na autentica��o. Por favor veja a documenta��o do m�todo de doLogin para entender o funcioanmento do UUID.");

    sessionsByUUID.put(uuid, ssVO);
    sessionsByThread.put(Thread.currentThread(), uuid);
    updateCardio(uuid);
    SessionManager.updateSessionVOActivity(ssVO);

    return ssVO;
  }

  /**
   * Recupera a sess�o da thread atual.
   *
   * @return {@link SessionVO} associada � Thread atual.
   * @throws RFWException caso ocorra alguma exception ou se a Thread atual n�o for encontrada (Exception com o c�digo "RFW_ERR_000005")
   */
  public static SessionVO getSession() throws RFWException {
    return getSession(sessionsByThread.get(Thread.currentThread()));
  }

  /**
   * Mesma opera��o que o m�todo {@link #getSession()}, por�m se a sess�o n�o for encontrada retorna nulo ao inv�s da Exception
   *
   * @return {@link SessionVO} associada � Thread atual. Ou nulo caso a sess�o n�o seja encontrada.
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
   * Recupera a sess�o pelo UUID
   *
   * @param uuid ID da sess�o
   * @throws RFWException Lan�ado em caso de erro. Caso a sess�o n�o exista, esteja expirada, inv�lida, etc. o c�digo da exception ser� "RFW_ERR_000005".
   */
  public static SessionVO getSession(String uuid) throws RFWException {
    if (uuid == null) {
      throw new RFWWarningException("RFW_ERR_000005");
    }

    SessionVO sessionVO = null;
    // verifica se o valor pode ser um Token de autentica��o ao inv�s de do UUID
    if (SessionManager.tokenPrefix != null && !"".equals(tokenPrefix) && uuid.startsWith(SessionManager.tokenPrefix)) {
      String token = uuid;

      // Se � um token de station, verificamos se j� temos uma sess�o criada para ele
      uuid = SessionManager.sessionsByToken.get(token);
      if (uuid != null) {
        sessionVO = SessionManager.sessionsByUUID.get(uuid);
      }

      if (sessionVO == null) {
        // Se n�o encontramos uma sess�o v�lida, tentamos realizar o Login pelo Token e criar uma nova sess�o.
        sessionVO = doLogin(null, null, token, null);
      }
    } else {
      // Busca pelo UUID de sess�o
      sessionVO = sessionsByUUID.get(uuid);
    }
    if (sessionVO == null) {
      throw new RFWValidationException("RFW_ERR_000005");
    }

    // Atualiza o heartBeat da Sess�o para mante-la viva
    updateCardio(uuid);

    return sessionVO;
  }

  /**
   * Registra uma sess�o de usu�rio � Thread. Este m�todo deve ser utilizado apra associar a Sess�o nas Threads. Incluindo as de Login/Interceptor quanto as threads de "Fork" usadas no CRUD.
   *
   * @param thread Thread para associa��o da sess�o
   * @param uuid Identificador da sess�o
   * @throws RFWException Lan�ado caso ocorra algum erro ou a sess�o n�o seja v�lida.
   */
  public static void attachSessionToThread(Thread thread, String uuid) throws RFWException {
    // Verificamos se a Thread j� tem alguma sess�o associada. N�o � pertido sobreescrever a sess�o.
    String threadUUID = sessionsByThread.get(thread);
    if (threadUUID != null && !uuid.equals(threadUUID)) {
      throw new RFWCriticalException("RFW_ERR_300037");
    }

    // Recupera a sess�o pelo UUID, assim j� verifica se est� valida
    getSession(uuid);

    // Se tudo OK, associamos essa sess�o na nova Thread
    sessionsByThread.put(thread, uuid);
  }

  /**
   * Remove a associa��o da sess�o � uma determinada Thread.
   *
   * @param currentThread Thread a ter a sess�o removida.
   */
  public static void cleanThread(Thread currentThread) {
    sessionsByThread.remove(currentThread);
  }

  /**
   * Finaliza o Sessionmanager. Uma vez finalizado nenhum tipo de Login ser� mais permitido e nenhuma sess�o ser� autorizada.
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
   * Este m�todo simplesmente "cancela" a sess�o passada. Uma vez que a sess�o seja desregistrada, ela passar� a dar erro de login/falha de acesso, como se j� tivesse expirado. Obrigado o usu�rio a refazer o login.
   *
   * @param uuid Identificador �nico da sess�o do usu�rio.
   */
  public static void unregisterSession(String uuid) {
    SessionVO ssVO = null;
    try {
      ssVO = getSession(uuid);
    } catch (RFWException e) {
      // se a sess�o n�o existir n�o tem problema, n�o lan�a erro
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
   * Este m�todo simplesmente "cancela" a sess�o passada.<br>
   * Uma vez que a sess�o seja desregistrada, ela passar� a dar erro de login/falha de acesso, como se j� tivesse expirado. Obrigado o usu�rio a refazer o login.
   *
   * @param token Identificador �nico da sess�o do usu�rio.
   */
  public static void unregisterSessionByToken(String token) {
    String uuid = sessionsByToken.get(token);
    unregisterSession(uuid);
  }

  /**
   * Este m�todo "cancela" a sess�o conforme o identificador �nico do usu�rio. Qualquer sess�o cujo m�todo {@link SessionVO#getUniqueID()} tenha um .equals() verdadeiro para o identificador passado ter� a sess�o finalizada.<br>
   * Uma vez que a sess�o seja desregistrada, ela passar� a dar erro de login/falha de acesso, como se j� tivesse expirado. Obrigado o usu�rio a refazer o login.
   *
   * @param uniqueID Identificador �nico da sess�o do usu�rio.
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
   * # refer�ncia para a implementa��o da Opera��o de Retaguarda do sistema. Interface que prov� as informa��es de autentica��o e acessos do usu�rio.
   *
   * @param backOperation the new refer�ncia para a implementa��o da Opera��o de Retaguarda do sistema
   */
  public static void setBackOperation(SessionBackOperation backOperation) throws RFWException {
    PreProcess.requiredNonNull(backOperation, "SessionBackOperation n�o pode ser nulo!");
    if (SessionManager.backOperation != null) throw new RFWCriticalException("N�o � permitido trocar o SessionBackOperation por quest�es de seguran�a.");
    SessionManager.backOperation = backOperation;
  }

  /**
   * # define um prefixo que ser� utilizado para identificar quando a String de acesso n�o � o UUID da sess�o, mas sim um Token utilizado para criar a sess�o de acesso de uma "m�quina".<br>
   * Note:
   * <li>que se este valor n�o for definido, o SessionManager n�o permitir� criar autentica��o por m�quina;
   * <li>Para inutilizar o acesso ao sistema via m�quina defina esse atributo como "";
   * <li>Por quest�o de seguran�a esse valor s� pode ser definido uma �nica vez.
   *
   * @param tokenPrefix the new define um prefixo que ser� utilizado para identificar quando a String de acesso n�o � o UUID da sess�o, mas sim um Token utilizado para criar a sess�o de acesso de uma "m�quina"
   */
  public static void setTokenPrefix(String tokenPrefix) throws RFWException {
    PreProcess.requiredNonNull(tokenPrefix, "Prefixo de Token Inv�lido!");
    if (SessionManager.tokenPrefix != null) throw new RFWCriticalException("O prefixo de Token j� foi definido anteriormente! Por quest�es de seguran�a ele n�o pode ser alterado!");
    SessionManager.tokenPrefix = tokenPrefix;
  }

  /**
   * # define o tempo (em segundos) que uma sess�o pode viver sem ser requisitada pelo Sessionmanager. Ao ocorrer o timeout, o SessionManager descarta a sess�o.<br>
   * Tempo em Segundos. Valor padr�o 20 minutos.
   *
   * @param timeToLive the new define o tempo (em segundos) que uma sess�o pode viver sem ser requisitada pelo Sessionmanager
   */
  public static void setTimeToLive(long timeToLive) throws RFWException {
    PreProcess.requiredPositive(timeToLive, "O TimeToLeave da Sess�o deve ser um n�mero positivo!");
    SessionManager.timeToLive = timeToLive;
  }

  /**
   * # define um prefixo que ser� utilizado para identificar quando a String de acesso n�o � o UUID da sess�o, mas sim um Token utilizado para criar a sess�o de acesso de uma "m�quina".<br>
   * Note:
   * <li>que se este valor n�o for definido, o SessionManager n�o permitir� criar autentica��o por m�quina;
   * <li>Para inutilizar o acesso ao sistema via m�quina defina esse atributo como "";
   * <li>Por quest�o de seguran�a esse valor s� pode ser definido uma �nica vez.
   *
   * @return the define um prefixo que ser� utilizado para identificar quando a String de acesso n�o � o UUID da sess�o, mas sim um Token utilizado para criar a sess�o de acesso de uma "m�quina"
   */
  public static String getTokenPrefix() {
    return tokenPrefix;
  }

  /**
   * # define o tempo (em segundos) que uma sess�o pode viver sem ser requisitada pelo Sessionmanager. Ao ocorrer o timeout, o SessionManager descarta a sess�o.<br>
   * Tempo em Segundos. Valor padr�o 20 minutos.
   *
   * @return the define o tempo (em segundos) que uma sess�o pode viver sem ser requisitada pelo Sessionmanager
   */
  public static long getTimeToLive() {
    return timeToLive;
  }

  /**
   * Repassa a notifica��o de SessionLastActiviry para o {@link SessionBackOperation}.
   *
   * @param ssVO Sess�o a ser notificada.
   * @throws RFWException
   */
  public static void updateSessionVOActivity(SessionVO ssVO) throws RFWException {
    backOperation.updateSessionVOActivity(ssVO);
  }
}
