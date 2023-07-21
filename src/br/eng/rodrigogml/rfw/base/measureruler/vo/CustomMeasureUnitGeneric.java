package br.eng.rodrigogml.rfw.base.measureruler.vo;

import java.math.BigDecimal;
import java.util.Objects;

import br.eng.rodrigogml.rfw.kernel.measureruler.interfaces.CustomMeasureUnit;

/**
 * Description: Objeto imut�vel utilizado para definir uma unidade de medida personalizada.<br>
 * Esta classe tem o m�todo equals implementando para que ela seja considerada a mesma classe sempre que os atributos {@link #name} e {@link #symbol} retornarem equals() == true.
 *
 * @author Rodrigo Leit�o
 * @since 10.0 (25 de nov. de 2021)
 */
public class CustomMeasureUnitGeneric implements CustomMeasureUnit {

  private static final long serialVersionUID = 6075304169912995798L;

  private final String name;
  private final String symbol;

  public CustomMeasureUnitGeneric(String name, String symbol) {
    super();
    this.name = name;
    this.symbol = symbol;
  }

  /**
   * N�o tem um Ratio pois as unidades de medidas personalizadas n�o tem equival�ncia entre elas. Retornamos sempre 1 para simplificar as formulas de transi��o entre elas e as dimens�es suportadas pelo sistema.
   *
   * @return BigDecimal com valor 1
   */
  @Override
  public BigDecimal getRatio() {
    return BigDecimal.ONE;
  }

  /**
   * Retorna sempre a constante {@link MeasureDimension#CUSTOM}
   *
   * @return Retorna sempre a constante {@link MeasureDimension#CUSTOM}
   */
  @Override
  public MeasureDimension getDimension() {
    return MeasureDimension.CUSTOM;
  }

  @Override
  public String getSymbol() {
    return this.symbol;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Sempre retorna -1 por n�o ter uma numera��o como o ENUM.
   */
  @Override
  public int ordinal() {
    return -1;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, symbol);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CustomMeasureUnitGeneric other = (CustomMeasureUnitGeneric) obj;
    return Objects.equals(name, other.name) && Objects.equals(symbol, other.symbol);
  }
}
