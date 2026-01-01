package br.eng.rodrigogml.rfw.base.jobmonitor;

import java.util.LinkedList;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus.JobStep;
import br.eng.rodrigogml.rfw.base.jobmonitor.interfaces.JobCheckerListener;
import br.eng.rodrigogml.rfw.base.jobmonitor.interfaces.JobStatusSupplier;
import br.eng.rodrigogml.rfw.base.jobmonitor.interfaces.JobStatusSupplierDefault;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;

/**
 * Description: JobChecker é uma classe utilitária para forçar a verificação temporária de um Job em background.<br>
 * O JobChecker, depois de chamado o método {@link #start()}, inicia um Thread que verifica o status do Job de tempos em tempos e dispara o método para atualização.<br>
 * A Thread para automaticamente quando o {@link JobStep#FINISHED}.
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public class JobChecker implements Runnable {

  /**
   * Identificador único do Job a ser verificado.
   */
  private final String jobUUID;

  /**
   * Thread de verificação do Job
   */
  private Thread t;

  /**
   * Referência para o {@link JobStatusSupplier} do {@link JobStatus}
   */
  private final JobStatusSupplier jobSupplier;

  /**
   * Listeners que receberão as notificações da tarefa.
   */
  private final LinkedList<JobCheckerListener> listeners = new LinkedList<JobCheckerListener>();

  // Deixei o construtor sem fornecer o JobStatusSupplier comentado de propósito, para forçar o dev a lembrar em que classloader ele está e a necessidade de passar o JobStatusSupplier ou null para o padrão.
  // /**
  // * Cria uma nova instância de JobChecker para monitorar um Job.<br>
  // * <b>Atenção:</b> Este construtor só deve ser utilizado quando o JobChecker está sendo instânciado dentro da mesma JVM que a tarefa foi criada. Caso contrário ele não encontrará o mesmo {@link JobMonitor} e consequentemente não encontrará a tarefa.
  // *
  // * @param jobUUID Identificador único do Job.
  // * @throws RFWException
  // */
  // public JobChecker(String jobUUID) throws RFWException {
  // this(jobUUID, null);
  // }

  /**
   * Cria uma nova instância de JobChecker para monitorar um Job.
   *
   * @param jobUUID Identificador único do Job.
   * @param supplier Fornecedor de uma "ponte" até o JobMonitor, quando JobMonitor for criado em uma outra JVM. Como uma máquina remota ou uma ClassLoader diferenciado. Para acessar o JobStatus dentro da mesma VM passe null.
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
    if (status == null) throw new RFWCriticalException("A tarefa '" + jobUUID + "' não pode ser encontrada no JobMonitor! Verifique a UUID e/ou a necessidade de implementar um JobStatusSupplier diferente.");
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
   * Inicia o procedimento de verificação do Job.<br>
   * Este método só pode ser chamado uma única vez.<br>
   * Chamadas subsequêntes serão apenas ignoradas.
   *
   * @throws RFWException
   */
  public synchronized void start() throws RFWException {
    if (this.t == null) {
      t = new Thread(this);
      t.setName("### JobChecker: " + this.jobSupplier.getJobStatus(this.jobUUID).getJobTitle());
      t.setPriority(Thread.MIN_PRIORITY); // Este é só um verificador, não a execução da tarefa, não precisa competir pelo processador.
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

          // Verifica se será última rodada antes do método update para
          boolean lastCall = jobStatus.getStep() == JobStep.FINISHED || jobStatus.getStep() == JobStep.EXCEPTION;

          for (JobCheckerListener listener : listeners) {
            try {
              listener.updateStatus(jobStatus, lastCall);
            } catch (Throwable t) { // Não permite que exceções do usuário interrompam o JobChecker
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
   * Adiciona um listener para receber os eventos de alteração da tarefa.
   *
   * @param listener
   */
  public void addListener(JobCheckerListener listener) {
    this.listeners.add(listener);
  }

  /**
   * # referência para o {@link JobStatusSupplier} do {@link JobStatus}.
   *
   * @return the referência para o {@link JobStatusSupplier} do {@link JobStatus}
   */
  public JobStatusSupplier getJobSupplier() {
    return jobSupplier;
  }
}
