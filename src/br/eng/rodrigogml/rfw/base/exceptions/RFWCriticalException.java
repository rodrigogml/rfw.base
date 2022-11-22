package br.eng.rodrigogml.rfw.base.exceptions;

/**
 * Description: Classe de exceção de erros críticos. Geralmente erros que nunca deveriam ocorrer no sistema e só ocorrem por má programação ou lógica com falha.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (11 de jul de 2018)
 */
public class RFWCriticalException extends RFWException {

  private static final long serialVersionUID = 2905650541268655880L;

  /**
   * Cria uma nova Exception
   */
  public RFWCriticalException() {
    super("Major RFW Exception");
  }

  /**
   * Cria uma nova Exception
   *
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWCriticalException(Throwable ex) {
    super(ex);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWCriticalException(String exceptionCode, String[] params, Throwable ex) {
    super(exceptionCode, params, ex);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   */
  public RFWCriticalException(String exceptionCode, String[] params) {
    super(exceptionCode, params);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWCriticalException(String exceptionCode, Throwable ex) {
    super(exceptionCode, ex);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   */
  public RFWCriticalException(String exceptionCode) {
    super(exceptionCode);
  }
}
