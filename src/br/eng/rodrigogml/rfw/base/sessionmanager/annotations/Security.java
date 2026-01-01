package br.eng.rodrigogml.rfw.base.sessionmanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotation usada para criar definições de segurança nos métodos da fachada..<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (25 de jul de 2018)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Security {

  static enum SecurityAction {
    /**
     * Define que a segurança deve ser "pulada", isto é, não verifica se tem sessão válida ou se tem a chave de acesso necessária.
     */
    SKIP,

    /**
     * Definição padrão. Faz apenas a verificação se temos uma sessão válida par permitir a chamada do método.<Br>
     * Funciona tanto recebendo uma UUID quanto um TOKEN válido.<br>
     * Exige que o primeiro parâmetro do método seja uma String contendo a UUID ou o Token.
     */
    HASSESSION,
    /**
     * Verifica se a sessão passada tem a permissão necessária para a chamada do método.<br>
     * Funciona tanto recebendo uma UUID quanto um TOKEN válido.<br>
     * Exige que o primeiro parâmetro do método seja uma String contendo a UUID ou o Token.
     */
    HASKEY,
    /**
     * Verifica se o solicitante tem uma sessão de estação.<br>
     * Funciona apenas com um TOKEN válido, rejeita sessões por UUID.<br>
     * Exige que o primeiro parâmetro do método seja uma String contendo o Token.
     */
    HASTOKENSESSION,
  }

  SecurityAction action() default SecurityAction.HASSESSION;

  /**
   * Usado para definir a chave de acesso quando o {@link #action()} é {@link SecurityAction#HASKEY}.<BR>
   * O usuário deve conter ao menos uma das chaves definidas para ser autorizado.
   */
  String[] key() default "";

  /**
   * Quando o acesso é por objeto, este atributo deve ser definido indicando o caminho até o atributo do ID usado na chave de permissão.
   */
  String idProperty() default "";

}
