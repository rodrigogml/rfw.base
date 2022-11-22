package br.eng.rodrigogml.rfw.base.dataformatters;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWRunTimeException;

/**
 * Description: Classe que formata dados de Data e Horário. não valida pois não é utilizado em campos, só para formatação em relatórios e campos de exibição.<br>
 * Esta classe aceita tanto o LocaleDate quanto o LocalDateTime. Embora o 'output' de formatação dependa do tipo da instancia obtido.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (13/12/2014)
 */
public class RFWDateTimeDataFormatter implements RFWDataFormatter<String, Object> {

  /**
   * Define o modelo de formatação.<br>
   * Utilizei como int, para não criar uma enum que seria utilizada só dentro desta classe.<br>
   * 0 = Data e Hora; 1 = Data
   */
  private int mode = 0;

  private RFWDateTimeDataFormatter(int mode) {
    this.mode = mode;
  }

  /**
   * Recupera uma instancia que formata o valor no formato de Data e Hora.
   */
  public static RFWDateTimeDataFormatter createDateTimeInstance() {
    return new RFWDateTimeDataFormatter(0);
  }

  /**
   * Recupera uma instancia que formata o valor no formato de Data.
   */
  public static RFWDateTimeDataFormatter createDateInstance() {
    return new RFWDateTimeDataFormatter(1);
  }

  @Override
  public String toPresentation(Object value, Locale locale) {
    String result = "";
    if (value != null) {
      // Sempre convertemos para o LocalDateTime para simplificar e trabalhar com uma classe só
      if (value instanceof LocalDate) {
        value = ((LocalDate) value).atStartOfDay();
      } else if (value instanceof Timestamp) {
        value = ((Timestamp) value).toLocalDateTime();
      }

      if (value instanceof LocalDateTime) {
        if (this.mode == 0) { // Data e Hora
          result = ((LocalDateTime) value).format(RFW.getDateTimeFormattter());
        } else if (this.mode == 1) { // Data
          result = ((LocalDateTime) value).format(RFW.getDateFormattter());
        }
      } else {
        throw new RFWRunTimeException("O RFWDataTimeDataFormatter não sabe trabalhar com a classe '" + value.getClass().getCanonicalName() + "'.");
      }
    }
    return result;
  }

  @Override
  public Object toVO(String formattedvalue, Locale locale) {
    throw new RFWRunTimeException("O RFWDateTimeDataFormatter não foi implementado para realizar o parser de Datas, apenas para formatar para relatórios e campos de exibição (como Grids, etc.)");
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    throw new RFWRunTimeException("O RFWDateTimeDataFormatter não foi implementado para realizar o parser de Datas, apenas para formatar para relatórios e campos de exibição (como Grids, etc.)");
  }

  @Override
  public int getMaxlenght() {
    return 19;
  }
}
