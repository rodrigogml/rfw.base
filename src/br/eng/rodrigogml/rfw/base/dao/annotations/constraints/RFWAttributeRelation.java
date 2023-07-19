package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: RFWDeprec Annotation usada para definir uma constraint de relação entre dois atributos da entidade.<BR>
 * <ul>
 * <li>As comparações de equal e notequal consideram iguais quando ambos são nulos, e diferente caso 1 seja nulo e outro não.</li>
 * <li>As comparações de grandeza (<, <=, > e >=) retornam true caso um dos atributos seja nulo. Pois não é possível compara-lo.</li>
 * </ul>
 * <br>
 * <b>A exceção será lançada caso a relação não seja válida, isto é, seja falsa!</b>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (17/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RFWContainerAttributeRelation.class)
public @interface RFWAttributeRelation {

  public static enum COMPAREOPERATION {
    EQUAL, NOTEQUAL, LESSTHAN, LESSOREQUALTHAN, MORETHAN, MOREOREQUALTHAN,
  }

  /**
   * Define o atributo da classe a ser comparado.
   */
  String attribute();

  /**
   * Segundo atributo com qual o primeiro será comparado.
   */
  String attribute2();

  /**
   * Operação de comparação a ser realizada entre os atributos definidos.
   */
  COMPAREOPERATION operation();

  /**
   * Código da exceção a ser lançada caso a operação de comparação não se satisfaça. Caso não seja informado um código específico a validação padrão será lançada.
   */
  String exceptioncode();

}
