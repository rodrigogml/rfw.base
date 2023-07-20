package br.eng.rodrigogml.rfw.base.jobmonitor;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus.JobStep;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;

/**
 * Description: Classe responsável por inciar e gerenciar a tarefa. Esta classe deve ser extendida para iniciar a execução da tarefa em paralelo (outra Thread).<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public abstract class Job extends Thread {

  /**
   * Recupera o Bean com as informações de jobStatus da tarefa, quando a tarefa suportar novas informações e mensagens podem ser retornadas (definidas) no bean durante a execução do método para que cheguem até o usuário, como textos, percentual de progresso, etc.
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
   * Método chamado quando começar a Thread.
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
   * Método chamado quando a Thread do Job é iniciada.
   *
   * @param job Instância do {@link Job}.
   * @param jStatus Bean para atualização do jobStatus de execução pela tarefa.
   *
   * @return Deve retornar o retorno da própria tarefa, para que o retorno fique salvo dentro do Job. Ou nulo caso o método não retorne nada.
   * @throws Throwable Qualquer exceção que ocorra dentro do método será capturada e salva dentro do Job para que possa ser recuperada posteriormente.
   */
  public abstract Object runJob(Job job, JobStatus jStatus) throws Throwable;

  /**
   * Recupera o recupera o Bean com as informações de jobStatus da tarefa, quando a tarefa suportar novas informações e mensagens podem ser retornadas (definidas) no bean durante a execução do método para que cheguem até o usuário, como textos, percentual de progresso, etc.
   *
   * @return the recupera o Bean com as informações de jobStatus da tarefa, quando a tarefa suportar novas informações e mensagens podem ser retornadas (definidas) no bean durante a execução do método para que cheguem até o usuário, como textos, percentual de progresso, etc
   */
  public JobStatus getJobStatus() {
    return jobStatus;
  }

}
