package br.eng.rodrigogml.rfw.base.exceptions;

import javax.ejb.ApplicationException;

/**
 * Description: Classe de exceções de tempo de execução. Devem ser evitadas ao máximo se substituindo, por exemplo, por exceções de validação sempre que possível.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.0 (25/01/2011)
 */
@ApplicationException(rollback = true) // Define que qualquer exceção do sistema aborta a transação
public class RFWRunTimeException extends RuntimeException {

  private static final long serialVersionUID = 4803314708800763826L;

  private final RFWException rfwException;
  private final String[] params;

  /**
   * Cria uma nova Exception
   *
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWRunTimeException(RFWException ex) {
    super(ex);
    this.rfwException = ex;
    this.params = null;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   */
  public RFWRunTimeException(String exceptionCode) {
    super(exceptionCode);
    this.rfwException = null;
    this.params = null;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   */
  public RFWRunTimeException(String exceptionCode, Throwable cause) {
    super(exceptionCode, cause);
    this.rfwException = null;
    this.params = null;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   */
  public RFWRunTimeException(String exceptionCode, String[] params) {
    super(exceptionCode);
    this.rfwException = null;
    this.params = params;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   */
  public RFWRunTimeException(String exceptionCode, String[] params, Throwable cause) {
    super(exceptionCode, cause);
    this.rfwException = null;
    this.params = params;
  }

  public String[] getParams() {
    return params;
  }

  public RFWException getRFWException() {
    return rfwException;
  }

}
