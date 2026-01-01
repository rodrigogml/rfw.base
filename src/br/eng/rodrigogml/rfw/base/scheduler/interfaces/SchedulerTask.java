package br.eng.rodrigogml.rfw.base.scheduler.interfaces;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Description: Interface que define os métodos fachada de uma tarefa para executar no Scheduler.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (19 de out de 2020)
 */
public interface SchedulerTask {

  public static enum RepeatFrequency {

    /**
     * Executa a tarefa de X em X tempo a partir da data de início. O tempo de repetição é configurado no attributo {@link SchedulerTask#getTimeToRepeat()}
     */
    TIMED,
    /**
     * Executa a tarefa Mensalmente. Isto é, a cada execução a data da próxima execução é calculada somando-se +1 (ou o valor definido em {@link SchedulerTask#getRecurrence()}) no mês.<br>
     * A frequência mensal permite que se crie a frequência anual também, bastando colocar a cada 12 meses a partir da data de início.
     */
    MONTHLY,
    /**
     * Executa a tarefa diariamente. Isto é, a cada execução a data da próxima execução é calculada somando-se +1 (ou o valor definido em {@link SchedulerTask#getRecurrence()}) no dia.<Br>
     * A frequência diária também permite criar a frequência semanal, bastando colocar a cada 7 dias a partir da data de início.
     */
    DAILY,
  }

  /**
   * Classe que implementa a interface {@link SchedulerRunnable} que deve ser chamada no momento de execução da tarefa.
   *
   * @return CanonicalName da casse que implementa a interface {@link SchedulerRunnable}
   */
  public String getTaskClass();

  /**
   * Identificador único da tarefa. Este ID deve ser único para a tarefa em todo o sistema. A tarefa é identificada pelo seu ID, logo, tarefas com o mesmo ID serão tratadas comos e fosem uma. Evitando que sejam executadas simultaneamente, sendo canceladas, etc.
   *
   * @return Identificador Long único. Quando salvas no banco de dados pode ser usado o ID do banco de dados. Quando não, pode-ser utilizar algum gerador único de Longs. Desde que se use em todo o sistema de forma a evitar duplicidades.
   */
  public Long getId();

  /**
   * Horário do próximo agendamento. Essa data não tem problema em ser uma data passada. No entanto para saber se a tarefa será executada ou não, mesmo com data atrasada serão avaliadas as demais definições da tarefa.
   *
   * @return Data/Hora do agendamento.
   */
  public LocalDateTime getScheduleTime();

  /**
   * Determina a frequência com que essa tarefa se repete, caso seja uma tarefa recursiva. Caso seja 'null' indica que a tarefa não se repete. Sobre as opções leia mais em {@link RepeatFrequency}.
   *
   * @return Frequência de repetição da tarefa.
   */
  public RepeatFrequency getRepeatFrequency();

  /**
   * Recupera o define o tempo máximo de atraso em milisegundos no qual a tarefa ainda pode ser executada.<br>
   * Defina NULL para impedir que seja executada em caso de atraso ou -1 para executar independente de quanto tempo já esteja atrasada.
   *
   * @return Define o tempo máximo de atraso em milisegundos no qual a tarefa ainda pode ser executada
   */
  public Long getLateExecution();

  /**
   * Recupera Data da última execução. Data em que a tarefa terminou de ser executada!.
   *
   * @return Data da última execução.
   */
  public LocalDateTime getLastExecution();

  /**
   * Para o {@link RepeatFrequency#TIMED}: Define em milisegundos o tempo de espera antes de reexecutar esta tarefa novamente.<br>
   *
   * @return Tempo de espera (em milisegundos) antes de reexecutar a tarefa.
   */
  public Long getTimeToRepeat();

  /**
   * Data para que a tarefa não seja mais executada. NULL para que a tarefa não tenha uma data fim.<br>
   * Note que se uma tarefa estiver atrasada, e sua configurações de execução atrasada permitirem, uma tarefa atrasada pode ser executada mesmo depois da data fim.<br>
   * Para garantir que uma tarefa atrasada não seja executada porteriormente à data fim, configure o tempo limite de execução atrasado coerentemente.
   *
   * @return Data para que a tarefa não seja mais executada
   */
  public LocalDateTime getStopDate();

  /**
   * Tempo da recorrência. Pode ter sentidos diferentes para cada frequência de repetição:<br>
   * Para {@link RepeatFrequency#MONTHLY}: de quantos em quantos meses o agendamento é executado. Se null o valor padrão considerado será 1. Se qualquer inteiro positivo, fará com que a repetição ocorra somente a cada X meses.<br>
   * Para {@link RepeatFrequency#DAILY}: de quantos em quantos dias o agendamento é executado. Se null o valor padrão considerado será 1. Se qualquer inteiro positivo, fará com que a repetição ocorra somente a cada X dias.<br>
   *
   * @return Tempo da recorrência
   */
  public Integer getRecurrence();

  /**
   * Para o {@link RepeatFrequency#MONTHLY} define se a repetição se dará pelo dia do mês (caso true ou null) ou pelo n° dia da semana (caso false), como por exemplo 2° quinta feira, etc.
   *
   * @return Para o {@link RepeatFrequency#MONTHLY} define se a repetição se dará pelo dia do mês (caso true ou null) ou pelo n° dia da semana (caso false), como por exemplo 2° quinta feira, etc
   */
  public Boolean getMonthlyRepeatByDayOfMonth();

  /**
   * Mapa com as propriedades da tarefa. Essas propriedades são passadas como argumento da tarefa {@link SchedulerRunnable}, e podem ser retornadas no fim da sua execução. O retorno da execução da tarefa será passado pelo método {@link #setProperties(Map)}.
   *
   * @return Propriedades da tarefa.
   */
  public Map<String, String> getProperties();

  /**
   * Utilizado para receber o mapa das propriedades recebido no fim da execução da tarefa. Veja mais detalhes em {@link #getProperties()}.<Br>
   * Note que este método só é chamado se a tarefa retornar um objeto. Caso seja nulo entendemos que não há nada para ser atualizado e este método não é chamado.<br>
   * Nota 2: Embora este método não seja chamado se retornar nulo, o Map passado pode acabar sendo alterado se estivermos sempre na mesma instância da JVM, uma vez que passamos apenas a referência da memória do objeto.
   *
   * @param properties Mapa de Propriedades
   */
  public void setProperties(Map<String, String> properties);

  /**
   * Chamado depois que a tarefa é executada para atualizar a data da última execução da tarefa. O valor passado aqui deve ser salvo e retornado no método {@link #getLastExecution()} para um funcionamento adequado do Scheduler.<br>
   * Para mais detalhes veja {@link #getLastExecution()}.
   *
   * @param lastExecution Horario da última execução
   */
  public void setLastExecution(LocalDateTime lastExecution);

  /**
   * Chamado depois que a tarefa é executada para atualiza a data do agendamento. Note que a data informada será passada, na verdade a data da última execução agendada. Esse valor é utilizado no cálculo para reagendamento da tarefa, o valor é atualizado para trazer o agendamento para uma data passada "mas mais próxima" da atual.<br>
   * O conteúdo passado aqui deve ser retornado no método {@link #getScheduleTime()} para um funcionamento adequado do Scheduler.<br>
   * Para mais detalhes veja {@link #getScheduleTime()}.
   *
   * @param scheduleTime Horário da próxima execução
   */
  public void setScheduleTime(LocalDateTime scheduleTime);
}
