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
 * Description: Classe de controle das tarefas agendadas do sistema. Reponsável por carrega-las, inicial-las e executa-las conforme suas regras.<br>
 * O SchedulerController é único para todas as empresas do sistema
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (27 de set de 2018)
 */
public class SchedulerController {

  /**
   * Instância do Singleton.
   */
  private static SchedulerController instance = null;

  /**
   * Controlador da geração de IDs automáticos.
   */
  private static long lastID = 0;

  /**
   * Lista de Listeners de Eventos do SchedulerController
   */
  private static List<SchedulerListener> listeners = new LinkedList<SchedulerListener>();

  /**
   * Mantém os agendamentos das tarefas em uma hash de acordo com o ID do SchedulingVO.
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
   * @param tasks Implementações do {@link SchedulerVO}.
   * @throws RFWException
   */
  public static void loadTasks(SchedulerTask... tasks) throws RFWException {
    for (SchedulerTask task : tasks) {
      processTask(task, false);
    }
  }

  protected static void processTask(SchedulerTask task, boolean runNow) {
    RFWLogger.logDebug("Processando Tarefa: " + task.getTaskClass(), task.getTaskClass());

    // Verifica se esta task já está no controller
    SchedulerTaskTiming taskTiming = getInstance().taskTimingHash.get(task.getId());
    if (taskTiming != null) {
      // Se encontrou criamos um novo tasktiming pois este não pode ser usado duas vezes, e por garantia de que não estamos reprocessando algo qua ainda não ocorreu, cancelamos a primeira...
      taskTiming.cancelTimer();
      getInstance().taskTimingHash.remove(task.getId());
    }

    try {
      LocalDateTime nextExecution = calcTaskNextExecution(task);
      if (nextExecution != null) {
        RFWLogger.logDebug("Tarefa '" + task.getTaskClass() + "' próxima execução agendada para " + RFW.getDateTimeFormattter().format(nextExecution), task.getTaskClass());
        // se temos uma nova execução, criamos o SchedulerTaskTiming
        // CUIDADO: Se criar o SchedulerTaskTiming ele automaticamente já cria a Thread e mantém ela em memória com o Timer dentro dela, mesmo sem agendar (chamar o método .schedule())
        taskTiming = new SchedulerTaskTiming(task);
        // Se tem a data de próxima execução, criamos o agendamento e colocamos na Hash
        getInstance().taskTimingHash.put(task.getId(), taskTiming);
        taskTiming.schedule(nextExecution, runNow);
      }
    } catch (RFWException e) {
      RFWLogger.logException(e, task.getTaskClass());
    }
  }

  /**
   * Este método calcula o tempo da próxima execução de um determinado agendamento.
   *
   * @param task
   * @return Date com a hora da próxima execução, ou null caso a tarefa não deva mais ser executada.
   */
  public static LocalDateTime calcTaskNextExecution(SchedulerTask task) throws RFWException {
    LocalDateTime execdate = null;
    final LocalDateTime now = RFW.getDateTime(); // Fixamos a hora atual pois mesmo que esse processamento demore, a hora será contabilizada a partir dessa variável, e não uma que fique mudando de linha em linha ;) - Sistema lento eim?
    LocalDateTime scheduleTime = task.getScheduleTime();

    if (scheduleTime.compareTo(now) > 0) {
      // Se a data de agendamento inicial é futura, retornamos essa própria data, pois se ela ainda não aconteceu nem temos que verificar lógica de repetição!
      execdate = task.getScheduleTime();
    } else {
      if (task.getRepeatFrequency() == null) {
        // Se a tarefa não tem repetição, vamos verificar a questão da execução atrasada.
        if (task.getLateExecution() != null) {
          // Se tem política de execução com atraso definida...
          if (task.getLastExecution() == null || task.getScheduleTime().compareTo(task.getLastExecution()) > 0) {
            // Garantimos aqui (no IF acima) que a execução atrasada só vai executar se realmente o agendamento ainda não foi executado. Ou seja, se a tarefa ainda não foi executada nenhuma vez (data da última execução é nula), ou se a data programada para execução for maior do que da última execução.
            if (task.getLateExecution().longValue() == -1) { // Se for como -1 (qualquer atraso), executamos agora mesmo
              execdate = RFW.getDateTime(); // Jogamos a data de "agora" para que depois o reagendamento já seja futuro (se houver)!
            } else if (scheduleTime.plus(task.getLateExecution().longValue(), ChronoUnit.MILLIS).compareTo(now) > 0) { // Se o tempo do agendamento + regra de atraso forem maior que a data atual, ainda executamos essa tarefa.
              execdate = RFW.getDateTime(); // Jogamos a data de "agora" para que depois o reagendamento já seja futuro (se houver)!
            }
          }
        }
      } else {
        // Se temos regras de repetição...
        switch (task.getRepeatFrequency()) {
          case TIMED: {
            // Em caso de repetição de tempos em tempos:
            // * calculamos a diferença entre AGORA e a primeira hora de execução;
            // * depois dividimos essa diferença pelo tempo de execução e obtemos quantas vezes a tarefa já se repetiu (ou deveria) desde sua data inicial de agendamento;
            float repeats = (float) Math.floor(Duration.between(scheduleTime, now).abs().toMillis() / (float) task.getTimeToRepeat());

            // Sabendo as repetições, calculamos o horário de qual teria sido a última repetição da tarefa (anterior ao momento atual)
            LocalDateTime mostPastExecution = scheduleTime.plus((long) repeats * task.getTimeToRepeat(), ChronoUnit.MILLIS);

            // Verificamos se a última execução + o tempo de atraso ainda está dentro do prazo para executar esta tarefa agora. Também verificamos se o horário calculado esta anterior à ultima execução para evitar que se repita sem parar
            if (task.getLateExecution() != null && (task.getLastExecution() != null || task.getLastExecution().compareTo(mostPastExecution) < 0) && (task.getLateExecution() == -1 || mostPastExecution.plus(task.getLateExecution(), ChronoUnit.MILLIS).compareTo(now) > 0)) {
              // Se está no tempo, enviamos a data da última execução, pois enviando tempo passado o Timing executa na hora
              execdate = mostPastExecution;
            } else {
              // Se não está mais no prazo, enviamos a hora da próxima repetição, que basta somar a hora da última execução passada com mais um tempo de repetição.
              execdate = mostPastExecution.plus(task.getTimeToRepeat(), ChronoUnit.MILLIS);
            }
          }
            break;
          case MONTHLY: {
            // Se é mensal temos que verificar o tempo da próxima execução de acordo com os parametros definidos
            LocalDateTime lastPastExecutionTime = null;
            LocalDateTime nextExecution = scheduleTime;
            do {
              lastPastExecutionTime = nextExecution; // Salvamos a última referência como lastPastExecutionTime
              nextExecution = getNextExecutionMonthly(task, nextExecution);
            } while (nextExecution.compareTo(now) < 0);
            // Se temos uma data de execução entre "a última execução" e a "próxima futura execução", verificamos se ela ainda é habil de ser executada de acordo com as regras de execução atrasada
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
            // Este loop fará uma iteração até que tenhamos em nextExecution uma data furuta em relação da data atual. E no lastPastExecutionTime teremos o momento da execução da última passada em relação ao momento atual.
            do {
              lastPastExecutionTime = nextExecution; // Salvamos a última referência como lastPastExecutionTime
              nextExecution = getNextExecutionDaily(task, nextExecution);
            } while (nextExecution.compareTo(now) < 0);

            // Se temos uma data de execução entre "a última execução" e a "próxima futura execução", verificamos se ela ainda é habil de ser executada de acordo com as regras de execução atrasada
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

    // Por fim verificamos, se é uma data futura (indica que não é uma execução em atraso) garantimos que não é maior que a data de fim de execução. Se for anulamos o resultado para não executar a terefa
    if (task.getStopDate() != null && execdate.compareTo(task.getStopDate()) > 0) execdate = null;

    return execdate;
  }

  /**
   * Método auxiliar usado para calcular qual é a data da próxima execução para frequencia de repetição mensal. Note que é a próxima execução em ralação a última (se houver) ou em relação a data do agendamento. Não necessariamente este método retornará a próxima execução futura!<br>
   * Este método não faz validações no VO, apenas espera que seus dados estejam preenchidos corretamente.
   *
   * @param task
   * @param baseTime Permite informar uma data de referência para calcular a próxima execução a partir desta data. Caso nula será usada a data original do agendamento do VO (getScheduleTime())..
   * @return
   */
  private static LocalDateTime getNextExecutionMonthly(SchedulerTask task, LocalDateTime baseTime) {
    LocalDateTime nextExecution = null;
    // Para o agendamento mensal, partimos sempre da data de agendamento para não perder a precisão do dia/horas/minutos/etc. que o LastExecution do VO atrasa a cada execução.
    if (baseTime == null) baseTime = task.getScheduleTime();// baseTime = LocalDateTime.of(2020, Month.OCTOBER, 15, 0, 0, 0)
    // Verificamos de quantos em quantos meses a repetição acontece
    int skipRecurrence = (task.getRecurrence() != null ? task.getRecurrence() : 1);
    // Verificamos se o avanço terá de ser de acordo com o dia do mês, ou de acordo com a contagem do dia da semana (Ex: 2° quinta feita)
    if (task.getMonthlyRepeatByDayOfMonth() == null || task.getMonthlyRepeatByDayOfMonth()) {
      nextExecution = baseTime.plus(skipRecurrence, ChronoUnit.MONTHS);
    } else {
      // TODO corrigir esse código para trabalhar só com LocalDateTime, sem o GregorianCalendar e as conversões de objetos temporais (Date e LocalDateTime)
      // Antes de incrementar salvamos o dia da semana, e quantas vezes esse dia da semana apareceu. Uma indica o dia da semana e a outra quantas vezes esse dia da semana já apareceu dentro do mês.
      // NOTE: que obtemos esse valor da data de agendamento original e não da última execução. Isso porque a data original pode representar a "quinta" semana, e a última execução pode indicar apenas a "quarta" caso aquele mês não tenha a quinta. Para manter sempre o dia definido no início temos que usar a data original.
      GregorianCalendar gc2 = new GregorianCalendar();
      gc2.setTime(BUDateTime.toDate(task.getScheduleTime()));
      int dwm = gc2.get(GregorianCalendar.DAY_OF_WEEK_IN_MONTH); // A "semana do mês", que indica sempre o valor de "dia do mês" / 7 - só a parte inteira. O que junto com o Day_OF_Week ajuda a indicar qual é a ocorrencia daquele dia da semana dentro do mês.
      int dw = gc2.get(GregorianCalendar.DAY_OF_WEEK);
      // Avançamos quantos meses forem solicitados
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTime(BUDateTime.toDate(baseTime));
      gc.add(GregorianCalendar.MONTH, skipRecurrence);
      // Salvamos o mês antes de acertar o dia da semana / mês
      int month = gc.get(GregorianCalendar.MONTH);
      // Definimos a semana do mês e o dia da semana desejados
      gc.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, dwm);
      gc.set(GregorianCalendar.DAY_OF_WEEK, dw);
      // Pode ocorrer do mês "pular". Por exemplo se marcamos "a quinta sexta feira" só que o mês só tem 4. Neste caso o GC pula para o próximo mes. Quando isso ocorrer voltamos as semanas até que chegue na última semana do mês esperado
      while (month != gc.get(GregorianCalendar.MONTH)) {
        gc.add(GregorianCalendar.WEEK_OF_YEAR, -1);
      }
      nextExecution = BUDateTime.toLocalDateTime(gc.getTime());
    }
    return nextExecution;
  }

  /**
   * Método auxiliar usado para calcular qual é a data da próxima execução para frequencia de repetição diária.<br>
   * Note que é a próxima execução em ralação a última execução (se já houver), ou em relação a data do agendamento inicial. Não necessariamente este método retornará a próxima execução futura!<br>
   * Este método não faz validações no VO, apenas espera que seus dados estejam preenchidos corretamente.
   *
   * @param task Objeto do agendamento
   * @param baseTime Permite informar uma data de referência para calcular a próxima execução a partir desta data. Caso nula será usada a data original do agendamento do VO (getScheduleTime())..
   * @return
   */
  private static LocalDateTime getNextExecutionDaily(SchedulerTask task, LocalDateTime baseTime) {
    LocalDateTime nextExecution = null;
    // Se não recebermos uma data como base, pegamos a data de agendamento do próprio VO
    if (baseTime == null) baseTime = task.getScheduleTime();// baseTime = LocalDateTime.of(2020, Month.OCTOBER, 15, 0, 0, 0)
    // Verificamos de quantos em quantos dias a repetição acontece
    int skipRecurrence = (task.getRecurrence() != null ? task.getRecurrence() : 1);
    // Verificamos se o avanço terá de ser de acordo com o dia do mês, ou de acordo com a contagem do dia da semana (Ex: 2° quinta feita)
    nextExecution = baseTime.plus(skipRecurrence, ChronoUnit.DAYS);
    return nextExecution;
  }

  /**
   * Este método é chamado para cancelar o agendamento de todas as tarefas agemdadas;
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
   * Este método é chamado para cancelar o agendamento de uma tarefa pelo seu ID;
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
        RFWLogger.logException(t, task.getTaskClass()); // Loga, mas não deixa o listener estragar o funcionamento do Scheduler
      }
    }
  }

  protected static void fireSuccessEvent(SchedulerTask task) {
    for (SchedulerListener listener : listeners) {
      try {
        listener.success(task);
      } catch (Throwable t) {
        RFWLogger.logException(t, task.getTaskClass()); // Loga, mas não deixa o listener estragar o funcionamento do Scheduler
      }
    }
  }

  /**
   * Gera um ID negativo sequencial para que as tarefas que não tenham seus próprios IDs únicos possam garantir um ID único.<br>
   * É importante que todas as tarefas do sistema tenham um ID único. Esse método gerará IDs sempre negativos para evitar conflitos com tarefas que tenham ID por serem persistidas em banco de dados.
   *
   * @return Próximo ID negativo sequencial.
   */
  public static long generateID() {
    return --SchedulerController.lastID;
  }

  /**
   * Recupera uma lista com todas as tarefas atualmente agendadas no sistema.<br>
   * Tarefas que foram executadas e não são reagendadas são eliminadas.
   *
   * @return Lista com os objetos {@link SchedulerTaskTiming} representando cada tarefa agendada.
   */
  public static ArrayList<SchedulerTaskTiming> getTasks() {
    return new ArrayList<SchedulerTaskTiming>(SchedulerController.instance.taskTimingHash.values());
  }

  /**
   * Este método força a inicialização de uma tarefa imediatamente
   *
   * @param id Identificador único da tarefa.
   * @throws RFWException
   */
  public static void executeTaskNow(Long id) throws RFWException {
    SchedulerTaskTiming tTiming = getInstance().taskTimingHash.get(id);
    if (tTiming == null) throw new RFWCriticalException("Tarefa não encontrada no SchedulerController! ID: ${0}", new String[] { "" + id });
    SchedulerTask task = tTiming.getSchedulerTask();

    cancelTak(id);

    processTask(task, true);
  }
}
