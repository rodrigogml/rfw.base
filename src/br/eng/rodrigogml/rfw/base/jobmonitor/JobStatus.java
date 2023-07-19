package br.eng.rodrigogml.rfw.base.jobmonitor;

import java.io.Serializable;
import java.util.HashMap;

import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Bean utilizado para recuperar o estatus de algum Job.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (18 de mar de 2020)
 */
public class JobStatus implements Serializable {

  private static final long serialVersionUID = -2878331974459255672L;

  public static enum JobStep {
    /**
     * Indica que o JOB ainda não começou. Passo inicial do trabalho.
     */
    IDLE,
    /**
     * Indica que a Thread já foi iniciada e que o método foi chamado e esta em execução.
     */
    RUNNING,
    /**
     * Indica que a Thread já foi finalizada e que o método encerrou de forma normal (sem exception. Neste passo, caso o método retorne algum valor ele já está disponível no {@link JobStatus#getJobReturn()}.
     */
    FINISHED,
    /**
     * Indica que a Thread finalizou com Exception. A exception da finalização estará disponível em {@link JobStatus#getException()}.
     */
    EXCEPTION
  }

  /**
   * Atributo que guarda quando a última alteração foi feita no JobStatus.<br>
   * Este atributo é atualizado em todo método "set()" com o valor de {@link System#currentTimeMillis()}.<br>
   * Depois que a tarefa é terminada, o lastChange é atualizado pela última vez ao definir o {@link JobStep#FINISHED}. Em outras palavras, depois que o status for finished, aqui ficará salvo o horário de termino de execução da tarefa.
   */
  private long lastChange = 0;

  /**
   * Salva o valor de {@link System#currentTimeMillis()} quando a tarefa foi iniciada, permitindo assim calcular a quanto tempo a tarefa já está rodando.
   */
  private long startTime = 0;

  /**
   * Passo em que o Job está no momento.<br>
   * Este atributo é atualizado pelo {@link Job}. Não deve ser atualizado pela tarefa
   */
  private JobStep step = JobStep.IDLE;

  /**
   * Este parâmetro indica se a tarefa informará um percentual de progresso (False), ou se é uma tarefa sem progresso determinado (True).<Br>
   * Por padrão não se espera que as tarefas informem seu progresso. Caso informem, a informação deve ser passada através do {@link #progress}.<br>
   * No que esse atributo refere-se apenas à informação de {@link #progress}. Mesmo que esteja definido como FALSE, {@link #progressMessage} pode ser informado independentemente.
   */
  private Boolean indeterminate = Boolean.TRUE;

  /**
   * Informação de progresso da tarefa, sendo 0.0 = 0% e 1.0 = 100%.<br>
   * O desenvolvedor pode definir este atributo diretamente, caso prefira realizar o cálculo do progresso dentro do próprio código, ou utilizar o conjunto de variáveis {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal}.
   */
  private double progress = 0;

  /**
   * Define a quantidade de passos a serem processados. No mínimo deve ter o valor 1.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina.
   *
   * <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   */
  private int stepsTotal = 1;

  /**
   * Passo atual sendo executado. Deve ser maior que zero, mas nunca maior que {@link #stepsTotal}
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina.
   *
   * <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   */
  private int stepsCount = 1;

  /**
   * Define o total de tarefas que serão executadas neste passo.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina.
   *
   * <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   */
  private int tasksTotal = 0;

  /**
   * Define a quantidade de tarefas já realizados.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina.
   *
   * <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   */
  private int tasksCount = 0;

  /**
   * A tarefa pode informar mensagens de progresso mesmo que não informe um percentual. Essas mensagens se destinam à informar ao usuário o que a tarefa está fazendo.<br>
   * Essas mensagens podem ser alteradas mesmo que {@link #indeterminate} seja False.
   */
  private String progressMessage = null;

  /**
   * Caso seja passado para true, a tarefa (se suportar), pode abortar no meio do processamento laçando exceção de Validação com o código "RFW_ERR_000004".<br>
   * Note que para simplificar a tarefa de cancelamento, o JobStatue disponibiliza o método {@link #checkInterrupt()}.
   */
  private Boolean interruptResquested = Boolean.FALSE;

  /**
   * Objeto retornado pelo Job, se ouver retorno e o método tiver terminado sem exception.
   */
  private Object jobReturn = null;

  /**
   * Exception retornado pelo Job, caso o resultado do método tenha sido uma exception.
   */
  private Throwable exception = null;

  /**
   * Parâmetros especiais do Job.<br>
   * Dependendo da tarefa sendo realizada, o Job pode oferecer informações adicionais em relação à sua execução, cabendo a documentação do método para mais informações. Essa Hash permite que sejam alocados no JobStatus qualquer valor/objeto, devendo apenas obritóriamente sendo serializável para que possa atravessar as fachadas e outras estruturas de comunicação.
   */
  private final HashMap<String, Serializable> params = new HashMap<String, Serializable>();

  /**
   * Identificador único do Job
   */
  private final String jobUUID;

  /**
   * Título de identificação da Tarela. Utilizada para realizar Logs e identicar a Thread no Debug.
   */
  private final String jobTitle;

  /**
   * StringBuilder utilizado para registrar os eventos do relatório.<br>
   * Note que as entradas do relatório são livres para serem criadas pela tarefa em execução e serem interpretadas pelo cliente que chamou, assim o relatório pode serguir o formato especificado pela tarefa.<Br>
   * No entanto, para uma melhor "mutabilidade" de formatos, é recomendado utilizar o FWMarkdown
   */
  private StringBuilder report = null;

  /**
   * Cria um Novo JobStatus.
   *
   * @param uuid Identificador único do Job gerado pelo {@link JobMonitor} quando o {@link Job} é registrado.
   * @param jobTitle Título de identificação da Tarela. Utilizada para realizar Logs e identicar a Thread no Debug.
   */
  JobStatus(String uuid, String jobTitle) {
    this.jobUUID = uuid;
    this.jobTitle = jobTitle;
  }

  /**
   * # passo em que o Job está no momento.<br>
   * Este atributo é atualizado pelo {@link Job}. Não deve ser atualizado pela tarefa.
   *
   * @return the passo em que o Job está no momento
   */
  public JobStep getStep() {
    return step;
  }

  /**
   * # passo em que o Job está no momento.<br>
   * Este atributo é atualizado pelo {@link Job}. Não deve ser atualizado pela tarefa.
   *
   * @param step the new passo em que o Job está no momento
   */
  void setStep(JobStep step) {
    this.step = step;
    this.lastChange = System.currentTimeMillis();
    if (step == JobStep.RUNNING && this.startTime == 0) this.startTime = System.currentTimeMillis();
  }

  /**
   * # este parâmetro indica se a tarefa informará um percentual de progresso (False), ou se é uma tarefa sem progresso determinado (True).<Br>
   * Por padrão não se espera que as tarefas informem seu progresso. Caso informem, a informação deve ser passada através do {@link #progress}.<br>
   * No que esse atributo refere-se apenas à informação de {@link #progress}. Mesmo que esteja definido como FALSE, {@link #progressMessage} pode ser informado independentemente.
   *
   * @return the este parâmetro indica se a tarefa informará um percentual de progresso (False), ou se é uma tarefa sem progresso determinado (True)
   */
  public Boolean getIndeterminate() {
    return indeterminate;
  }

  /**
   * # informação de progresso da tarefa, sendo 0.0 = 0% e 1.0 = 100%.<br>
   * O desenvolvedor pode definir este atributo diretamente, caso prefira realizar o cálculo do progresso dentro do próprio código, ou utilizar o conjunto de variáveis {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal}.
   *
   * @return the informação de progresso da tarefa, sendo 0
   */
  public double getProgress() {
    return progress;
  }

  /**
   * # a tarefa pode informar mensagens de progresso mesmo que não informe um percentual. Essas mensagens se destinam à informar ao usuário o que a tarefa está fazendo.<br>
   * Essas mensagens podem ser alteradas mesmo que {@link #indeterminate} seja False.
   *
   * @return the a tarefa pode informar mensagens de progresso mesmo que não informe um percentual
   */
  public String getProgressMessage() {
    return progressMessage;
  }

  /**
   * # este parâmetro indica se a tarefa informará um percentual de progresso (False), ou se é uma tarefa sem progresso determinado (True).<Br>
   * Por padrão não se espera que as tarefas informem seu progresso. Caso informem, a informação deve ser passada através do {@link #progress}.<br>
   * No que esse atributo refere-se apenas à informação de {@link #progress}. Mesmo que esteja definido como FALSE, {@link #progressMessage} pode ser informado independentemente.
   *
   * @param indeterminate the new este parâmetro indica se a tarefa informará um percentual de progresso (False), ou se é uma tarefa sem progresso determinado (True)
   */
  public void setIndeterminate(Boolean indeterminate) {
    this.indeterminate = indeterminate;
    this.lastChange = System.currentTimeMillis();
  }

  /**
   * # informação de progresso da tarefa, sendo 0.0 = 0% e 1.0 = 100%.<br>
   * O desenvolvedor pode definir este atributo diretamente, caso prefira realizar o cálculo do progresso dentro do próprio código, ou utilizar o conjunto de variáveis {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal}.
   *
   * @param progress the new informação de progresso da tarefa, sendo 0
   */
  public void setProgress(double progress) {
    this.setIndeterminate(false);
    this.progress = progress;
    this.lastChange = System.currentTimeMillis();
  }

  /**
   * # a tarefa pode informar mensagens de progresso mesmo que não informe um percentual. Essas mensagens se destinam à informar ao usuário o que a tarefa está fazendo.<br>
   * Essas mensagens podem ser alteradas mesmo que {@link #indeterminate} seja False.
   *
   * @param progressMessage the new a tarefa pode informar mensagens de progresso mesmo que não informe um percentual
   */
  public void setProgressMessage(String progressMessage) {
    this.progressMessage = progressMessage;
    this.lastChange = System.currentTimeMillis();
  }

  /**
   * Este método lança a exceção padrão de "Cancelado pelo Usuário" caso o {@link #interruptResquested} tenha sido acionado.<br>
   * Desta forma, o Job sendo executado só precisa chamar este método dentro do seu loop ou em lugares em que seja conveniente verificar se a tarefa deva ser cancelada.
   *
   * @throws RFWException
   */
  public void checkInterrupt() throws RFWException {
    if (this.interruptResquested) throw new RFWValidationException("RFW_ERR_000004");
  }

  /**
   * # caso seja passado para true, a tarefa (se suportar), pode abortar no meio do processamento laçando exceção de Validação com o código "RFW_ERR_000004".<br>
   * Note que para simplificar a tarefa de cancelamento, o JobStatue disponibiliza o método {@link #checkInterrupt()}.
   *
   * @return the caso seja passado para true, a tarefa (se suportar), pode abortar no meio do processamento laçando exceção de Validação com o código "RFW_ERR_000004"
   */
  public Boolean getInterruptResquested() {
    return interruptResquested;
  }

  /**
   * Solicita que a tarefa seja interrompida (se suportar). Aborta no meio do processamento laçando exceção de Validação com o código "RFW_ERR_000004".
   */
  protected void interrupt() {
    this.interruptResquested = Boolean.TRUE;
  }

  /**
   * Solicita que a tarefa seja interrompida (se suportar). Mas passa uma exception personalizada para realizar o canelamento do trabalho.
   */
  protected void interrupt(RFWException ex) {
    this.interruptResquested = Boolean.TRUE;
    this.lastChange = System.currentTimeMillis();
    setException(ex);
  }

  /**
   * # identificador único do Job.
   *
   * @return the identificador único do Job
   */
  public String getJobUUID() {
    return jobUUID;
  }

  /**
   * # atributo que guarda quando a última alteração foi feita no JobStatus.<br>
   * Este atributo é atualizado em todo método "set()" com o valor de {@link System#currentTimeMillis()}.<br>
   * Depois que a tarefa é terminada, o lastChange é atualizado pela última vez ao definir o {@link JobStep#FINISHED}. Em outras palavras, depois que o status for finished, aqui ficará salvo o horário de termino de execução da tarefa.
   *
   * @return the atributo que guarda quando a última alteração foi feita no JobStatus
   */
  public long getLastChange() {
    return lastChange;
  }

  /**
   * # salva o valor de {@link System#currentTimeMillis()} quando a tarefa foi iniciada, permitindo assim calcular a quanto tempo a tarefa já está rodando.
   *
   * @return the salva o valor de {@link System#currentTimeMillis()} quando a tarefa foi iniciada, permitindo assim calcular a quanto tempo a tarefa já está rodando
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Parâmetros especiais do Job.<br>
   * Dependendo da tarefa sendo realizada, o Job pode oferecer informações adicionais em relação à sua execução, cabendo a documentação do método para mais informações. Essa Hash permite que sejam alocados no JobStatus qualquer valor/objeto, devendo apenas obritóriamente sendo serializável para que possa atravessar as fachadas e outras estruturas de comunicação.
   */
  public void setParam(String param, Serializable value) {
    this.params.put(param, value);
    this.lastChange = System.currentTimeMillis();
  }

  /**
   * Parâmetros especiais do Job.<br>
   * Dependendo da tarefa sendo realizada, o Job pode oferecer informações adicionais em relação à sua execução, cabendo a documentação do método para mais informações. Essa Hash permite que sejam alocados no JobStatus qualquer valor/objeto, devendo apenas obritóriamente sendo serializável para que possa atravessar as fachadas e outras estruturas de comunicação.
   */
  public Object getParam(String param) {
    return this.params.get(param);
  }

  /**
   * Parâmetros especiais do Job.<br>
   * Dependendo da tarefa sendo realizada, o Job pode oferecer informações adicionais em relação à sua execução, cabendo a documentação do método para mais informações. Essa Hash permite que sejam alocados no JobStatus qualquer valor/objeto, devendo apenas obritóriamente sendo serializável para que possa atravessar as fachadas e outras estruturas de comunicação.
   */
  public void removeParam(String param) {
    this.params.remove(param);
    this.lastChange = System.currentTimeMillis();
  }

  /**
   * # objeto retornado pelo Job, se ouver retorno e o método tiver terminado sem exception.
   *
   * @return the objeto retornado pelo Job, se ouver retorno e o método tiver terminado sem exception
   */
  public Object getJobReturn() {
    return jobReturn;
  }

  /**
   * # exception retornado pelo Job, caso o resultado do método tenha sido uma exception.
   *
   * @return the exception retornado pelo Job, caso o resultado do método tenha sido uma exception
   */
  public Throwable getException() {
    return exception;
  }

  /**
   * # objeto retornado pelo Job, se ouver retorno e o método tiver terminado sem exception.
   *
   * @param jobReturn the new objeto retornado pelo Job, se ouver retorno e o método tiver terminado sem exception
   */
  void setJobReturn(Object jobReturn) {
    this.jobReturn = jobReturn;
  }

  /**
   * # exception retornado pelo Job, caso o resultado do método tenha sido uma exception.
   *
   * @param exception the new exception retornado pelo Job, caso o resultado do método tenha sido uma exception
   */
  void setException(Throwable exception) {
    // Só mantém a primeira exception recebida
    if (this.exception == null) this.exception = exception;
  }

  /**
   * # título de identificação da Tarela. Utilizada para realizar Logs e identicar a Thread no Debug.
   *
   * @return the título de identificação da Tarela
   */
  public String getJobTitle() {
    return jobTitle;
  }

  /**
   * Anexa um conteúdo do relatório da tarefa. (Log) <br>
   * <br>
   * StringBuilder utilizado para registrar os eventos do relatório. A utilização de um relatório pode ser a melhor maneira de exibir o resultado da operação para o usuário.<br>
   * Note que as entradas do relatório são livres para serem criadas pela tarefa em execução e serem interpretadas pelo cliente que chamou, assim o relatório pode serguir o formato especificado pela tarefa.<Br>
   * No entanto, para uma melhor "mutabilidade" de formatos, é recomendado utilizar o FWMarkdown.
   *
   * @param content Conteúdo a ser anexado do relatório.
   * @return O próprio JobStatus para conveniência de podermos chamar o .append() em sequência.
   */
  public JobStatus append(String content) {
    if (this.report == null) this.report = new StringBuilder();
    this.report.append(content);
    return this;
  }

  /**
   * Anexa um conteúdo do relatório da tarefa. (Log) <br>
   * Este método faz o append de "\r\n" no final do conteúdo passado para indicar uma quebra de linha no formato texto (também utilizado no FWMarkdown.<Br>
   * <br>
   * StringBuilder utilizado para registrar os eventos do relatório. A utilização de um relatório pode ser a melhor maneira de exibir o resultado da operação para o usuário.<br>
   * Note que as entradas do relatório são livres para serem criadas pela tarefa em execução e serem interpretadas pelo cliente que chamou, assim o relatório pode serguir o formato especificado pela tarefa.<Br>
   * No entanto, para uma melhor "mutabilidade" de formatos, é recomendado utilizar o FWMarkdown.
   *
   * @param content Conteúdo a ser anexado do relatório.
   * @return O próprio JobStatus para conveniência de podermos chamar o .append() em sequência.
   */
  public JobStatus appendLn(String content) {
    if (this.report == null) this.report = new StringBuilder();
    this.report.append(content).append("\r\n");
    return this;
  }

  /**
   * # stringBuilder utilizado para registrar os eventos do relatório.<br>
   * Note que as entradas do relatório são livres para serem criadas pela tarefa em execução e serem interpretadas pelo cliente que chamou, assim o relatório pode serguir o formato especificado pela tarefa.<Br>
   * No entanto, para uma melhor "mutabilidade" de formatos, é recomendado utilizar o FWMarkdown.
   *
   * @return the stringBuilder utilizado para registrar os eventos do relatório
   */
  public String getReport() {
    if (this.report == null) return null;
    return this.report.toString();
  }

  /**
   * Verifica se já tivemos alguma entrada de append no relatório ou não. Valida com menos custo a existência do relatório do que validar se .getReport() retorna nulo, pois não chama o .toString() do StringBuilder o tempo todo.
   *
   * @return indica se temos alguma entrada de relatório (true) ou não (false).
   */
  public boolean hasReport() {
    return this.report != null;
  }

  /**
   * Permite recuperar o tamanho do Report sem forçar a montagem do {@link StringBuilder}
   *
   * @return Retorna o tamanho do {@link #report}
   */
  /*
   * Permite recuperar o tamanho do Report sem forçar a montagem do {@link StringBuilder}
   *
   * @return Retorna o tamanho do {@link #report}
   */
  public int getReportSize() {
    if (this.report == null) return 0;
    return this.report.length();
  }

  /**
   * # define a quantidade de passos a serem processados. No mínimo deve ter o valor 1.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @return the define a quantidade de passos a serem processados
   */
  public int getStepsTotal() {
    return stepsTotal;
  }

  /**
   * # define a quantidade de passos a serem processados. No mínimo deve ter o valor 1.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @param stepsTotal the new define a quantidade de passos a serem processados
   */
  public void setStepsTotal(int stepsTotal) {
    this.stepsTotal = stepsTotal;
    calcProgress();
  }

  /**
   * # passo atual sendo executado. Deve ser maior que zero, mas nunca maior que {@link #stepsTotal}
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @return the passo atual sendo executado
   */
  public int getStepsCount() {
    return stepsCount;
  }

  /**
   * # passo atual sendo executado. Deve ser maior que zero, mas nunca maior que {@link #stepsTotal}
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @param stepsCount the new passo atual sendo executado
   */
  public void setStepsCount(int stepsCount) {
    this.stepsCount = stepsCount;
    calcProgress();
  }

  /**
   * # define o total de tarefas que serão executadas neste passo.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @return the define o total de tarefas que serão executadas neste passo
   */
  public int getTasksTotal() {
    return tasksTotal;
  }

  /**
   * # define o total de tarefas que serão executadas neste passo.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @param tasksTotal the new define o total de tarefas que serão executadas neste passo
   */
  public void setTasksTotal(int tasksTotal) {
    this.tasksTotal = tasksTotal;
    calcProgress();
  }

  /**
   * # define a tarefa atual e o total de tarefas em único passo. Mesmo que chamar os métodos {@link #setTasksCount(int)} e {@link #setTasksTotal(int)} em sequência, sem o overhead do cálculo do progresso.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @param tasksTotal the new define o total de tarefas que serão executadas neste passo
   */
  public void setTasks(int tasksCount, int tasksTotal) {
    this.tasksCount = tasksCount;
    this.tasksTotal = tasksTotal;
    calcProgress();
  }

  /**
   * # define a quantidade de tarefas já realizados.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @return the define a quantidade de tarefas já realizados
   */
  public int getTasksCount() {
    return tasksCount;
  }

  /**
   * # define a quantidade de tarefas já realizados.
   * <hr>
   * O {@link JobStatus} oferece uma maneira de evitar que o desenvolvedor tenha que calcular explicitamente no seu código o percentual de progresso da execução do código em Background. Para isso utilizamos o conjunto de variáveis: {@link #stepsTotal}, {@link #stepsCount}, {@link #tasksCount} e {@link #tasksTotal} da seguinte maneira:
   * <li><b>Tarefas simples, com um únito "passo"</b>
   * <ul>
   * Quando a tarefa tem um único passo, ou seja seu progresso é linear em um único fluxo de execução, como um único loop. Podemos utilizar apenas duas variáveis:
   * <li><b>{@link #tasksTotal}</b> - define o total de itens que vamos processar, em outras palavras, o número que representa o 100% de tarefa completada. Ex: tamanho do array de objetos para processar.
   * <li><b>{@link #tasksCount}</b> - define em que item estamos do total à processar. Ex: índice da interação do array sendo processado.
   * </ul>
   * <li><b>Tarefas complexas, com passos sequenciais ou múltiplos loops</b>
   * <ul>
   * Quando a tarefa tem diversos passos ou passos recursivos, por exemplo, vamos iterar as empresas de um sistema, e para cada empresa vamos analizar registros "X". Não temos de antemão o total de registros que serão analizados até o final. Assim, vamos separar cada parte do processamento em passos. A divisão exata fica a cargo do desenvolvedor. No nosso exemplo podemos definir a quantidade de
   * passos como sendo a quantidade de empresas que temos. E para cada passo (cada iteração dos registros X) reiniciamos a contagem de <b>tasks</b>.<Br>
   * Vamos imaginar o seguinte exemplo:<br>
   * Uma rotina vai iterar as empresas do sistema, e para cada empresa vai iterar os registros X para processamento, e em seguida os registros Y. Nesse caso podemos definir nosso total de passos como: <b>Total de Empresas do Sistema</b> x <b>Números de Tarefas para cada Empresa (2 no exemplo)</b>.<br>
   * Esses valores seriam definidos nas seguintes variáveis:
   * <li><b>{@link #stepsTotal}</b> Define a quantidade total de passos que o processamento terá. No nosso exemplo, se tivermos 5 empresas, seria 5 empresas x 2 rotinas/empresa = 10 passos.
   * <li><b>{@link #stepsCount}</b> Dedine o passo atual em que estamos. No nosso exemplo o passo é incrementado a cada rotina de cada empresa que é completado, de forma que chegue nos 10 quando a última rotina terminar. <br>
   * Dentro de cada rotina vamos reiniciando os valores de:
   * <li><b>{@link #tasksTotal}</b> - define o total de registros X ou de Y que temos para processar a cada início de rotina.
   * <li><b>{@link #tasksCount}</b> - incrementa a cada registro X ou Y que foi processado dentro da rotina. <Br>
   * <br>
   * Cada vez que uma dessas veriávies é alterada o {@link JobStatus} tentará recalcular o progresso geral e defini-lo em {@link #progress} atuomaticamente. Ou seja, quando o desenvolvedor utilizar o sistema de cálculo interno, ele não deve definir o {@link #progress} manualmente.<Br>
   * <br>
   * O Cálculo será feito de maneira que o valor total da barra seja primeiro dividido na quantidade de passos definidos. Por exemplo, se tivermos 10 passos, cada passo completo será responsável por preencher a 10% da barra. E o proporcional das "tasks" completadas incrementa a barra entre a posição de um step e outro.<br>
   * .
   *
   * @param tasksCount the new define a quantidade de tarefas já realizados
   */
  public void setTasksCount(int tasksCount) {
    this.tasksCount = tasksCount;
    calcProgress();
  }

  private void calcProgress() {
    try {
      double stepSize = 1 / stepsTotal; // Caso não tenhamos outros passos o valor é 1, e o cálculo do prograsso de tasks será distribuido em 100%.

      double stepProgress = stepSize * (tasksCount / tasksCount); // Progresso da tarefa escalonado para o tamanho de cada passo.
      double fullProgress = (stepSize * (stepsCount - 1)) + stepProgress;

      setProgress(fullProgress);
    } catch (Exception e) {
      // Em caso de erro só imprime para o desenvolvimento
      if (RFW.isDevelopmentEnvironment()) e.printStackTrace();
    }
  }

  /**
   * Incrementa a contagem de tarefas completas.<br>
   * Equivalente à {@link #setTasksCount(int)} passando o parâmetro ({@link #getTasksCount()} + 1).
   */
  public void incTasksCount() {
    this.tasksCount++;
    calcProgress();
  }

  /**
   * Incrementa a contagem de passos completos<br>
   * Equivalente à {@link #setStepsCount(int)} passando o parâmetro ({@link #getStepsCount()} + 1).<Br>
   * <Br>
   * <b>JÁ ZERA O ATRIBUTO {@link #tasksCount}.</b> Faz isso para que o valor do progresso não "pule" considerando o tasksCount do passo anterior.
   */
  public void incStepsCount() {
    this.stepsCount++;
    this.tasksCount = 0;
    calcProgress();
  }
}
