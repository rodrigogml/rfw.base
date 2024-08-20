package br.eng.rodrigogml.rfw.base.utils;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe utilitária de métodos auxiliares para conexões/lookups JNDI.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (11 de jul de 2018)
 */
public class BUJNDI {

  private BUJNDI() {
  }

  /**
   * Faz o lookup por uma fachada utilizando um Context Local.<br>
   * Este método pode ser usado por exemplo para um módulo encontrar a fachada de outro quando estão deployed no mesmo servidor.
   *
   * @param jndiName JNDI Name para lookup.
   * @return Interface para o recurso solicitado se encontrado com sucesso.
   * @throws RFWException Lançado caso o método falhe em encontrar o recurso pelo JNDI name.
   */
  public static Object lookup(String jndiName) throws RFWException {
    Object facade = null;
    try {
      InitialContext context = new InitialContext();
      facade = context.lookup(jndiName);
    } catch (NamingException e) {
      throw new RFWCriticalException(e);
    }
    return facade;
  }

  /**
   * Recupera o contexto remoto passando um host e porta específicos.
   *
   * @param host host do servidor
   * @param port porta do servidor
   * @return Contexto Remoto usado para recupear as fachadas dos EJBs dos módulos.
   * @throws RFWException
   */
  public static InitialContext getRemoteContextGlassFish5(String host, Integer port) throws RFWException {
    Properties props = new Properties();
    props.setProperty("org.omg.CORBA.ORBInitialHost", host);
    props.setProperty("org.omg.CORBA.ORBInitialPort", "" + port);

    InitialContext context;
    try {
      context = new InitialContext(props);
    } catch (NamingException e) {
      throw new RFWCriticalException(e);
    }
    return context;
  }

  /**
   * Recupera o contexto remoto passando um host e porta específicos.
   *
   * @param host host do servidor
   * @param port porta do servidor
   * @return Contexto Remoto usado para recupear as fachadas dos EJBs dos módulos.
   * @throws RFWException
   */
  public static InitialContext getRemoteContextWildFly24(String host, Integer port) throws RFWException {
    Properties props = new Properties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
    props.put(Context.PROVIDER_URL, "http-remoting://" + host + ":" + port);
    // props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    props.put("jboss.naming.client.ejb.context", true);
    // props.put("org.jboss.ejb.client.scoped.context", "true");

    InitialContext context;
    try {
      context = new InitialContext(props);
    } catch (NamingException e) {
      throw new RFWCriticalException(e);
    }
    return context;
  }

  /**
   * Recupera a interface a partir de um contexto remoto passando um host e porta específicos e o JNDI name.
   *
   * @param host host do servidor
   * @param port porta do servidor
   * @param jndiName Nome do JNDI para o looup do EJB. Normalmente ao levantar um EJB o WildFly dá uma coleção de nomes no seu log, por exemplo:<br>
   *          <ul>
   *          2024-08-19 20:55:57,524 INFO [org.jboss.as.ejb3.deployment] (MSC service thread 1-8) WFLYEJB0473: JNDI bindings for session bean named 'BISKernelFacade' in deployment unit 'subdeployment "BISCoreEJB.jar" of deployment "BISEAR-8.0.0.ear"' are as follows:<br>
   *          <ul>
   *          java:global/BISERP/BISCoreEJB/BISKernelFacade!br.com.biserp.biskernel.facade.BISKernelFacadeRemote<br>
   *          java:app/BISCoreEJB/BISKernelFacade!br.com.biserp.biskernel.facade.BISKernelFacadeRemote<br>
   *          java:module/BISKernelFacade!br.com.biserp.biskernel.facade.BISKernelFacadeRemote<br>
   *          java:jboss/exported/BISERP/BISCoreEJB/BISKernelFacade!br.com.biserp.biskernel.facade.BISKernelFacadeRemote<br>
   *          ejb:BISERP/BISCoreEJB/BISKernelFacade!br.com.biserp.biskernel.facade.BISKernelFacadeRemote<br>
   *          java:global/BISERP/BISCoreEJB/BISKernelFacade<br>
   *          java:app/BISCoreEJB/BISKernelFacade<br>
   *          java:module/BISKernelFacade<br>
   *          </ul>
   *          Este método funcionará com o nome completo, incluindo a definição da interface remota (o nome da classe depois do !). Considerando os exemplos acima, o valor a ser passado neste argumento seria:<br>
   *          <ul>
   *          <li>/BISERP/BISCoreEJB/BISKernelFacade!br.com.biserp.biskernel.facade.BISKernelFacadeRemote</li>
   *          </ul>
   *          </ul>
   *
   * @return A interface solicitada.
   * @throws RFWException
   */
  public static Object lookupRemoteContextWildFly24(String host, Integer port, String jndiName) throws RFWException {
    InitialContext context = getRemoteContextWildFly24(host, port);
    try {
      return context.lookup(jndiName);
    } catch (NamingException e) {
      throw new RFWCriticalException(e);
    }
  }
}
