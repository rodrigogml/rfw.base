package br.eng.rodrigogml.rfw.base.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import br.eng.rodrigogml.rfw.base.dao.annotations.dao.RFWDAO;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.vo.GVO;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.utils.RUReflex;
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
   * Recupera a classe de uma propriedade do bean passado.<br>
   * Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente. Neste caso, será usado o método "get" da propriedade para descobrir sua classe. <b>Este método usará sempre o tipo do objeto retornado pela instância. Logo, se a instância do objeto retornar null este método será incapaz de descobrir a classe da propriedade.</b>
   *
   * @param bean Object objeto o qual o metodo GET será chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Class objeto retornado pelo método get do atributo. Null caso algum algum método GET retorne null.
   */
  public static Class<?> getPropertyTypeByObject(Object bean, String propertyname) throws RFWException {
    if (bean == null) {
      throw new NullPointerException("O objeto bean recebido não pode ser nulo!");
    }
    try {
      Class<?> returned = null;
      int index = propertyname.indexOf(".");
      if (index > -1) {
        String firstproperty = propertyname.substring(0, index);
        Object tmpobj;
        try {
          tmpobj = bean.getClass().getMethod("get" + firstproperty.substring(0, 1).toUpperCase() + firstproperty.substring(1, firstproperty.length()), (Class[]) null).invoke(bean, new Object[0]);
        } catch (NoSuchMethodException e) {
          try {
            tmpobj = bean.getClass().getMethod("is" + firstproperty.substring(0, 1).toUpperCase() + firstproperty.substring(1, firstproperty.length()), (Class[]) null).invoke(bean, new Object[0]);
          } catch (NoSuchMethodException e2) {
            tmpobj = bean.getClass().getMethod("are" + firstproperty.substring(0, 1).toUpperCase() + firstproperty.substring(1, firstproperty.length()), (Class[]) null).invoke(bean, new Object[0]);
          }
        }
        if (tmpobj != null) {
          returned = getPropertyTypeByObject(tmpobj, propertyname.substring(index + 1, propertyname.length()));
        }
      } else {
        Object tmpobj;
        try {
          tmpobj = bean.getClass().getMethod("get" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).invoke(bean, new Object[0]);
        } catch (NoSuchMethodException e) {
          try {
            tmpobj = bean.getClass().getMethod("is" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).invoke(bean, new Object[0]);
          } catch (NoSuchMethodException e2) {
            tmpobj = bean.getClass().getMethod("are" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).invoke(bean, new Object[0]);
          }
        }
        if (tmpobj != null) {
          returned = tmpobj.getClass();
        }
      }
      return returned;
    } catch (Throwable e) {
      throw new RFWCriticalException("RFW_ERR_200480", e);
    }
  }

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
   * Este método cria um novo ClassLoader de URL com todos os arquivos JARs encontrados no classpath atual.
   *
   * @return ClassLoader com todos os arquivos .jar carregados.
   */
  public static URLClassLoader createURLClassLoader() {
    Collection<String> resources = getResources(Pattern.compile(".*\\.jar"));
    Collection<URL> urls = new ArrayList<>();
    for (String resource : resources) {
      File file = new File(resource);
      // Ensure that the JAR exists and is in the globalclasspath directory.
      if (file.isFile() && "globalclasspath".equals(file.getParentFile().getName())) {
        try {
          urls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          // This should never happen.
          e.printStackTrace();
        }
      }
    }
    return new URLClassLoader(urls.toArray(new URL[urls.size()]));
  }

  /**
   * Recupera todos os "resources" do ClassLoader atual baseados em um pattern.
   *
   * @param pattern Expressão Regularar para filtrar os "resources" que serão retornados.
   * @return Lista com o URL de todos os recursos encontrados.
   */
  public static Collection<String> getResources(final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    final String classPath = System.getProperty("java.class.path", ".");
    final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
    for (final String element : classPathElements) {
      retval.addAll(getResources(element, pattern));
    }
    return retval;
  }

  /**
   * Busca um "resource" no classPath de acordo com seu nome. Este método tenta encontrar primeiro no mesmo classPath da classe que chamou este método, se não encontrar procura no ClassLoader completo.<br>
   * <b>ATENÇÃO:</B> Este método não funciona para resources que estejam dentro de pacotes do tipo EJB/WAR, quando chamado de uma classe de fora. Isso porque o classloader deles é separado por segurança. Para ter os resources visíveis por este método coloque em um Jar "público", como o pacote client, ou um Jar específico para carregar os resources.
   *
   *
   * @param resourceName Nome do Recurso sendo procurado. O nome deve incluir a pasta conforme sua posição relativa à raiz. Ex: "resources/img.gif"
   * @return InputStream pronto para ser lido com o conteúdo do Resource.
   * @throws RFWException
   */
  public static InputStream getResourceAsStream(String resourceName) throws RFWException {
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    if (stream == null) {
      try {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        stream = Class.forName(stack[2].getClassName()).getClassLoader().getResourceAsStream(resourceName);
      } catch (ClassNotFoundException e1) {
        // Não deve ocorrer pe estamos criando a partir do próprio nome da classe, mas logamos
        RFWLogger.logException(e1);
      }
      if (stream == null) {
        final URL url = BUReflex.createURLClassLoader().getResource(resourceName);
        try {
          if (url != null) stream = url.openStream();
        } catch (IOException e) {
          throw new RFWCriticalException("Falha ao abrir o stream da URL do Resource encontrado!", e);
        }
      }
    }
    return stream;
  }

  /**
   * Recupera uma lista de "resources" baseado em um determinado elemento, filtrados por um pattern.
   *
   * @param element URI do Jar ou Diretório onde podemos encontrar os "resources".
   * @param pattern Expressão Regular para filtrar os recursos desejados.
   * @return Lista de URI com os "resources" encontrados.
   */
  private static Collection<String> getResources(final String element, final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    final File file = new File(element);
    if (file.isDirectory()) {
      retval.addAll(getResourcesFromDirectory(file, pattern));
    } else {
      retval.addAll(getResourcesFromJarFile(file, pattern));
    }
    return retval;
  }

  /**
   * Recupera os "resources" de um diretório
   *
   * @param directory Diretório para recuperar os "resources"
   * @param pattern Expressão Regulara para filtrar os "resources" retornados.
   * @return Lista com os "resources" encontrados
   */
  private static Collection<String> getResourcesFromDirectory(final File directory, final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    final File[] fileList = directory.listFiles();
    for (final File file : fileList) {
      if (file.isDirectory()) {
        retval.addAll(getResourcesFromDirectory(file, pattern));
      } else {
        try {
          final String fileName = file.getCanonicalPath();
          final boolean accept = pattern.matcher(fileName).matches();
          if (accept) {
            retval.add(fileName);
          }
        } catch (final IOException e) {
          throw new Error(e);
        }
      }
    }
    return retval;
  }

  /**
   * Recupera os "resources" de um arquivo JAR.
   *
   * @param file URI do arquivo JAR
   * @param pattern Expressão Regular para filtrar os resultados.
   * @return Lita de Resources encontrados.
   */
  private static Collection<String> getResourcesFromJarFile(final File file, final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    ZipFile zf;
    try {
      zf = new ZipFile(file);
    } catch (final ZipException e) {
      throw new Error(e);
    } catch (final IOException e) {
      throw new Error(e);
    }
    final Enumeration<?> e = zf.entries();
    while (e.hasMoreElements()) {
      final ZipEntry ze = (ZipEntry) e.nextElement();
      final String fileName = ze.getName();
      final boolean accept = pattern.matcher(fileName).matches();
      if (accept) {
        retval.add(fileName);
      }
    }
    try {
      zf.close();
    } catch (final IOException e1) {
      throw new Error(e1);
    }
    return retval;
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
