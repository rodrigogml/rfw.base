package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotation usada para definir um atributo do tipo BigDecimal.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (17/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaDoubleField {

  /**
   * Define o nome do atributo/campo. Este nome � usado para facilitar mensagens de erros, valida��es, em UIs, etc.<br>
   * N�o utilize ":" no final ou outras formata��es espec�ficas do local de uso. Aqui deve ser definido apenas o nome, como "Caixa", "Nome do Usu�rio", etc.
   */
  String caption();

  /**
   * Define se o atributo � obrigat�rio ou n�o na entidade.
   */
  boolean required();

  /**
   * Define se o atributo � �nico.
   */
  boolean unique() default false;

  /**
   * Valor m�ximo aceito pelo atributo.
   */
  double maxValue() default Double.MAX_VALUE;

  /**
   * Valor m�nimo aceito pelo atributo.
   */
  double minValue() default Double.MIN_VALUE;

}
