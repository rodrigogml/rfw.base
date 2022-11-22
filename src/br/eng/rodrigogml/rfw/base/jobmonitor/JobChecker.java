package br.eng.rodrigogml.rfw.base.jobmonitor;

import java.util.LinkedList;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus.JobStep;
import br.eng.rodrigogml.rfw.base.jobmonitor.interfaces.JobCheckerListener;
import br.eng.rodrigogml.rfw.base.jobmonitor.interfaces.JobStatusSupplier;
import br.eng.rodrigogml.rfw.base.jobmonitor.interfaces.JobStatusSupplierDefault;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;

/**
 * Description: JobChecker � uma classe utilit�ria para for�ar a verifica��o tempor�ria de um Job em background.<br>
 * O JobChecker, depois de chamado o m�todo {@link #start()}, inicia um Thread que verifica o status do Job de tempos em tempos e dispara o m�todo para atualiza��o.<br>
 * A Thread para automaticamente quando o {@link JobStep#FINISHED}.
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public class JobChecker implements Runnable {

  /**
   * Identificador �nico do Job a ser verificado.
   */
  private final String jobUUID;

  /**
   * Thread de verifica��o do Job
   */
  private Thread t;

  /**
   * Refer�ncia para o {@link JobStatusSupplier} do {@link JobStatus}
   */
  private final JobStatusSupplier jobSupplier;

  /**
   * Listeners que receber�o as notifica��es da tarefa.
   */
  private final LinkedList<JobCheckerListener> listeners = new LinkedList<JobCheckerListener>();

  // Deixei o construtor sem fornecer o JobStatusSupplier comentado de prop�sito, para for�ar o dev a lembrar em que classloader ele est� e a necessidade de passar o JobStatusSupplier ou null para o padr�o.
  // /**
  // * Cria uma nova inst�ncia de JobChecker para monitorar um Job.<br>
  // * <b>Aten��o:</b> Este construtor s� deve ser utilizado quando o JobChecker est� sendo inst�nciado dentro da mesma JVM que a tarefa foi criada. Caso contr�rio ele n�o encontrar� o mesmo {@link JobMonitor} e consequentemente n�o encontrar� a tarefa.
  // *
  // * @param jobUUID Identificador �nico do Job.
  // * @throws RFWException
  // */
  // public JobChecker(String jobUUID) throws RFWException {
  // this(jobUUID, null);
  // }

  /**
   * Cria uma nova inst�ncia de JobChecker para monitorar um Job.
   *
   * @param jobUUID Identificador �nico do Job.
   * @param supplier Fornecedor de uma "ponte" at� o JobMonitor, quando JobMonitor for criado em uma outra JVM. Como uma m�quina remota ou uma ClassLoader diferenciado. Para acessar o JobStatus dentro da mesma VM passe null.
   * @throws RFWException
   */
  public JobChecker(String jobUUID, JobStatusSupplier supplier) throws RFWException {
    this.jobUUID = jobUUID;
    if (supplier == null) {
      supplier = new JobStatusSupplierDefault();
    }
    this.jobSupplier = supplier;

    // Valida se a tarefa existe
    JobStatus status = supplier.getJobStatus(jobUUID);
    if (status == null) throw new RFWCriticalException("A tarefa '" + jobUUID + "' n�o pode ser encontrada no JobMonitor! Verifique a UUID e/ou a necessidade de implementar um JobStatusSupplier diferente.");
  }

  /**
   *
   *
   * @return
   * @throws RFWException
   */
  public JobStatus getJobStatus() throws RFWException {
    return this.jobSupplier.getJobStatus(this.jobUUID);
  }

  /**
   * Inicia o procedimento de verifica��o do Job.<br>
   * Este m�todo s� pode ser chamado uma �nica vez.<br>
   * Chamadas subsequ�ntes ser�o apenas ignoradas.
   *
   * @throws RFWException
   */
  public synchronized void start() throws RFWException {
    if (this.t == null) {
      t = new Thread(this);
      t.setName("### JobChecker: " + this.jobSupplier.getJobStatus(this.jobUUID).getJobTitle());
      t.setPriority(Thread.MIN_PRIORITY); // Este � s� um verificador, n�o a execu��o da tarefa, n�o precisa competir pelo processador.
      t.start();
    }
  }

  @Override
  public final void run() {
    try {
      long lastchange = -1;
      while (true) {
        JobStatus jobStatus = JobChecker.this.getJobStatus();
        if (jobStatus.getLastChange() > lastchange) {
          lastchange = jobStatus.getLastChange();

          // Verifica se ser� �ltima rodada antes do m�todo update para
          boolean lastCall = jobStatus.getStep() == JobStep.FINISHED || jobStatus.getStep() == JobStep.EXCEPTION;

          for (JobCheckerListener listener : listeners) {
            try {
              listener.updateStatus(jobStatus, lastCall);
            } catch (Throwable t) { // N�o permite que exce��es do usu�rio interrompam o JobChecker
              RFWLogger.logException(t);
            }
          }

          if (lastCall) break;
        }

        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
        }
      }
    } catch (Throwable e) {
      RFWLogger.logException(e);
    }
  }

  /**
   * Adiciona um listener para receber os eventos de altera��o da tarefa.
   *
   * @param listener
   */
  public void addListener(JobCheckerListener listener) {
    this.listeners.add(listener);
  }

  /**
   * # refer�ncia para o {@link JobStatusSupplier} do {@link JobStatus}.
   *
   * @return the refer�ncia para o {@link JobStatusSupplier} do {@link JobStatus}
   */
  public JobStatusSupplier getJobSupplier() {
    return jobSupplier;
  }
}
