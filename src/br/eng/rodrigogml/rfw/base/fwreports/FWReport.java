package br.eng.rodrigogml.rfw.base.fwreports;

import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import br.eng.rodrigogml.rfw.base.fwreports.bean.FWReportOptionBean;
import br.eng.rodrigogml.rfw.base.fwreports.bean.FWReportOptionBean.PAGE_ORIENTATION;
import br.eng.rodrigogml.rfw.base.utils.BUString;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.kernel.utils.RUFile;

/**
 * Description: Classe principal de relatorios. Todas as classes geradoras de relatorios do sistema deve extender esta classe.<br>
 *
 * @author Rodrigo Leit�o
 * @since 4.2.0 (13/02/2012)
 */
public abstract class FWReport {

  /**
   * Bean de Configura��o e Personaliza��o do relat�rio.
   */
  private FWReportOptionBean reportBean = null;

  private Document document = null;
  private PdfWriter writer = null;
  private final File tmpFile;

  private BaseFont bfPlain = null;
  private BaseFont bfBold = null;
  private BaseFont bfItalic = null;
  private BaseFont bfBoldItalic = null;

  /**
   * Tamanho do texto padr�o.
   */
  protected final float TEXTSIZE_NORMAL = 10; // Tamanho padr�o da Fonte
  protected final float TEXTSIZE_T1 = 16; // Tamanho do T�tulo 1
  protected final float TEXTSIZE_T2 = 12; // Tamanho do T�tulo 2

  /**
   * Espa�amento entre linhas. Este valor deve ser adicionado ao tamanho da letra em uso.
   */
  protected final float LINESPACING = 5; // Altura padr�o da linha

  /**
   * Inicializa o Engine de gera��o do relat�rio.
   *
   * @param reportbean Bean com as configura��es do relat�rio.
   */
  public FWReport(FWReportOptionBean reportbean) throws RFWException {
    this.reportBean = reportbean;
    try {
      Rectangle pageSize = null;
      if (reportBean.getOrientation() == PAGE_ORIENTATION.PORTRAIT) {
        pageSize = PageSize.A4;
      } else {
        pageSize = PageSize.A4.rotate();
      }
      document = new Document(pageSize, reportbean.getMarginLeft(), reportbean.getMarginRight(), reportbean.getMarginTop(), reportbean.getMarginBottom());

      // Escrevemos o conte�do em um arquivo tempor�rio
      try {
        this.tmpFile = RUFile.createFileInTemporaryPath(this.reportBean.getReportFileName() + ".pdf", null, StandardCharsets.UTF_8);
        writer = PdfWriter.getInstance(document, new FileOutputStream(this.tmpFile));
      } catch (FileNotFoundException e) {
        throw new RFWCriticalException("Falha ao inicializar o arquivo tempor�rio para escrita do relat�rio!");
      }
    } catch (DocumentException ex) {
      throw new RFWWarningException("Erro ao inicializar o documento para criar relat�rio!", ex);
    }

    // Inicializa as fontes para o relatorio
    try {
      bfPlain = com.itextpdf.text.pdf.BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      bfItalic = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      bfBoldItalic = BaseFont.createFont(BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    } catch (Exception ex) {
      throw new RFWWarningException("Erro ao inicializar as fontes para criar relat�rio!", ex);
    }
  }

  /**
   * M�todo chamado para realizar a escrita do relat�rio no "documento PDF".
   *
   * @throws RFWException
   */
  public void writeReport() throws RFWException {
    try {
      document.open();
      writeReportContent();
      document.close();
    } catch (ExceptionConverter e) {
      // O iText lan�a essa exce��o para converter exce��es em RuntimeExceptions, tratamos ela como erro critico... Exce��es n�o cr�ticas devem ser melhor tratada abaixo
      throw new RFWCriticalException("Falha ao gerar relat�rio.", e);
    }
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. Calcula a posi��o do texto automaticamente de acordo com o tamanho da fonte e alinhamento do texto.<br>
   * Este m�todo sempre coloca o texto um pouco acima do baseline '0' para evitar o corte das letras e ',' que ficam abaixo da baseline.
   *
   * @param font Fonte a ser utilizada.
   * @param textsize Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conte�do a ser escrito.
   * @param rotate Rota��o a ser aplicada no texto, em graus, no sentido anti-hor�rio.
   * @param fieldwidth largura do clip a ser executado.
   * @param fieldheight altura do clip a ser executado.
   * @return
   */
  public PdfTemplate createTextFieldClipped(BaseFont font, float textsize, int align, String content, float rotate, float fieldwidth, float fieldheight) {
    float x = 0;
    switch (align) {
      case PdfContentByte.ALIGN_CENTER:
        x = fieldwidth / 2;
        break;
      case PdfContentByte.ALIGN_RIGHT:
        x = fieldwidth;
        break;
    }
    float y = (float) (Math.ceil(textsize) * 0.15f); // Sempre que este m�todo cortar o texto, vamos ajutando ele aumentando graduamnete em unidades de .01f nesta multiplica��o

    return createTextFieldClipped(font, textsize, align, content, x, y, rotate, fieldwidth, fieldheight);
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. Calcula a posi��o do texto automaticamente de acordo com o tamanho da fonte e alinhamento do texto.<br>
   * Este m�todo sempre coloca o texto um pouco acima do baseline '0' para evitar o corte das letras e ',' que ficam abaixo da baseline.
   *
   * @param font Fonte a ser utilizada.
   * @param TEXTSIZE Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conte�do a ser escrito.
   * @param rotate Rota��o a ser aplicada no texto, em graus, no sentido anti-hor�rio.
   * @param fieldwidth largura do clip a ser executado.
   * @param fieldheight altura do clip a ser executado.
   * @param fontColor Cor da Fonte a ser usada na escrita
   * @return
   */
  public PdfTemplate createTextFieldClipped(BaseFont font, float textsize, int align, String content, float rotate, float fieldwidth, float fieldheight, BaseColor fontColor) {
    float x = 0;
    switch (align) {
      case PdfContentByte.ALIGN_CENTER:
        x = fieldwidth / 2;
        break;
      case PdfContentByte.ALIGN_RIGHT:
        x = fieldwidth;
        break;
    }
    float y = (float) (Math.ceil(textsize) * 0.15f); // aumenta graduamnete em unidades de .01f sempre que detectar algum texto cortado abaixo

    return createTextFieldClipped(font, textsize, align, content, x, y, rotate, fieldwidth, fieldheight, fontColor);
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. E permite especificar onde ficar� a ancora do texto dentro desto Clip.
   *
   * @param font Fonte a ser utilizada.
   * @param TEXTSIZE Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conte�do a ser escrito.
   * @param x Posi��o no eixo X da "�ncora" do texto.
   * @param y Posi��o no eixo Y da "�ncora" do texto.
   * @param rotate Rota��o a ser aplicada no texto, em graus, no sentido anti-hor�rio.
   * @param fieldwidth largura do clip a ser executado.
   * @param fieldheight altura do clip a ser executado.
   * @return
   */
  public PdfTemplate createTextFieldClipped(BaseFont font, float textsize, int align, String content, float x, float y, float rotate, float fieldwidth, float fieldheight) {
    return createTextFieldClipped(font, textsize, align, content, x, y, rotate, fieldwidth, fieldheight, null);
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. E permite especificar onde ficar� a ancora do texto dentro desto Clip.
   *
   * @param font Fonte a ser utilizada.
   * @param TEXTSIZE Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conte�do a ser escrito.
   * @param x Posi��o no eixo X da "�ncora" do texto.
   * @param y Posi��o no eixo Y da "�ncora" do texto.
   * @param rotate Rota��o a ser aplicada no texto, em graus, no sentido anti-hor�rio.
   * @param fieldwidth largura do clip a ser executado.
   * @param fieldheight altura do clip a ser executado.
   * @param fontColor Cor da Fonte a ser usada na escrita
   * @return
   */
  public PdfTemplate createTextFieldClipped(BaseFont font, float textsize, int align, String content, float x, float y, float rotate, float fieldwidth, float fieldheight, BaseColor fontColor) {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(fieldwidth, fieldheight);

    tmpl.saveState();
    tmpl.setFontAndSize(font, textsize);
    if (fontColor != null) tmpl.setColorFill(fontColor);

    // Cria o Clip
    tmpl.rectangle(0, 0, fieldwidth, fieldheight);
    tmpl.clip();
    tmpl.newPath();
    tmpl.beginText();
    // Deixa o Null com o Label "null" de prop�sito, pois valores nulos devem ser tratados antes de serem enviados at� este m�todo.
    tmpl.showTextAligned(align, (content == null ? "null" : content), x, y, rotate);
    tmpl.endText();

    // Faz uma borda em volta do clip para DEBUG, ver exatamente onde o campo est�
    // tmpl.rectangle(0, 0, fieldwidth, fieldheight);
    // tmpl.setColorStroke(BaseColor.RED);
    // tmpl.setLineWidth(1f);
    // tmpl.stroke();

    tmpl.restoreState();
    return tmpl;
  }

  /**
   * Este m�todo cria um template com largura definida contendo um determinado texto. O texto ser� quebrado nos espa�os entre as palavras sempre que o texto extrapolar a largura definida ou for encontrado o caracter '\n'.
   *
   * @param content Texto a ser escrito no campo.
   * @param font Fonte para escrita do conte�do.
   * @param columnWidth Largura m�xima do campo.
   * @param lineLeading dist�ncia entre linhas. Este tamanho � o espa�o entre a "baseline" de uma linha e a "baseline" de outra. Como em um caderno escolar o espa�o entre as linhas, tendo texto ou n�o e independente de seu tamanho. Um valor menor que o tamanho da fonte aqui far� com que as linhas se sobreponham.
   * @param yoffset Por padr�o o texto � escrito em y=0. Isto �, a baseline da �ltima linha fica na posi��o (0,0) do template. Isso ocasionalmente "corta" parte dos caracteres com conte�do abaixo da baseline, como 'p', 'g', ',', 'q', etc. Para evitar esse corete pode-se definir um valor posisivo aqui para que o texto seja escrito um pouco acima evitando que o texto seja "cliped" pelo template. No
   *          entanto, para manter a baseline do texto criado aqui com o restante esse offset deve ser levado em considera��o no momento de posicionar o template no conte�do principal.
   * @param alignment Define o alinhamento dentro da "caixa de texto" que vai ser criada. O valor � inteiro por que passamos direto os valores do pr�prio iText. Verifique as constantes em {@link Element}, como {@link Element#ALIGN_CENTER}.
   * @param maxHeight Define uma altura m�xima para o campo. Caso a altura necess�ria para este campo seja maior que o valor aqui deifnido, o campo ser� cortado e o texto ser� escrito s� at� a linha que couber. Para n�o ter limite defina um valor grande, como {@link Float#MAX_VALUE}. Para limitar em n�mero de linhas, calcule o valor a ser passado como linhas * lineLeading
   * @return Retorna o template para ser colocado no documento.
   *
   * @throws RFWWarningException Lan�ado caso algum objeto n�o possa ser criado corretamente.
   */
  public PdfTemplate createTextFieldWraped(String content, Font font, float columnWidth, float lineLeading, float yoffset, int alignment, float maxHeight) throws RFWWarningException {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(columnWidth, 100); // Inicia o template com uma altura de 100, mas ser� corrijido no final de acordo com o tamanho necess�rio para que o texto caiba.

    try {
      int lines = 0;
      int go;
      float height;
      boolean clipped = false;
      final Phrase phrase = new Phrase(lineLeading, content, font);
      do {
        lines++;
        height = lines * lineLeading; // Calculamos a altura necess�ria de acordo com a quantidade de linhas
        if (height > maxHeight) {
          height = --lines * lineLeading; // Retorna Height para seu valor anterior
          clipped = true;
          break;
        }
        ColumnText ct = new ColumnText(tmpl); // ATEN��O: O ColumnText � "descart�vel", mesmo que chame o m�todo ct.go em simula��o as pr�ximas itera��es d�o problema e retornam valores errados. Por isso passamos a criar o objeto dentro do FOR, e depois outro para quando formos escrever em definitivo.
        ct.setSimpleColumn(phrase, 0, 0 + yoffset, columnWidth, height + yoffset, lineLeading, alignment); // Colocamos a nova frase limitando o tamanho na largura definida e testando a altura gradualmente (calculada em height)
        go = ct.go(true); // Simulamos a coloca��o do texto para verifica se vai caber ou n�o
      } while (go != ColumnText.NO_MORE_TEXT); // Continua testando e aumentando a quantidade de linhas at� que o texto caiba.

      // Uma ves que as simula��es obtiveram sucesso j� temos a altura correta, podemos agora escrever o texto pra valer
      ColumnText ct = new ColumnText(tmpl); // ATEN��O: O ColumnText � "descart�vel", mesmo que chameo m�todo ct.go em simula��o as pr�ximas itera��es d�o problema e retornam valores errados. Por isso passamos a criar o objeto dentro do FOR, e depois outro para quando formos escrever em definitivo.
      ct.setSimpleColumn(phrase, 0, 0 + yoffset, columnWidth, height + yoffset, lineLeading, alignment); // Colocamos a nova frase limitando o tamanho na largura definida e testando a altura gradualmente (calculada em height)
      ct.go();

      // Se o texto est� incompleto, escrevemos o "..."
      if (clipped) {
        tmpl.saveState();
        tmpl.setFontAndSize(font.getBaseFont(), font.getSize());
        tmpl.beginText();
        // Coloca o caracter sempre com uma fonte comum e 2 pontos abaixo da linha base, para que mesmo quando o tempo chegar at� o fim do espa�o o simbolo continue aparecendo por baixo do texto.
        tmpl.showTextAligned(Element.ALIGN_RIGHT, "\u2026", columnWidth, yoffset - 2, 0);
        tmpl.endText();
        tmpl.restoreState();
      }

      // Agora corrigimos o tamanho do template para n�o cortar o texto de acordo com as dimens�es usadas.
      tmpl.setHeight(height);

      // Faz uma borda em volta do clip para DEBUG, ver exatamente onde o campo est�
      // tmpl.rectangle(0, 0, columnWidth, height);
      // tmpl.setColorStroke(BaseColor.RED);
      // tmpl.setLineWidth(1f);
      // tmpl.stroke();

      return tmpl;
    } catch (DocumentException e) {
      throw new RFWWarningException("RFW_ERR_200408", e);
    }
  }

  /**
   * Cria um template com o Cabe�alho de Relat�rio modelo 1.
   *
   * @param reportname Nome do Relat�rio
   * @param locale Locale (usado para formatar a data de impress�o do relat�rio)
   * @return Template criado
   */
  protected PdfTemplate createTemplateReportHeaderModel1(String reportname, Locale locale) {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(getWritableWidth(), 40);
    float yoffset = 5; // Contador de offset para saber o quando j� temos que pular a cada linha - come�a em 5 para n�o encostar com a linha desenhada para separar o cabe�alho

    /*
     * Como o iText faz o y crescente para "cima" vamos escrever o cabe�alho das linhas de baixo pra as de cima.
     */
    tmpl.beginText();
    {
      // Escreve o nome do relatorio
      tmpl.setFontAndSize(getBaseFontBoldItalic(), TEXTSIZE_T2);
      tmpl.showTextAligned(PdfContentByte.ALIGN_LEFT, reportname, 0, yoffset, 0);

      yoffset += TEXTSIZE_T2 + 5; // Pula o tamanho da linha mais uma margem de 5
    }
    {
      String enterpriseName = this.reportBean.getEnterpriseName();
      if (enterpriseName == null) enterpriseName = "RFWDeprec";

      // Escreve o nome da empresa
      tmpl.setFontAndSize(getBaseFontBold(), TEXTSIZE_T1);
      tmpl.showTextAligned(PdfContentByte.ALIGN_LEFT, enterpriseName, 0, yoffset, 0);

      // Escreve a data e a hora em que foi impresso
      tmpl.setFontAndSize(getBaseFontItalic(), TEXTSIZE_T2);
      tmpl.showTextAligned(PdfContentByte.ALIGN_RIGHT, RFW.getDateTime().format(RFW.getDateTimeFormattter()), tmpl.getWidth(), yoffset, 0);

      yoffset += TEXTSIZE_T1 + 5; // Pula o tamanho da linha maior mais uma margem de 5
    }
    tmpl.endText();

    // Desenha a linha de fim do cabe�alho de relat�rio
    tmpl.setColorStroke(new BaseColor(251, 150, 34));
    tmpl.setLineWidth(2f);
    tmpl.moveTo(0, 2);
    tmpl.lineTo(tmpl.getWidth(), 2);
    tmpl.stroke();

    // Corrige o tamanho do template
    tmpl.setHeight(yoffset);
    return tmpl;
  }

  /**
   * Cria o template de rodap� PageFooterModel1 mas aceita os valores de actualPage e actualSidePage para gerar a numera��o de p�ginas e p�ginas laterais.
   *
   * @param locale Localidade para formata��o (sem uso atual)
   * @param actualPage P�gina atual. O valor passado deve ser o valor que sair� na p�gina. Se passar zero, sai '0'.
   * @param actualSidePage P�gina lateral atual. O valor passado deve ser 1 para a p�gina A, 2 para a P�gina B etc. Por padr�o se passado o valor 1, equivalente a letra A, p�gina principal, ela simplesmente � ignorada mostrando apenas a numera��o de p�ginas comum.
   * @return
   */
  protected PdfTemplate createTemplatePageFooterModel1(Locale locale, int actualPage, int actualSidePage) {
    return createTemplatePageFooterModel1(locale, (actualSidePage > 1 ? BUString.convertToExcelColumnLetters(actualSidePage) + "-" + actualPage : "" + actualPage));
  }

  protected PdfTemplate createTemplatePageFooterModel1(String title, Locale locale, int actualPage, int actualSidePage) {
    return createTemplatePageFooterModel1(title, locale, (actualSidePage > 1 ? BUString.convertToExcelColumnLetters(actualSidePage) + "-" + actualPage : "" + actualPage));
  }

  protected PdfTemplate createTemplatePageFooterModel1(Locale locale, String actualpage) {
    return createTemplatePageFooterModel1(null, locale, actualpage);
  }

  /**
   * Este modelo de rodap� � identico ao {@link #createTemplatePageFooterModel1(Locale, String)} por�m ele permite colocar um t�tulo do lado esquerdo do rodap�.
   *
   * @param title T�tulo a ser exibido no rodap� da p�gina.
   * @param locale
   * @param actualpage
   * @return
   */
  protected PdfTemplate createTemplatePageFooterModel1(String title, Locale locale, String actualpage) {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(getWritableWidth(), 40);
    String pages = "P�gina: " + actualpage;

    final int t2size = 12;
    float yoffset = 0; // Contador de offset para saber o quando j� temos que pular a cada linha

    tmpl.beginText();
    {
      // Escreve o numero da p�gina
      tmpl.setFontAndSize(getBaseFontItalic(), t2size);
      tmpl.showTextAligned(PdfContentByte.ALIGN_RIGHT, pages, tmpl.getWidth(), yoffset + 3, 0);
      // Escreve o t�tulo
      if (title != null) tmpl.showTextAligned(PdfContentByte.ALIGN_LEFT, title, 0f, yoffset + 3, 0);

      yoffset += t2size + 5; // Pula o tamanho da linha maior mais uma margem de 5
    }
    // Terminamos as opera��es com texto
    tmpl.endText();

    // Desenha a linha de limite do cabe�alho de relat�rio
    tmpl.setColorStroke(new BaseColor(251, 150, 34));
    tmpl.setLineWidth(2f);
    tmpl.moveTo(0, yoffset);
    tmpl.lineTo(tmpl.getWidth(), yoffset);
    tmpl.stroke();

    // Corrige o tamanho do template
    tmpl.setHeight(yoffset);

    return tmpl;
  }

  protected abstract void writeReportContent() throws RFWException;

  /**
   * Define que o relat�rio ficar� no formato retrato.
   */
  public void setPageSizeA4Portrait() {
    getDocument().setPageSize(PageSize.A4);
  }

  /**
   * Define que o relat�rio ficar� no formato paisagem.
   */
  public void setPageSizeA4Landscape() {
    getDocument().setPageSize(PageSize.A4.rotate());
  }

  /**
   * Define as margens que ser�o utilizadas no relat�rio.
   *
   * @param left margem esquerda - padr�o 36
   * @param right margem direita - padr�o 36
   * @param top margem superior - padr�o 36
   * @param bottom margem inferior - padr�o 36
   */
  public void setMargins(float left, float right, float top, float bottom) {
    getDocument().setMargins(left, right, top, bottom);
  }

  /**
   * Recupera o arquivo tempor�rio com o conte�do do relat�rio.
   */
  public File getReportFile() {
    return this.tmpFile;
  }

  protected FWReportOptionBean getReportBean() {
    return reportBean;
  }

  public BaseFont getBaseFontBold() {
    return bfBold;
  }

  public BaseFont getBaseFontBoldItalic() {
    return bfBoldItalic;
  }

  public BaseFont getBaseFontItalic() {
    return bfItalic;
  }

  public BaseFont getBaseFontPlain() {
    return bfPlain;
  }

  public Document getDocument() {
    return document;
  }

  public PdfWriter getWriter() {
    return writer;
  }

  protected float getCoordFromTop(float coordy) {
    return document.getPageSize().getHeight() - coordy - document.topMargin();
  }

  protected float getCoordFromBottom(float coordy) {
    return coordy + document.bottomMargin();
  }

  protected float getCoordFromRight(float coordx) {
    return document.getPageSize().getWidth() - coordx - document.rightMargin();
  }

  protected float getCoordFromLeft(float coordx) {
    return coordx + document.leftMargin();
  }

  protected float getCoordFromMiddleX(float coordx) {
    return coordx + ((document.leftMargin() + document.getPageSize().getWidth() - document.rightMargin()) / 2);
  }

  protected float getCoordFromMiddleY(float coordy) {
    return coordy + ((document.bottomMargin() + document.getPageSize().getHeight() - document.top()) / 2);
  }

  protected float getWritableWidth() {
    return document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
  }

  protected float getWritableHeight() {
    return document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
  }

  /**
   * Cria um template com um SVG convertido para imagem dentro. O template ter� o tamanho definido na string SVG.
   *
   * @param svgContent C�digo SVG para gera��o da Imagem.
   * @return Template PDF para ser colocado no documento.
   * @throws RFWException
   */
  public PdfTemplate drawSvg(String svgContent) throws RFWException {
    return drawSvg(svgContent, 1);
  }

  /**
   * Cria um template com um SVG convertido para imagem dentro. O template ter� o tamanho definido na string svg multiplcado pelo valor de scale.
   *
   * @param svgContent C�digo SVG para gera��o da Imagem.
   * @param scale valor do fator de escala. Para tamanho real utilize 1, para reduzir utilize um valor entre 0 e 1, para aumentar utilize valores maiores que 1. Ex. para metade utilize 0.5, para o dobre utilize 2.
   * @return Template PDF para ser colocado no documento.
   * @throws RFWException
   */
  public PdfTemplate drawSvg(String svgContent, double scale) throws RFWException {
    return drawSvg(svgContent, scale, null, null, false);
  }

  /**
   * Cria um template com um SVG convertido para imagem dentro. O template ter� o tamanho definido de acordo com os tamanho m�ximos e o atributo de preservar o aspecto da imagem.
   *
   * @param svgContent C�digo SVG para gera��o da Imagem.
   * @param maxwidth Largura m�xima da imagem. (n�o podem ser nulo, por isso foi usado o tipo primitivo)
   * @param maxheight Altura m�xima da imagem. (n�o podem ser nulo, por isso foi usado o tipo primitivo)
   * @param preservratio Indica se a imagem deve preservar o aspecto (true) ou se permite distor��o (false). Caso true, a largura e/ou a altura ser� o limite de dimens�o da imagem gerada dependendo da propor��o da imagem SVG e das medidas definidas em maxwidth e maxheight. Caso false, a imagem ter� exatamente a largura e altura desejada no entando a imagem poder� ser distorcida para chegar �s
   *          medidas solicitadas.
   * @return Template PDF para ser colocado no documento.
   * @throws RFWException
   */
  public PdfTemplate drawSvg(String svgContent, double maxwidth, double maxheight, boolean preservratio) throws RFWException {
    return drawSvg(svgContent, 1, maxwidth, maxheight, preservratio);
  }

  /*
   * M�todo privado com o c�digo, n�o � p�blico para n�o oferecer para as classe filhas um m�todo com as op��es de "escala" e de "tamanho" ao mesmo tempo, j� que s�o atirbutos contr�rios. Este m�todo ignora a escala caso o tamanho seja definido.
   */
  private PdfTemplate drawSvg(String svgContent, double scale, Double maxwidth, Double maxheight, boolean preservratio) throws RFWException {
    UserAgent userAgent = new UserAgentAdapter();
    DocumentLoader loader = new DocumentLoader(userAgent);
    BridgeContext ctx = new BridgeContext(userAgent, loader);
    ctx.setDynamicState(BridgeContext.DYNAMIC);

    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
    SVGDocument chart;
    try {
      chart = factory.createSVGDocument("c:\\svg\\sample.svg", new ByteArrayInputStream(svgContent.getBytes(Charset.forName("UTF-8"))));
    } catch (IOException e) {
      throw new RFWCriticalException("RFW_ERR_200329", new String[] { svgContent }, e);
    }

    float width = Float.parseFloat(chart.getDocumentElement().getAttribute("width"));
    float height = Float.parseFloat(chart.getDocumentElement().getAttribute("height"));

    double scalex = scale;
    double scaley = scale;

    // Se a largura ou altura m�ximas foram definidas, calculamos a escala adequada para seus valores
    if (maxwidth != null) {
      scalex = maxwidth / width;
    }
    if (maxheight != null) {
      scaley = maxheight / height;
    }

    // Agora, se preservratio estiver definido escolhemos a menor scala para respeitar ambos os tamanho m�ximos e n�o distorcer a imagem aplicando escalas diferentes
    if (preservratio) {
      if (scalex < scaley)
        scaley = scalex;
      else
        scalex = scaley;
    }

    // Com a escala definida de acordo com defini��es, atualizamos as vari�veis de largura e altura para que os templates sejam feitos corretamente.
    width *= scalex;
    height *= scaley;

    GVTBuilder builder = new GVTBuilder();
    GraphicsNode mapGraphics = builder.build(ctx, chart);

    PdfTemplate template = getWriter().getDirectContent().createTemplate(width, height);
    Graphics2D g2d = new PdfGraphics2D(template, width, height);

    g2d.scale(scalex, scaley);
    mapGraphics.paint(g2d);
    g2d.dispose();

    return template;
  }

  /**
   * Converte um tamanho em mm (mil�metros) para o tamanho utilizado no iText.<br>
   * O valor � aproximado, e utiliza como base do calculo a dimens�o definida pelo pr�prio iText para o papel A4.<br>
   *
   * @param mm Tamanho em mil�metros
   * @return Tamanho a ser utilizado no iText
   */
  public static float fromMilimeters(float mm) {
    // (PageSize.A4.getHeight() / 297f) = 2.8350167f (a Parte constante da regra de tr�s j� est� resolvida para acelerar o m�todo, afinal ele � usado muitas vezes durante a produ��o de um relat�rio
    return 2.8350167f * mm;
  }

  /**
   * Cria um novo template nas dimens�es desejadas e j� aplica um Clip para evitar que conte�do escrito fora dele sejam exibidos.
   *
   * @param width Largura do template
   * @param height Altura do template
   * @return Template para edi��o e escrita de novo conte�do.
   */
  public PdfTemplate createTemplateClipped(float width, float height) {
    return getWriter().getDirectContent().createTemplate(width, height);
  }

  /**
   * M�todo usado para desenhar uma Linha dentro de um template.<br>
   * <br>
   * <B>ATEN��O: </b> Este m�todo interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
   * <br>
   *
   */
  public void drawLineStroke(PdfTemplate tmpl, float x, float y, float xEnd, float yEnd, BaseColor strokeColor, float strokeWidth) {
    tmpl.saveState();
    tmpl.newPath();
    tmpl.setColorStroke(strokeColor);
    tmpl.setLineWidth(strokeWidth);
    tmpl.moveTo(x, iY(tmpl, y));
    tmpl.lineTo(xEnd, iY(tmpl, yEnd));
    tmpl.stroke();
    tmpl.restoreState();
  }

  /**
   * M�todo usado para desenhar um retangulo dentro de um template.<br>
   * <br>
   * <B>ATEN��O: </b> Este m�todo interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
   * <br>
   *
   */
  public void drawRetangleStroke(PdfTemplate tmpl, float x, float y, float width, float height, float radius, BaseColor strokeColor, float strokeWidth) {
    tmpl.saveState();
    tmpl.newPath();
    tmpl.setColorStroke(strokeColor);
    tmpl.roundRectangle(x, iY(tmpl, y), width, -height, radius);
    tmpl.setLineWidth(strokeWidth);
    tmpl.stroke();
    tmpl.restoreState();
  }

  /**
   * M�todo usado para desenhar um retangulo dentro de um template.<br>
   * <br>
   * <B>ATEN��O: </b> Este m�todo interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
   * <br>
   */
  public void drawRetangleFill(PdfTemplate tmpl, float x, float y, float width, float height, float radius, BaseColor fillColor) {
    tmpl.saveState();
    tmpl.newPath();
    tmpl.setColorFill(fillColor);
    tmpl.roundRectangle(x, iY(tmpl, y), width, -height, radius);
    tmpl.fill();
    tmpl.restoreState();
  }

  /**
   * M�todo usado para desenhar um retangulo dentro de um template.<br>
   * <br>
   * <B>ATEN��O: </b> Este m�todo interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
   * <br>
   */
  public void drawRetangleStrokeAndFill(PdfTemplate tmpl, float x, float y, float width, float height, float radius, BaseColor strokeColor, float strokeWidth, BaseColor fillColor) {
    tmpl.saveState();
    tmpl.newPath();
    tmpl.setColorFill(fillColor);
    tmpl.setColorStroke(strokeColor);
    tmpl.roundRectangle(x, iY(tmpl, y), width, -height, radius);
    tmpl.setLineWidth(strokeWidth);
    tmpl.stroke();
    tmpl.fillStroke();
    tmpl.restoreState();
  }

  /**
   * Este m�todo inverte uma posi��o Y em rela��o ao padr�o do iText. Como o iText considera que a posi��o 0 � no fim da p�gina, este m�todo ajuda a inverter os valores.<br>
   * Isto �, se passar o valor 0 para este m�todo, ele retorna o valor a ser utilizado como se 0 fosse no topo da p�gina.<br>
   * Este m�todo inverte os valores em considera��o � p�gina.
   *
   * @param y Posi��o y desejada como se a coordenada 0 fosse no topo da p�gina.
   * @return retorna valor utilizado pelo iText para representar a posi��o desejada.
   */
  public float iY(float y) {
    return getDocument().getPageSize().getHeight() - y;
  }

  /**
   * Este m�todo inverte uma posi��o Y em rela��o ao padr�o do iText. Como o iText considera que a posi��o 0 � no fim da p�gina, este m�todo ajuda a inverter os valores.<br>
   * Isto �, se passar o valor 0 para este m�todo, ele retorna o valor a ser utilizado como se 0 fosse no topo da p�gina.<br>
   * Este m�todo inverte os valores em considera��o ao tamanho do template passado.
   *
   * @param y Posi��o y desejada como se a coordenada 0 fosse no topo da p�gina.
   * @return retorna valor utilizado pelo iText para representar a posi��o desejada.
   */
  public float iY(PdfTemplate tmpl, float y) {
    return tmpl.getHeight() - y;
  }

  /**
   * Adiciona um template dentro de outro nas posi��es desejadas.<br>
   * <br>
   * <B>ATEN��O: </b> Este m�todo interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
   * <br>
   *
   * @param tmpl Template pai, onde o conte�do ser� escrito.
   * @param block Template com o conte�do que dever� ser adiconado ao template pai.
   * @param x posi��o x onde ser� colocada a lateral esquerda do "block"
   * @param y posi��o y onde ser� colocado o topo do "block"
   */
  public void add(PdfTemplate tmpl, PdfTemplate block, float x, float y) {
    tmpl.addTemplate(block, x, iY(tmpl, y) - block.getHeight());// Soma o Height do block pois o iText tamb�m considera o lado inferior do block na hora de posicionar
  }

  /**
   * Cria uma nova p�gina no documento.
   */
  public void newPage() {
    getDocument().newPage();
  }

  /**
   * Retornar a largura que um texto precisa para ser escrito totalmente.
   *
   * @param font Fonte que ser� utilizada para escrever
   * @param fontSize Tamanho da fonte que ser� utilizada
   * @param content Conte�do do Texto.
   * @return Tamanho em pontos (unidade padr�o do iText)
   */
  public float getTextWidth(BaseFont font, float fontSize, String content) {
    return font.getWidthPoint(content, fontSize);
  }

}
