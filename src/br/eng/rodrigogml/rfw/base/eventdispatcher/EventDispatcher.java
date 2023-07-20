package br.eng.rodrigogml.rfw.base.eventdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;
import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;

/**
 * Description: Classe de lógica/controle dos disparos dos eventos do sistema.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (27 de set de 2018)
 */
public class EventDispatcher {

  /**
   * Listeners registrados no EventDispatcher. <br>
   * Chave da Hash é o ID do evento para o qual o Listener foi registrado. O conteúdo é uma HashSet com todos os listeners registrados para o evento. (Usamos a HashSet para evitar que o mesmo listener seja registrado múltiplas vezes)
   */
  private static final HashMap<String, HashSet<EventDispatcherListener>> listeners = new HashMap<>();

  /*
   * Nome da propriedade temporária utilizada para salvar o ID do evento dentro da própria hash de parâmetros do evento. Esse artifício foi utilizado para não termos de complicar a estrutura de objetos dos eventos do escopo.
   */
  private static final String eventIDUUID = RUGenerators.generateUUID();

  /**
   * HashMap utilizado para armezanar a pilha de escopos associados a Thread.<br>
   * A chave da Hash é a própria instância da Thread corrente.<Br>
   * O valor da hash é uma pilha. Sempre que abrimos um novo scopo para a Thread uma nova Hash é colocada na pilha, quando fechamos ela é removida. Assim, o total de itens da pilha é a quantidade de escopos abertos. A ausência de pilha indica nenhum escopo aberto.<br>
   * O objeto da Pilha é uma Lista de outra HashMap, Cada item da lista é um evento que foi registrado dentro deste escopo.<br>
   * A HashMap da lista é o parâmetro recebido para disparar o evento, mesma HashMap que será passada no disparo do evento do fechamento do escopo.<br>
   * Note que, para não criarmos uma nova estrutura para salvar o eventID, vamos utilizar a própria hash de parâmetros para salvar o eventID. O eventID será colocado na Hash com a chave definida em {@link #eventIDUUID}.<br>
   * Caso tenhamos recebido uma HashNula no evento, instanciamos uma para salvar o eventID e incluímos um segundo atributo identificado por {@link #eventIDUUID} + "-isNull", com um valor qualquer. A simples presença dessa chave indica que a hash original era nula e não vazia.
   */
  private static final HashMap<Thread, Stack<List<HashMap<String, Object>>>> threadScopes = new HashMap<Thread, Stack<List<HashMap<String, Object>>>>();

  /**
   * Prioridade da Thread de Notificação de Evento.<br>
   * Por padrão a prioridade é muito baixa pois eventos normalmente são tarefas que são assincronas à operação que lançou o evento. Sendo assim o usuário não está esperando que a tarefa ocorra, nem é um grande problema se ela demorar um pouco mais.
   */
  private static Integer eventThreadPriority = Thread.MIN_PRIORITY;

  /**
   * Construtor privado, classe estática.
   */
  private EventDispatcher() {
  }

  /**
   * Registra um Listener para um detemrinado evento do sistema.
   *
   * @param eventID ID do evento
   * @param listener Instância do Listener que será notificado na ocorrência do evento.
   * @throws RFWException
   */
  public static void addListener(EventDispatcherListener listener, String... eventIDs) throws RFWException {
    PreProcess.requiredNonEmptyCritical(eventIDs, "O array de IDs dos Eventos do Listener não pode ser nulo ou vazio.");

    synchronized (listeners) {
      for (String eventID : eventIDs) {
        HashSet<EventDispatcherListener> set = listeners.get(eventID);
        if (set == null) {
          set = new HashSet<>();
          listeners.put(eventID, set);
        }
        set.add(listener);
      }
    }
  }

  /**
   * Dispara um determinado evento para todos os listeners registrados.<br>
   * Note que os eventos são executados em uma Thread paralela, sem sessão ou Transaction Definidos.
   *
   * @param eventID ID do evento.
   * @param params Parametros do evento a ser compartilhado com os listeners.
   */
  public static void fire(final String eventID, final HashMap<String, Object> params) {
    final HashSet<EventDispatcherListener> set;
    synchronized (listeners) {
      if (listeners.get(eventID) == null || listeners.get(eventID).size() == 0) return;
      set = new HashSet<>();
    }
    if (set != null && set.size() > 0) {
      Thread t = new Thread("### EventDispatcher: " + eventID) {
        @Override
        public void run() {
          for (EventDispatcherListener listener : set) {
            try {
              listener.event(eventID, params);
            } catch (Throwable e) {
              RFWLogger.logError("O listener '" + listener.getClass().getCanonicalName() + "' do evento '" + eventID + "' deixou vazar a exception a seguir:");
              RFWLogger.logException(e);
            }
          }
        }
      };
      t.setPriority(EventDispatcher.eventThreadPriority);
      t.setDaemon(false);
      t.start();
    }
  }

  /**
   * Abre o escopo para iniciar o registro de eventos que devem ser disparados quando o escopo for fechado com sucesso.
   */
  public static void beginScope() {
    Stack<List<HashMap<String, Object>>> scope = threadScopes.get(Thread.currentThread());
    if (scope == null) {
      scope = new Stack<List<HashMap<String, Object>>>();
      threadScopes.put(Thread.currentThread(), scope);
    }

    scope.push(new ArrayList<HashMap<String, Object>>());
  }

  /**
   * Sinaliza a finalização/fechamento de um escopo de eventos.<br>
   * <li>Caso não seja o último escopo aberto para a Thread:
   * <ul>
   * <li>e caso tenha terminado com sucesso, os eventos desse escopo são passados para o escopo pai para serem disparados ou descartados junto com o escopo anterior;
   * <li>e caso tenha terminado com exception (rollback), os evento desse escopo serão descartados e não serão disparados.
   * </ul>
   * <li>Se for o último escopo aberto para a Thread:
   * <ul>
   * <li>e caso tenha terminado com sucesso, os eventos serão disparados;
   * <li>e caso tenha terminado com exception (rollback), os evento serão descartados.
   * </ul>
   *
   * @param committed Indica se o escopo está sendo finalizado com sucesso. True indica que devemos fazer commit dos eventos e dispara-los/associar ao escopo anterior conforme documentoração. False indica que devemos dar um rollback e descartar os eventos desse escopo.
   * @throws RFWException
   */
  public static void endScope(boolean committed) throws RFWException {
    Stack<List<HashMap<String, Object>>> scopes = threadScopes.get(Thread.currentThread());
    PreProcess.requiredNonNullCritical(scopes, "Não há nenhum escopo iniciado para essa Thread! Impossível finalizar um escopo inexistente!");

    List<HashMap<String, Object>> eventList = scopes.pop();

    boolean lastScope = scopes.size() == 0;
    if (lastScope) {
      // se e o último scope, já vamos limpar os objetos da memória para liberar os recursos
      threadScopes.remove(Thread.currentThread());
    }

    // Se não tivermos nenhum evento, não precisamos nem processar nada
    if (eventList != null && eventList.size() > 0) {
      if (lastScope) {
        if (committed) {
          // Dispara todos os eventos da lista
          for (HashMap<String, Object> eventParam : eventList) {
            String eventID = (String) eventParam.get(eventIDUUID);
            if (eventParam.containsKey(eventIDUUID + "-isNull")) {
              eventParam = null;
            } else {
              eventParam.remove(eventIDUUID);
            }

            // Disparamos uma Thread para cada evento recebido, exatamente como se fosse um evento do fire(), se tivermos listeners para esse evento
            final HashSet<EventDispatcherListener> listenersSet;
            synchronized (listeners) {
              listenersSet = new HashSet<>(listeners.get(eventID));
            }
            if (listenersSet != null && listenersSet.size() > 0) {
              HashMap<String, Object> eventParamFinal = eventParam;
              Thread t = new Thread("### EventDispatcher: " + eventID) {
                @Override
                public void run() {
                  // Aguardamos uns segundos antes de iniciar, porque no caso do constrole dos escopos serem feitos pelo Interceptor, o commit não foi de fato realizado quando essa Thread iniciou
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException e1) {
                  }
                  for (EventDispatcherListener listener : listenersSet) {
                    try {
                      listener.event(eventID, eventParamFinal);
                    } catch (Throwable e) {
                      RFWLogger.logError("O listener '" + listener.getClass().getCanonicalName() + "' do evento '" + eventID + "' deixou vazar a exception a seguir:");
                      RFWLogger.logException(e);
                    }
                  }
                }
              };
              t.setPriority(EventDispatcher.eventThreadPriority);
              t.setDaemon(false);
              t.start();
            }
          }
        } else {
          // Não faz nada, só vamos descartar toda a lista de eventos
        }
      } else {
        if (committed) {
          // Passamos todos os eventos para o próximo escopo para avaliar no fim dele se os eventos serão disparados ou não
          scopes.get(scopes.size() - 1).addAll(eventList);
        } else {
          // Não faz nada, só vamos descartar toda a lista de eventos
        }
      }
    }

  }

  /**
   * Registra um evento que deverá ser disparado na finalização do escopo quando terminado com sucesso.
   *
   * @param eventID ID do evento.
   * @param params Parametros do evento a ser compartilhado com os listeners.
   * @throws RFWException
   */
  public static void fireOnCommit(final String eventID, HashMap<String, Object> params) throws RFWException {
    Stack<List<HashMap<String, Object>>> scopes = threadScopes.get(Thread.currentThread());
    PreProcess.requiredNonNullCritical(scopes, "Não há nenhum escopo iniciado para essa Thread! Impossível registrar um evento 'OnCommit' neste momento.");

    List<HashMap<String, Object>> eventList = scopes.get(scopes.size() - 1);

    if (params == null) {
      params = new HashMap<String, Object>();
      params.put(eventIDUUID + "-isNull", Boolean.TRUE);
    }
    params.put(eventIDUUID, eventID);

    eventList.add(params);
  }

  public static Integer getEventThreadPriority() {
    return eventThreadPriority;
  }

  public static void setEventThreadPriority(Integer eventThreadPriority) throws RFWException {
    PreProcess.requiredBetweenCritical(eventThreadPriority, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY, "Valor de Prioridade da Thread de Eventos inválido! Valor deve estar entre Thread.MIN_PRIORITY e Thread.MAX_PRIORITY.");
    EventDispatcher.eventThreadPriority = eventThreadPriority;
  }

}
