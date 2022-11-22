package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.base.vo.RFWVO;

/**
 * Description: Meta Annotation para indicar quando uma classe é utilizada por outra de forma dependente.<br>
 * Ao indicar que uma classe é utilizada por outra, a outra classe deverá conter uma {@link RFWDependency} para indiciar a forma da dependencia com esta classe.<br>
 * Esta indicação só deve serfeita nas seguitnes condições:
 * <ul>
 * <li>Caso este objeto não possa ser excluído quando algum objeto que depende deste impeça a exclusão, como ForeignKeys no banco sem</Li>
 * </ul>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (16/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RFWContainerUsedBy.class)
public @interface RFWUsedBy {

  /**
   * Define a classe da entidade que utiliza esta classe.
   */
  Class<? extends RFWVO> voClass();

}
