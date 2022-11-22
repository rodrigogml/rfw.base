package br.eng.rodrigogml.rfw.base.jobmonitor.interfaces;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobChecker;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus.JobStep;

/**
 * Description: Interface que define um Listener Utilizado pelo {@link JobChecker} para notificar sobre as alterações da tarefa.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (31 de out de 2020)
 */
public interface JobCheckerListener {

  /**
   * Método que será chamado periodicamente com o status do Job.<br>
   * Este método só será chamado quando o JobChecker notar alguma diferença entre os atributos do objeto.<br>
   * É garantida a chamada do método updateStatus() depois do {@link #start()} pelo menos uma única vez depois que o set chegar em {@link JobStep#FINISHED}.
   *
   * @param status Objeto com os status do Job.
   * @param lastCall caso true, indica que é a última chamada do JobChecker
   */
  public void updateStatus(JobStatus jobStatus, boolean lastCall);

}
