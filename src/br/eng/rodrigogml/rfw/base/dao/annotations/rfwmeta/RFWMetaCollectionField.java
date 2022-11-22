package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.base.dao.interfaces.RFWDAOConverterInterface;

/**
 * Description: Esta annotation permite uma validação simples de objetos de coleção.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (18/03/2016)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaCollectionField {

  /**
   * Define o nome do atributo/campo. Este nome é usado para facilitar mensagens de erros, validações, em UIs, etc.<br>
   * Não utilize ":" no final ou outras formatações específicas do local de uso. Aqui deve ser definido apenas o nome, como "Caixa", "Nome do Usuário", etc.
   */
  String caption();

  /**
   * Define se o atributo é obrigatório ou não na entidade. Ser obrigatório basta ser diferente de null.
   */
  boolean required();

  /**
   * Define a quantidade mínima de elementos dentro da colection. Este atributo só é validado se o objeto for diferente de nulo. Neste caso combine com o {@link #required()}
   */
  int minSize() default -1;

  /**
   * Define a tabela que armazena a lista de itens da coleção.
   */
  String table();

  /**
   * Define o nome da coluna que armazena o valor desta field.
   *
   * @return
   */
  String column();

  /**
   * Define o nome da coluna da tabela onde será salvo o índice de ordenação dos itens da coleção.
   */
  String sortColumn() default "";

  /**
   * Define o nome da coluna onde será salvo a chave da hash, caso a colection esteja em uma Hash.
   */
  String keyColumn() default "";

  /**
   * Define a classe do {@link RFWDAOConverterInterface} que será utilizado.<Br>
   * Se não for uma classe do tipo {@link RFWDAOConverterInterface} é considerado como valor null do atributo
   */
  Class<?> keyConverterClass() default Object.class;

  /**
   * Define o tamanho máximo da chave do Map.<br>
   * <b>Este atrubuto é analizado apenas quando o tipo do atributo é um MAP e as chaves são do tipo String.</b>
   */
  int maxLenghtKey() default -1;

  /**
   * Define o tamanho máximo de caracteres dos valores da coleção.<br>
   * Valido para os tipos:
   * <li>String
   */
  int maxLenght() default -1;

  /**
   * Coluna onde será colocado o ID do objeto pai para criar o relacionamento.
   */
  String fkColumn();

  /**
   * Define se dentro da lista os itens podem se repetir. O valor padrão é 'true' (não permite valores repetidos).<br>
   * Para Hashs o valor acaba sendo true, pois as "Set" só permitem 1 item com a mesma chave.<br>
   * ATENÇÃO: ESTE ATRIBUTO NÃO ESTÁ SENDO VALIDADO PELO VALIDATOR AINDA!!!!
   */
  boolean uniqueValues() default true;

  /**
   * Define a classe alvo da Lista. Ao indicar a classe neste atributo os objetos serão convertidos para este objeto ao serem recuperados/persistidos na base de dados.<br>
   */
  Class<?> targetRelationship();
}
