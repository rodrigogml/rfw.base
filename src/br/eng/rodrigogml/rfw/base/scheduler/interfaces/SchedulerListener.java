package br.eng.rodrigogml.rfw.base.scheduler.interfaces;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;

/**
 * Description: Listener de eventos do Scheduler.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (19 de out de 2020)
 */
public interface SchedulerListener {

  /**
   * Chamado quando a tarefa encontra algum problema de execu��o.
   *
   * @param task Tarefa em execu��o que deu problema.
   * @param e Exception com a descri��o do Problema.
   */
  public void fail(SchedulerTask task, RFWException e);

  /**
   * Chamado quando a tarefa for executada com sucesso.<br>
   * Este m�todo � chamado no fim da execu��o, j� depois de terem sido chamados os m�todos de "set" para atualiza��o dos atributos da tarefa.
   *
   * @param task Tarefa que foi executada com sucesso.
   */
  public void success(SchedulerTask task);

}
