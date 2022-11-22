package br.eng.rodrigogml.rfw.base.fwreports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWRunTimeException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.base.fwreports.FWGridReport.FWGridReportMatrix.TextWrapMode;
import br.eng.rodrigogml.rfw.base.fwreports.FWListReport.ReportBlock.ALIGNMENT;
import br.eng.rodrigogml.rfw.base.fwreports.bean.FWGridReportOptionBean;

/**
 * Description: Classe de relat�rio que permite a gera��o de um relat�rio baseada em um "GRID". O desenvolvedor deve extender esta classe e montar a estrutura dos dados e configura��es das c�lulas de acordo com suas prefer�ncias. Uma vez que todos os dados estejam definidos dentro desse GRID (uma matriz) deve-se mandar gerar o relat�rio. O {@link FWGridReport} montar� o relat�rio quebrando as
 * linhas e as colunas conforme a necessidade.<br>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (28/08/2015)
 */
public abstract class FWGridReport extends FWListReport {

  /**
   * Esta classe tem a finalidade de carregar as defini��es de estilo. Essas defini��es poder�o ser aplicadas nas colunas ou diretamente nos valores.<br>
   * O {@link FWGridReport} dar� prefer�ncia para as defini��es feitas diretamente na c�lula. Se n�o houverem defini��es diretamente na c�lula as defini��es das colunas ser�o utilizadas.<br>
   */
  public static class FWGridReportStyle {

    private Boolean bold = false;
    private Boolean italic = false;
    private ALIGNMENT alignment = null;
    private BaseColor baseColor = null;
    private TextWrapMode textWrapMode = null;

    /**
     * Sobrep�es as defini��es de tamanho da fonte.
     */
    private Float fontSize = null;

    public FWGridReportStyle() {
    }

    /**
     * Construtor completo
     *
     * @param bold Define se a Fonte ter� negrito
     * @param alignment Define o alinhamento do texto
     * @param baseColor Define a cor do Texto
     */
    public FWGridReportStyle(Boolean bold, ALIGNMENT alignment, BaseColor baseColor, TextWrapMode textWrapMode) {
      this.bold = bold;
      this.alignment = alignment;
      this.baseColor = baseColor;
      this.textWrapMode = textWrapMode;
    }

    public FWGridReportStyle(Boolean bold, ALIGNMENT alignment, BaseColor baseColor) {
      this.bold = bold;
      this.alignment = alignment;
      this.baseColor = baseColor;
    }

    public FWGridReportStyle(Boolean bold) {
      this.bold = bold;
    }

    public FWGridReportStyle(Boolean bold, ALIGNMENT alignment) {
      this.bold = bold;
      this.alignment = alignment;
    }

    public FWGridReportStyle(Boolean bold, ALIGNMENT alignment, float fontSize) {
      this.bold = bold;
      this.alignment = alignment;
      this.fontSize = fontSize;
    }

    public FWGridReportStyle(ALIGNMENT alignment) {
      this.alignment = alignment;
    }

    public Boolean getBold() {
      return bold;
    }

    public void setBold(Boolean bold) {
      this.bold = bold;
    }

    public ALIGNMENT getAlignment() {
      return alignment;
    }

    public void setAlignment(ALIGNMENT alignment) {
      this.alignment = alignment;
    }

    public BaseColor getBaseColor() {
      return baseColor;
    }

    public void setBaseColor(BaseColor baseColor) {
      this.baseColor = baseColor;
    }

    public TextWrapMode getTextWrapMode() {
      return textWrapMode;
    }

    public void setTextWrapMode(TextWrapMode textWrapMode) {
      this.textWrapMode = textWrapMode;
    }

    public Float getFontSize() {
      return fontSize;
    }

    public void setFontSize(Float fontSize) {
      this.fontSize = fontSize;
    }

    public Boolean getItalic() {
      return italic;
    }

    public void setItalic(Boolean italic) {
      this.italic = italic;
    }

  }

  /**
   * Interface usada para agrupar os diferentes tipos de conte�dos em um �nico objeto pai. J� pensando no dia em que teremos outros tipos de conte�dos, como templates personalizados por exemplo.
   */
  public static interface FWGridReportContent {
    public FWGridReportStyle getStyle();

    /**
     * Recupera quantas colunas esse conte�do ocupa. O valor padr�o � 1. N�o pode ser menor que 1.
     */
    public int getColSpan();

    /**
     * Para tipos de conte�do que n�o suportem colSpan, podem lan�ar exce��o.
     */
    public void setColSpan(int colSpan) throws RFWException;
  }

  /**
   * Classe usada para definir um conte�do do tipo String para ser colocado na c�lula.
   */
  public static class FWGridReportStringContent implements FWGridReportContent {
    private final String content;
    private FWGridReportStyle style = null;
    private int colSpan = 1;

    public FWGridReportStringContent(String content) {
      this.content = content;
    }

    public FWGridReportStringContent(String content, FWGridReportStyle style) {
      this.content = content;
      this.style = style;
    }

    protected String getContent() {
      return content;
    }

    @Override
    public FWGridReportStyle getStyle() {
      return style;
    }

    public void setStyle(FWGridReportStyle style) {
      this.style = style;
    }

    @Override
    public int getColSpan() {
      return colSpan;
    }

    @Override
    public void setColSpan(int colSpan) {
      this.colSpan = colSpan;
    }
  }

  /**
   * Esta classe representa o conte�do de um relat�rio, isto �, um grid de dados para ser impresso nas folhas.
   */
  public static class FWGridReportMatrix {
    /**
     * Configura��es de quebra e limite do texto das colunas.
     */
    public static enum TextWrapMode {
      /**
       * N�o executa nenhum m�todo. Far� com que o texto seja escrito fora de "templates" o que deixa o PDF mais enxuto e leve, no entanto caso a coluna n�o tenha espa�o suficiente para o conte�do ele passar� por cima da coluna do lado.
       */
      NONE,
      /**
       * Define que o texto poder� ser quebrado em m�ltiplas linhas.
       */
      TEXTWRAP,
      /**
       * Define que o texto dever� ser "cortado" quando o tamanho da coluna acabar.
       */
      TRUNCATE
    }

    /**
     * Armazena as linhas do grid. Cada item da List ser� uma linha do Grid. O conte�do da list � um array com seus conte�dos. O tamanho de cara array ser� exatamente o definido em {@link #columns}.
     */
    private final LinkedList<FWGridReportContent[]> gridRows = new LinkedList<>();
    /**
     * Define a quantidade de colunas que o Grid ter�.
     */
    private final int columns;
    /**
     * Armazena as defini��es de style das colunas.
     */
    private final FWGridReportStyle[] columnStyles;
    /**
     * Armazena os captions das colunas.
     */
    private final String[] columnCaptions;
    /**
     * Define a largura m�nima que a coluna deve ter. Caso n�o caiba na p�gina ela ser� passada para uma quebra lateral.<br>
     */
    private final Integer[] columnMinWidth;
    /**
     * Em geral o tamanho aplicado �s colunas � o valor definido em ColumnMinWidth. Mas quando uma coluna n�o cabe mais na p�gina sobre um espa�o. A coluna que tiver este atributo definido com true primeiro, se expande para ocupar o espa�o restante
     */
    private final Boolean[] columnExpandable;

    /**
     * Permite definir a cor de fundo de cada linha par adicionada na matriz.
     */
    private HashMap<FWGridReportContent[], String> rowOddLineBackgroundColor = new HashMap<>();

    /**
     * Permite definir a cor de fundo de cada linha adicionada na matriz.
     */
    private HashMap<FWGridReportContent[], String> rowLineBackgroundColor = new HashMap<>();

    /**
     * Esta lista carrega as informa��es sobre a configura��o de texto da coluna. Se ela deve quebrar o texto em m�ltiplas linhas ou n�o. Se estiver configurada como NONE (ou null - valor padr�o) nenhum modo ser� aplicado. Este m�todo evita a cria��o de templates mar permite que o texto passe um por cima do outro. <br>
     * Esta configura��o s� � v�lida para o conte�do "String".
     */
    private final TextWrapMode[] columnTextWrapMode;

    /**
     * Cria uma nova matriz de dados.
     *
     * @param columns Total de colunas que esta matriz deve ter.
     */
    public FWGridReportMatrix(int columns) {
      this.columns = columns;
      this.columnStyles = new FWGridReportStyle[this.columns];
      this.columnCaptions = new String[this.columns];
      this.columnMinWidth = new Integer[this.columns];
      this.columnExpandable = new Boolean[this.columns];
      this.columnTextWrapMode = new TextWrapMode[this.columns];
    }

    public void addRow(FWGridReportContent[] rowContent) {
      if (rowContent == null || rowContent.length != this.columns) throw new ArrayIndexOutOfBoundsException("Tamanho do array com o conte�do da linha inv�lido! Linha: " + this.gridRows.size());
      gridRows.add(rowContent);
    }

    /**
     * Define a Cor de Linha Impar.
     *
     * @param row
     * @param color Defina a cor no "estilo web" usando hexa e o prefixo "#". Ex: "#F5F5F5"
     */
    public void setRowOddLineBackgroundColor(FWGridReportContent[] row, String color) {
      this.rowOddLineBackgroundColor.put(row, color);
    }

    public String getRowOddLineBackgroundColor(FWGridReportContent[] row) {
      return this.rowOddLineBackgroundColor.get(row);
    }

    public boolean containsRowOddLineBackgroundColor(FWGridReportContent[] row) {
      return this.rowOddLineBackgroundColor.containsKey(row);
    }

    public String removeRowOddLineBackgroundColor(FWGridReportContent[] row) {
      return this.rowOddLineBackgroundColor.remove(row);
    }

    /**
     * Define a Cor de Linha.
     *
     * @param row
     * @param color Defina a cor no "estilo web" usando hexa e o prefixo "#". Ex: "#F5F5F5"
     */
    public void setRowLineBackgroundColor(FWGridReportContent[] row, String color) {
      this.rowLineBackgroundColor.put(row, color);
    }

    public String getRowLineBackgroundColor(FWGridReportContent[] row) {
      return this.rowLineBackgroundColor.get(row);
    }

    public boolean containsRowLineBackgroundColor(FWGridReportContent[] row) {
      return this.rowLineBackgroundColor.containsKey(row);
    }

    public String removeRowLineBackgroundColor(FWGridReportContent[] row) {
      return this.rowLineBackgroundColor.remove(row);
    }

    public void setColumnTextWrapMode(TextWrapMode wrapMode, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      this.columnTextWrapMode[columnIndex] = wrapMode;
    }

    public void setColumnStyle(FWGridReportStyle style, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      this.columnStyles[columnIndex] = style;
    }

    public void setColumnCaption(String caption, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      this.columnCaptions[columnIndex] = caption;
    }

    public void setColumnMinWidth(Integer minWidth, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      this.columnMinWidth[columnIndex] = minWidth;
    }

    public void setColumnExpandable(Boolean expandable, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      this.columnExpandable[columnIndex] = expandable;
    }

    protected TextWrapMode getColumnTextWrapMode(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      return this.columnTextWrapMode[columnIndex];
    }

    protected FWGridReportStyle getColumnStyle(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      return this.columnStyles[columnIndex];
    }

    protected String getColumnCaption(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      return this.columnCaptions[columnIndex];
    }

    protected Integer getColumnMinWidth(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      if (this.columnMinWidth[columnIndex] != null) {
        return this.columnMinWidth[columnIndex];
      } else {
        return 100;
      }
    }

    protected Boolean getColumnExpandable(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("�ndice de coluna inv�lido! �ndice: " + columnIndex);
      return this.columnExpandable[columnIndex];
    }

    protected final LinkedList<FWGridReportContent[]> getGridRows() {
      return gridRows;
    }

    public final int getColumns() {
      return columns;
    }
  }

  /**
   * Armazena a lista de relat�rios Matrix que ser]ao transformados em relat�rio. Normalmente s� temos uma matriz, mas m�ltiplas podem ser adicionadas para que m�ltiplos relat�rios sejam gerados de uma �nica vez.
   */
  private final ArrayList<FWGridReportMatrix> matrixList = new ArrayList<>();

  /**
   * Armazena a lista de lista de blocos para ser passada para o ListReport.<br>
   * Cada block representa uma linha do relat�rio. Cada lista de blocos um relat�rio diferente a ser gerado dentro do mesmo PDF.
   */
  private final ArrayList<ArrayList<ReportBlock>> reportBlockLists = new ArrayList<>();

  /**
   * Armazena o �ndice da pr�xima lista de reportBlocks que ser� retornada na chamada do m�todo {@link #getReportBlocksList()}.
   */
  private int reportBlockListsIteratorNextIndex = 0;

  /**
   * Lista de Tamanhos reais das colunas depois de processadas para expandir e ocupar o tamanho total da p�gina. Esses valores s�o calculados no {@link #prepareReportData()} e s�o salvos na lista com o mesmo �ndice da Matrix.
   */
  private final ArrayList<Integer[]> realColumnWidthList = new ArrayList<>();

  /**
   * Lista de index da primeira coluna de cada p�gina. Esses valores s�o calculados no {@link #prepareReportData()} e s�o salvos na lista com o mesmo �ndice da Matrix.<br>
   * O array tem sempre o mesmo tamanho da quantidade de colunas totais da Matrix, no entanto o array s� ser� totalmente ocupado caso s� tenhamos 1 coluna por p�gina. O �ndex do array � o �ndex da p�gina lateral. Sendo o �ndex 0 a p�gina principal "A", 1 a primeira p�gina lateral "B", etc.
   */
  private final ArrayList<Integer[]> firstColumnOnPageList = new ArrayList<>();

  /**
   * Esta lista cont�m arrays com os cabe�alhos j� prontos de cada coluna de cada relat�rio. <Br>
   * Cada array da list representa um relat�rio. Cada item dentro do array um cabe�alho da coluna. O index do array � o index da coluna dentro da matrix.
   */
  private final ArrayList<PdfTemplate[]> columnsHeadersTemplates = new ArrayList<>();

  /**
   * Este hash � um auxiliar do {@link #getRowHeight(FWGridReportContent[], FWGridReportMatrix)} para evitar que a altura de cada linha seja recalculada diversas vezes durante a escrita do relat�rio. Normalmente a mesma chamada para cada linha � realizada a cada escrita da c�lula de uma linha.<br>
   * A hash mais externa: sua chave � o index do report sendo gerado e seu valor outra Hash. Essa hash interna: sua chave � o n�mero da linha e seu valor (Float) a altura computada para alinha.
   */
  private final HashMap<Integer, HashMap<Integer, Float>> rowHeightCache = new HashMap<>();

  /**
   * Este hash � um auxiliar do {@link #getCellTemplate(FWGridReportContent, FWGridReportMatrix)} para evitar que o mesmo conte�do seja gerado m�ltiplas vezes. O conte�do gerado � necess�rio para c�lculo da altura da linha diversas vezes e posteriormente para real escrita no ReportBlock. <br>
   * A hash mais externa: sua chave � o index do report sendo gerado e seu valor outra Hash. Essa hash interna: sua chave � uma string composta pelo n�mero da linha e coluna separados por um '|'. Ex; "35|12" para a linha 35 coluna 12. E seu valor (PdfTemplate) o template com o conte�do pronto.
   */
  private final HashMap<Integer, HashMap<String, PdfTemplate>> cellTemplateCache = new HashMap<>();

  public FWGridReport(FWGridReportOptionBean reportbean) throws RFWException {
    super(reportbean);
  }

  public void addMatrix(FWGridReportMatrix matrix) {
    this.matrixList.add(matrix);
  }

  @Override
  protected void prepareReportData() throws RFWException {
    // Vamos converter cada Matrix em um relat�rio diferente (lista de blocks)
    for (FWGridReportMatrix matrix : this.matrixList) {
      Integer[] realColumnWidth = new Integer[matrix.getColumns()]; // Armazena o tamanho real a ser utilizado em cada coluna. Tamanho m�nimo + tamanho excedente caso a coluna seja expans�vel.
      realColumnWidthList.add(realColumnWidth); // Salva o array na lista para uso em outros m�todos.

      /*
       * Este array guardar� a informa��o de qual � o �ndice da primeira coluna alocada naquela p�gina. Definimos o tamanho do array de acordo com o tamanho do n�mero de colunas para evitar problemas, pois no pior caso teremos 1 coluna por p�gina no m�nimo. Normalmente este array ser� muito maior doque o necess�rio. O �ndice do array � o n�mero da p�gina lateral, come�ando no �ndice 0, sendo o
       * valor o �ndice da coluna da matrix que � a primeira coluna daquela p�gina.
       */
      Integer[] firstColumnOnPage = new Integer[matrix.getColumns()];
      firstColumnOnPageList.add(firstColumnOnPage); // Salva o array na lista para uso em outros m�todos.
      firstColumnOnPage[0] = 0; // Definimos que na primeira p�gina a primeira coluna � a coluna 0. Como � constante e necess�rio para o funcionamento do c�digo abaixo sem maiores condi��es (IFs) j� fica definido aqui.

      // O primeiro passo � calcular o tamanho real de cada coluna e quantas colunas v�o caber em cada p�gina. A medida que estrapolem teremos p�ginas laterais.
      // O c�lculo � feito de acordo com a largura de cada coluna e salvo no array realColumnWidth. Quando a coluna n�o couber mais na mesma p�gina
      int countExpandable = 0; // Contador de quantas colunas expans�veis encontramos na mesma p�gina
      int sidePage = 0; // Contador de p�ginas laterais. Para saber em que p�gina estamos colocando cada coluna. Note que diferente do padr�o da ListReport aqui o contador come�a em 0 para facilitar o uso nos arrays logo abaixo.
      float usedWidth = 0; // Somat�ria das larguras das colunas da p�gina atual.

      for (int colCount = 0; colCount < matrix.getColumns(); colCount++) {
        float remainWidth = getWritableWidth() - usedWidth; // Calcula a largura restante na p�gina

        // Verificamos se essa coluna 'i' n�o cabe mais na mesma p�gina. Se n�o couber quebramos a p�gina lateral. Para evitar problemas caso algu�m fa�a uma coluna maior que o espa�o livre para escrita for�amos a falha de entrada no IF caso seja a primeira coluna sendo escrita na p�gina.
        if (matrix.getColumnMinWidth(colCount) > remainWidth && usedWidth > 0) {
          // Se n�o cabe na p�gina vamos verificar os itens que j� est�o na p�gina e difividir o espa�o restante entre as colunas expans�veis
          if (countExpandable > 0 && remainWidth > 0) { // Se a coluna for maior que o espa�o dispon�vel remainWidth retornar� negativo, o que vai foder as contas
            int splitWidth = (int) (remainWidth / countExpandable);
            // Reiteramos as colunas desta p�gina para ver quem tem o tamanho expans�vel para somar a parte que sobrou nele.
            for (int splitCol = firstColumnOnPage[sidePage]; splitCol < colCount; splitCol++) {
              if (matrix.getColumnExpandable(splitCol) != null && matrix.getColumnExpandable(splitCol)) {
                realColumnWidth[splitCol] += splitWidth;
              }
            }
          }
          usedWidth = 0;
          countExpandable = 0;
          sidePage++; // Incrementa para a pr�xima p�gina lateral
          firstColumnOnPage[sidePage] = colCount; // Salva que essa coluna ser� a primeira da pr�xima p�gina lateral.
        }
        realColumnWidth[colCount] = matrix.getColumnMinWidth(colCount); // Salvamos o valor da coluna atual como tamanho real. Durante a quebra de p�gina lateral recalcularemos para redistribuir o espa�o restante se houver
        usedWidth += realColumnWidth[colCount];
        if (matrix.getColumnExpandable(colCount) != null && matrix.getColumnExpandable(colCount)) countExpandable++;
      }
      // quando o for acabar ainda verificamos o espa�o que sobrou na �ltima p�gina para tentar espalhar esse espa�o entre as demais colunas
      float remainWidth = getWritableWidth() - usedWidth; // Calcula a largura restante na p�gina
      if (countExpandable > 0 && remainWidth > 0) { // Se a coluna for maior que o espa�o dispon�vel remainWidth retornar� negativo, o que vai foder as contas
        int splitWidth = (int) (remainWidth / countExpandable);
        // Reiteramos as colunas desta p�gina para ver quem tem o tamanho expans�vel para somar a parte que sobrou nele.
        for (int splitCol = firstColumnOnPage[sidePage]; splitCol < matrix.getColumns(); splitCol++) {
          if (matrix.getColumnExpandable(splitCol)) {
            realColumnWidth[splitCol] += splitWidth;
          }
        }
      }

      // Uma vez que o FOR acima calculou as larguras reais de cada coluna (depois da expans�o do espa�o de sobra) podemos come�ar a montar a lista de reportBlocks j� que sabemos onde cada um vai
      ArrayList<ReportBlock> blockList = new ArrayList<>();
      this.reportBlockLists.add(blockList); // J� adiciona esse relat�rio � lista de relat�rios

      final float lineLeading = TEXTSIZE_NORMAL + LINESPACING; // Define o tamanho de cada linha. Entre Baseline de uma e a Baseline de outra.
      for (FWGridReportContent[] reportContent : matrix.getGridRows()) { // Iteramos cada linha da matrix para gerar as linhas, incluindo as linhas das p�ginas laterais
        ReportBlock block = new ReportBlock(); // Report Block que ter� o conte�do desta linha da Matrix
        { // Define as configura��es da linha no ReportBlock (se existirem)
          if (matrix.containsRowOddLineBackgroundColor(reportContent)) block.setOddLineBackgroundColor(matrix.getRowOddLineBackgroundColor(reportContent));
          if (matrix.containsRowLineBackgroundColor(reportContent)) block.setLineBackgroundColor(matrix.getRowLineBackgroundColor(reportContent));
        }
        if (sidePage > 0) block.setTemplateSidePages(new PdfTemplate[sidePage]); // Se temos p�ginas laterias, j� deixamos o array preparado no tamanho certo para receber os templates que ser�o criados abaixo.
        blockList.add(block);

        for (int sp = 0; sp <= sidePage; sp++) { // Iteramos o total de p�ginas laterais que precisam ser criada
          PdfTemplate pdfTemplate = getWriter().getDirectContent().createTemplate(getWritableWidth(), lineLeading);
          float position = 0;
          // Agora vamos iterar as colunas come�ando na primeira coluna da sidePage atual e limitando at� a �ltima coluna da matrix (�ltima sidePage) ou at� antes a primeira coluna da pr�xima sidePage. Note que temos que verificar se firstColumnOnPage[sp+1] n�o � nulo porque na �ltima p�gina esse valor ser� nulo. Validamos ainda inclusive se sp n�o � maior que o length porque no caso de 1 coluna
          // por p�gina sp + 1 dar� outofbounds na �ltima p�gina.
          int limitColumn = matrix.getColumns(); // Inicialmente limita a coluna no total de colunas. Abaixo vamos rever esse limite se encontrarmos uma defini��o de primeira coluna para a pr�xima p�gina lateral
          if (sp + 1 < firstColumnOnPage.length && firstColumnOnPage[sp + 1] != null) limitColumn = firstColumnOnPage[sp + 1];
          for (int col = firstColumnOnPage[sp]; col < limitColumn; col++) {
            // Escrevemos o content de acordo com seu style definido
            writeCellGrid(pdfTemplate, reportContent, col, matrix, position, realColumnWidth);
            position += realColumnWidth[col];
          }

          // Com o template criado e totalmente escrito, colocamos no Block de acordo com a contagem de p�ginas laterais.
          if (sp == 0) {
            block.setTemplateMainPage(pdfTemplate);
          } else {
            block.getTemplateSidePages()[sp - 1] = pdfTemplate;
          }
        }
      }

      // Depois de ter os dados preparados, j� vamos deixar preparado os templates dos cabe�alhos das colunas.
      // Os cabe�alhos s�o preparados antes por duas raz�es:
      // 1-Se usarmos os mesmos templates em todas as p�ginas teremos um PDF mais enxuto.
      // 2-Ter todos os cabe�alhos j� preparados desde o in�cio facilita pra calcular o tamanho do template para o PageHeader de todas as p�ginas, j� que diferentes p�ginas poder�o ter diferententes tamanhos de textos e de cabe�alhos.

      // Vamos iterrar todas as colunas para gerar seus cabe�alhos e salvar no array
      PdfTemplate[] headerTempls = new PdfTemplate[matrix.getColumns()];
      this.columnsHeadersTemplates.add(headerTempls);
      for (int i = 0; i < matrix.getColumns(); i++) {
        int alignment = Element.ALIGN_LEFT; // Deixamos o alinhamento a esquerda j� definido para servir de padr�o se nenhum alinhamanheo for definido.
        if (matrix.getColumnStyle(i).getAlignment() == ALIGNMENT.CENTER) {
          alignment = Element.ALIGN_CENTER;
        } else if (matrix.getColumnStyle(i).getAlignment() == ALIGNMENT.RIGHT) {
          alignment = Element.ALIGN_RIGHT;
        }
        // Cria o template sem limita��o de altura, afinal os cabe�alhos s�o feitos pelos programadores, espera-se que s� fa�am cabe�alhos grandes se for realmente necess�rio.
        headerTempls[i] = createTextFieldWraped(matrix.getColumnCaption(i), new Font(getBaseFontBold(), TEXTSIZE_NORMAL), realColumnWidth[i], lineLeading, 3, alignment, Float.MAX_VALUE);
      }
    }
  }

  /**
   * Este m�todo tem a finalidade de escrever o valor da c�lula no template. <Br>
   * Este m�todo altera a altura do template, caso a altura do template seja menor que a altura necess�ria para o template da c�lula.
   *
   * @param pdfTemplate Template PDF no qual o campo ser� escrito.
   * @param reportContent Conte�do da Matrix para ser escrito.
   * @param col �ndice da coluna a ser escrito.
   * @param matrix Matrix com todas as informa��es do relat�rio.
   * @param position Posi��o em que a coluna deve ser escrita no template. Essa posi��o � sempre o valor a esquerda da coluna.
   * @param realColumnWidth Largura real de cada coluna. Calculada depois de redistribuir o espa�o restante de cada p�gina pelas colunas expans�veis.
   * @throws RFWWarningException
   */
  private void writeCellGrid(PdfTemplate pdfTemplate, FWGridReportContent[] reportContent, int col, FWGridReportMatrix matrix, float position, Integer[] realColumnWidth) throws RFWWarningException {
    // Se esta c�lula n�o tem conte�do (comum em casos de colspan) simplesmente retornamos e n�o fazemos nada.
    if (reportContent[col] == null) return;

    FWGridReportStyle style = getMergedStyle(matrix.getColumnStyle(col), reportContent[col].getStyle());

    if (reportContent[col] instanceof FWGridReportStringContent) {
      FWGridReportStringContent content = (FWGridReportStringContent) reportContent[col];

      // Obtem o tamanho da linha para c�lculos
      final int row = matrix.getGridRows().indexOf(reportContent);
      Float rowHeight = getRowHeight(reportContent, matrix);
      if (pdfTemplate.getHeight() < rowHeight) pdfTemplate.setHeight(rowHeight);

      // Calcula a largura da coluna. Inclui os valores das colunas adicionais nos casos de colspan
      int colTotalWidth = 0;
      for (int i = 0; i < content.getColSpan(); i++) {
        colTotalWidth += realColumnWidth[col + i];
      }

      // Verifica se h� algum tipo de quebra de texto definido, se n�o houver s� escrevermos o texto, se houver solicitamos a cria��o do template no m�todo "centralizador"
      TextWrapMode wrapMode = matrix.getColumnTextWrapMode(col);
      if (style.getTextWrapMode() != null) wrapMode = style.getTextWrapMode(); // Se tiver um TextWarp no style, sobrep�e o herdado da Matriz
      if (wrapMode == null || wrapMode == TextWrapMode.NONE) {
        float fontSize = TEXTSIZE_NORMAL;
        if (style != null && style.getFontSize() != null) fontSize = style.getFontSize();

        float y = (rowHeight - fontSize - LINESPACING) / 2 + LINESPACING;
        pdfTemplate.saveState();
        if (style != null && style.getBold()) {
          if (style.getItalic()) {
            pdfTemplate.setFontAndSize(getBaseFontBoldItalic(), fontSize);
          } else {
            pdfTemplate.setFontAndSize(getBaseFontBold(), fontSize);
          }
        } else {
          if (style.getItalic()) {
            pdfTemplate.setFontAndSize(getBaseFontItalic(), fontSize);
          } else {
            pdfTemplate.setFontAndSize(getBaseFontPlain(), fontSize);
          }
        }
        pdfTemplate.beginText();

        if (style != null && style.getBaseColor() != null) {
          pdfTemplate.setColorFill(style.getBaseColor());
        }

        // Escreve o conte�do
        if (style == null || style.getAlignment() == null || style.getAlignment() == ALIGNMENT.RIGHT) {
          pdfTemplate.showTextAligned(PdfContentByte.ALIGN_RIGHT, content.getContent(), position + colTotalWidth, y, 0);
        } else if (style.getAlignment() == ALIGNMENT.CENTER) {
          pdfTemplate.showTextAligned(PdfContentByte.ALIGN_CENTER, content.getContent(), position + (colTotalWidth / 2), y, 0);
        } else if (style.getAlignment() == ALIGNMENT.LEFT) {
          pdfTemplate.showTextAligned(PdfContentByte.ALIGN_LEFT, content.getContent(), position, y, 0);
        }
        pdfTemplate.endText();
        pdfTemplate.restoreState();
      } else if (wrapMode == TextWrapMode.TEXTWRAP || wrapMode == TextWrapMode.TRUNCATE) {
        // Para os casos de Truncate e TextWarp, colocamos o template na posi��o descontando o offset de 3 que � dado por padr�o pelo m�todo de getCellTemplate
        PdfTemplate cellTemplate = getCellTemplate(row, col, matrix);
        pdfTemplate.addTemplate(cellTemplate, position, ((rowHeight - cellTemplate.getHeight() + 3) / 2));
      }
    } else {
      throw new RFWRunTimeException("Conte�do n�o suportado para escrita pelo FWGridReport!");
    }
  }

  /**
   * Este m�todo tem a finalidade de calcular a altura de uma detarminada linha do relat�rio de acordo com as configura��es das colunas e seus conte�dos. De forma centralizada, este m�todo deve ser capaz de observar todo o conte�do da linha e calcular sua altura adequadamente para que outros m�todos (como o
   * {@link #writeCellGrid(PdfTemplate, FWGridReportContent[], int, FWGridReportMatrix, float, Integer[])} possam utilizar esse valor na hora de escrever.
   *
   * @param rowContent Linha para an�lise.
   * @param matrix Matrix do relat�rio para obter as configura��es e valores necess�rios para o c�lculo.
   * @return Retorna a altura da linha.
   * @throws RFWWarningException
   */
  private Float getRowHeight(FWGridReportContent[] rowContent, FWGridReportMatrix matrix) throws RFWWarningException {
    // Verifica se j� temos algum valor na cache
    HashMap<Integer, Float> reportCache = this.rowHeightCache.get(getReportCount());
    if (reportCache == null) {
      reportCache = new HashMap<>();
      this.rowHeightCache.put(getReportCount(), reportCache);
    }
    int row = matrix.getGridRows().indexOf(rowContent);
    Float height = reportCache.get(row);
    if (height == null) {
      // Itera seu conte�do para processar e verifica a altura de cada um
      height = 0f;
      for (int col = 0; col < rowContent.length; col++) {
        FWGridReportContent content = rowContent[col];
        if (content != null) {
          if (content instanceof FWGridReportStringContent) {
            final FWGridReportStyle style = getMergedStyle(matrix.getColumnStyle(col), content.getStyle());

            // Se � do tipo string sua altura tem o tamanho padr�o de tamanho da fonte e espa�amento entre linhas, exceto se tiver quebra de linha definida. Neste caso teremos de obter o template para saber o tamanho resultante.
            TextWrapMode mode = matrix.getColumnTextWrapMode(col);
            if (style != null && style.getTextWrapMode() != null) mode = style.getTextWrapMode(); // Se tiver um TextWarp no style, sobrep�e o herdado da Matriz

            float fontSize = TEXTSIZE_NORMAL;
            if (style != null && style.getFontSize() != null) fontSize = style.getFontSize();
            if (mode == null || mode == TextWrapMode.NONE || mode == TextWrapMode.TRUNCATE) {
              // Se n�o tiver modo, ou se for truncate, o tamanho da linha � o tamanho padr�o de Text e espa�amento de linha.
              if (height < fontSize + LINESPACING) height = fontSize + LINESPACING;
            } else if (mode == TextWrapMode.TEXTWRAP) {
              // Com quebra de texto verificamos o tamanho do template gerado para ver o tamanho que a linha ter� que ter para incorpora-lo.
              PdfTemplate cellTemplate = getCellTemplate(row, col, matrix);
              if (height < cellTemplate.getHeight()) height = cellTemplate.getHeight();
            } else {
              throw new RFWRunTimeException("Tipo de Quebra de texto desconhecido pelo m�todo de c�lculo de altura da linha! Imposs�vel gerar relat�rio! " + mode);
            }
          } else {
            throw new RFWRunTimeException("Conte�do desconhecido pelo m�todo de c�lculo de altura da linha! Imposs�vel gerar relat�rio! " + content.getClass().getCanonicalName());
          }
        }
      }
      // Salvamos o valor na cache
      reportCache.put(row, height);
    }
    return height;
  }

  /**
   * Este m�todo � usado para gerar o template de uma c�lula quando necess�rio. Exceto pelos conte�dos de String sem Quebra de linha que s�o escritos diretamente no PDF, este m�todo deve gerar o template que representa o conte�do da c�lula. Seus tamanhos j� devem estar de acordo com o necess�rio para escrita na linha. <Br>
   * <B>Aten��o: este m�todo pode retornar null se o conte�do n�o obriga a gera��o do template!</b>
   *
   * @param col Coluna do conte�do
   * @param row Linha do Conte�do
   * @param matrix Matrix do relat�rio para obter as configura��es da coluna.
   *
   * @return Template com o conte�do pronto para ser usado como conte�do da c�lula do grid.
   * @throws RFWWarningException
   */
  private PdfTemplate getCellTemplate(int row, int col, FWGridReportMatrix matrix) throws RFWWarningException {
    // Verifica se j� temos algum valor na cache
    HashMap<String, PdfTemplate> reportCache = this.cellTemplateCache.get(getReportCount());
    if (reportCache == null) {
      reportCache = new HashMap<>();
      this.cellTemplateCache.put(getReportCount(), reportCache);
    }
    PdfTemplate tpl = reportCache.get(row + "|" + col);
    if (tpl == null) {
      FWGridReportContent content = matrix.getGridRows().get(row)[col];

      // Calcula a largura da coluna. Inclui os valores das colunas adicionais nos casos de colspan
      int colTotalWidth = 0;
      for (int i = 0; i < content.getColSpan(); i++) {
        colTotalWidth += this.realColumnWidthList.get(getReportCount())[col + i];
      }

      final FWGridReportStyle style = getMergedStyle(matrix.getColumnStyle(col), content.getStyle());

      BaseFont baseFont = getBaseFontPlain(); // inicializa a vari�vel com a font comum. Garante que n�o chegar� null nos m�todos de uso.
      if (style != null) {
        if (style.getBold()) {
          if (style.getItalic()) {
            baseFont = getBaseFontBoldItalic();
          } else {
            baseFont = getBaseFontBold();
          }
        } else {
          if (style.getItalic()) {
            baseFont = getBaseFontItalic();
          } else {
            baseFont = getBaseFontPlain();
          }
        }
      }

      // Verifica se precisamos criar o template
      if (content instanceof FWGridReportStringContent) {
        float fontSize = TEXTSIZE_NORMAL;
        if (style != null && style.getFontSize() != null) fontSize = style.getFontSize();

        FWGridReportStringContent c = (FWGridReportStringContent) content;
        // Se � do tipo string sua altura tem o tamanho padr�o de tamanho da fonte e espa�amento entre linhas, exceto se tiver quebra de linha definida. Neste caso teremos de obter o template para saber o tamanho resultante.
        TextWrapMode mode = matrix.getColumnTextWrapMode(col);
        if (style.getTextWrapMode() != null) mode = style.getTextWrapMode(); // Se tiver um TextWarp no style, sobrep�e o herdado da Matriz
        if (mode == null || mode == TextWrapMode.NONE) {
          // N�o gera template neste caso, deixa seguir null
        } else if (mode == TextWrapMode.TRUNCATE) {
          int align = Element.ALIGN_LEFT;
          float x = 0f;
          if (style.getAlignment() == ALIGNMENT.CENTER) {
            align = Element.ALIGN_CENTER;
            x = (colTotalWidth / 2);
          } else if (style.getAlignment() == ALIGNMENT.RIGHT) {
            align = Element.ALIGN_RIGHT;
            x = colTotalWidth;
          }

          // Por padr�o todos os templates do tipo string est�o sendo escritos com offset de 3 para n�o cortarem as letras com conte�do abaixo da baseline, e ao serem posicionados o valor � descontado na coordenada para manter o baseline.
          tpl = createTextFieldClipped(baseFont, fontSize, align, c.getContent(), x, 3f, 0f, colTotalWidth, fontSize + LINESPACING);
        } else if (mode == TextWrapMode.TEXTWRAP) {
          int align = Element.ALIGN_LEFT;
          if (style.getAlignment() == ALIGNMENT.CENTER) {
            align = Element.ALIGN_CENTER;
          } else if (style.getAlignment() == ALIGNMENT.RIGHT) {
            align = Element.ALIGN_RIGHT;
          }
          // Por padr�o todos os templates do tipo string est�o sendo escritos com offset de 3 para n�o cortarem as letras com conte�do abaixo da baseline, e ao serem posicionados o valor � descontado na coordenada para manter o baseline.
          tpl = createTextFieldWraped(c.getContent(), new Font(baseFont, fontSize), colTotalWidth, fontSize + LINESPACING, 3, align, Float.MAX_VALUE);
        } else {
          throw new RFWRunTimeException("Tipo de Quebra de texto desconhecido pelo m�todo de c�lculo de altura da linha! Imposs�vel gerar relat�rio! " + mode);
        }
      }
      // Salva o template na cache
      reportCache.put(row + "|" + col, tpl);
    }
    return tpl;
  }

  /**
   * Este m�todo tem a finalidade de unir as defini��es em um �nico objeto Style.<br>
   * Lembrando que a prioridade � sempre do style definido diretamente na c�lula do Grid. Qualquer defini��o que esteja feita no cellStyle n�o ser� sobreescrita.<br>
   * Para evitar a cria��o de novos objetos, as informa��es necess�rias ser�o copiadas do columnStyle para o cellStyle. Como o objeto recebido ser� alterado, este m�todo pode destruir as informa��es originais do objeto. Caso essas informa��es se tornem importantes trocar a implementa��o deste m�todo para criar objetos distintos.
   *
   * @param columnStyle Estilo da Coluna
   * @param cellStyle Estilo da c�lula.
   * @return objeto com o estilo resultante da mescla entre os dois. Pode retornar nulo caso ambos os styles passados sejam nulo.
   */
  private FWGridReportStyle getMergedStyle(FWGridReportStyle columnStyle, FWGridReportStyle cellStyle) {
    if (cellStyle == null) {
      // Se cellstyle � nulo simplesmente retornamos o columnStyle. Se ele for nulo tamb�m paci�ncia...
      cellStyle = columnStyle;
    } else if (columnStyle != null) {
      // Se cellstyle n�o � nulo, e columnstyle tamb�m n�o, fazemos o merge. Se columnstyle for nulo, retornamos o pr�prio cellStyle pois n�o haver� nada para realizar o merge.
      if (cellStyle.getBold() == null) cellStyle.setBold(columnStyle.getBold());
      if (cellStyle.getItalic() == null) cellStyle.setBold(columnStyle.getItalic());
      if (cellStyle.getAlignment() == null) cellStyle.setAlignment(columnStyle.getAlignment());
      if (cellStyle.getTextWrapMode() == null) cellStyle.setTextWrapMode(columnStyle.getTextWrapMode());
    }
    return cellStyle;
  }

  @Override
  protected List<ReportBlock> getReportBlocksList() throws RFWException {
    if (this.reportBlockListsIteratorNextIndex < this.reportBlockLists.size()) {
      return this.reportBlockLists.get(this.reportBlockListsIteratorNextIndex++);
    }
    return null;
  }

  @Override
  protected PdfTemplate writePageHeader() throws RFWException {
    // Obtem a Matrix correta de acordo com o relat�rio atual
    FWGridReportMatrix matrix = this.matrixList.get(getReportCount());

    // Obtemos os cabe�alhos das colunas criados para este relat�rio durante o m�todo de prepareReportData
    final PdfTemplate[] headerTempls = this.columnsHeadersTemplates.get(getReportCount());

    // Vamos iterar todos os templates para saber qual � a altura do maior e usar de guia para posicionar todos os demais templates
    float maxHeight = 0;
    for (int col = 0; col < headerTempls.length; col++) {
      PdfTemplate hTmpl = headerTempls[col];
      if (headerTempls[col] != null && hTmpl.getHeight() > maxHeight) maxHeight = hTmpl.getHeight();
    }

    PdfTemplate pdfTemplate = getWriter().getDirectContent().createTemplate(getWritableWidth(), maxHeight);

    // Para saber quais colunas escrever e seus valores vamos separar alguns valores
    Integer[] firstColumnOnPage = this.firstColumnOnPageList.get(getReportCount());
    Integer firstColumn = firstColumnOnPage[getPageSideCount() - 1]; // Subtraimos 1 do PageSideCount pois ListReport come�a sua contagem em 1 para a p�gina A, e n�o em 0 como usado aqui no GridReport
    Integer lastColumn = matrix.getColumns() - 1; // Iniciamos com o �ndice da �ltima coluna, ai verificamos se temos uma outra p�gina lateral a seguir com a informa��o da primeira coluna que ser� da pr�xima coluna e abaixamos esse valor
    if (getPageSideCount() < firstColumnOnPage.length && firstColumnOnPage[getPageSideCount()] != null) lastColumn = firstColumnOnPage[getPageSideCount()] - 1;

    // Agora iteramos as colunas de definidas para a p�gina atual
    int pos = 0;
    for (int col = firstColumn; col <= lastColumn; col++) {
      if (headerTempls[col] == null) continue;
      // Vamos colocar lado a lado no template a ser retornado cada um dos templates das colunas. A altura do posicionamento � calculada para que ela fique centralizada
      pdfTemplate.addTemplate(headerTempls[col], pos, (maxHeight - headerTempls[col].getHeight()) / 2);
      pos += headerTempls[col].getWidth();
    }

    // Desenha a linha laranja abaixo do texto.
    pdfTemplate.setColorStroke(new BaseColor(251, 150, 34));
    pdfTemplate.setLineWidth(1f);
    pdfTemplate.moveTo(0, 0);
    pdfTemplate.lineTo(getWritableWidth(), 0);
    pdfTemplate.stroke();
    return pdfTemplate;
  }
}
