package br.eng.rodrigogml.rfw.base.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Description: Classe de exceção de validação de dados do Framework. Usada para acumular exceções de validações da camada de negócio.<br>
 *
 * @author Rodrigo Leitão
 * @since 1.0 (JUN / 2006)
 */
public class RFWValidationGroupException extends RFWValidationException {

  private static final long serialVersionUID = -4776059506193716369L;

  private final ArrayList<RFWValidationException> validationlist = new ArrayList<>();

  public RFWValidationGroupException() {
    super(null);
  }

  public RFWValidationGroupException(ArrayList<RFWValidationException> validationlist) {
    super(null);
    if (validationlist != null) for (RFWValidationException ex : validationlist) {
      if (getExceptioncode() == null) setExceptioncode(ex.getExceptioncode());
      addValidationException(ex);
    }
  }

  public RFWValidationGroupException(RFWValidationException ex) {
    super(ex.getExceptioncode());
    addValidationException(ex);
  }

  public void addValidationException(RFWValidationException ex) {
    validationlist.add(ex);
    if (getExceptioncode() == null) setExceptioncode(ex.getExceptioncode());
  }

  public void addValidationException(RFWValidationGroupException ex) {
    validationlist.addAll(ex.getValidationlist());
    if (getExceptioncode() == null) setExceptioncode(ex.getExceptioncode());
  }

  public void addValidationException(String exceptioncode) {
    validationlist.add(new RFWValidationException(exceptioncode));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, Throwable ex) {
    validationlist.add(new RFWValidationException(exceptioncode, ex));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, String[] params) {
    validationlist.add(new RFWValidationException(exceptioncode, params));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, String[] params, Throwable ex) {
    validationlist.add(new RFWValidationException(exceptioncode, params, ex));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, String fieldname) {
    validationlist.add(new RFWValidationException(exceptioncode, fieldname));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, Throwable ex, String fieldname) {
    validationlist.add(new RFWValidationException(exceptioncode, ex, fieldname));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, String[] params, String fieldname) {
    validationlist.add(new RFWValidationException(exceptioncode, params, fieldname));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public void addValidationException(String exceptioncode, String[] params, Throwable ex, String fieldname) {
    validationlist.add(new RFWValidationException(exceptioncode, params, ex, fieldname));
    if (getExceptioncode() == null) setExceptioncode(exceptioncode);
  }

  public ArrayList<RFWValidationException> getValidationlist() {
    return validationlist;
  }

  @Override
  public String getFieldPath() {
    // Recupera os dados do primeiro item da lista de exceptions, transformando esta exception em uma transparência da primeira exception da lista
    if (validationlist != null && validationlist.size() > 0) return this.validationlist.get(0).getFieldPath();
    return super.getFieldPath();
  }

  @Override
  public String getClassName() {
    // Recupera os dados do primeiro item da lista de exceptions, transformando esta exception em uma transparência da primeira exception da lista
    if (validationlist != null && validationlist.size() > 0) return this.validationlist.get(0).getClassName();
    return super.getClassName();
  }

  @Override
  public String[] getFieldName() {
    // Recupera os dados do primeiro item da lista de exceptions, transformando esta exception em uma transparência da primeira exception da lista
    if (validationlist != null && validationlist.size() > 0) return this.validationlist.get(0).getFieldName();
    return super.getFieldName();
  }

  @Override
  public String[] getParams() {
    // Recupera os dados do primeiro item da lista de exceptions, transformando esta exception em uma transparência da primeira exception da lista
    if (validationlist != null && validationlist.size() > 0) return this.validationlist.get(0).getParams();
    return super.getParams();
  }

  @Override
  public void printStackTrace() {
    super.printStackTrace();
    // Em seguinda imprime o Stack Trace das Exceções filhas
    for (RFWValidationException e : this.validationlist) {
      e.printStackTrace();
    }
  }

  @Override
  public void printStackTrace(PrintStream s) {
    super.printStackTrace(s);
    // Em seguinda imprime o Stack Trace das Exceções filhas
    for (RFWValidationException e : this.validationlist) {
      e.printStackTrace(s);
    }
  }

  @Override
  public void printStackTrace(PrintWriter w) {
    super.printStackTrace(w);
    // Em seguinda imprime o Stack Trace das Exceções filhas
    for (RFWValidationException e : this.validationlist) {
      e.printStackTrace(w);
    }
  }

  /**
   * Retorna a quantidade de excepcions existentes dentro do Grupo.
   *
   * @return
   */
  public int size() {
    return this.validationlist.size();
  }
}
