package br.eng.rodrigogml.rfw.base.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.preprocess.PreProcess;

/**
 * Description: Classe utilitária para manipulação de Arrays.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (18/02/2016)
 */
public class BUArray {

  private BUArray() {
    // Construtor privado para classe statica
  }

  /**
   * Concatena arrays do mesmo tipo, criando um novo array com a soma dos tamanhos dos Arrays.
   *
   * @param first Primeiro Array que será clonado e redimensionado. ESTE OBJETO NÂO PODE SER NULO.
   * @param rest Lista de Arrays que devem ser concatenados ao clone do primeiro.
   * @return Array redimensionado com todos os objetos.
   * @throws RFWException
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] concatAll(T[] first, T[]... rest) throws RFWException {
    PreProcess.requiredNonNullCritical(first, "O primeiro Array não pode ser nulo.");
    int totalLength = first.length;
    for (T[] array : rest) {
      if (array != null) totalLength += array.length;
    }
    T[] result = Arrays.copyOf(first, totalLength);
    int offset = first.length;
    for (T[] array : rest) {
      if (array != null) {
        System.arraycopy(array, 0, result, offset, array.length);
        offset += array.length;
      }
    }
    return result;
  }

  /**
   * Acrescenta um valor ao final do array.
   *
   * @param first Primeiro Array que será clonado e redimensionado. ESTE OBJETO NÂO PODE SER NULO.
   * @param values Valore a serem colocado no Array.
   * @return Array redimensionado com todos os objetos.
   * @throws RFWException
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] addValues(T[] first, T... values) throws RFWException {
    return concatAll(first, values);
  }

  /**
   * Cria um {@link ArrayList} a partir de valores passados.
   *
   * @param <T> Tipo do ArrayList
   * @param content Conteúdo a ser coocado no {@link ArrayList}
   * @return {@link ArrayList} com o conteúdo desejado.
   */
  @SuppressWarnings("unchecked")
  public static <T> ArrayList<T> createArrayList(T... content) {
    final ArrayList<T> list = new ArrayList<>();
    for (int i = 0; i < content.length; i++) {
      list.add(content[i]);
    }
    return list;
  }

  /**
   * Cria um {@link HashSet} a partir de valores passados.
   *
   * @param <T> Tipo do HashSet
   * @param content Conteúdo a ser coocado no {@link HashSet}
   * @return {@link HashSet} com o conteúdo desejado.
   */
  @SuppressWarnings("unchecked")
  public static <T> HashSet<T> createHashSet(T... content) {
    final HashSet<T> set = new HashSet<>();
    for (int i = 0; i < content.length; i++) {
      set.add(content[i]);
    }
    return set;
  }

  /**
   * Cria um Array com todos os objetos recidos que não sejam nulos, e a cada objeto inclui no array um objeto "separador". Note que este método não permite que separadores sejam colocados em sequência. Caso um objeto seja nulo, entre dois objetos não nulos, apenas um separador é colocado.
   *
   * @param <T>
   * @param separator Separador a ser colocado no array entre os objetos válidos.
   * @param values Array de objetos valores, que serão verificados apenas os diferentes de nulo, e colocados no array de retorno separados pelo objeto separador.
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] createArrayWithAllNonNullAndSeparator(T separator, T... values) {
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) count++;
    }
    if (count == 0) return Arrays.copyOf(values, 0);
    T[] result = Arrays.copyOf(values, count * 2 - 1); // Cria o array com o total de objetos válidos + 1 separados para cada intermédio entre um objeto e outro.
    int a = 0;
    for (int i = 0; i < result.length; i++) {
      while (values[a] == null)
        a++;
      result[i] = values[a++];
      i++;
      if (i < result.length) result[i] = separator;
    }
    return result;
  }

  /**
   * Verifique se um Array contém determinado objeto. Verifique com o equals.
   *
   * @param itemsArray
   * @param item
   * @return
   */
  public static <E extends Object> boolean contains(E[] itemsArray, E item) {
    for (E e : itemsArray) {
      if (e.equals(item)) return true;
    }
    return false;
  }

  /**
   * Este método obtem um array de Enumerations de valores, e escreve os mesmos em uma lista separadas por ",". <br>
   * Se o total de itens a serem escritos estrapolar o valor definido em maxItens o total restante é resumindo com a expressão "+N", onde N indica quantos itens foram suprimidos.<br>
   * Caso o total de items passado seja o total de itens disponíveis na enumeration retorna apenas o texto "*".
   *
   * @param <E> Tipo da enumeration.
   * @param items Lista de Itens selecionados
   * @param maxItens Máximo de itens a serem escritos
   * @return
   */
  public static <E extends Enum<?>> String concatArrayIntoString(E[] items, int maxItens) {
    return concatArrayIntoString(items, maxItens, ", ");
  }

  /**
   * Este método obtem um array de Enumerations de valores, e escreve os mesmos em uma lista separadas por ",". <br>
   * Se o total de itens a serem escritos estrapolar o valor definido em maxItens o total restante é resumindo com a expressão "+N", onde N indica quantos itens foram suprimidos.<br>
   * Caso o total de items passado seja o total de itens disponíveis na enumeration retorna apenas o texto "*".
   *
   * @param <E> Tipo da enumeration.
   * @param items Lista de Itens selecionados
   * @param maxItens Máximo de itens a serem escritos
   * @param separator Define a String que será concatenada entre os elementos. Se utilizado ", " o resultado será o mesmo que o do método {@link #concatArrayIntoString(Enum[], int)}
   * @return
   */
  public static <E extends Enum<?>> String concatArrayIntoString(E[] items, int maxItens, String separator) {
    StringBuilder buff = new StringBuilder();
    if (items != null) {
      if (items.length == items.getClass().getComponentType().getEnumConstants().length) {
        buff.append("*");
      } else {
        int count = 0;
        for (E v : items) {
          if (buff.length() > 0) buff.append(separator);
          if (count >= maxItens) {
            buff.append("+").append(items.length - maxItens);
            break;
          }
          buff.append(RFWBundle.get(v));
          count++;
        }
      }
    }
    return buff.toString();
  }

  /**
   * Este método obtem uma lista de Enumerations de valores, e escreve os mesmos em uma lista separadas por ",". <br>
   * Se o total de itens a serem escritos estrapolar o valor definido em maxItens o total restante é resumindo com a expressão "+N", onde N indica quantos itens foram suprimidos.<br>
   *
   * @param <E> Tipo da enumeration.
   * @param list Lista de Itens selecionados
   * @param maxItens Máximo de itens a serem escritos
   * @return
   */
  public static <E extends Enum<?>> String concatArrayIntoString(List<E> list, int maxItens) {
    return concatArrayIntoString(list, maxItens, ", ");
  }

  /**
   * Este método obtem uma lista de Enumerations de valores, e escreve os mesmos em uma lista separadas por ",". <br>
   * Se o total de itens a serem escritos estrapolar o valor definido em maxItens o total restante é resumindo com a expressão "+N", onde N indica quantos itens foram suprimidos.<br>
   *
   * @param <E> Tipo da enumeration.
   * @param list Lista de Itens selecionados
   * @param maxItens Máximo de itens a serem escritos
   * @param separator Define a String que será concatenada entre os elementos. Se utilizado ", " o resultado será o mesmo que o do método {@link #concatArrayIntoString(Enum[], int)}
   * @return
   */
  public static <E extends Enum<?>> String concatArrayIntoString(List<E> list, int maxItens, String separator) {
    StringBuilder buff = new StringBuilder();
    if (list != null) {
      int count = 0;
      for (E v : list) {
        if (buff.length() > 0) buff.append(separator);
        if (count >= maxItens) {
          buff.append("+").append(list.size() - maxItens);
          break;
        }
        buff.append(RFWBundle.get(v));
        count++;
      }
    }
    return buff.toString();
  }

  /**
   * Recebe um array de String e concatena seus valroes em uma única String.
   *
   * @param values Valores a serem concatenados
   * @param separator Separados a ser concatenado entre cada um dos valores.
   * @return String com seus valores concatenados
   */
  public static String concatArrayIntoString(String[] values, String separator) {
    return concatArrayIntoString(values, separator, Integer.MAX_VALUE);
  }

  /**
   * Recebe um array de Integer e concatena seus valroes em uma única String.
   *
   * @param values Valores a serem concatenados
   * @param separator Separados a ser concatenado entre cada um dos valores.
   * @return String com seus valores concatenados
   */
  public static String concatArrayIntoString(int[] values, String separator) {
    return concatArrayIntoString(values, separator, Integer.MAX_VALUE);
  }

  /**
   * Recebe um array de String e concatena seus valroes em uma única String. Utilizando o separador ", ".
   *
   * @param values Valores a serem concatenados
   * @param maxItens Máximo de itens a serem escritos
   * @return String com seus valores concatenados
   */
  public static String concatArrayIntoString(String[] values, int maxItens) {
    return concatArrayIntoString(values, ", ", maxItens);
  }

  /**
   * Este método obtem um array de Enumerations de valores, e escreve os mesmos em uma lista separadas por ",". <br>
   * Se o total de itens a serem escritos estrapolar o valor definido em maxItens o total restante é resumindo com a expressão "+N", onde N indica quantos itens foram suprimidos.<br>
   * Caso o total de items passado seja o total de itens disponíveis na enumeration retorna apenas o texto "*".
   *
   * @param <E> Tipo da enumeration.
   * @param items Lista de Itens selecionados
   * @param separator Define a String que será concatenada entre os elementos. Se utilizado ", " o resultado será o mesmo que o do método {@link #concatArrayIntoString(Enum[], int)}
   * @return
   */
  public static String concatArrayIntoString(String[] items, String separator, int maxItens) {
    StringBuilder buff = new StringBuilder();
    if (items != null) {
      int count = 0;
      for (String v : items) {
        if (buff.length() > 0) buff.append(separator);
        if (count >= maxItens) {
          buff.append("+").append(items.length - maxItens);
          break;
        }
        buff.append(v);
        count++;
      }
    }
    return buff.toString();
  }

  /**
   * Este método obtem um array de Enumerations de valores, e escreve os mesmos em uma lista separadas por ",". <br>
   * Se o total de itens a serem escritos estrapolar o valor definido em maxItens o total restante é resumindo com a expressão "+N", onde N indica quantos itens foram suprimidos.<br>
   * Caso o total de items passado seja o total de itens disponíveis na enumeration retorna apenas o texto "*".
   *
   * @param <E> Tipo da enumeration.
   * @param items Lista de Itens selecionados
   * @param separator Define a String que será concatenada entre os elementos. Se utilizado ", " o resultado será o mesmo que o do método {@link #concatArrayIntoString(Enum[], int)}
   * @return
   */
  public static String concatArrayIntoString(int[] items, String separator, int maxItens) {
    StringBuilder buff = new StringBuilder();
    if (items != null) {
      int count = 0;
      for (int v : items) {
        if (buff.length() > 0) buff.append(separator);
        if (count >= maxItens) {
          buff.append("+").append(items.length - maxItens);
          break;
        }
        buff.append(v);
        count++;
      }
    }
    return buff.toString();
  }

}
