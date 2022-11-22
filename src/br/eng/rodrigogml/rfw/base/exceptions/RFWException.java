package br.eng.rodrigogml.rfw.base.exceptions;

import javax.ejb.ApplicationException;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;

/**
 * Description: Classe de exce��o principal do framework. Todas as demais classes de exce��o devem derivar desta.<br>
 *
 * @author Rodrigo Leit�o
 * @since 10.0.0 (11 de jul de 2018)
 */
@ApplicationException(rollback = true)
public abstract class RFWException extends Exception {

  private static final long serialVersionUID = 2597613532959200058L;

  /**
   * Flag usada pelo RFWLogger para evitar o m�ltiplo log de uma exception.
   */
  private Boolean logged = false;

  /**
   * C�digo de identifica��o do erro, ou mensagem de erro (n�o recomendado).
   */
  private String exceptionCode = null;

  /**
   * Par�metros para substituir na mensagem de bundle ou para registro no Log.
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
   * @param exceptionCode C�digo da Exception para identifica��o. Este c�digo � utilizado tamb�m para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando n�o encontrado no bundle o valor passado aqui � utilizado.
   */
  public RFWException(String exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode C�digo da Exception para identifica��o. Este c�digo � utilizado tamb�m para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando n�o encontrado no bundle o valor passado aqui � utilizado.
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWException(String exceptionCode, Throwable ex) {
    super(ex);
    this.exceptionCode = exceptionCode;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode C�digo da Exception para identifica��o. Este c�digo � utilizado tamb�m para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando n�o encontrado no bundle o valor passado aqui � utilizado.
   * @param params Par�metros que ser�o substitu�dos na mensagem do Bundle com o padr�o ${0}, ${1} ...
   */
  public RFWException(String exceptionCode, String[] params) {
    this(exceptionCode);
    this.params = params;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode C�digo da Exception para identifica��o. Este c�digo � utilizado tamb�m para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando n�o encontrado no bundle o valor passado aqui � utilizado.
   * @param params Par�metros que ser�o substitu�dos na mensagem do Bundle com o padr�o ${0}, ${1} ...
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
   * # par�metros para substituir na mensagem de bundle ou para registro no Log.
   *
   * @return par�metros para substituir na mensagem de bundle ou para registro no Log
   */
  public String[] getParams() {
    return params;
  }

  /**
   * @return Obtem a mensagem de erro para o usu�rio definida pelos par�metros e tipo de Exception.
   */
  @Override
  public String getMessage() {
    String msg = RFWBundle.get(this);
    if (msg == null) msg = super.getMessage();
    return msg;
  }

  /**
   * # flag usada pelo RFWLogger para evitar o m�ltiplo log de uma exception.
   *
   * @return flag usada pelo RFWLogger para evitar o m�ltiplo log de uma exception
   */
  public boolean getLogged() {
    return logged;
  }

  /**
   * # flag usada pelo RFWLogger para evitar o m�ltiplo log de uma exception.
   *
   * @param logged flag usada pelo RFWLogger para evitar o m�ltiplo log de uma exception
   */
  public void setLogged(boolean logged) {
    this.logged = logged;
  }

}
