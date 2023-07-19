package br.eng.rodrigogml.rfw.base.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.eng.rodrigogml.rfw.base.dao.RFWDAO;
import br.eng.rodrigogml.rfw.base.sessionmanager.interceptors.SMInterceptor;
import br.eng.rodrigogml.rfw.base.utils.BUReflex;
import br.eng.rodrigogml.rfw.base.utils.RFWRecursiveClone;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe pai de todos os Value Objects do sistema. Permitindo assim que métodos consigam operar genericamente em muitos casos.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.0.0 (SET / 2009)
 */
public abstract class RFWVO implements RFWRecursiveClonable, Serializable, Cloneable {

  private static final long serialVersionUID = 3213685744810281101L;

  /**
   * Este atributo indica se o objeto foi totalmente do banco de dados (com o método {@link RFWDAO#findForFullUpdate}). Permitindo que o objeto seja atualizado com o método {@link RFWDAO#update(RFWVO)}.<br>
   * Caso este atributo esteja como false o método lançarpa uma exception avisando que o objeto usado não está completo para edição. Evitando assim que um update de um objeto errado faça alterações irreversíveis aos dados do sistema.<br>
   * <b>ATENÇÃO: Não altere o valor deste atributo manualmente, ele é gerenciado pelo {@link RFWDAO}.</b>
   *
   * @deprecated NÃO DEVE SER UTILIZADO EM <B><U>NENHUM</U></B> PONTO DO SISTEMA PELO USU�?RIO! EM HIPÓTESE ALGUMA FAZER O OVERRIDE! ESTE ATRIBUTO DEVE SER UTILIZADO APENAS NO {@link RFWDAO}
   */
  @Deprecated
  private boolean _fullLoaded = false; // Começa com _ para ficar no topo quando inspecionamos o VO no debug e não misturar com os outros atributos

  /**
   * Este atributo permite que um VO seja inserido no banco de dados forçando a definição do ID do objeto.<br>
   * Esta opção só tem a finalidade de permitir a migração de dados de outros sitemas, permitindo que os objetos utilizem os IDs de identificação já utilizados. Também só é recomendado o uso em uma base de dados "limpa" para evitar conflitos de IDs com objetos que já existam no banco. <Br>
   * <Br>
   * Note que alguns dialetos de bando de dados, como o DerbyDB, nunca inserem o atributo ID no statement de insert uma vez que ele seja gerado automaticamente. Para esses casos esse atributo não fará nenhuma diferença exceto nas validações.<br>
   * <b>Atenção:</b> Quando um objeto tem ID, mas tem essa flag definida como true, indica que o objeto não está persistido no banco de dados. Caso o objeto seja inserido, esta flag deve ser definida como false pelo {@link RFWDAO}.
   *
   * @deprecated NÃO DEVE SER UTILIZADO PELO SISTEMA. Este recurso é útil em casos muito específicos, como migração de dados, nunca para as rotinas do sistema.
   */
  @Deprecated
  private boolean _insertWithID = false; // Começa com _ para ficar no topo quando inspecionamos o VO no debug e não misturar com os outros atributos

  public RFWVO() {
  }

  public RFWVO(Long id) {
    this.id = id;
  }

  /**
   * ID do Objeto. Usado sempre como PK no banco de dados.
   */
  private Long id = null;

  /**
   * # iD do Objeto. Usado sempre como PK no banco de dados.
   *
   * @return the iD do Objeto
   */
  public Long getId() {
    return id;
  }

  /**
   * # iD do Objeto. Usado sempre como PK no banco de dados.
   *
   * @param id the new iD do Objeto
   */
  public void setId(Long id) {
    this.id = id;
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
   * Este método não deve ser sobrescrito, e serve apenas para ser evocado quando no início de um output dos VOs.<br>
   * Caso já tenha iniciado, usar o método <code>{@link #printMySelf(StringBuilder, List)}</code>
   *
   * @return String com o conteúdo do objeto.
   */
  public final String printMySelf() {
    StringBuilder buff = new StringBuilder();
    printMySelf(buff, new ArrayList<>());
    return buff.toString();
  }

  /**
   * Este método pode (e deve em muitos casos) ser sobrescrito pelas classes filhas para otimizar o "output" do objeto para efeitos de Debug.<br>
   * VOs que devem sobrescrever este método são basicamente os métodos que tem atributos como List ou Map, cujo print padrão não consegue descobrir as classes genéricas implementadas e imprimir o objeto padrão. Para simplificar o output dos objetos cheque os métodos usados no print padrão e não use seus próprios métodos para não haver diferença de padrões de saída.
   *
   * @param buff
   * @param printedobjects
   */
  public void printMySelf(StringBuilder buff, List<Object> printedobjects) {
    BUReflex.printObject(this, getClass(), buff, printedobjects);
  }

  /**
   * Método responsável por clonar o RFWVO inteiro evitando que suas referências sejam alteradas.<br>
   * Usado, por exemplo, nas fachadas {@link SMInterceptor} para evitar que o objeto seja alterado no CRUD e fique diferente caso Facade e Invoker estejam na mesma JVM.
   *
   * @return Objeto clonado recursivamente.
   * @throws RFWException
   */
  @Override
  public RFWVO cloneRecursive() throws RFWException {
    return cloneRecursive(new HashMap<RFWRecursiveClonable, RFWRecursiveClonable>());
  }

  @Override
  public RFWVO cloneRecursive(HashMap<RFWRecursiveClonable, RFWRecursiveClonable> clonedobjects) throws RFWException {
    return (RFWVO) RFWRecursiveClone.cloneRecursive(this, clonedobjects);
  }

  /**
   * Este atributo indica se o objeto foi totalmente do banco de dados (com o método {@link RFWDAO#findForUpdate(Long, String[])}). Permitindo que o objeto seja atualizado com o método {@link RFWDAO#persist(RFWVO)}.<br>
   * Caso este atributo esteja como false o método lançarpa uma exception avisando que o objeto usado não está completo para edição. Evitando assim que um update de um objeto errado faça alterações irreversíveis aos dados do sistema.<br>
   * <b>ATENÇÃO: Não altere o valor deste atributo manualmente, ele é gerenciado pelo {@link RFWDAO}.</b>
   *
   * @deprecated NÃO DEVE SER UTILIZADO EM <B><U>NENHUM</U></B> PONTO DO SISTEMA PELO USU�?RIO! EM HIPÓTESE ALGUMA FAZER O OVERRIDE! ESTE ATRIBUTO DEVE SER UTILIZADO APENAS NO {@link RFWDAO}
   * @return true se o objeto foi completamente carregado no banco de dados, false caso contrário.
   */
  @Deprecated
  public boolean isFullLoaded() {
    return _fullLoaded;
  }

  /**
   * Este atributo indica se o objeto foi totalmente do banco de dados (com o método {@link RFWDAO#findForUpdate(Long, String[])}). Permitindo que o objeto seja atualizado com o método {@link RFWDAO#persist(RFWVO)}.<br>
   * Caso este atributo esteja como false o método lançarpa uma exception avisando que o objeto usado não está completo para edição. Evitando assim que um update de um objeto errado faça alterações irreversíveis aos dados do sistema.<br>
   * <b>ATENÇÃO: Não altere o valor deste atributo manualmente, ele é gerenciado pelo {@link RFWDAO}.</b>
   *
   * @deprecated NÃO DEVE SER UTILIZADO EM <B><U>NENHUM</U></B> PONTO DO SISTEMA PELO USU�?RIO! EM HIPÓTESE ALGUMA FAZER O OVERRIDE! ESTE ATRIBUTO DEVE SER UTILIZADO APENAS NO {@link RFWDAO}
   * @param fullLoaded indicador se o objeto foi inteiramente carregado no banco de dados.
   */
  @Deprecated
  public void setFullLoaded(boolean fullLoaded) {
    this._fullLoaded = fullLoaded;
  }

  /**
   * Implementação padrão do HashCode. Pode ser substituida em cada VO
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /**
   * NÃO MUDE A IMPLEMENTAÇÃO DO EQUALS!!!!<br>
   * <br>
   *
   * O método de equals deve utilizar o equals padrão da classe Object por várias questões do framework.<br>
   * Uma das necessidades é que o {@link RFWDAO} utiliza um cache para saber se já persistiu determinado objeto (quando está investigando cadeias circulares de referência ds objetos) e não ficar em loop persistindo o mesmo objeto.<br>
   * <br>
   * Uma das necessidades de implementar um equals diferente (que comparasse IDs ao invés do objeto na memória), era necessária para o funcionamento do TreeGrid do Vaadin, que utiliza o equals para comparar os Lazy Objects na hora de expandir e reorganizar.<br>
   * <br>
   * Para resolver esse e outros problemas que dependam de um equals baseado no ID e não na instância do objeto, verifique a solução dada pelo {@link GVO}.
   *
   */
  @Override
  public final boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * Este atributo permite que um VO seja inserido no banco de dados forçando a definição do ID do objeto.<br>
   * Esta opção só tem a finalidade de permitir a migração de dados de outros sitemas, permitindo que os objetos utilizem os IDs de identificação já utilizados. Também só é recomendado o uso em uma base de dados "limpa" para evitar conflitos de IDs com objetos que já existam no banco. <Br>
   * <Br>
   * Note que alguns dialetos de bando de dados, como o DerbyDB, nunca inserem o atributo ID no statement de insert uma vez que ele seja gerado automaticamente. Para esses casos esse atributo não fará nenhuma diferença exceto nas validações.<br>
   * <b>Atenção:</b> Quando um objeto tem ID, mas tem essa flag definida como true, indica que o objeto não está persistido no banco de dados. Caso o objeto seja inserido, esta flag deve ser definida como false pelo {@link RFWDAO}.
   *
   * @deprecated NÃO DEVE SER UTILIZADO PELO SISTEMA. Este recurso é útil em casos muito específicos, como migração de dados, nunca para as rotinas do sistema.
   * @return se o objeto é uma inserção com ID.
   */
  @Deprecated
  public boolean isInsertWithID() {
    return _insertWithID;
  }

  /**
   * Este atributo permite que um VO seja inserido no banco de dados forçando a definição do ID do objeto.<br>
   * Esta opção só tem a finalidade de permitir a migração de dados de outros sitemas, permitindo que os objetos utilizem os IDs de identificação já utilizados. Também só é recomendado o uso em uma base de dados "limpa" para evitar conflitos de IDs com objetos que já existam no banco. <Br>
   * <Br>
   * Note que alguns dialetos de bando de dados, como o DerbyDB, nunca inserem o atributo ID no statement de insert uma vez que ele seja gerado automaticamente. Para esses casos esse atributo não fará nenhuma diferença exceto nas validações.<br>
   * <b>Atenção:</b> Quando um objeto tem ID, mas tem essa flag definida como true, indica que o objeto não está persistido no banco de dados. Caso o objeto seja inserido, esta flag deve ser definida como false pelo {@link RFWDAO}.
   *
   * @deprecated NÃO DEVE SER UTILIZADO PELO SISTEMA. Este recurso é útil em casos muito específicos, como migração de dados, nunca para as rotinas do sistema.
   * @param insertWithID define se este objeto deve ser inserido com um ID predefinido.
   */
  @Deprecated
  public void setInsertWithID(boolean insertWithID) {
    this._insertWithID = insertWithID;
  }

}
