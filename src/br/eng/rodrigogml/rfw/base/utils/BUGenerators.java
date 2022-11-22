package br.eng.rodrigogml.rfw.base.utils;

import java.util.Random;
import java.util.UUID;

/**
 * Description: Classe utilitária com métodos de geração de dados.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (12 de jul de 2018)
 */
public class BUGenerators {

  /**
   * Expressão regular para validar o UUID gerado pelo método {@link #generateUUID()}
   */
  public static final String UUID_REGEXP = "[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}";

  private BUGenerators() {
  }

  /**
   * Gera um identificador único baseado no UUID. Veja mais em http://www.baeldung.com/java-uuid <br>
   * <br>
   * Exemplo: '00e4a76d-c792-4909-994f-371633f60b63' 36 carecteres.
   *
   * <br>
   * <br>
   * Para validar a UUID utilize a regexp definida em {@link #UUID_REGEXP}.
   *
   * @return Identificador Único gerado
   */
  public static String generateUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Gera uma String alphanumérica (que combina com a expressão regular: "[0-9a-zA-Z]+" com o tamanho desejado.
   *
   * @param length Tamanho da String
   * @return String gerada aleatóriamente.
   */
  public static String generateString(int length) {
    // Mapa de caracteres
    // 0 => 48 até 9 => 57 = Total 10
    // A => 65 até Z => 90 = total 26
    // a => 97 até z => 122 = total 26
    int totalchars = 62;

    final Random random = new Random(System.nanoTime());
    StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int r = (int) (random.nextFloat() * totalchars);
      if (r < 10) {
        r += 48;
      } else if (r < 36) {
        r += 55; // 65 -10
      } else {
        r += 61; // 97 - 26 -10
      }
      buffer.append((char) r);
    }
    return buffer.toString();
  }
}
