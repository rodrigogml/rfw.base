package br.eng.rodrigogml.rfw.base.vo;

import java.util.HashMap;

import br.eng.rodrigogml.rfw.base.exceptions.RFWException;

/**
 * Description: Esta interface define a implementação de um Clone Recursivo usado pelo framework para duplicar uma estrutura completa de Objetos.<br>
 * Os objetos que implementam essa interfacem devem duplicar qualquer objeto que possa ser obtido pelo método get e definido pelo set e também implemente essa interface.
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (18/02/2016)
 */
public interface RFWRecursiveClonable extends Cloneable {

  public Object clone() throws CloneNotSupportedException;

  public RFWRecursiveClonable cloneRecursive() throws RFWException;

  public RFWRecursiveClonable cloneRecursive(HashMap<RFWRecursiveClonable, RFWRecursiveClonable> clonedObjects) throws RFWException;

}
