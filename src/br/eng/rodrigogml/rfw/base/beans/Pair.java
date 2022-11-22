package br.eng.rodrigogml.rfw.base.beans;

import java.io.Serializable;

/**
 * Description: Simples bean para carregar um conjundo de valores tipo chave/valor.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (24 de out de 2020)
 */
public class Pair<K extends Object, V extends Object> implements Serializable {

  private static final long serialVersionUID = 5451950348161479230L;

  /**
   * Chave do conjunto
   */
  private K key = null;

  /**
   * Valor do Conjunto
   */
  private V value = null;

  /**
   * Cria um conjunto de par vazio.
   */
  public Pair() {
    super();
  }

  /**
   * Cria um conjunto de par definido.
   */
  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

}
