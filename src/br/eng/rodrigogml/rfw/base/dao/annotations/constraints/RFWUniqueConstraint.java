package br.eng.rodrigogml.rfw.base.dao.annotations.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotation usada para definir constraints de Unicidade entre atributos de um VO.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (16/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RFWUniqueConstraint {

  /**
   * Define o conjunto de campos que tevem ter valor único.
   */
  String[] fields();

}
