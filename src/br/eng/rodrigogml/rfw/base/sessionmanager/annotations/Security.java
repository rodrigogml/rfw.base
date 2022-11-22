package br.eng.rodrigogml.rfw.base.sessionmanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotation usada para criar defini��es de seguran�a nos m�todos da fachada..<br>
 *
 * @author Rodrigo Leit�o
 * @since 10.0.0 (25 de jul de 2018)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Security {

  static enum SecurityAction {
    /**
     * Define que a seguran�a deve ser "pulada", isto �, n�o verifica se tem sess�o v�lida ou se tem a chave de acesso necess�ria.
     */
    SKIP,

    /**
     * Defini��o padr�o. Faz apenas a verifica��o se temos uma sess�o v�lida par permitir a chamada do m�todo.<Br>
     * Funciona tanto recebendo uma UUID quanto um TOKEN v�lido.<br>
     * Exige que o primeiro par�metro do m�todo seja uma String contendo a UUID ou o Token.
     */
    HASSESSION,
    /**
     * Verifica se a sess�o passada tem a permiss�o necess�ria para a chamada do m�todo.<br>
     * Funciona tanto recebendo uma UUID quanto um TOKEN v�lido.<br>
     * Exige que o primeiro par�metro do m�todo seja uma String contendo a UUID ou o Token.
     */
    HASKEY,
    /**
     * Verifica se o solicitante tem uma sess�o de esta��o.<br>
     * Funciona apenas com um TOKEN v�lido, rejeita sess�es por UUID.<br>
     * Exige que o primeiro par�metro do m�todo seja uma String contendo o Token.
     */
    HASTOKENSESSION,
  }

  SecurityAction action() default SecurityAction.HASSESSION;

  /**
   * Usado para definir a chave de acesso quando o {@link #action()} � {@link SecurityAction#HASKEY}.<BR>
   * O usu�rio deve conter ao menos uma das chaves definidas para ser autorizado.
   */
  String[] key() default "";

  /**
   * Quando o acesso � por objeto, este atributo deve ser definido indicando o caminho at� o atributo do ID usado na chave de permiss�o.
   */
  String idProperty() default "";

}
