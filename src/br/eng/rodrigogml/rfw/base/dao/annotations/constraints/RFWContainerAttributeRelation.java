package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Meta Annotation usada para estabelecer um conjundo de valida��es que s� s�o v�lidas baseadas com uma pr�condi��o de valores de outras vari�veis.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (17/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RFWContainerAttributeRelation {

  RFWAttributeRelation[] value();

}
