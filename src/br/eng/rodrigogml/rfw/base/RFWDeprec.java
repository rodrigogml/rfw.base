package br.eng.rodrigogml.rfw.base;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.base.eventdispatcher.EventDispatcher;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;
import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionBackOperation;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;;

/**
 * Description: Classe utilit�ria geral do RFWDeprec com m�todos utilit�rios comuns gen�ricos.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 * @deprecated Deve ser utilizada a Classe que foi passada para o m�dulo RFWDeprec.Kernel
 */
@Deprecated
public class RFWDeprec {

  /**
   * Construtor privado para uma classe completamente est�tica
   */
  private RFWDeprec() {
  }

  /**
   * Inicializa o servi�o do RFWLogger configurando seus par�metros.<Br>
   * Note que este m�todo de fato s� define as configura��es do RFWDeprec. Suas thread s� s�o iniciadas a partir do primeiro log() realizado.<br>
   * <br>
   * <b>ATEN��O: </B>Depois de inicializado, o RFWLogger exige que o m�todo {@link #shutdownFW()} seja chamado para que suas Threads sejam encerradas.
   *
   * @param timeToLive Define o tempo que uma entrada de LOG pode ficar na mem�ria aguardando para ser consumida. Depois de passado do periodo definido aqui, a o pr�prio {@link RFWLogger} descartar� a entrada do Log.<br>
   *          Tempo definido em segundos.
   * @param timeToClean Define de quanto em quanto tempo o {@link RFWLogger} executar� a limpeza das entradas que n�o foram consumidas. Note que conforme os valores definidos uma entrada de log ficar� aguardando ser consumida descartada entre no m�nimo pelo valor definido como timeToLive e no m�ximo at� timeToLive + timeToClean.<br>
   *          Tempo definido em segundos.
   * @throws RFWException
   */
  public static void initializeFWLogger(long timeToLive, long timeToClean) throws RFWException {
    RFWLogger.setTimeToLive(timeToLive);
    RFWLogger.setTimeToClean(timeToClean);
  }

  /**
   * Inicializa o servi�o de Controle de Sess�o do RFWDeprec.
   *
   * @param timeToLive Tempo que a sess�o sobrevive sem atividade at� ser descartada.<br>
   *          Valor em segundos.
   * @param backOperation Implementa��o da interface que far� a autentica��o dos usu�rios e acesso por tokens.
   * @param tokenPrefix Prefixo dos tokens de acesso. Quando o sistema permite que m�quinas se conectem � fachada, o acesso pode ser feito por um token ao inv�s do usu�rio/senha. Mas para que o {@link SessionManager} identifique que se trata de um Token e n�o de um UUID � preciso definir um prefixo de Token. <b>Caso n�o utilize acesso por token para m�quinas, passe "" neste par�metro por
   *          seguran�a, n�o passe NULL.</b>
   * @throws RFWException
   */
  public static void initializeSessionManager(long timeToLive, SessionBackOperation backOperation, String tokenPrefix) throws RFWException {
    SessionManager.setTimeToLive(timeToLive);
    SessionManager.setBackOperation(backOperation);
    SessionManager.setTokenPrefix(tokenPrefix);
  }

  /**
   * Este m�todo inicializa {@link RFWBundle} com um novo arquivo.<br>
   * Note que cada novo arquivo carregado � lido sobre o mesmo properties. Isso faz com que em caso de conflito de chaves o conte�do do �ltimo arquivo lido se sobreponha. Embora pare�a uma falha, a ideia � proposital, assim � poss�vel substituir mensagens padr�o do RFWDeprec pelo sistema sendo feito.
   *
   * @param bundleName
   * @throws RFWException
   */
  public static void initializeBundle(String bundleName) throws RFWException {
    RFWBundle.loadBundle(bundleName);
  }

  /**
   * Este m�todo inicializa {@link EventDispatcher}.<br>
   *
   * @param eventThreadPriority Prioridade que as Threads de disparo dos eventos devem ser executadas. Valor padr�o {@link Thread#MIN_PRIORITY}.
   * @throws RFWException
   */
  public static void initializeEventDispatcher(Integer eventThreadPriority) throws RFWException {
    EventDispatcher.setEventThreadPriority(eventThreadPriority);
  }

}
