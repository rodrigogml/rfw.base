package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.kernel.vo.RFWVO;

/**
 * Description: Meta Annotation para indica dependência com outras classes.<br>
 * Ao anotar esta classe como dependente de outra, indicará que as outras entidades não poderão ser excluídas enquanto esta à estiver usando. Note que além desta indicação, é necessário que na outra entidade seja anotada a {@link RFWUsedBy} pra garantir a validação da dependência quando ele for manipulado.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (16/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RFWContainerDependency.class)
public @interface RFWDependency {

  /**
   * Define a classe da entidade da qual esta classe depende.
   */
  Class<? extends RFWVO> voClass();

  /**
   * Nome do atributo da classe que mantém o relacionamento/uso desta esta classe.
   */
  String attribute();

}
