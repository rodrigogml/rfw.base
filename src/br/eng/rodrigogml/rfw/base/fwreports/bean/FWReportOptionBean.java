package br.eng.rodrigogml.rfw.base.fwreports.bean;

import java.io.Serializable;
import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.RFW;

/**
 * Description: Bean de configura��o geral de relat�rios. Deve ser estendido pelos de VOs de configura��o de cada relat�rio.<br>
 *
 * @author Rodrigo Leit�o
 * @since 4.2.0 (13/02/2012)
 */

public abstract class FWReportOptionBean implements Serializable {

  private static final long serialVersionUID = 6546390082261081700L;

  /**
   * Defini��o da orienta��o do relat�rio.
   */
  public static enum PAGE_ORIENTATION {
    PORTRAIT, LANDSCAPE
  }

  /**
   * Locale que deve ser utilizado para formatar as informa��es do relat�rio
   */
  private Locale locale = null;

  /**
   * Nome do empreendimento, configur�vel para sair no relat�rio.
   */
  private String enterpriseName = null;

  /**
   * Orienta��o da p�gina do relat�rio.
   */
  private PAGE_ORIENTATION orientation = PAGE_ORIENTATION.PORTRAIT;

  /**
   * Margem esquerda que deve ser obedecida na impress�o.
   */
  private float marginLeft = 10;
  /**
   * Margem direita que deve ser obedecida na impress�o.
   */
  private float marginRight = 10;
  /**
   * Margem superior que deve ser obedecida na impress�o.
   */
  private float marginTop = 10;
  /**
   * Margem inferior que deve ser obedecida na impress�o.
   */
  private float marginBottom = 10;

  /**
   * Define o nome que ser� dado ao arquivo tempor�rio da escrita do relat�rio. N�O INCLUIR A EXTENS�O DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO
   */
  private final String reportFileName;

  /**
   * Inicializa o Bean
   *
   * @param reportFileName Define o nome que ser� dado ao arquivo tempor�rio da escrita do relat�rio. N�O INCLUIR A EXTENS�O DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO
   */
  public FWReportOptionBean(String enterpriseName, String reportFileName) {
    this(enterpriseName, null, reportFileName);
  }

  /**
   * Inicializa o Bean
   *
   * @param enterpriseName Nome da empresa nos relat�rios
   * @param locale Define a localidade a ser utilizada no relat�rio. Se nenhuma for passada o bean � inicializado com o Locale do sistema do servidor.
   * @param reportFileName Define o nome que ser� dado ao arquivo tempor�rio da escrita do relat�rio. N�O INCLUIR A EXTENS�O DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO
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
   * # margem superior que deve ser obedecida na impress�o.
   *
   * @param marginTop the new margem superior que deve ser obedecida na impress�o
   */
  public void setMarginTop(float marginTop) {
    this.marginTop = marginTop;
  }

  /**
   * # margem direita que deve ser obedecida na impress�o.
   *
   * @param marginRight the new margem direita que deve ser obedecida na impress�o
   */
  public void setMarginRight(float marginRight) {
    this.marginRight = marginRight;
  }

  /**
   * # margem esquerda que deve ser obedecida na impress�o.
   *
   * @param marginLeft the new margem esquerda que deve ser obedecida na impress�o
   */
  public void setMarginLeft(float marginLeft) {
    this.marginLeft = marginLeft;
  }

  /**
   * # margem inferior que deve ser obedecida na impress�o.
   *
   * @param marginBottom the new margem inferior que deve ser obedecida na impress�o
   */
  public void setMarginBottom(float marginBottom) {
    this.marginBottom = marginBottom;
  }

  /**
   * # locale que deve ser utilizado para formatar as informa��es do relat�rio.
   *
   * @param locale the new locale que deve ser utilizado para formatar as informa��es do relat�rio
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * # margem superior que deve ser obedecida na impress�o.
   *
   * @return the margem superior que deve ser obedecida na impress�o
   */
  public float getMarginTop() {
    return marginTop;
  }

  /**
   * # margem direita que deve ser obedecida na impress�o.
   *
   * @return the margem direita que deve ser obedecida na impress�o
   */
  public float getMarginRight() {
    return marginRight;
  }

  /**
   * # margem esquerda que deve ser obedecida na impress�o.
   *
   * @return the margem esquerda que deve ser obedecida na impress�o
   */
  public float getMarginLeft() {
    return marginLeft;
  }

  /**
   * # margem inferior que deve ser obedecida na impress�o.
   *
   * @return the margem inferior que deve ser obedecida na impress�o
   */
  public float getMarginBottom() {
    return marginBottom;
  }

  /**
   * # locale que deve ser utilizado para formatar as informa��es do relat�rio.
   *
   * @return the locale que deve ser utilizado para formatar as informa��es do relat�rio
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * # orienta��o da p�gina do relat�rio.
   *
   * @return the orienta��o da p�gina do relat�rio
   */
  public PAGE_ORIENTATION getOrientation() {
    return orientation;
  }

  /**
   * # orienta��o da p�gina do relat�rio.
   *
   * @param orientation the new orienta��o da p�gina do relat�rio
   */
  public void setOrientation(PAGE_ORIENTATION orientation) {
    this.orientation = orientation;
  }

  /**
   * # nome do empreendimento, configur�vel para sair no relat�rio.
   *
   * @return the nome do empreendimento, configur�vel para sair no relat�rio
   */
  public String getEnterpriseName() {
    return enterpriseName;
  }

  /**
   * # nome do empreendimento, configur�vel para sair no relat�rio.
   *
   * @param enterpriseName the new nome do empreendimento, configur�vel para sair no relat�rio
   */
  public void setEnterpriseName(String enterpriseName) {
    this.enterpriseName = enterpriseName;
  }

  /**
   * # define o nome que ser� dado ao arquivo tempor�rio da escrita do relat�rio. N�O INCLUIR A EXTENS�O DO ARQUIVO, APENAS O NOME PRINCIPAL DO ARQUIVO.
   *
   * @return the define o nome que ser� dado ao arquivo tempor�rio da escrita do relat�rio
   */
  public String getReportFileName() {
    return reportFileName;
  }

}
