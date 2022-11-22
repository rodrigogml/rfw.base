package br.eng.rodrigogml.rfw.base.sessionmanager.interfaces;

import java.io.Serializable;

/**
 * Description: Interface que define os métodos necessários para definir um objeto de sessão.<br>
 * O objeto de sessão pode ser criado pelo sistema e carregar as informações que ele julgar necessário.
 *
 * @author Rodrigo GML
 * @since 10.0 (15 de out de 2020)
 */
public interface SessionVO extends Serializable {

  /**
   * Este método deve retornar o UUID que foi passado pelo SessionManager para o {@link SessionBackOperation#doLogin(String, String, String)}.<br>
   * Este conteúdo deve ser imutável e serve para identificar a sessão no SessionManager.
   *
   * @return Identificador Único da Sessão
   */
  public String getUUID();

  /**
   * Nome do usuário logado, quando sessão é do tipo usuário.<br>
   * Retorne o Nome do Sistema quando o login for do tipo sistemico.<br>
   * Esse valor é utilizado apenas para exibição / Log
   */
  public String getUser();

  /**
   * Permite identificar a sessão com um identificador único. Este identificador deve ser único por usuário que crie uma sessão no sistema, mas deve ser sempre constante para o mesmo usuário em toda sessão criada. <br>
   * Entre as utilidades deste ID é finalizar a sessão de um detemrinado usuário.
   *
   * @return Identificador único por usuário de sessão.
   */
  public String getUniqueID();

  /**
   * Método utilizado para verificar se um objeto de sessão tem autorização para uma determinada chave dinâmica.<br>
   * Chaves dinâmicas são chaves que oferecem permissões de acesso à nível de objeto.<br>
   * O usuário deve ser considerado autorizado se tiver qualquer uma das chaves passadas.
   *
   * @param keys Conjunto de chages de acesso.
   * @param objID ID do objeto de que se deseja acesso.
   * @return true caso a sessão tenha acesso à chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String[] keys, Long objID);

  /**
   * Método utilizado para verificar se um objeto de sessão tem autorização para uma determinada chave estática.<br>
   * O usuário deve ser considerado autorizado se tiver qualquer uma das chaves passadas.
   *
   * @param keys Chave de acesso.
   * @return true caso a sessão tenha acesso à chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String[] keys);

  /**
   * Método utilizado para verificar se um objeto de sessão tem autorização para uma determinada chave dinâmica.<br>
   * Chaves dinâmicas são chaves que oferecem permissões de acesso à nível de objeto.
   *
   * @param key Chave de acesso.
   * @param objID ID do objeto de que se deseja acesso.
   * @return true caso a sessão tenha acesso à chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String key, Long objID);

  /**
   * Método utilizado para verificar se um objeto de sessão tem autorização para uma determinada chave estática.<br>
   *
   * @param key Chave de acesso.
   * @return true caso a sessão tenha acesso à chave, false caso o acesso seja negado.
   */
  public boolean hasAccess(String key);

}
