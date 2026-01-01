package br.eng.rodrigogml.rfw.base.fwreports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;

import br.eng.rodrigogml.rfw.base.fwreports.FWListReport.ReportBlock.ALIGNMENT;
import br.eng.rodrigogml.rfw.base.fwreports.bean.FWListReportOptionBean;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe de execução de relatório de listagem.<BR>
 * <br>
 * O {@link FWListReport} extende a classe principal de relatórios {@link FWReport} e cria um template mais simples para relatórios do tipo "listagem". Bastando que o relatório de listagem implemente os métodos definidos aqui como abstratos.<br>
 * O funcionamento "básico" consiste em receber da classe filha uma lista de {@link ReportBlock} que representam os "blocos" a serem colocados na página um a baixo do outro. Normalmente cada bloco representa uma linha do relatório, mas podem também conter gráficos, imagens, e qualquer informação desejada. Quando não couberem mais blocos na mesma linha o relatório será automaticamente quebrado em
 * uma nova página.<br>
 * Há a possibilidade também de criar blocos para páginas "laterais". Para relatórios com muitas colunas é possível expandi-lo para páginas que serão colocadas "à direita" de cada página do relatório. Essas páginas, quando existentes receberão letras para indicar a numeração de páginas adicionais. Formando uma espécie de grid, similar à uma planilha de cálculo.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (06/06/2015)
 */
public abstract class FWListReport extends FWReport {

  /**
   * Classe que representa um "bloco" do relatório. Um bloco normalmente é uma linha, um gráfio, etc. Uma coleção de blocos compõe um relatório.
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
     * Define o template a ser desenhado no página "principal" do relatório.<br>
     */
    private PdfTemplate templateMainPage = null;

    /**
     * Define os templates que serão colocados nas páginas à direita da página principal. Cada item deste array será colocado em um nova página "à direita" da página principal do relatório.
     */
    private PdfTemplate[] templateSidePages = null;

    /**
     * Define uma margem superior para ser adicionada ao bloco. Usada para separar o bloco do bloco superior.<br>
     * <b>Esta margem é descartada caso seja o primeiro bloco da página.</b>
     */
    private float margintop = 0f;

    /**
     * Este atributo define a cor a ser usada para imprimir o background de linha impar. Para que o background apareça é obrigatório que a funcionalidade esteja ligada no ReportBean.<br>
     * Para garantir que algum bloco específico não terá uma cor de fundo defina este atributo como null. Ao definir como null o relatório reiniciará a contagem das linhas, para que no próximo bloco as linhas reiniciem a contagem de linha impar novamente. Isso evita que a cada bloco (com linha de fundo) depois de blocos sem linhas de fundo comecem com cores diferentes.<br>
     * O valor da cor deve ser definido como padrão WEB, sempre contendo o "#" para deixar evidente e incluir os 6 dígitos.
     */
    private String oddLineBackgroundColor = "#F5F5F5";

    /**
     * Define a cor de fundo de uma linha do relatório. Essa propriedade se sobrepõe a cor definida em oddLineBackgroundColor.<br>
     * O valor da cor deve ser definido como padrão WEB, sempre contendo o "#" para deixar evidente e incluir os 6 dígitos.
     */
    private String lineBackgroundColor = null;

    /**
     * Caso true, a página é quebrada antes de escrever este bloco (não será quebrada se for o primei block já de uma nova página)
     */
    private Boolean newPage = Boolean.FALSE;

    /**
     * Se True, nenhum cabeçalho será escrito na página.<br>
     * <b>ATENÇÃO:</b> Para funcionar essa opção deve estar habilitada no primeiro bloco da página.
     */
    private Boolean forbidHeaders = Boolean.FALSE;

    /**
     * Se True, nenhum rodapé será escrito na página.<br>
     * <b>ATENÇÃO:</b> Para funcionar essa opção deve estar habilitada no primeiro bloco da página.
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

  // Variáveis de Controle do Relatório
  private boolean wroteReportHeader = false; // Indica se o cabeçalho do relatório já foi escrito.
  private boolean wrotePageHeader = false; // Indica se o cabeçalho do página já foi escrito.
  private boolean wrotePageFooter = false; // Indica se o rodapé do página já foi escrito.
  private int blocksOnPageCount = 0; // Contador de blocos na página. Em outras palavras: 0 indica que ainda não temos nenhum block escrito na página, >0, indica que já temos a quantidade indicada escrita na página.
  private boolean oddPage = true; // Flag que indica se estamos em uma linha impar
  private int pageCount = 1; // Indica em qual página o relatório está atualmente.
  private int pageSideCount = 1; // Indica em qual página lateral está atualmente. 1 é a página A, 2 a página B, etc.
  private float y = 0; // Variável que guarda o ponto vertical em que podemos continuar a escrever/desenhar
  private float wastePageFooterHeight = 0; // Armazena a altura perdida com o Rodapé de Página na página atual.
  private int reportCount = 0; // Conta o índex do relatório atual. Sendo 0 o primeiro relatório, 1 o segundo relatório, etc.. Consireramos 1 realatório cada lista de ReportBlocks retornados através do método getReportBlocksList(); Pode se considerar que é um contador de quantas vezes o método já foi chamado, embora o valor vá permanecer no "zero" (no mesmo valor) até que o método seja chamado
  // novamente.

  /**
   * Esta hash é usada para que, durante a iteração e criação da página principal, seja salvo quais blocos pertencem a cada página.<br>
   * Depois que a página principal é criada, essas informações serão utilizadas para gerar as páginas laterias com os mesmos blocks.<br>
   * A chave da hash é o número da página (valor de {@link #pageCount} no momento da criação. O valor é uma lista com ReportBlocks ordenados conforme recebido da classe filha.
   */
  private final HashMap<Integer, ArrayList<ReportBlock>> pageBlocksIndexMap = new HashMap<Integer, ArrayList<ReportBlock>>();

  public FWListReport(FWListReportOptionBean reportbean) throws RFWException {
    super(reportbean);
  }

  /**
   * Este método é chamado depois que o Documento já foi aberto, mas antes de iterar os relatórios.<br>
   * Tem a finalidade de permitir que a classe filha possa preparar os relatórios antes da iteração.
   *
   * @throws RFWException
   */
  protected abstract void prepareReportData() throws RFWException;

  /**
   * Este método será chamado diversas vezes para obter a lista de blocos de cada relatório.<br>
   * A classe filha deve retornar uma lista de PdfTemplate, sendo que cada Template representa o próximo bloco a ser escrito na listagem. O bloco normalmente é uma linha do relatório, embora possa ser até uma linha dupla ou algum conteúdo mais específico. Sempre que o bloco couber na mesma página ele será imprsso na mesma página. Quando o bloco não couber na mesma página ele automaticamente será
   * jogado na próxima folha.<br>
   * Este método será chamado recursivamente até que retorne null. A cada bloco retornado o sistema considerará como um "relatório diferente". Fará parte do mesmo PDF gerado, mas reiniciará a contagem de páginas, redesenhará o cabeçalho de relatório e assim por diante. O resultado é similar à diversos relatórios concatenados em um único arquivo.
   *
   * @return
   * @throws RFWException
   */
  protected abstract List<ReportBlock> getReportBlocksList() throws RFWException;

  /**
   * Este método deve escrever o cabeçalho de relatório e retorna-lo como um template.<br>
   * O Cabeçalho de relatório só aparece no começo de um novo relatório. Não será repetido a cada nova página. Escreva neste cabeçalho apenas as informações de identificação do relatório.
   */
  protected abstract PdfTemplate writeReportHeader() throws RFWException;

  /**
   * Este método deve escrever o cabeçalho de página e retorna-lo como um template.<br>
   * O Cabeçalho de página normalmente repete os nomes das colunas da listagem para que o leitor saiba o que significam os dados. Noralmente é um conteúdo curto para ocupar pouco espaço e evitar o consumo desnecessário de folhas.
   */
  protected abstract PdfTemplate writePageHeader() throws RFWException;

  /**
   * Este método é chamado quando a {@link FWListReport} termina de imprimir o relatório.<br>
   * Sua implementação pode ser "vazia", não há necessidade dela para a {@link FWListReport}.<br>
   * Sua existência tem a finalidade de proporcionar às classes filhas que possam fazer últimos ajustes do relatório. Como por exemplo, a impressão do número de páginas em cada página passada no formato "Página X de Y" só pode ser feita no final, que é quando saberemos o valor de Y.
   */
  protected abstract void onReportFinished() throws RFWException;

  @Override
  protected void writeReportContent() throws RFWException {
    prepareReportData();

    List<ReportBlock> blocklist = getReportBlocksList();

    while (blocklist != null) {
      // Inicializa as variáveis de controle neste ponto, antes de começar a iteração. Isso porque até chegar neste ponto a classe filha pode ter alterado algumas configurações do relatório que podem redefinir os valores dessas variáveis. Como por exemplo, tamanho do papel ou margens.
      y = getCoordFromTop(0);

      for (ReportBlock rblock : blocklist) {
        writeBlockOnCurrentPage(rblock);
      }

      // Depois de iterarmos todo o conteúdo do relatório, verificamos se temos páginas lateria para serem feitas E SE a orientação é vertical. Isso porque quando a orientação é horizontal o tratamento de impressão das páginas laterais é feito a cada quebra de página.
      if (getReportBean().getVerticalOrderPage()) {
        int maxPages = this.pageCount; // Salva o número da última página para orientar a iteração
        int maxSidePages = countMaxSidePages(blocklist);

        // iteramos a quantidade de páginas laterais que serão criadas
        while (this.pageSideCount <= maxSidePages) {
          this.pageCount = 1; // Reiniciamos a contagem da primeira página.
          createPage(false); // Cria nova página lateral - o que já incrementa pageSideCount
          boolean usedpage = false; // Flag usada para definir se a página foi usada ou não. Se ela não fui usada (caso nenhum bloco indexado na pagina atual tenha um template para esta página ela não é usada, podendo ser aproveitada para a próxima iteração sem gerar uma página em branco).

          // Para cada coluna de pagina lateral, vamos iterar página por página criada
          while (this.pageCount <= maxPages) {
            ArrayList<ReportBlock> pageBlocks = getReportBlockFromPageIndex(this.pageCount);
            // Iteramos cada um dos blocos indexados na página atual
            for (ReportBlock rblock : pageBlocks) {
              if (rblock.getTemplateSidePages() != null && rblock.getTemplateSidePages().length >= this.pageSideCount - 1 && rblock.getTemplateSidePages()[this.pageSideCount - 2] != null) {
                writeBlockOnCurrentPage(rblock);
                usedpage = true;
              }
            }
            // Quando acaba a iteração dos itens da uma página, temos que quebrar a página, isso porque os blocks já estão na medida e não quebram a página dentro do método writeBlockOnCurrentPage.
            if (usedpage) {
              breakPage();
            } else {
              // se não utilizou a página temos que pelo menos avançar o númro da página para não ter erro de que página estamos agora
              this.pageCount++;
            }
          }
        }
      } else { // Se a ordem é Horizontal, na última página temos que escrever as páginas laterias da última página
        ArrayList<ReportBlock> pageBlocks = getReportBlockFromPageIndex(this.pageCount);

        // Se temos que quebrar na horizontal, primeiro calculamos o máximo de páginas laterais que esta página tem em seu índice (evitando de imprimir páginas em branco caso os templates não tenham todos páginas laterais).
        int maxLateralPages = countMaxSidePages(pageBlocks);
        // Verificamos se temos mais Página lateral para avançar ou se já finalizamos o máximo de páginas laterais
        while (this.pageSideCount < maxLateralPages + 1) {
          createPage(false);
          // Note que no caso de avanço horizontal ao invés do vertical, a lógica de iniciar a escrita da próxima página fica aqui no break page, afinal aqui conseguimos saber quando a página acabou para avançar para a lateral. Quando o avanço é vertical, a lógica de escrever as páginas laterais é controla pelo método de writeReportContent
          for (ReportBlock rblock : pageBlocks) {
            writeBlockOnCurrentPage(rblock);
          }
        }

      }

      // Quando acabamos de fazer o relatório, avisamos a classe filha
      onReportFinished();

      // Agora que acabou o relatório recebido, verificamos se teremos um novo para fazer:
      blocklist = getReportBlocksList();
      if (blocklist != null) {
        // Se temos um novo relatório fazemos a quebra de relatório
        breakReport();
      }
      reportCount++; // Incrementa o contador de relatórios prontos antes de começar o próximo.
    }
  }

  /**
   * Este método separa a lógica de escrever um determinado bloco em uma página.<br>
   * Este método também é responsável por "analisar" a página e verificar a necessidade de escrever cabeçalhos, rodapés, espaço restante etc.<br>
   * <b>Note que caso o bloco a ser escrito não caiba na página atual, este método forçará a quebra da página!!!</b>
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

    // Se não é o primeiro bloco adicioamos a "margem superior" do bloco em "y" para que o cálculo do espaço já considere o lugar correto de colocar o template.
    if (blocksOnPageCount > 0) y -= rblock.getMargintop();

    // buscamos e trabalhamos com o tamanho do maior template de qualquer página (entre principal e laterais) para que ao iterar cada página (laterais e principal) todas fiquem sincronizadas caso os templates tenham tamanhos diferentes.
    float maxBlockHights = getMaxBlockHights(rblock);

    // Verificamos se NÃO Temos espaço na folha para escrever o próximo bloco. Lembrando que em toda página o rodapé de página é obrigatório e deve ser descontado do espaço "livre" restante.
    // Também quebra a página caso o bloco exiga uma nova página e já tivermos algum bloco escrito na página atual.
    if (y - maxBlockHights - this.wastePageFooterHeight < getCoordFromBottom(0) || (rblock.getNewPage() && this.blocksOnPageCount > 0)) {
      // NOTA: na primeira iteração da página ou do relatório o valor de espaço restante não é 100% preciso já que ainda não escrevemos os cabeçalhos. No entanto, só teremos um erro se alguém fizer um relatório absurdo em que em uma folha não caiba ambos os cabeçalhos, o rodapé e 1 bloco.
      // Se não temos espaço temos que quebrar a folha automaticamente
      breakPage();
    }

    // Os cabeçalhos e Rodapés só serão escritos se: 1-Ainda não tiverem sido escritos; 2-Se ainda não tiver nenhum bloco escrito na página; 3-Se o primeiro bloco da página não tiver as opções de forbidHeaders ou forbidFooters desligadas
    // Antes de escrever a nova linha, verificamos se já escrevemos o cabeçalho de relatório
    if (!wroteReportHeader && this.blocksOnPageCount == 0 && !rblock.getForbidHeaders()) {
      final PdfTemplate t = writeReportHeader();
      if (t != null) {
        y -= t.getHeight();
        cb.addTemplate(t, getCoordFromLeft(0), y - 5);
      }
      this.wroteReportHeader = true;
    }
    // Em seguida verificamos se precisamos escrever o cabeçalho de página
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
        // O rodapé nós escrevemos mas não movimentamos "y", já que ele é escrito no final da página e não precisa "empurrar o conteúdo" mais para baixo. Apenas salvamos o seu tamanho para calcular quando a folha acaba
        this.wastePageFooterHeight = t.getHeight();
        cb.addTemplate(t, getCoordFromLeft(0), getCoordFromBottom(0));
      }
      this.wrotePageFooter = true;
    }
    // Por fim escreve o bloco agora que temos "y" definido de acordo com a posição livre do campo
    y -= +maxBlockHights; // primeiro decrementa a posição de y porque o template tem sua origem no canto bottomleft
    // Calcula a posição X para colocar o bloco de acordo com a definição de alinhamento
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
        // Verificamos se vamos escrever um background, obviamente antes de escrever o template, e só escrito em linhas pares
        if (getReportBean().isPrintOddBackgrounds() && !oddPage) {
          cb.saveState();
          cb.rectangle(x, y - 5, template.getWidth(), template.getHeight());
          cb.setColorFill(createColor(rblock.getOddLineBackgroundColor()));
          cb.fill();
          cb.restoreState();
        }
      } else {
        // Se a cor de fundo está nula, significa que este bloco não "conta" como fundo de linha, neste caso reiniciamos a flag para que o próximo bloco que possa ter o fundo pintado seja considerado como bloco ímpar.
        oddPage = false; // Reiniciamos como par, pois logo abaixo o valor será invertido
      }
    }

    cb.addTemplate(template, x, y - 5);
    addReportBlockToCurrentPageIndex(rblock); // Se escrevemos o bloco na página já o indexamos a esta página
    blocksOnPageCount++;
    oddPage = !oddPage;

  }

  private BaseColor createColor(String colorStr) {
    return new BaseColor(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
  }

  /**
   * Método que busca entre todos os templates do block (templates da página principal e laterais) o maior tamanho usado.
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
   * Este método salva um bloco à uma determinada página. Com a vantagem de abstrair a validação de já existir uma List para a página. Se não existir uma nova é criada automaticamente. Além de não permitir que itens sejam adicionados em duplicidade.
   *
   * @param rblock Bloco a ser salvo no índice da página.
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
   * Este método retorna a lista de blocks que estão atualmente indexados para uma determinada página. Note que este método pode retornar nulo se nenhum bloco foi indexado para a página ainda.
   *
   * @param page número da página que se deseja a lista de blocos.
   * @return Lista de blocos indexados na página solicitada, ou nulo caso nenhum item tenha sido indexado ainda.
   */
  private ArrayList<ReportBlock> getReportBlockFromPageIndex(int page) {
    return this.pageBlocksIndexMap.get(page);
  }

  /**
   * Este método é usado para forçar uma quebra de relatório. Fazendo com que as variáveis de controle sejam reiniciadas como se tivessemos começando o relatório a partir deste ponto.
   *
   * @throws RFWException
   */
  private void breakReport() throws RFWException {
    // O primeiro passo é encerrar a página atual e começar uma nova
    breakPage();
    // No entanto ainda temos que reiniciar as variáveis de relatório:
    this.wroteReportHeader = false;
    this.pageCount = 1;
  }

  /**
   * Este método é usado para forçar uma quebra de página.
   *
   * @throws RFWException
   */
  private void breakPage() throws RFWException {

    // Para saber para qual página vamos (para a pagina lateral ou para a página à baixo, temos que ver a definição do bean
    if (getReportBean().getVerticalOrderPage()) {
      createPage(true);
    } else {
      ArrayList<ReportBlock> pageBlocks = getReportBlockFromPageIndex(this.pageCount);

      // Se temos que quebrar na horizontal, primeiro calculamos o máximo de páginas laterais que esta página tem em seu índice (evitando de imprimir páginas em branco caso os templates não tenham todos páginas laterais).
      int maxLateralPages = countMaxSidePages(pageBlocks);
      // Verificamos se temos mais Página lateral para avançar ou se já finalizamos o máximo de páginas laterais
      if (this.pageSideCount < maxLateralPages + 1) {
        createPage(false);
        // Note que no caso de avanço horizontal ao invés do vertical, a lógica de iniciar a escrita da próxima página fica aqui no break page, afinal aqui conseguimos saber quando a página acabou para avançar para a lateral. Quando o avanço é vertical, a lógica de escrever as páginas laterais é controla pelo método de writeReportContent
        for (ReportBlock rblock : pageBlocks) {
          writeBlockOnCurrentPage(rblock);
        }
        // A escrita da página lateral não chega a quebrar a página pois isso já foi calculado durante a escrita da página principal. Assim, os blocks indexados na página são exatamente a medida da página. Por isso temos que ao final da iteração desses blocos quebrar a página novamente.
        breakPage();
      } else {
        createPage(true); // Se não temos mais páginas para quebrar na horizontal, quebramos na vertical
        this.pageSideCount = 1; // E neste caso voltamos garantimos que voltamos para a página principal para continuar a iteração padrão.
      }
    }

  }

  private void createPage(boolean isVertical) {
    // quebramos a página no documento
    getDocument().newPage();

    if (isVertical) {
      this.pageCount++;
    } else {
      this.pageSideCount++;
    }

    // Caso ao criar uma nova página ainda estejamos na página 1 (acontece no caso das páginas laterais da página 1 sendo criadas) redefinimos que o cabeçalho de relatório não foi escrito para que seja escrito novamente nesta página
    if (this.pageCount == 1) this.wroteReportHeader = false;

    // Reinicia valores que quebra de página padrão, indiferente da direção quebrada
    this.wrotePageFooter = false;
    this.wrotePageHeader = false;
    this.wastePageFooterHeight = 0;
    this.blocksOnPageCount = 0;
    this.oddPage = true;
    this.y = getCoordFromTop(0);
  }

  private int countMaxSidePages(List<ReportBlock> pageBlocks) {
    int maxLateralPages = 0; // Só calcula o total de páginas lateral, não inclui a página principal
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
   * Faz o override apenas para já deixar o cast certo para esta classe
   */
  @Override
  protected FWListReportOptionBean getReportBean() {
    return (FWListReportOptionBean) super.getReportBean();
  }

  public int getPageSideCount() {
    return pageSideCount;
  }

  /**
   * Este método deve escrever o rodapé de página e retorna-lo como um template.<br>
   * O Rodapé de página normalmente apresenta o número da página e algumas outras informações sucintas. Noralmente é um conteúdo curto para ocupar pouco espaço e evitar o consumo desnecessário de folhas.
   */
  protected PdfTemplate writePageFooter() throws RFWException {
    return createTemplatePageFooterModel1(getReportBean().getLocale(), getPageCount(), getPageSideCount());
  }

  public int getReportCount() {
    return reportCount;
  }
}
