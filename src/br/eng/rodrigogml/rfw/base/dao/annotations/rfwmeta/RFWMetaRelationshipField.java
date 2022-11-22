package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;

import br.eng.rodrigogml.rfw.base.vo.RFWVO;

/**
 * Description: Annotation usada para definir um atributo de associação com outro VO que extenda RFWVO.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (04/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaRelationshipField {

  public static enum RelationshipTypes {
    /**
     * Associação indica que os dois objetos no relacionamento podem se "ter um link" entre eles, mas que ambos existem independente do relacionamento. Ex: Pessoas e Revistas, associadas por um link de assinantes/assinaturas.<br>
     * <b>Notas do RFWValidator:</b>
     * <ul>
     * <li>Este relacionamento obrigará que o filho já tenha um ID definido. Afinal o relacionamento é entre dois objetos distintos sem dependência de existência entre eles.</li>
     * </ul>
     */
    ASSOCIATION,
    /**
     * Estabelece exatamente o mesmo relacionamento que {@link #ASSOCIATION}. No entanto, quando é ASSOCIATION o RFWDAO ao persistir verifica as associações que sumiram ou novas criadas para persistir.<br>
     * Nesta WEAK_ASSOCIATION, indica que não vamos atualiar as associações entre os objetos a partir deste objeto. (Normalmente só através da contra-parte).<br>
     * Este mapemanto é utilizad por exemplo quando tempos um Objeto principal, e outros secunrádios que se ligam a este. Mas olhando da perspectiva do objeto principal as associações são indiferentes e não é interessante altera-las ou corrigi-las a partir do objeto principal.<br>
     * Então fica a pergunta, e pra que fazer esta associação? Pq não deixar só no objeto secundário? A associação a partir do objeto principal pode ser útil para filtrar dados, pois assim estabelece uma relação entre este objeto e o outro. Ou mesmo montagem de relatório.<br>
     * <br>
     * Note então que este relacionamento não será retornado pelo "findForUpdate()" e será ignorado pelos métodos de persist().
     */
    WEAK_ASSOCIATION,
    /**
     * Composição indica que os dois objetos se relacionam de maneira que um objeto "completa" o outro, isto é, um dos objetos só existe para suprir dados ou informações de outro objetos. Neste tipo de relacionamento, o objeto do atributo não existe caso o pai deixe de existir. Consequêntemente um objeto filho não pode ser "reutilizado" por outro objeto pai.<Br>
     * <b>Notas do RFWValidator:</b>
     * <ul>
     * <li>Este relacionamento aceitará que o objeto filho só tenha ID definido caso o objeto pai já tenha um ID definido. Caso o pai seja um objeto novo (para inserção) o objeto filho deverá ser um objeto novo também.</li>
     * </ul>
     */
    COMPOSITION,
    /**
     * Similiar à {@value #COMPOSITION}, este atributo indica uma composição de um objeto para outro. A única diferença que a composição é com o próprio objeto.<br>
     * Esse tipo de composição indica que um objeto tem outros do mesmo tipo dentro dele mesmo. Como em uma organização hierarquica (forma de árvore).<br>
     * Esse tipo de composição precisa de tratamento especial pois pode ter "infitas" composições de objeto.
     */
    COMPOSITION_TREE,
    /**
     * Associação do Pai indica que o objeto associado no relacionamento é o "pai" deste relacionamento. Isto é, que este objeto é parte da composição do objeto pai. Obrigatóriamente o objeto relacionado aqui deve ter uma referência à este objeto do tipo {@link RelationshipTypes#COMPOSITION}.<br>
     * <b>Notas do RFWValidator:</b>
     * <ul>
     * <li>Este relacionamento será validado exatamente como uma ASSOCIATION.</li>
     * </ul>
     * <b>Notas do RFWDAO:</b>
     * <ul>
     * <li>Neste tipo de relacionamento o objeto será carregado automaticamente durante o método findForFullUpdate, para que seja possível detectar a ausencia de algum objeto e fazer a exclusão no banco de dados.</li>
     * </ul>
     */
    PARENT_ASSOCIATION,
    /**
     * Similar ao PARENT_ASSOCIATION, este relacionamento permite que um objeto referencie outro que será adicionado/editado junto com o objeto principal, mas não necessariamente é o objeto pai deste objeto.<br>
     * Objetos com esse tipo de relacionamento podem ter que ser inseridos em duas etapas. Por exemplo, se o objeto associado ainda não tiver um ID, vamos inserir este objeto no banco, e posteriormente fazer um update para completar o ID da associação.<br>
     * ISSO QUER DIZER QUE, a não ser que o objeto associado esteja hierarquicamente acima deste, a coluna no banco de dados não pode estar definida como Not Null ou a primeira atapalha falhará.<br>
     * Além disso, a FK deve ser sempre ON DELETE CASCADE ou ON DELETE SET NULL, para que exclusão do objeto associado não seja barrada pelo banco de dados.
     */
    INNER_ASSOCIATION,
    /**
     * Uma associação "Muitos para Muitos" ou "N:N" é sempre do tipo associação, isto é, os objetos existem independentemente e é criado apenas uma associação entre eles.<br>
     * <b>Notas do RFWValidator:</b>
     * <ul>
     * <li>Este relacionamento obrigará que os filhos já tenham ID definido. Afinal o relacionamento é entre dois objetos distintos sem dependência de existência entre eles.</li>
     * </ul>
     * <b>Notas do RFWDAO:</b>
     * <ul>
     * <li>Neste tipo de relacionamento os objetos serão carregados automaticamente durante o método findForFullUpdate, para que seja possível detectar a ausencia de algum objeto e fazer a exclusão no banco de dados.</li>
     * </ul>
     */
    MANY_TO_MANY,
  }

  /**
   * Nome da tabela de "join" para os relacionamentos de Many_To_Many.
   *
   * @return
   */
  String joinTable() default "";

  /**
   * Coluna do ID do objeto relacionado. Deve ser preenchido caso a FK fique na tabela deste objeto.<br>
   * Se {@link RelationshipTypes#MANY_TO_MANY}: deve ser informado a coluna da tabela de join em que está o ID deste objeto.<br>
   * Se {@link RelationshipTypes#PARENT_ASSOCIATION}: obrigatório com o nome da coluna com a FK da tabela do objeto pai.<br>
   * Se {@link RelationshipTypes#ASSOCIATION} : preenchido somente se a coluna estiver na tabela deste objeto. Caso contrário, veja {@link #columnMapped()}.<br>
   * Se {@link RelationshipTypes#COMPOSITION} : nunca utilizado, já que é sempre o objeto filho quem carrega o ID deste objeto.<br>
   * Se {@link RelationshipTypes#COMPOSITION_TREE} : nunca utilizado, já que é sempre o objeto filho quem carrega o ID deste objeto.<br>
   * Se {@link RelationshipTypes#INNER_ASSOCIATION} : preenchido somente se a coluna estiver na tabela deste objeto. Caso contrário, veja {@link #columnMapped()}.<br>
   * Se {@link RelationshipTypes#WEAK_ASSOCIATION} : preenchido somente se a coluna estiver na tabela deste objeto. Caso contrário, veja {@link #columnMapped()}.<br>
   *
   */
  String column() default "";

  /**
   * Coluna com a FK quando na tabela da contra-parte do relacionamento.<br>
   * Se {@link RelationshipTypes#MANY_TO_MANY}: Nome da coluna na tabela de join, com o ID do "outro" objeto no relacionamento.<Br>
   * Se {@link RelationshipTypes#PARENT_ASSOCIATION} : Nunca utilizado já que o coluna com o ID do pai deve estar na nossa tabela e definido em {@link #column()}<br>
   * Se {@link RelationshipTypes#ASSOCIATION} : Nome da coluna, quando a FK está na tabela do outro objeto.<br>
   * Se {@link RelationshipTypes#COMPOSITION} : Indica o nome da coluna da tabela filha que contém o nosso ID para associação.<br>
   * Se {@link RelationshipTypes#COMPOSITION_TREE} : Indica o nome da coluna da tabela filha que contém o nosso ID para associação.<br>
   * Se {@link RelationshipTypes#INNER_ASSOCIATION} : Nome da coluna, quando a FK está na tabela do outro objeto.<br>
   * Se {@link RelationshipTypes#WEAK_ASSOCIATION} : Nome da coluna, quando a FK está na tabela do outro objeto.<br>
   *
   * @return valor definido.
   */
  String columnMapped() default "";

  /**
   * Define o nome do atributo/campo. Este nome é usado para facilitar mensagens de erros, validações, em UIs, etc.<br>
   * Não utilize ":" no final ou outras formatações específicas do local de uso. Aqui deve ser definido apenas o nome, como "Caixa", "Nome do Usuário", etc.
   *
   * @return valor definido.
   */
  String caption();

  /**
   * Define se o atributo é obrigatório ou não na entidade.
   *
   * @return valor definido.
   */
  boolean required();

  /**
   * Define se o atributo é único.
   *
   * @return valor definido.
   */
  boolean unique() default false;

  /**
   * Define se o objeto associado já deve existir no banco de dados. Quando true, indica que o objeto associado neste atributo já existe no banco de dados antes da eixstência deste objeto. Em outras palavras, caso true, o VO associado deve ter um ID definido.
   *
   * @return valor definido.
   */
  RelationshipTypes relationship();

  /**
   * Define a classe alvo do relacionamento. Ao indicar a classe neste atributo qualquer objeto associado ao atributo deverá ser igual ou herdeirada classe indicada.<br>
   * <ul>
   * <li>RFWValidator: Utiliza este argumento para validar os objetos quando em uma {@link List} ou {@link HashMap}.</li>
   * </ul>
   *
   * @return valor definido.
   */
  Class<? extends RFWVO> targetRelationship() default RFWVO.class;

  /**
   * Para relacionamentos dentro de uma {@link List} ou {@link HashMap} este atributo pode ser usado para validar a quantidade mínima que a coleção deve conter.<br>
   * Lembrando que o atributo {@link #required()} simplesmente valida se não é nulo, não se a coleção está vazia.
   *
   * @return valor definido.
   */
  int minSize() default -1;

  /**
   * Define o nome do atributo do objeto destino que é utilizado para <b>colocar o objeto dentro da Hash</b>. Este atributo só é utilizado quando a coleção do mapeamento é uma Hash.<br>
   * ATENÇÃO, NÃO SUPORTA PROPRIEDADE ANINHADAS!!! O sistema falha se tentar utilizar propriedades aninhadas para a chave da Map, isso pq os sub-objetos ainda não terão sido montados quando precisamos colocar o objeto na Hash.
   *
   * @return valor definido.
   */
  String keyMap() default "";

  /**
   * Em caso de mapeamento em uma lista, permite informar o nome da coluna que contém o índice de ordem do objeto. Neste atributo será persistido o index da ordem que estava na lista, e na recuperação do objeto será montado na mesma ordem.<br>
   * <b>Atenção, esta coluna não deve ser um atributo no objeto filho!</b>
   *
   * @return valor definido.
   */
  String sortColumn() default "";
}
