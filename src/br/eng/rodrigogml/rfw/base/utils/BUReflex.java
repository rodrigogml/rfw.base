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
 * Description: Classe com utilit�rios para tratar objetos apartir de reflex�o.<br>
 *
 * @author Rodrigo Leit�o
 * @since 3.0.0 (SET / 2009)
 * @deprecated Movido para o RFW.Kernel na classe RUReflex
 */
@Deprecated
public class BUReflex {

  /**
   * Recupera a classe de uma propriedade do bean passado.<br>
   * Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente. Neste caso, ser� usado o m�todo "get" da propriedade para descobrir sua classe. <b>Este m�todo usar� sempre o tipo do objeto retornado pela inst�ncia. Logo, se a inst�ncia do objeto retornar null este m�todo ser� incapaz de descobrir a classe da propriedade.</b>
   *
   * @param bean Object objeto o qual o metodo GET ser� chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Class objeto retornado pelo m�todo get do atributo. Null caso algum algum m�todo GET retorne null.
   */
  public static Class<?> getPropertyTypeByObject(Object bean, String propertyname) throws RFWException {
    if (bean == null) {
      throw new NullPointerException("O objeto bean recebido n�o pode ser nulo!");
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
   * @param bean Object objeto o qual o metodo GET ser� chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Class objeto retornado pelo m�todo get do atributo.
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
   * Este m�todo recebe um array de caminhos a serem recuperados (no padr�o do MO do Framework) e filtra apenas os caminhos a partir de um determinado ponto.<br>
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
   * e que queremos carregar apenas o voB para substituir no objeto principal. Mas como queremos buscar diretamente o objeto voB. Neste caso o array de "exploit" n�o servir� e precisa ser convertido para:<br>
   * <ul>
   * new String[] { "voBA", "voBB" };
   * </ul>
   *
   * Filtrando as entradas que n�o pertencem ao trecho do objeto desejado, e adaptando o caminho para partir apenas do novo objeto.
   *
   * @param tree Array com os caminhos a partir do objeto principal.
   * @param base Caminho base a partir de onde queremos o fazer a busca. No exemplo citado acima seria "voB".
   * @return Retorna a lista de caminhos a partir do ponto desejado, adaptadaos para o trecho em diante. Retorna null caso tenha nenhum resultado no final.
   * @throws RFWException
   */
  public static String[] getSubTree(String[] tree, String base) throws RFWException {
    final ArrayList<String> finalList = new ArrayList<>();

    base = base + '.'; // Faz com que a base termine com '.' por dois motivos: primeiro evita filtrar errado caso o �ltimo atributo da base coincida com o come�o de outro atributo na �rvore. Segundo evita de incluir no filtro quando o caminho da �rvore � exatamente o da base, caso contr�rio teriamos um valor "" adicionado.

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
   * Permite que uma determinada �rvore seja adicionada como parte de outra, adicionando a cada elemento do array uma base inicial da �rvore pai.
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
   * Este m�todo retorna uma inst�ncia de uma determinada classe. A classe deve estar dispon�vel no ClassLoader para que seja poss�vel recupera-la.
   *
   * @param className "CanonicalName" da classe desejada.
   * @return Inst�ncia da classe desejada.
   * @throws RFWException Lan�ado caso qualquer falha ocorra.
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
   * Este m�todo cria um novo ClassLoader de URL com todos os arquivos JARs encontrados no classpath atual.
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
   * @param pattern Express�o Regularar para filtrar os "resources" que ser�o retornados.
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
   * Busca um "resource" no classPath de acordo com seu nome. Este m�todo tenta encontrar primeiro no mesmo classPath da classe que chamou este m�todo, se n�o encontrar procura no ClassLoader completo.<br>
   * <b>ATEN��O:</B> Este m�todo n�o funciona para resources que estejam dentro de pacotes do tipo EJB/WAR, quando chamado de uma classe de fora. Isso porque o classloader deles � separado por seguran�a. Para ter os resources vis�veis por este m�todo coloque em um Jar "p�blico", como o pacote client, ou um Jar espec�fico para carregar os resources.
   *
   *
   * @param resourceName Nome do Recurso sendo procurado. O nome deve incluir a pasta conforme sua posi��o relativa � raiz. Ex: "resources/img.gif"
   * @return InputStream pronto para ser lido com o conte�do do Resource.
   * @throws RFWException
   */
  public static InputStream getResourceAsStream(String resourceName) throws RFWException {
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    if (stream == null) {
      try {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        stream = Class.forName(stack[2].getClassName()).getClassLoader().getResourceAsStream(resourceName);
      } catch (ClassNotFoundException e1) {
        // N�o deve ocorrer pe estamos criando a partir do pr�prio nome da classe, mas logamos
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
   * @param element URI do Jar ou Diret�rio onde podemos encontrar os "resources".
   * @param pattern Express�o Regular para filtrar os recursos desejados.
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
   * Recupera os "resources" de um diret�rio
   *
   * @param directory Diret�rio para recuperar os "resources"
   * @param pattern Express�o Regulara para filtrar os "resources" retornados.
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
   * @param pattern Express�o Regular para filtrar os resultados.
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
   * @return Lista com o caminho das propriedades que apresentaram diferen�as.
   * @throws RFWException
   */
  public static <T extends Object> List<String> compareRecursively(T obj1, T obj2) throws RFWException {
    return compareRecursively(obj1, obj2, null, new LinkedList<Object>());
  }

  /**
   * M�todo auxiliar do {@link #compareRecursively(RFWVO, RFWVO)}
   *
   * @param basepath Caminho dos atributos (recursivo) at� chegarem nesta compara��o. Passar nulo case sejam os dois objetos raiz.
   * @param cache Lista com os objetos que j� foram comparados, evitando assim loop infinito em caso de link c�clico dos objetos. Para o objeto raiz passar uma lista vazia.t� nada
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
                throw new RFWCriticalException("Falha ao comparar semelhan�a dos objetos!", e);
              }
            }
          }
        } else {
          throw new RFWCriticalException("M�todo despreparado para comparar o objeto '" + obj1.getClass().getCanonicalName() + "'.");
        }
      }
    }
    return atts;
  }

  /**
   * Transforma uma Cole��o de VOs em um Array contendo todos os IDs dos vos recebidos.
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
   * Transforma uma Cole��o de GVO em um Array contendo todos os IDs dos vos recebidos.
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
   * Transforma uma Cole��o de GVO em uma Lista contendo todos os IDs dos vos recebidos.
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
      throw new RFWCriticalException("A entidade '${0}' n�o possui a Annotation RFWDAO, e n�o pode ser interpretada.", new String[] { type.getCanonicalName() });
    }
    return ann;
  }

  /**
   * M�todo auxiliar para obter o "caminho pai".<br>
   *
   * @param path Caminho para extrair o caminho pai.
   * @return Retorna o caminho pai. Ex:
   *         <li>Se passado "a.b.c.d.e" este m�todo deve retornar "a.b.c.d".
   *         <li>Se passado "a" o m�todo deve retornar "" j� que vazio � caminho raiz.
   *         <li>Se passado "" retornamos null j� que n�o temos um caminho pai.
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
   * M�todo auxiliar para obter o "�ltimo atributo do caminho".<br>
   *
   * @param path Caminho para extrair o �ltimo bloco.
   * @return Retorna o caminho pai. Ex:
   *         <li>Se passado "a.b.c.d.e" este m�todo deve retornar "e".
   *         <li>Se passado "a" o m�todo deve retornar "a".
   *         <li>Se passado null ou "" retornamos null j� que n�o temos nenhum atributo.
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
   * Filtra uma lista conforme a defini��o do predicate.
   *
   * @param <E> Tipo do objeto dentro do da lista
   * @param list Lista com os objetos para serem filtrados
   * @param predicate Implementa��o do predicato para filtro. S� permite os itens cujo retorno seja "true".<br>
   *          Ex: "e -> e.isEnabled()"
   * @return Lista com os objetos que retornaram true no predicate.
   */
  public static <E extends Object> List<E> filterCollection(List<E> list, Predicate<? super E> predicate) {
    return list.stream().filter(predicate).collect(Collectors.toList());
  }

  /**
   * Este m�todo copia "todos" os atributos de um VO para outro. Com exce��o dos atributos declarados diretamente no RFWVO.<br>
   * Atualmente s�o eles (podem aparecer mais no futuro): {@link RFWVO#getId()}, {@link RFWVO#isFullLoaded()} e {@link RFWVO#isInsertWithID()}.<br>
   * <br>
   * Este m�todo s� copia dados/classes imut�veis do Java. Como valores primitivos e suas classes equivalentes. N�o "adentra" arrays, listas, hashs, maps, etc., nem copia outros objetos que n�o sejam imut�veis, inclusive outros RFWVO.<br>
   * Valores nulos ser�o copiados.<br>
   * Para que a c�pia ocorra o objeto deve ser um m�todo "set" para a escrita, e um m�todo "get", "is" ou "has" para leitura. Atributos que n�o tenham ambos os m�todos ser�o ignorados.
   *
   * @param <VO> VO filho de {@link RFWVO}
   * @param sourceVO objeto que servir� as informa��es a serem copiadas.
   * @param destinationVO objeto em que as informa��es ser�o escritas.
   * @throws RFWException
   */
  public static <VO extends RFWVO> void copyVOs(VO sourceVO, VO destinationVO) throws RFWException {
    Method[] methods = sourceVO.getClass().getMethods();
    for (Method setM : methods) {
      // Verifica se � um m�todo de "Set"
      if (setM.getName().startsWith("set") && !setM.getName().equals("setId") && !setM.getName().equals("setInsertWithID") && !setM.getName().equals("setFullLoaded")) {
        // Procura o m�todo get equivalente
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

        if (getM != null) { // Se n�o encontrou um m�todo get ou equivalente, simplesmente ignora a c�pia desse atributo
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
                System.out.println("Classe n�o copiada: " + value.getClass().getCanonicalName());
              }
            }
          } catch (Throwable e) {
            throw new RFWCriticalException("Falha ao copiar o valor do m�todo '" + getM.getName() + "'!", e);
          }
        }
      }
    }
  }

}
