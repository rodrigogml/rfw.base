package br.eng.rodrigogml.rfw.base.vo;

import java.io.Serializable;

import br.eng.rodrigogml.rfw.base.utils.BUReflex;

/**
 * Description: Classe padrão de definição do Meta Object.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (15/07/2015)
 */
public abstract class RFWVO_ implements Serializable {

  private static final long serialVersionUID = 1070633715215781062L;

  private final String basepath;

  protected RFWVO_() {
    this.basepath = null;
  }

  public RFWVO_(String basepath) {
    this.basepath = basepath;
  }

  public String path() {
    return (basepath != null ? basepath : "");
  }

  protected String getAttributePath(String attribute) {
    return BUReflex.getAttributePath(attribute, basepath);
  }

  protected String getAttributePath(String attribute, int index) {
    return BUReflex.getAttributePath(attribute, index, basepath);
  }

  /**
   * Cria o caminho para um item dentro de uma hash.
   *
   * @param attribute Nome do atributo que contém a hash.
   * @param key Representação em String do objeto chave da hash.
   * @param clazz Classe da chave de Hash.
   * @return
   */
  protected String getAttributePath(String attribute, Object key, Class<?> clazz) {
    return BUReflex.getAttributePath(attribute, key, clazz, basepath);
  }

  public final String id() {
    return getAttributePath("id");
  }
}
