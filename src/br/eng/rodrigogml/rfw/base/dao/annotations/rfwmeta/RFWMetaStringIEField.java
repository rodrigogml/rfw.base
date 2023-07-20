package br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess.PreProcessOption;

/**
 * Description: Annotation usada para definir um atributo do tipo String que funcionará como um IE.<BR>
 * O campo IE será uma String apenas com os dígitos, sem nenhum tipo de formatação.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (04/07/2015)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWMetaStringIEField {

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
   * Define a UF da IE. Se deixar como uma String vazia indica que pode ser uma IE de qualquer estado.
   */
  String uf() default "";

  /**
   * Define o atributo do mesmo VO que contém a UF desta IE. O atributo pode ser indicado de forma encadeada com separação de pontos.<br>
   * O Atributo apontado, se for uma String, deve conter a UF com duas letras.
   */
  String uffield() default "";

  /**
   * Define o padrão de pré-processamento a ser aplicado na String antes da validação.<br>
   * Note que o preprocessamento é aplicado na ordem que for definido!
   */
  PreProcessOption[] preProcess() default { PreProcessOption.NONE };
}
