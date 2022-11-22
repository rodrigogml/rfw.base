package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Permite o agrupamento de {@link RFWUsedBy}.<br>
 * Não precisa ser utilizada, o JDK 1.8+ criará essa classe automaticamente durante a compilação.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (16/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RFWContainerUsedBy {

  RFWUsedBy[] value();

}
