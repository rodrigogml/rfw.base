package br.eng.rodrigogml.rfw.base.sessionmanager.interfaces;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;

/**
 * Description: Interface que precisa ser implementada pelo sistema e definida no {@link SessionManager} para fornecer as informa��es necess�rias de autentica��o e controle.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (15 de out de 2020)
 */
public interface SessionBackOperation {

  /**
   * Realiza o Login de um usu�rio no sistema.
   *
   * @param user Usu�rio que est� tentando realizar o LogIn.
   * @param password Senha do usu�rio que est� realizando o LogIn.
   * @param locale Locale a ser utilizado para o usu�rio conforme recebido pelo m�todo {@link SessionManager#doLogin(String, String, Locale)}
   * @param uuid Identificador �nido de Sess�o. O valor passado neste atributo deve ser imut�vel e ser o mesmo que o objeto {@link SessionVO} retornado no m�todo retorne no seu m�todo {@link SessionVO#getUUID()}.
   * @return O m�todo deve retornar o objeto com as informa��es que o sistema julgar necess�rio para manter a sess�o do usu�rio em p�. Deve-se lembrar que este objeto ser� mentido em mem�ria o tempo que a sess�o se mantiver em p�.
   * @throws RFWException Lan�ar em caso de falha na autentita��o.
   */
  public SessionVO doLogin(String user, String password, Locale locale, String uuid) throws RFWException;

  /**
   * Realiza o Login de uma esta��o no sistema.
   *
   * @param token Token de identifica��o e acesso para realizar o LogIn.
   * @param uuid Identificador �nido de Sess�o. O valor passado neste atributo deve ser imut�vel e ser o mesmo que o objeto {@link SessionVO} retornado no m�todo retorne no seu m�todo {@link SessionVO#getUUID()}.
   * @return O m�todo deve retornar o objeto com as informa��es que o sistema julgar necess�rio para manter a sess�o do usu�rio em p�. Deve-se lembrar que este objeto ser� mentido em mem�ria o tempo que a sess�o se mantiver em p�.
   * @throws RFWException Lan�ar em caso de falha na autentita��o.
   */
  public SessionVO doLogin(String token, String uuid) throws RFWException;

  /**
   * Notifica o BackOperation de que o Session acabou de registrar uma atividade no sistema, isto �, abriu uma conex�o pela fachada do sistema.
   *
   * @param ssVO {@link SessionVO} contendo as informa��es da sess�o, mesmo objeto recebido de um dos m�todos {@link #doLogin(String, String)}.
   * @throws RFWException
   */
  public void updateSessionVOActivity(SessionVO ssVO) throws RFWException;

}
