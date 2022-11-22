/*
 *
 */
package br.eng.rodrigogml.rfw.base.scheduler;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.scheduler.interfaces.SchedulerRunnable;
import br.eng.rodrigogml.rfw.base.scheduler.interfaces.SchedulerTask;
import br.eng.rodrigogml.rfw.base.utils.BUDateTime;

/**
 * Description: Esta classe serve para gerenciar execução da tarefa que está agendada pelo SchedulerController.
 *
 * @author Rodrigo Leitão
 * @since 4.2.0 (27/10/2011)
 */
public class SchedulerTaskTiming extends TimerTask implements Serializable {

  private static final long serialVersionUID = -7972178243987860155L;

  public static enum TaskStatus {
    /**
     * Este status indica que a tarefa está agendada para ser executada em algum momento.
     */
    SCHEDULED,
    /**
     * Este status indica que a tarefa está em execução neste momento.
     */
    RUNNING,
    /**
     * Este status indica que a tarefa está parada, isto é, não está sendo executada e nem está agendada executar no futuro.
     */
    STOPED
  }

  /**
   * Timer criado para iniciar a tarefa no momento exato.
   */
  private final Timer timer;

  /**
   * Instância da task a ser executada.
   */
  private SchedulerRunnable task = null;

  /**
   * Referência para o SchedulerVO que critou esse SchedulerTaskTiming.
   */
  private SchedulerTask schedulerTask = null;

  /**
   * Status atual da "operação".
   */
  private TaskStatus status = TaskStatus.STOPED;

  /**
   * Horário definido para próxima execução. Horário futuro enquanto o status for {@link TaskStatus#SCHEDULED}
   */
  private LocalDateTime lastScheduledTime = null;

  public SchedulerTaskTiming(SchedulerTask task) {
    this.schedulerTask = task;
    String[] split = task.getTaskClass().split("\\.");
    this.timer = new Timer("### SchedulerTaskTiming: " + split[split.length - 1]);
  }

  /**
   * Agenda o Timer para ser executado aqui a X tempo (em milisegundos) a contar de agora.
   *
   * @param delay tempo em milisegundos para esperar antes de executar a tarefa.
   */
  public synchronized void schedule(long delay) {
    schedule(RFW.getDateTime().plus(delay, ChronoUnit.MILLIS));
  }

  /**
   * Agenda a tarefa para um terminado horário.
   *
   * @param time Data/Hora para executar a tarefa.
   */
  public synchronized void schedule(LocalDateTime time) {
    schedule(time, false);
  }

  /**
   * Agenda a tarefa para um terminado horário.
   *
   * @param time Data/Hora para executar a tarefa.
   * @param runNow Apesar do horário de agendamento, se aqui for passado como True a tarefa será executada imediatamente. A data passada será utilizada apenas para o cálculo da próxima execução, caso de ser uma tarefa recorrente.
   */
  public synchronized void schedule(LocalDateTime time, boolean runNow) {
    this.status = TaskStatus.SCHEDULED;
    this.lastScheduledTime = time;
    if (runNow) {
      timer.schedule(this, 0);
    } else {
      timer.schedule(this, BUDateTime.toDate(time));
    }
  }

  /**
   * Este método só cancela o timer, não interrompe sua execução caso a atividade já esteja em execução.
   */
  public synchronized void cancelTimer() {
    this.cancel();
    timer.cancel();
    this.status = TaskStatus.STOPED;
  }

  /**
   * Método chamado quando chegar a hora da task ser executada.
   */
  @Override
  public synchronized void run() {
    try {
      if (TaskStatus.SCHEDULED.equals(this.status)) {
        RFWLogger.logDebug("Executando tarefa do Scheduler ID:" + this.schedulerTask.getId() + " Class:" + this.schedulerTask.getTaskClass());
        Throwable failEx = null;
        try {
          this.status = TaskStatus.RUNNING;
          // Converte as propriedades para a interface da tarefa
          Map<String, String> newProperties = getTask().runTask(this.schedulerTask.getProperties());
          // Caso a Hash retornada não seja nula, significa que devemos salvar as novas propriedades
          if (newProperties != null) {
            this.schedulerTask.setProperties(newProperties);
          }
        } catch (Throwable e) {
          RFWLogger.logException(e);
          failEx = e;
        }

        this.status = TaskStatus.STOPED;
        this.schedulerTask.setLastExecution(RFW.getDateTime()); // SEMPRE salvamos a data em que a execução terminou, não confundir com a data de último agendamento (que não é salva em lugar nenhum). Dependendo do tipo de repetição (Se timed ou por periodo do calendário) será utilizada esta data ou a data original da primeira execução para o cálculo da repetição
        this.schedulerTask.setScheduleTime(lastScheduledTime); // Atualiza a data de atualização (que agora é passada) para uma referência mais próxima. Caso contrário com o tempo a data ficará muito antiga e a cada processamento do agendamento teremos mais iterações para encontrar uma data futura (nas tarefas de repetição).

        try {
          if (failEx == null) {
            SchedulerController.fireSuccessEvent(this.schedulerTask);
          } else {
            SchedulerController.fireFailEvent(this.schedulerTask, new RFWCriticalException("Erro ao executar a tarefa: '" + this.getSchedulerTask().getTaskClass() + "'", failEx));
          }
        } catch (Throwable e) {
          // Se ocorrer algum erro logamos, mas deixamos a tarefa continuar sua programação
          RFWLogger.logException(e);
        }
        // Agora que a task terminou, processamos a task novamente, caso ela tenha repetição precisa ser reagendada
        SchedulerController.processTask(this.schedulerTask, false);
      }
    } catch (Throwable e) {
      RFWLogger.logException(e);
    }
  }

  /**
   * # instância da task a ser executada.
   *
   * @return the instância da task a ser executada
   */
  public SchedulerRunnable getTask() throws RFWCriticalException {
    if (this.task == null) {
      try {
        Class<?> clazz = SchedulerTaskTiming.class.getClassLoader().loadClass(this.schedulerTask.getTaskClass());
        this.task = (SchedulerRunnable) clazz.getConstructor().newInstance();
      } catch (Exception e) {
        throw new RFWCriticalException("Erro ao executar tarefa agendada.", e);
      }
    }
    return this.task;
  }

  /**
   * # referência para o SchedulerVO que critou esse SchedulerTaskTiming.
   *
   * @return the referência para o SchedulerVO que critou esse SchedulerTaskTiming
   */
  public SchedulerTask getSchedulerTask() {
    return schedulerTask;
  }

  /**
   * # status atual da "operação".
   *
   * @return the status atual da "operação"
   */
  public TaskStatus getStatus() {
    return status;
  }

  /**
   * # horário definido para próxima execução. Horário futuro enquanto o status for {@link TaskStatus#SCHEDULED}.
   *
   * @return the horário definido para próxima execução
   */
  public LocalDateTime getLastScheduledTime() {
    return lastScheduledTime;
  }

}