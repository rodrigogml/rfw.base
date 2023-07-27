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
 * @author Rodrigo Leitão
 * @since 4.2.0 (13/02/2012)
 */
public abstract class FWReport {

  /**
   * Bean de Configuração e Personalização do relatório.
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
   * Tamanho do texto padrão.
   */
  protected final float TEXTSIZE_NORMAL = 10; // Tamanho padrão da Fonte
  protected final float TEXTSIZE_T1 = 16; // Tamanho do Título 1
  protected final float TEXTSIZE_T2 = 12; // Tamanho do Título 2

  /**
   * Espaçamento entre linhas. Este valor deve ser adicionado ao tamanho da letra em uso.
   */
  protected final float LINESPACING = 5; // Altura padrão da linha

  /**
   * Inicializa o Engine de geração do relatório.
   *
   * @param reportbean Bean com as configurações do relatório.
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

      // Escrevemos o conteúdo em um arquivo temporário
      try {
        this.tmpFile = RUFile.createFileInTemporaryPath(this.reportBean.getReportFileName() + ".pdf", null, StandardCharsets.UTF_8);
        writer = PdfWriter.getInstance(document, new FileOutputStream(this.tmpFile));
      } catch (FileNotFoundException e) {
        throw new RFWCriticalException("Falha ao inicializar o arquivo temporário para escrita do relatório!");
      }
    } catch (DocumentException ex) {
      throw new RFWWarningException("Erro ao inicializar o documento para criar relatório!", ex);
    }

    // Inicializa as fontes para o relatorio
    try {
      bfPlain = com.itextpdf.text.pdf.BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      bfItalic = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      bfBoldItalic = BaseFont.createFont(BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    } catch (Exception ex) {
      throw new RFWWarningException("Erro ao inicializar as fontes para criar relatório!", ex);
    }
  }

  /**
   * Método chamado para realizar a escrita do relatório no "documento PDF".
   *
   * @throws RFWException
   */
  public void writeReport() throws RFWException {
    try {
      document.open();
      writeReportContent();
      document.close();
    } catch (ExceptionConverter e) {
      // O iText lança essa exceção para converter exceções em RuntimeExceptions, tratamos ela como erro critico... Exceções não críticas devem ser melhor tratada abaixo
      throw new RFWCriticalException("Falha ao gerar relatório.", e);
    }
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. Calcula a posição do texto automaticamente de acordo com o tamanho da fonte e alinhamento do texto.<br>
   * Este método sempre coloca o texto um pouco acima do baseline '0' para evitar o corte das letras e ',' que ficam abaixo da baseline.
   *
   * @param font Fonte a ser utilizada.
   * @param textsize Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conteúdo a ser escrito.
   * @param rotate Rotação a ser aplicada no texto, em graus, no sentido anti-horário.
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
    float y = (float) (Math.ceil(textsize) * 0.15f); // Sempre que este método cortar o texto, vamos ajutando ele aumentando graduamnete em unidades de .01f nesta multiplicação

    return createTextFieldClipped(font, textsize, align, content, x, y, rotate, fieldwidth, fieldheight);
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. Calcula a posição do texto automaticamente de acordo com o tamanho da fonte e alinhamento do texto.<br>
   * Este método sempre coloca o texto um pouco acima do baseline '0' para evitar o corte das letras e ',' que ficam abaixo da baseline.
   *
   * @param font Fonte a ser utilizada.
   * @param TEXTSIZE Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conteúdo a ser escrito.
   * @param rotate Rotação a ser aplicada no texto, em graus, no sentido anti-horário.
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
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. E permite especificar onde ficará a ancora do texto dentro desto Clip.
   *
   * @param font Fonte a ser utilizada.
   * @param TEXTSIZE Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conteúdo a ser escrito.
   * @param x Posição no eixo X da "âncora" do texto.
   * @param y Posição no eixo Y da "âncora" do texto.
   * @param rotate Rotação a ser aplicada no texto, em graus, no sentido anti-horário.
   * @param fieldwidth largura do clip a ser executado.
   * @param fieldheight altura do clip a ser executado.
   * @return
   */
  public PdfTemplate createTextFieldClipped(BaseFont font, float textsize, int align, String content, float x, float y, float rotate, float fieldwidth, float fieldheight) {
    return createTextFieldClipped(font, textsize, align, content, x, y, rotate, fieldwidth, fieldheight, null);
  }

  /**
   * Escreve o texto em um template e aplica o Clip para cortar o template nos tamanhos especificados. E permite especificar onde ficará a ancora do texto dentro desto Clip.
   *
   * @param font Fonte a ser utilizada.
   * @param TEXTSIZE Tamanho da Fonte
   * @param align Alinhamento do texto, verifique as constantes <code>PdfContentByte.ALIGN_*</code>
   * @param content Conteúdo a ser escrito.
   * @param x Posição no eixo X da "âncora" do texto.
   * @param y Posição no eixo Y da "âncora" do texto.
   * @param rotate Rotação a ser aplicada no texto, em graus, no sentido anti-horário.
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
    // Deixa o Null com o Label "null" de propósito, pois valores nulos devem ser tratados antes de serem enviados até este método.
    tmpl.showTextAligned(align, (content == null ? "null" : content), x, y, rotate);
    tmpl.endText();

    // Faz uma borda em volta do clip para DEBUG, ver exatamente onde o campo está
    // tmpl.rectangle(0, 0, fieldwidth, fieldheight);
    // tmpl.setColorStroke(BaseColor.RED);
    // tmpl.setLineWidth(1f);
    // tmpl.stroke();

    tmpl.restoreState();
    return tmpl;
  }

  /**
   * Este método cria um template com largura definida contendo um determinado texto. O texto será quebrado nos espaços entre as palavras sempre que o texto extrapolar a largura definida ou for encontrado o caracter '\n'.
   *
   * @param content Texto a ser escrito no campo.
   * @param font Fonte para escrita do conteúdo.
   * @param columnWidth Largura máxima do campo.
   * @param lineLeading distância entre linhas. Este tamanho é o espaço entre a "baseline" de uma linha e a "baseline" de outra. Como em um caderno escolar o espaço entre as linhas, tendo texto ou não e independente de seu tamanho. Um valor menor que o tamanho da fonte aqui fará com que as linhas se sobreponham.
   * @param yoffset Por padrão o texto é escrito em y=0. Isto é, a baseline da última linha fica na posição (0,0) do template. Isso ocasionalmente "corta" parte dos caracteres com conteúdo abaixo da baseline, como 'p', 'g', ',', 'q', etc. Para evitar esse corete pode-se definir um valor posisivo aqui para que o texto seja escrito um pouco acima evitando que o texto seja "cliped" pelo template. No
   *          entanto, para manter a baseline do texto criado aqui com o restante esse offset deve ser levado em consideração no momento de posicionar o template no conteúdo principal.
   * @param alignment Define o alinhamento dentro da "caixa de texto" que vai ser criada. O valor é inteiro por que passamos direto os valores do próprio iText. Verifique as constantes em {@link Element}, como {@link Element#ALIGN_CENTER}.
   * @param maxHeight Define uma altura máxima para o campo. Caso a altura necessária para este campo seja maior que o valor aqui deifnido, o campo será cortado e o texto será escrito só até a linha que couber. Para não ter limite defina um valor grande, como {@link Float#MAX_VALUE}. Para limitar em número de linhas, calcule o valor a ser passado como linhas * lineLeading
   * @return Retorna o template para ser colocado no documento.
   *
   * @throws RFWWarningException Lançado caso algum objeto não possa ser criado corretamente.
   */
  public PdfTemplate createTextFieldWraped(String content, Font font, float columnWidth, float lineLeading, float yoffset, int alignment, float maxHeight) throws RFWWarningException {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(columnWidth, 100); // Inicia o template com uma altura de 100, mas será corrijido no final de acordo com o tamanho necessário para que o texto caiba.

    try {
      int lines = 0;
      int go;
      float height;
      boolean clipped = false;
      final Phrase phrase = new Phrase(lineLeading, content, font);
      do {
        lines++;
        height = lines * lineLeading; // Calculamos a altura necessária de acordo com a quantidade de linhas
        if (height > maxHeight) {
          height = --lines * lineLeading; // Retorna Height para seu valor anterior
          clipped = true;
          break;
        }
        ColumnText ct = new ColumnText(tmpl); // ATENÇÃO: O ColumnText é "descartável", mesmo que chame o método ct.go em simulação as próximas iterações dão problema e retornam valores errados. Por isso passamos a criar o objeto dentro do FOR, e depois outro para quando formos escrever em definitivo.
        ct.setSimpleColumn(phrase, 0, 0 + yoffset, columnWidth, height + yoffset, lineLeading, alignment); // Colocamos a nova frase limitando o tamanho na largura definida e testando a altura gradualmente (calculada em height)
        go = ct.go(true); // Simulamos a colocação do texto para verifica se vai caber ou não
      } while (go != ColumnText.NO_MORE_TEXT); // Continua testando e aumentando a quantidade de linhas até que o texto caiba.

      // Uma ves que as simulações obtiveram sucesso já temos a altura correta, podemos agora escrever o texto pra valer
      ColumnText ct = new ColumnText(tmpl); // ATENÇÃO: O ColumnText é "descartável", mesmo que chameo método ct.go em simulação as próximas iterações dão problema e retornam valores errados. Por isso passamos a criar o objeto dentro do FOR, e depois outro para quando formos escrever em definitivo.
      ct.setSimpleColumn(phrase, 0, 0 + yoffset, columnWidth, height + yoffset, lineLeading, alignment); // Colocamos a nova frase limitando o tamanho na largura definida e testando a altura gradualmente (calculada em height)
      ct.go();

      // Se o texto está incompleto, escrevemos o "..."
      if (clipped) {
        tmpl.saveState();
        tmpl.setFontAndSize(font.getBaseFont(), font.getSize());
        tmpl.beginText();
        // Coloca o caracter sempre com uma fonte comum e 2 pontos abaixo da linha base, para que mesmo quando o tempo chegar até o fim do espaço o simbolo continue aparecendo por baixo do texto.
        tmpl.showTextAligned(Element.ALIGN_RIGHT, "\u2026", columnWidth, yoffset - 2, 0);
        tmpl.endText();
        tmpl.restoreState();
      }

      // Agora corrigimos o tamanho do template para não cortar o texto de acordo com as dimensões usadas.
      tmpl.setHeight(height);

      // Faz uma borda em volta do clip para DEBUG, ver exatamente onde o campo está
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
   * Cria um template com o Cabeçalho de Relatório modelo 1.
   *
   * @param reportname Nome do Relatório
   * @param locale Locale (usado para formatar a data de impressão do relatório)
   * @return Template criado
   */
  protected PdfTemplate createTemplateReportHeaderModel1(String reportname, Locale locale) {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(getWritableWidth(), 40);
    float yoffset = 5; // Contador de offset para saber o quando já temos que pular a cada linha - começa em 5 para não encostar com a linha desenhada para separar o cabeçalho

    /*
     * Como o iText faz o y crescente para "cima" vamos escrever o cabeçalho das linhas de baixo pra as de cima.
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

    // Desenha a linha de fim do cabeçalho de relatório
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
   * Cria o template de rodapé PageFooterModel1 mas aceita os valores de actualPage e actualSidePage para gerar a numeração de páginas e páginas laterais.
   *
   * @param locale Localidade para formatação (sem uso atual)
   * @param actualPage Página atual. O valor passado deve ser o valor que sairá na página. Se passar zero, sai '0'.
   * @param actualSidePage Página lateral atual. O valor passado deve ser 1 para a página A, 2 para a Página B etc. Por padrão se passado o valor 1, equivalente a letra A, página principal, ela simplesmente é ignorada mostrando apenas a numeração de páginas comum.
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
   * Este modelo de rodapé é identico ao {@link #createTemplatePageFooterModel1(Locale, String)} porém ele permite colocar um título do lado esquerdo do rodapé.
   *
   * @param title Título a ser exibido no rodapé da página.
   * @param locale
   * @param actualpage
   * @return
   */
  protected PdfTemplate createTemplatePageFooterModel1(String title, Locale locale, String actualpage) {
    final PdfTemplate tmpl = getWriter().getDirectContent().createTemplate(getWritableWidth(), 40);
    String pages = "Página: " + actualpage;

    final int t2size = 12;
    float yoffset = 0; // Contador de offset para saber o quando já temos que pular a cada linha

    tmpl.beginText();
    {
      // Escreve o numero da página
      tmpl.setFontAndSize(getBaseFontItalic(), t2size);
      tmpl.showTextAligned(PdfContentByte.ALIGN_RIGHT, pages, tmpl.getWidth(), yoffset + 3, 0);
      // Escreve o título
      if (title != null) tmpl.showTextAligned(PdfContentByte.ALIGN_LEFT, title, 0f, yoffset + 3, 0);

      yoffset += t2size + 5; // Pula o tamanho da linha maior mais uma margem de 5
    }
    // Terminamos as operações com texto
    tmpl.endText();

    // Desenha a linha de limite do cabeçalho de relatório
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
   * Define que o relatório ficará no formato retrato.
   */
  public void setPageSizeA4Portrait() {
    getDocument().setPageSize(PageSize.A4);
  }

  /**
   * Define que o relatório ficará no formato paisagem.
   */
  public void setPageSizeA4Landscape() {
    getDocument().setPageSize(PageSize.A4.rotate());
  }

  /**
   * Define as margens que serão utilizadas no relatório.
   *
   * @param left margem esquerda - padrão 36
   * @param right margem direita - padrão 36
   * @param top margem superior - padrão 36
   * @param bottom margem inferior - padrão 36
   */
  public void setMargins(float left, float right, float top, float bottom) {
    getDocument().setMargins(left, right, top, bottom);
  }

  /**
   * Recupera o arquivo temporário com o conteúdo do relatório.
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
   * Cria um template com um SVG convertido para imagem dentro. O template terá o tamanho definido na string SVG.
   *
   * @param svgContent Código SVG para geração da Imagem.
   * @return Template PDF para ser colocado no documento.
   * @throws RFWException
   */
  public PdfTemplate drawSvg(String svgContent) throws RFWException {
    return drawSvg(svgContent, 1);
  }

  /**
   * Cria um template com um SVG convertido para imagem dentro. O template terá o tamanho definido na string svg multiplcado pelo valor de scale.
   *
   * @param svgContent Código SVG para geração da Imagem.
   * @param scale valor do fator de escala. Para tamanho real utilize 1, para reduzir utilize um valor entre 0 e 1, para aumentar utilize valores maiores que 1. Ex. para metade utilize 0.5, para o dobre utilize 2.
   * @return Template PDF para ser colocado no documento.
   * @throws RFWException
   */
  public PdfTemplate drawSvg(String svgContent, double scale) throws RFWException {
    return drawSvg(svgContent, scale, null, null, false);
  }

  /**
   * Cria um template com um SVG convertido para imagem dentro. O template terá o tamanho definido de acordo com os tamanho máximos e o atributo de preservar o aspecto da imagem.
   *
   * @param svgContent Código SVG para geração da Imagem.
   * @param maxwidth Largura máxima da imagem. (não podem ser nulo, por isso foi usado o tipo primitivo)
   * @param maxheight Altura máxima da imagem. (não podem ser nulo, por isso foi usado o tipo primitivo)
   * @param preservratio Indica se a imagem deve preservar o aspecto (true) ou se permite distorção (false). Caso true, a largura e/ou a altura será o limite de dimensão da imagem gerada dependendo da proporção da imagem SVG e das medidas definidas em maxwidth e maxheight. Caso false, a imagem terá exatamente a largura e altura desejada no entando a imagem poderá ser distorcida para chegar às
   *          medidas solicitadas.
   * @return Template PDF para ser colocado no documento.
   * @throws RFWException
   */
  public PdfTemplate drawSvg(String svgContent, double maxwidth, double maxheight, boolean preservratio) throws RFWException {
    return drawSvg(svgContent, 1, maxwidth, maxheight, preservratio);
  }

  /*
   * Método privado com o código, não é público para não oferecer para as classe filhas um método com as opções de "escala" e de "tamanho" ao mesmo tempo, já que são atirbutos contrários. Este método ignora a escala caso o tamanho seja definido.
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

    // Se a largura ou altura máximas foram definidas, calculamos a escala adequada para seus valores
    if (maxwidth != null) {
      scalex = maxwidth / width;
    }
    if (maxheight != null) {
      scaley = maxheight / height;
    }

    // Agora, se preservratio estiver definido escolhemos a menor scala para respeitar ambos os tamanho máximos e não distorcer a imagem aplicando escalas diferentes
    if (preservratio) {
      if (scalex < scaley)
        scaley = scalex;
      else
        scalex = scaley;
    }

    // Com a escala definida de acordo com definições, atualizamos as variáveis de largura e altura para que os templates sejam feitos corretamente.
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
   * Converte um tamanho em mm (milímetros) para o tamanho utilizado no iText.<br>
   * O valor é aproximado, e utiliza como base do calculo a dimensão definida pelo próprio iText para o papel A4.<br>
   *
   * @param mm Tamanho em milímetros
   * @return Tamanho a ser utilizado no iText
   */
  public static float fromMilimeters(float mm) {
    // (PageSize.A4.getHeight() / 297f) = 2.8350167f (a Parte constante da regra de três já está resolvida para acelerar o método, afinal ele é usado muitas vezes durante a produção de um relatório
    return 2.8350167f * mm;
  }

  /**
   * Cria um novo template nas dimensões desejadas e já aplica um Clip para evitar que conteúdo escrito fora dele sejam exibidos.
   *
   * @param width Largura do template
   * @param height Altura do template
   * @return Template para edição e escrita de novo conteúdo.
   */
  public PdfTemplate createTemplateClipped(float width, float height) {
    return getWriter().getDirectContent().createTemplate(width, height);
  }

  /**
   * Método usado para desenhar uma Linha dentro de um template.<br>
   * <br>
   * <B>ATENÇÃO: </b> Este método interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
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
   * Método usado para desenhar um retangulo dentro de um template.<br>
   * <br>
   * <B>ATENÇÃO: </b> Este método interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
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
   * Método usado para desenhar um retangulo dentro de um template.<br>
   * <br>
   * <B>ATENÇÃO: </b> Este método interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
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
   * Método usado para desenhar um retangulo dentro de um template.<br>
   * <br>
   * <B>ATENÇÃO: </b> Este método interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
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
   * Este método inverte uma posição Y em relação ao padrão do iText. Como o iText considera que a posição 0 é no fim da página, este método ajuda a inverter os valores.<br>
   * Isto é, se passar o valor 0 para este método, ele retorna o valor a ser utilizado como se 0 fosse no topo da página.<br>
   * Este método inverte os valores em consideração à página.
   *
   * @param y Posição y desejada como se a coordenada 0 fosse no topo da página.
   * @return retorna valor utilizado pelo iText para representar a posição desejada.
   */
  public float iY(float y) {
    return getDocument().getPageSize().getHeight() - y;
  }

  /**
   * Este método inverte uma posição Y em relação ao padrão do iText. Como o iText considera que a posição 0 é no fim da página, este método ajuda a inverter os valores.<br>
   * Isto é, se passar o valor 0 para este método, ele retorna o valor a ser utilizado como se 0 fosse no topo da página.<br>
   * Este método inverte os valores em consideração ao tamanho do template passado.
   *
   * @param y Posição y desejada como se a coordenada 0 fosse no topo da página.
   * @return retorna valor utilizado pelo iText para representar a posição desejada.
   */
  public float iY(PdfTemplate tmpl, float y) {
    return tmpl.getHeight() - y;
  }

  /**
   * Adiciona um template dentro de outro nas posições desejadas.<br>
   * <br>
   * <B>ATENÇÃO: </b> Este método interpreta as coordenadas como sendo a origem (0,0) no topo esquerdo. <br>
   * <br>
   *
   * @param tmpl Template pai, onde o conteúdo será escrito.
   * @param block Template com o conteúdo que deverá ser adiconado ao template pai.
   * @param x posição x onde será colocada a lateral esquerda do "block"
   * @param y posição y onde será colocado o topo do "block"
   */
  public void add(PdfTemplate tmpl, PdfTemplate block, float x, float y) {
    tmpl.addTemplate(block, x, iY(tmpl, y) - block.getHeight());// Soma o Height do block pois o iText também considera o lado inferior do block na hora de posicionar
  }

  /**
   * Cria uma nova página no documento.
   */
  public void newPage() {
    getDocument().newPage();
  }

  /**
   * Retornar a largura que um texto precisa para ser escrito totalmente.
   *
   * @param font Fonte que será utilizada para escrever
   * @param fontSize Tamanho da fonte que será utilizada
   * @param content Conteúdo do Texto.
   * @return Tamanho em pontos (unidade padrão do iText)
   */
  public float getTextWidth(BaseFont font, float fontSize, String content) {
    return font.getWidthPoint(content, fontSize);
  }

}
