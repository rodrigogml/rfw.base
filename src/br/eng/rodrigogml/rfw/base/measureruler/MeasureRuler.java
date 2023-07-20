package br.eng.rodrigogml.rfw.base.measureruler;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.base.measureruler.interfaces.MeasureRulerEquivalenceInterface;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.preprocess.PreProcess;

/**
 * Description: Classe utilizada para criar as definições de unidades de medidas do framework. Conversões, "traduções", e outras necessidades relacionadas a grandezas de medidas.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (10 de out de 2018)
 */
public final class MeasureRuler {

  /**
   * Construtor privado para classe esttática.
   */
  private MeasureRuler() {
  }

  /**
   * Enum utilizada para definir as grandezas de medidas do sistema.
   */
  public static enum MeasureDimension {
    WEIGHT(WeightUnit.values()), UNIT(UnitUnit.values()), VOLUME(VolumeUnit.values()), LENGTH(LengthUnit.values()), AREA(AreaUnit.values()), CUSTOM(null);

    private MeasureUnit[] units = null;

    MeasureDimension(MeasureUnit[] units) {
      this.units = units;
    }

    public MeasureUnit[] getUnits() {
      return units;
    }
  }

  /**
   * Interface utilizada para definir uma unidade de medida.
   */
  public static interface MeasureUnit extends Serializable {

    /**
     * Razão de tamanho da unidade dentro da Dimensão.
     *
     * @return Ex: 1000 para Kg, 1 para g, 1 para m, 1000 para Km, etc.
     */
    BigDecimal getRatio();

    /**
     * Retorna a Dimensão da unidade atual.
     *
     * @return {@link MeasureDimension} do {@link MeasureUnit} atual.
     */
    MeasureDimension getDimension();

    /**
     * Simbolo da Unidade de Medida.
     *
     * @return Ex: Kg para Kilograma, Km para Kilometro, t para Tonelada,
     */
    String getSymbol();

    /**
     * Nome da Unidade de Medida.<br>
     * Redundância em relação ao método {@link #name()}, para que seja possível utilziar o padrão de métodos "get" com as classes do Framework.
     *
     * @return Retorna o .name() do Enum mascarado pela interface MeasureUnit.
     */
    String getName();

    /**
     * Nome da Unidade de Medida.
     *
     * @return Retorna o .name() do Enum mascarado pela interface MeasureUnit.
     */
    String name();

    /**
     * Número Ordinal do item.
     *
     * @return Retorna o .orginal() do Enum mascarado pela interface MeasureUnit.
     */
    int ordinal();
  }

  /**
   * Interface abaixo da {@link MeasureUnit} simplesmente para diferenciar as unidades de medida padrão das {@link MeasureDimension#CUSTOM}.
   *
   * Nenhum método adicional.
   */
  public static interface CustomMeasureUnit extends MeasureUnit {
  }

  public static enum WeightUnit implements MeasureUnit {
    KILOGRAM(new BigDecimal("1000"), "Kg"), GRAM(BigDecimal.ONE, "g"), MILIGRAM(new BigDecimal("0.001"), "mg"), TONNE(new BigDecimal("1000000"), "t");

    WeightUnit(BigDecimal ratio, String symbol) {
      this.ratio = ratio;
      this.symbol = symbol;
    }

    private BigDecimal ratio;
    private String symbol;

    @Override
    public BigDecimal getRatio() {
      return this.ratio;
    }

    @Override
    public String getSymbol() {
      return this.symbol;
    }

    @Override
    public String getName() {
      return super.name();
    }

    @Override
    public MeasureDimension getDimension() {
      return MeasureDimension.WEIGHT;
    }
  }

  public static enum UnitUnit implements MeasureUnit {
    UNIT(BigDecimal.ONE, "Und"), COUPLE(new BigDecimal("2"), "Par"), DOZEN(new BigDecimal("12"), "Dz"), CENT(RFW.BIGHUNDRED, "CNT"), MIL(new BigDecimal("1000"), "MIL");

    private BigDecimal ratio;
    private String symbol;

    UnitUnit(BigDecimal ratio, String symbol) {
      this.ratio = ratio;
      this.symbol = symbol;
    }

    @Override
    public BigDecimal getRatio() {
      return this.ratio;
    }

    @Override
    public String getName() {
      return super.name();
    }

    @Override
    public String getSymbol() {
      return this.symbol;
    }

    @Override
    public MeasureDimension getDimension() {
      return MeasureDimension.UNIT;
    }
  }

  public static enum VolumeUnit implements MeasureUnit {
    LITER(BigDecimal.ONE, "L"), MILLILITER(new BigDecimal("0.001"), "mL"), CUBICMETER(new BigDecimal("1000"), "m3");

    private BigDecimal ratio;
    private String symbol;

    VolumeUnit(BigDecimal ratio, String symbol) {
      this.ratio = ratio;
      this.symbol = symbol;
    }

    @Override
    public BigDecimal getRatio() {
      return this.ratio;
    }

    @Override
    public String getName() {
      return super.name();
    }

    @Override
    public String getSymbol() {
      return this.symbol;
    }

    @Override
    public MeasureDimension getDimension() {
      return MeasureDimension.VOLUME;
    }
  }

  public static enum LengthUnit implements MeasureUnit {
    METER(BigDecimal.ONE, "m"), CENTIMETER(new BigDecimal("0.01"), "cm"), MILIMETER(new BigDecimal("0.001"), "mm");

    private BigDecimal ratio;
    private String symbol;

    LengthUnit(BigDecimal ratio, String symbol) {
      this.ratio = ratio;
      this.symbol = symbol;
    }

    @Override
    public BigDecimal getRatio() {
      return this.ratio;
    }

    @Override
    public String getName() {
      return super.name();
    }

    @Override
    public String getSymbol() {
      return this.symbol;
    }

    @Override
    public MeasureDimension getDimension() {
      return MeasureDimension.LENGTH;
    }
  }

  public static enum AreaUnit implements MeasureUnit {
    SQUAREMETER(BigDecimal.ONE, "m2");

    private BigDecimal ratio;
    private String symbol;

    AreaUnit(BigDecimal ratio, String symbol) {
      this.ratio = ratio;
      this.symbol = symbol;
    }

    @Override
    public BigDecimal getRatio() {
      return this.ratio;
    }

    @Override
    public String getName() {
      return super.name();
    }

    @Override
    public String getSymbol() {
      return this.symbol;
    }

    @Override
    public MeasureDimension getDimension() {
      return MeasureDimension.AREA;
    }
  }

  public static MeasureUnit valueOf(String name) throws RFWCriticalException {
    MeasureUnit result = null;
    try {
      result = Enum.valueOf(UnitUnit.class, name);
    } catch (IllegalArgumentException e) {
      try {
        result = Enum.valueOf(WeightUnit.class, name);
      } catch (IllegalArgumentException e1) {
        try {
          result = Enum.valueOf(VolumeUnit.class, name);
        } catch (IllegalArgumentException e2) {
          try {
            result = Enum.valueOf(AreaUnit.class, name);
          } catch (IllegalArgumentException e3) {
            try {
              result = Enum.valueOf(LengthUnit.class, name);
            } catch (IllegalArgumentException e4) {
              // Se não é de nenhuma unidade acima, tentamos as EQUIVALENCIAS
              // try {
              // final String[] params = name.split("\\|");
              // final EntityManager em = TransactionManager.getEntityManager();
              // final CriteriaBuilder cb = em.getCriteriaBuilder();
              // final CriteriaQuery<MeasureEquivalenceVO> cq = cb.createQuery(MeasureEquivalenceVO.class);
              // final Root<MeasureEquivalenceVO> root = cq.from(MeasureEquivalenceVO.class);
              //
              // final MeasureEquivalenceVO_ v_ = MeasureEquivalenceVO_.VO;
              // final RFWMO mo = new RFWMO();
              // mo.equal(v_.measurerulervo().id(), params[0]);
              // mo.equal(v_.measureCustomVO().id(), params[1]);
              //
              // cq.where(RFWDAO.createPredicateFromMO(cb, cq, root, mo));
              // final TypedQuery<MeasureEquivalenceVO> query = em.createQuery(cq);
              // if (query.getResultList().size() == 1) {
              // MeasureEquivalenceVO equiVO = query.getResultList().get(0);
              // RFWDAO.exploitAttribute(equiVO, MeasureEquivalenceVO_.vo().measureCustomVO().id(), em);
              // RFWDAO.exploitAttribute(equiVO, MeasureEquivalenceVO_.vo().measurerulervo().id(), em);
              // em.flush();
              // em.detach(equiVO);
              // result = new MeasureEquivalenceBean(equiVO);
              // } else if (query.getResultList().size() > 1) {
              // throw new RFWCriticalException("RFW_ERR_100713", e4);
              // }
              // } catch (Exception e5) {
              // throw new RFWCriticalException("não foi possível desserializar o valor '" + name + "'", e4);
              // }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Converte um valor de uma unidade de medida para outra unidade de medida.<br>
   * O método só permite converter unidades entre a mesma Dimensão (área para área, volume para volume, massa para massa, etc.)<br>
   * Este método retorna o valor com uma precisão de 3 casas decimais. Em geral 3 casas são o suficiente para exibição para o usuário, mas para cálculos continuados esse arredondamento pode gerar falta de precisão no cálculo. Para trabalhar com maior precisão de casas utilize o método {@link #convertTo(BigDecimal, MeasureUnit, MeasureUnit, int)}
   *
   * @param value Valor da unidade a ser convertido.
   * @param startUnit Unidade de medida atual do valor passado.
   * @param endUnit Unidade de medida para a qual desejamos converter
   * @return Valor convertido para a nova unidade de medida com a precisão de 3 casas decimais.
   * @throws RFWException
   */
  public static BigDecimal convertTo(BigDecimal value, MeasureUnit startUnit, MeasureUnit endUnit) throws RFWException {
    return convertTo(value, startUnit, endUnit, 3);
  }

  /**
   * Converte um valor de uma unidade de medida para outra unidade de medida.<br>
   * O método só permite converter unidades entre a mesma Dimensão (área para área, volume para volume, massa para massa, etc.)<br>
   *
   * @param value Valor da unidade a ser convertido.
   * @param startUnit Unidade de medida atual do valor passado.
   * @param endUnit Unidade de medida para a qual desejamos converter
   * @param precision Total de casas decimais utilizadas. Evita que o arredondamento seja feito "muito curto" criando imprecisão de cálculo quando a conversão é utilizada em cálculos maiores.
   *
   * @return Valor convertido para a nova unidade de medida com a precisão das casas decimais definidas pelo parâmetro 'precision".
   * @throws RFWException
   */
  public static BigDecimal convertTo(BigDecimal value, MeasureUnit startUnit, MeasureUnit endUnit, int precision) throws RFWException {
    return convertTo(null, value, startUnit, endUnit, precision);
  }

  /**
   * Converte um valor de uma unidade de medida para outra unidade de medida.<br>
   * O método só permite converter unidades entre a mesma Dimensão (área para área, volume para volume, massa para massa, etc.)<br>
   *
   * @param equivalence Interface de definição de equivalências entre as unidades de medidas.
   * @param value Valor da unidade a ser convertido.
   * @param startUnit Unidade de medida atual do valor passado.
   * @param endUnit Unidade de medida para a qual desejamos converter
   *
   * @return Valor convertido para a nova unidade de medida com a precisão de 3 casas decimais.
   * @throws RFWException
   */
  public static BigDecimal convertTo(MeasureRulerEquivalenceInterface equivalence, BigDecimal value, MeasureUnit startUnit, MeasureUnit endUnit) throws RFWException {
    return convertTo(equivalence, value, startUnit, endUnit, 3);
  }

  /**
   * Converte um valor de uma unidade de medida para outra unidade de medida.<br>
   * O método só permite converter unidades entre a mesma Dimensão (área para área, volume para volume, massa para massa, etc.)<br>
   *
   * @param equivalence Interface de definição de equivalências entre as unidades de medidas.
   * @param value Valor da unidade a ser convertido.
   * @param startUnit Unidade de medida atual do valor passado.
   * @param endUnit Unidade de medida para a qual desejamos converter
   * @param precision Total de casas decimais utilizadas. Evita que o arredondamento seja feito "muito curto" criando imprecisão de cálculo quando a conversão é utilizada em cálculos maiores.
   *
   * @return Valor convertido para a nova unidade de medida com a precisão das casas decimais definidas pelo parâmetro 'precision".
   * @throws RFWException
   */
  public static BigDecimal convertTo(MeasureRulerEquivalenceInterface equivalence, BigDecimal value, MeasureUnit startUnit, MeasureUnit endUnit, int precision) throws RFWException {
    if (startUnit == null || endUnit == null || value == null) {
      throw new RFWCriticalException("Para converter unidades de medidas, todas as informaçães são obrigatórias!");
    } else {
      BigDecimal startWeight = null;
      BigDecimal endWeight = null;

      if (startUnit.getDimension() != MeasureDimension.CUSTOM && startUnit.getDimension() == endUnit.getDimension()) {
        startWeight = BigDecimal.ONE;
        endWeight = BigDecimal.ONE;
      } else {
        if (equivalence == null) throw new RFWValidationException("Não é possível converter entre Dimensões diferentes sem uma regra de equivalência definida! (" + RFWBundle.get(startUnit.getDimension()) + " -> " + RFWBundle.get(endUnit.getDimension()) + "')");

        HashMap<MeasureUnit, BigDecimal> eqHash = equivalence.getMeasureUnitHash();
        for (Entry<MeasureUnit, BigDecimal> entry : eqHash.entrySet()) {
          MeasureUnit tmu = entry.getKey();
          BigDecimal weight = entry.getValue();

          if (tmu.getDimension() == MeasureDimension.CUSTOM) {
            if (startUnit.getDimension() == MeasureDimension.CUSTOM && tmu.getSymbol().equals(startUnit.getSymbol()) && tmu.name().equals(startUnit.name())) {
              startWeight = weight.multiply(tmu.getRatio());
            } else if (endUnit.getDimension() == MeasureDimension.CUSTOM && tmu.getSymbol().equals(endUnit.getSymbol()) && tmu.name().equals(endUnit.name())) {
              endWeight = weight.multiply(tmu.getRatio());
            }
          } else {
            if (startUnit.getClass() == tmu.getClass()) {
              startWeight = weight.multiply(tmu.getRatio());
            } else if (endUnit.getClass() == tmu.getClass()) {
              endWeight = weight.multiply(tmu.getRatio());
            }
          }
          if (startWeight != null && endWeight != null) break;
        }
        if (startWeight == null) throw new RFWValidationException("Não foi possível converter as dimensões de medida pois a régua de equivalencias não tem informações para a '" + RFWBundle.get(startUnit) + "/" + RFWBundle.get(startUnit.getDimension()) + "'!");
        if (endWeight == null) throw new RFWValidationException("Não foi possível converter as dimensões de medida pois a régua de equivalencias não tem informações para a '" + RFWBundle.get(endUnit) + "/" + RFWBundle.get(endUnit.getDimension()) + "'!");
      }

      BigDecimal num = value.multiply(startUnit.getRatio()).multiply(endWeight);
      BigDecimal div = startWeight.multiply(endUnit.getRatio());
      return num.divide(div, precision, RFW.getRoundingMode());
    }
  }

  /**
   * Valida o conteúdo da Hash retornada. <br>
   * Procura por valores redundantes e/ou incoerentes.<Br>
   * Garante que tenha no mínimo 2 dimensões diferentes, uma tabela de equivalências com um único valor não tem utilidade alguma.
   *
   * @param equivalence Interface que retorna a hash com as informações de equivalencia.
   * @throws RFWException
   */
  public static void validateMeasureRulerEquivalence(MeasureRulerEquivalenceInterface equivalence) throws RFWException {
    PreProcess.requiredNonNullCritical(equivalence, "A interface de equivalência não pode ser passada nula!");
    HashMap<MeasureUnit, BigDecimal> hash = equivalence.getMeasureUnitHash();
    PreProcess.requiredNonNullCritical(hash, "A hash de equivalências não pode ser nula!");

    ArrayList<Entry<MeasureUnit, BigDecimal>> entryList = new ArrayList<>(hash.entrySet());
    for (int i = 0; i < entryList.size(); i++) {
      Entry<MeasureUnit, BigDecimal> entry = entryList.get(i);

      MeasureUnit mu = entry.getKey();
      BigDecimal weight = entry.getValue();
      PreProcess.requiredNonNullCritical(mu, "A hash de equivalências não pode ter uma chave nula!");
      PreProcess.requiredNonNullCritical(weight, "A hash de equivalências não pode ter uma valor nulo! '" + mu.name() + "'!");

      if (weight.compareTo(BigDecimal.ZERO) <= 0) throw new RFWValidationException("O peso de equivalência deve ser positivo maior que zero! '" + mu.name() + "'.");

      for (int x = i + 1; x < entryList.size(); x++) {
        Entry<MeasureUnit, BigDecimal> entryTmp = entryList.get(x);
        if (mu.getDimension() == MeasureDimension.CUSTOM && entryTmp.getKey().getDimension() == MeasureDimension.CUSTOM) {
          if (entryTmp.getKey().getClass() == mu.getClass() && entryTmp.getKey().getSymbol().equals(mu.getSymbol()) && entryTmp.getKey().name().equals(mu.name())) {
            throw new RFWValidationException("Foram encontradas mais de uma informação para a unidade de medida personalizada '" + mu.name() + " (" + mu.getSymbol() + ")'");
          }
        } else {
          if (entryTmp.getKey().getDimension() == mu.getDimension()) {
            throw new RFWValidationException("Foram encontradas mais de uma informação para a dimensão '" + mu.getDimension() + "'");
          }
        }
      }
    }

    if (hash.size() < 2) throw new RFWValidationException("A tabela de equivalência precisa ter 2 ou mais equivalências para ter alguma utilidade.");
  }

  /**
   * Extrai todas as unidades de medida que <b>podem</b> ser utilizadas a partir das equivalências definidas.<br>
   * Realiza a chamada do método {@link #cleanInvalidEquivalences(MeasureRulerEquivalenceInterface)} antes de separar as unidades de medida.
   *
   * @param equivalence Equivalência entre unidades de medidas.
   * @return Lista com todos os objetos MeasureUnit de todas as dimensões definidas na equivalência e/ou unidades personalizadas.
   * @throws RFWException
   */
  public static LinkedHashSet<MeasureUnit> extractAllMeasureUnites(MeasureRulerEquivalenceInterface equivalence) throws RFWException {
    return extractAllMeasureUnites(equivalence, null);
  }

  /**
   * Extrai todas as unidades de medida que <b>podem</b> ser utilizadas a partir das equivalências definidas + as unidades de medida de uma dimensão específica.<br>
   * Realiza a chamada do método {@link #cleanInvalidEquivalences(MeasureRulerEquivalenceInterface)} antes de separar as unidades de medida.<br>
   * Este método foi criado para que tenhamos as unidades de medida da régua + a unidade padrão de um item, já que não conseguimos ter uma régia de equivalências só com uma Dimensão (o método {@link #cleanInvalidEquivalences(MeasureRulerEquivalenceInterface)} excluí).
   *
   * @param equivalence Equivalência entre unidades de medidas.
   * @return Lista com todos os objetos MeasureUnit de todas as dimensões definidas na equivalência e/ou unidades personalizadas.
   * @throws RFWException
   */
  public static LinkedHashSet<MeasureUnit> extractAllMeasureUnites(MeasureRulerEquivalenceInterface equivalence, MeasureDimension dimension) throws RFWException {
    HashMap<MeasureUnit, BigDecimal> hash = null;
    if (equivalence != null) hash = cleanInvalidEquivalences(equivalence);

    final LinkedHashSet<MeasureUnit> set = new LinkedHashSet<MeasureRuler.MeasureUnit>();

    if (dimension != null) {
      for (MeasureUnit unit : dimension.getUnits()) {
        set.add(unit);
      }
    }

    if (hash != null) {
      for (MeasureUnit measureUnit : hash.keySet()) {
        if (measureUnit.getDimension() == MeasureDimension.CUSTOM) {
          set.add(measureUnit);
        } else {
          for (MeasureUnit mu2 : measureUnit.getDimension().getUnits()) {
            set.add(mu2);
          }
        }
      }
    }

    return set;
  }

  /**
   * Valida os objetos dentro do equivalence e remove todos os objetos que não tiverem as informações de "ratio" e "MeasureUnit" (name e symbol) definidos.<br>
   * Se depois de limpar os itens inválidos sobrar apenas 0 ou 1 entrada válida o método retorna null, pois equivalências só são úteis quando estabelecem a relação entre pelo menos 2 grandezas de medidas.
   *
   * @param equivalence Interface de acesso às Equivalências.
   * @return HashMap apenas com as equivalências válidas, ou nulo caso só exista uma ou nenhuma entrada válida.
   * @throws RFWException
   */
  public static HashMap<MeasureUnit, BigDecimal> cleanInvalidEquivalences(MeasureRulerEquivalenceInterface equivalence) throws RFWException {
    PreProcess.requiredNonNullCritical(equivalence, "A interface de equivalência não pode ser passada nula!");
    HashMap<MeasureUnit, BigDecimal> hash = equivalence.getMeasureUnitHash();
    PreProcess.requiredNonNullCritical(hash, "A hash de equivalências não pode ser nula!");

    final HashMap<MeasureUnit, BigDecimal> newHash = new HashMap<MeasureRuler.MeasureUnit, BigDecimal>();
    for (Entry<MeasureUnit, BigDecimal> entry : hash.entrySet()) {
      if (entry.getValue() != null && entry.getKey() != null && entry.getKey().getRatio() != null && entry.getKey().getRatio().compareTo(BigDecimal.ZERO) > 0 && entry.getKey().name() != null && entry.getKey().getSymbol() != null) {
        newHash.put(entry.getKey(), entry.getValue());
      }
    }

    if (newHash.size() < 2) return null;

    return newHash;
  }

  /**
   * Verifica se a unidade de medida está configurada nas equivalências passada.<br>
   * Verifica as unidades de medida padrões do sistema comparando suas dimensões. Já as unidades personalizadas são comparadas com base no seu nome e símbolo.<br>
   * Executa a limpeza da régua antes de avaliar, ou seja, só returna que é equivalente caso a unidade esteja devidamente configurada.
   *
   * @param equivalence Interface de equivalências
   * @param measureUnit Unidade de medidas à ser testada contra a coleção de equivalências.
   * @return true caso a unidade de medida esteja configurada nas equivalências, false caso contrário.
   * @throws RFWException
   */
  public static boolean isMeasureUnitEquivalent(MeasureRulerEquivalenceInterface equivalence, MeasureUnit measureUnit) throws RFWException {
    PreProcess.requiredNonNullCritical(equivalence, "A interface de equivalência não pode ser passada nula!");
    PreProcess.requiredNonNullCritical(equivalence.getMeasureUnitHash(), "A hash de equivalências não pode ser nula!");

    HashMap<MeasureUnit, BigDecimal> hash = cleanInvalidEquivalences(equivalence);

    if (hash != null) {
      for (Entry<MeasureUnit, BigDecimal> entry : hash.entrySet()) {
        MeasureUnit mu = entry.getKey();
        if (mu.getDimension() == MeasureDimension.CUSTOM && measureUnit.getDimension() == MeasureDimension.CUSTOM) {
          if (mu.getSymbol().equals(measureUnit.getSymbol()) && mu.name().equals(measureUnit.name())) {
            return true;
          }
        } else if (mu.getDimension() == measureUnit.getDimension()) {
          if (mu.getClass() == measureUnit.getClass()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Recupera o ratio de conversão de uma unidade de medida atualmente configurada na régua de equivalências passada.<br>
   * Unidades de medidas personalizadas serão comparadas com base no seu nome e símbolo.<br>
   * Unidades de medida do sistema serão comparadas conforme sua dimensão, e caso necessário serão convertidas. Isto quer dizer que, se na régua estiver configurado 1Kg, e for solicitado o ratio para "GRAM" o valor retornado será de 1000 e não de 1 como consta na Hash. (Afinal, internamente já há a configuração de 1kg = 1000g).
   *
   * @param equivalence Interface de equivalências
   * @param measureUnit Unidade de medidas à ser encontrada na coleção de equivalências.
   * @return Ratio de equivalência da unidade de medida solicitada, ou nulo, caso a unidade de medida solicitada não seja encontrada.
   * @throws RFWException
   */
  public static BigDecimal getRatio(MeasureRulerEquivalenceInterface equivalence, MeasureUnit measureUnit) throws RFWException {
    PreProcess.requiredNonNullCritical(equivalence, "A interface de equivalência não pode ser passada nula!");
    PreProcess.requiredNonNullCritical(equivalence.getMeasureUnitHash(), "A hash de equivalências não pode ser nula!");

    HashMap<MeasureUnit, BigDecimal> hash = cleanInvalidEquivalences(equivalence);

    if (hash != null) {
      for (Entry<MeasureUnit, BigDecimal> entry : hash.entrySet()) {
        MeasureUnit mu = entry.getKey();
        if (mu.getDimension() == MeasureDimension.CUSTOM && measureUnit.getDimension() == MeasureDimension.CUSTOM) {
          if (mu.getSymbol().equals(measureUnit.getSymbol()) && mu.name().equals(measureUnit.name())) {
            return entry.getValue();
          }
        } else if (mu.getDimension() == measureUnit.getDimension()) {
          if (mu.getClass() == measureUnit.getClass()) {
            return convertTo(entry.getValue(), mu, measureUnit);
          }
        }
      }
    }
    return null;
  }
}
