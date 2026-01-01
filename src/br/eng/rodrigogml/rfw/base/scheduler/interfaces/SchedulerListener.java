package br.eng.rodrigogml.rfw.base.scheduler.interfaces;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Listener de eventos do Scheduler.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (19 de out de 2020)
 */
public interface SchedulerListener {

  /**
   * Chamado quando a tarefa encontra algum problema de execução.
   *
   * @param task Tarefa em execução que deu problema.
   * @param e Exception com a descrição do Problema.
   */
  public void fail(SchedulerTask task, RFWException e);

  /**
   * Chamado quando a tarefa for executada com sucesso.<br>
   * Este método é chamado no fim da execução, já depois de terem sido chamados os métodos de "set" para atualização dos atributos da tarefa.
   *
   * @param task Tarefa que foi executada com sucesso.
   */
  public void success(SchedulerTask task);

}
