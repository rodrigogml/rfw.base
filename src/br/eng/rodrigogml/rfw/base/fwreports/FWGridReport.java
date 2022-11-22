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
 * Description: Classe de relatório que permite a geração de um relatório baseada em um "GRID". O desenvolvedor deve extender esta classe e montar a estrutura dos dados e configurações das células de acordo com suas preferências. Uma vez que todos os dados estejam definidos dentro desse GRID (uma matriz) deve-se mandar gerar o relatório. O {@link FWGridReport} montará o relatório quebrando as
 * linhas e as colunas conforme a necessidade.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (28/08/2015)
 */
public abstract class FWGridReport extends FWListReport {

  /**
   * Esta classe tem a finalidade de carregar as definições de estilo. Essas definições poderão ser aplicadas nas colunas ou diretamente nos valores.<br>
   * O {@link FWGridReport} dará preferência para as definições feitas diretamente na célula. Se não houverem definições diretamente na célula as definições das colunas serão utilizadas.<br>
   */
  public static class FWGridReportStyle {

    private Boolean bold = false;
    private Boolean italic = false;
    private ALIGNMENT alignment = null;
    private BaseColor baseColor = null;
    private TextWrapMode textWrapMode = null;

    /**
     * Sobrepões as definições de tamanho da fonte.
     */
    private Float fontSize = null;

    public FWGridReportStyle() {
    }

    /**
     * Construtor completo
     *
     * @param bold Define se a Fonte terá negrito
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
   * Interface usada para agrupar os diferentes tipos de conteúdos em um único objeto pai. Já pensando no dia em que teremos outros tipos de conteúdos, como templates personalizados por exemplo.
   */
  public static interface FWGridReportContent {
    public FWGridReportStyle getStyle();

    /**
     * Recupera quantas colunas esse conteúdo ocupa. O valor padrão é 1. Não pode ser menor que 1.
     */
    public int getColSpan();

    /**
     * Para tipos de conteúdo que não suportem colSpan, podem lançar exceção.
     */
    public void setColSpan(int colSpan) throws RFWException;
  }

  /**
   * Classe usada para definir um conteúdo do tipo String para ser colocado na célula.
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
   * Esta classe representa o conteúdo de um relatório, isto é, um grid de dados para ser impresso nas folhas.
   */
  public static class FWGridReportMatrix {
    /**
     * Configurações de quebra e limite do texto das colunas.
     */
    public static enum TextWrapMode {
      /**
       * Não executa nenhum método. Fará com que o texto seja escrito fora de "templates" o que deixa o PDF mais enxuto e leve, no entanto caso a coluna não tenha espaço suficiente para o conteúdo ele passará por cima da coluna do lado.
       */
      NONE,
      /**
       * Define que o texto poderá ser quebrado em múltiplas linhas.
       */
      TEXTWRAP,
      /**
       * Define que o texto deverá ser "cortado" quando o tamanho da coluna acabar.
       */
      TRUNCATE
    }

    /**
     * Armazena as linhas do grid. Cada item da List será uma linha do Grid. O conteúdo da list é um array com seus conteúdos. O tamanho de cara array será exatamente o definido em {@link #columns}.
     */
    private final LinkedList<FWGridReportContent[]> gridRows = new LinkedList<>();
    /**
     * Define a quantidade de colunas que o Grid terá.
     */
    private final int columns;
    /**
     * Armazena as definições de style das colunas.
     */
    private final FWGridReportStyle[] columnStyles;
    /**
     * Armazena os captions das colunas.
     */
    private final String[] columnCaptions;
    /**
     * Define a largura mínima que a coluna deve ter. Caso não caiba na página ela será passada para uma quebra lateral.<br>
     */
    private final Integer[] columnMinWidth;
    /**
     * Em geral o tamanho aplicado às colunas é o valor definido em ColumnMinWidth. Mas quando uma coluna não cabe mais na página sobre um espaço. A coluna que tiver este atributo definido com true primeiro, se expande para ocupar o espaço restante
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
     * Esta lista carrega as informações sobre a configuração de texto da coluna. Se ela deve quebrar o texto em múltiplas linhas ou não. Se estiver configurada como NONE (ou null - valor padrão) nenhum modo será aplicado. Este método evita a criação de templates mar permite que o texto passe um por cima do outro. <br>
     * Esta configuração só é válida para o conteúdo "String".
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
      if (rowContent == null || rowContent.length != this.columns) throw new ArrayIndexOutOfBoundsException("Tamanho do array com o conteúdo da linha inválido! Linha: " + this.gridRows.size());
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
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      this.columnTextWrapMode[columnIndex] = wrapMode;
    }

    public void setColumnStyle(FWGridReportStyle style, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      this.columnStyles[columnIndex] = style;
    }

    public void setColumnCaption(String caption, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      this.columnCaptions[columnIndex] = caption;
    }

    public void setColumnMinWidth(Integer minWidth, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      this.columnMinWidth[columnIndex] = minWidth;
    }

    public void setColumnExpandable(Boolean expandable, int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      this.columnExpandable[columnIndex] = expandable;
    }

    protected TextWrapMode getColumnTextWrapMode(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      return this.columnTextWrapMode[columnIndex];
    }

    protected FWGridReportStyle getColumnStyle(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      return this.columnStyles[columnIndex];
    }

    protected String getColumnCaption(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      return this.columnCaptions[columnIndex];
    }

    protected Integer getColumnMinWidth(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
      if (this.columnMinWidth[columnIndex] != null) {
        return this.columnMinWidth[columnIndex];
      } else {
        return 100;
      }
    }

    protected Boolean getColumnExpandable(int columnIndex) {
      if (columnIndex >= this.columns || columnIndex < 0) throw new ArrayIndexOutOfBoundsException("Índice de coluna inválido! Índice: " + columnIndex);
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
   * Armazena a lista de relatórios Matrix que ser]ao transformados em relatório. Normalmente só temos uma matriz, mas múltiplas podem ser adicionadas para que múltiplos relatórios sejam gerados de uma única vez.
   */
  private final ArrayList<FWGridReportMatrix> matrixList = new ArrayList<>();

  /**
   * Armazena a lista de lista de blocos para ser passada para o ListReport.<br>
   * Cada block representa uma linha do relatório. Cada lista de blocos um relatório diferente a ser gerado dentro do mesmo PDF.
   */
  private final ArrayList<ArrayList<ReportBlock>> reportBlockLists = new ArrayList<>();

  /**
   * Armazena o índice da próxima lista de reportBlocks que será retornada na chamada do método {@link #getReportBlocksList()}.
   */
  private int reportBlockListsIteratorNextIndex = 0;

  /**
   * Lista de Tamanhos reais das colunas depois de processadas para expandir e ocupar o tamanho total da página. Esses valores são calculados no {@link #prepareReportData()} e são salvos na lista com o mesmo índice da Matrix.
   */
  private final ArrayList<Integer[]> realColumnWidthList = new ArrayList<>();

  /**
   * Lista de index da primeira coluna de cada página. Esses valores são calculados no {@link #prepareReportData()} e são salvos na lista com o mesmo índice da Matrix.<br>
   * O array tem sempre o mesmo tamanho da quantidade de colunas totais da Matrix, no entanto o array só será totalmente ocupado caso só tenhamos 1 coluna por página. O índex do array é o índex da página lateral. Sendo o índex 0 a página principal "A", 1 a primeira página lateral "B", etc.
   */
  private final ArrayList<Integer[]> firstColumnOnPageList = new ArrayList<>();

  /**
   * Esta lista contém arrays com os cabeçalhos já prontos de cada coluna de cada relatório. <Br>
   * Cada array da list representa um relatório. Cada item dentro do array um cabeçalho da coluna. O index do array é o index da coluna dentro da matrix.
   */
  private final ArrayList<PdfTemplate[]> columnsHeadersTemplates = new ArrayList<>();

  /**
   * Este hash é um auxiliar do {@link #getRowHeight(FWGridReportContent[], FWGridReportMatrix)} para evitar que a altura de cada linha seja recalculada diversas vezes durante a escrita do relatório. Normalmente a mesma chamada para cada linha é realizada a cada escrita da célula de uma linha.<br>
   * A hash mais externa: sua chave é o index do report sendo gerado e seu valor outra Hash. Essa hash interna: sua chave é o número da linha e seu valor (Float) a altura computada para alinha.
   */
  private final HashMap<Integer, HashMap<Integer, Float>> rowHeightCache = new HashMap<>();

  /**
   * Este hash é um auxiliar do {@link #getCellTemplate(FWGridReportContent, FWGridReportMatrix)} para evitar que o mesmo conteúdo seja gerado múltiplas vezes. O conteúdo gerado é necessário para cálculo da altura da linha diversas vezes e posteriormente para real escrita no ReportBlock. <br>
   * A hash mais externa: sua chave é o index do report sendo gerado e seu valor outra Hash. Essa hash interna: sua chave é uma string composta pelo número da linha e coluna separados por um '|'. Ex; "35|12" para a linha 35 coluna 12. E seu valor (PdfTemplate) o template com o conteúdo pronto.
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
    // Vamos converter cada Matrix em um relatório diferente (lista de blocks)
    for (FWGridReportMatrix matrix : this.matrixList) {
      Integer[] realColumnWidth = new Integer[matrix.getColumns()]; // Armazena o tamanho real a ser utilizado em cada coluna. Tamanho mínimo + tamanho excedente caso a coluna seja expansível.
      realColumnWidthList.add(realColumnWidth); // Salva o array na lista para uso em outros métodos.

      /*
       * Este array guardará a informação de qual é o índice da primeira coluna alocada naquela página. Definimos o tamanho do array de acordo com o tamanho do número de colunas para evitar problemas, pois no pior caso teremos 1 coluna por página no mínimo. Normalmente este array será muito maior doque o necessário. O índice do array é o número da página lateral, começando no índice 0, sendo o
       * valor o índice da coluna da matrix que é a primeira coluna daquela página.
       */
      Integer[] firstColumnOnPage = new Integer[matrix.getColumns()];
      firstColumnOnPageList.add(firstColumnOnPage); // Salva o array na lista para uso em outros métodos.
      firstColumnOnPage[0] = 0; // Definimos que na primeira página a primeira coluna é a coluna 0. Como é constante e necessário para o funcionamento do código abaixo sem maiores condições (IFs) já fica definido aqui.

      // O primeiro passo é calcular o tamanho real de cada coluna e quantas colunas vão caber em cada página. A medida que estrapolem teremos páginas laterais.
      // O cálculo é feito de acordo com a largura de cada coluna e salvo no array realColumnWidth. Quando a coluna não couber mais na mesma página
      int countExpandable = 0; // Contador de quantas colunas expansíveis encontramos na mesma página
      int sidePage = 0; // Contador de páginas laterais. Para saber em que página estamos colocando cada coluna. Note que diferente do padrão da ListReport aqui o contador começa em 0 para facilitar o uso nos arrays logo abaixo.
      float usedWidth = 0; // Somatória das larguras das colunas da página atual.

      for (int colCount = 0; colCount < matrix.getColumns(); colCount++) {
        float remainWidth = getWritableWidth() - usedWidth; // Calcula a largura restante na página

        // Verificamos se essa coluna 'i' não cabe mais na mesma página. Se não couber quebramos a página lateral. Para evitar problemas caso alguém faça uma coluna maior que o espaço livre para escrita forçamos a falha de entrada no IF caso seja a primeira coluna sendo escrita na página.
        if (matrix.getColumnMinWidth(colCount) > remainWidth && usedWidth > 0) {
          // Se não cabe na página vamos verificar os itens que já estão na página e difividir o espaço restante entre as colunas expansíveis
          if (countExpandable > 0 && remainWidth > 0) { // Se a coluna for maior que o espaço disponível remainWidth retornará negativo, o que vai foder as contas
            int splitWidth = (int) (remainWidth / countExpandable);
            // Reiteramos as colunas desta página para ver quem tem o tamanho expansível para somar a parte que sobrou nele.
            for (int splitCol = firstColumnOnPage[sidePage]; splitCol < colCount; splitCol++) {
              if (matrix.getColumnExpandable(splitCol) != null && matrix.getColumnExpandable(splitCol)) {
                realColumnWidth[splitCol] += splitWidth;
              }
            }
          }
          usedWidth = 0;
          countExpandable = 0;
          sidePage++; // Incrementa para a próxima página lateral
          firstColumnOnPage[sidePage] = colCount; // Salva que essa coluna será a primeira da próxima página lateral.
        }
        realColumnWidth[colCount] = matrix.getColumnMinWidth(colCount); // Salvamos o valor da coluna atual como tamanho real. Durante a quebra de página lateral recalcularemos para redistribuir o espaço restante se houver
        usedWidth += realColumnWidth[colCount];
        if (matrix.getColumnExpandable(colCount) != null && matrix.getColumnExpandable(colCount)) countExpandable++;
      }
      // quando o for acabar ainda verificamos o espaço que sobrou na última página para tentar espalhar esse espaço entre as demais colunas
      float remainWidth = getWritableWidth() - usedWidth; // Calcula a largura restante na página
      if (countExpandable > 0 && remainWidth > 0) { // Se a coluna for maior que o espaço disponível remainWidth retornará negativo, o que vai foder as contas
        int splitWidth = (int) (remainWidth / countExpandable);
        // Reiteramos as colunas desta página para ver quem tem o tamanho expansível para somar a parte que sobrou nele.
        for (int splitCol = firstColumnOnPage[sidePage]; splitCol < matrix.getColumns(); splitCol++) {
          if (matrix.getColumnExpandable(splitCol)) {
            realColumnWidth[splitCol] += splitWidth;
          }
        }
      }

      // Uma vez que o FOR acima calculou as larguras reais de cada coluna (depois da expansão do espaço de sobra) podemos começar a montar a lista de reportBlocks já que sabemos onde cada um vai
      ArrayList<ReportBlock> blockList = new ArrayList<>();
      this.reportBlockLists.add(blockList); // Já adiciona esse relatório à lista de relatórios

      final float lineLeading = TEXTSIZE_NORMAL + LINESPACING; // Define o tamanho de cada linha. Entre Baseline de uma e a Baseline de outra.
      for (FWGridReportContent[] reportContent : matrix.getGridRows()) { // Iteramos cada linha da matrix para gerar as linhas, incluindo as linhas das páginas laterais
        ReportBlock block = new ReportBlock(); // Report Block que terá o conteúdo desta linha da Matrix
        { // Define as configurações da linha no ReportBlock (se existirem)
          if (matrix.containsRowOddLineBackgroundColor(reportContent)) block.setOddLineBackgroundColor(matrix.getRowOddLineBackgroundColor(reportContent));
          if (matrix.containsRowLineBackgroundColor(reportContent)) block.setLineBackgroundColor(matrix.getRowLineBackgroundColor(reportContent));
        }
        if (sidePage > 0) block.setTemplateSidePages(new PdfTemplate[sidePage]); // Se temos páginas laterias, já deixamos o array preparado no tamanho certo para receber os templates que serão criados abaixo.
        blockList.add(block);

        for (int sp = 0; sp <= sidePage; sp++) { // Iteramos o total de páginas laterais que precisam ser criada
          PdfTemplate pdfTemplate = getWriter().getDirectContent().createTemplate(getWritableWidth(), lineLeading);
          float position = 0;
          // Agora vamos iterar as colunas começando na primeira coluna da sidePage atual e limitando até a última coluna da matrix (última sidePage) ou até antes a primeira coluna da próxima sidePage. Note que temos que verificar se firstColumnOnPage[sp+1] não é nulo porque na última página esse valor será nulo. Validamos ainda inclusive se sp não é maior que o length porque no caso de 1 coluna
          // por página sp + 1 dará outofbounds na última página.
          int limitColumn = matrix.getColumns(); // Inicialmente limita a coluna no total de colunas. Abaixo vamos rever esse limite se encontrarmos uma definição de primeira coluna para a próxima página lateral
          if (sp + 1 < firstColumnOnPage.length && firstColumnOnPage[sp + 1] != null) limitColumn = firstColumnOnPage[sp + 1];
          for (int col = firstColumnOnPage[sp]; col < limitColumn; col++) {
            // Escrevemos o content de acordo com seu style definido
            writeCellGrid(pdfTemplate, reportContent, col, matrix, position, realColumnWidth);
            position += realColumnWidth[col];
          }

          // Com o template criado e totalmente escrito, colocamos no Block de acordo com a contagem de páginas laterais.
          if (sp == 0) {
            block.setTemplateMainPage(pdfTemplate);
          } else {
            block.getTemplateSidePages()[sp - 1] = pdfTemplate;
          }
        }
      }

      // Depois de ter os dados preparados, já vamos deixar preparado os templates dos cabeçalhos das colunas.
      // Os cabeçalhos são preparados antes por duas razões:
      // 1-Se usarmos os mesmos templates em todas as páginas teremos um PDF mais enxuto.
      // 2-Ter todos os cabeçalhos já preparados desde o início facilita pra calcular o tamanho do template para o PageHeader de todas as páginas, já que diferentes páginas poderão ter diferententes tamanhos de textos e de cabeçalhos.

      // Vamos iterrar todas as colunas para gerar seus cabeçalhos e salvar no array
      PdfTemplate[] headerTempls = new PdfTemplate[matrix.getColumns()];
      this.columnsHeadersTemplates.add(headerTempls);
      for (int i = 0; i < matrix.getColumns(); i++) {
        int alignment = Element.ALIGN_LEFT; // Deixamos o alinhamento a esquerda já definido para servir de padrão se nenhum alinhamanheo for definido.
        if (matrix.getColumnStyle(i).getAlignment() == ALIGNMENT.CENTER) {
          alignment = Element.ALIGN_CENTER;
        } else if (matrix.getColumnStyle(i).getAlignment() == ALIGNMENT.RIGHT) {
          alignment = Element.ALIGN_RIGHT;
        }
        // Cria o template sem limitação de altura, afinal os cabeçalhos são feitos pelos programadores, espera-se que só façam cabeçalhos grandes se for realmente necessário.
        headerTempls[i] = createTextFieldWraped(matrix.getColumnCaption(i), new Font(getBaseFontBold(), TEXTSIZE_NORMAL), realColumnWidth[i], lineLeading, 3, alignment, Float.MAX_VALUE);
      }
    }
  }

  /**
   * Este método tem a finalidade de escrever o valor da célula no template. <Br>
   * Este método altera a altura do template, caso a altura do template seja menor que a altura necessária para o template da célula.
   *
   * @param pdfTemplate Template PDF no qual o campo será escrito.
   * @param reportContent Conteúdo da Matrix para ser escrito.
   * @param col Índice da coluna a ser escrito.
   * @param matrix Matrix com todas as informações do relatório.
   * @param position Posição em que a coluna deve ser escrita no template. Essa posição é sempre o valor a esquerda da coluna.
   * @param realColumnWidth Largura real de cada coluna. Calculada depois de redistribuir o espaço restante de cada página pelas colunas expansíveis.
   * @throws RFWWarningException
   */
  private void writeCellGrid(PdfTemplate pdfTemplate, FWGridReportContent[] reportContent, int col, FWGridReportMatrix matrix, float position, Integer[] realColumnWidth) throws RFWWarningException {
    // Se esta célula não tem conteúdo (comum em casos de colspan) simplesmente retornamos e não fazemos nada.
    if (reportContent[col] == null) return;

    FWGridReportStyle style = getMergedStyle(matrix.getColumnStyle(col), reportContent[col].getStyle());

    if (reportContent[col] instanceof FWGridReportStringContent) {
      FWGridReportStringContent content = (FWGridReportStringContent) reportContent[col];

      // Obtem o tamanho da linha para cálculos
      final int row = matrix.getGridRows().indexOf(reportContent);
      Float rowHeight = getRowHeight(reportContent, matrix);
      if (pdfTemplate.getHeight() < rowHeight) pdfTemplate.setHeight(rowHeight);

      // Calcula a largura da coluna. Inclui os valores das colunas adicionais nos casos de colspan
      int colTotalWidth = 0;
      for (int i = 0; i < content.getColSpan(); i++) {
        colTotalWidth += realColumnWidth[col + i];
      }

      // Verifica se há algum tipo de quebra de texto definido, se não houver só escrevermos o texto, se houver solicitamos a criação do template no método "centralizador"
      TextWrapMode wrapMode = matrix.getColumnTextWrapMode(col);
      if (style.getTextWrapMode() != null) wrapMode = style.getTextWrapMode(); // Se tiver um TextWarp no style, sobrepõe o herdado da Matriz
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

        // Escreve o conteúdo
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
        // Para os casos de Truncate e TextWarp, colocamos o template na posição descontando o offset de 3 que é dado por padrão pelo método de getCellTemplate
        PdfTemplate cellTemplate = getCellTemplate(row, col, matrix);
        pdfTemplate.addTemplate(cellTemplate, position, ((rowHeight - cellTemplate.getHeight() + 3) / 2));
      }
    } else {
      throw new RFWRunTimeException("Conteúdo não suportado para escrita pelo FWGridReport!");
    }
  }

  /**
   * Este método tem a finalidade de calcular a altura de uma detarminada linha do relatório de acordo com as configurações das colunas e seus conteúdos. De forma centralizada, este método deve ser capaz de observar todo o conteúdo da linha e calcular sua altura adequadamente para que outros métodos (como o
   * {@link #writeCellGrid(PdfTemplate, FWGridReportContent[], int, FWGridReportMatrix, float, Integer[])} possam utilizar esse valor na hora de escrever.
   *
   * @param rowContent Linha para análise.
   * @param matrix Matrix do relatório para obter as configurações e valores necessários para o cálculo.
   * @return Retorna a altura da linha.
   * @throws RFWWarningException
   */
  private Float getRowHeight(FWGridReportContent[] rowContent, FWGridReportMatrix matrix) throws RFWWarningException {
    // Verifica se já temos algum valor na cache
    HashMap<Integer, Float> reportCache = this.rowHeightCache.get(getReportCount());
    if (reportCache == null) {
      reportCache = new HashMap<>();
      this.rowHeightCache.put(getReportCount(), reportCache);
    }
    int row = matrix.getGridRows().indexOf(rowContent);
    Float height = reportCache.get(row);
    if (height == null) {
      // Itera seu conteúdo para processar e verifica a altura de cada um
      height = 0f;
      for (int col = 0; col < rowContent.length; col++) {
        FWGridReportContent content = rowContent[col];
        if (content != null) {
          if (content instanceof FWGridReportStringContent) {
            final FWGridReportStyle style = getMergedStyle(matrix.getColumnStyle(col), content.getStyle());

            // Se é do tipo string sua altura tem o tamanho padrão de tamanho da fonte e espaçamento entre linhas, exceto se tiver quebra de linha definida. Neste caso teremos de obter o template para saber o tamanho resultante.
            TextWrapMode mode = matrix.getColumnTextWrapMode(col);
            if (style != null && style.getTextWrapMode() != null) mode = style.getTextWrapMode(); // Se tiver um TextWarp no style, sobrepõe o herdado da Matriz

            float fontSize = TEXTSIZE_NORMAL;
            if (style != null && style.getFontSize() != null) fontSize = style.getFontSize();
            if (mode == null || mode == TextWrapMode.NONE || mode == TextWrapMode.TRUNCATE) {
              // Se não tiver modo, ou se for truncate, o tamanho da linha é o tamanho padrão de Text e espaçamento de linha.
              if (height < fontSize + LINESPACING) height = fontSize + LINESPACING;
            } else if (mode == TextWrapMode.TEXTWRAP) {
              // Com quebra de texto verificamos o tamanho do template gerado para ver o tamanho que a linha terá que ter para incorpora-lo.
              PdfTemplate cellTemplate = getCellTemplate(row, col, matrix);
              if (height < cellTemplate.getHeight()) height = cellTemplate.getHeight();
            } else {
              throw new RFWRunTimeException("Tipo de Quebra de texto desconhecido pelo método de cálculo de altura da linha! Impossível gerar relatório! " + mode);
            }
          } else {
            throw new RFWRunTimeException("Conteúdo desconhecido pelo método de cálculo de altura da linha! Impossível gerar relatório! " + content.getClass().getCanonicalName());
          }
        }
      }
      // Salvamos o valor na cache
      reportCache.put(row, height);
    }
    return height;
  }

  /**
   * Este método é usado para gerar o template de uma célula quando necessário. Exceto pelos conteúdos de String sem Quebra de linha que são escritos diretamente no PDF, este método deve gerar o template que representa o conteúdo da célula. Seus tamanhos já devem estar de acordo com o necessário para escrita na linha. <Br>
   * <B>Atenção: este método pode retornar null se o conteúdo não obriga a geração do template!</b>
   *
   * @param col Coluna do conteúdo
   * @param row Linha do Conteúdo
   * @param matrix Matrix do relatório para obter as configurações da coluna.
   *
   * @return Template com o conteúdo pronto para ser usado como conteúdo da célula do grid.
   * @throws RFWWarningException
   */
  private PdfTemplate getCellTemplate(int row, int col, FWGridReportMatrix matrix) throws RFWWarningException {
    // Verifica se já temos algum valor na cache
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

      BaseFont baseFont = getBaseFontPlain(); // inicializa a variável com a font comum. Garante que não chegará null nos métodos de uso.
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
        // Se é do tipo string sua altura tem o tamanho padrão de tamanho da fonte e espaçamento entre linhas, exceto se tiver quebra de linha definida. Neste caso teremos de obter o template para saber o tamanho resultante.
        TextWrapMode mode = matrix.getColumnTextWrapMode(col);
        if (style.getTextWrapMode() != null) mode = style.getTextWrapMode(); // Se tiver um TextWarp no style, sobrepõe o herdado da Matriz
        if (mode == null || mode == TextWrapMode.NONE) {
          // Não gera template neste caso, deixa seguir null
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

          // Por padrão todos os templates do tipo string estão sendo escritos com offset de 3 para não cortarem as letras com conteúdo abaixo da baseline, e ao serem posicionados o valor é descontado na coordenada para manter o baseline.
          tpl = createTextFieldClipped(baseFont, fontSize, align, c.getContent(), x, 3f, 0f, colTotalWidth, fontSize + LINESPACING);
        } else if (mode == TextWrapMode.TEXTWRAP) {
          int align = Element.ALIGN_LEFT;
          if (style.getAlignment() == ALIGNMENT.CENTER) {
            align = Element.ALIGN_CENTER;
          } else if (style.getAlignment() == ALIGNMENT.RIGHT) {
            align = Element.ALIGN_RIGHT;
          }
          // Por padrão todos os templates do tipo string estão sendo escritos com offset de 3 para não cortarem as letras com conteúdo abaixo da baseline, e ao serem posicionados o valor é descontado na coordenada para manter o baseline.
          tpl = createTextFieldWraped(c.getContent(), new Font(baseFont, fontSize), colTotalWidth, fontSize + LINESPACING, 3, align, Float.MAX_VALUE);
        } else {
          throw new RFWRunTimeException("Tipo de Quebra de texto desconhecido pelo método de cálculo de altura da linha! Impossível gerar relatório! " + mode);
        }
      }
      // Salva o template na cache
      reportCache.put(row + "|" + col, tpl);
    }
    return tpl;
  }

  /**
   * Este método tem a finalidade de unir as definições em um único objeto Style.<br>
   * Lembrando que a prioridade é sempre do style definido diretamente na célula do Grid. Qualquer definição que esteja feita no cellStyle não será sobreescrita.<br>
   * Para evitar a criação de novos objetos, as informações necessárias serão copiadas do columnStyle para o cellStyle. Como o objeto recebido será alterado, este método pode destruir as informações originais do objeto. Caso essas informações se tornem importantes trocar a implementação deste método para criar objetos distintos.
   *
   * @param columnStyle Estilo da Coluna
   * @param cellStyle Estilo da célula.
   * @return objeto com o estilo resultante da mescla entre os dois. Pode retornar nulo caso ambos os styles passados sejam nulo.
   */
  private FWGridReportStyle getMergedStyle(FWGridReportStyle columnStyle, FWGridReportStyle cellStyle) {
    if (cellStyle == null) {
      // Se cellstyle é nulo simplesmente retornamos o columnStyle. Se ele for nulo também paciência...
      cellStyle = columnStyle;
    } else if (columnStyle != null) {
      // Se cellstyle não é nulo, e columnstyle também não, fazemos o merge. Se columnstyle for nulo, retornamos o próprio cellStyle pois não haverá nada para realizar o merge.
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
    // Obtem a Matrix correta de acordo com o relatório atual
    FWGridReportMatrix matrix = this.matrixList.get(getReportCount());

    // Obtemos os cabeçalhos das colunas criados para este relatório durante o método de prepareReportData
    final PdfTemplate[] headerTempls = this.columnsHeadersTemplates.get(getReportCount());

    // Vamos iterar todos os templates para saber qual é a altura do maior e usar de guia para posicionar todos os demais templates
    float maxHeight = 0;
    for (int col = 0; col < headerTempls.length; col++) {
      PdfTemplate hTmpl = headerTempls[col];
      if (headerTempls[col] != null && hTmpl.getHeight() > maxHeight) maxHeight = hTmpl.getHeight();
    }

    PdfTemplate pdfTemplate = getWriter().getDirectContent().createTemplate(getWritableWidth(), maxHeight);

    // Para saber quais colunas escrever e seus valores vamos separar alguns valores
    Integer[] firstColumnOnPage = this.firstColumnOnPageList.get(getReportCount());
    Integer firstColumn = firstColumnOnPage[getPageSideCount() - 1]; // Subtraimos 1 do PageSideCount pois ListReport começa sua contagem em 1 para a página A, e não em 0 como usado aqui no GridReport
    Integer lastColumn = matrix.getColumns() - 1; // Iniciamos com o índice da última coluna, ai verificamos se temos uma outra página lateral a seguir com a informação da primeira coluna que será da próxima coluna e abaixamos esse valor
    if (getPageSideCount() < firstColumnOnPage.length && firstColumnOnPage[getPageSideCount()] != null) lastColumn = firstColumnOnPage[getPageSideCount()] - 1;

    // Agora iteramos as colunas de definidas para a página atual
    int pos = 0;
    for (int col = firstColumn; col <= lastColumn; col++) {
      if (headerTempls[col] == null) continue;
      // Vamos colocar lado a lado no template a ser retornado cada um dos templates das colunas. A altura do posicionamento é calculada para que ela fique centralizada
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
