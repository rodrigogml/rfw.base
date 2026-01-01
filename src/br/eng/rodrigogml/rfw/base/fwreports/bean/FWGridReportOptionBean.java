package br.eng.rodrigogml.rfw.base.fwreports.bean;

import java.util.Locale;

/**
 * Description: Bean de dados usados para fazer o relatório do tipo Listagem.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (28/08/2015)
 */
public class FWGridReportOptionBean extends FWListReportOptionBean {

  private static final long serialVersionUID = 6026158678472772181L;

  public FWGridReportOptionBean(String enterpriseName, String reportFileName) {
    super(enterpriseName, reportFileName);
  }

  public FWGridReportOptionBean(String enterpriseName, Locale locale, String reportFileName) {
    super(enterpriseName, locale, reportFileName);
  }

}
