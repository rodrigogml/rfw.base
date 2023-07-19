package br.eng.rodrigogml.rfw.base.fwreports.bean;

import java.io.Serializable;
import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.RFW;

/**
 * Description: Bean de configuração geral de relatórios. Deve ser estendido pelos de VOs de configuração de cada relatório.<br>
 *
 * @author Rodrigo Leitão
 * @since 4.2.0 (13/02/2012)
 */

public abstract class FWReportOptionBean implements Serializable {

  private static final long serialVersionUID = 6546390082261081700L;

  /**
   * Definição da orientação do relatório.
   */
  public static enum PAGE_ORIENTATION {
    PORTRAIT, LANDSCAPE
  }

  /**
   * Locale que deve ser utilizado para formatar as informações do relatório
   */
  private Locale locale = null;

  /**
   * Nome do empreendimento, configurável para sair no relatório.
   */
  private String enterpriseName = null;

  /**
   * Orientação da página do relatório.
   */
  private PAGE_ORIENTATION orientation = PAGE_ORIENTATION.PORTRAIT;

  /**
   * Margem esquerda que deve ser obedecida na impressão.
   */
  private float marginLeft = 10;
  /**
   * Margem direita que deve ser obedecida na impressão.
   */
  private float marginRight = 10;
  /**
   * Margem superior que deve ser obedecida na impressão.
   */
  private float marginTop = 10;
  /**
   * Margem inferior que deve ser obedecida na impressão.
   */
  private float marginBottom = 10;

  /**
   * Define o nome que será dado ao arquivo temporário da escrita do relatório. NÃO INCLUIR A EXTENSÃO DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO
   */
  private final String reportFileName;

  /**
   * Inicializa o Bean
   *
   * @param reportFileName Define o nome que será dado ao arquivo temporário da escrita do relatório. NÃO INCLUIR A EXTENSÃO DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO
   */
  public FWReportOptionBean(String enterpriseName, String reportFileName) {
    this(enterpriseName, null, reportFileName);
  }

  /**
   * Inicializa o Bean
   *
   * @param enterpriseName Nome da empresa nos relatórios
   * @param locale Define a localidade a ser utilizada no relatório. Se nenhuma for passada o bean é inicializado com o Locale do sistema do servidor.
   * @param reportFileName Define o nome que será dado ao arquivo temporário da escrita do relatório. NÃO INCLUIR A EXTENSÃO DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO
   */
  public FWReportOptionBean(String enterpriseName, Locale locale, String reportFileName) {
    this.enterpriseName = enterpriseName;
    this.reportFileName = reportFileName;
    if (locale != null) {
      this.locale = locale;
    } else {
      this.locale = RFW.getLocale();
    }
  }

  /**
   * # margem superior que deve ser obedecida na impressão.
   *
   * @param marginTop the new margem superior que deve ser obedecida na impressão
   */
  public void setMarginTop(float marginTop) {
    this.marginTop = marginTop;
  }

  /**
   * # margem direita que deve ser obedecida na impressão.
   *
   * @param marginRight the new margem direita que deve ser obedecida na impressão
   */
  public void setMarginRight(float marginRight) {
    this.marginRight = marginRight;
  }

  /**
   * # margem esquerda que deve ser obedecida na impressão.
   *
   * @param marginLeft the new margem esquerda que deve ser obedecida na impressão
   */
  public void setMarginLeft(float marginLeft) {
    this.marginLeft = marginLeft;
  }

  /**
   * # margem inferior que deve ser obedecida na impressão.
   *
   * @param marginBottom the new margem inferior que deve ser obedecida na impressão
   */
  public void setMarginBottom(float marginBottom) {
    this.marginBottom = marginBottom;
  }

  /**
   * # locale que deve ser utilizado para formatar as informações do relatório.
   *
   * @param locale the new locale que deve ser utilizado para formatar as informações do relatório
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * # margem superior que deve ser obedecida na impressão.
   *
   * @return the margem superior que deve ser obedecida na impressão
   */
  public float getMarginTop() {
    return marginTop;
  }

  /**
   * # margem direita que deve ser obedecida na impressão.
   *
   * @return the margem direita que deve ser obedecida na impressão
   */
  public float getMarginRight() {
    return marginRight;
  }

  /**
   * # margem esquerda que deve ser obedecida na impressão.
   *
   * @return the margem esquerda que deve ser obedecida na impressão
   */
  public float getMarginLeft() {
    return marginLeft;
  }

  /**
   * # margem inferior que deve ser obedecida na impressão.
   *
   * @return the margem inferior que deve ser obedecida na impressão
   */
  public float getMarginBottom() {
    return marginBottom;
  }

  /**
   * # locale que deve ser utilizado para formatar as informações do relatório.
   *
   * @return the locale que deve ser utilizado para formatar as informações do relatório
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * # orientação da página do relatório.
   *
   * @return the orientação da página do relatório
   */
  public PAGE_ORIENTATION getOrientation() {
    return orientation;
  }

  /**
   * # orientação da página do relatório.
   *
   * @param orientation the new orientação da página do relatório
   */
  public void setOrientation(PAGE_ORIENTATION orientation) {
    this.orientation = orientation;
  }

  /**
   * # nome do empreendimento, configurável para sair no relatório.
   *
   * @return the nome do empreendimento, configurável para sair no relatório
   */
  public String getEnterpriseName() {
    return enterpriseName;
  }

  /**
   * # nome do empreendimento, configurável para sair no relatório.
   *
   * @param enterpriseName the new nome do empreendimento, configurável para sair no relatório
   */
  public void setEnterpriseName(String enterpriseName) {
    this.enterpriseName = enterpriseName;
  }

  /**
   * # define o nome que será dado ao arquivo temporário da escrita do relatório. NÃO INCLUIR A EXTENSÃO DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO.
   *
   * @return the define o nome que será dado ao arquivo temporário da escrita do relatório
   */
  public String getReportFileName() {
    return reportFileName;
  }

}
