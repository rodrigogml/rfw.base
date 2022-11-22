package br.eng.rodrigogml.rfw.base.fwreports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.fwreports.FWListReport.ReportBlock.ALIGNMENT;
import br.eng.rodrigogml.rfw.base.fwreports.bean.FWListReportOptionBean;

/**
 * Description: Classe de execu��o de relat�rio de listagem.<BR>
 * <br>
 * O {@link FWListReport} extende a classe principal de relat�rios {@link FWReport} e cria um template mais simples para relat�rios do tipo "listagem". Bastando que o relat�rio de listagem implemente os m�todos definidos aqui como abstratos.<br>
 * O funcionamento "b�sico" consiste em receber da classe filha uma lista de {@link ReportBlock} que representam os "blocos" a serem colocados na p�gina um a baixo do outro. Normalmente cada bloco representa uma linha do relat�rio, mas podem tamb�m conter gr�ficos, imagens, e qualquer informa��o desejada. Quando n�o couberem mais blocos na mesma linha o relat�rio ser� automaticamente quebrado em
 * uma nova p�gina.<br>
 * H� a possibilidade tamb�m de criar blocos para p�ginas "laterais". Para relat�rios com muitas colunas � poss�vel expandi-lo para p�ginas que ser�o colocadas "� direita" de cada p�gina do relat�rio. Essas p�ginas, quando existentes receber�o letras para indicar a numera��o de p�ginas adicionais. Formando uma esp�cie de grid, similar � uma planilha de c�lculo.
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (06/06/2015)
 */
public abstract class FWListReport extends FWReport {

  /**
   * Classe que representa um "bloco" do relat�rio. Um bloco normalmente � uma linha, um gr�fio, etc. Uma cole��o de blocos comp�e um relat�rio.
   */
  public static class ReportBlock {

    public static enum ALIGNMENT {
      LEFT, CENTER, RIGHT
    }

    /**
     * Define o alinhamento do bloco.
     */
    private ALIGNMENT alighnment = ALIGNMENT.LEFT;

    /**
     * Define o template a ser desenhado no p�gina "principal" do relat�rio.<br>
     */
    private PdfTemplate templateMainPage = null;

    /**
     * Define os templates que ser�o colocados nas p�ginas � direita da p�gina principal. Cada item deste array ser� colocado em um nova p�gina "� direita" da p�gina principal do relat�rio.
     */
    private PdfTemplate[] templateSidePages = null;

    /**
     * Define uma margem superior para ser adicionada ao bloco. Usada para separar o bloco do bloco superior.<br>
     * <b>Esta margem � descartada caso seja o primeiro bloco da p�gina.</b>
     */
    private float margintop = 0f;

    /**
     * Este atributo define a cor a ser usada para imprimir o background de linha impar. Para que o background apare�a � obrigat�rio que a funcionalidade esteja ligada no ReportBean.<br>
     * Para garantir que algum bloco espec�fico n�o ter� uma cor de fundo defina este atributo como null. Ao definir como null o relat�rio reiniciar� a contagem das linhas, para que no pr�ximo bloco as linhas reiniciem a contagem de linha impar novamente. Isso evita que a cada bloco (com linha de fundo) depois de blocos sem linhas de fundo comecem com cores diferentes.<br>
     * O valor da cor deve ser definido como padr�o WEB, sempre contendo o "#" para deixar evidente e incluir os 6 d�gitos.
     */
    private String oddLineBackgroundColor = "#F5F5F5";

    /**
     * Define a cor de fundo de uma linha do relat�rio. Essa propriedade se sobrep�e a cor definida em oddLineBackgroundColor.<br>
     * O valor da cor deve ser definido como padr�o WEB, sempre contendo o "#" para deixar evidente e incluir os 6 d�gitos.
     */
    private String lineBackgroundColor = null;

    /**
     * Caso true, a p�gina � quebrada antes de escrever este bloco (n�o ser� quebrada se for o primei block j� de uma nova p�gina)
     */
    private Boolean newPage = Boolean.FALSE;

    /**
     * Se True, nenhum cabe�alho ser� escrito na p�gina.<br>
     * <b>ATEN��O:</b> Para funcionar essa op��o deve estar habilitada no primeiro bloco da p�gina.
     */
    private Boolean forbidHeaders = Boolean.FALSE;

    /**
     * Se True, nenhum rodap� ser� escrito na p�gina.<br>
     * <b>ATEN��O:</b> Para funcionar essa op��o deve estar habilitada no primeiro bloco da p�gina.
     */
    private Boolean forbidFooters = Boolean.FALSE;

    public ReportBlock() {
    }

    public ReportBlock(PdfTemplate templatePageA) {
      this.templateMainPage = templatePageA;
    }

    public ALIGNMENT getAlighnment() {
      return alighnment;
    }

    public ReportBlock setAlighnment(ALIGNMENT alighnment) {
      this.alighnment = alighnment;
      return this;
    }

    public PdfTemplate getTemplateMainPage() {
      return templateMainPage;
    }

    public ReportBlock setTemplateMainPage(PdfTemplate pdftemplate) {
      this.templateMainPage = pdftemplate;
      return this;
    }

    public float getMargintop() {
      return margintop;
    }

    public ReportBlock setMargintop(float margintop) {
      this.margintop = margintop;
      return this;
    }

    public PdfTemplate[] getTemplateSidePages() {
      return templateSidePages;
    }

    public void setTemplateSidePages(PdfTemplate[] templateOtherPages) {
      this.templateSidePages = templateOtherPages;
    }

    public String getOddLineBackgroundColor() {
      return oddLineBackgroundColor;
    }

    public void setOddLineBackgroundColor(String oddLineBackgroundColor) {
      this.oddLineBackgroundColor = oddLineBackgroundColor;
    }

    public Boolean getNewPage() {
      return newPage;
    }

    public void setNewPage(Boolean newPage) {
      this.newPage = newPage;
    }

    public Boolean getForbidHeaders() {
      return forbidHeaders;
    }

    public void setForbidHeaders(Boolean forbidHeaders) {
      this.forbidHeaders = forbidHeaders;
    }

    public Boolean getForbidFooters() {
      return forbidFooters;
    }

    public void setForbidFooters(Boolean forbidFooters) {
      this.forbidFooters = forbidFooters;
    }

    public String getLineBackgroundColor() {
      return lineBackgroundColor;
    }

    public void setLineBackgroundColor(String lineBackgroundColor) {
      this.lineBackgroundColor = lineBackgroundColor;
    }
  }

  // Vari�veis de Controle do Relat�rio
  private boolean wroteReportHeader = false; // Indica se o cabe�alho do relat�rio j� foi escrito.
  private boolean wrotePageHeader = false; // Indica se o cabe�alho do p�gina j� foi escrito.
  private boolean wrotePageFooter = false; // Indica se o rodap� do p�gina j� foi escrito.
  private int blocksOnPageCount = 0; // Contador de blocos na p�gina. Em outras palavras: 0 indica que ainda n�o temos nenhum block escrito na p�gina, >0, indica que j� temos a quantidade indicada escrita na p�gina.
  private boolean oddPage = true; // Flag que indica se estamos em uma linha impar
  private int pageCount = 1; // Indica em qual p�gina o relat�rio est� atualmente.
  private int pageSideCount = 1; // Indica em qual p�gina lateral est� atualmente. 1 � a p�gina A, 2 a p�gina B, etc.
  private float y = 0; // Vari�vel que guarda o ponto vertical em que podemos continuar a escrever/desenhar
  private float wastePageFooterHeight = 0; // Armazena a altura perdida com o Rodap� de P�gina na p�gina atual.
  private int reportCount = 0; // Conta o �ndex do relat�rio atual. Sendo 0 o primeiro relat�rio, 1 o segundo relat�rio, etc.. Consireramos 1 realat�rio cada lista de ReportBlocks retornados atrav�s do m�todo getReportBlocksList(); Pode se considerar que � um contador de quantas vezes o m�todo j� foi chamado, embora o valor v� permanecer no "zero" (no mesmo valor) at� que o m�todo seja chamado
  // novamente.

  /**
   * Esta hash � usada para que, durante a itera��o e cria��o da p�gina principal, seja salvo quais blocos pertencem a cada p�gina.<br>
   * Depois que a p�gina principal � criada, essas informa��es ser�o utilizadas para gerar as p�ginas laterias com os mesmos blocks.<br>
   * A chave da hash � o n�mero da p�gina (valor de {@link #pageCount} no momento da cria��o. O valor � uma lista com ReportBlocks ordenados conforme recebido da classe filha.
   */
  private final HashMap<Integer, ArrayList<ReportBlock>> pageBlocksIndexMap = new HashMap<Integer, ArrayList<ReportBlock>>();

  public FWListReport(FWListReportOptionBean reportbean) throws RFWException {
    super(reportbean);
  }

  /**
   * Este m�todo � chamado depois que o Documento j� foi aberto, mas antes de iterar os relat�rios.<br>
   * Tem a finalidade de permitir que a classe filha possa preparar os relat�rios antes da itera��o.
   *
   * @throws RFWException
   */
  protected abstract void prepareReportData() throws RFWException;

  /**
   * Este m�todo ser� chamado diversas vezes para obter a lista de blocos de cada relat�rio.<br>
   * A classe filha deve retornar uma lista de PdfTemplate, sendo que cada Template representa o pr�ximo bloco a ser escrito na listagem. O bloco normalmente � uma linha do relat�rio, embora possa ser at� uma linha dupla ou algum conte�do mais espec�fico. Sempre que o bloco couber na mesma p�gina ele ser� imprsso na mesma p�gina. Quando o bloco n�o couber na mesma p�gina ele automaticamente ser�
   * jogado na pr�xima folha.<br>
   * Este m�todo ser� chamado recursivamente at� que retorne null. A cada bloco retornado o sistema considerar� como um "relat�rio diferente". Far� parte do mesmo PDF gerado, mas reiniciar� a contagem de p�ginas, redesenhar� o cabe�alho de relat�rio e assim por diante. O resultado � similar � diversos relat�rios concatenados em um �nico arquivo.
   *
   * @return
   * @throws RFWException
   */
  protected abstract List<ReportBlock> getReportBlocksList() throws RFWException;

  /**
   * Este m�todo deve escrever o cabe�alho de relat�rio e retorna-lo como um template.<br>
   * O Cabe�alho de relat�rio s� aparece no come�o de um novo relat�rio. N�o ser� repetido a cada nova p�gina. Escreva neste cabe�alho apenas as informa��es de identifica��o do relat�rio.
   */
  protected abstract PdfTemplate writeReportHeader() throws RFWException;

  /**
   * Este m�todo deve escrever o cabe�alho de p�gina e retorna-lo como um template.<br>
   * O Cabe�alho de p�gina normalmente repete os nomes das colunas da listagem para que o leitor saiba o que significam os dados. Noralmente � um conte�do curto para ocupar pouco espa�o e evitar o consumo desnecess�rio de folhas.
   */
  protected abstract PdfTemplate writePageHeader() throws RFWException;

  /**
   * Este m�todo � chamado quando a {@link FWListReport} termina de imprimir o relat�rio.<br>
   * Sua implementa��o pode ser "vazia", n�o h� necessidade dela para a {@link FWListReport}.<br>
   * Sua exist�ncia tem a finalidade de proporcionar �s classes filhas que possam fazer �ltimos ajustes do relat�rio. Como por exemplo, a impress�o do n�mero de p�ginas em cada p�gina passada no formato "P�gina X de Y" s� pode ser feita no final, que � quando saberemos o valor de Y.
   */
  protected abstract void onReportFinished() throws RFWException;

  @Override
  protected void writeReportContent() throws RFWException {
    prepareReportData();

    List<ReportBlock> blocklist = getReportBlocksList();

    while (blocklist != null) {
      // Inicializa as vari�veis de controle neste ponto, antes de come�ar a itera��o. Isso porque at� chegar neste ponto a classe filha pode ter alterado algumas configura��es do relat�rio que podem redefinir os valores dessas vari�veis. Como por exemplo, tamanho do papel ou margens.
      y = getCoordFromTop(0);

      for (ReportBlock rblock : blocklist) {
        writeBlockOnCurrentPage(rblock);
      }

      // Depois de iterarmos todo o conte�do do relat�rio, verificamos se temos p�ginas lateria para serem feitas E SE a orienta��o � vertical. Isso porque quando a orienta��o � horizontal o tratamento de impress�o das p�ginas laterais � feito a cada quebra de p�gina.
      if (getReportBean().getVerticalOrderPage()) {
        int maxPages = this.pageCount; // Salva o n�mero da �ltima p�gina para orientar a itera��o
        int maxSidePages = countMaxSidePages(blocklist);

        // iteramos a quantidade de p�ginas laterais que ser�o criadas
        while (this.pageSideCount <= maxSidePages) {
          this.pageCount = 1; // Reiniciamos a contagem da primeira p�gina.
          createPage(false); // Cria nova p�gina lateral - o que j� incrementa pageSideCount
          boolean usedpage = false; // Flag usada para definir se a p�gina foi usada ou n�o. Se ela n�o fui usada (caso nenhum bloco indexado na pagina atual tenha um template para esta p�gina ela n�o � usada, podendo ser aproveitada para a pr�xima itera��o sem gerar uma p�gina em branco).

          // Para cada coluna de pagina lateral, vamos iterar p�gina por p�gina criada
          while (this.pageCount <= maxPages) {
            ArrayList<ReportBlock> pageBlocks = getReportBlockFromPageIndex(this.pageCount);
            // Iteramos cada um dos blocos indexados na p�gina atual
            for (ReportBlock rblock : pageBlocks) {
              if (rblock.getTemplateSidePages() != null && rblock.getTemplateSidePages().length >= this.pageSideCount - 1 && rblock.getTemplateSidePages()[this.pageSideCount - 2] != null) {
                writeBlockOnCurrentPage(rblock);
                usedpage = true;
              }
            }
            // Quando acaba a itera��o dos itens da uma p�gina, temos que quebrar a p�gina, isso porque os blocks j� est�o na medida e n�o quebram a p�gina dentro do m�todo writeBlockOnCurrentPage.
            if (usedpage) {
              breakPage();
            } else {
              // se n�o utilizou a p�gina temos que pelo menos avan�ar o n�mro da p�gina para n�o ter erro de que p�gina estamos agora
              this.pageCount++;
            }
          }
        }
      } else { // Se a ordem � Horizontal, na �ltima p�gina temos que escrever as p�ginas laterias da �ltima p�gina
        ArrayList<ReportBlock> pageBlocks = getReportBlockFromPageIndex(this.pageCount);

        // Se temos que quebrar na horizontal, primeiro calculamos o m�ximo de p�ginas laterais que esta p�gina tem em seu �ndice (evitando de imprimir p�ginas em branco caso os templates n�o tenham todos p�ginas laterais).
        int maxLateralPages = countMaxSidePages(pageBlocks);
        // Verificamos se temos mais P�gina lateral para avan�ar ou se j� finalizamos o m�ximo de p�ginas laterais
        while (this.pageSideCount < maxLateralPages + 1) {
          createPage(false);
          // Note que no caso de avan�o horizontal ao inv�s do vertical, a l�gica de iniciar a escrita da pr�xima p�gina fica aqui no break page, afinal aqui conseguimos saber quando a p�gina acabou para avan�ar para a lateral. Quando o avan�o � vertical, a l�gica de escrever as p�ginas laterais � controla pelo m�todo de writeReportContent
          for (ReportBlock rblock : pageBlocks) {
            writeBlockOnCurrentPage(rblock);
          }
        }

      }

      // Quando acabamos de fazer o relat�rio, avisamos a classe filha
      onReportFinished();

      // Agora que acabou o relat�rio recebido, verificamos se teremos um novo para fazer:
      blocklist = getReportBlocksList();
      if (blocklist != null) {
        // Se temos um novo relat�rio fazemos a quebra de relat�rio
        breakReport();
      }
      reportCount++; // Incrementa o contador de relat�rios prontos antes de come�ar o pr�ximo.
    }
  }

  /**
   * Este m�todo separa a l�gica de escrever um determinado bloco em uma p�gina.<br>
   * Este m�todo tamb�m � respons�vel por "analisar" a p�gina e verificar a necessidade de escrever cabe�alhos, rodap�s, espa�o restante etc.<br>
   * <b>Note que caso o bloco a ser escrito n�o caiba na p�gina atual, este m�todo for�ar� a quebra da p�gina!!!</b>
   *
   * @param cb
   * @param rblock
   * @param block
   * @throws RFWException
   */
  private void writeBlockOnCurrentPage(ReportBlock rblock) throws RFWException {
    final PdfTemplate template;
    if (this.pageSideCount == 1) {
      template = rblock.getTemplateMainPage();
    } else {
      template = rblock.getTemplateSidePages()[this.pageSideCount - 2];
    }
    final PdfContentByte cb = getWriter().getDirectContent();

    // Se n�o � o primeiro bloco adicioamos a "margem superior" do bloco em "y" para que o c�lculo do espa�o j� considere o lugar correto de colocar o template.
    if (blocksOnPageCount > 0) y -= rblock.getMargintop();

    // buscamos e trabalhamos com o tamanho do maior template de qualquer p�gina (entre principal e laterais) para que ao iterar cada p�gina (laterais e principal) todas fiquem sincronizadas caso os templates tenham tamanhos diferentes.
    float maxBlockHights = getMaxBlockHights(rblock);

    // Verificamos se N�O Temos espa�o na folha para escrever o pr�ximo bloco. Lembrando que em toda p�gina o rodap� de p�gina � obrigat�rio e deve ser descontado do espa�o "livre" restante.
    // Tamb�m quebra a p�gina caso o bloco exiga uma nova p�gina e j� tivermos algum bloco escrito na p�gina atual.
    if (y - maxBlockHights - this.wastePageFooterHeight < getCoordFromBottom(0) || (rblock.getNewPage() && this.blocksOnPageCount > 0)) {
      // NOTA: na primeira itera��o da p�gina ou do relat�rio o valor de espa�o restante n�o � 100% preciso j� que ainda n�o escrevemos os cabe�alhos. No entanto, s� teremos um erro se algu�m fizer um relat�rio absurdo em que em uma folha n�o caiba ambos os cabe�alhos, o rodap� e 1 bloco.
      // Se n�o temos espa�o temos que quebrar a folha automaticamente
      breakPage();
    }

    // Os cabe�alhos e Rodap�s s� ser�o escritos se: 1-Ainda n�o tiverem sido escritos; 2-Se ainda n�o tiver nenhum bloco escrito na p�gina; 3-Se o primeiro bloco da p�gina n�o tiver as op��es de forbidHeaders ou forbidFooters desligadas
    // Antes de escrever a nova linha, verificamos se j� escrevemos o cabe�alho de relat�rio
    if (!wroteReportHeader && this.blocksOnPageCount == 0 && !rblock.getForbidHeaders()) {
      final PdfTemplate t = writeReportHeader();
      if (t != null) {
        y -= t.getHeight();
        cb.addTemplate(t, getCoordFromLeft(0), y - 5);
      }
      this.wroteReportHeader = true;
    }
    // Em seguida verificamos se precisamos escrever o cabe�alho de p�gina
    if (!wrotePageHeader && this.blocksOnPageCount == 0 && !rblock.getForbidHeaders()) {
      final PdfTemplate t = writePageHeader();
      if (t != null) {
        y -= t.getHeight();
        cb.addTemplate(t, getCoordFromLeft(0), y - 5);
      }
      this.wrotePageHeader = true;
    }
    if (!wrotePageFooter && this.blocksOnPageCount == 0 && !rblock.getForbidFooters()) {
      final PdfTemplate t = writePageFooter();
      if (t != null) {
        // O rodap� n�s escrevemos mas n�o movimentamos "y", j� que ele � escrito no final da p�gina e n�o precisa "empurrar o conte�do" mais para baixo. Apenas salvamos o seu tamanho para calcular quando a folha acaba
        this.wastePageFooterHeight = t.getHeight();
        cb.addTemplate(t, getCoordFromLeft(0), getCoordFromBottom(0));
      }
      this.wrotePageFooter = true;
    }
    // Por fim escreve o bloco agora que temos "y" definido de acordo com a posi��o livre do campo
    y -= +maxBlockHights; // primeiro decrementa a posi��o de y porque o template tem sua origem no canto bottomleft
    // Calcula a posi��o X para colocar o bloco de acordo com a defini��o de alinhamento
    float x = getCoordFromLeft(0); // LEFT
    if (rblock.getAlighnment() == ALIGNMENT.CENTER) {
      x = getCoordFromMiddleX(0) - (template.getWidth() / 2);
    } else if (rblock.getAlighnment() == ALIGNMENT.RIGHT) {
      x = getCoordFromRight(0) - template.getWidth();
    }

    if (rblock.getLineBackgroundColor() != null) {
      // Verificamos se temos uma cor de fundo do bloco, se tiver desenhamos antes de fazer o resto do template.
      cb.saveState();
      cb.rectangle(x, y - 5, template.getWidth(), template.getHeight());
      cb.setColorFill(createColor(rblock.getLineBackgroundColor()));
      cb.fill();
      cb.restoreState();
    } else {
      if (rblock.getOddLineBackgroundColor() != null) {
        // Verificamos se vamos escrever um background, obviamente antes de escrever o template, e s� escrito em linhas pares
        if (getReportBean().isPrintOddBackgrounds() && !oddPage) {
          cb.saveState();
          cb.rectangle(x, y - 5, template.getWidth(), template.getHeight());
          cb.setColorFill(createColor(rblock.getOddLineBackgroundColor()));
          cb.fill();
          cb.restoreState();
        }
      } else {
        // Se a cor de fundo est� nula, significa que este bloco n�o "conta" como fundo de linha, neste caso reiniciamos a flag para que o pr�ximo bloco que possa ter o fundo pintado seja considerado como bloco �mpar.
        oddPage = false; // Reiniciamos como par, pois logo abaixo o valor ser� invertido
      }
    }

    cb.addTemplate(template, x, y - 5);
    addReportBlockToCurrentPageIndex(rblock); // Se escrevemos o bloco na p�gina j� o indexamos a esta p�gina
    blocksOnPageCount++;
    oddPage = !oddPage;

  }

  private BaseColor createColor(String colorStr) {
    return new BaseColor(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
  }

  /**
   * M�todo que busca entre todos os templates do block (templates da p�gina principal e laterais) o maior tamanho usado.
   *
   * @param rblock Bloco contendo os templates
   * @return
   */
  private float getMaxBlockHights(ReportBlock rblock) {
    float height = 0;
    if (rblock.getTemplateMainPage() != null && rblock.getTemplateMainPage().getHeight() > height) height = rblock.getTemplateMainPage().getHeight();
    if (rblock.getTemplateSidePages() != null) {
      for (int i = 0; i < rblock.getTemplateSidePages().length; i++) {
        PdfTemplate pdftpl = rblock.getTemplateSidePages()[i];
        if (pdftpl.getHeight() > height) height = pdftpl.getHeight();
      }
    }
    return height;
  }

  /**
   * Este m�todo salva um bloco � uma determinada p�gina. Com a vantagem de abstrair a valida��o de j� existir uma List para a p�gina. Se n�o existir uma nova � criada automaticamente. Al�m de n�o permitir que itens sejam adicionados em duplicidade.
   *
   * @param rblock Bloco a ser salvo no �ndice da p�gina.
   */
  private void addReportBlockToCurrentPageIndex(ReportBlock rblock) {
    ArrayList<ReportBlock> pageList = this.pageBlocksIndexMap.get(this.pageCount);
    if (pageList == null) {
      pageList = new ArrayList<FWListReport.ReportBlock>(30);
      this.pageBlocksIndexMap.put(this.pageCount, pageList);
    }
    if (!pageList.contains(rblock)) pageList.add(rblock);
  }

  /**
   * Este m�todo retorna a lista de blocks que est�o atualmente indexados para uma determinada p�gina. Note que este m�todo pode retornar nulo se nenhum bloco foi indexado para a p�gina ainda.
   *
   * @param page n�mero da p�gina que se deseja a lista de blocos.
   * @return Lista de blocos indexados na p�gina solicitada, ou nulo caso nenhum item tenha sido indexado ainda.
   */
  private ArrayList<ReportBlock> getReportBlockFromPageIndex(int page) {
    return this.pageBlocksIndexMap.get(page);
  }

  /**
   * Este m�todo � usado para for�ar uma quebra de relat�rio. Fazendo com que as vari�veis de controle sejam reiniciadas como se tivessemos come�ando o relat�rio a partir deste ponto.
   *
   * @throws RFWException
   */
  private void breakReport() throws RFWException {
    // O primeiro passo � encerrar a p�gina atual e come�ar uma nova
    breakPage();
    // No entanto ainda temos que reiniciar as vari�veis de relat�rio:
    this.wroteReportHeader = false;
    this.pageCount = 1;
  }

  /**
   * Este m�todo � usado para for�ar uma quebra de p�gina.
   *
   * @throws RFWException
   */
  private void breakPage() throws RFWException {

    // Para saber para qual p�gina vamos (para a pagina lateral ou para a p�gina � baixo, temos que ver a defini��o do bean
    if (getReportBean().getVerticalOrderPage()) {
      createPage(true);
    } else {
      ArrayList<ReportBlock> pageBlocks = getReportBlockFromPageIndex(this.pageCount);

      // Se temos que quebrar na horizontal, primeiro calculamos o m�ximo de p�ginas laterais que esta p�gina tem em seu �ndice (evitando de imprimir p�ginas em branco caso os templates n�o tenham todos p�ginas laterais).
      int maxLateralPages = countMaxSidePages(pageBlocks);
      // Verificamos se temos mais P�gina lateral para avan�ar ou se j� finalizamos o m�ximo de p�ginas laterais
      if (this.pageSideCount < maxLateralPages + 1) {
        createPage(false);
        // Note que no caso de avan�o horizontal ao inv�s do vertical, a l�gica de iniciar a escrita da pr�xima p�gina fica aqui no break page, afinal aqui conseguimos saber quando a p�gina acabou para avan�ar para a lateral. Quando o avan�o � vertical, a l�gica de escrever as p�ginas laterais � controla pelo m�todo de writeReportContent
        for (ReportBlock rblock : pageBlocks) {
          writeBlockOnCurrentPage(rblock);
        }
        // A escrita da p�gina lateral n�o chega a quebrar a p�gina pois isso j� foi calculado durante a escrita da p�gina principal. Assim, os blocks indexados na p�gina s�o exatamente a medida da p�gina. Por isso temos que ao final da itera��o desses blocos quebrar a p�gina novamente.
        breakPage();
      } else {
        createPage(true); // Se n�o temos mais p�ginas para quebrar na horizontal, quebramos na vertical
        this.pageSideCount = 1; // E neste caso voltamos garantimos que voltamos para a p�gina principal para continuar a itera��o padr�o.
      }
    }

  }

  private void createPage(boolean isVertical) {
    // quebramos a p�gina no documento
    getDocument().newPage();

    if (isVertical) {
      this.pageCount++;
    } else {
      this.pageSideCount++;
    }

    // Caso ao criar uma nova p�gina ainda estejamos na p�gina 1 (acontece no caso das p�ginas laterais da p�gina 1 sendo criadas) redefinimos que o cabe�alho de relat�rio n�o foi escrito para que seja escrito novamente nesta p�gina
    if (this.pageCount == 1) this.wroteReportHeader = false;

    // Reinicia valores que quebra de p�gina padr�o, indiferente da dire��o quebrada
    this.wrotePageFooter = false;
    this.wrotePageHeader = false;
    this.wastePageFooterHeight = 0;
    this.blocksOnPageCount = 0;
    this.oddPage = true;
    this.y = getCoordFromTop(0);
  }

  private int countMaxSidePages(List<ReportBlock> pageBlocks) {
    int maxLateralPages = 0; // S� calcula o total de p�ginas lateral, n�o inclui a p�gina principal
    if (pageBlocks != null) {
      for (ReportBlock block : pageBlocks) {
        if (block.getTemplateSidePages() != null && block.getTemplateSidePages().length > maxLateralPages) maxLateralPages = block.getTemplateSidePages().length;
      }
    }
    return maxLateralPages;
  }

  public int getPageCount() {
    return pageCount;
  }

  /**
   * Faz o override apenas para j� deixar o cast certo para esta classe
   */
  @Override
  protected FWListReportOptionBean getReportBean() {
    return (FWListReportOptionBean) super.getReportBean();
  }

  public int getPageSideCount() {
    return pageSideCount;
  }

  /**
   * Este m�todo deve escrever o rodap� de p�gina e retorna-lo como um template.<br>
   * O Rodap� de p�gina normalmente apresenta o n�mero da p�gina e algumas outras informa��es sucintas. Noralmente � um conte�do curto para ocupar pouco espa�o e evitar o consumo desnecess�rio de folhas.
   */
  protected PdfTemplate writePageFooter() throws RFWException {
    return createTemplatePageFooterModel1(getReportBean().getLocale(), getPageCount(), getPageSideCount());
  }

  public int getReportCount() {
    return reportCount;
  }
}
