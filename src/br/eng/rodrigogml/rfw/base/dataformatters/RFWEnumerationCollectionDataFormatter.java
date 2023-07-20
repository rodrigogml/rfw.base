package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.List;
import java.util.Locale;

import br.eng.rodrigogml.rfw.base.utils.BUArray;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.rfwmeta.RFWMetaCollectionField;

/**
 * Description: DataFormater utilizado para exibir o conteúdo de uma Lista de enumerations, normalmente é anotado com {@link RFWMetaCollectionField}.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (16 de nov de 2020)
 */
public class RFWEnumerationCollectionDataFormatter<E extends Enum<?>> implements RFWDataFormatter<String, List<E>> {

  private int maxItems = 3;

  /**
   * Cria um DataFormatter capaz de converter o conteúdo de uma lista de Enumerations em uma String com os valores concatenados.<br>
   * Utiliza o método {@link BUArray#concatArrayIntoString(Enum[], int)}
   *
   * @param maxItems Número máximo de itens para ser escrito, se tiver mais itens que a quantidade definida, é resumodo com o texto ", +X"
   */
  public RFWEnumerationCollectionDataFormatter(int maxItems) {
    this.maxItems = maxItems;
  }

  @Override
  public String toPresentation(List<E> list, Locale locale) throws RFWException {
    return BUArray.concatArrayIntoString(list, this.maxItems);
  }

  @Override
  public List<E> toVO(String formattedvalue, Locale locale) throws RFWException {
    throw new RFWCriticalException("Sem suporte à conversão do valor para o VO!");
  }

  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    // Não temos validação pois é um DataFormatter apenas de formatação
  }

  @Override
  public int getMaxlenght() {
    return 100;
  }

}
