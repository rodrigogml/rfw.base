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
    props.put("jboss.naming.client.ejb.context", true);

    InitialContext context;
    try {
      context = new InitialContext(props);
    } catch (NamingException e) {
      throw new RFWCriticalException(e);
    }
    return context;
  }
}
