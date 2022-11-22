package br.eng.rodrigogml.rfw.base.fwreports.bean;

import java.util.Locale;

/**
 * Description: Bean de dados usados para fazer o relat�rio do tipo Listagem.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (06/06/2015)
 */
public class FWListReportOptionBean extends FWReportOptionBean {

  private static final long serialVersionUID = 8119455819729024114L;

  /**
   * Define a orienta��o de gera��o das p�ginas. �til quando o relat�rio apresenta p�ginas laterais.<br>
   * Caso true, define que primeiro ser�o geradas todas as p�ginas de uma mesma coluna para depois gerar a pr�xima coluna de p�ginas laterais. Caso false, orienta que as p�ginas laterais devem ser geradas primeiro. S� depois que todas as p�ginas laterais tiverem sido geradas � que passagemos para a p�gina 2.
   */
  private Boolean verticalOrderPage = true;

  /**
   * Define se o relat�rio deve ou n�o imprimir o background de linhas impares no relat�rio como um todo.<br>
   * Caso esteja definido como true, algum block pode ignorar a impress�o definindo a cor de background como null. Ou mesmo trocar a cor do background para alguma personalizada.
   */
  private boolean printOddBackgrounds = true;

  public FWListReportOptionBean(String enterpriseName, String reportFileName) {
    super(enterpriseName, reportFileName);
  }

  public FWListReportOptionBean(String enterpriseName, Locale locale, String reportFileName) {
    super(enterpriseName, locale, reportFileName);
  }

  /**
   * # define a orienta��o de gera��o das p�ginas. �til quando o relat�rio apresenta p�ginas laterais.<br>
   * Caso true, define que primeiro ser�o geradas todas as p�ginas de uma mesma coluna para depois gerar a pr�xima coluna de p�ginas laterais. Caso false, orienta que as p�ginas laterais devem ser geradas primeiro. S� depois que todas as p�ginas laterais tiverem sido geradas � que passagemos para a p�gina 2.
   *
   * @return the define a orienta��o de gera��o das p�ginas
   */
  public Boolean getVerticalOrderPage() {
    return verticalOrderPage;
  }

  /**
   * # define a orienta��o de gera��o das p�ginas. �til quando o relat�rio apresenta p�ginas laterais.<br>
   * Caso true, define que primeiro ser�o geradas todas as p�ginas de uma mesma coluna para depois gerar a pr�xima coluna de p�ginas laterais. Caso false, orienta que as p�ginas laterais devem ser geradas primeiro. S� depois que todas as p�ginas laterais tiverem sido geradas � que passagemos para a p�gina 2.
   *
   * @param verticalorderpage the new define a orienta��o de gera��o das p�ginas
   */
  public void setVerticalOrderPage(Boolean verticalorderpage) {
    this.verticalOrderPage = verticalorderpage;
  }

  /**
   * # define se o relat�rio deve ou n�o imprimir o background de linhas impares no relat�rio como um todo.<br>
   * Caso esteja definido como true, algum block pode ignorar a impress�o definindo a cor de background como null. Ou mesmo trocar a cor do background para alguma personalizada.
   *
   * @return the define se o relat�rio deve ou n�o imprimir o background de linhas impares no relat�rio como um todo
   */
  public boolean isPrintOddBackgrounds() {
    return printOddBackgrounds;
  }

  /**
   * # define se o relat�rio deve ou n�o imprimir o background de linhas impares no relat�rio como um todo.<br>
   * Caso esteja definido como true, algum block pode ignorar a impress�o definindo a cor de background como null. Ou mesmo trocar a cor do background para alguma personalizada.
   *
   * @param printOddBackgrounds the new define se o relat�rio deve ou n�o imprimir o background de linhas impares no relat�rio como um todo
   */
  public void setPrintOddBackgrounds(boolean printOddBackgrounds) {
    this.printOddBackgrounds = printOddBackgrounds;
  }

}
