package br.eng.rodrigogml.rfw.base.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

import br.eng.rodrigogml.rfw.base.vo.RFWVO;

/**
 * Description: Classe de exceção de dados inválidos. Lançado sempre que algum dados for submetido a alguma validação e falhar, interrompendo o fluxo normal.<br>
 *
 * @author Rodrigo Leitão
 * @since 1.0
 */
public class RFWValidationException extends RFWException {

  private static final long serialVersionUID = 4119085880770750787L;

  /**
   * Armazena o caminho para o atributo que deu erro. Quando o atributo está dentro de outro objeto que não o principal que foi validado, ou dentro de uma lista, collection, etc.
   */
  private String fieldPath = null;

  /**
   * Classe do objeto que gerou esta validação.
   */
  private String className = null;

  /**
   * Nome do atributo que o problema de validação foi encontrado.
   */
  private String[] fieldName = null;

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   */
  public RFWValidationException(String exceptionCode) {
    super(exceptionCode);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWValidationException(String exceptionCode, Throwable ex) {
    super(exceptionCode, ex);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   */
  public RFWValidationException(String exceptionCode, String[] params) {
    super(exceptionCode, params);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWValidationException(String exceptionCode, String[] params, Throwable ex) {
    super(exceptionCode, params, ex);
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param fieldPath caminho para o field do VO que falhou na validação.
   */
  public RFWValidationException(String exceptionCode, String fieldPath) {
    super(exceptionCode);
    this.fieldPath = fieldPath;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   * @param fieldPath caminho para o field do VO que falhou na validação.
   */
  public RFWValidationException(String exceptionCode, Throwable ex, String fieldPath) {
    super(exceptionCode, ex);
    this.fieldPath = fieldPath;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param fieldPath caminho para o field do VO que falhou na validação.
   */
  public RFWValidationException(String exceptionCode, String[] params, String fieldPath) {
    super(exceptionCode, params);
    this.fieldPath = fieldPath;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   */
  public RFWValidationException(String exceptionCode, String[] params, Throwable ex, String fieldPath) {
    super(exceptionCode, params, ex);
    this.fieldPath = fieldPath;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param fieldPath caminho para o field do VO que falhou na validação.
   * @param className Nome da Classe descendente do {@link RFWVO} que falhou na validação.
   * @param fieldName Nome dos campos que falharam na validação.
   */
  public RFWValidationException(String exceptionCode, String[] params, String fieldPath, String className, String[] fieldName) {
    super(exceptionCode, params);
    this.className = className;
    this.fieldName = fieldName;
    this.fieldPath = fieldPath;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param params Parâmetros que serão substituídos na mensagem do Bundle com o padrão ${0}, ${1} ...
   * @param ex Exception causadora anteriore. Sempre que houver uma exception anterior ela deve ser passada aqui para que o dev tenha a pilha completa do problema.
   * @param fieldPath caminho para o field do VO que falhou na validação.
   * @param className Nome da Classe descendente do {@link RFWVO} que falhou na validação.
   * @param fieldName Nome dos campos que falharam na validação.
   */
  public RFWValidationException(String exceptionCode, String[] params, Throwable ex, String fieldPath, String className, String[] fieldName) {
    super(exceptionCode, params, ex);
    this.className = className;
    this.fieldName = fieldName;
    this.fieldPath = fieldPath;
  }

  /**
   * Cria uma nova Exception
   *
   * @param exceptionCode Código da Exception para identificação. Este código é utilizado também para resovler no arquivo de bundle. Alternativamente pode ser passada a mensagem de erro diretamente, pois quando não encontrado no bundle o valor passado aqui é utilizado.
   * @param fieldPath caminho para o field do VO que falhou na validação.
   * @param className Nome da Classe descendente do {@link RFWVO} que falhou na validação.
   * @param fieldName Nome dos campos que falharam na validação.
   */
  public RFWValidationException(String exceptionCode, String fieldPath, String className, String[] fieldName) {
    super(exceptionCode);
    this.className = className;
    this.fieldName = fieldName;
    this.fieldPath = fieldPath;
  }

  public String getFieldPath() {
    return fieldPath;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String[] getFieldName() {
    return fieldName;
  }

  public void setFieldPath(String fieldPath) {
    this.fieldPath = fieldPath;
  }

  @Override
  public void printStackTrace(PrintStream s) {
    if (this.fieldName != null) s.println("<fieldName>" + this.fieldName + "</fieldName>");
    if (this.fieldPath != null) s.println("<fieldPath>" + this.fieldPath + "</fieldPath>");
    super.printStackTrace(s);
  }

  @Override
  public void printStackTrace(PrintWriter s) {
    if (this.fieldName != null) s.println("<fieldName>" + this.fieldName + "</fieldName>");
    if (this.fieldPath != null) s.println("<fieldPath>" + this.fieldPath + "</fieldPath>");
    super.printStackTrace(s);
  }

}
