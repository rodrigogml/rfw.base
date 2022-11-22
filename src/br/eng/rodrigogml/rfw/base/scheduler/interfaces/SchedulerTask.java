package br.eng.rodrigogml.rfw.base.scheduler.interfaces;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Description: Interface que define os m�todos fachada de uma tarefa para executar no Scheduler.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (19 de out de 2020)
 */
public interface SchedulerTask {

  public static enum RepeatFrequency {

    /**
     * Executa a tarefa de X em X tempo a partir da data de in�cio. O tempo de repeti��o � configurado no attributo {@link SchedulerTask#getTimeToRepeat()}
     */
    TIMED,
    /**
     * Executa a tarefa Mensalmente. Isto �, a cada execu��o a data da pr�xima execu��o � calculada somando-se +1 (ou o valor definido em {@link SchedulerTask#getRecurrence()}) no m�s.<br>
     * A frequ�ncia mensal permite que se crie a frequ�ncia anual tamb�m, bastando colocar a cada 12 meses a partir da data de in�cio.
     */
    MONTHLY,
    /**
     * Executa a tarefa diariamente. Isto �, a cada execu��o a data da pr�xima execu��o � calculada somando-se +1 (ou o valor definido em {@link SchedulerTask#getRecurrence()}) no dia.<Br>
     * A frequ�ncia di�ria tamb�m permite criar a frequ�ncia semanal, bastando colocar a cada 7 dias a partir da data de in�cio.
     */
    DAILY,
  }

  /**
   * Classe que implementa a interface {@link SchedulerRunnable} que deve ser chamada no momento de execu��o da tarefa.
   *
   * @return CanonicalName da casse que implementa a interface {@link SchedulerRunnable}
   */
  public String getTaskClass();

  /**
   * Identificador �nico da tarefa. Este ID deve ser �nico para a tarefa em todo o sistema. A tarefa � identificada pelo seu ID, logo, tarefas com o mesmo ID ser�o tratadas comos e fosem uma. Evitando que sejam executadas simultaneamente, sendo canceladas, etc.
   *
   * @return Identificador Long �nico. Quando salvas no banco de dados pode ser usado o ID do banco de dados. Quando n�o, pode-ser utilizar algum gerador �nico de Longs. Desde que se use em todo o sistema de forma a evitar duplicidades.
   */
  public Long getId();

  /**
   * Hor�rio do pr�ximo agendamento. Essa data n�o tem problema em ser uma data passada. No entanto para saber se a tarefa ser� executada ou n�o, mesmo com data atrasada ser�o avaliadas as demais defini��es da tarefa.
   *
   * @return Data/Hora do agendamento.
   */
  public LocalDateTime getScheduleTime();

  /**
   * Determina a frequ�ncia com que essa tarefa se repete, caso seja uma tarefa recursiva. Caso seja 'null' indica que a tarefa n�o se repete. Sobre as op��es leia mais em {@link RepeatFrequency}.
   *
   * @return Frequ�ncia de repeti��o da tarefa.
   */
  public RepeatFrequency getRepeatFrequency();

  /**
   * Recupera o define o tempo m�ximo de atraso em milisegundos no qual a tarefa ainda pode ser executada.<br>
   * Defina NULL para impedir que seja executada em caso de atraso ou -1 para executar independente de quanto tempo j� esteja atrasada.
   *
   * @return Define o tempo m�ximo de atraso em milisegundos no qual a tarefa ainda pode ser executada
   */
  public Long getLateExecution();

  /**
   * Recupera Data da �ltima execu��o. Data em que a tarefa terminou de ser executada!.
   *
   * @return Data da �ltima execu��o.
   */
  public LocalDateTime getLastExecution();

  /**
   * Para o {@link RepeatFrequency#TIMED}: Define em milisegundos o tempo de espera antes de reexecutar esta tarefa novamente.<br>
   *
   * @return Tempo de espera (em milisegundos) antes de reexecutar a tarefa.
   */
  public Long getTimeToRepeat();

  /**
   * Data para que a tarefa n�o seja mais executada. NULL para que a tarefa n�o tenha uma data fim.<br>
   * Note que se uma tarefa estiver atrasada, e sua configura��es de execu��o atrasada permitirem, uma tarefa atrasada pode ser executada mesmo depois da data fim.<br>
   * Para garantir que uma tarefa atrasada n�o seja executada porteriormente � data fim, configure o tempo limite de execu��o atrasado coerentemente.
   *
   * @return Data para que a tarefa n�o seja mais executada
   */
  public LocalDateTime getStopDate();

  /**
   * Tempo da recorr�ncia. Pode ter sentidos diferentes para cada frequ�ncia de repeti��o:<br>
   * Para {@link RepeatFrequency#MONTHLY}: de quantos em quantos meses o agendamento � executado. Se null o valor padr�o considerado ser� 1. Se qualquer inteiro positivo, far� com que a repeti��o ocorra somente a cada X meses.<br>
   * Para {@link RepeatFrequency#DAILY}: de quantos em quantos dias o agendamento � executado. Se null o valor padr�o considerado ser� 1. Se qualquer inteiro positivo, far� com que a repeti��o ocorra somente a cada X dias.<br>
   *
   * @return Tempo da recorr�ncia
   */
  public Integer getRecurrence();

  /**
   * Para o {@link RepeatFrequency#MONTHLY} define se a repeti��o se dar� pelo dia do m�s (caso true ou null) ou pelo n� dia da semana (caso false), como por exemplo 2� quinta feira, etc.
   *
   * @return Para o {@link RepeatFrequency#MONTHLY} define se a repeti��o se dar� pelo dia do m�s (caso true ou null) ou pelo n� dia da semana (caso false), como por exemplo 2� quinta feira, etc
   */
  public Boolean getMonthlyRepeatByDayOfMonth();

  /**
   * Mapa com as propriedades da tarefa. Essas propriedades s�o passadas como argumento da tarefa {@link SchedulerRunnable}, e podem ser retornadas no fim da sua execu��o. O retorno da execu��o da tarefa ser� passado pelo m�todo {@link #setProperties(Map)}.
   *
   * @return Propriedades da tarefa.
   */
  public Map<String, String> getProperties();

  /**
   * Utilizado para receber o mapa das propriedades recebido no fim da execu��o da tarefa. Veja mais detalhes em {@link #getProperties()}.<Br>
   * Note que este m�todo s� � chamado se a tarefa retornar um objeto. Caso seja nulo entendemos que n�o h� nada para ser atualizado e este m�todo n�o � chamado.<br>
   * Nota 2: Embora este m�todo n�o seja chamado se retornar nulo, o Map passado pode acabar sendo alterado se estivermos sempre na mesma inst�ncia da JVM, uma vez que passamos apenas a refer�ncia da mem�ria do objeto.
   *
   * @param properties Mapa de Propriedades
   */
  public void setProperties(Map<String, String> properties);

  /**
   * Chamado depois que a tarefa � executada para atualizar a data da �ltima execu��o da tarefa. O valor passado aqui deve ser salvo e retornado no m�todo {@link #getLastExecution()} para um funcionamento adequado do Scheduler.<br>
   * Para mais detalhes veja {@link #getLastExecution()}.
   *
   * @param lastExecution Horario da �ltima execu��o
   */
  public void setLastExecution(LocalDateTime lastExecution);

  /**
   * Chamado depois que a tarefa � executada para atualiza a data do agendamento. Note que a data informada ser� passada, na verdade a data da �ltima execu��o agendada. Esse valor � utilizado no c�lculo para reagendamento da tarefa, o valor � atualizado para trazer o agendamento para uma data passada "mas mais pr�xima" da atual.<br>
   * O conte�do passado aqui deve ser retornado no m�todo {@link #getScheduleTime()} para um funcionamento adequado do Scheduler.<br>
   * Para mais detalhes veja {@link #getScheduleTime()}.
   *
   * @param scheduleTime Hor�rio da pr�xima execu��o
   */
  public void setScheduleTime(LocalDateTime scheduleTime);
}
