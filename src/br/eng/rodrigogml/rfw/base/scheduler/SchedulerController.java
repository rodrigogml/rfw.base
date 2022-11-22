package br.eng.rodrigogml.rfw.base.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.scheduler.interfaces.SchedulerListener;
import br.eng.rodrigogml.rfw.base.scheduler.interfaces.SchedulerTask;
import br.eng.rodrigogml.rfw.base.utils.BUDateTime;

/**
 * Description: Classe de controle das tarefas agendadas do sistema. Repons�vel por carrega-las, inicial-las e executa-las conforme suas regras.<br>
 * O SchedulerController � �nico para todas as empresas do sistema
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (27 de set de 2018)
 */
public class SchedulerController {

  /**
   * Inst�ncia do Singleton.
   */
  private static SchedulerController instance = null;

  /**
   * Controlador da gera��o de IDs autom�ticos.
   */
  private static long lastID = 0;

  /**
   * Lista de Listeners de Eventos do SchedulerController
   */
  private static List<SchedulerListener> listeners = new LinkedList<SchedulerListener>();

  /**
   * Mant�m os agendamentos das tarefas em uma hash de acordo com o ID do SchedulingVO.
   */
  private final HashMap<Long, SchedulerTaskTiming> taskTimingHash = new HashMap<>();

  private SchedulerController() {
  }

  private static SchedulerController getInstance() {
    if (SchedulerController.instance == null) SchedulerController.instance = new SchedulerController();
    return SchedulerController.instance;
  }

  /**
   * Carrega uma ou mais tarefas no agendador do Scheduler
   *
   * @param tasks Implementa��es do {@link SchedulerVO}.
   * @throws RFWException
   */
  public static void loadTasks(SchedulerTask... tasks) throws RFWException {
    for (SchedulerTask task : tasks) {
      processTask(task, false);
    }
  }

  protected static void processTask(SchedulerTask task, boolean runNow) {
    RFWLogger.logDebug("Processando Tarefa: " + task.getTaskClass(), task.getTaskClass());

    // Verifica se esta task j� est� no controller
    SchedulerTaskTiming taskTiming = getInstance().taskTimingHash.get(task.getId());
    if (taskTiming != null) {
      // Se encontrou criamos um novo tasktiming pois este n�o pode ser usado duas vezes, e por garantia de que n�o estamos reprocessando algo qua ainda n�o ocorreu, cancelamos a primeira...
      taskTiming.cancelTimer();
      getInstance().taskTimingHash.remove(task.getId());
    }

    try {
      LocalDateTime nextExecution = calcTaskNextExecution(task);
      if (nextExecution != null) {
        RFWLogger.logDebug("Tarefa '" + task.getTaskClass() + "' pr�xima execu��o agendada para " + RFW.getDateTimeFormattter().format(nextExecution), task.getTaskClass());
        // se temos uma nova execu��o, criamos o SchedulerTaskTiming
        // CUIDADO: Se criar o SchedulerTaskTiming ele automaticamente j� cria a Thread e mant�m ela em mem�ria com o Timer dentro dela, mesmo sem agendar (chamar o m�todo .schedule())
        taskTiming = new SchedulerTaskTiming(task);
        // Se tem a data de pr�xima execu��o, criamos o agendamento e colocamos na Hash
        getInstance().taskTimingHash.put(task.getId(), taskTiming);
        taskTiming.schedule(nextExecution, runNow);
      }
    } catch (RFWException e) {
      RFWLogger.logException(e, task.getTaskClass());
    }
  }

  /**
   * Este m�todo calcula o tempo da pr�xima execu��o de um determinado agendamento.
   *
   * @param task
   * @return Date com a hora da pr�xima execu��o, ou null caso a tarefa n�o deva mais ser executada.
   */
  public static LocalDateTime calcTaskNextExecution(SchedulerTask task) throws RFWException {
    LocalDateTime execdate = null;
    final LocalDateTime now = RFW.getDateTime(); // Fixamos a hora atual pois mesmo que esse processamento demore, a hora ser� contabilizada a partir dessa vari�vel, e n�o uma que fique mudando de linha em linha ;) - Sistema lento eim?
    LocalDateTime scheduleTime = task.getScheduleTime();

    if (scheduleTime.compareTo(now) > 0) {
      // Se a data de agendamento inicial � futura, retornamos essa pr�pria data, pois se ela ainda n�o aconteceu nem temos que verificar l�gica de repeti��o!
      execdate = task.getScheduleTime();
    } else {
      if (task.getRepeatFrequency() == null) {
        // Se a tarefa n�o tem repeti��o, vamos verificar a quest�o da execu��o atrasada.
        if (task.getLateExecution() != null) {
          // Se tem pol�tica de execu��o com atraso definida...
          if (task.getLastExecution() == null || task.getScheduleTime().compareTo(task.getLastExecution()) > 0) {
            // Garantimos aqui (no IF acima) que a execu��o atrasada s� vai executar se realmente o agendamento ainda n�o foi executado. Ou seja, se a tarefa ainda n�o foi executada nenhuma vez (data da �ltima execu��o � nula), ou se a data programada para execu��o for maior do que da �ltima execu��o.
            if (task.getLateExecution().longValue() == -1) { // Se for como -1 (qualquer atraso), executamos agora mesmo
              execdate = RFW.getDateTime(); // Jogamos a data de "agora" para que depois o reagendamento j� seja futuro (se houver)!
            } else if (scheduleTime.plus(task.getLateExecution().longValue(), ChronoUnit.MILLIS).compareTo(now) > 0) { // Se o tempo do agendamento + regra de atraso forem maior que a data atual, ainda executamos essa tarefa.
              execdate = RFW.getDateTime(); // Jogamos a data de "agora" para que depois o reagendamento j� seja futuro (se houver)!
            }
          }
        }
      } else {
        // Se temos regras de repeti��o...
        switch (task.getRepeatFrequency()) {
          case TIMED: {
            // Em caso de repeti��o de tempos em tempos:
            // * calculamos a diferen�a entre AGORA e a primeira hora de execu��o;
            // * depois dividimos essa diferen�a pelo tempo de execu��o e obtemos quantas vezes a tarefa j� se repetiu (ou deveria) desde sua data inicial de agendamento;
            float repeats = (float) Math.floor(Duration.between(scheduleTime, now).abs().toMillis() / (float) task.getTimeToRepeat());

            // Sabendo as repeti��es, calculamos o hor�rio de qual teria sido a �ltima repeti��o da tarefa (anterior ao momento atual)
            LocalDateTime mostPastExecution = scheduleTime.plus((long) repeats * task.getTimeToRepeat(), ChronoUnit.MILLIS);

            // Verificamos se a �ltima execu��o + o tempo de atraso ainda est� dentro do prazo para executar esta tarefa agora. Tamb�m verificamos se o hor�rio calculado esta anterior � ultima execu��o para evitar que se repita sem parar
            if (task.getLateExecution() != null && (task.getLastExecution() != null || task.getLastExecution().compareTo(mostPastExecution) < 0) && (task.getLateExecution() == -1 || mostPastExecution.plus(task.getLateExecution(), ChronoUnit.MILLIS).compareTo(now) > 0)) {
              // Se est� no tempo, enviamos a data da �ltima execu��o, pois enviando tempo passado o Timing executa na hora
              execdate = mostPastExecution;
            } else {
              // Se n�o est� mais no prazo, enviamos a hora da pr�xima repeti��o, que basta somar a hora da �ltima execu��o passada com mais um tempo de repeti��o.
              execdate = mostPastExecution.plus(task.getTimeToRepeat(), ChronoUnit.MILLIS);
            }
          }
            break;
          case MONTHLY: {
            // Se � mensal temos que verificar o tempo da pr�xima execu��o de acordo com os parametros definidos
            LocalDateTime lastPastExecutionTime = null;
            LocalDateTime nextExecution = scheduleTime;
            do {
              lastPastExecutionTime = nextExecution; // Salvamos a �ltima refer�ncia como lastPastExecutionTime
              nextExecution = getNextExecutionMonthly(task, nextExecution);
            } while (nextExecution.compareTo(now) < 0);
            // Se temos uma data de execu��o entre "a �ltima execu��o" e a "pr�xima futura execu��o", verificamos se ela ainda � habil de ser executada de acordo com as regras de execu��o atrasada
            if (lastPastExecutionTime != null && task.getLateExecution() != null && (task.getLastExecution() == null || task.getLastExecution().compareTo(lastPastExecutionTime) < 0) && (task.getLateExecution() == -1 || lastPastExecutionTime.plus(task.getLateExecution(), ChronoUnit.MILLIS).compareTo(now) > 0)) {
              execdate = lastPastExecutionTime;
            } else {
              execdate = nextExecution;
            }
          }
            break;
          case DAILY: {
            LocalDateTime lastPastExecutionTime = null;
            LocalDateTime nextExecution = scheduleTime;
            // Este loop far� uma itera��o at� que tenhamos em nextExecution uma data furuta em rela��o da data atual. E no lastPastExecutionTime teremos o momento da execu��o da �ltima passada em rela��o ao momento atual.
            do {
              lastPastExecutionTime = nextExecution; // Salvamos a �ltima refer�ncia como lastPastExecutionTime
              nextExecution = getNextExecutionDaily(task, nextExecution);
            } while (nextExecution.compareTo(now) < 0);

            // Se temos uma data de execu��o entre "a �ltima execu��o" e a "pr�xima futura execu��o", verificamos se ela ainda � habil de ser executada de acordo com as regras de execu��o atrasada
            if (lastPastExecutionTime != null && task.getLateExecution() != null && (task.getLastExecution() == null || task.getLastExecution().compareTo(lastPastExecutionTime) < 0) && (task.getLateExecution() == -1 || lastPastExecutionTime.plus(task.getLateExecution(), ChronoUnit.MILLIS).compareTo(now) > 0)) {
              execdate = lastPastExecutionTime;
            } else {
              execdate = nextExecution;
            }
          }
            break;
        }
      }
    }

    // Por fim verificamos, se � uma data futura (indica que n�o � uma execu��o em atraso) garantimos que n�o � maior que a data de fim de execu��o. Se for anulamos o resultado para n�o executar a terefa
    if (task.getStopDate() != null && execdate.compareTo(task.getStopDate()) > 0) execdate = null;

    return execdate;
  }

  /**
   * M�todo auxiliar usado para calcular qual � a data da pr�xima execu��o para frequencia de repeti��o mensal. Note que � a pr�xima execu��o em rala��o a �ltima (se houver) ou em rela��o a data do agendamento. N�o necessariamente este m�todo retornar� a pr�xima execu��o futura!<br>
   * Este m�todo n�o faz valida��es no VO, apenas espera que seus dados estejam preenchidos corretamente.
   *
   * @param task
   * @param baseTime Permite informar uma data de refer�ncia para calcular a pr�xima execu��o a partir desta data. Caso nula ser� usada a data original do agendamento do VO (getScheduleTime())..
   * @return
   */
  private static LocalDateTime getNextExecutionMonthly(SchedulerTask task, LocalDateTime baseTime) {
    LocalDateTime nextExecution = null;
    // Para o agendamento mensal, partimos sempre da data de agendamento para n�o perder a precis�o do dia/horas/minutos/etc. que o LastExecution do VO atrasa a cada execu��o.
    if (baseTime == null) baseTime = task.getScheduleTime();// baseTime = LocalDateTime.of(2020, Month.OCTOBER, 15, 0, 0, 0)
    // Verificamos de quantos em quantos meses a repeti��o acontece
    int skipRecurrence = (task.getRecurrence() != null ? task.getRecurrence() : 1);
    // Verificamos se o avan�o ter� de ser de acordo com o dia do m�s, ou de acordo com a contagem do dia da semana (Ex: 2� quinta feita)
    if (task.getMonthlyRepeatByDayOfMonth() == null || task.getMonthlyRepeatByDayOfMonth()) {
      nextExecution = baseTime.plus(skipRecurrence, ChronoUnit.MONTHS);
    } else {
      // TODO corrigir esse c�digo para trabalhar s� com LocalDateTime, sem o GregorianCalendar e as convers�es de objetos temporais (Date e LocalDateTime)
      // Antes de incrementar salvamos o dia da semana, e quantas vezes esse dia da semana apareceu. Uma indica o dia da semana e a outra quantas vezes esse dia da semana j� apareceu dentro do m�s.
      // NOTE: que obtemos esse valor da data de agendamento original e n�o da �ltima execu��o. Isso porque a data original pode representar a "quinta" semana, e a �ltima execu��o pode indicar apenas a "quarta" caso aquele m�s n�o tenha a quinta. Para manter sempre o dia definido no in�cio temos que usar a data original.
      GregorianCalendar gc2 = new GregorianCalendar();
      gc2.setTime(BUDateTime.toDate(task.getScheduleTime()));
      int dwm = gc2.get(GregorianCalendar.DAY_OF_WEEK_IN_MONTH); // A "semana do m�s", que indica sempre o valor de "dia do m�s" / 7 - s� a parte inteira. O que junto com o Day_OF_Week ajuda a indicar qual � a ocorrencia daquele dia da semana dentro do m�s.
      int dw = gc2.get(GregorianCalendar.DAY_OF_WEEK);
      // Avan�amos quantos meses forem solicitados
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTime(BUDateTime.toDate(baseTime));
      gc.add(GregorianCalendar.MONTH, skipRecurrence);
      // Salvamos o m�s antes de acertar o dia da semana / m�s
      int month = gc.get(GregorianCalendar.MONTH);
      // Definimos a semana do m�s e o dia da semana desejados
      gc.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, dwm);
      gc.set(GregorianCalendar.DAY_OF_WEEK, dw);
      // Pode ocorrer do m�s "pular". Por exemplo se marcamos "a quinta sexta feira" s� que o m�s s� tem 4. Neste caso o GC pula para o pr�ximo mes. Quando isso ocorrer voltamos as semanas at� que chegue na �ltima semana do m�s esperado
      while (month != gc.get(GregorianCalendar.MONTH)) {
        gc.add(GregorianCalendar.WEEK_OF_YEAR, -1);
      }
      nextExecution = BUDateTime.toLocalDateTime(gc.getTime());
    }
    return nextExecution;
  }

  /**
   * M�todo auxiliar usado para calcular qual � a data da pr�xima execu��o para frequencia de repeti��o di�ria.<br>
   * Note que � a pr�xima execu��o em rala��o a �ltima execu��o (se j� houver), ou em rela��o a data do agendamento inicial. N�o necessariamente este m�todo retornar� a pr�xima execu��o futura!<br>
   * Este m�todo n�o faz valida��es no VO, apenas espera que seus dados estejam preenchidos corretamente.
   *
   * @param task Objeto do agendamento
   * @param baseTime Permite informar uma data de refer�ncia para calcular a pr�xima execu��o a partir desta data. Caso nula ser� usada a data original do agendamento do VO (getScheduleTime())..
   * @return
   */
  private static LocalDateTime getNextExecutionDaily(SchedulerTask task, LocalDateTime baseTime) {
    LocalDateTime nextExecution = null;
    // Se n�o recebermos uma data como base, pegamos a data de agendamento do pr�prio VO
    if (baseTime == null) baseTime = task.getScheduleTime();// baseTime = LocalDateTime.of(2020, Month.OCTOBER, 15, 0, 0, 0)
    // Verificamos de quantos em quantos dias a repeti��o acontece
    int skipRecurrence = (task.getRecurrence() != null ? task.getRecurrence() : 1);
    // Verificamos se o avan�o ter� de ser de acordo com o dia do m�s, ou de acordo com a contagem do dia da semana (Ex: 2� quinta feita)
    nextExecution = baseTime.plus(skipRecurrence, ChronoUnit.DAYS);
    return nextExecution;
  }

  /**
   * Este m�todo � chamado para cancelar o agendamento de todas as tarefas agemdadas;
   */
  public static void cancelAllTaks() {
    final SchedulerController i = SchedulerController.getInstance();
    final LinkedList<Long> keyset = new LinkedList<>(i.taskTimingHash.keySet());
    for (Long id : keyset) {
      SchedulerTaskTiming tasktiming = i.taskTimingHash.get(id);
      tasktiming.cancelTimer();
      i.taskTimingHash.remove(id);
    }
  }

  /**
   * Este m�todo � chamado para cancelar o agendamento de uma tarefa pelo seu ID;
   */
  public static void cancelTak(Long id) {
    final SchedulerController i = SchedulerController.getInstance();

    SchedulerTaskTiming tasktiming = i.taskTimingHash.get(id);
    tasktiming.cancelTimer();
    i.taskTimingHash.remove(id);
  }

  public static void addListener(SchedulerListener listener) {
    listeners.add(listener);
  }

  public static void removeListener(SchedulerListener listener) {
    listeners.remove(listener);
  }

  protected static void fireFailEvent(SchedulerTask task, RFWException e) {
    for (SchedulerListener listener : listeners) {
      try {
        listener.fail(task, e);
      } catch (Throwable t) {
        RFWLogger.logException(t, task.getTaskClass()); // Loga, mas n�o deixa o listener estragar o funcionamento do Scheduler
      }
    }
  }

  protected static void fireSuccessEvent(SchedulerTask task) {
    for (SchedulerListener listener : listeners) {
      try {
        listener.success(task);
      } catch (Throwable t) {
        RFWLogger.logException(t, task.getTaskClass()); // Loga, mas n�o deixa o listener estragar o funcionamento do Scheduler
      }
    }
  }

  /**
   * Gera um ID negativo sequencial para que as tarefas que n�o tenham seus pr�prios IDs �nicos possam garantir um ID �nico.<br>
   * � importante que todas as tarefas do sistema tenham um ID �nico. Esse m�todo gerar� IDs sempre negativos para evitar conflitos com tarefas que tenham ID por serem persistidas em banco de dados.
   *
   * @return Pr�ximo ID negativo sequencial.
   */
  public static long generateID() {
    return --SchedulerController.lastID;
  }

  /**
   * Recupera uma lista com todas as tarefas atualmente agendadas no sistema.<br>
   * Tarefas que foram executadas e n�o s�o reagendadas s�o eliminadas.
   *
   * @return Lista com os objetos {@link SchedulerTaskTiming} representando cada tarefa agendada.
   */
  public static ArrayList<SchedulerTaskTiming> getTasks() {
    return new ArrayList<SchedulerTaskTiming>(SchedulerController.instance.taskTimingHash.values());
  }

  /**
   * Este m�todo for�a a inicializa��o de uma tarefa imediatamente
   *
   * @param id Identificador �nico da tarefa.
   * @throws RFWException
   */
  public static void executeTaskNow(Long id) throws RFWException {
    SchedulerTaskTiming tTiming = getInstance().taskTimingHash.get(id);
    if (tTiming == null) throw new RFWCriticalException("Tarefa n�o encontrada no SchedulerController! ID: ${0}", new String[] { "" + id });
    SchedulerTask task = tTiming.getSchedulerTask();

    cancelTak(id);

    processTask(task, true);
  }
}
