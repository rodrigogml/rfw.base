package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.base.preprocess.PreProcess.PreProcessOption;

/**
 * Description: Annotation usada para definir um atributo do tipo String que funcionará como um Email.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (13/08/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaStringEmailField {

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
   * Define o padrão de pré-processamento a ser aplicado na String antes da validação.<br>
   * Note que o preprocessamento é aplicado na ordem que for definido!
   */
  PreProcessOption[] preProcess() default { PreProcessOption.NONE };

  /**
   * Define se o padrão do endereço de e-mail segue a especificação do RFC822.<br>
   * Caso false o valor deverá ser sempre "só" o endereço de e-mail. Ex: nome@dominio.com.br<Br>
   * Caso true o valor poderá ser "só" o endereço e-mail, como em caso de falso, mas também aceita o formato:<br>
   * <li>"Nome e Sobrenome" <nome@dominio.com.br>
   */
  boolean useRFC822() default false;

}
