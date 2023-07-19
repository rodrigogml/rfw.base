package br.eng.rodrigogml.rfw.base.utils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Esta classe tem a finalidade de concentrar fun��es de configura��o e inicializa��o de conex�es.<BR>
 * A classe nasceu com a necessidade da configura��o da comunica��o SSL para comunica��o com WebServices (Para os m�dulos da NFp e NFe).<br>
 *
 * @author Rodrigo Leit�o
 * @since 5.1.0 (13/10/2013)
 */
public class BUConnection {

  private BUConnection() {
  }

  /**
   * Prepara o java para conseseguir realizar conex�es HTTPS com encripta��o SSL.
   *
   * @param km define o KeyStore com os certificados privados que podem ser usados para criptografia da conex�o.
   * @param tm define o TrustManager, gerenciador de confiabilidade de certificados, para permitir que o java valide a realize a conex�o com os servidores que usem um desses certificados.
   * @throws RFWException
   */
  public static void setupSSLConnection(final KeyManager[] km, final TrustManager[] tm) throws RFWException {
    // Define que as conex�es que usam o protocolo de encripta��o PKGS devem utilizar a classe do pacote da SUN. Isso � necess�rio porque a implementa��o nativa do java.net.URL n�o d� suporte � HTTPS
    System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

    try {
      // Recupera o Contexto do SSL e define os certificados definidos
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(km, tm, null);
      SSLContext.setDefault(sc); // Define esse contexto de chaves para as conex�es SSL
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200055", e);
    }
  }

  /**
   * Prepara o java para conseseguir realizar conex�es HTTPS com encripta��o TLS 1.2.
   *
   * @param km define o KeyStore com os certificados privados que podem ser usados para criptografia da conex�o.
   * @param tm define o TrustManager, gerenciador de confiabilidade de certificados, para permitir que o java valide a realize a conex�o com os servidores que usem um desses certificados.
   * @param sslProtocol Procolo de conex�o SSL. Ex: SSLv3, TLSv1, TLSv1.1 and TLSv1.2
   * @throws RFWException
   */
  public static void setupTLSConnection(final KeyManager[] km, final TrustManager[] tm, String sslProtocol) throws RFWException {
    // Define que as conex�es que usam o protocolo de encripta��o PKGS devem utilizar a classe do pacote da SUN. Isso � necess�rio porque a implementa��o nativa do java.net.URL n�o d� suporte � HTTPS
    // System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

    try {
      // Recupera o Contexto do SSL e define os certificados definidos
      SSLContext sc = SSLContext.getInstance(sslProtocol);
      sc.init(km, tm, null);
      SSLContext.setDefault(sc); // Define esse contexto de chaves para as conex�es SSL
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200055", e);
    }
  }

  /**
   * Define que o java deve permitir a renegocia��o de SSL/TLS.<br>
   * Usada por alguns provedores HTTPS ao tentar negociar o protocolo de encripta��o a ser usado. Por padr�o a renegocia��o foi desabilitada no java por "problemas de seguran�a". Para comunicar com alguns provedores, essa propriedade deve ser definida como TRUE.
   */
  public static void setUnsafeRenegotiation(final Boolean allow) {
    if (allow) {
      System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
    } else {
      System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
    }
  }

  // /**
  // * Cria um SocketFatory com KeyManager e TrustManager customizados e registra no protoclo HTTPS na porta 443. Assim, qualquer conex�o que o sistema fizer com esse protocolo e porta (como acessar um webservice) utilizar� um socket personalizado.
  // *
  // * @param keyManagers Gerenciadores de Certificados para identifica��o/autentica��o na conex�o.
  // * @param trustManagers Gerenciadores de valida��o do certificado do servidor.
  // * @param sslProtocol C�digo do protocolo SSL. Ex: SSLv3, TLSv1, TLSv1.1 and TLSv1.2
  // * @throws RFWException
  // */
  // public static void createSocketFactoryHTTPS443(KeyManager[] keyManagers, TrustManager[] trustManagers, String sslProtocol) throws RFWException {
  // BUSocketFactory factory = new BUSocketFactory(keyManagers, trustManagers, sslProtocol);
  // Protocol protocol = new Protocol("https", factory, 443);
  // Protocol.registerProtocol("https", protocol);
  // }

  // /**
  // * Description: ProtocolSocketFactory que permite configurar as conex�es antes de se criar os sockets para conex�o de v�rios m�dulos.<br>
  // * Veja os m�todos dispon�veis em {@link BUConnection} para cria��o do socketFactory
  // *
  // * @author Rodrigo GML
  // * @since 10.0 (26 de ago de 2021)
  // */
  // public static class BUSocketFactory implements ProtocolSocketFactory {
  //
  // private final SSLContext sslContext;
  //
  // public BUSocketFactory(KeyManager[] keyManagers, TrustManager[] trustManagers, String sslProtocol) throws RFWException {
  // try {
  // sslContext = SSLContext.getInstance(sslProtocol);
  // sslContext.init(keyManagers, trustManagers, null);
  // } catch (KeyManagementException | NoSuchAlgorithmException e) {
  // throw new RFWCriticalException("Falha ao criar SSL Context!", e);
  // }
  // }
  //
  // @Override
  // public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
  // return this.sslContext.getSocketFactory().createSocket(host, port, localAddress, localPort);
  // }
  //
  // @Override
  // public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
  // final Socket socket = this.sslContext.getSocketFactory().createSocket();
  // socket.bind(new InetSocketAddress(localAddress, localPort));
  // socket.connect(new InetSocketAddress(host, port), 60000);
  // return socket;
  // }
  //
  // @Override
  // public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
  // return this.sslContext.getSocketFactory().createSocket(host, port);
  // }
  //
  // public SSLContext getSslContext() {
  // return sslContext;
  // }
  // }
}
