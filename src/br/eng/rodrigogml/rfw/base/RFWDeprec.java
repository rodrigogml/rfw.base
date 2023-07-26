package br.eng.rodrigogml.rfw.base;

import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;
import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionBackOperation;
import br.eng.rodrigogml.rfw.kernel.eventdispatcher.EventDispatcher;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;;

/**
 * Description: Classe utilitária geral do RFWDeprec com métodos utilitários comuns genéricos.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 * @deprecated Deve ser utilizada a Classe que foi passada para o módulo RFWDeprec.Kernel
 */
@Deprecated
public class RFWDeprec {

  /**
   * Construtor privado para uma classe completamente estática
   */
  private RFWDeprec() {
  }

  /**
   * Inicializa o serviço de Controle de Sessão do RFWDeprec.
   *
   * @param timeToLive Tempo que a sessão sobrevive sem atividade até ser descartada.<br>
   *          Valor em segundos.
   * @param backOperation Implementação da interface que fará a autenticação dos usuários e acesso por tokens.
   * @param tokenPrefix Prefixo dos tokens de acesso. Quando o sistema permite que máquinas se conectem à fachada, o acesso pode ser feito por um token ao invés do usuário/senha. Mas para que o {@link SessionManager} identifique que se trata de um Token e não de um UUID é preciso definir um prefixo de Token. <b>Caso não utilize acesso por token para máquinas, passe "" neste parâmetro por
   *          segurança, não passe NULL.</b>
   * @throws RFWException
   */
  public static void initializeSessionManager(long timeToLive, SessionBackOperation backOperation, String tokenPrefix) throws RFWException {
    SessionManager.setTimeToLive(timeToLive);
    SessionManager.setBackOperation(backOperation);
    SessionManager.setTokenPrefix(tokenPrefix);
  }

  /**
   * Este método inicializa {@link EventDispatcher}.<br>
   *
   * @param eventThreadPriority Prioridade que as Threads de disparo dos eventos devem ser executadas. Valor padrão {@link Thread#MIN_PRIORITY}.
   * @throws RFWException
   */
  public static void initializeEventDispatcher(Integer eventThreadPriority) throws RFWException {
    EventDispatcher.setEventThreadPriority(eventThreadPriority);
  }

}
