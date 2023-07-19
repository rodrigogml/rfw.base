package br.eng.rodrigogml.rfw.base.measureruler.daoconverter;

import br.eng.rodrigogml.rfw.base.dao.interfaces.RFWDAOConverterInterface;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler.MeasureDimension;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler.MeasureUnit;
import br.eng.rodrigogml.rfw.base.measureruler.vo.CustomMeasureUnitGeneric;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;

/**
 * Description: Implementa o conversor do DAO para que seja possível persistir um objeto que utilize a MeasureUnit como um de seus atributos..<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (13 de out de 2018)
 */
public class MeasureUnitDAOConverter implements RFWDAOConverterInterface {

  @Override
  public Object toVO(Object obj) {
    String value = (String) obj;
    MeasureUnit result = null;
    try {
      if (value != null) {
        if (value.startsWith("#")) {
          int indexParenteses = value.indexOf('(');
          String symbol = value.substring(1, indexParenteses - 1); // -1 para remover o espaço
          String name = value.substring(indexParenteses + 1, value.length() - 1);
          result = new CustomMeasureUnitGeneric(name, symbol);
        } else {
          result = MeasureRuler.valueOf(value);
        }
      }
    } catch (RFWCriticalException e) {
      RFWLogger.logException(e);
      throw new RuntimeException("Erro ao converter MEASUREUNIT! " + value, e);
    }
    return result;
  }

  @Override
  public Object toDB(Object obj) {
    MeasureUnit value = (MeasureUnit) obj;
    String rvalue = null;
    if (value != null) {
      if (value.getDimension() == MeasureDimension.CUSTOM) {
        // Salva a unidade de medida custom começando com #, no padrão "#<Symbol> (<name>)", para que seja possível fazer o parser e recria-la quando for lida do banco de dados.
        rvalue = "#" + value.getSymbol() + " (" + value.name() + ")";
      } else {
        rvalue = value.name();
      }
    }
    return rvalue;
  }
}
