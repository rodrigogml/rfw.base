package br.eng.rodrigogml.rfw.base.jobmonitor.interfaces;

import br.eng.rodrigogml.rfw.base.jobmonitor.Job;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobMonitor;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Interface do JobMonitor que permite implementar um "Supplier".<br>
 *
 * Supplier � um pattern semelhante ao Factory, mas ao inv�s de criar um objeto, ele � capaz de encontrar um j� existente.<Br>
 * <br>
 * No caso do {@link JobStatusSupplier} sua fun��o � encontrar o {@link JobStatus} de uma determinada tarefa. Quando estamos utilizando a mesma JVM de quem criou o {@link Job}, podemos encontrar o objeto apenas chamando o {@link JobMonitor#getJobStatus(String)} estaticamente. Mas se a tarefa estiver rodando em outra VM ser� preciso acessa-lo a partir de uma Fachada de servi�os. Nesses casos �
 * preciso imeplementar essa busca utilizando essa interface.
 *
 * @author Rodrigo GML
 * @since 10.0 (30 de out de 2020)
 */
public interface JobStatusSupplier {

  /**
   * Recupera o JobStatus de acordo com o jobUUID.
   *
   * @param jobUUID Identificador do Job.
   * @return Objeto de status da tarefa.
   * @throws RFWException
   */
  public JobStatus getJobStatus(String jobUUID) throws RFWException;

  /**
   * Deve chamar o m�todo de limpeza {@link JobMonitor#cleanJob(String)} para liberar os recursos do sistema.
   *
   * @param jobUUID Idenficiador do Job.
   * @return true caso o job tenha sido encontrado no JobMonitor, false caso contr�rio.
   * @throws RFWException
   */
  public boolean cleanJob(String jobUUID) throws RFWException;

  /**
   * Solicita que a tarefa seja interrompida (se suportar). Aborta no meio do processamento la�ando exce��o de Valida��o com o c�digo "RFW_ERR_000004".
   *
   * @param jobUUID Idenficiador do Job.
   */
  public void interrupt(String jobUUID) throws RFWException;

  /**
   * Solicita que a tarefa seja interrompida (se suportar). Mas passa uma exception personalizada para realizar o canelamento do trabalho.
   *
   * @param jobUUID Idenficiador do Job.
   * @param ex Exception com o motivo do cancelamento.
   */
  public void interrupt(String jobUUID, RFWException ex) throws RFWException;

}
