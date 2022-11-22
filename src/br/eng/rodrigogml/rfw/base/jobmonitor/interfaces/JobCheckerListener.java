package br.eng.rodrigogml.rfw.base.jobmonitor.interfaces;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobChecker;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus.JobStep;

/**
 * Description: Interface que define um Listener Utilizado pelo {@link JobChecker} para notificar sobre as altera��es da tarefa.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (31 de out de 2020)
 */
public interface JobCheckerListener {

  /**
   * M�todo que ser� chamado periodicamente com o status do Job.<br>
   * Este m�todo s� ser� chamado quando o JobChecker notar alguma diferen�a entre os atributos do objeto.<br>
   * � garantida a chamada do m�todo updateStatus() depois do {@link #start()} pelo menos uma �nica vez depois que o set chegar em {@link JobStep#FINISHED}.
   *
   * @param status Objeto com os status do Job.
   * @param lastCall caso true, indica que � a �ltima chamada do JobChecker
   */
  public void updateStatus(JobStatus jobStatus, boolean lastCall);

}
