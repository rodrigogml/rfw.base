package br.eng.rodrigogml.rfw.base.measureruler.interfaces;

import java.math.BigDecimal;
import java.util.HashMap;

import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler.MeasureUnit;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Esta interface define os métodos que o sistema deve implementar para que o {@link MeasureRuler} consiga realizar a transformação entre unidades de medidas de dimenções diferentes. Incluindo unidades definidas pelo usuário.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0 (22 de nov. de 2021)
 */
public interface MeasureRulerEquivalenceInterface {

  /**
   * Deve retornar uma hash com as unidades de medidas da régua de equivalência e seus pesos de conversão. Não confundir com os {@link MeasureUnit#getRatio()}, esses pesos são os pesos de conversão entre dimensões diferentes e/ou unidades de medidas personalizadas, não entre as unidades da mesma dimensão.
   *
   * @return Hash contendo as unidades de medidas e seus pessos para conversão entre dimensões de medidas.
   * @throws RFWException
   */
  public HashMap<MeasureUnit, BigDecimal> getMeasureUnitHash() throws RFWException;

}
