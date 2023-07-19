package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.base.dataformatters.RFWPhoneDataFormatter;

/**
 * Description: Annotation usada para definir um atributo do tipo String que funcionará como um número de telefone no padrão RFWDeprec: "DDI|DDD|Phone".<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (04/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaStringPhoneField {

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
   * Indica se o atributo aceita apenas formátos conhecidos ou aceita qualquer string como telefone.<br>
   * Na prática faz com que o RFWValidator não falhe caso o método {@link RFWPhoneDataFormatter#getPhoneType(String)} retorne UNKNOW, e avisa aos desenvolvedores que o número do telefone está fora do formato do RFWDeprec, o que pode causar falhar em campos, relatórios, etc. ao ser utilizado.
   */
  boolean acceptUnknowFormats() default false;

}
