package br.eng.rodrigogml.rfw.base.utils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Esta classe tem a finalidade de concentrar funções de configuração e inicialização de conexões.<BR>
 * A classe nasceu com a necessidade da configuração da comunicação SSL para comunicação com WebServices (Para os módulos da NFp e NFe).<br>
 *
 * @author Rodrigo Leitão
 * @since 5.1.0 (13/10/2013)
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
 */
@Deprecated
public class BUConnection {

  private BUConnection() {
  }

  /**
   * Prepara o java para conseseguir realizar conexões HTTPS com encriptação SSL.
   *
   * @param km define o KeyStore com os certificados privados que podem ser usados para criptografia da conexão.
   * @param tm define o TrustManager, gerenciador de confiabilidade de certificados, para permitir que o java valide a realize a conexão com os servidores que usem um desses certificados.
   * @throws RFWException
   */
  public static void setupSSLConnection(final KeyManager[] km, final TrustManager[] tm) throws RFWException {
    // Define que as conexões que usam o protocolo de encriptação PKGS devem utilizar a classe do pacote da SUN. Isso é necessário porque a implementação nativa do java.net.URL não dá suporte à HTTPS
    System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

    try {
      // Recupera o Contexto do SSL e define os certificados definidos
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(km, tm, null);
      SSLContext.setDefault(sc); // Define esse contexto de chaves para as conexões SSL
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200055", e);
    }
  }

  /**
   * Define que o java deve permitir a renegociação de SSL/TLS.<br>
   * Usada por alguns provedores HTTPS ao tentar negociar o protocolo de encriptação a ser usado. Por padrão a renegociação foi desabilitada no java por "problemas de segurança". Para comunicar com alguns provedores, essa propriedade deve ser definida como TRUE.
   */
  public static void setUnsafeRenegotiation(final Boolean allow) {
    if (allow) {
      System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
    } else {
      System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
    }
  }

  // /**
  // * Cria um SocketFatory com KeyManager e TrustManager customizados e registra no protoclo HTTPS na porta 443. Assim, qualquer conexão que o sistema fizer com esse protocolo e porta (como acessar um webservice) utilizará um socket personalizado.
  // *
  // * @param keyManagers Gerenciadores de Certificados para identificação/autenticação na conexão.
  // * @param trustManagers Gerenciadores de validação do certificado do servidor.
  // * @param sslProtocol Código do protocolo SSL. Ex: SSLv3, TLSv1, TLSv1.1 and TLSv1.2
  // * @throws RFWException
  // */
  // public static void createSocketFactoryHTTPS443(KeyManager[] keyManagers, TrustManager[] trustManagers, String sslProtocol) throws RFWException {
  // BUSocketFactory factory = new BUSocketFactory(keyManagers, trustManagers, sslProtocol);
  // Protocol protocol = new Protocol("https", factory, 443);
  // Protocol.registerProtocol("https", protocol);
  // }

  // /**
  // * Description: ProtocolSocketFactory que permite configurar as conexões antes de se criar os sockets para conexão de vários módulos.<br>
  // * Veja os métodos disponíveis em {@link BUConnection} para criação do socketFactory
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
