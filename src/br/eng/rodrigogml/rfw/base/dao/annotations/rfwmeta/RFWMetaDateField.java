package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess.PreProcessOption;

/**
 * Description: Annotation usada para definir um atributo do tipo Date.<BR>
 * ATENÇÃO: Essa tag automaticamente define que a resulução é apenas de DIA. Para uma resolução utilize a RFWMetaDateTimeField (Não acriada ainda até o momento de escrita desta observação).
 *
 * @author Davy Monteoliva
 * @since 7.1.0 (20/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaDateField {

  public static enum DateResolution {
    MILLISECONDS, SECOND, MINUTE, HOUR, DAY, MONTH, YEAR;
  }

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
   * Define a data máxima. <br>
   * Formato: yyyyMMddHHmmssSSSS<br>
   * <li>yyyy = ano com quatro dígitos
   * <li>MM = mês com dois dígitos
   * <li>dd = dia com dois dígitos
   * <li>HH = hora com dois dígitos
   * <li>mm = minuto com dois dígitos
   * <li>ss = segundo com dois dígitos
   * <li>SSSS = milisegundo com quatro dígitos
   */
  String maxValue() default "";

  /**
   * Define a data mínima. <br>
   * Formato: yyyyMMddHHmmssSSSS<br>
   * <li>yyyy = ano com quatro dígitos
   * <li>MM = mês com dois dígitos
   * <li>dd = dia com dois dígitos
   * <li>HH = hora com dois dígitos
   * <li>mm = minuto com dois dígitos
   * <li>ss = segundo com dois dígitos
   * <li>SSSS = milisegundo com quatro dígitos
   */
  String minValue() default "";

  /**
   * Define a resolução do campo Date.
   */
  DateResolution resolution() default DateResolution.DAY;

  /**
   * Define um Pré Processamento a ser feito no atributo antes das persistências do objeto.
   */
  PreProcessOption preProcess() default PreProcessOption.NONE;

}
