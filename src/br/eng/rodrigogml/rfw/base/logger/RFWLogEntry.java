package br.eng.rodrigogml.rfw.base.logger;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;

/**
 * Description: VO para registrar uma entrada de LOG.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 */
public class RFWLogEntry implements Serializable {

  private static final long serialVersionUID = 5142257805811050560L;

  /**
   * Prioridades/Severidade do evento.<br>
   */
  public static enum RFWLogSeverity {
    /**
     * Registra um objeto no LOG. O RFWLogger fará o máximo para imprimir as informações do objeto no log.
     */
    OBJECT,
    /**
     * Registra uma informação com a severidade DEBUG
     */
    DEBUG,
    /**
     * Registra uma informação com a severidade INFO
     */
    INFO,
    /**
     * Registra uma informação com a severidade WARN
     */
    WARN,
    /**
     * Registra uma informação com a severidade ERROR
     */
    ERROR,
    /**
     * Registra uma informação para os desenvolvedores. Criado para que seja possível criar notificações para os desenvolvedores quando alguma nova informação entrada no sistema é relevante a ponto de ser informada para o DEV mas não é um erro ou aviso.
     */
    DEV,
    /**
     * Registra uma Exceção que representa uma Validação.
     *
     * @deprecated Esta enum foi substituída pela VALIDATION. Só é mantida para compatibilidade de sistema legados.
     */
    @Deprecated
    BISVALIDATION,
    /**
     * Registra uma Exceção que representa uma Validação.
     */
    VALIDATION,
    /**
     * Registra uma Exceção que representa Erro/Problema.
     */
    EXCEPTION;
  }

  /**
   * Data e Hora de criação da entrada de Log.
   */
  private LocalDateTime time = null;

  /**
   * Mensagem de Log. Recomendado o limite de 255 caracteres. Qualquer conteúdo maior que 255 caracteres deve ser jogado para o {@link #content}<br>
   * Note que o tamanho é uma recomendação uma vez que este atributo só fica na memória enquanto está no RFW, a limitação é a limitação de tamanho do java, a separação entre message e content ganha mais sentido quando se pensa em banco de dados e a persistência do objeto.
   */
  private String message = null;

  /**
   * Conteúdo é um atributo que permite uma grande quantidade de caracteres.<br>
   * {@link #message} deve ser a mensagem de erro. Neste atributo podemos jogar conteúdos maiores de informações, como pilha, prints de objetos, XMLs, etc. Que possam ajudar o desenvolvedor a resolver o problema.
   */
  private String content = null;

  /**
   * Salvar o endereço completo de onde a exception foi lançada.<br>
   * Salva o ponto da última exception, mesmo que seja uma exception já de tratamento pois está mais vinculada com a ação que foi realizada. A Exception da causa inicial pode ter ocorrido porque foram passados parâmetros inválidos. Fazendo por exemplo que a exception venha de dentro de uma classe nativa do Java.
   */
  private String exPoint = null;

  /**
   * Gravidade do evento ocorrido.
   */
  private RFWLogSeverity severity = null;

  /**
   * Definição de Tags que permitem que o filtro das entradas de log.<br>
   */
  private HashSet<String> tags = null;

  /**
   * Get data e Hora de criação da entrada de Log.
   *
   * @return the data e Hora de criação da entrada de Log
   */
  public LocalDateTime getTime() {
    return time;
  }

  /**
   * Set data e Hora de criação da entrada de Log.
   *
   * @param time the new data e Hora de criação da entrada de Log
   */
  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  /**
   * Get mensagem de Log. Recomendado o limite de 255 caracteres. Qualquer conteúdo maior que 255 caracteres deve ser jogado para o {@link #content}<br>
   * Note que o tamanho é uma recomendação uma vez que este atributo só fica na memória enquanto está no RFW, a limitação é a limitação de tamanho do java, a separação entre message e content ganha mais sentido quando se pensa em banco de dados e a persistência do objeto.
   *
   * @return the mensagem de Log
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set mensagem de Log. Recomendado o limite de 255 caracteres. Qualquer conteúdo maior que 255 caracteres deve ser jogado para o {@link #content}<br>
   * Note que o tamanho é uma recomendação uma vez que este atributo só fica na memória enquanto está no RFW, a limitação é a limitação de tamanho do java, a separação entre message e content ganha mais sentido quando se pensa em banco de dados e a persistência do objeto.
   *
   * @param message the new mensagem de Log
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get gravidade do evento ocorrido.
   *
   * @return the gravidade do evento ocorrido
   */
  public RFWLogSeverity getSeverity() {
    return severity;
  }

  /**
   * Set gravidade do evento ocorrido.
   *
   * @param severity the new gravidade do evento ocorrido
   */
  public void setSeverity(RFWLogSeverity severity) {
    this.severity = severity;
  }

  /**
   * Get definição de Tags que permitem que o filtro das entradas de log.<br>
   * .
   *
   * @return the definição de Tags que permitem que o filtro das entradas de log
   */
  public HashSet<String> getTags() {
    return tags;
  }

  /**
   * Set definição de Tags que permitem que o filtro das entradas de log.<br>
   * .
   *
   * @param tags the new definição de Tags que permitem que o filtro das entradas de log
   */
  public void setTags(HashSet<String> tags) {
    this.tags = tags;
  }

  /**
   * Get conteúdo é um atributo que permite uma grande quantidade de caracteres.<br>
   * {@link #message} deve ser a mensagem de erro. Neste atributo podemos jogar conteúdos maiores de informações, como pilha, prints de objetos, XMLs, etc. Que possam ajudar o desenvolvedor a resolver o problema.
   *
   * @return the conteúdo é um atributo que permite uma grande quantidade de caracteres
   */
  public String getContent() {
    return content;
  }

  /**
   * Set conteúdo é um atributo que permite uma grande quantidade de caracteres.<br>
   * {@link #message} deve ser a mensagem de erro. Neste atributo podemos jogar conteúdos maiores de informações, como pilha, prints de objetos, XMLs, etc. Que possam ajudar o desenvolvedor a resolver o problema.
   *
   * @param content the new conteúdo é um atributo que permite uma grande quantidade de caracteres
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Este método coloca tags nesta entrada de Log.<br>
   * Recomenda-se utilizar este método para simplificar o código externo, para que não seja necessário verificar toda vez se a lista já foi inicializada ou está nula.
   *
   * @param tag Tag a ser colocada no relatório.
   */
  public void addTag(String tag) {
    if (this.tags == null) this.tags = new HashSet<String>();
    this.tags.add(tag);
  }

  /**
   * Imprime o objeto de Log em detalhes
   */
  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();

    if (this.time != null) buff.append(RFW.getDateTimeFormattter().format(this.time)).append(" ");
    if (this.severity != null) buff.append("[").append(RFWBundle.get(this.severity)).append("]:");
    if (this.message != null) buff.append(this.message);
    if (this.tags != null) {
      buff.append("\r\n");
      buff.append("Tags: ");
      int c = 0;
      for (String tag : tags) {
        if (c > 0) buff.append("|");
        buff.append(tag);
        c++;
      }
    }
    if (this.content != null) {
      buff.append("\r\n");
      buff.append("Conteúdo: ").append(this.content).append("\r\n");
    }
    return buff.toString();
  }

  public String getExPoint() {
    return exPoint;
  }

  public void setExPoint(String exPoint) {
    this.exPoint = exPoint;
  }

}
