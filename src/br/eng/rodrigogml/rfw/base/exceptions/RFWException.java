package br.eng.rodrigogml.rfw.base.exceptions;

import javax.ejb.ApplicationException;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;

/**
 * Description: Classe de exceção principal do framework. Todas as demais classes de exceção devem derivar desta.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (11 de jul de 2018)
 */
@ApplicationException(rollback = true)
public abstract class RFWException extends Exception {

  private static final long serialVersionUID = 2597613532959200058L;

  /**
   * Flag usada pelo RFWLogger para evitar o múltiplo log de uma exception.
   */
  private Boolean logged = false;

  /**
   * Código de identificação do erro, ou mensagem de erro (não recomendado).
   */
  private String exceptionCode = null;

  /**
   * Parâmetros para substituir na mensagem de bundle ou para registro no Log.
   */
  private String[] params = null;

  /**
   * Cria uma nova Exception
   *
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWException(Throwable ex) {
    super(ex);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   */
  public RFWException(String exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWException(String exceptionCode, Throwable ex) {
    super(ex);
    this.exceptionCode = exceptionCode;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   */
  public RFWException(String exceptionCode, String[] params) {
    this(exceptionCode);
    this.params = params;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWException(String exceptionCode, String[] params, Throwable ex) {
    this(exceptionCode, ex);
    this.params = params;
  }

  public String getExceptioncode() {
    return exceptionCode;
  }

  /**
   *
   *
   * @param exceptionCode
   */
  protected void setExceptioncode(String exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  /**
   * # parâmetros para substituir na mensagem de bundle ou para registro no Log.
   *
   * @return parâmetros para substituir na mensagem de bundle ou para registro no Log
   */
  public String[] getParams() {
    return params;
  }

  /**
   * @return Obtem a mensagem de erro para o usuário definida pelos parâmetros e tipo de Exception.
   */
  @Override
  public String getMessage() {
    String msg = RFWBundle.get(this);
    if (msg == null) msg = super.getMessage();
    return msg;
  }

  /**
   * # flag usada pelo RFWLogger para evitar o múltiplo log de uma exception.
   *
   * @return flag usada pelo RFWLogger para evitar o múltiplo log de uma exception
   */
  public boolean getLogged() {
    return logged;
  }

  /**
   * # flag usada pelo RFWLogger para evitar o múltiplo log de uma exception.
   *
   * @param logged flag usada pelo RFWLogger para evitar o múltiplo log de uma exception
   */
  public void setLogged(boolean logged) {
    this.logged = logged;
  }

}
