package br.eng.rodrigogml.rfw.base.utils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import br.eng.rodrigogml.rfw.base.dao.annotations.dao.RFWDAO;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.utils.RUReflex;
import br.eng.rodrigogml.rfw.kernel.vo.GVO;
import br.eng.rodrigogml.rfw.kernel.vo.RFWVO;

/**
 * Description: Classe com utilitários para tratar objetos apartir de reflexão.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.0.0 (SET / 2009)
 * @deprecated Movido para o RFW.Kernel na classe RUReflex
 */
@Deprecated
public class BUReflex {

  /**
   * Verifica se existe esta propriedade no Bean passado. Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente.
   *
   * @param bean Object objeto o qual o metodo GET será chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Class objeto retornado pelo método get do atributo.
   */
  public static boolean hasProperty(Class<?> objtype, String propertyname) {
    boolean returned = false;
    try {
      int index = propertyname.indexOf(".");
      if (index > -1) {
        String firstproperty = propertyname.substring(0, index);
        Class<?> tmpclass = null;
        try {
          tmpclass = objtype.getMethod("get" + firstproperty.substring(0, 1).toUpperCase() + firstproperty.substring(1, firstproperty.length()), (Class[]) null).getReturnType();
        } catch (NoSuchMethodException e) {
          try {
            tmpclass = objtype.getMethod("is" + firstproperty.substring(0, 1).toUpperCase() + firstproperty.substring(1, firstproperty.length()), (Class[]) null).getReturnType();
          } catch (NoSuchMethodException e2) {
            tmpclass = objtype.getMethod("are" + firstproperty.substring(0, 1).toUpperCase() + firstproperty.substring(1, firstproperty.length()), (Class[]) null).getReturnType();
          }
        }
        returned = hasProperty(tmpclass, propertyname.substring(index + 1, propertyname.length()));
      } else {
        Method method = null;
        try {
          method = objtype.getMethod("get" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null);
        } catch (NoSuchMethodException e) {
          try {
            method = objtype.getMethod("is" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null);
          } catch (NoSuchMethodException e2) {
            method = objtype.getMethod("are" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null);
          }
        }
        returned = (method != null);
      }
    } catch (NoSuchMethodException e) {
      returned = false;
    }
    return returned;
  }

  /**
   * Este método recebe um array de caminhos a serem recuperados (no padrão do MO do Framework) e filtra apenas os caminhos a partir de um determinado ponto.<br>
   * Exemplo, imagine um objeto:<br>
   * <li>vo
   * <ul>
   * <li>voA
   * <ul>
   * <li>voAA
   * </ul>
   * <ul>
   * <li>voAB
   * </ul>
   * </ul>
   * <ul>
   * <li>voB
   * <ul>
   * <li>voBA
   * </ul>
   * <ul>
   * <li>voBB
   * </ul>
   * </ul>
   * <br>
   * <br>
   *
   * Agora imagine que tenha um array de caminhos para expandir todo o objeto 'vo' e todos os seus filhos:<br>
   * <ul>
   * new String[] { "voA.voAA", "voA.voAB", "voB.voBA", "voB.voBB" };
   * </ul>
   *
   * e que queremos carregar apenas o voB para substituir no objeto principal. Mas como queremos buscar diretamente o objeto voB. Neste caso o array de "exploit" não servirá e precisa ser convertido para:<br>
   * <ul>
   * new String[] { "voBA", "voBB" };
   * </ul>
   *
   * Filtrando as entradas que não pertencem ao trecho do objeto desejado, e adaptando o caminho para partir apenas do novo objeto.
   *
   * @param tree Array com os caminhos a partir do objeto principal.
   * @param base Caminho base a partir de onde queremos o fazer a busca. No exemplo citado acima seria "voB".
   * @return Retorna a lista de caminhos a partir do ponto desejado, adaptadaos para o trecho em diante. Retorna null caso tenha nenhum resultado no final.
   * @throws RFWException
   */
  public static String[] getSubTree(String[] tree, String base) throws RFWException {
    final ArrayList<String> finalList = new ArrayList<>();

    base = base + '.'; // Faz com que a base termine com '.' por dois motivos: primeiro evita filtrar errado caso o último atributo da base coincida com o começo de outro atributo na árvore. Segundo evita de incluir no filtro quando o caminho da árvore é exatamente o da base, caso contrário teriamos um valor "" adicionado.

    for (int i = 0; i < tree.length; i++) {
      if (tree[i].startsWith(base)) {
        finalList.add(tree[i].substring(base.length()));
      }
    }

    if (finalList.size() == 0) {
      return null;
    } else {
      return finalList.<String> toArray(new String[0]);
    }
  }

  /**
   * Permite que uma determinada árvore seja adicionada como parte de outra, adicionando a cada elemento do array uma base inicial da árvore pai.
   *
   * @param baseprefix
   * @param values
   * @return
   */
  public static String[] addBasePrefix(String baseprefix, String[] values) {
    String[] newvalues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      newvalues[i] = baseprefix + '.' + values[i];
    }
    return newvalues;
  }

  /**
   * Este método retorna uma instãncia de uma determinada classe. A classe deve estar disponível no ClassLoader para que seja possível recupera-la.
   *
   * @param className "CanonicalName" da classe desejada.
   * @return Instância da classe desejada.
   * @throws RFWException Lançado caso qualquer falha ocorra.
   */
  public static Object getClassInstance(String className) throws RFWException {
    try {
      Class<?> c = Class.forName(className);
      if (c == null) throw new RFWCriticalException("RFW_ERR_200473", new String[] { className });
      return c.newInstance();
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200473", new String[] { className }, e);
    }
  }

  /**
   * Compra dois Objectos (RFWVO) e verifica os valores diferentes recursivamente.
   *
   * @param obj1 Objeto 1 para ser comparado
   * @param obj2 Objeto 2 para ser comparado
   * @return Lista com o caminho das propriedades que apresentaram diferenças.
   * @throws RFWException
   */
  public static <T extends Object> List<String> compareRecursively(T obj1, T obj2) throws RFWException {
    return compareRecursively(obj1, obj2, null, new LinkedList<Object>());
  }

  /**
   * Método auxiliar do {@link #compareRecursively(RFWVO, RFWVO)}
   *
   * @param basepath Caminho dos atributos (recursivo) até chegarem nesta comparação. Passar nulo case sejam os dois objetos raiz.
   * @param cache Lista com os objetos que já foram comparados, evitando assim loop infinito em caso de link cíclico dos objetos. Para o objeto raiz passar uma lista vazia.tá nada
   * @throws RFWException
   */
  private static <T extends Object> List<String> compareRecursively(T obj1, T obj2, String basepath, List<Object> cache) throws RFWException {
    LinkedList<String> atts = new LinkedList<String>();

    if (obj1 == null ^ obj2 == null) {
      atts.add(RUReflex.getAttributePath("", basepath));
    } else if (obj1 != null && obj2 != null) {
      if (!cache.contains(obj1)) {
        cache.add(obj1);
        if (obj1.getClass().isPrimitive() ||
            String.class.isInstance(obj1) ||
            Long.class.isInstance(obj1) ||
            Integer.class.isInstance(obj1) ||
            Boolean.class.isInstance(obj1) ||
            Double.class.isInstance(obj1) ||
            Float.class.isInstance(obj1) ||
            Enum.class.isInstance(obj1) ||
            Date.class.isInstance(obj1) ||
            LocalDate.class.isInstance(obj1) ||
            LocalDateTime.class.isInstance(obj1) ||
            BigDecimal.class.isInstance(obj1) ||
            Class.class.isInstance(obj1)) {
          if (!obj1.equals(obj2)) {
            atts.add(RUReflex.getAttributePath("", basepath));
          }
        } else if (Iterable.class.isInstance(obj1)) {
          Iterator<?> it1 = ((Iterable<?>) obj1).iterator();
          Iterator<?> it2 = ((Iterable<?>) obj2).iterator();
          int index = 0;
          while (true) {
            if (!it1.hasNext() && !it2.hasNext()) {
              break;
            } else if (it1.hasNext() ^ it2.hasNext()) {
              atts.add(RUReflex.getAttributePath("", basepath));
              break;
            } else {
              Object itObj1 = it1.next();
              Object itObj2 = it2.next();
              atts.addAll(compareRecursively(itObj1, itObj2, RUReflex.getAttributePath("", index, basepath), cache));
            }
            index++;
          }
        } else if (Map.class.isInstance(obj1)) {
          Map<?, ?> map1 = (Map<?, ?>) obj1;
          Map<?, ?> map2 = (Map<?, ?>) obj2;
          if (map1.size() != map2.size()) {
            atts.add(RUReflex.getAttributePath("", basepath));
          } else {
            for (Object key : map1.keySet()) {
              Object mapObj1 = map1.get(key);
              Object mapObj2 = map2.get(key);
              if (mapObj1 == null ^ mapObj2 == null) {
                atts.add(RUReflex.getAttributePath("", key, key.getClass(), basepath));
              } else if (mapObj1 != null && mapObj2 != null) {
                atts.addAll(compareRecursively(mapObj1, mapObj2, RUReflex.getAttributePath("", key, key.getClass(), basepath), cache));
              }
            }
          }
        } else if (RFWVO.class.isAssignableFrom(obj1.getClass())) {
          Method[] methods = obj1.getClass().getMethods();
          for (Method method : methods) {
            if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && !method.getName().equals("getClass")) {
              try {
                Object ret1 = method.invoke(obj1);
                Object ret2 = method.invoke(obj2);
                String attribute = null;
                if (method.getName().startsWith("get")) {
                  attribute = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                } else if (method.getName().startsWith("is")) {
                  attribute = method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
                }
                atts.addAll(compareRecursively(ret1, ret2, RUReflex.getAttributePath(attribute, basepath), cache));
              } catch (Exception e) {
                throw new RFWCriticalException("Falha ao comparar semelhança dos objetos!", e);
              }
            }
          }
        } else {
          throw new RFWCriticalException("Método despreparado para comparar o objeto '" + obj1.getClass().getCanonicalName() + "'.");
        }
      }
    }
    return atts;
  }

  /**
   * Transforma uma Coleção de VOs em um Array contendo todos os IDs dos vos recebidos.
   *
   * @param <VO> Qualquer classe de VO do RFWDeprec (extenda RFWVO).
   * @param vos Array com os VOs
   * @return Array contendo todos os IDs dos VOs recebidos.
   * @throws RFWException
   */
  public static <VO extends RFWVO> Long[] collectVOIDsToArray(Collection<VO> vos) throws RFWException {
    return vos.stream().map(VO::getId).collect(Collectors.toList()).toArray(new Long[0]);
  }

  /**
   * Transforma uma Coleção de GVO em um Array contendo todos os IDs dos vos recebidos.
   *
   * @param <VO> Qualquer classe de VO do RFWDeprec (extenda RFWVO).
   * @param vos Array com os VOs
   * @return Array contendo todos os IDs dos VOs recebidos.
   * @throws RFWException
   */
  public static <VO extends RFWVO> Long[] collectGVOIDsToArray(Collection<GVO<VO>> vos) throws RFWException {
    return vos.stream().map(GVO::getId).collect(Collectors.toList()).toArray(new Long[0]);
  }

  /**
   * Transforma uma Coleção de GVO em uma Lista contendo todos os IDs dos vos recebidos.
   *
   * @param <VO> Qualquer classe de VO do RFWDeprec (extenda RFWVO).
   * @param vos Array com os VOs
   * @return Lista contendo todos os IDs dos VOs recebidos.
   * @throws RFWException
   */
  public static <VO extends RFWVO> List<Long> collectGVOIDsToList(Collection<GVO<VO>> vos) throws RFWException {
    return vos.stream().map(GVO::getId).collect(Collectors.toList());
  }

  /**
   * Transforma um Array de VOs em um Array contendo todos os IDs dos vos recebidos.
   *
   * @param <VO> Qualquer classe de VO do RFWDeprec (extenda RFWVO).
   * @param vos Array com os VOs
   * @return Array contendo todos os IDs dos VOs recebidos.
   * @throws RFWException
   */
  public static <VO extends RFWVO> Long[] collectVOIDsToArray(VO[] vos) throws RFWException {
    return Arrays.asList(vos).stream().map(VO::getId).collect(Collectors.toList()).toArray(new Long[0]);
  }

  /**
   * Transforma um Array de VOs em uma Lista contendo todos os IDs dos vos recebidos.
   *
   * @param <VO> Qualquer classe de VO do RFWDeprec (extenda RFWVO).
   * @param vos Array com os VOs
   * @return Lista contendo todos os IDs dos VOs recebidos.
   * @throws RFWException
   */
  public static <VO extends RFWVO> List<Long> collectVOIDsToList(VO[] vos) throws RFWException {
    return Arrays.asList(vos).stream().map(VO::getId).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Obtem Annotation RFWDAO da entidade.
   *
   * @return Annotation RFWDAO da entidade
   */
  public static RFWDAO getRFWDAOAnnotation(Class<? extends RFWVO> type) throws RFWException {
    final RFWDAO ann = type.getAnnotation(RFWDAO.class);
    if (ann == null) {
      throw new RFWCriticalException("A entidade '${0}' não possui a Annotation RFWDAO, e não pode ser interpretada.", new String[] { type.getCanonicalName() });
    }
    return ann;
  }

  /**
   * Método auxiliar para obter o "caminho pai".<br>
   *
   * @param path Caminho para extrair o caminho pai.
   * @return Retorna o caminho pai. Ex:
   *         <li>Se passado "a.b.c.d.e" este método deve retornar "a.b.c.d".
   *         <li>Se passado "a" o método deve retornar "" já que vazio é caminho raiz.
   *         <li>Se passado "" retornamos null já que não temos um caminho pai.
   */
  public static String getParentPath(String path) {
    if (path == null || "".equals(path)) {
      return null;
    }
    int p = path.lastIndexOf(".");
    if (p == -1) {
      return "";
    } else {
      return path.substring(0, p);
    }
  }

  /**
   * Método auxiliar para obter o "último atributo do caminho".<br>
   *
   * @param path Caminho para extrair o último bloco.
   * @return Retorna o caminho pai. Ex:
   *         <li>Se passado "a.b.c.d.e" este método deve retornar "e".
   *         <li>Se passado "a" o método deve retornar "a".
   *         <li>Se passado null ou "" retornamos null já que não temos nenhum atributo.
   */
  public static String getLastPath(String path) {
    if (path == null || "".equals(path)) {
      return null;
    }
    int p = path.lastIndexOf(".");
    if (p == -1) {
      return path;
    } else {
      return path.substring(p + 1, path.length());
    }
  }

  /**
   * Filtra uma lista conforme a definição do predicate.
   *
   * @param <E> Tipo do objeto dentro do da lista
   * @param list Lista com os objetos para serem filtrados
   * @param predicate Implementação do predicato para filtro. Só permite os itens cujo retorno seja "true".<br>
   *          Ex: "e -> e.isEnabled()"
   * @return Lista com os objetos que retornaram true no predicate.
   */
  public static <E extends Object> List<E> filterCollection(List<E> list, Predicate<? super E> predicate) {
    return list.stream().filter(predicate).collect(Collectors.toList());
  }

  /**
   * Este método copia "todos" os atributos de um VO para outro. Com exceção dos atributos declarados diretamente no RFWVO.<br>
   * Atualmente são eles (podem aparecer mais no futuro): {@link RFWVO#getId()}, {@link RFWVO#isFullLoaded()} e {@link RFWVO#isInsertWithID()}.<br>
   * <br>
   * Este método só copia dados/classes imutáveis do Java. Como valores primitivos e suas classes equivalentes. Não "adentra" arrays, listas, hashs, maps, etc., nem copia outros objetos que não sejam imutáveis, inclusive outros RFWVO.<br>
   * Valores nulos serão copiados.<br>
   * Para que a cópia ocorra o objeto deve ser um método "set" para a escrita, e um método "get", "is" ou "has" para leitura. Atributos que não tenham ambos os métodos serão ignorados.
   *
   * @param <VO> VO filho de {@link RFWVO}
   * @param sourceVO objeto que servirá as informações a serem copiadas.
   * @param destinationVO objeto em que as informações serão escritas.
   * @throws RFWException
   */
  public static <VO extends RFWVO> void copyVOs(VO sourceVO, VO destinationVO) throws RFWException {
    Method[] methods = sourceVO.getClass().getMethods();
    for (Method setM : methods) {
      // Verifica se é um método de "Set"
      if (setM.getName().startsWith("set") && !setM.getName().equals("setId") && !setM.getName().equals("setInsertWithID") && !setM.getName().equals("setFullLoaded")) {
        // Procura o método get equivalente
        Method getM = null;
        try {
          getM = sourceVO.getClass().getMethod("get" + setM.getName().substring(3));
        } catch (NoSuchMethodException e) {
          try {
            getM = sourceVO.getClass().getMethod("is" + setM.getName().substring(3));
          } catch (NoSuchMethodException e1) {
            try {
              getM = sourceVO.getClass().getMethod("has" + setM.getName().substring(3));
            } catch (NoSuchMethodException e2) {
            }
          }
        }

        if (getM != null) { // Se não encontrou um método get ou equivalente, simplesmente ignora a cópia desse atributo
          try {
            Object value = getM.invoke(sourceVO);
            if (value == null
                || value.getClass().isPrimitive()
                || value.getClass().isEnum()
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Double
                || value instanceof Float
                || value instanceof BigDecimal
                || value instanceof Date
                || value instanceof LocalDate
                || value instanceof LocalDateTime
                || value instanceof LocalTime
                || value instanceof String
                || value instanceof Boolean) {
              setM.invoke(destinationVO, value);
            } else {
              if (!RFWVO.class.isAssignableFrom(value.getClass())
                  && !List.class.isAssignableFrom(value.getClass())) {
                System.out.println("Classe não copiada: " + value.getClass().getCanonicalName());
              }
            }
          } catch (Throwable e) {
            throw new RFWCriticalException("Falha ao copiar o valor do método '" + getM.getName() + "'!", e);
          }
        }
      }
    }
  }

}
