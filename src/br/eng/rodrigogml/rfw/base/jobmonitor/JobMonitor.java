package br.eng.rodrigogml.rfw.base.jobmonitor;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;

/**
 * Description: JobMonitor é o serviço do Framework responsável por manter referência de Thread que estão rodando "trabalhos" pesados em paralelo.<br>
 * O JobMonitor pode ser ser utilizado sempre que alguma requisição do usuário leve muito tempo para ser realizada e além de não querermos que a interface fique parada, queremos atualizar o usuário sobre os passos da tarefa sendo realizado.<br>
 *
 * Esta classe é a responsável por manter o registro, identificar e remover da memória (depois de terminado) o resultado dos JOBs.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public class JobMonitor {

  /**
   * Hash com os registros dos Jobs do sistema.<Br>
   * A chave é o UUID gerado na criação do Job
   */
  private static HashMap<String, Job> hashJob = new HashMap<String, Job>();

  /**
   * Hash com os Timers de finalização da tarefa.<br>
   * A chave é o UUID gerado na criação do Job
   */
  private static HashMap<String, Timer> hashTimer = new HashMap<String, Timer>();

  /**
   * Construtor privado apra classe estática
   */
  private JobMonitor() {
  }

  /**
   * Registra o Job no JobMonitor para er acompanhado posteriormente
   *
   * @param job {@link Job} a ser registrado para posterior consulta.
   * @return Identificador único para referência futura da tarefa.
   */
  static String registerJob(final Job job) {
    String jobUUID = RUGenerators.generateUUID();
    hashJob.put(jobUUID, job);

    final String jobTitle = "##### JobMonitorTimer LeakCatcher: " + jobUUID;

    // Ao criar a tarefa já vamos ter um prazo máximo de vida para essa tarefa fica no Monitor. No momento damos 6horas (exagero?)
    Timer timer = new Timer(jobTitle, true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        switch (job.getJobStatus().getStep()) {
          case IDLE:
          case EXCEPTION:
          case FINISHED:
            if (cleanJob(jobUUID)) {
              // Esse código "jamais" deve acontecer.
              // Se acontecer ou é uma tarefa levando mais de 6 horas para ser terminada, ou, alguma tarefa foi alocada mas não iniciada, ou, a notificação de endOfJob falhou
              RFWLogger.logError("Job foi descartado pela Thread de Vazamento da JobMonitor! '" + jobTitle + "'.");
            }
            break;
          case RUNNING:
            RFWLogger.logImprovement("A tarefa está rodando a mais tempo que o limite de execução do JobMonitor! '" + jobTitle + "'.");
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
   * @return Objeto com o Status da Tarefa, ou NULL caso não encontre a tarefa
   */
  public static JobStatus getJobStatus(String jobUUID) {
    Job job = hashJob.get(jobUUID);
    if (job == null) return null;
    return job.getJobStatus();
  }

  /**
   * Este método deve ser chamado pelo Job quando for finalizado. Colocamos assim um timer para remover o Job da memória (caso ele não seja dado baixa pela manualmente).
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

    // Criamos o novo (se a tarefa ainda existir, pois o método cleanJob pode ter sido chamado a partir do JobChecker antes de iniciar o notify (por isso a sincronização dos métodos), dando um tempo para que o usuário possa recuperar o status da tarefa, depois limpamos o Job da memória.
    if (hashJob.containsKey(jobUUID)) {
      final Timer timer = new Timer("##### JobMonitorTimer EndOfJob: " + jobUUID, true);
      hashTimer.put(jobUUID, timer); // Colocamos o novo para que seja possível cancela-lo manualmente caso o usuário chame o método cleanJob()
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          cleanJob(jobUUID);
        }
      }, 600000); // 10min

      try {
        // BUG: Aguardamos a Thread do Timer subir, assim garantimos que se em seguida ela chamar o .cancel() no timer, o Timer de fato será cancelado. Se a thread não tiver subido ainda, o .cancel não faz nada.
        // Ao invés do sleep poderiamos ter algum método que identificasse que a Thread já subiu.
        // A chamada do método cleanJob vai aguardar pq estamos sincronizados. O problema é que a sincronização vai travar todos os usuários
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Remove o Job da memória (do JobMonitor). Força a interrupção da tarefa (caso não tenha terminado).<br>
   * Este método tem o intuíto de livrar os Jobs da memória para livrar recurso.
   *
   * @param jobUUID Identificador único da tarefa.
   * @return true caso o job tenha sido encontrado no JobMonitor, false caso contrário.
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
   * Solicita que a tarefa seja interrompida (se suportar). Aborta no meio do processamento laçando exceção de Validação com o código "RFW_ERR_000004".
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
