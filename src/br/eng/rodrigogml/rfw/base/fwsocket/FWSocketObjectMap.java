package br.eng.rodrigogml.rfw.base.fwsocket;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Description: Object que define um comando a ser transmitido ou recebido través do FWSocket.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.0.0 (12/11/2014)
 */
public class FWSocketObjectMap implements Serializable {

  private static final long serialVersionUID = -5954956264115360259L;

  public static final String PROPERTY_EXCEPTION = FWSocketObjectMap.class.getCanonicalName() + ".EXCEPTION";

  private HashMap<String, Serializable> properties = new HashMap<>();

  public FWSocketObjectMap() {
  }

  public FWSocketObjectMap(Throwable e) {
    this.put(PROPERTY_EXCEPTION, e);
  }

  public void put(String property, Serializable value) {
    this.properties.put(property, value);
  }

  public void clear() {
    this.properties.clear();
  }

  public void remove(String property) {
    this.properties.remove(property);
  }

  public Serializable get(String property) {
    return this.properties.get(property);
  }

  public Set<String> keySet() {
    return properties.keySet();
  }
}
