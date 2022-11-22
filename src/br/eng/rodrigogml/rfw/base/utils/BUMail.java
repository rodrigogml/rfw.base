package br.eng.rodrigogml.rfw.base.utils;

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

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;

/**
 * Description: Classe utilit�ria para conter m�todos de aux�lio do servi�o de e-mail.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 4.2.0 (30/10/2011)
 */
public class BUMail {

  /**
   * Defini��o do protocolo de e-mail.
   */
  public static enum MailProtocol {
    /**
     * Define o protocolo SSL
     */
    SSL,
    /**
     * Define o protocolo TLS
     */
    TLS
  }

  /**
   * Interface para definir um objeto com os campos de e-mails.
   */
  public static interface Mail {

    /**
     * Endere�o do sender da Mensagem.
     */
    public String getFrom();

    /**
     * Array com os endere�os de "Para" quem esta mensagem ser� enviada.
     *
     * @return
     */
    public List<String> getTo();

    /**
     * Array com os endere�os de "C�pia de Carbono" esta mensagem ser� enviada.
     *
     * @return
     */
    public List<String> getCc();

    /**
     * Array com os endere�os de "C�pia de Carbono Oculta" (Blind Carbon Copy) esta mensagem ser� enviada.
     *
     * @return
     */
    public List<String> getBcc();

    /**
     * Texto a ser utilizado no assunto da mensagem.
     *
     * @return
     */
    public String getSubject();

    /**
     * Corpo da mensagem do e-mail compat�vel com o mimeType "text/html".
     *
     * @return
     */
    public String getBody();

  }

  /**
   * Patter dos caracteres aceitos em um e-mail. Tanto na �rea de usu�rio quando de dom�nio.<br>
   * Esse pattern est� incompleto pois n�o aceita os "escape caracteres", por exemplo segundo as especifica��es um email pode ter uma @ como parte do nome do usu�rio deste que seja "escaped" com o caractere \@. Esses caracteres incomuns n�o est�o sendo tratados neste pattern.<Br>
   * <b>Aten��o:</n> N�o incluir neste patter os caracteres de sintaxe. Por exemplo, o e-mail pode ter o nome do usu�rio cercado por aspas, e o dom�nio em forma de ip se cercado por colchetes. Ex: "rodiro leitao"@[10.0.0.1]. Esses caracteres de "entorno" n�o devem ser considerados neste patter, mesmo que a " possa fazer parte do nome do usu�rio como um escaped caracter \", n�o estamos falando dos
   * escapade caracteres, apenas dos caracteres de entorno. Tamb�m n�o colocar nenhum tipo de defini��o de quantidade de repeti��o, deixar apenas a lista de caracteres v�lidos.
   */
  private static final String mailAcceptedChar = "[a-zA-Z0-9\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~\\.]";

  /**
   * Este patter usa o pattern anterior e criar o patter completo de formata��o do e-mail.<br>
   * Este patter ainda n�o aceita os e-mails com escapecaracteres, nem partes envolto de aspas e nem o dom�nio na forma de IPs v4/v6.<br>
   * Tamb�m vale ressaltar que este patter n�o � completo para valida��o, por exemplo, n�o invalida dois pontos seguidos, nem dois hifens ou underscore seguidos, n�o invalida blocos que comecem ou terminem com ponto final, etc. Para validar um endere�o utilize o m�todo {@link #validateMailAddress(String)} ou {@link #isMailAddress(String)}.
   */
  private static final String mailPattern = BUMail.mailAcceptedChar + "+@" + BUMail.mailAcceptedChar + "+";

  private BUMail() {
  }

  /**
   * Recebe uma string e tenta encontrar e-mails nela para retornar. Retorna todos os e-mails encontrados.<br>
   * <Br>
   * e retornar:<br>
   * <br>
   * <li>rodrigogml@gmail.com
   *
   * @return Lista com todos os e-mails encontrados ou uma lista vazia caso n�o encontre nenhum.
   */
  public static List<String> parseMailAddresses(String data) {
    List<String> addresses = new ArrayList<>();

    Pattern pat = Pattern.compile(BUMail.mailPattern);
    Matcher matcher = pat.matcher(data);
    while (matcher.find()) {
      addresses.add(matcher.group(0));
    }

    return addresses;
  }

  /**
   * Recebe uma string e tenta encontrar e-mails nela para retornar. Retorna apenas o primeiro e-mail encontrado.<br>
   * Esta classe foi criada como o prop�sito inicial de interpretar strings vindas nos cabe�alhos dos e-mails, como por exemplo:
   * <li>Rodrigo Leit�o &lt;rodrigogml@gmail.com&gt; <br>
   * <Br>
   * e retornar:<br>
   * <br>
   * <li>rodrigogml@gmail.com
   *
   * @return O primeiro encontrado ou NULL caso n�o encontre nenhum.
   */
  public static String parseMailAddress(String data) {
    String address = null;

    Pattern pat = Pattern.compile(BUMail.mailPattern);
    Matcher matcher = pat.matcher(data);
    if (matcher.find()) {
      address = matcher.group(0);
    }

    return address;
  }

  /**
   * Verifica se a string passada � um e-mail v�lido. Aceita apenas o e-mail e n�o uma string contendo um e-mail! <br>
   * Este m�todo n�o lan�a exce��es com o problema encontrado no e-mail. Para isso utilize o m�todo {@link #validateMailAddress(String)}.
   *
   * @param mail endere�o de e-mail para ser validado
   * @return true caso seja um e-mail v�lido, false caso n�o.
   */
  public static boolean isMailAddress(String mail) {
    boolean result = true;
    try {
      validateMailAddress(mail);
    } catch (RFWException e) {
      result = false;
    }
    return result;
  }

  /**
   * Valida se o endere�o � v�lido de acordo com a RFC822.<br>
   * Note que este m�todo aceita e-mails como:<br>
   * <li>user@[10.9.8.7]
   * <li>user@localhost
   *
   * @param mail
   * @throws RFWException
   */
  public static void validateMailAddressThroughRFC822(String mail) throws RFWException {
    try {
      InternetAddress emailAddr = new InternetAddress(mail);
      emailAddr.validate();
    } catch (AddressException ex) {
      throw new RFWValidationException("O endere�o de e-mail n�o � um endere�o v�lido.");
    }
  }

  /**
   * Valida se a string passada � um e-mail. Aceita apenas o e-mail e n�o uma string contendo um e-mail.
   *
   * @param mail E-mail a ser validado.
   * @throws RFWException lan�a a exce��o com a mensagem do porque o e-mail n�o � v�lido.
   */
  public static void validateMailAddress(String mail) throws RFWException {
    if (mail == null) throw new RFWValidationException("RFW_ERR_200489");

    // 1-Quebramos o valor pela @ para pegar o usu�rio e o domain do e-mail.
    String[] parts = mail.toString().split("@");
    if (parts.length != 2) {
      throw new RFWValidationException("RFW_ERR_200317", new String[] { mail });
    }
    // Pattern geral usado apenas para validar se os caractes usados no e-mail s�o validos
    String generalpatter = mailAcceptedChar + "+";
    // 2- Validamos a parte do usu�rio
    if (!parts[0].matches(generalpatter)) {
      throw new RFWValidationException("RFW_ERR_200318", new String[] { mail });
    }
    if (parts[0].indexOf("..") >= 0) {
      throw new RFWValidationException("RFW_ERR_200319", new String[] { mail });
    }
    if (parts[0].charAt(0) == '.' || parts[0].charAt(parts[0].length() - 1) == '.') {
      throw new RFWValidationException("RFW_ERR_200320", new String[] { mail });
    }
    if (parts[0].length() > 64) {
      throw new RFWValidationException("RFW_ERR_200322", new String[] { mail });
    }

    // 3-Validamos o domain
    if (!parts[1].matches(generalpatter)) {
      throw new RFWValidationException("RFW_ERR_200318", new String[] { mail });
    }
    if (parts[1].indexOf("..") >= 0) {
      throw new RFWValidationException("RFW_ERR_200319", new String[] { mail });
    }
    if (parts[1].indexOf("--") >= 0) {
      throw new RFWValidationException("RFW_ERR_200325", new String[] { mail });
    }
    if (parts[1].charAt(0) == '.' || parts[1].charAt(parts[1].length() - 1) == '.') {
      throw new RFWValidationException("RFW_ERR_200321", new String[] { mail });
    }
    if (parts[1].length() > 253) {
      throw new RFWValidationException("RFW_ERR_200323", new String[] { mail });
    }
    // Validamos se todos os dom�nios do e-mail t�m menos de 63 caracteres.
    String[] domainparts = parts[1].split("\\.");
    for (int i = 0; i < domainparts.length; i++) {
      if (domainparts[i].length() > 63) {
        throw new RFWValidationException("RFW_ERR_200324", new String[] { mail });
      }
      if (domainparts[i].charAt(0) == '-' || domainparts[i].charAt(domainparts[i].length() - 1) == '-') {
        throw new RFWValidationException("RFW_ERR_200326", new String[] { mail });
      }
    }
    // Valida se a parte mais a direia � s� alfab�tica
    if (!domainparts[domainparts.length - 1].matches("[A-Za-z]+")) {
      throw new RFWValidationException("RFW_ERR_200327", new String[] { mail });
    }
  }

  /**
   * Espera uma string em alguns dos formatos suportados e extrai o nome da pessoa no e-mail.<br>
   * Formatos suportados:<br>
   * <li>Rodrigo Leit�o &lt;rodrigogml@gmail.com&gt; <br>
   * <br>
   *
   * @return Apenas o nome, dos exemplos acima retornaria "Rodrigo Leit�o"
   */
  public static String parseMailName(String data) {
    // Verifica se est� no primeiro padr�o apresentado no javadoc
    String name = null;
    if (data.matches("(([^\\@\\<\\>])*)\\<[\\w-]+(\\.[\\w-]+)*@[A-Za-z0-9-]+(\\.[\\w-]+)*(\\.[A-Za-z]{2,3})\\>")) {
      String patternname = "[^\\@\\<\\>]*";

      Pattern pat = Pattern.compile(patternname);
      Matcher matcher = pat.matcher(data);
      if (matcher.find()) {
        name = matcher.group();
      }
    }
    if (name != null) {
      name = name.trim();
    }
    return name;
  }

  /**
   * Procura um Resource no ClassPath, l� seu conte�do e substitu� vari�veis no formado ${variableName} pelos valores.
   *
   * @param templateResourceName Nome do recurso que servir� de template. Deve conter o caminho completo desde a "raiz" do pacote em que o arquivo se encontrar.<br>
   *          <b>ATEN��O:</B> Os templates devem sempre ser salvos com o Charset UTF-8 para que n�o ocorram erros nos caracteres.
   * @param fieldContents HashMap com as vari�vies que ser�o substitu�das no template. A chava da hash deve ser o nome da vari�vel. COLOCAR APENAS O NOME! N�O UTILIZAR OS ENTORNOS ${} NA HASH, UTILIZA-LOS S� NO TEMPLATE.
   * @return Conte�do do template com as vari�veis passadas substitu�das.
   * @throws RFWException
   */
  public static String loadMessageTemplate(String templateResourceName, HashMap<String, String> fieldContents) throws RFWException {
    String content = BUIO.readToString(BUReflex.getResourceAsStream(templateResourceName), StandardCharsets.UTF_8);
    for (Entry<String, String> entry : fieldContents.entrySet()) {
      content = BUString.replaceAll(content, "${" + entry.getKey() + "}", entry.getValue());
    }
    return content;
  }

  public static void sendMail(final MailProtocol protocol, final String host, final String port, final String accountLogin, final String password, Mail mail) throws RFWException {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", host); // "smtp.gmail.com");
    props.put("mail.smtp.port", port); // TLS:"587" / SSL:"465");
    switch (protocol) {
      case SSL:
        props.put("mail.smtp.socketFactory.port", port); // "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        break;
      case TLS:
        props.put("mail.smtp.starttls.enable", "true");
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

      // Escreve a Mensagem no objeto do JavaMail
      writeMessage(mail, message);

      Transport.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  private static void writeMessage(Mail mail, MimeMessage message) throws MessagingException, AddressException {
    message.setFrom(new InternetAddress(mail.getFrom()));

    // Address[] toUser = InternetAddress.parse("seuamigo@gmail.com, seucolega@hotmail.com, seuparente@yahoo.com.br");
    if (mail.getTo() != null) {
      for (String address : mail.getTo())
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
    }
    if (mail.getCc() != null) {
      for (String address : mail.getCc())
        message.addRecipient(Message.RecipientType.CC, new InternetAddress(address));
    }
    if (mail.getBcc() != null) {
      for (String address : mail.getBcc())
        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(address));
    }

    message.setSubject(mail.getSubject());
    // message.setText(vo.getBody());

    message.setContent(mail.getBody(), "text/html");
    {
      // MimeMultipart multipart = new MimeMultipart("related");
      //
      // // Cria o conte�do do corpo do e-mail e adiciona ao "MimeMultipart"
      // BodyPart body = new MimeBodyPart();
      // body.setContent(vo.getBody(), "text/html");
      // multipart.addBodyPart(body);
      //
      // message.setContent(multipart);
    }

  }

}
