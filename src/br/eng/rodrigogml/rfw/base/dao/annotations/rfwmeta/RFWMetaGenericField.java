package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Esta annotation permite uma validação simples, orientada ao próprio 'Object', para campos anotados por interfaces muito genéricas que dificultam a validação por conta do polimorfismo.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (18/02/2016)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaGenericField {

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

}
