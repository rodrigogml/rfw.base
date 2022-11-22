package br.eng.rodrigogml.rfw.base.fwreports.bean;

import java.util.Locale;

/**
 * Description: Bean de dados usados para fazer o relatório do tipo Listagem.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (06/06/2015)
 */
public class FWListReportOptionBean extends FWReportOptionBean {

  private static final long serialVersionUID = 8119455819729024114L;

  /**
   * Define a orientação de geração das páginas. Útil quando o relatório apresenta páginas laterais.<br>
   * Caso true, define que primeiro serão geradas todas as páginas de uma mesma coluna para depois gerar a próxima coluna de páginas laterais. Caso false, orienta que as páginas laterais devem ser geradas primeiro. Só depois que todas as páginas laterais tiverem sido geradas é que passagemos para a página 2.
   */
  private Boolean verticalOrderPage = true;

  /**
   * Define se o relatório deve ou não imprimir o background de linhas impares no relatório como um todo.<br>
   * Caso esteja definido como true, algum block pode ignorar a impressão definindo a cor de background como null. Ou mesmo trocar a cor do background para alguma personalizada.
   */
  private boolean printOddBackgrounds = true;

  public FWListReportOptionBean(String enterpriseName, String reportFileName) {
    super(enterpriseName, reportFileName);
  }

  public FWListReportOptionBean(String enterpriseName, Locale locale, String reportFileName) {
    super(enterpriseName, locale, reportFileName);
  }

  /**
   * # define a orientação de geração das páginas. Útil quando o relatório apresenta páginas laterais.<br>
   * Caso true, define que primeiro serão geradas todas as páginas de uma mesma coluna para depois gerar a próxima coluna de páginas laterais. Caso false, orienta que as páginas laterais devem ser geradas primeiro. Só depois que todas as páginas laterais tiverem sido geradas é que passagemos para a página 2.
   *
   * @return the define a orientação de geração das páginas
   */
  public Boolean getVerticalOrderPage() {
    return verticalOrderPage;
  }

  /**
   * # define a orientação de geração das páginas. Útil quando o relatório apresenta páginas laterais.<br>
   * Caso true, define que primeiro serão geradas todas as páginas de uma mesma coluna para depois gerar a próxima coluna de páginas laterais. Caso false, orienta que as páginas laterais devem ser geradas primeiro. Só depois que todas as páginas laterais tiverem sido geradas é que passagemos para a página 2.
   *
   * @param verticalorderpage the new define a orientação de geração das páginas
   */
  public void setVerticalOrderPage(Boolean verticalorderpage) {
    this.verticalOrderPage = verticalorderpage;
  }

  /**
   * # define se o relatório deve ou não imprimir o background de linhas impares no relatório como um todo.<br>
   * Caso esteja definido como true, algum block pode ignorar a impressão definindo a cor de background como null. Ou mesmo trocar a cor do background para alguma personalizada.
   *
   * @return the define se o relatório deve ou não imprimir o background de linhas impares no relatório como um todo
   */
  public boolean isPrintOddBackgrounds() {
    return printOddBackgrounds;
  }

  /**
   * # define se o relatório deve ou não imprimir o background de linhas impares no relatório como um todo.<br>
   * Caso esteja definido como true, algum block pode ignorar a impressão definindo a cor de background como null. Ou mesmo trocar a cor do background para alguma personalizada.
   *
   * @param printOddBackgrounds the new define se o relatório deve ou não imprimir o background de linhas impares no relatório como um todo
   */
  public void setPrintOddBackgrounds(boolean printOddBackgrounds) {
    this.printOddBackgrounds = printOddBackgrounds;
  }

}
