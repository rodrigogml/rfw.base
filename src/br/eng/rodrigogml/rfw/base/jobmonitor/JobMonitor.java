package br.eng.rodrigogml.rfw.base.jobmonitor;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;

/**
 * Description: JobMonitor � o servi�o do Framework respons�vel por manter refer�ncia de Thread que est�o rodando "trabalhos" pesados em paralelo.<br>
 * O JobMonitor pode ser ser utilizado sempre que alguma requisi��o do usu�rio leve muito tempo para ser realizada e al�m de n�o querermos que a interface fique parada, queremos atualizar o usu�rio sobre os passos da tarefa sendo realizado.<br>
 *
 * Esta classe � a respons�vel por manter o registro, identificar e remover da mem�ria (depois de terminado) o resultado dos JOBs.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public class JobMonitor {

  /**
   * Hash com os registros dos Jobs do sistema.<Br>
   * A chave � o UUID gerado na cria��o do Job
   */
  private static HashMap<String, Job> hashJob = new HashMap<String, Job>();

  /**
   * Hash com os Timers de finaliza��o da tarefa.<br>
   * A chave � o UUID gerado na cria��o do Job
   */
  private static HashMap<String, Timer> hashTimer = new HashMap<String, Timer>();

  /**
   * Construtor privado apra classe est�tica
   */
  private JobMonitor() {
  }

  /**
   * Registra o Job no JobMonitor para er acompanhado posteriormente
   *
   * @param job {@link Job} a ser registrado para posterior consulta.
   * @return Identificador �nico para refer�ncia futura da tarefa.
   */
  static String registerJob(final Job job) {
    String jobUUID = RUGenerators.generateUUID();
    hashJob.put(jobUUID, job);

    final String jobTitle = "##### JobMonitorTimer LeakCatcher: " + jobUUID;

    // Ao criar a tarefa j� vamos ter um prazo m�ximo de vida para essa tarefa fica no Monitor. No momento damos 6horas (exagero?)
    Timer timer = new Timer(jobTitle, true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        switch (job.getJobStatus().getStep()) {
          case IDLE:
          case EXCEPTION:
          case FINISHED:
            if (cleanJob(jobUUID)) {
              // Esse c�digo "jamais" deve acontecer.
              // Se acontecer ou � uma tarefa levando mais de 6 horas para ser terminada, ou, alguma tarefa foi alocada mas n�o iniciada, ou, a notifica��o de endOfJob falhou
              RFWLogger.logError("Job foi descartado pela Thread de Vazamento da JobMonitor! '" + jobTitle + "'.");
            }
            break;
          case RUNNING:
            RFWLogger.logImprovement("A tarefa est� rodando a mais tempo que o limite de execu��o do JobMonitor! '" + jobTitle + "'.");
            break;
        }
      }
    }, 3600000); // 1hora
    // Salvamos o timer na hash para que possa ser encontrado depois
    hashTimer.put(jobUUID, timer);

    return jobUUID;
  }

  /**
   * Obtem o objeto de status de uma determinada tarefa.
   *
   * @param jobUUID Identificador da tarefa.
   * @return Objeto com o Status da Tarefa, ou NULL caso n�o encontre a tarefa
   */
  public static JobStatus getJobStatus(String jobUUID) {
    Job job = hashJob.get(jobUUID);
    if (job == null) return null;
    return job.getJobStatus();
  }

  /**
   * Este m�todo deve ser chamado pelo Job quando for finalizado. Colocamos assim um timer para remover o Job da mem�ria (caso ele n�o seja dado baixa pela manualmente).
   *
   * @param jobUUID
   */
  static synchronized void notifyEndOfJob(String jobUUID) {
    // Buscamos o timer de vazamento e cancelamos ele
    Timer leakTimer = hashTimer.get(jobUUID);
    if (leakTimer != null) {
      leakTimer.cancel();
      hashTimer.remove(jobUUID);
    }

    // Criamos o novo (se a tarefa ainda existir, pois o m�todo cleanJob pode ter sido chamado a partir do JobChecker antes de iniciar o notify (por isso a sincroniza��o dos m�todos), dando um tempo para que o usu�rio possa recuperar o status da tarefa, depois limpamos o Job da mem�ria.
    if (hashJob.containsKey(jobUUID)) {
      final Timer timer = new Timer("##### JobMonitorTimer EndOfJob: " + jobUUID, true);
      hashTimer.put(jobUUID, timer); // Colocamos o novo para que seja poss�vel cancela-lo manualmente caso o usu�rio chame o m�todo cleanJob()
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          cleanJob(jobUUID);
        }
      }, 600000); // 10min

      try {
        // BUG: Aguardamos a Thread do Timer subir, assim garantimos que se em seguida ela chamar o .cancel() no timer, o Timer de fato ser� cancelado. Se a thread n�o tiver subido ainda, o .cancel n�o faz nada.
        // Ao inv�s do sleep poderiamos ter algum m�todo que identificasse que a Thread j� subiu.
        // A chamada do m�todo cleanJob vai aguardar pq estamos sincronizados. O problema � que a sincroniza��o vai travar todos os usu�rios
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Remove o Job da mem�ria (do JobMonitor). For�a a interrup��o da tarefa (caso n�o tenha terminado).<br>
   * Este m�todo tem o intu�to de livrar os Jobs da mem�ria para livrar recurso.
   *
   * @param jobUUID Identificador �nico da tarefa.
   * @return true caso o job tenha sido encontrado no JobMonitor, false caso contr�rio.
   */
  public synchronized static boolean cleanJob(String jobUUID) {
    Job job = hashJob.remove(jobUUID);
    if (job != null) job.interrupt(); // Solicita o cancelamento do andamento do Job para o caso da tarefa ainda estar rodando.

    // Descartamos do JobMonitor
    Timer timer = hashTimer.get(jobUUID);
    if (timer != null) {
      timer.cancel();
    }
    hashTimer.remove(jobUUID);
    return job != null;
  }

  /**
   * Solicita que a tarefa seja interrompida (se suportar). Aborta no meio do processamento la�ando exce��o de Valida��o com o c�digo "RFW_ERR_000004".
   *
   * @param jobUUID Idenficiador do Job.
   */
  public static void interrupt(String jobUUID) {
    getJobStatus(jobUUID).interrupt();
  }

  /**
   * Solicita que a tarefa seja interrompida (se suportar). Mas passa uma exception personalizada para realizar o canelamento do trabalho.
   *
   * @param jobUUID Idenficiador do Job.
   * @param ex Exception com o motivo do cancelamento.
   */
  public static void interrupt(String jobUUID, RFWException ex) {
    getJobStatus(jobUUID).interrupt(ex);
  }

}
