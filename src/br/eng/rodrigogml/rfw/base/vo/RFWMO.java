package br.eng.rodrigogml.rfw.base.vo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import br.eng.rodrigogml.rfw.base.preprocess.PreProcess;
import br.eng.rodrigogml.rfw.base.sessionmanager.interceptors.SMInterceptor;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWRunTimeException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Classe pai de todos os Match Objects do sistema. Permitindo assim que métodos consigam operar genericamente em muitos casos.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.0.0 (SET / 2009)
 * @version 7.1.0 (18/07/2015) - Rodrigo Leitão - Substituição do sistema hierarquico de MOs {@link RFWMO} para este MO ùnico.
 */
public final class RFWMO implements Serializable, Cloneable {

  private static final long serialVersionUID = 5581362952656098332L;

  /**
   * Classe interna usada para guardar os valores das condições de busca do RFWMO.
   */
  public static final class RFWMOData implements Serializable {
    private static final long serialVersionUID = -9013306659167465139L;

    final String fieldname;
    final Object value;

    public RFWMOData(String fieldname, Object value) {
      this.fieldname = fieldname;
      this.value = value;
    }

    /**
     * Gets the fieldname.
     *
     * @return the fieldname
     */
    public String getFieldname() {
      return fieldname;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Object getValue() {
      return value;
    }

    @Override
    public String toString() {
      return this.fieldname + ": " + this.value;
    }

  }

  public static enum APPENDMETHOD {
    AND, OR
  }

  /**
   * Define como os argumentos deste MO serão conectados entre si.<br>
   * Por padrão os argumentos definidos recebem o conector condicional "AND" entre eles. Caso alterado, todos os atributos deste MO passarão se ser conectados com o novo conector condicional.
   */
  private APPENDMETHOD appendmethod = APPENDMETHOD.AND;

  /**
   * Permite definir uma lista de MOs que montarão uma condição "isolada". Em conjunto com as definições possíveis em {@link #appendmethod} permite que condições com diferentes conectores sejam usadas em conjunto. Cria o efeito dos "parenteses" nas clausulas where do SQL.
   */
  private List<RFWMO> submo = null;

  private LinkedList<RFWMOData> equal = new LinkedList<>();
  private LinkedList<RFWMOData> notEqual = new LinkedList<>();
  private LinkedList<RFWMOData> greaterThanOrEqualTo = new LinkedList<>();
  private LinkedList<RFWMOData> greaterThan = new LinkedList<>();
  private LinkedList<RFWMOData> lessThanOrEqualTo = new LinkedList<>();
  private LinkedList<RFWMOData> lessThan = new LinkedList<>();
  private LinkedList<RFWMOData> isNull = new LinkedList<>();
  private LinkedList<RFWMOData> isNotNull = new LinkedList<>();
  private LinkedList<RFWMOData> like = new LinkedList<>();
  private LinkedList<RFWMOData> in = new LinkedList<>();
  private LinkedList<RFWMOData> notIn = new LinkedList<>();

  public RFWMO() {
  }

  public RFWMO(APPENDMETHOD appendMethod) {
    this.appendmethod = appendMethod;
  }

  public RFWMO isNotNull(String fieldname) {
    this.isNotNull.add(new RFWMOData(fieldname, null));
    return this;
  }

  public RFWMO isNull(String fieldname) {
    this.isNull.add(new RFWMOData(fieldname, null));
    return this;
  }

  public RFWMO equal(String fieldname, Object value) {
    this.equal.add(new RFWMOData(fieldname, value));
    return this;
  }

  /**
   * Este método simplifica a escrita de uma condição para buscar/selecionar um registro que retorne TRUE para uma sobreposição de valores (datas, ranges numéricos, etc.). <br>
   * <br>
   * Simplismente escreve a seguinte condição: (fs == null || fs < ne) && (fe == null || fe > ns), onde:
   * <li>fs = Field Start (Coluna da tabela com o valor inicial do período na base)
   * <li>fe = Field End (Coluna da tabela com o valor final do período na base)
   * <li>ns = New Start (Valor inicial do período que estamos querendo testar a sobreposição)
   * <li>ne = New End (Valod final do período que estamos querendo testar a sobreposição) <br>
   * <br>
   * <b>Observação:</b> por escrever duas condições que obrigatoriamente precisam ser consideradas com o operador {@link APPENDMETHOD#AND}, caso este MO esteja definido para outro {@link #appendmethod} que não {@link APPENDMETHOD#AND} será automaticamente criado um novo {@link RFWMO} e colocado como um SubMO.<br>
   * <b>Obsevação 2:</b> Os intervalos "são exclusivos", isto é, são utilizados os comparativos > e < e não >= e <=. Em outras palavras, mesmo que um periodo termine em uma data e o outro comece na mesma data, não é considerado sobreposição. <br>
   * <b>Observação 3:</b> Caso os valores das colunas estejam nulos é considerado que trata-se de valor "infinito". Isto é, se a coluna de início do período for nula é considerado que ela começou no "infinito incial", logo qualquer valor passado periodo informado terá iniciado depois. O mesmo vale para a coluna de fim do período, vindo valor nulo é considerado que o período do banco não terminou
   * (final infinito), em outras palavras qualquer período informado terá terminado antes do período da base.
   *
   * @param startFieldName Coluna que contém o valor do início do período na base de dados.
   * @param endFieldName Coluna que contém o valor do fim do período na base de dados.
   * @param startValue Valor inicial do período que será testado contra o período da base. Não pode ser nulo. Caso não tenha um valor inicial a busca pode ser realizada "forçando" o menor valor possível para o tipo de dado, algo como {@link Integer#MIN_VALUE} ou {@link LocalDate#MAX}.
   * @param endValue Valor final do período que será testado contra o período da base. Não pode ser nulo. Caso não tenha um valor final a busca pode ser realizada "forçando" o maior valor possível para o tipo de dado, algo como {@link Integer#MAX_VALUE} ou {@link LocalDate#MIN}
   * @return Retorna esta instância do RFWMO.
   */
  public RFWMO overlap(String startFieldName, String endFieldName, Object startValue, Object endValue) {
    RFWMO mo = this;
    if (mo.getAppendmethod() != APPENDMETHOD.AND) { // Observação 1
      mo = new RFWMO(APPENDMETHOD.AND);
      this.getSubmo().add(mo);
    }
    {// (fs == null || fs < ne)
      RFWMO mo2 = new RFWMO(APPENDMETHOD.OR);
      mo2.isNull(startFieldName); // fs == null
      mo2.lessThan(startFieldName, endValue); // fs < ne
      mo.getSubmo().add(mo2);
    }
    { // (fe == null || fe > ns)
      RFWMO mo2 = new RFWMO(APPENDMETHOD.OR);
      mo2.isNull(endFieldName);
      mo2.greaterThan(endFieldName, startValue);
      mo.getSubmo().add(mo2);
    }
    return this;
  }

  public RFWMO like(String fieldname, String pattern) {
    this.like.add(new RFWMOData(fieldname, pattern));
    return this;
  }

  public RFWMO in(String fieldname, Collection<?> valuelist) {
    if (valuelist == null || valuelist.size() == 0) throw new RFWRunTimeException("Não é permitido incluir uma lista sem valores no atributo IN do RFWMO!");
    this.in.add(new RFWMOData(fieldname, valuelist));
    return this;
  }

  public RFWMO in(String fieldname, Object[] valuelist) {
    List<Object> l = new LinkedList<>();
    for (Object t : valuelist) {
      l.add(t);
    }
    this.in.add(new RFWMOData(fieldname, l));
    return this;
  }

  public RFWMO notIn(String fieldname, Collection<?> valuelist) {
    if (valuelist == null || valuelist.size() == 0) throw new RFWRunTimeException("Não é permitido incluir uma lista sem valores no atributo NOT IN do RFWMO!");
    this.notIn.add(new RFWMOData(fieldname, valuelist));
    return this;
  }

  public RFWMO notIn(String fieldname, Object[] valuelist) {
    List<Object> l = new LinkedList<>();
    for (Object t : valuelist) {
      l.add(t);
    }
    this.notIn.add(new RFWMOData(fieldname, l));
    return this;
  }

  public RFWMO notEqual(String fieldname, Object value) {
    this.notEqual.add(new RFWMOData(fieldname, value));
    return this;
  }

  public RFWMO greaterThanOrEqualTo(String fieldname, Object value) {
    this.greaterThanOrEqualTo.add(new RFWMOData(fieldname, value));
    return this;
  }

  public RFWMO greaterThan(String fieldname, Object value) {
    this.greaterThan.add(new RFWMOData(fieldname, value));
    return this;
  }

  public RFWMO lessThanOrEqualTo(String fieldname, Object value) {
    this.lessThanOrEqualTo.add(new RFWMOData(fieldname, value));
    return this;
  }

  public RFWMO lessThan(String fieldname, Object value) {
    this.lessThan.add(new RFWMOData(fieldname, value));
    return this;
  }

  /**
   * Implementação de um clone genérico para todos os VOs do Framework.<br>
   * Duplica o objeto e todos os objetos não mutáveis que tenham métodos get e set.<br>
   * Para uma clonagem mais específica extender este método em cada VO
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * Método responsável por clonar o RFWMO inteiro evitando que suas referências sejam alteradas.<br>
   * Usado, por exemplo, nas fachadas {@link SMInterceptor} para evitar que o objeto seja alterado no CRUD e fique diferente caso Facade e Invoker estejam na mesma JVM.
   *
   * @return Retorna o RFWMO clonado recursivamente
   * @throws RFWException
   */
  public RFWMO cloneRecursive() throws RFWException {
    try {
      return cloneRecursive(new HashMap<RFWMO, RFWMO>());
    } catch (CloneNotSupportedException e) {
      throw new RFWCriticalException(e);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected RFWMO cloneRecursive(HashMap<RFWMO, RFWMO> clonedobjects) throws CloneNotSupportedException {
    // Veririca se já estamos na hash, se estiver retornamos esse objeto
    RFWMO clonedvo = clonedobjects.get(this);
    if (clonedvo != null) {
      return clonedvo;
    }

    // Se ainda não estamos na hash de objetos clonados, nos clonamos e incluimos na hash
    clonedvo = (RFWMO) super.clone();
    clonedobjects.put(this, clonedvo);

    // Clona primeiro os LinkedList dos valores
    clonedvo.equal = (LinkedList<RFWMOData>) this.equal.clone();
    clonedvo.notEqual = (LinkedList<RFWMOData>) this.notEqual.clone();
    clonedvo.greaterThanOrEqualTo = (LinkedList<RFWMOData>) this.greaterThanOrEqualTo.clone();
    clonedvo.greaterThan = (LinkedList<RFWMOData>) this.greaterThan.clone();
    clonedvo.lessThanOrEqualTo = (LinkedList<RFWMOData>) this.lessThanOrEqualTo.clone();
    clonedvo.lessThan = (LinkedList<RFWMOData>) this.lessThan.clone();
    clonedvo.isNull = (LinkedList<RFWMOData>) this.isNull.clone();
    clonedvo.isNotNull = (LinkedList<RFWMOData>) this.isNotNull.clone();
    clonedvo.like = (LinkedList<RFWMOData>) this.like.clone();
    clonedvo.in = (LinkedList<RFWMOData>) this.in.clone();
    clonedvo.notIn = (LinkedList<RFWMOData>) this.notIn.clone();

    // Recupera a lista de métodos desse objeto
    Method[] methods = this.getClass().getMethods();
    // Itera essa lista atrás de métodos do tipo "get" ou "is"
    for (int i = 0; i < methods.length; i++) {
      String methodname = methods[i].getName();
      if (methodname.startsWith("get") || methodname.startsWith("is")) {
        Method methodget = methods[i];
        Class<?> returntype = methodget.getReturnType();
        Method methodset = null;
        try {
          methodset = this.getClass().getMethod("set" + methodname.substring(3, methodname.length()), returntype);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
        // Verifica a existência do Método SET
        if (methodset != null) {
          // Verifica se o retorno do método get não é nulo
          try {
            Object gettedvalue = methodget.invoke(this);
            if (gettedvalue != null) {
              // Verifica se o tipo de objeto é um dos que desejamos fazer o "deep clone"
              if (gettedvalue instanceof RFWMO) {
                RFWMO clonedvalue = ((RFWMO) gettedvalue).cloneRecursive(clonedobjects);
                methodset.invoke(clonedvo, clonedvalue);
              } else if (gettedvalue instanceof LinkedList) {
                LinkedList currentvalue = (LinkedList) gettedvalue;
                LinkedList clonedvalue = (LinkedList) currentvalue.clone();
                clonedvalue.clear();
                for (Object object : currentvalue) {
                  if (object instanceof RFWMO) {
                    clonedvalue.add(((RFWMO) object).cloneRecursive(clonedobjects));
                  } else {
                    clonedvalue.add(object);
                  }
                }
                methodset.invoke(clonedvo, clonedvalue);
              } else if (gettedvalue instanceof HashMap) {
                HashMap currentvalue = (HashMap) gettedvalue;
                HashMap clonedvalue = (HashMap) currentvalue.clone();
                clonedvalue.clear();
                for (Object key : currentvalue.keySet()) {
                  Object object = currentvalue.get(key);
                  if (object instanceof RFWMO) {
                    clonedvalue.put(key, ((RFWMO) object).cloneRecursive(clonedobjects));
                  } else {
                    clonedvalue.put(key, object);
                  }
                }
                methodset.invoke(clonedvo, clonedvalue);
              }
            }
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }
    }

    return clonedvo;
  }

  /**
   * Recupera o define como os argumentos deste MO serão conectados entre si.<br>
   * Por padrão os argumentos definidos recebem o conector condicional "AND" entre eles. Caso alterado, todos os atributos deste MO passarão se ser conectados com o novo conector condicional.
   *
   * @return the define como os argumentos deste MO serão conectados entre si
   */
  public APPENDMETHOD getAppendmethod() {
    return appendmethod;
  }

  /**
   * Define o define como os argumentos deste MO serão conectados entre si.<br>
   * Por padrão os argumentos definidos recebem o conector condicional "AND" entre eles. Caso alterado, todos os atributos deste MO passarão se ser conectados com o novo conector condicional.
   *
   * @param appendmethod the new define como os argumentos deste MO serão conectados entre si
   */
  public void setAppendmethod(APPENDMETHOD appendmethod) {
    this.appendmethod = appendmethod;
  }

  /**
   * Recupera o permite definir uma lista de MOs que montarão uma condição "isolada". Em conjunto com as definições possíveis em {@link #appendmethod} permite que condições com diferentes conectores sejam usadas em conjunto. Cria o efeito dos "parenteses" nas clausulas where do SQL.
   *
   * @return the permite definir uma lista de MOs que montarão uma condição "isolada"
   */
  public List<RFWMO> getSubmo() {
    if (submo == null) this.submo = new LinkedList<>();
    return submo;
  }

  /**
   * Define o permite definir uma lista de MOs que montarão uma condição "isolada". Em conjunto com as definições possíveis em {@link #appendmethod} permite que condições com diferentes conectores sejam usadas em conjunto. Cria o efeito dos "parenteses" nas clausulas where do SQL.
   *
   * @param submo the new permite definir uma lista de MOs que montarão uma condição "isolada"
   */
  public void setSubmo(List<RFWMO> submo) {
    this.submo = submo;
  }

  /**
   * Gets the equal.
   *
   * @return the equal
   */
  public LinkedList<RFWMOData> getEqual() {
    return equal;
  }

  /**
   * Gets the not equal.
   *
   * @return the not equal
   */
  public LinkedList<RFWMOData> getNotEqual() {
    return notEqual;
  }

  /**
   * Gets the greater than or equal to.
   *
   * @return the greater than or equal to
   */
  public LinkedList<RFWMOData> getGreaterThanOrEqualTo() {
    return greaterThanOrEqualTo;
  }

  /**
   * Gets the greater than.
   *
   * @return the greater than
   */
  public LinkedList<RFWMOData> getGreaterThan() {
    return greaterThan;
  }

  /**
   * Gets the less than or equal to.
   *
   * @return the less than or equal to
   */
  public LinkedList<RFWMOData> getLessThanOrEqualTo() {
    return lessThanOrEqualTo;
  }

  /**
   * Gets the less than.
   *
   * @return the less than
   */
  public LinkedList<RFWMOData> getLessThan() {
    return lessThan;
  }

  /**
   * Gets the checa se null.
   *
   * @return the checa se null
   */
  public LinkedList<RFWMOData> getIsNull() {
    return isNull;
  }

  /**
   * Gets the checa se not null.
   *
   * @return the checa se not null
   */
  public LinkedList<RFWMOData> getIsNotNull() {
    return isNotNull;
  }

  /**
   * Gets the like.
   *
   * @return the like
   */
  public LinkedList<RFWMOData> getLike() {
    return like;
  }

  /**
   * Gets the in.
   *
   * @return the in
   */
  public LinkedList<RFWMOData> getIn() {
    return in;
  }

  /**
   * Gets the not in.
   *
   * @return the not in
   */
  public LinkedList<RFWMOData> getNotIn() {
    return notIn;
  }

  /**
   * Sets the equal.
   *
   * @param equal the new equal
   */
  protected final void setEqual(LinkedList<RFWMOData> equal) {
    this.equal = equal;
  }

  /**
   * Sets the not equal.
   *
   * @param notEqual the new not equal
   */
  protected final void setNotEqual(LinkedList<RFWMOData> notEqual) {
    this.notEqual = notEqual;
  }

  /**
   * Sets the greater than or equal to.
   *
   * @param greaterThanOrEqualTo the new greater than or equal to
   */
  protected final void setGreaterThanOrEqualTo(LinkedList<RFWMOData> greaterThanOrEqualTo) {
    this.greaterThanOrEqualTo = greaterThanOrEqualTo;
  }

  /**
   * Sets the greater than.
   *
   * @param greaterThan the new greater than
   */
  protected final void setGreaterThan(LinkedList<RFWMOData> greaterThan) {
    this.greaterThan = greaterThan;
  }

  /**
   * Sets the less than or equal to.
   *
   * @param lessThanOrEqualTo the new less than or equal to
   */
  protected final void setLessThanOrEqualTo(LinkedList<RFWMOData> lessThanOrEqualTo) {
    this.lessThanOrEqualTo = lessThanOrEqualTo;
  }

  /**
   * Sets the less than.
   *
   * @param lessThan the new less than
   */
  protected final void setLessThan(LinkedList<RFWMOData> lessThan) {
    this.lessThan = lessThan;
  }

  /**
   * Sets the checa se null.
   *
   * @param isNull the new checa se null
   */
  protected final void setIsNull(LinkedList<RFWMOData> isNull) {
    this.isNull = isNull;
  }

  /**
   * Sets the checa se not null.
   *
   * @param isNotNull the new checa se not null
   */
  protected final void setIsNotNull(LinkedList<RFWMOData> isNotNull) {
    this.isNotNull = isNotNull;
  }

  /**
   * Sets the like.
   *
   * @param like the new like
   */
  protected final void setLike(LinkedList<RFWMOData> like) {
    this.like = like;
  }

  /**
   * Sets the in.
   *
   * @param in the new in
   */
  protected final void setIn(LinkedList<RFWMOData> in) {
    this.in = in;
  }

  /**
   * Sets the not in.
   *
   * @param notIn the new not in
   */
  protected final void setNotIn(LinkedList<RFWMOData> notIn) {
    this.notIn = notIn;
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findEqual(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : equal) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findNotEqual(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : notEqual) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findGreaterThanOrEqualTo(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : greaterThanOrEqualTo) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findGreaterThan(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : greaterThan) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findLessThan(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : lessThan) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findLessThanOrEqualTo(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : lessThanOrEqualTo) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findIsNull(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : isNull) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findIsNotNull(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : isNotNull) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findLike(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : like) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findIn(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : in) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Procura os filtros de um determinado campo.
   *
   * @param fieldname Nome do Campo para procurar os filtros
   * @return Array com todos os valores definidos para o filtro.
   */
  public Object[] findNotIn(String fieldname) {
    final LinkedList<Object> found = new LinkedList<>();
    for (RFWMOData data : in) {
      if (fieldname.equals(data.getFieldname())) {
        found.add(data.value);
      }
    }
    return found.toArray(new Object[0]);
  }

  /**
   * Este método imprime as condições deste MO como sendo o SQL. Usado apenas para DEBUG e registro do objeto
   *
   * @return String para visualização com as condições definidas no RFWMO.
   */
  public String printConditions() {
    StringBuilder sb = new StringBuilder();
    for (RFWMOData d : equal) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append('=').append(d.value);
    }
    for (RFWMOData d : notEqual) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append("!=").append(d.value);
    }
    for (RFWMOData d : greaterThan) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append('>').append(d.value);
    }
    for (RFWMOData d : greaterThanOrEqualTo) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append(">=").append(d.value);
    }
    for (RFWMOData d : lessThan) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append('<').append(d.value);
    }
    for (RFWMOData d : lessThanOrEqualTo) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append("<=").append(d.value);
    }
    for (RFWMOData d : isNull) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append(" is null");
    }
    for (RFWMOData d : isNotNull) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append(" is not null");
    }
    for (RFWMOData d : like) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append(" like '").append(d.value).append("'");
    }
    for (RFWMOData d : in) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append(" in (").append(d.value).append(")");
    }
    for (RFWMOData d : notIn) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append(d.fieldname).append(" not in (").append(d.value).append(")");
    }
    for (RFWMO bMO : getSubmo()) {
      if (sb.length() > 0) sb.append(' ').append(this.getAppendmethod()).append(' ');
      sb.append("(").append(bMO.printConditions()).append(")");
    }
    return sb.toString();
  }

  /**
   * Sobreescrito para ajudar no DEBUG durante o desenvolvimento. Não utilizar como parte do código já que sua implementação pode ser alterada. Para obter o "impressão" das condições utilize o método printConditions()
   */
  @Override
  public String toString() {
    return super.toString() + "/" + printConditions();
  }

  /**
   * Recupera a lista com todos os atributos usados no MO, incluindo dos SubMOs, caso existam.
   *
   * @return lista dos atributos.
   */
  public List<String> getAttributes() {
    final LinkedList<String> list = new LinkedList<>();

    for (RFWMOData d : equal)
      list.add(d.fieldname);
    for (RFWMOData d : notEqual)
      list.add(d.fieldname);
    for (RFWMOData d : greaterThan)
      list.add(d.fieldname);
    for (RFWMOData d : greaterThanOrEqualTo)
      list.add(d.fieldname);
    for (RFWMOData d : lessThan)
      list.add(d.fieldname);
    for (RFWMOData d : lessThanOrEqualTo)
      list.add(d.fieldname);
    for (RFWMOData d : isNull)
      list.add(d.fieldname);
    for (RFWMOData d : isNotNull)
      list.add(d.fieldname);
    for (RFWMOData d : like)
      list.add(d.fieldname);
    for (RFWMOData d : in)
      list.add(d.fieldname);
    for (RFWMOData d : notIn)
      list.add(d.fieldname);
    for (RFWMO bMO : getSubmo())
      list.addAll(bMO.getAttributes());

    return list;
  }

  /**
   * Total de condições que este MO contém.
   *
   * @return Retorna o total de "condições" que este MO contêm. Incluindo as condições dos SubMOs.
   */
  public int size() {
    int size = 0;
    if (this.submo != null) for (RFWMO mo : this.submo) {
      size += mo.size();
    }
    return size + this.equal.size() + this.notEqual.size() + this.greaterThanOrEqualTo.size() + this.greaterThan.size() + this.lessThanOrEqualTo.size() + this.lessThan.size() + this.isNull.size() + this.isNotNull.size() + this.like.size() + this.in.size() + this.notIn.size();
  }

  /**
   * Permite criar um filtro que verifica se a data de hoje está entre dois campos de datas do VO.<br>
   * Fará uma consulta similar à: <b>periodStartField <= Hoje && (periodEndField >= hoje || periodEndField is null)</b>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campin com a data de fim do período.
   * @throws RFWException
   */
  public void periodHasNow(String periodStartField, String periodEndField) throws RFWException {
    final LocalDateTime now = RFW.getDateTime();
    periodHasFullPeriod(periodStartField, periodEndField, now, now);
  }

  /**
   * Filtra os registros cujo período contenham outro período completamente. Em outras palavras, o período passado deve estar completamente dentro de um período definido no registro (VO).<br>
   * Caso a data fim do período do registro seja nula, é considerada como uma "data infinita" (um valor muito grande), fazendo com que nulo seja maior que qualquer data.<br>
   * Fará uma consulta similar à: <b>periodStartField <= periodStart && (periodEndField >= periodEnd || periodEndField is null)</b>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campo com a data de fim do período.
   * @param periodStart Data de início do período. Inclusivo.
   * @param periodEnd Data de fim do período. Inclusivo.
   * @throws RFWException
   */
  public void periodHasFullPeriod(String periodStartField, String periodEndField, LocalDate periodStart, LocalDate periodEnd) throws RFWException {
    periodHasFullPeriod(periodStartField, periodEndField, periodStart.atStartOfDay(), periodEnd.atStartOfDay());
  }

  /**
   * Filtra os registros cujo período contenham outro período completamente. Em outras palavras, o período passado deve estar completamente dentro de um período definido no registro (VO).<br>
   * Caso a data fim do período do registro seja nula, é considerada como uma "data infinita" (um valor muito grande), fazendo com que nulo seja maior que qualquer data.<br>
   * Fará uma consulta similar à: <b>periodStartField <= date && (periodEndField >= date || periodEndField is null)</b>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campin com a data de fim do período.
   * @param date Data que deve estar entre as datas.
   * @throws RFWException
   */
  public void periodHasDate(String periodStartField, String periodEndField, LocalDate date) throws RFWException {
    periodHasFullPeriod(periodStartField, periodEndField, date.atStartOfDay(), date.atStartOfDay());
  }

  /**
   * Filtra os registros cujo período contenham outro período completamente. Em outras palavras, o período passado deve estar completamente dentro de um período definido no registro (VO).<br>
   * Caso a data fim do período do registro seja nula, é considerada como uma "data infinita" (um valor muito grande), fazendo com que nulo seja maior que qualquer data.<br>
   * Fará uma consulta similar à: <b>periodStartField <= periodStart && (periodEndField >= periodEnd || periodEndField is null)</b> Fará uma consulta similar à: <b>periodStartField <= dateTime && (periodEndField >= dateTime || periodEndField is null)</b>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campin com a data de fim do período.
   * @param dateTime DateTime que deve estar entre as datas.
   * @throws RFWException
   */
  public void periodHasDate(String periodStartField, String periodEndField, LocalDateTime dateTime) throws RFWException {
    periodHasFullPeriod(periodStartField, periodEndField, dateTime, dateTime);
  }

  /**
   * Filtra os registros cujo período contenham outro período completamente. Em outras palavras, o período passado deve estar completamente dentro de um período definido no registro (VO).<br>
   * Caso a data fim do período do registro seja nula, é considerada como uma "data infinita" (um valor muito grande), fazendo com que nulo seja maior que qualquer data.<br>
   * Fará uma consulta similar à: <b>periodStartField <= periodStart && (periodEndField >= periodEnd || periodEndField is null)</b> <br>
   * <b>Observação:</b> por escrever duas condições que obrigatoriamente precisam ser consideradas com o operador {@link APPENDMETHOD#AND}, caso este MO esteja definido para outro {@link #appendmethod} que não {@link APPENDMETHOD#AND} será automaticamente criado um novo {@link RFWMO} e colocado como um SubMO.<br>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campo com a data de fim do período.
   * @param periodStart Data de início do período. Inclusivo.
   * @param periodEnd Data de fim do período. Inclusivo.
   * @throws RFWException
   */
  public void periodHasFullPeriod(String periodStartField, String periodEndField, LocalDateTime periodStart, LocalDateTime periodEnd) throws RFWException {
    PreProcess.requiredNonNull(periodStartField);
    PreProcess.requiredNonNull(periodEndField);
    PreProcess.requiredNonNull(periodEnd);
    PreProcess.requiredNonNull(periodStart);
    if (periodEnd.compareTo(periodStart) < 0) throw new RFWValidationException("Não há um período válido entre " + periodStart + " e " + periodEnd + "!");

    RFWMO mo = this;
    if (mo.getAppendmethod() != APPENDMETHOD.AND) { // Observação 1
      mo = new RFWMO(APPENDMETHOD.AND);
      this.getSubmo().add(mo);
    }

    mo.lessThanOrEqualTo(periodStartField, periodStart); // Filtra os registros que tenham iniciado antes do período
    RFWMO mo2 = new RFWMO(APPENDMETHOD.OR);
    mo2.isNull(periodEndField); // Filtra os objetos cuja da de fim não esteja definido ("válido para sempre")
    mo2.greaterThanOrEqualTo(periodEndField, periodEnd); // Ou que o período de término da vigência só tenha ocorrido depois que o nosso período tenha começado
    mo.getSubmo().add(mo2);
  }

  /**
   * Filtra os registros cujo período interseccione outro período. Em outras palavras, a intersecção de um período com outro deve ser maior que zero.<br>
   * Caso a data fim do período do registro seja nula, é considerada como uma "data infinita" (um valor muito grande), fazendo com que nulo seja maior que qualquer data.<br>
   * Fará uma consulta similar à: <b>periodStartField <= periodEnd && (periodEndField >= periodStart || periodEndField is null)</b>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campo com a data de fim do período.
   * @param periodStart Data de início do período. Inclusivo.
   * @param periodEnd Data de fim do período. Inclusivo.
   * @throws RFWException
   */
  public void periodIntersectsAnotherPeriod(String periodStartField, String periodEndField, LocalDate periodStart, LocalDate periodEnd) throws RFWException {
    periodIntersectsAnotherPeriod(periodStartField, periodEndField, periodStart.atStartOfDay(), periodEnd.atStartOfDay());
  }

  /**
   * Filtra os registros cujo período interseccione outro período. Em outras palavras, a intersecção de um período com outro deve ser maior que zero.<br>
   * Caso a data fim do período do registro seja nula, é considerada como uma "data infinita" (um valor muito grande), fazendo com que nulo seja maior que qualquer data.<br>
   * Fará uma consulta similar à: <b>periodStartField <= periodEnd && (periodEndField >= periodStart || periodEndField is null)</b> <br>
   * <b>Observação:</b> por escrever duas condições que obrigatoriamente precisam ser consideradas com o operador {@link APPENDMETHOD#AND}, caso este MO esteja definido para outro {@link #appendmethod} que não {@link APPENDMETHOD#AND} será automaticamente criado um novo {@link RFWMO} e colocado como um SubMO.<br>
   *
   * @param periodStartField Campo com a data de início do período.
   * @param periodEndField Campo com a data de fim do período.
   * @param periodStart Data de início do período. Inclusivo.
   * @param periodEnd Data de fim do período. Inclusivo.
   * @throws RFWException
   */
  public void periodIntersectsAnotherPeriod(String periodStartField, String periodEndField, LocalDateTime periodStart, LocalDateTime periodEnd) throws RFWException {
    PreProcess.requiredNonNull(periodStartField);
    PreProcess.requiredNonNull(periodEndField);
    PreProcess.requiredNonNull(periodEnd);
    PreProcess.requiredNonNull(periodStart);
    if (periodEnd.compareTo(periodStart) < 0) throw new RFWValidationException("Não há um período válido entre " + periodStart + " e " + periodEnd + "!");

    RFWMO mo = this;
    if (mo.getAppendmethod() != APPENDMETHOD.AND) { // Observação 1
      mo = new RFWMO(APPENDMETHOD.AND);
      this.getSubmo().add(mo);
    }

    mo.lessThanOrEqualTo(periodStartField, periodEnd);
    RFWMO mo2 = new RFWMO(APPENDMETHOD.OR);
    mo2.isNull(periodEndField); // Filtra os objetos cuja da de fim não esteja definido ("válido para sempre")
    mo2.greaterThanOrEqualTo(periodEndField, periodStart); // Ou que o período de término da vigência só tenha ocorrido depois que o nosso período tenha começado
    mo.getSubmo().add(mo2);
  }

  /**
   * Filetra os registros que tenham uma data dentro de um período específico.
   *
   * @param dateField Campo do VO que contém a data.
   * @param periodStart Início do Período. InclusivoS.
   * @param periodEnd Fim do Período. Inclusivo.
   * @throws RFWException
   */
  public void dateInPeriod(String dateField, LocalDate periodStart, LocalDate periodEnd) throws RFWException {
    dateInPeriod(dateField, periodStart.atStartOfDay(), periodEnd.atStartOfDay());
  }

  /**
   * Filetra os registros que tenham uma data dentro de um período específico.
   *
   * @param dateField Campo do VO que contém a data.
   * @param periodStart Início do Período. InclusivoS.
   * @param periodEnd Fim do Período. Inclusivo.
   * @throws RFWException
   */
  public void dateInPeriod(String dateField, LocalDateTime periodStart, LocalDateTime periodEnd) throws RFWException {
    PreProcess.requiredNonNull(periodEnd);
    PreProcess.requiredNonNull(periodStart);
    if (periodEnd.compareTo(periodStart) < 0) throw new RFWValidationException("Não há um período válido entre " + periodStart + " e " + periodEnd + "!");

    this.greaterThanOrEqualTo(dateField, periodStart);
    this.lessThanOrEqualTo(dateField, periodEnd);
  }

}