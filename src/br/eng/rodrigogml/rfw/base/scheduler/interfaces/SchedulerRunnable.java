package br.eng.rodrigogml.rfw.base.scheduler.interfaces;

import java.util.Map;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Esta interface para implementar a tarefa execut�vel do Scheduler.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 4.2.0 (26/10/2011)
 */
public interface SchedulerRunnable {

  /**
   * M�todo chamado na hora em que a tarefa se iniciar.
   */
  public Map<String, String> runTask(Map<String, String> properties) throws RFWException;

}
