package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotation usada para definir um atributo do tipo BigDecimal que é usado para definir um valor percentual.<BR>
 * Para utilizar essa anottation o campo deve ter o valor em porcentagem, não em decimal. Ex: 24,5% = new BigDecimal("24.5");
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (17/08/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaBigDecimalPercentageField {

  /**
   * Define o nome da coluna na tabela do banco de dados. Caso deixado em branco, será utilizado o nome da propriedade.
   */
  String column() default "";

  /**
   * Define o nome do atributo/campo. Este nome é usado para facilitar mensagens de erros, validações, em UIs, etc.<br>
   * Não utilize ":" no final ou outras formatações específicas do local de uso. Aqui deve ser definido apenas o nome, como "Caixa", "Nome do Usuário", etc.
   */
  String caption();

  /**
   * Define se o atributo é obrigatório ou não na entidade.
   */
  boolean required();

  /**
   * Define se o atributo é único.
   */
  boolean unique() default false;

  /**
   * Define o valor máximo aceito pelo atributo.<br>
   * Escrever o valor em String, no formato aceido pelo BigDecimal. Não usamos ponto flutuante (como Double ou Float) pela falta de precisão.
   */
  String maxValue() default "";

  /**
   * Define o valor mínimo aceito pelo atributo.<br>
   * Escrever o valor em String, no formato aceido pelo BigDecimal. Não usamos ponto flutuante (como Double ou Float) pela falta de precisão.
   */
  String minValue() default "";

  /**
   * Define o tamanho da escala/precisão que este BigDecimal deve ter.
   */
  int scale() default -1;

  /**
   * Define o tamanho máximo da escala/precisão que este BigDecimal pode ter.
   */
  int scaleMax() default -1;

  /**
   * Define se o valor deve sempre ser absoluto, isto é, se deve apenar ser positivo.
   */
  boolean absolute() default false;
}
