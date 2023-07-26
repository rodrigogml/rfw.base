package br.eng.rodrigogml.rfw.base.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.login.FailedLoginException;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;

import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.kernel.logger.RFWLogger;
import br.eng.rodrigogml.rfw.kernel.utils.RUDateTime;
//import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
//import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
//import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
//import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
//import sun.security.pkcs11.wrapper.PKCS11;
//import sun.security.pkcs11.wrapper.PKCS11Constants;
//import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * Description: Classe utilitária com os métodos de manipulação de certificados.<BR>
 *
 * @author Rodrigo Leitão
 * @since 5.1.0 (21/10/2013)
 */
public class BUCert {

  private BUCert() {
  }

  /**
   * Cria um TrustManager que aceita qualquer certificado como válido para conexões SSL.
   *
   * @return
   * @throws RFWException
   */
  public static TrustManager[] createTrustManagerAcceptAll() throws RFWException {
    X509TrustManager trustManager = new X509TrustManager() {
      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      @Override
      public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }

      @Override
      public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }
    };
    return new TrustManager[] { trustManager };
  }

  /**
   * Cria um TrustManager para ser usado em conexões SSL a partir de cadeias de certificados carregados de um arquivo criado pelo keytool (KeyStore).<Br>
   * Melhor explicação de como usar o keytool na documentação do Framework.
   *
   * @param in arquivo com uma ou mais cadeias de certificados para serem confiados.
   * @param pass senha do arquivo para importação dos certificados.
   * @return
   * @throws RFWException
   *           <li>RFW_ERR_000002 - Falha ao abrir certificado. Verifique o arquivo e a senha.
   */
  public static TrustManager[] createTrustManager(final InputStream in, final String pass) throws RFWException {
    try {
      // Cria um novo KeyStore com o tipo de algorítimo JKS
      KeyStore truststore = KeyStore.getInstance("JKS");
      truststore.load(in, pass.toCharArray());
      try {
        // Uma vez que já foi lido, forçamos seu fechamento para garantir que não teremos vazamento de recurso, mas se falhar não ligamos, logamos mas continuamos o método.
        in.close();
      } catch (Exception e) {
        RFWLogger.logException(e);
      }

      // Cria o gerenciador de confiabilidade de certificados passando a cadeia de certificados lida
      TrustManagerFactory trustmanagerfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustmanagerfactory.init(truststore);
      TrustManager[] trustmanagers = trustmanagerfactory.getTrustManagers();

      return trustmanagers;
    } catch (KeyStoreException e) {
      throw new RFWCriticalException("RFW_ERR_200082", e);
    } catch (NoSuchAlgorithmException e) {
      throw new RFWCriticalException("RFW_ERR_200082", e);
    } catch (CertificateException e) {
      throw new RFWCriticalException("RFW_ERR_000002", e);
    } catch (IOException e) {
      throw new RFWValidationException("RFW_ERR_200081", e);
    }
  }

  /**
   * Coloca a KeyStore dentro da KeyManager do java. Essas Keymanager podem ser usadas nas conexões SSL para autenticar a origem da conexão.<br>
   * Utiliza o Algoritimo "SunX509" como padrão do certificado.
   *
   * @param clientkeystore KeyStore contendo o certificado do usuário.
   * @param certpass Senha da KeyStore.
   * @return Array com as KeyManager atualmente carregadas na aplicação.
   * @throws RFWException
   *           <li>RFW_ERR_000002 - Falha ao abrir certificado. Verifique o arquivo e a senha.
   */
  public static KeyManager[] createKeyManager(KeyStore clientkeystore, String certpass) throws RFWException {
    try {
      KeyManagerFactory keymanagerfactory = KeyManagerFactory.getInstance("SunX509"); // KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keymanagerfactory.init(clientkeystore, certpass.toCharArray());
      KeyManager[] keymanagers = keymanagerfactory.getKeyManagers();
      return keymanagers;
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_000002");
    }
  }

  /**
   * Este método carrega um certificado do tipo A1 dentro de uma keystore.<br>
   *
   * @param certpfx InputStream com o arquivo do certificado A1.
   * @param certpass Senha para acesso do certificado.
   * @return
   * @throws RFWException
   *           <li>RFW_ERR_000002 - Falha ao abrir certificado. Verifique o arquivo e a senha.
   */
  public static KeyStore loadKeyStoreFromPKCS12(InputStream certpfx, String certpass) throws RFWException {
    try {
      final KeyStore keystore = KeyStore.getInstance("PKCS12");
      keystore.load(certpfx, certpass.toCharArray());
      return keystore;
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_000002", e);
    }
  }

  /**
   * Este método retorna o fingerprint MD5 do certificado.
   *
   * @param certificate instancia do certificado.
   * @return retorna o fingerprint do certificado, Ex: '1A:DE:60:21:DE:B1:BF:C3:D1:AD:11:F1:21:22:D7:9E'
   * @throws RFWException Lançado caso o certificado não possua algorítimo MD5, ou ocorra algum erro ao decodificar o certificado.
   */
  public static String getMD5FingerPrintFromCertificate(Certificate certificate) throws RFWException {
    /*
     * Este código foi criado com base no código fonte da ferramenta keytool fornecida junto com o java. Não sei explicar exatamente seu funcionamento nem hoje, que estou implementando, não me pergunte no futuro! Link do SourceCode usado de Base: http://www.docjar.com/html/api/sun/security/tools/KeyTool.java.html
     */
    try {
      byte[] digest = MessageDigest.getInstance("MD5").digest(certificate.getEncoded());
      StringBuilder buf = new StringBuilder();
      int len = digest.length;
      char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
      for (int i = 0; i < len; i++) {
        int high = ((digest[i] & 0xf0) >> 4);
        int low = (digest[i] & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
        if (i < len - 1) {
          buf.append(":");
        }
      }
      return buf.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RFWValidationException("RFW_ERR_200219", e);
    } catch (CertificateEncodingException e) {
      // Lançado como erro crítico porque não sei quando esse erro ocorrerá, a medida que for acontecendo vamos melhorando o tratamento do erro
      throw new RFWCriticalException("RFW_ERR_200220", e);
    }
  }

  /**
   * Este método retorna o fingerprint SHA1 do certificado.
   *
   * @param certificate instancia do certificado.
   * @return retorna o fingerprint do certificado, Ex: '72:3A:D9:2E:1A:DE:60:21:DE:B1:BF:C3:D1:AD:11:F1:21:22:D7:9E'
   * @throws RFWException Lançado caso o certificado não possua algorítimo SHA1, ou ocorra algum erro ao decodificar o certificado.
   */
  public static String getSHA1FingerPrintFromCertificate(Certificate certificate) throws RFWException {
    /*
     * Este código foi criado com base no código fonte da ferramenta keytool fornecida junto com o java. Não sei explicar exatamente seu funcionamento nem hoje, que estou implementando, não me pergunte no futuro! Link do SourceCode usado de Base: http://www.docjar.com/html/api/sun/security/tools/KeyTool.java.html
     */
    try {
      byte[] digest = MessageDigest.getInstance("SHA1").digest(certificate.getEncoded());
      StringBuilder buf = new StringBuilder();
      int len = digest.length;
      char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
      for (int i = 0; i < len; i++) {
        int high = ((digest[i] & 0xf0) >> 4);
        int low = (digest[i] & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
        if (i < len - 1) {
          buf.append(":");
        }
      }
      return buf.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RFWValidationException("RFW_ERR_200219", e);
    } catch (CertificateEncodingException e) {
      // Lançado como erro crítico porque não sei quando esse erro ocorrerá, a medida que for acontecendo vamos melhorando o tratamento do erro
      throw new RFWCriticalException("RFW_ERR_200220", e);
    }
  }

  /**
   * Este método carrega certificados do tipo A3 dentro de uma keystore, do primeiro provider que encontrar.<br>
   * Para encontrar um certificado mais precisamente, verifique outros métodos que carregam os providers e listam seus certificados.<br>
   * <b>Lembre-se que em casos de haver mais de um cartão/token ligado a máquina o sistema pode usar a senha no cartão errada, e a insistência da operação pode travar o device até que a senha PUK seja colocada.</b>
   *
   * @param certpass Senha para ser usada no equipamento.
   * @return
   * @throws RFWException
   */
  public static KeyStore loadKeyStoreFromPKCS11(String certpass) throws RFWException {
    return BUCert.loadKeyStoreFromPKCS11(certpass, null);
  }

  /**
   * Este método carrega certificados do tipo A3 dentro de uma keystore.<br>
   * Caso exista mais de um certificado do mesmo provider, todos serão importados para dentro da mesma KeyStore, cada um com seu Alias.<br>
   * No caso de múltiplos tokens (devices) que compartilhem o mesmo "name" de provider, é necessário saber o nome extado do provider, carregado no slot correto! Para ter uma lista dos providers completa e seus slots disponíveis consulte outros métodos.
   *
   * @param certpass Senha para acesso do certificado.
   * @param provider Nome do provedor do certificado.
   * @return
   * @throws RFWException
   *           <li>RFW_ERR_000002 - Falha ao abrir certificado. Verifique o arquivo e a senha.
   *           <li>RFW_ERR_000003 - Login inválido para abrir o certificado! Verifique a senha e tente novamente.
   */
  public static KeyStore loadKeyStoreFromPKCS11(String certpass, String provider) throws RFWException {
    try {
      final KeyStore keystore = KeyStore.getInstance("PKCS11", provider);
      keystore.load(null, certpass.toCharArray());
      return keystore;
    } catch (IOException e) {
      if (e.getCause() instanceof FailedLoginException) {
        // Falha de senha para obter o certificado (Senha incorreta)
        throw new RFWCriticalException("RFW_ERR_000003", e);
      }
      throw new RFWCriticalException("RFW_ERR_000002", e);
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_000002", e);
    }
  }

  // /**
  // * Obtem os slots encontrados de uma biblioteca PKCS11.<br>
  // *
  // *
  // * @param librarypath caminho completo para a biblioteca do equipamento (Token/SmartCard), .dll para Windows ou .so para linux.
  // * @return Slots que podem ser utilizados para carregar o provider desejado. Retorna um Array de tamanho 0 se conseguir carregar a biblioteca mas não encontrar nenhum dispositivo, como nenhum cartão inserido ou tokenUSB plugado.
  // * @throws RFWException Lançado caso ocorra algum problema ao executar a operação.
  // */
  // public static long[] getSlotsPKCS11(String librarypath) throws RFWException {
  // CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
  // initArgs.flags = PKCS11Constants.CKF_OS_LOCKING_OK;
  //
  // String functionList = "C_GetFunctionList";
  // PKCS11 tmpPKCS11 = null;
  // long[] slotList = null;
  // try {
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, false);
  // } catch (PKCS11Exception e) {
  // try {
  // initArgs = null;
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, true);
  // } catch (PKCS11Exception ex) {
  // throw new RFWCriticalException("RFW_ERR_200086", ex);
  // } catch (IOException ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // } catch (IOException e) {
  // throw new RFWWarningException("RFW_ERR_200086", e);
  // }
  //
  // try {
  // slotList = tmpPKCS11.C_GetSlotList(true);
  // } catch (PKCS11Exception ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // return slotList;
  // }

  // /**
  // * Obtem um objeto com as informações do token de um determinado slot de uma biblioteca PKCS11.<br>
  // *
  // *
  // * @param librarypath caminho completo para a biblioteca do equipamento (Token/SmartCard), .dll para Windows ou .so para linux.
  // * @param slot número do slot do device que desejamos obter informações do token inserido.
  // * @return Objeto com as informações obtidas do device plugado nesse slot.
  // * @throws RFWException Lançado caso ocorra algum problema ao executar a operação.
  // */
  // public static CK_TOKEN_INFO getTokenInfoPKCS11(String librarypath, long slot) throws RFWException {
  // CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
  // initArgs.flags = PKCS11Constants.CKF_OS_LOCKING_OK;
  //
  // String functionList = "C_GetFunctionList";
  // PKCS11 tmpPKCS11 = null;
  // try {
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, false);
  // } catch (PKCS11Exception e) {
  // try {
  // initArgs = null;
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, true);
  // } catch (PKCS11Exception ex) {
  // throw new RFWCriticalException("RFW_ERR_200086", ex);
  // } catch (IOException ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // } catch (IOException e) {
  // throw new RFWWarningException("RFW_ERR_200086", e);
  // }
  //
  // try {
  // return tmpPKCS11.C_GetTokenInfo(slot);
  // } catch (PKCS11Exception ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // }

  // /**
  // * Obtem um objeto com as informações de um determinado slot de uma biblioteca PKCS11.<br>
  // *
  // *
  // * @param librarypath caminho completo para a biblioteca do equipamento (Token/SmartCard), .dll para Windows ou .so para linux.
  // * @param slot número do slot do device que desejamos obter informações.
  // * @return Objeto com as informações obtidas do device plugado nesse slot.
  // * @throws RFWException Lançado caso ocorra algum problema ao executar a operação.
  // */
  // public static CK_SLOT_INFO getSlotsInfoPKCS11(String librarypath, long slot) throws RFWException {
  // CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
  // initArgs.flags = PKCS11Constants.CKF_OS_LOCKING_OK;
  //
  // String functionList = "C_GetFunctionList";
  // PKCS11 tmpPKCS11 = null;
  // try {
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, false);
  // } catch (PKCS11Exception e) {
  // try {
  // initArgs = null;
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, true);
  // } catch (PKCS11Exception ex) {
  // throw new RFWCriticalException("RFW_ERR_200086", ex);
  // } catch (IOException ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // } catch (IOException e) {
  // throw new RFWWarningException("RFW_ERR_200086", e);
  // }
  //
  // try {
  // return tmpPKCS11.C_GetSlotInfo(slot);
  // } catch (PKCS11Exception ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // }

  // /**
  // * Método auxiliar que retorna uma lista com todos os atributos que puderam ser lidos do objeto encontrado dentro de um Token.<br>
  // * <b>Note que nem todos os atributos são suportados por todos os tipos de objetos, esse método ignora os atributos que resultaram em erro automaticamente.</b><br>
  // * Antes de chamar esse método o método C_FindObjects já deve ter sido evocado e, depois do retorno desse método, deve ser fechado e tratado normalmente.
  // *
  // * @param tmpPKCS11 objeto com sessão aberta para o device.
  // * @param session ID de sessão aberta para a a consulta.
  // * @param findindex índice do objeto retornado na busca para obter os parametros.
  // *
  // * @return Lista com todos os atributos que retornaram sem erro na consulta do objeto.
  // */
  // private static List<CK_ATTRIBUTE> getTokenQueryAttributesPKCS11(PKCS11 tmpPKCS11, long session, long findindex) {
  // final CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] { new CK_ATTRIBUTE(PKCS11Constants.CKA_AC_ISSUER), new CK_ATTRIBUTE(PKCS11Constants.CKA_ALWAYS_SENSITIVE), new CK_ATTRIBUTE(PKCS11Constants.CKA_APPLICATION), new CK_ATTRIBUTE(PKCS11Constants.CKA_ATTR_TYPES), new CK_ATTRIBUTE(PKCS11Constants.CKA_AUTH_PIN_FLAGS), new CK_ATTRIBUTE(PKCS11Constants.CKA_BASE), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_CERTIFICATE_TYPE), new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS), new CK_ATTRIBUTE(PKCS11Constants.CKA_COEFFICIENT), new CK_ATTRIBUTE(PKCS11Constants.CKA_DECRYPT), new CK_ATTRIBUTE(PKCS11Constants.CKA_DERIVE), new CK_ATTRIBUTE(PKCS11Constants.CKA_EC_PARAMS), new CK_ATTRIBUTE(PKCS11Constants.CKA_EC_POINT), new CK_ATTRIBUTE(PKCS11Constants.CKA_ECDSA_PARAMS), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_ENCRYPT), new CK_ATTRIBUTE(PKCS11Constants.CKA_END_DATE), new CK_ATTRIBUTE(PKCS11Constants.CKA_EXPONENT_1), new CK_ATTRIBUTE(PKCS11Constants.CKA_EXPONENT_2), new CK_ATTRIBUTE(PKCS11Constants.CKA_EXTRACTABLE),
  // new CK_ATTRIBUTE(PKCS11Constants.CKA_HAS_RESET), new CK_ATTRIBUTE(PKCS11Constants.CKA_HW_FEATURE_TYPE), new CK_ATTRIBUTE(PKCS11Constants.CKA_ID), new CK_ATTRIBUTE(PKCS11Constants.CKA_ISSUER), new CK_ATTRIBUTE(PKCS11Constants.CKA_KEY_GEN_MECHANISM), new CK_ATTRIBUTE(PKCS11Constants.CKA_KEY_TYPE), new CK_ATTRIBUTE(PKCS11Constants.CKA_LABEL), new CK_ATTRIBUTE(PKCS11Constants.CKA_LOCAL), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_MODIFIABLE), new CK_ATTRIBUTE(PKCS11Constants.CKA_MODULUS), new CK_ATTRIBUTE(PKCS11Constants.CKA_MODULUS_BITS), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_BASE), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_CERT_MD5_HASH), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_CERT_SHA1_HASH), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_DB), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_TRUST_BASE), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_TRUST_CLIENT_AUTH), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_TRUST_CODE_SIGNING),
  // new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_TRUST_EMAIL_PROTECTION), new CK_ATTRIBUTE(PKCS11Constants.CKA_NETSCAPE_TRUST_SERVER_AUTH), new CK_ATTRIBUTE(PKCS11Constants.CKA_NEVER_EXTRACTABLE), new CK_ATTRIBUTE(PKCS11Constants.CKA_OBJECT_ID), new CK_ATTRIBUTE(PKCS11Constants.CKA_OWNER), new CK_ATTRIBUTE(PKCS11Constants.CKA_PRIME), new CK_ATTRIBUTE(PKCS11Constants.CKA_PRIME_1), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_PRIME_2), new CK_ATTRIBUTE(PKCS11Constants.CKA_PRIME_BITS), new CK_ATTRIBUTE(PKCS11Constants.CKA_PRIVATE), new CK_ATTRIBUTE(PKCS11Constants.CKA_PRIVATE_EXPONENT), new CK_ATTRIBUTE(PKCS11Constants.CKA_PUBLIC_EXPONENT), new CK_ATTRIBUTE(PKCS11Constants.CKA_RESET_ON_INIT), new CK_ATTRIBUTE(PKCS11Constants.CKA_SECONDARY_AUTH), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_SENSITIVE), new CK_ATTRIBUTE(PKCS11Constants.CKA_SERIAL_NUMBER), new CK_ATTRIBUTE(PKCS11Constants.CKA_SIGN), new CK_ATTRIBUTE(PKCS11Constants.CKA_SIGN_RECOVER), new CK_ATTRIBUTE(PKCS11Constants.CKA_START_DATE),
  // new CK_ATTRIBUTE(PKCS11Constants.CKA_SUB_PRIME_BITS), new CK_ATTRIBUTE(PKCS11Constants.CKA_SUBJECT), new CK_ATTRIBUTE(PKCS11Constants.CKA_SUBPRIME), new CK_ATTRIBUTE(PKCS11Constants.CKA_TOKEN), new CK_ATTRIBUTE(PKCS11Constants.CKA_TRUSTED), new CK_ATTRIBUTE(PKCS11Constants.CKA_UNWRAP), new CK_ATTRIBUTE(PKCS11Constants.CKA_VALUE), new CK_ATTRIBUTE(PKCS11Constants.CKA_VALUE_BITS), new
  // CK_ATTRIBUTE(PKCS11Constants.CKA_VALUE_LEN), new CK_ATTRIBUTE(PKCS11Constants.CKA_VENDOR_DEFINED), new CK_ATTRIBUTE(PKCS11Constants.CKA_VERIFY), new CK_ATTRIBUTE(PKCS11Constants.CKA_VERIFY_RECOVER), new CK_ATTRIBUTE(PKCS11Constants.CKA_WRAP) };
  // final List<CK_ATTRIBUTE> resultlist = new ArrayList<>();
  //
  // // Itera cada attriuto em busca dos atributos válidos para este objeto
  // for (int i = 0; i < attributes.length; i++) {
  // CK_ATTRIBUTE[] cka = new CK_ATTRIBUTE[1];
  // cka[0] = attributes[i];
  // try {
  // // Lê os objetos
  // tmpPKCS11.C_GetAttributeValue(session, findindex, cka);
  // resultlist.add(cka[0]);
  // } catch (PKCS11Exception e) {
  // // se não é suportado nem verificamos erro, apenas seguimos para o próximo atributo e não o adicionamos na lista de retorno
  // }
  // }
  // // retorna lista final
  // return resultlist;
  // }

  // /**
  // * Conecta-se no dispositivo e obtem uma lista de atributos de cada objeto encontrado no dispositivo.
  // *
  // * @param librarypath biblioteca de acesso ao dispositivo
  // * @param slot número do slot do objeto.
  // * @return Lista com uma lista de atributos encontrados em cada objeto.
  // * @throws RFWException Lançado caso não seja possível concluir algum passo.
  // */
  // public static List<List<CK_ATTRIBUTE>> getSlotsObjectsInfoPKCS11(String librarypath, long slot) throws RFWException {
  // CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
  // initArgs.flags = PKCS11Constants.CKF_OS_LOCKING_OK;
  //
  // // Tenta criar o acesso para o device e slot indicados
  // String functionList = "C_GetFunctionList";
  // PKCS11 tmpPKCS11 = null;
  // try {
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, false);
  // } catch (PKCS11Exception e) {
  // try {
  // initArgs = null;
  // tmpPKCS11 = PKCS11.getInstance(librarypath, functionList, initArgs, true);
  // } catch (PKCS11Exception ex) {
  // throw new RFWCriticalException("RFW_ERR_200086", ex);
  // } catch (IOException ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // }
  // } catch (IOException e) {
  // throw new RFWWarningException("RFW_ERR_200086", e);
  // }
  //
  // long session = -1;
  // try {
  // // Abre a sessão com o dispositivo
  // session = tmpPKCS11.C_OpenSession(slot, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
  //
  // // Cria o filtro dos objetos que queremos buscar
  // CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
  // attributes[0] = new CK_ATTRIBUTE();
  // attributes[0].type = PKCS11Constants.CKA_TOKEN;
  // attributes[0].pValue = true;
  // tmpPKCS11.C_FindObjectsInit(session, attributes);
  //
  // // Lista que retornará com as informações
  // final List<List<CK_ATTRIBUTE>> objectsinfolist = new ArrayList<>();
  //
  // // Buscamos os objetos
  // int lastindex = 1;
  // while (true) {
  // long[] keyhandles = tmpPKCS11.C_FindObjects(session, lastindex++);
  // if (keyhandles.length == 0) break;
  //
  // // Iteraos as chaves dos objetoe contrados
  // for (int i = 0; i < keyhandles.length; i++) {
  // long findindex = keyhandles[i];
  // // Faz a consulta dos atributos que esse objeto suportar e colocar na lista de retorno
  // final List<CK_ATTRIBUTE> list = getTokenQueryAttributesPKCS11(tmpPKCS11, session, findindex);
  // objectsinfolist.add(list);
  // }
  //
  // }
  // return objectsinfolist;
  // } catch (PKCS11Exception ex) {
  // throw new RFWWarningException("RFW_ERR_200086", ex);
  // } finally {
  // try {
  // if (session >= 0) {
  // tmpPKCS11.C_FindObjectsFinal(session);
  // tmpPKCS11.C_CloseSession(session);
  // }
  // } catch (PKCS11Exception e) {
  // }
  // }
  // }

  /**
   * Carrega o Provider solicitado.
   *
   * @param providername Nome de identificação do Provider
   * @param librarypath caminho completo para a biblioteca de comunicação com o device de criptografia.
   * @param slot Número do device. Deve ser informado principalmente quando há mais de um device na mesma máquina para evitar que se retorne o errado. Se não informado é retornado o primeiro Provider encontrado. Para obter os números dos slotes existentes para um provider utilize o método {@link BUConnection#getSlotsPKCS11Library(String)}
   * @return
   */
  public static Provider loadProviderPKCS11(String providername, String librarypath, Long slot) {
    String tokenconfiguration = null;
    if (slot != null) {
      tokenconfiguration = new String("name = " + providername + "_" + slot + "\n" + "library = " + librarypath + "\nslot = " + slot + "\n");
    } else {
      tokenconfiguration = new String("name = " + providername + "\n" + "library = " + librarypath + "\n");
    }
    Provider provider = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(tokenconfiguration.getBytes()));
    final Provider existentprovider = Security.getProvider(provider.getName());
    if (existentprovider != null) {
      Security.addProvider(provider);
    } else {
      provider = existentprovider;
    }
    return provider;
  }

  // /**
  // * Este método tenta carregar todos os "Provider" possíveis (conhecidos por esta classe) para certificados tipo A3.<br>
  // *
  // * @throws RFWException Caso nenhum provider suportado/conhecido pelo Framework possa ser encontrado.
  // * @Deprecated Este método deve ser inutilizado por manter os caminhos das bibliotexas constantes.
  // */
  // public static void loadProvidersA3Token() throws RFWException {
  // // p = new sun.security.pkcs11.SunPKCS11(getClass().getClassLoader().getResourceAsStream("token_smartcard.cfg"));
  // boolean loaded = false;
  // Provider p = null;
  // p = Security.getProvider("SunPKCS11-SmartCard");
  // if (p == null) {
  // try {
  // p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream("name=SunPKCS11-SmartCard\nlibrary=C:/Windows/SysWOW64/aetpkss1.dll".getBytes()));
  // p.list(System.out);
  // Security.addProvider(p);
  // loaded = true;
  // } catch (ProviderException e) {
  // }
  // } else {
  // loaded = true;
  // }
  // p = Security.getProvider("SunPKCS11-Safesign");
  // if (p == null) {
  // try {
  // p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream("name=Safesign\nlibrary=C:/WINDOWS/system32/eTpkcs11.dll".getBytes()));
  // Security.addProvider(p);
  // loaded = true;
  // } catch (ProviderException e) {
  // }
  // } else {
  // loaded = true;
  // }
  // p = Security.getProvider("SunPKCS11-Safenetikey2032");
  // if (p == null) {
  // try {
  // p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream("name=Safenetikey2032\nlibrary=c:/windows/system32/dkck201.dll".getBytes()));
  // Security.addProvider(p);
  // loaded = true;
  // } catch (ProviderException e) {
  // }
  // } else {
  // loaded = true;
  // }
  // p = Security.getProvider("SunPKCS11-eToken");
  // if (p == null) {
  // try {
  // p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream("name=eToken\nlibrary=C:/WINDOWS/system32/eTpkcs11.dll".getBytes()));
  // Security.addProvider(p);
  // loaded = true;
  // } catch (ProviderException e) {
  // }
  // } else {
  // loaded = true;
  // }
  // p = Security.getProvider("SunPKCS11-FeitianPKCS");
  // if (p == null) {
  // try {
  // p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream("name=FeitianPKCS\nlibrary=c:/windows/system32/ngp11v211.dll\nslot=1".getBytes()));
  // Security.addProvider(p);
  // loaded = true;
  // } catch (ProviderException e) {
  // }
  // } else {
  // loaded = true;
  // }
  // if (!loaded) {
  // throw new RFWWarningException("RFW_ERR_200084");
  // }
  // }

  /**
   * Este método verifica já temos o provider de segurança SSL carregado no sistema. Se não encontrar, iniciamos e carregarmos na VM. Providers necessários para certificados do tipo A1.<br>
   *
   * @throws RFWException Caso nenhum provider suportado/conhecido pelo RFWDeprec possa ser encontrado.
   */
  public static void loadProvidersA1() throws RFWException {
    boolean loaded = false;
    Provider p = null;
    p = Security.getProvider("SunJSSE");
    if (p == null) {
      try {
        p = new com.sun.net.ssl.internal.ssl.Provider();
        Security.addProvider(p);
        loaded = true;
      } catch (ProviderException e) {
      }
    } else {
      loaded = true;
    }
    if (!loaded) {
      throw new RFWWarningException("Falha ao encontrar/carregar o Provider de segurança para certificados A1");
    }
  }

  /**
   * Este método retorna uma string com as informações coletadas do Certificado.
   *
   * @return String com as informações coletadas do Certificado
   * @throws RFWException
   */
  public static String getCertificateInfo(Certificate certificate) throws RFWException {
    StringBuilder buff = new StringBuilder();
    if (certificate instanceof X509Certificate) {
      X509Certificate x509 = (X509Certificate) certificate;

      final String[] subject = x509.getSubjectDN().getName().split(",");
      buff.append("Owner: ").append(subject[0]).append('\n');
      for (int i = 1; i < subject.length; i++) {
        buff.append("      ").append(subject[i]).append('\n');
      }
      final String[] issuerDN = x509.getIssuerDN().getName().split(",");
      buff.append("Issuer: ").append(issuerDN[0]).append('\n');
      for (int i = 1; i < issuerDN.length; i++) {
        buff.append("      ").append(issuerDN[i]).append('\n');
      }
      buff.append("Serial Number: ").append(x509.getSerialNumber()).append('\n');
      buff.append("Valid from: ").append(x509.getNotBefore()).append(" until: ").append(x509.getNotAfter()).append('\n');
      buff.append("Certificate fingerprints: \n");
      try {
        buff.append("\tMD5:").append(BUCert.getMD5FingerPrintFromCertificate(certificate)).append('\n');
      } catch (Exception e) {
        // Só não escreve se não tiver fingerprint md5
      }
      try {
        buff.append("\tSHA1:").append(BUCert.getSHA1FingerPrintFromCertificate(certificate)).append('\n');
      } catch (Exception e) {
        // Só não escreve se não tiver fingerprint sha1
      }
      buff.append("\tSignature algorithm name: ").append(x509.getSigAlgName()).append('\n');
      buff.append("\tVersion: ").append(x509.getVersion()).append('\n');

      // x509.getIssuerX500Principal();
      // x509.getKeyUsage();
      // x509.getNonCriticalExtensionOIDs();
      // x509.getSigAlgOID();
      // x509.getSubjectAlternativeNames();
      // x509.getSubjectX500Principal();
      // x509.getType();
      // x509.getVersion();
    }
    return buff.toString();
  }

  /**
   * Este método retorna uma string com as informações coletadas do Certificado montadas em um Array BiDimensional String[x][y].<br>
   * Onde x tem tamanho indefinido de acordo com a quantidade de parametros encontratos no certificado; e y vai de 0 à 1, sendo 0 o título do atributo e 1 o valor do atributo.
   *
   * @return
   * @throws RFWException
   */
  public static String[][] getCertificateInfoArray(Certificate certificate) throws RFWException {
    String[][] ret = null;
    if (certificate instanceof X509Certificate) {
      X509Certificate x509 = (X509Certificate) certificate;

      final ArrayList<String> topic = new ArrayList<>();
      final ArrayList<String> value = new ArrayList<>();

      topic.add("Owner");
      value.add(x509.getSubjectDN().getName());

      topic.add("Issuer");
      value.add(x509.getIssuerDN().getName());

      topic.add("Serial Number");
      value.add(x509.getSerialNumber().toString());

      topic.add("Valid From");
      value.add(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(x509.getNotBefore()));

      topic.add("Valid To");
      value.add(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(x509.getNotAfter()));

      topic.add("Certificate MD5 Fingerprint");
      value.add(BUCert.getMD5FingerPrintFromCertificate(certificate));

      topic.add("Certificate SHA1 Fingerprint");
      value.add(BUCert.getSHA1FingerPrintFromCertificate(certificate));

      topic.add("Signature Algorithm Name");
      value.add(x509.getSigAlgName());

      topic.add("Version");
      value.add("" + x509.getVersion());

      ret = new String[topic.size()][2];
      for (int i = 0; i < topic.size(); i++) {
        ret[i][0] = topic.get(i);
        ret[i][1] = value.get(i);
      }

    }
    return ret;
  }

  /**
   * Este método retorna uma string com as informações coletadas do Certificado montadas em um Array BiDimensional String[x][y].<br>
   * Onde x tem tamanho indefinido de acordo com a quantidade de parametros encontratos no certificado; e y vai de 0 à 1, sendo 0 o título do atributo e 1 o valor do atributo.<br>
   * A didiferença deste método para o {@link #getCertificateInfoArray(Certificate)} é que este método utiliza a biblioteca BouncyCastle e adentra as informações adicionais do certificado.
   *
   * @return
   * @throws RFWException
   */
  public static String[][] getCertificateExtendedInfoArray(Certificate certificate) throws RFWException {
    String[][] ret = null;
    try {
      if (certificate instanceof X509Certificate) {
        X509Certificate x509 = (X509Certificate) certificate;

        final ArrayList<String> topic = new ArrayList<>();
        final ArrayList<String> value = new ArrayList<>();

        topic.add("Owner");
        value.add(x509.getSubjectDN().getName());

        topic.add("Issuer");
        value.add(x509.getIssuerDN().getName());

        topic.add("Serial Number");
        value.add(x509.getSerialNumber().toString());

        topic.add("Valid From");
        value.add(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(x509.getNotBefore()));

        topic.add("Valid To");
        value.add(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(x509.getNotAfter()));

        topic.add("Certificate MD5 Fingerprint");
        value.add(BUCert.getMD5FingerPrintFromCertificate(certificate));

        topic.add("Certificate SHA1 Fingerprint");
        value.add(BUCert.getSHA1FingerPrintFromCertificate(certificate));

        topic.add("Signature Algorithm Name");
        value.add(x509.getSigAlgName());

        topic.add("Version");
        value.add("" + x509.getVersion());

        // Collection<?> alternativeNames = X509ExtensionUtil.getSubjectAlternativeNames((X509Certificate) certificate);
        Collection<?> alternativeNames = JcaX509ExtensionUtils.getSubjectAlternativeNames((X509Certificate) certificate);
        for (Object alternativeName : alternativeNames) {
          if (alternativeName instanceof ArrayList) {
            ArrayList<?> listOfValues = (ArrayList<?>) alternativeName;
            Object v = listOfValues.get(1);
            if (v instanceof DERSequence) {
              DERSequence derSequence = (DERSequence) v;
              // DERObjectIdentifier derObjectIdentifier = (DERObjectIdentifier) derSequence.getObjectAt(0);
              ASN1ObjectIdentifier asn1ObjectIdentifier = (ASN1ObjectIdentifier) derSequence.getObjectAt(0);
              DERTaggedObject derTaggedObject = (DERTaggedObject) derSequence.getObjectAt(1);
              ASN1Primitive derObject = derTaggedObject.getObject();

              String valueOfTag = "";
              if (derObject instanceof DEROctetString) {
                DEROctetString octet = (DEROctetString) derObject;
                valueOfTag = new String(octet.getOctets());
              } else if (derObject instanceof DERPrintableString) {
                DERPrintableString octet = (DERPrintableString) derObject;
                valueOfTag = new String(octet.getOctets());
              } else if (derObject instanceof DERUTF8String) {
                DERUTF8String str = (DERUTF8String) derObject;
                valueOfTag = str.getString();
              }

              if ((valueOfTag != null) && (!"".equals(valueOfTag))) {
                if (asn1ObjectIdentifier.equals(new ASN1ObjectIdentifier("2.16.76.1.3.2"))) {
                  topic.add("Nome do Responsável");
                  value.add(valueOfTag);
                } else if (asn1ObjectIdentifier.equals(new ASN1ObjectIdentifier("2.16.76.1.3.3"))) {
                  topic.add("CNPJ/CPF");
                  value.add(valueOfTag);
                } else {
                  topic.add("OID [" + asn1ObjectIdentifier + "]");
                  value.add(valueOfTag);
                }
              }
            }
          }
        }

        ret = new String[topic.size()][2];
        for (int i = 0; i < topic.size(); i++) {
          ret[i][0] = topic.get(i);
          ret[i][1] = value.get(i);
        }
      }
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200454", e);
    }
    return ret;
  }

  /**
   * Este método retorna a data de início de vigência (validade) do certificado.
   *
   * @return
   * @throws RFWException
   *           <li>RFW_ERR_000001 - Caso não seja possível lêr a data do certificado.
   */
  public static LocalDateTime getCertificateValidityStart(Certificate certificate) throws RFWException {
    Date dt = null;
    if (certificate instanceof X509Certificate) {
      X509Certificate x509 = (X509Certificate) certificate;
      dt = x509.getNotBefore();
    }
    if (dt == null) {
      throw new RFWValidationException("RFW_ERR_000001");
    }
    return RUDateTime.toLocalDateTime(dt, RFW.getZoneId());
  }

  /**
   * Este método retorna a data de fim de vigência (validade) do certificado.
   *
   * @return
   * @throws RFWException
   *           <li>RFW_ERR_000001 - Caso não seja possível lêr a data do certificado.
   */
  public static LocalDateTime getCertificateValidityEnd(Certificate certificate) throws RFWException {
    Date dt = null;
    if (certificate instanceof X509Certificate) {
      X509Certificate x509 = (X509Certificate) certificate;
      dt = x509.getNotAfter();
    }
    if (dt == null) {
      throw new RFWValidationException("RFW_ERR_000001");
    }
    return RUDateTime.toLocalDateTime(dt, RFW.getZoneId());
  }

  /**
   * Tenta encontrar o CNPJ do certificado.<br>
   * Por padrão o certificado para emissão de NFe tem o CNPJ no OID "2.16.76.1.3.3.", e é esta informação do certificado que este método procura e retorna.
   *
   * @param certificate
   * @return CNPJ sem formatação, ou null caso o valor não seja encontrado no certificado
   * @throws RFWException
   */
  public static String getCertificateCNPJ(Certificate certificate) throws RFWException {
    try {
      // Collection<?> alternativeNames = X509ExtensionUtil.getSubjectAlternativeNames((X509Certificate) certificate);
      Collection<?> alternativeNames = JcaX509ExtensionUtils.getSubjectAlternativeNames((X509Certificate) certificate);
      for (Object alternativeName : alternativeNames) {
        if (alternativeName instanceof ArrayList) {
          ArrayList<?> listOfValues = (ArrayList<?>) alternativeName;
          Object value = listOfValues.get(1);
          if (value instanceof DERSequence) {
            DERSequence derSequence = (DERSequence) value;
            ASN1ObjectIdentifier asn1ObjectIdentifier = (ASN1ObjectIdentifier) derSequence.getObjectAt(0);
            // DERObjectIdentifier derObjectIdentifier = (DERObjectIdentifier) derSequence.getObjectAt(0);
            if (asn1ObjectIdentifier.equals(new ASN1ObjectIdentifier("2.16.76.1.3.3"))) {
              DERTaggedObject derTaggedObject = (DERTaggedObject) derSequence.getObjectAt(1);
              ASN1Primitive derObject = derTaggedObject.getObject();

              String valueOfTag = "";
              if (derObject instanceof DEROctetString) {
                DEROctetString octet = (DEROctetString) derObject;
                valueOfTag = new String(octet.getOctets());
              } else if (derObject instanceof DERPrintableString) {
                DERPrintableString octet = (DERPrintableString) derObject;
                valueOfTag = new String(octet.getOctets());
              } else if (derObject instanceof DERUTF8String) {
                DERUTF8String str = (DERUTF8String) derObject;
                valueOfTag = str.getString();
              }

              if ((valueOfTag != null) && (!"".equals(valueOfTag))) {
                return valueOfTag;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200454", e);
    }
    return null;
  }

  // /**
  // * Tenta encontrar o CNPJ do certificado.<br>
  // * Por padrão o certificado para emissão de NFe tem o CNPJ no OID "2.16.76.1.3.3.", e é esta informação do certificado que este método procura e retorna.
  // *
  // * @param certificate Objeto {@link RFWCertificateVO} com o certificado dentro
  // * @return
  // * @return CNPJ sem formatação, ou null caso o valor não seja encontrado no certificado
  // * @throws RFWException
  // */
  // public static String getCertificateCNPJ(CertificateVO certVO) throws RFWException {
  // KeyStore ks = loadKeyStoreFromPKCS12(new ByteArrayInputStream(certVO.getCertificatefilevo().getFilecontentvo().getContent()), certVO.getPassword());
  // // Recupera chave privada do certificado cliente
  // KeyStore.PrivateKeyEntry pkEntry;
  // try {
  // pkEntry = null;
  // Enumeration<String> aliases = ks.aliases();
  // while (aliases.hasMoreElements()) {
  // String alias = aliases.nextElement();
  // if (ks.isKeyEntry(alias)) {
  // pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(certVO.getPassword().toCharArray()));
  // break;
  // }
  // }
  // } catch (Exception e) {
  // throw new RFWCriticalException("RFW_ERR_100175", e);
  // }
  // X509Certificate cert = (X509Certificate) pkEntry.getCertificate();
  // return getCertificateCNPJ(cert);
  // }

  /**
   * Cria uma Hash do conteúdo com o SHA-256 e o assina com a chave privada do certificado passado. O conteúdo assinado é retornado em uma String de base 64.<br>
   * Este tipo de assinatura é utilizado para gerar o código de vinculação do SAT, favor não alterar seu conteúdo/modo de operação.
   *
   * @return
   */
  public static String signContentSHA256andRSA(final String content, final KeyStore keyStore, final String alias, final String pin) throws RFWException {
    throw new RFWCriticalException("Desde a atualização do BounceCastle, este método precisa de revisão! - signContentSHA256andRSA(...)");
    // try {
    // PrivateKey pKey = (PrivateKey) keyStore.getKey(alias, pin.toCharArray());
    //
    // MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    // messageDigest.update(content.getBytes());
    // byte[] outputDigest = messageDigest.digest();
    //
    // AlgorithmIdentifier sha256Aid = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, null);
    // DigestInfo di = new DigestInfo(sha256Aid, outputDigest);
    //
    // Signature rsaSignature = Signature.getInstance("NONEwithRSA");
    // rsaSignature.initSign(pKey);
    // rsaSignature.update(di.toASN1Object().getEncoded());
    // byte[] signed = rsaSignature.sign();
    // // Codifica na base 64 e remove os "enters" e espaços da string
    // final String finalKey = BUString.encodeBase64(signed).replaceAll("[\r\n ]", "");
    // return finalKey;
    // } catch (Throwable e) {
    // throw new RFWCriticalException("RFW_ERR_200461", e);
    // }
  }

  /**
   * Este método aplica à um {@link HttpsURLConnection} os certificados encontrados no KeyStore (que deve estar no mesmo pacote desta classe em "keystore/keystore.jks").<br>
   * É preciso incluir neste keystore o certificado dos endereços em que será possível realizar uma conexão https.
   *
   * @param conn Conexão a ser preparada para aceitar o HTTPS.
   * @throws RFWException
   */
  public static void configureSSLCertificatesOnConnection(HttpsURLConnection conn) throws RFWException {
    try {
      SSLContext ssl = SSLContext.getInstance("SSL");
      InputStream in = BUCert.class.getClassLoader().getResourceAsStream("keystore/keystore.jks");
      ssl.init(null, BUCert.createTrustManager(in, "RFWSecretPass"), new SecureRandom());
      conn.setSSLSocketFactory(ssl.getSocketFactory());
    } catch (KeyManagementException e) {
      throw new RFWCriticalException("Falha gerenciar o arquivo de certificados.", e);
    } catch (NoSuchAlgorithmException e) {
      throw new RFWCriticalException("Falha ao recuperar o protocolo SSL para conexão.", e);
    }
  }
}
