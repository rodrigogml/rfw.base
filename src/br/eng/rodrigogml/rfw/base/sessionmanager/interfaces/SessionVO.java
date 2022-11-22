package br.eng.rodrigogml.rfw.base.sessionmanager.interfaces;

import java.io.Serializable;

/**
 * Description: Interface que define os m�todos necess�rios para definir um objeto de sess�o.<br>
 * O objeto de sess�o pode ser criado pelo sistema e carregar as informa��es que ele julgar necess�rio.
 *
 * @author Rodrigo GML
 * @since 10.0 (15 de out de 2020)
 */
public interface SessionVO extends Serializable {

  /**
   * Este m�todo deve retornar o UUID que foi passado pelo SessionManager para o {@link SessionBackOperation#doLogin(String, String, String)}.<br>
   * Este conte�do deve ser imut�vel e serve para identificar a sess�o no SessionManager.
   *
   * @return Identificador �nico da Sess�o
   */
  public String getUUID();

  /**
   * Nome do usu�rio logado, quando sess�o � do tipo usu�rio.<br>
   * Retorne o Nome do Sistema quando o login for do tipo sistemico.<br>
   * Esse valor � utilizado apenas para exibi��o / Log
   */
  public String getUser();

  /**
   * Permite identificar a sess�o com um identificador �nico. Este identificador deve ser �nico por usu�rio que crie uma sess�o no sistema, mas deve ser sempre constante para o mesmo usu�rio em toda sess�o criada. <br>
   * Entre as utilidades deste ID � finalizar a sess�o de um detemrinado usu�rio.
   *
   * @return Identificador �nico por usu�rio de sess�o.
   */
  public String getUniqueID();

  /**
   * M�todo utilizado para verificar se um objeto de sess�o tem autoriza��o para uma determinada chave din�mica.<br>
   * Chaves din�micas s�o chaves que oferecem permiss�es de acesso � n�vel de objeto.<br>
   * O usu�rio deve ser considerado autorizado se tiver qualquer uma das chaves passadas.
   *
   * @param keys Conjunto de chages de acesso.
   * @param objID ID do objeto de que se deseja acesso.
   * @return true caso a sess�o tenha acesso � chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String[] keys, Long objID);

  /**
   * M�todo utilizado para verificar se um objeto de sess�o tem autoriza��o para uma determinada chave est�tica.<br>
   * O usu�rio deve ser considerado autorizado se tiver qualquer uma das chaves passadas.
   *
   * @param keys Chave de acesso.
   * @return true caso a sess�o tenha acesso � chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String[] keys);

  /**
   * M�todo utilizado para verificar se um objeto de sess�o tem autoriza��o para uma determinada chave din�mica.<br>
   * Chaves din�micas s�o chaves que oferecem permiss�es de acesso � n�vel de objeto.
   *
   * @param key Chave de acesso.
   * @param objID ID do objeto de que se deseja acesso.
   * @return true caso a sess�o tenha acesso � chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String key, Long objID);

  /**
   * M�todo utilizado para verificar se um objeto de sess�o tem autoriza��o para uma determinada chave est�tica.<br>
   *
   * @param key Chave de acesso.
   * @return true caso a sess�o tenha acesso � chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String key);

}
