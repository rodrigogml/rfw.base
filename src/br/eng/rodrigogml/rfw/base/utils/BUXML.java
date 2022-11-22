package br.eng.rodrigogml.rfw.base.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;

/**
 * Description: Esta classe utilitária tem a finalidade de agregar métodos para facilitar a vida ao trabalhar com estruturas de XML.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (11 de out de 2016)
 */
public final class BUXML {

  private BUXML() {
    // Construtor privado para classe estática
  }

  /**
   * Transforma um XML em uma estrutura DOM para navegar entre os valores do XML.<br>
   * Este método normaliza o conteúdo do XML, isto é, evita que nos casos em que o XML esteja desta forma:<br>
   * <b>&lt;foo%gt;hello<br>
   * wor<br>
   * ld&lt;/foo&gt;</b><br>
   * <br>
   * Seja interpretado como:<br>
   * <b>Element foo<br>
   * <ul>
   * Text node: ""<br>
   * Text node: "Hello "<br>
   * Text node: "wor"<br>
   * Text node: "ld"<br>
   * </ul>
   * </b> <br>
   * e sim como:<br>
   *
   * <b>Element foo<br>
   * <ul>
   * Text node: "Hello world"
   * </ul>
   * </b>
   *
   * <br>
   * <br>
   * ATENÇÃO: No caso de múltiplos acessos ao mesmo XML, considere criar um único documento DOM e passar diretamente nos métodos ao invés de forçar o sistema a realizar o parser múltiplas vezes, atrapalhando a performance do sistema.
   *
   * @param xml Conteúdo no formato XML.
   * @return Objeto DOM
   * @throws RFWException
   */
  public static Document parseXMLToDOMDocumentNormalized(String xml) throws RFWException {
    try {
      return parseXMLToDOMDocumentNormalized(xml.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RFWCriticalException("Falha ao realizar o Parser do XML.", e);
    }
  }

  /**
   * Transforma um XML em uma estrutura DOM para navegar entre os valores do XML.<br>
   * Este método normaliza o conteúdo do XML, isto é, evita que nos casos em que o XML esteja desta forma:<br>
   * <b>&lt;foo%gt;hello<br>
   * wor<br>
   * ld&lt;/foo&gt;</b><br>
   * <br>
   * Seja interpretado como:<br>
   * <b>Element foo<br>
   * <ul>
   * Text node: ""<br>
   * Text node: "Hello "<br>
   * Text node: "wor"<br>
   * Text node: "ld"<br>
   * </ul>
   * </b> <br>
   * e sim como:<br>
   *
   * <b>Element foo<br>
   * <ul>
   * Text node: "Hello world"
   * </ul>
   * </b>
   *
   * <br>
   * <br>
   * ATENÇÃO: No caso de múltiplos acessos ao mesmo XML, considere criar um único documento DOM e passar diretamente nos métodos ao invés de forçar o sistema a realizar o parser múltiplas vezes, atrapalhando a performance do sistema.
   *
   * @param bytes Conteúdo no formato XML em bytes no encoding UTF-8.
   * @return Objeto DOM
   * @throws RFWException
   */
  public static Document parseXMLToDOMDocumentNormalized(byte[] bytes) throws RFWException {
    return parseXMLToDOMDocumentNormalized(new ByteArrayInputStream(bytes));
  }

  /**
   * Transforma um XML em uma estrutura DOM para navegar entre os valores do XML.<br>
   * Este método normaliza o conteúdo do XML, isto é, evita que nos casos em que o XML esteja desta forma:<br>
   * <b>&lt;foo%gt;hello<br>
   * wor<br>
   * ld&lt;/foo&gt;</b><br>
   * <br>
   * Seja interpretado como:<br>
   * <b>Element foo<br>
   * <ul>
   * Text node: ""<br>
   * Text node: "Hello "<br>
   * Text node: "wor"<br>
   * Text node: "ld"<br>
   * </ul>
   * </b> <br>
   * e sim como:<br>
   *
   * <b>Element foo<br>
   * <ul>
   * Text node: "Hello world"
   * </ul>
   * </b>
   *
   * <br>
   * <br>
   * ATENÇÃO: No caso de múltiplos acessos ao mesmo XML, considere criar um único documento DOM e passar diretamente nos métodos ao invés de forçar o sistema a realizar o parser múltiplas vezes, atrapalhando a performance do sistema.
   *
   * @param input
   *
   * @param bytes Conteúdo no formato XML em bytes no encoding UTF-8.
   * @return Objeto DOM
   * @throws RFWException
   */
  public static Document parseXMLToDOMDocumentNormalized(InputStream input) throws RFWException {
    final Document doc;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input, "UTF-8");
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200441", e);
    }
    doc.getDocumentElement().normalize();
    return doc;
  }

  /**
   * Este método cria uma árvore DOM nova a partir "do zero". Que pode ser usada para começar a escrita de um novo XML.
   *
   * @return
   * @throws RFWExecption
   */
  public static Document createNewDocument() throws RFWException {
    try {
      DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder icBuilder = icFactory.newDocumentBuilder();
      Document doc = icBuilder.newDocument();
      return doc;
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200445", e);
    }
  }

  /**
   * Este método cria uma árvore DOM nova (um novo documento) a partir de algum nó de outra árvore DOM.
   *
   * @param externalNode Nó da árvore externa que será a tag raiz do novo documento.
   * @return
   * @throws RFWExecption
   */
  public static Document createNewDocument(Node externalNode) throws RFWException {
    final Document newDoc = createNewDocument();
    final Node newNode = newDoc.importNode(externalNode, true);
    newDoc.appendChild(newNode);
    return newDoc;
  }

  /**
   * Cria um novo elemento raiz no XML.
   *
   * @param doc Árvore XML para criar o documento.
   * @param elementName Nome do Elemento (Tag) a ser criado.
   * @return Elemento/Tag criado
   * @throws RFWException
   */
  public static Element createElementTag(Document doc, String elementName) throws RFWException {
    final Element elem = doc.createElement(elementName);
    doc.appendChild(elem);
    return elem;
  }

  /**
   * Cria um novo elemento raiz no XML com uma definição de name space (cria o atributo "xmlns").
   *
   * @param doc Árvore XML para criar o documento.
   * @param elementName Nome do Elemento (Tag) a ser criado.
   * @param nameSpace NameSpace a ser atribuido neste elento (atributo 'xmlns')
   * @return Elemento/Tag criado
   * @throws RFWException
   */
  public static Element createElementTag(Document doc, String elementName, String nameSpace) throws RFWException {
    final Element elem = doc.createElement(elementName);
    doc.appendChild(elem);
    BUXML.addAttribute(elem, "xmlns", nameSpace);
    return elem;
  }

  /**
   * Cria um novo elemento filho no elemento passado.
   *
   * @param doc Árvore XML para criar o documento.
   * @param parentTag Elemento da tag pai, onde o novo elemento será adicionado.
   * @param elementName Nome do Elemento (Tag) a ser criado.
   * @return Elemento/Tag criado
   * @throws RFWException
   */
  public static Element createElementTag(Document doc, Element parentTag, String elementName) throws RFWException {
    final Element elem = doc.createElement(elementName);
    parentTag.appendChild(elem);
    return elem;
  }

  /**
   * Cria um elemento já com um conteúdo de texto dentro de suas Tags.
   *
   * @param doc Documento DOM
   * @param parentTag
   * @param elementName
   * @param content
   * @return retorna o elemento filho criado
   * @throws RFWException
   */
  public static Element createElementTagWithTextContent(Document doc, Element parentTag, String elementName, String content) throws RFWException {
    final Element elem = createElementTag(doc, parentTag, elementName);
    if (content != null) elem.appendChild(doc.createTextNode(content));
    return elem;
  }

  /**
   * Define um atributo em uma tag já existente.
   *
   * @param tag TAG a receber o atributo
   * @param attribute Nome do Atributo
   * @param value Valor do atributo
   * @throws RFWException
   */
  public static void addAttribute(Element tag, String attribute, String value) throws RFWException {
    tag.setAttribute(attribute, value);
  }

  /**
   * Este método recebe um documento DOM e retorna seu conteúdo escrito diretamente em uma String.
   *
   * @param doc Árvore DOM
   * @return String com todo o seu conteúdo escrito.
   * @throws RFWException
   */
  public static String writeDOMToString(Document doc) throws RFWException {
    return writeDOMToString(doc, false);
  }

  /**
   * Este método recebe um documento DOM e retorna seu conteúdo escrito diretamente em uma String.
   *
   * @param doc Árvore DOM
   * @param omitXMLDeclaration Se true, o resultado do XML não terá a deração inicial: "<?xml version="1.0" encoding="UTF-8"?>"
   * @return String com todo o seu conteúdo escrito.
   * @throws RFWException
   */
  public static String writeDOMToString(Document doc, boolean omitXMLDeclaration) throws RFWException {
    try {
      final Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      if (omitXMLDeclaration) transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      final DOMSource source = new DOMSource(doc);
      final StringWriter writer = new StringWriter();
      transformer.transform(source, new StreamResult(writer));
      return writer.getBuffer().toString().replaceAll("\n|\r", "");
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200446", e);
    }
  }

  /**
   * Este método recebe um nó da árvore DOM e retorna seu conteúdo escrito diretamente em uma String.
   *
   * @param node nó da Árvore DOM
   * @return String com todo o seu conteúdo escrito.
   * @throws RFWException
   */
  public static String writeNodeToString(Node node) throws RFWException {
    return writeNodeToString(node, false);
  }

  /**
   * Este método recebe um documento DOM e retorna seu conteúdo escrito diretamente em uma String.
   *
   * @param node Árvore DOM
   * @param omitXMLDeclaration Se true, o resultado do XML não terá a deração inicial: "<?xml version="1.0" encoding="UTF-8"?>"
   * @return String com todo o seu conteúdo escrito.
   * @throws RFWException
   */
  public static String writeNodeToString(Node node, boolean omitXMLDeclaration) throws RFWException {
    try {
      final Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      if (omitXMLDeclaration) transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      final DOMSource source = new DOMSource(node);
      final StringWriter writer = new StringWriter();
      transformer.transform(source, new StreamResult(writer));
      return writer.getBuffer().toString().replaceAll("\n|\r", "");
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200446", e);
    }
  }

  /**
   * Retorna o NodeList da árvore DOM baseado no caminho fornecido. O caminho deve estar no padrão XPath.<br>
   * <ul>
   * <li>"/nfeProc/NFe/infNFe/ide/NFref" - Começe com a barra para indicar desde o objeto raiz;</li>
   * <li>"NFe/infNFe/ide/NFref" - comece sem a barra para indicar a partir do objeto (node) atual.</li>
   * <li>"/CFe/infCFe/ide/nCFe"</li>
   * <li>"/CFe/infCFe[1]/ide[3]/nCFe"</li>
   * <li>"/howto/topic[@name='PowerBuilder']/url[2]/text()"</li>
   * <li>"/howto/topic/url/text/@nItem"</li>
   * </ul>
   * <br>
   *
   * @param doc
   * @param xPath
   * @return NodeList de acordo com o caminho passado.
   * @throws RFWException
   */
  public static NodeList getNodeList(Document doc, String xPath) throws RFWException {
    try {
      return (NodeList) XPathFactory.newInstance().newXPath().evaluate(xPath, doc.getDocumentElement(), XPathConstants.NODESET);
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200442", new String[] { xPath }, e);
    }
  }

  /**
   * Retorna o NodeList relativo à um nó da árvore DOM baseado no caminho fornecido. O caminho deve estar no padrão XPath.<br>
   * <ul>
   * <li>"/nfeProc/NFe/infNFe/ide/NFref" - Começe com a barra para indicar desde o objeto raiz;</li>
   * <li>"NFe/infNFe/ide/NFref" - comece sem a barra para indicar a partir do objeto (node) atual.</li>
   * <li>"/CFe/infCFe/ide/nCFe"</li>
   * <li>"/CFe/infCFe[1]/ide[3]/nCFe"</li>
   * <li>"/howto/topic[@name='PowerBuilder']/url[2]/text()"</li>
   * <li>"/howto/topic/url/text/@nItem"</li>
   * </ul>
   * <br>
   * <B>ATENÇÃO:</B> Node que o caminho relativo não começa com "/", pois isso indica raiz, comece o XPath a partir do nó passado em Node!
   *
   * @param node
   * @param xPath
   * @return NodeList de acordo com o caminho passado.
   * @throws RFWException
   */
  public static NodeList getNodeList(Node node, String xPath) throws RFWException {
    try {
      return (NodeList) XPathFactory.newInstance().newXPath().evaluate(xPath, node, XPathConstants.NODESET);
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200442", new String[] { xPath }, e);
    }
  }

  /**
   * Recupera o primeiro nó do caminho.
   *
   * <ul>
   * <li>"/nfeProc/NFe/infNFe/ide/NFref" - Começe com a barra para indicar desde o objeto raiz;</li>
   * <li>"NFe/infNFe/ide/NFref" - comece sem a barra para indicar a partir do objeto (node) atual.</li>
   * <li>"/CFe/infCFe/ide/nCFe"</li>
   * <li>"/CFe/infCFe[1]/ide[3]/nCFe"</li>
   * <li>"/howto/topic[@name='PowerBuilder']/url[2]/text()"</li>
   * <li>"/howto/topic/url/text/@nItem"</li>
   * </ul>
   *
   * @param node Nó de referência
   * @param xPath Caminho XPath para procurar
   * @return
   * @throws RFWException
   */
  public static Node getFirstNode(Node node, String xPath) throws RFWException {
    NodeList nodeList = getNodeList(node, xPath);
    if (nodeList != null && nodeList.getLength() > 0) return nodeList.item(0);
    return null;
  }

  /**
   * Recupera o primeiro nó do caminho.
   *
   * <ul>
   * <li>"/nfeProc/NFe/infNFe/ide/NFref" - Começe com a barra para indicar desde o objeto raiz;</li>
   * <li>"NFe/infNFe/ide/NFref" - comece sem a barra para indicar a partir do objeto (node) atual.</li>
   * <li>"/CFe/infCFe/ide/nCFe"</li>
   * <li>"/CFe/infCFe[1]/ide[3]/nCFe"</li>
   * <li>"/howto/topic[@name='PowerBuilder']/url[2]/text()"</li>
   * <li>"/howto/topic/url/text/@nItem"</li>
   * </ul>
   *
   * @param doc Árvore DOM para pesquisa.
   * @param xPath Caminho XPath para procurar
   * @return
   * @throws RFWException
   */
  public static Node getFirstNode(Document doc, String xPath) throws RFWException {
    NodeList nodeList = getNodeList(doc, xPath);
    if (nodeList != null && nodeList.getLength() > 0) return nodeList.item(0);
    return null;
  }

  /**
   * Recupera o conteúdo de texto dentro do conteúdo de uma TAG do XML baseado no seu xPath.<br>
   * Exemplos de XPath:<br>
   * <ul>
   * <li>"/nfeProc/NFe/infNFe/ide/NFref" - Começe com a barra para indicar desde o objeto raiz;</li>
   * <li>"NFe/infNFe/ide/NFref" - comece sem a barra para indicar a partir do objeto (node) atual.</li>
   * <li>"/CFe/infCFe/ide/nCFe"</li>
   * <li>"/CFe/infCFe[1]/ide[3]/nCFe"</li>
   * <li>"/howto/topic[@name='PowerBuilder']/url[2]/text()"</li>
   * <li>"/howto/topic/url/text/@nItem"</li>
   * </ul>
   * <br>
   *
   * @param doc
   * @param xPath
   * @return
   * @throws RFWException
   */
  public static String getTextContent(Document doc, String xPath) throws RFWException {
    String value = null;
    final NodeList nodeList = getNodeList(doc, xPath);
    if (nodeList != null && nodeList.getLength() >= 1) {
      value = nodeList.item(0).getTextContent();
    }
    return value;
  }

  /**
   * Recupera o conteúdo de texto dentro do conteúdo de uma TAG do XML baseado no seu xPath.<br>
   *
   * <ul>
   * <li>"/nfeProc/NFe/infNFe/ide/NFref" - Começe com a barra para indicar desde o objeto raiz;</li>
   * <li>"NFe/infNFe/ide/NFref" - comece sem a barra para indicar a partir do objeto (node) atual.</li>
   * <li>"/CFe/infCFe/ide/nCFe"</li>
   * <li>"/CFe/infCFe[1]/ide[3]/nCFe"</li>
   * <li>"/howto/topic[@name='PowerBuilder']/url[2]/text()"</li>
   * <li>"/howto/topic/url/text/@nItem"</li>
   * </ul>
   * <br>
   * <B>ATENÇÃO:</B> Node que o caminho relativo não começa com "/", pois isso indica raiz, comece o XPath a partir do nó passado em Node!
   *
   * @param node
   * @param xPath
   * @return
   * @throws RFWException
   */
  public static String getTextContent(Node node, String xPath) throws RFWException {
    String value = null;
    final NodeList nodeList = getNodeList(node, xPath);
    if (nodeList != null && nodeList.getLength() >= 1) {
      value = nodeList.item(0).getTextContent();
    }
    return value;
  }

  /**
   * Lê um XML pronto e importa a tag rais e toda sua estrutura como uma tag filha dentro do documento passado.
   *
   * @param doc Documento DOM já criado e existente.
   * @param tag Tag pai onde o XML será incluso como estrutura filha.
   * @param xml XML a ser importando no documento.
   * @throws RFWException
   */
  public static void createElementTagFromXML(Document doc, Element tag, String xml) throws RFWException {
    final Document newDoc = parseXMLToDOMDocumentNormalized(xml);
    final Node rootElement = newDoc.getFirstChild();
    final Node newNode = doc.importNode(rootElement, true);
    tag.appendChild(newNode);
  }

  /**
   * Importa a tag raiz de outra árvode DOM e toda sua estrutura como uma tag filha dentro do documento passado.
   *
   * @param doc Documento DOM já criado e existente.
   * @param tag Tag pai onde o XML será incluso como estrutura filha.
   * @param childDoc Árvore DOM a ser importada como tag filha.
   * @throws RFWException
   */
  public static void createElementTagFromDOMRoot(Document doc, Element tag, Document childDoc) throws RFWException {
    final Node rootElement = childDoc.getFirstChild();
    final Node newNode = doc.importNode(rootElement, true);
    tag.appendChild(newNode);
  }

  /**
   * Importa um elemento de outra árvore DOM e toda sua estrutura como uma tag filha dentro do documento passado.
   *
   * @param doc Documento DOM já criado e existente.
   * @param tag Tag pai onde o XML será incluso como estrutura filha.
   * @param newElement Elemento da outra árvore que deve ser importado como tag filha.
   * @throws RFWException
   */
  public static void createElementTagFromExternalNode(Document doc, Node tag, Node newElement) throws RFWException {
    final Node newNode = doc.importNode(newElement, true);
    tag.appendChild(newNode);
  }

  /**
   * Este método faz a validação de um XML contra seu Schema (XSD).
   *
   * @param xml Conteúdo do XML que será validado.
   * @param schemapath Caminho para o XSD de acordo com o caminho passado no atributo basePath (ou somente o nome do arquivo, caso ele já esteja na pasta do basePath)
   * @param basepath Caminho base para os XSD de validação. Útil quando o XML faz referência para outros XSD ao invés de ter todo o schema dentro do mesmo arquivo.
   * @throws RFWException
   */
  public static void validateXMLAgainstSchema2(String xml, String schemapath, String basepath) throws RFWException {
    try {
      InputStream in = BUXML.class.getResourceAsStream(basepath + schemapath); // Lê durante o deploy "unexploded" do ear no eclipse (produção)
      if (in == null) {
        in = BUXML.class.getClassLoader().getResourceAsStream(basepath + schemapath); // Lê durante o deploy "exploded" do ear no eclipse (desenvolvimento)
      }

      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      factory.setResourceResolver(new SchemaResourceResolver(basepath));
      Schema schema = factory.newSchema(new StreamSource(in));
      Validator validator = schema.newValidator();

      validator.validate(new StreamSource(new StringReader(xml)));
    } catch (SAXException e) {
      throw new RFWCriticalException("RFW_ERR_200486", e);
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200487", e);
    }
  }
}

/**
 * Description: Esta classe carrega os XSDs conforme o validador necessita. Foi necessário implementar essa classe para buscar os XSD importados pelos outros XSDs. Já que o validador não encontra eles sozinhos dentro do JAR.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.0.0 (27/01/2011)
 */
class SchemaResourceResolver implements LSResourceResolver {

  /**
   * Define o caminho base para encontrar os XSD dependentes.
   */
  private String basepath = null;

  public SchemaResourceResolver(String basepath) {
    super();
    if (basepath == null) {
      basepath = "";
    } else {
      this.basepath = basepath;
    }
  }

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemid, String baseURI) {
    InputStream in = getClass().getResourceAsStream(this.basepath + systemid); // Como lê no ambiente de produção (Deploy em .EAR)
    if (in == null) {
      in = getClass().getClassLoader().getResourceAsStream(this.basepath + systemid); // Como Lê no ambiente de desenvolvimento (Deploy em diretório)
    }
    return new LSInputImpl(publicId, systemid, in);
  }

  protected class LSInputImpl implements LSInput {
    private String publicId;
    private String systemId;

    private BufferedInputStream inputStream;

    public LSInputImpl(String publicId, String sysId, InputStream input) {
      this.publicId = publicId;
      this.systemId = sysId;
      this.inputStream = new BufferedInputStream(input);
    }

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
      return null;
    }

    @Override
    public InputStream getByteStream() {
      return null;
    }

    @Override
    public boolean getCertifiedText() {
      return false;
    }

    @Override
    public Reader getCharacterStream() {
      return null;
    }

    @Override
    public String getEncoding() {
      return null;
    }

    @Override
    public String getStringData() {
      synchronized (inputStream) {
        try {
          byte[] input = new byte[inputStream.available()];
          inputStream.read(input);
          String contents = new String(input);
          return contents.replaceAll("ï»¿", "");
        } catch (IOException e) {
          RFWLogger.logException(e);
          return null;
        } finally {
          try {
            inputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    public void setBaseURI(String baseURI) {
    }

    @Override
    public void setByteStream(InputStream byteStream) {
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
    }

    @Override
    public void setEncoding(String encoding) {
    }

    @Override
    public void setStringData(String stringData) {
    }

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

    public BufferedInputStream getInputStream() {
      return inputStream;
    }

    public void setInputStream(BufferedInputStream inputStream) {
      this.inputStream = inputStream;
    }
  }
}
