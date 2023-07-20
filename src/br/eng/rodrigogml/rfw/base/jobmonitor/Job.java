package br.eng.rodrigogml.rfw.base.jobmonitor;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus.JobStep;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;

/**
 * Description: Classe respons�vel por inciar e gerenciar a tarefa. Esta classe deve ser extendida para iniciar a execu��o da tarefa em paralelo (outra Thread).<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public abstract class Job extends Thread {

  /**
   * Recupera o Bean com as informa��es de jobStatus da tarefa, quando a tarefa suportar novas informa��es e mensagens podem ser retornadas (definidas) no bean durante a execu��o do m�todo para que cheguem at� o usu�rio, como textos, percentual de progresso, etc.
   */
  private final JobStatus jobStatus;

  /**
   * Inicia um novo Job
   */
  public Job(String jobTitle) {
    super("##### Job: " + PreProcess.coalesce(jobTitle, "Job Without Title :/"));
    this.setDaemon(false);
    this.jobStatus = new JobStatus(JobMonitor.registerJob(this), PreProcess.coalesce(jobTitle, "Job Without Title :/"));
  }

  /**
   * M�todo chamado quando come�ar a Thread.
   */
  @Override
  public void run() {
    try {
      this.jobStatus.setStep(JobStep.RUNNING);
      this.jobStatus.setJobReturn(runJob(this, this.jobStatus));
      this.jobStatus.setStep(JobStep.FINISHED);
    } catch (Throwable e) {
      RFWLogger.logException(e);
      this.jobStatus.setException(e);
      this.jobStatus.setStep(JobStep.EXCEPTION);
    }
    JobMonitor.notifyEndOfJob(this.jobStatus.getJobUUID());
  }

  /**
   * M�todo chamado quando a Thread do Job � iniciada.
   *
   * @param job Inst�ncia do {@link Job}.
   * @param jStatus Bean para atualiza��o do jobStatus de execu��o pela tarefa.
   *
   * @return Deve retornar o retorno da pr�pria tarefa, para que o retorno fique salvo dentro do Job. Ou nulo caso o m�todo n�o retorne nada.
   * @throws Throwable Qualquer exce��o que ocorra dentro do m�todo ser� capturada e salva dentro do Job para que possa ser recuperada posteriormente.
   */
  public abstract Object runJob(Job job, JobStatus jStatus) throws Throwable;

  /**
   * Recupera o recupera o Bean com as informa��es de jobStatus da tarefa, quando a tarefa suportar novas informa��es e mensagens podem ser retornadas (definidas) no bean durante a execu��o do m�todo para que cheguem at� o usu�rio, como textos, percentual de progresso, etc.
   *
   * @return the recupera o Bean com as informa��es de jobStatus da tarefa, quando a tarefa suportar novas informa��es e mensagens podem ser retornadas (definidas) no bean durante a execu��o do m�todo para que cheguem at� o usu�rio, como textos, percentual de progresso, etc
   */
  public JobStatus getJobStatus() {
    return jobStatus;
  }

}
