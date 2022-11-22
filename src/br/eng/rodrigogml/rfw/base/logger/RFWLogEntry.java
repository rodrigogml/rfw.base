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
     * Registra um objeto no LOG. O RFWLogger far� o m�ximo para imprimir as informa��es do objeto no log.
     */
    OBJECT,
    /**
     * Registra uma informa��o com a severidade DEBUG
     */
    DEBUG,
    /**
     * Registra uma informa��o com a severidade INFO
     */
    INFO,
    /**
     * Registra uma informa��o com a severidade WARN
     */
    WARN,
    /**
     * Registra uma informa��o com a severidade ERROR
     */
    ERROR,
    /**
     * Registra uma informa��o para os desenvolvedores. Criado para que seja poss�vel criar notifica��es para os desenvolvedores quando alguma nova informa��o entrada no sistema � relevante a ponto de ser informada para o DEV mas n�o � um erro ou aviso.
     */
    DEV,
    /**
     * Registra uma Exce��o que representa uma Valida��o.
     *
     * @deprecated Esta enum foi substitu�da pela VALIDATION. S� � mantida para compatibilidade de sistema legados.
     */
    @Deprecated
    BISVALIDATION,
    /**
     * Registra uma Exce��o que representa uma Valida��o.
     */
    VALIDATION,
    /**
     * Registra uma Exce��o que representa Erro/Problema.
     */
    EXCEPTION;
  }

  /**
   * Data e Hora de cria��o da entrada de Log.
   */
  private LocalDateTime time = null;

  /**
   * Mensagem de Log. Recomendado o limite de 255 caracteres. Qualquer conte�do maior que 255 caracteres deve ser jogado para o {@link #content}<br>
   * Note que o tamanho � uma recomenda��o uma vez que este atributo s� fica na mem�ria enquanto est� no RFW, a limita��o � a limita��o de tamanho do java, a separa��o entre message e content ganha mais sentido quando se pensa em banco de dados e a persist�ncia do objeto.
   */
  private String message = null;

  /**
   * Conte�do � um atributo que permite uma grande quantidade de caracteres.<br>
   * {@link #message} deve ser a mensagem de erro. Neste atributo podemos jogar conte�dos maiores de informa��es, como pilha, prints de objetos, XMLs, etc. Que possam ajudar o desenvolvedor a resolver o problema.
   */
  private String content = null;

  /**
   * Salvar o endere�o completo de onde a exception foi lan�ada.<br>
   * Salva o ponto da �ltima exception, mesmo que seja uma exception j� de tratamento pois est� mais vinculada com a a��o que foi realizada. A Exception da causa inicial pode ter ocorrido porque foram passados par�metros inv�lidos. Fazendo por exemplo que a exception venha de dentro de uma classe nativa do Java.
   */
  private String exPoint = null;

  /**
   * Gravidade do evento ocorrido.
   */
  private RFWLogSeverity severity = null;

  /**
   * Defini��o de Tags que permitem que o filtro das entradas de log.<br>
   */
  private HashSet<String> tags = null;

  /**
   * Get data e Hora de cria��o da entrada de Log.
   *
   * @return the data e Hora de cria��o da entrada de Log
   */
  public LocalDateTime getTime() {
    return time;
  }

  /**
   * Set data e Hora de cria��o da entrada de Log.
   *
   * @param time the new data e Hora de cria��o da entrada de Log
   */
  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  /**
   * Get mensagem de Log. Recomendado o limite de 255 caracteres. Qualquer conte�do maior que 255 caracteres deve ser jogado para o {@link #content}<br>
   * Note que o tamanho � uma recomenda��o uma vez que este atributo s� fica na mem�ria enquanto est� no RFW, a limita��o � a limita��o de tamanho do java, a separa��o entre message e content ganha mais sentido quando se pensa em banco de dados e a persist�ncia do objeto.
   *
   * @return the mensagem de Log
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set mensagem de Log. Recomendado o limite de 255 caracteres. Qualquer conte�do maior que 255 caracteres deve ser jogado para o {@link #content}<br>
   * Note que o tamanho � uma recomenda��o uma vez que este atributo s� fica na mem�ria enquanto est� no RFW, a limita��o � a limita��o de tamanho do java, a separa��o entre message e content ganha mais sentido quando se pensa em banco de dados e a persist�ncia do objeto.
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
   * Get defini��o de Tags que permitem que o filtro das entradas de log.<br>
   * .
   *
   * @return the defini��o de Tags que permitem que o filtro das entradas de log
   */
  public HashSet<String> getTags() {
    return tags;
  }

  /**
   * Set defini��o de Tags que permitem que o filtro das entradas de log.<br>
   * .
   *
   * @param tags the new defini��o de Tags que permitem que o filtro das entradas de log
   */
  public void setTags(HashSet<String> tags) {
    this.tags = tags;
  }

  /**
   * Get conte�do � um atributo que permite uma grande quantidade de caracteres.<br>
   * {@link #message} deve ser a mensagem de erro. Neste atributo podemos jogar conte�dos maiores de informa��es, como pilha, prints de objetos, XMLs, etc. Que possam ajudar o desenvolvedor a resolver o problema.
   *
   * @return the conte�do � um atributo que permite uma grande quantidade de caracteres
   */
  public String getContent() {
    return content;
  }

  /**
   * Set conte�do � um atributo que permite uma grande quantidade de caracteres.<br>
   * {@link #message} deve ser a mensagem de erro. Neste atributo podemos jogar conte�dos maiores de informa��es, como pilha, prints de objetos, XMLs, etc. Que possam ajudar o desenvolvedor a resolver o problema.
   *
   * @param content the new conte�do � um atributo que permite uma grande quantidade de caracteres
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Este m�todo coloca tags nesta entrada de Log.<br>
   * Recomenda-se utilizar este m�todo para simplificar o c�digo externo, para que n�o seja necess�rio verificar toda vez se a lista j� foi inicializada ou est� nula.
   *
   * @param tag Tag a ser colocada no relat�rio.
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
      buff.append("Conte�do: ").append(this.content).append("\r\n");
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
