package br.eng.rodrigogml.rfw.base.sessionmanager.interfaces;

import java.util.Locale;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;

/**
 * Description: Interface que precisa ser implementada pelo sistema e definida no {@link SessionManager} para fornecer as informações necessárias de autenticação e controle.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (15 de out de 2020)
 */
public interface SessionBackOperation {

  /**
   * Realiza o Login de um usuário no sistema.
   *
   * @param user Usuário que está tentando realizar o LogIn.
   * @param password Senha do usuário que está realizando o LogIn.
   * @param locale Locale a ser utilizado para o usuário conforme recebido pelo método {@link SessionManager#doLogin(String, String, Locale)}
   * @param uuid Identificador Únido de Sessão. O valor passado neste atributo deve ser imutável e ser o mesmo que o objeto {@link SessionVO} retornado no método retorne no seu método {@link SessionVO#getUUID()}.
   * @return O método deve retornar o objeto com as informações que o sistema julgar necessário para manter a sessão do usuário em pé. Deve-se lembrar que este objeto será mentido em memória o tempo que a sessão se mantiver em pé.
   * @throws RFWException Lançar em caso de falha na autentitação.
   */
  public SessionVO doLogin(String user, String password, Locale locale, String uuid) throws RFWException;

  /**
   * Realiza o Login de uma estação no sistema.
   *
   * @param token Token de identificação e acesso para realizar o LogIn.
   * @param uuid Identificador Únido de Sessão. O valor passado neste atributo deve ser imutável e ser o mesmo que o objeto {@link SessionVO} retornado no método retorne no seu método {@link SessionVO#getUUID()}.
   * @return O método deve retornar o objeto com as informações que o sistema julgar necessário para manter a sessão do usuário em pé. Deve-se lembrar que este objeto será mentido em memória o tempo que a sessão se mantiver em pé.
   * @throws RFWException Lançar em caso de falha na autentitação.
   */
  public SessionVO doLogin(String token, String uuid) throws RFWException;

  /**
   * Notifica o BackOperation de que o Session acabou de registrar uma atividade no sistema, isto é, abriu uma conexão pela fachada do sistema.
   *
   * @param ssVO {@link SessionVO} contendo as informações da sessão, mesmo objeto recebido de um dos métodos {@link #doLogin(String, String)}.
   * @throws RFWException
   */
  public void updateSessionVOActivity(SessionVO ssVO) throws RFWException;

}
