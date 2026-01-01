package br.eng.rodrigogml.rfw.base.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.utils.RUIO;
import br.eng.rodrigogml.rfw.kernel.utils.RUReflex;
import br.eng.rodrigogml.rfw.kernel.utils.RUString;

/**
 * Description: Classe utilitária para conter métodos de auxílio do serviço de e-mail.<BR>
 *
 * @author Rodrigo Leitão
 * @since 4.2.0 (30/10/2011)
 */
public class BUMail {

  /**
   * Patter dos caracteres aceitos em um e-mail. Tanto na área de usuário quando de domínio.<br>
   * Esse pattern está incompleto pois não aceita os "escape caracteres", por exemplo segundo as especificações um email pode ter uma @ como parte do nome do usuário deste que seja "escaped" com o caractere \@. Esses caracteres incomuns não estão sendo tratados neste pattern.<Br>
   * <b>Atenção:</n> Não incluir neste patter os caracteres de sintaxe. Por exemplo, o e-mail pode ter o nome do usuário cercado por aspas, e o domínio em forma de ip se cercado por colchetes. Ex: "rodiro leitao"@[10.0.0.1]. Esses caracteres de "entorno" não devem ser considerados neste patter, mesmo que a " possa fazer parte do nome do usuário como um escaped caracter \", não estamos falando dos
   * escapade caracteres, apenas dos caracteres de entorno. Também não colocar nenhum tipo de definição de quantidade de repetição, deixar apenas a lista de caracteres válidos.
   */
  private static final String mailAcceptedChar = "[a-zA-Z0-9\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~\\.]";

  /**
   * Pattern simplificado para localização de endereços de e-mail em um texto.<br>
   * Não deve ser utilizado para validação formal; use {@link #validateMailAddress(String)} para validação completa.
   */
  private static final String mailPattern = BUMail.mailAcceptedChar + "+@" + BUMail.mailAcceptedChar + "+";

  private static final Pattern MAIL_ADDRESS_PATTERN = Pattern.compile(mailPattern);
  private static final Pattern NAME_WITH_ADDRESS_PATTERN = Pattern.compile("^\\s*\\\"?\\s*([^<>\\\"]*?)\\s*\\\"?\\s*<" + mailPattern + ">\\s*$");
  private static final java.nio.charset.Charset DEFAULT_TEMPLATE_CHARSET = StandardCharsets.UTF_8;

  /**
   * Construtor privado para classe estática.
   */
  private BUMail() {
  }

  /**
   * Definição do protocolo de e-mail.
   */
  public static enum MailProtocol {
    /** Define o protocolo SSL */
    SSL,
    /** Define o protocolo TLS */
    TLS
  }

  /**
   * Interface para definir um objeto com os campos de e-mails.
   */
  public static interface Mail {

    /** Endereço do remetente da mensagem. */
    public String getFrom();

    /** Lista de destinatários principais (campo Para). */
    public List<String> getTo();

    /** Lista de destinatários em cópia de carbono (CC). */
    public List<String> getCc();

    /** Lista de destinatários em cópia oculta (BCC). */
    public List<String> getBcc();

    /** Assunto da mensagem. */
    public String getSubject();

    /** Corpo da mensagem, compatível com o mimeType "text/html". */
    public String getBody();

  }

  /**
   * Valida se o endereço é válido de acordo com a RFC822.<br>
   *
   * @param mail
   * @throws RFWException
   */
  public static void validateMailAddress(String mail) throws RFWException {
    if (mail == null) throw new RFWValidationException("RFW_000018");
    try {
      InternetAddress address = new InternetAddress(mail, true);
      address.validate();
    } catch (AddressException ex) {
      throw new RFWValidationException("O endereço de e-mail não é um endereço válido.", ex);
    }
  }

  /**
   * Localiza todos os endereços de e-mail presentes no texto informado.
   *
   * @param data texto que poderá conter endereços de e-mail.
   * @return lista com todos os e-mails encontrados ou lista vazia caso nenhum seja localizado.
   */
  public static List<String> parseMailAddresses(String data) {
    List<String> addresses = new ArrayList<>();
    if (data == null || data.isEmpty()) return addresses;

    Matcher matcher = MAIL_ADDRESS_PATTERN.matcher(data);
    while (matcher.find()) {
      addresses.add(matcher.group(0));
    }

    return addresses;
  }

  /**
   * Recupera o primeiro endereço de e-mail encontrado no texto informado.
   *
   * @param data texto que poderá conter um endereço de e-mail.
   * @return endereço encontrado ou {@code null} quando não houver correspondência.
   */
  public static String parseMailAddress(String data) {
    List<String> addresses = parseMailAddresses(data);
    return addresses.isEmpty() ? null : addresses.get(0);
  }

  /**
   * Tenta extrair o nome do remetente de uma string contendo um endereço de e-mail.<br>
   * Formatos suportados:<br>
   * <li>Rodrigo Leitão &lt;rodrigogml@gmail.com&gt;</li>
   *
   * @param data texto contendo o nome e um endereço de e-mail.
   * @return nome sem o endereço ou {@code null} caso nenhum nome seja reconhecido.
   */
  public static String parseMailName(String data) {
    if (data == null) return null;

    try {
      InternetAddress[] addresses = InternetAddress.parse(data, false);
      if (addresses.length > 0) {
        String personal = addresses[0].getPersonal();
        if (personal != null && !personal.trim().isEmpty()) {
          return personal.trim();
        }
      }
    } catch (AddressException ignored) {
      // Fallback para regex quando o parse estrito falhar com entradas parciais.
    }

    Matcher matcher = NAME_WITH_ADDRESS_PATTERN.matcher(data);
    if (matcher.matches()) {
      String name = matcher.group(1).trim();
      return name.isEmpty() ? null : name;
    }
    return null;
  }

  /**
   * Procura um Resource no ClassPath, lê seu conteúdo e substituí variáveis no formado ${variableName} pelos valores informados.
   *
   * @param templateResourceName Nome do recurso que servirá de template. Deve conter o caminho completo desde a "raiz" do pacote em que o arquivo se encontrar.<br>
   *          <b>ATENÇÃO:</B> Os templates devem sempre ser salvos com o charset UTF-8 para evitar problemas com acentuação e demais caracteres.
   * @param fieldContents HashMap com as variáveis que serão substituídas no template. A chave da hash deve ser o nome da variável. Informar apenas o nome, sem o entorno ${}.
   * @return Conteúdo do template com as variáveis passadas substituídas.
   * @throws RFWException em caso de falha ao ler o recurso ou substituir variáveis.
   */
  public static String loadMessageTemplate(String templateResourceName, HashMap<String, String> fieldContents) throws RFWException {
    String content;
    try (InputStream templateStream = RUReflex.getResourceAsStream(templateResourceName)) {
      content = RUIO.toString(templateStream, DEFAULT_TEMPLATE_CHARSET);
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao carregar template de email! '${0}'.", new String[] { templateResourceName });
    }

    if (fieldContents != null) {
      for (Entry<String, String> entry : fieldContents.entrySet()) {
        content = RUString.replaceAll(content, "${" + entry.getKey() + "}", entry.getValue());
      }
    }
    return content;
  }

  /**
   * Envia um e-mail utilizando JavaMail com autenticação e suporte a SSL ou TLS.
   *
   * @param protocol protocolo a ser utilizado para a conexão.
   * @param host host do servidor SMTP.
   * @param port porta do servidor SMTP.
   * @param accountLogin usuário utilizado na autenticação.
   * @param password senha utilizada na autenticação.
   * @param mail objeto contendo os dados do e-mail a ser enviado.
   * @throws RFWException em caso de falha na montagem ou envio do e-mail.
   */
  public static void sendMail(final MailProtocol protocol, final String host, final String port, final String accountLogin, final String password, Mail mail) throws RFWException {
    if (mail == null) throw new RFWValidationException("Dados do e-mail não informados.");

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", port);

    switch (protocol) {
      case SSL:
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        break;
      case TLS:
        props.put("mail.smtp.starttls.enable", "true");
        break;
      default:
        break;
    }

    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(accountLogin, password);
      }
    });

    try {
      MimeMessage message = new MimeMessage(session);
      writeMessage(mail, message);
      Transport.send(message);
    } catch (MessagingException e) {
      throw new RFWValidationException("Falha ao enviar e-mail.", e);
    }
  }

  private static void writeMessage(Mail mail, MimeMessage message) throws MessagingException, AddressException {
    message.setFrom(new InternetAddress(mail.getFrom()));

    addRecipients(message, Message.RecipientType.TO, mail.getTo());
    addRecipients(message, Message.RecipientType.CC, mail.getCc());
    addRecipients(message, Message.RecipientType.BCC, mail.getBcc());

    message.setSubject(mail.getSubject());
    message.setContent(mail.getBody(), "text/html");
  }

  private static void addRecipients(MimeMessage message, Message.RecipientType type, List<String> recipients) throws MessagingException {
    if (recipients == null) return;

    for (String address : recipients) {
      if (address != null && !address.trim().isEmpty()) {
        message.addRecipient(type, new InternetAddress(address.trim()));
      }
    }
  }
}
