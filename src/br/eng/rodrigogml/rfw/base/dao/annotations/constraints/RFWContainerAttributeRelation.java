package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Meta Annotation usada para estabelecer um conjundo de validações que só são válidas baseadas com uma précondição de valores de outras variáveis.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (17/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RFWContainerAttributeRelation {

  RFWAttributeRelation[] value();

}
