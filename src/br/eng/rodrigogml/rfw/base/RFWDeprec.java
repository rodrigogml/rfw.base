package br.eng.rodrigogml.rfw.base;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.base.eventdispatcher.EventDispatcher;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;
import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionBackOperation;
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
   * Inicializa o serviço do RFWLogger configurando seus parâmetros.<Br>
   * Note que este método de fato só define as configurações do RFWDeprec. Suas thread só são iniciadas a partir do primeiro log() realizado.<br>
   * <br>
   * <b>ATENÇÃO: </B>Depois de inicializado, o RFWLogger exige que o método {@link #shutdownFW()} seja chamado para que suas Threads sejam encerradas.
   *
   * @param timeToLive Define o tempo que uma entrada de LOG pode ficar na memória aguardando para ser consumida. Depois de passado do periodo definido aqui, a o próprio {@link RFWLogger} descartará a entrada do Log.<br>
   *          Tempo definido em segundos.
   * @param timeToClean Define de quanto em quanto tempo o {@link RFWLogger} executará a limpeza das entradas que não foram consumidas. Note que conforme os valores definidos uma entrada de log ficará aguardando ser consumida descartada entre no mínimo pelo valor definido como timeToLive e no máximo até timeToLive + timeToClean.<br>
   *          Tempo definido em segundos.
   * @throws RFWException
   */
  public static void initializeFWLogger(long timeToLive, long timeToClean) throws RFWException {
    RFWLogger.setTimeToLive(timeToLive);
    RFWLogger.setTimeToClean(timeToClean);
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
   * Este método inicializa {@link RFWBundle} com um novo arquivo.<br>
   * Note que cada novo arquivo carregado é lido sobre o mesmo properties. Isso faz com que em caso de conflito de chaves o conteúdo do último arquivo lido se sobreponha. Embora pareça uma falha, a ideia é proposital, assim é possível substituir mensagens padrão do RFWDeprec pelo sistema sendo feito.
   *
   * @param bundleName
   * @throws RFWException
   */
  public static void initializeBundle(String bundleName) throws RFWException {
    RFWBundle.loadBundle(bundleName);
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
