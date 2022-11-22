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
 * Description: Esta classe serve para gerenciar execu��o da tarefa que est� agendada pelo SchedulerController.
 *
 * @author Rodrigo Leit�o
 * @since 4.2.0 (27/10/2011)
 */
public class SchedulerTaskTiming extends TimerTask implements Serializable {

  private static final long serialVersionUID = -7972178243987860155L;

  public static enum TaskStatus {
    /**
     * Este status indica que a tarefa est� agendada para ser executada em algum momento.
     */
    SCHEDULED,
    /**
     * Este status indica que a tarefa est� em execu��o neste momento.
     */
    RUNNING,
    /**
     * Este status indica que a tarefa est� parada, isto �, n�o est� sendo executada e nem est� agendada executar no futuro.
     */
    STOPED
  }

  /**
   * Timer criado para iniciar a tarefa no momento exato.
   */
  private final Timer timer;

  /**
   * Inst�ncia da task a ser executada.
   */
  private SchedulerRunnable task = null;

  /**
   * Refer�ncia para o SchedulerVO que critou esse SchedulerTaskTiming.
   */
  private SchedulerTask schedulerTask = null;

  /**
   * Status atual da "opera��o".
   */
  private TaskStatus status = TaskStatus.STOPED;

  /**
   * Hor�rio definido para pr�xima execu��o. Hor�rio futuro enquanto o status for {@link TaskStatus#SCHEDULED}
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
   * Agenda a tarefa para um terminado hor�rio.
   *
   * @param time Data/Hora para executar a tarefa.
   */
  public synchronized void schedule(LocalDateTime time) {
    schedule(time, false);
  }

  /**
   * Agenda a tarefa para um terminado hor�rio.
   *
   * @param time Data/Hora para executar a tarefa.
   * @param runNow Apesar do hor�rio de agendamento, se aqui for passado como True a tarefa ser� executada imediatamente. A data passada ser� utilizada apenas para o c�lculo da pr�xima execu��o, caso de ser uma tarefa recorrente.
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
   * Este m�todo s� cancela o timer, n�o interrompe sua execu��o caso a atividade j� esteja em execu��o.
   */
  public synchronized void cancelTimer() {
    this.cancel();
    timer.cancel();
    this.status = TaskStatus.STOPED;
  }

  /**
   * M�todo chamado quando chegar a hora da task ser executada.
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
          // Caso a Hash retornada n�o seja nula, significa que devemos salvar as novas propriedades
          if (newProperties != null) {
            this.schedulerTask.setProperties(newProperties);
          }
        } catch (Throwable e) {
          RFWLogger.logException(e);
          failEx = e;
        }

        this.status = TaskStatus.STOPED;
        this.schedulerTask.setLastExecution(RFW.getDateTime()); // SEMPRE salvamos a data em que a execu��o terminou, n�o confundir com a data de �ltimo agendamento (que n�o � salva em lugar nenhum). Dependendo do tipo de repeti��o (Se timed ou por periodo do calend�rio) ser� utilizada esta data ou a data original da primeira execu��o para o c�lculo da repeti��o
        this.schedulerTask.setScheduleTime(lastScheduledTime); // Atualiza a data de atualiza��o (que agora � passada) para uma refer�ncia mais pr�xima. Caso contr�rio com o tempo a data ficar� muito antiga e a cada processamento do agendamento teremos mais itera��es para encontrar uma data futura (nas tarefas de repeti��o).

        try {
          if (failEx == null) {
            SchedulerController.fireSuccessEvent(this.schedulerTask);
          } else {
            SchedulerController.fireFailEvent(this.schedulerTask, new RFWCriticalException("Erro ao executar a tarefa: '" + this.getSchedulerTask().getTaskClass() + "'", failEx));
          }
        } catch (Throwable e) {
          // Se ocorrer algum erro logamos, mas deixamos a tarefa continuar sua programa��o
          RFWLogger.logException(e);
        }
        // Agora que a task terminou, processamos a task novamente, caso ela tenha repeti��o precisa ser reagendada
        SchedulerController.processTask(this.schedulerTask, false);
      }
    } catch (Throwable e) {
      RFWLogger.logException(e);
    }
  }

  /**
   * # inst�ncia da task a ser executada.
   *
   * @return the inst�ncia da task a ser executada
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
   * # refer�ncia para o SchedulerVO que critou esse SchedulerTaskTiming.
   *
   * @return the refer�ncia para o SchedulerVO que critou esse SchedulerTaskTiming
   */
  public SchedulerTask getSchedulerTask() {
    return schedulerTask;
  }

  /**
   * # status atual da "opera��o".
   *
   * @return the status atual da "opera��o"
   */
  public TaskStatus getStatus() {
    return status;
  }

  /**
   * # hor�rio definido para pr�xima execu��o. Hor�rio futuro enquanto o status for {@link TaskStatus#SCHEDULED}.
   *
   * @return the hor�rio definido para pr�xima execu��o
   */
  public LocalDateTime getLastScheduledTime() {
    return lastScheduledTime;
  }

}