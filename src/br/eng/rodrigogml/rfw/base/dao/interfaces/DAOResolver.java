package br.eng.rodrigogml.rfw.base.dao.interfaces;

import br.eng.rodrigogml.rfw.base.dao.annotations.dao.RFWDAO;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.vo.RFWVO;

/**
 * Description: Interface para permitir que algumas informa??es que precisam ser passadas para o RFWDAO sejam manipuladas/resolvidas pela aplica??o a medida que necess?rias pelo RFWDAO.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 */
public interface DAOResolver {

  /**
   * Este m?todo deve retornar o schema do banco de dados que deve ser utilizado com o VO.
   *
   * @param entityType entidade/RFWVO que se deseja saber o schema a ser utilizado.
   * @param entityDAOAnn {@link RFWDAO} Annotation atual da entidade.
   * @return String com o nome do Schema a ser utilizado no comando SQL.
   */
  public String getSchema(Class<? extends RFWVO> entityType, RFWDAO entityDAOAnn) throws RFWException;

}
