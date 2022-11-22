package br.eng.rodrigogml.rfw.base.vo;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import br.eng.rodrigogml.rfw.base.dataformatters.RFWGVODataFormatter;

/**
 * <i>*GVO vem da ideia de "Ghost VO", um objeto transparente para acrescentar uma funcionalidade à objetos já existentes. </i> <br>
 * <br>
 *
 * Esta classe é utilizada como um "encapsulamento" dos VOs quando precisamos que o equals do RFWVO compare os IDs e não a instância de memória (implementação padrão do RFWVO).<br>
 * <Br>
 * <br>
 * <b>Explicação Detalhada:</b><Br>
 * <br>
 * O RFWVO tem utiliza por definição o equals do Java, que compara se o objeto é o mesmo objeto na memória da máquina. Logo, depois VOs iguais (mesmo ID) mas que tenham sido recuperados em instantes diferentes no banco são tratados como diferentes pelo equals. (equals is false)<br>
 * <br>
 * Os componentes do Vaadin, como tabelas, checkbox, combos, etc. utilizam o equals sempre que vc der um setValue() para saber se o objeto passado é igual ao que está no container do componente. Uma vez que a lista do componente é carregada do banco em um momento, e o objeto do set geralmente é carregado junto com o objeto que está sendo editado, temos duas instâncias diferentes para o mesmo
 * objeto. Como o RFWVO retornará "not equals" para esses objetos, o setValue() do vaadin parece nunca funcionar.<br>
 * <br>
 * Para contornar isso, encapsulamos o VO em um objeto chamado {@link GVO}, que simplesmente aceita o VO e imprementa o equals considerando o ID do objeto. Note que pela implementação do equals com base no ID, o GVO não pode ser utilizado com objetos novos (ID nulo). <Br>
 * Uma vez que a o componente utilize o GVO e não mais o VO diretamente, (Ex: ComboBox<GVO<RFWVO>> ao invés de ComboBox<RFWVO>) temos que encapsular a lista dos itens do objeto, para isso utilize o método {@link GVO#wrapToList(List)} ou similires para outras coleções/arrays.<br>
 * <br>
 * Já para fazer o BIND entre o valor do componente e um campo de lista do VO, utilize o {@link RFWGVODataFormatter}, assim ele encapsula os itens do VO no momento de colocar no componente, e o processo contrário ao colocar no VO.
 *
 * @author rodrigo
 * @param <VO> the generic type
 */
public class GVO<VO extends RFWVO> {

  /** The vo. */
  private final VO vo;

  /**
   * Instantiates a new gvo.
   *
   * @param vo the vo
   */
  public GVO(VO vo) {
    Objects.requireNonNull(vo, "VO não pode ser nulo!");
    Objects.requireNonNull(vo.getId(), "O ID do VO não pode ser nulo pois o TreeBean utliza o ID para comparar se ambos são o mesmo objeto durante o Lazy Load do Grid!");
    this.vo = vo;
  }

  /**
   * Gets the vo.
   *
   * @return the vo
   */
  public VO getVO() {
    return vo;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((vo == null || vo.getId() == null) ? 0 : vo.getId().hashCode());
    return result;
  }

  /**
   * O equals do objeto vai verificar a classe e se o ID do objeto é o mesmo para garantir que são os mesmos objetos.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (!(obj instanceof GVO)) return false;
    if (this.vo.getClass() != ((GVO<?>) obj).getVO().getClass()) return false;
    return this.vo.getId().equals(((GVO<?>) obj).getVO().getId());
  }

  /**
   * Retorna o ID do VO de dentro do GVO. Mesmo que {@link #getVO()}.getId().
   *
   * @return the id
   */
  public Long getId() {
    return this.getVO().getId();
  }

  /**
   * Método para encapsular uma lista de RFWVO em uma {@link List} de GVO<RFWVO>.
   *
   * @param <VO> Tipo do VO que será encapsulado.
   * @param list Lista original de VOs.
   * @return Lista com os objetos encapsulados.
   */
  public static <VO extends RFWVO> List<GVO<VO>> wrapToList(List<VO> list) {
    return list.stream().map(GVO::new).collect(Collectors.toList());
  }

  /**
   * Método para encapsular uma lista de RFWVO em um {@link Set} de GVO<RFWVO>.
   *
   * @param <VO> Tipo do VO que será encapsulado.
   * @param list Lista original de VOs.
   * @return Lista com os objetos encapsulados.
   */
  public static <VO extends RFWVO> Set<GVO<VO>> wrapToSet(List<VO> list) {
    return list.stream().map(GVO::new).collect(Collectors.toSet());
  }

  /**
   * Método para desencapsular uma lista de GVO<RFWVO> em uma lista de RFWVO.
   *
   * @param <VO> Tipo do VO que será encapsulado.
   * @param list Lista original de GVO<VOs).
   * @return Lista com os objetos "fora" do GVO.
   */
  public static <VO extends RFWVO> List<VO> unwrapToList(List<GVO<VO>> list) {
    return list.stream().map(GVO::getVO).collect(Collectors.toList());
  }
}