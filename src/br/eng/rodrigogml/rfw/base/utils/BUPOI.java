package br.eng.rodrigogml.rfw.base.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;

import br.eng.rodrigogml.rfw.kernel.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;

/**
 * Description: Classe utilitária para ajudar a escrever e criar documentos do Office utilizando a ferramenta POI.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (21 de dez de 2019)
 */
public class BUPOI {

  private BUPOI() {
    // Construtor privado para calsse utilitária
  }

  public static void writeCellValue(Cell cell, Object value) throws RFWCriticalException {
    if (value != null) {
      if (value instanceof String) {
        cell.setCellValue((String) value);
      } else if (value instanceof Integer) {
        cell.setCellValue((Integer) value);
      } else if (value instanceof Double) {
        cell.setCellValue((Double) value);
      } else if (value instanceof Float) {
        cell.setCellValue((Float) value);
      } else if (value instanceof BigDecimal) {
        cell.setCellValue(((BigDecimal) value).doubleValue());
      } else if (value instanceof Long) {
        cell.setCellValue((Long) value);
      } else if (value instanceof Date) {
        cell.setCellValue((Date) value);
      } else if (value instanceof LocalDate) {
        cell.setCellValue((LocalDate) value);
      } else if (value instanceof LocalDateTime) {
        cell.setCellValue((LocalDateTime) value);
      } else if (value instanceof Boolean) {
        cell.setCellValue((Boolean) value);
      } else if (value instanceof Calendar) {
        cell.setCellValue((Calendar) value);
      } else if (value instanceof Enum<?>) {
        cell.setCellValue(RFWBundle.get((Enum<?>) value));
      } else {
        throw new RFWCriticalException("Não foi possível escrever um objeto do tipo '" + value.getClass().getCanonicalName() + "' na célula da planilha.");
      }
    }
  }

}
