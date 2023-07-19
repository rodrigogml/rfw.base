package br.eng.rodrigogml.rfw.base.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import br.eng.rodrigogml.rfw.base.dao.annotations.dao.RFWDAO;
import br.eng.rodrigogml.rfw.base.dao.annotations.dao.RFWDAOEncrypt;
import br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta.RFWMetaCollectionField;
import br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta.RFWMetaRelationshipField;
import br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta.RFWMetaStringField;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.preprocess.PreProcess.PreProcessOption;
import br.eng.rodrigogml.rfw.base.vo.GVO;
import br.eng.rodrigogml.rfw.base.vo.RFWMO;
import br.eng.rodrigogml.rfw.base.vo.RFWVO;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWRunTimeException;

/**
 * Description: Classe com utilitários para tratar objetos apartir de reflexão.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.0.0 (SET / 2009)
 */
public class BUReflex {

  /**
   * Imprime os valores de um objeto simulando uma estrutura XML.<br>
   * Recupera os métodos Get para imprimir os objetos.
   *
   * @throws RFWCriticalException
   */
  public static String printObject(Object obj) {
    StringBuilder buff = new StringBuilder();
    if (obj == null) {
      buff.append("<NULL>");
    } else {
      final Class<?> objclass = obj.getClass();
      if (obj instanceof RFWVO) {
        ((RFWVO) obj).printMySelf(buff, new ArrayList<>());
      } else if (obj instanceof RFWMO) {
        buff.append(((RFWMO) obj).printConditions());
      } else {
        buff.append("[Classe do Objeto: " + objclass.getCanonicalName() + "]\n");
        BUReflex.printObject(obj, objclass, buff, new ArrayList<>());
      }
    }
    return buff.toString();
  }

  public static void printObject(Object obj, Class<?> objclass, StringBuilder buff, List<Object> printedobjects) {
    if (obj != null) {

      if (printedobjects.contains(obj)) {
        buff.append("Objeto já impresso! ").append(objclass.getName()).append(" hashCode: \"").append(obj.hashCode()).append("\"");
      } else {
        if (obj.getClass().isPrimitive()) {
          buff.append(obj);
        } else if (String.class.isInstance(obj)) {
          buff.append(obj);
        } else if (Long.class.isInstance(obj)) {
          buff.append(obj);
        } else if (Integer.class.isInstance(obj)) {
          buff.append(obj);
        } else if (Boolean.class.isInstance(obj)) {
          buff.append(obj);
        } else if (Double.class.isInstance(obj)) {
          buff.append(obj);
        } else if (Float.class.isInstance(obj)) {
          buff.append(obj);
        } else if (Enum.class.isInstance(obj)) {
          buff.append(obj);
        } else if (obj instanceof LocalDate) {
          buff.append(obj);
        } else if (Date.class.isInstance(obj)) {
          buff.append(obj);
        } else if (BigDecimal.class.isInstance(obj)) {
          buff.append(obj.toString());
        } else if (Class.class.isInstance(obj)) {
          buff.append(((Class<?>) obj).getName());
        } else if (Iterable.class.isInstance(obj)) {
          for (Object item : (Iterable<?>) obj) {
            buff.append("\n<iterableitem>");
            printObject(item, item.getClass(), buff, printedobjects);
            buff.append("</iterableitem>");
          }
        } else if (Map.class.isInstance(obj)) {
          Map<?, ?> map = (Map<?, ?>) obj;
          for (Object key : map.keySet()) {
            buff.append("\n<mapitem>");
            buff.append("\n<key>");
            printObject(key, Object.class, buff, printedobjects);
            buff.append("</key>");
            buff.append("\n<value>");
            printObject(map.get(key), Object.class, buff, printedobjects);
            buff.append("</value>");
            buff.append("</mapitem>");
          }
        } else {
          printedobjects.add(obj);
          buff.append("\n<").append(objclass.getName()).append(" hashCode=\"").append(obj.hashCode()).append("\">");
          // Itera os métodos do Objeto
          Method[] methods = objclass.getMethods();
          for (Method method : methods) {
            // Verifica se é um método de "get" ou "is" para pegar o valor e imprimir
            if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && !method.getName().equals("getClass")) {
              buff.append("\n<").append(method.getName()).append(">");
              // Recupera o retorno do método
              try {
                Object ret = method.invoke(obj);
                if (ret != null) {
                  if (ret instanceof RFWVO) {
                    ((RFWVO) ret).printMySelf(buff, printedobjects);
                  } else {
                    printObject(ret, method.getReturnType(), buff, printedobjects);
                  }
                }
              } catch (Exception e) {
                buff.append("Exception: '").append(e.getMessage()).append("'");
              }
              buff.append("</").append(method.getName()).append(">");
            }
          }
          buff.append("\n</").append(objclass.getName()).append(">");
        }
      }
    }
  }

  /**
   * Recupera o valor de uma propriedade do bean passado. Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente.
   *
   * @param bean Object objeto o qual o metodo GET será chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Object objeto retornado pelo método get do atributo.
   *
   */
  public static Object getPropertyValue(Object bean, String propertyname) throws RFWException {
    try {
      Object returned = null;
      int index = propertyname.indexOf(".");
      if (index > -1) {
        String firstproperty = propertyname.substring(0, index);
        Object tmpobj = getPropertyValue(bean, firstproperty);
        if (tmpobj != null) returned = getPropertyValue(tmpobj, propertyname.substring(index + 1, propertyname.length()));
      } else {
        // Verifica se temos índice/chave, se tivermos mas é a última propriedade tornamos o objeto dentro da lista/hash
        int indexkey = propertyname.indexOf("[");
        String key;
        if (indexkey > -1) {
          key = propertyname.substring(indexkey + 1, propertyname.length() - 1);
          propertyname = propertyname.substring(0, indexkey);
          Object tmpobj = getPropertyValue(bean, propertyname); // Recupera a Hash/Lista deste atributo
          if (tmpobj != null) {
            if (tmpobj instanceof List<?>) {
              List<?> list = (List<?>) tmpobj;
              // Se é uma lista validamos que o index seja numérico
              if (key == null || !key.matches("[0-9]*")) {
                throw new RFWCriticalException("RFW_ERR_200395", new String[] { propertyname });
              }
              int listindex = Integer.parseInt(key);
              if (listindex >= list.size()) {
                throw new RFWCriticalException("RFW_ERR_200396", new String[] { key, propertyname });
              }
              tmpobj = list.get(listindex);
            } else if (tmpobj instanceof Map<?, ?>) {
              // Extrai o tipo de classe da chave
              int classIndex = key.indexOf('{');
              String clazz = null;
              if (classIndex > -1) {
                clazz = key.substring(classIndex + 1, key.length() - 1);
                key = key.substring(0, classIndex);
              }
              // Com a classe verificamos se é algum tipo conhecido
              Object hashKey = null;
              if ("String".equals(clazz)) {
                hashKey = key;
              } else if ("Long".equals(clazz)) {
                hashKey = Long.parseLong(key);
              }
              if (key == null) {
                throw new RFWCriticalException("RFW_ERR_200397", new String[] { propertyname, key });
              }
              tmpobj = ((Map<?, ?>) tmpobj).get(hashKey);
            }
            returned = tmpobj;
          }
        } else {
          try {
            returned = bean.getClass().getMethod("get" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).invoke(bean, (Object[]) null);
          } catch (NoSuchMethodException e) {
            try {
              returned = bean.getClass().getMethod("is" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).invoke(bean, (Object[]) null);
            } catch (NoSuchMethodException e2) {
              returned = bean.getClass().getMethod("are" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).invoke(bean, (Object[]) null);
            }
          }
        }
      }
      return returned;
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200067", new String[] { propertyname }, e);
    }
  }

  /**
   * Define o valor de uma propriedade em um bean. Para recuperar o método de set, procuramos um que receba um objeto da mesma classe que é retornado pelo get.<Br>
   * Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente até encontrar a propriedade a definir.
   *
   * @param bean Object objeto o qual o metodo SET será chamado
   * @param propertyname String nome da propriedade que deseja-se definir o valor
   * @param instantiatenullobjs tenta instanciar objetos que sejam nulos no caminho para definir a propriedade. Para que esta função funcione, os objetos devem ter um construtor sem argumentos.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void setPropertyValue(Object bean, String propertyname, Object value, boolean instantiatenullobjs) throws RFWException {
    try {
      if (bean == null) {
        throw new NullPointerException("O objeto bean recebido não pode ser nulo!");
      }
      int index = propertyname.indexOf(".");
      if (index > -1) {
        String firstproperty = propertyname.substring(0, index);
        Object tmpobj = getPropertyValue(bean, firstproperty);

        // Verifica se a propriedade tem colchetes com a chave/index de coleções e os separa
        int indexkey = firstproperty.indexOf("[");
        // Verifica se o tmpobj é nulo, e se devemos tentar criar o novo objeto
        if (tmpobj == null) {
          if (!instantiatenullobjs) {
            throw new NullPointerException("Foi encontrado um objeto nulo ao tentar reflexão para definir o valor de uma propriedade!");
          } else {
            // Se temos índices ou chaves nem tentamos criar, não temos como criar uma hash ou lista adequadamente
            if (indexkey > -1) throw new RFWCriticalException("RFW_ERR_200398", new String[] { propertyname });
            Class<?> tmpclass = getPropertyTypeByType(bean.getClass(), firstproperty); // Basta procurar pelo type de retorno, pois já sabemos que o objeto é nulo!
            Constructor<?> constructor = tmpclass.getConstructor();
            if (constructor == null) {
              throw new NullPointerException("Não foi possível encontrar um construtor sem argumentos para o objeto: " + tmpclass.toString());
            }
            tmpobj = constructor.newInstance();
            setPropertyValue(bean, firstproperty, tmpobj, instantiatenullobjs);
          }
        }
        setPropertyValue(tmpobj, propertyname.substring(index + 1, propertyname.length()), value, instantiatenullobjs);
      } else {
        String key = null;
        // Verifica se a propriedade tem colchetes com a chave/index de coleções e os separa
        int indexkey = propertyname.indexOf("[");
        if (indexkey > -1) {
          key = propertyname.substring(indexkey + 1, propertyname.length() - 1);
          propertyname = propertyname.substring(0, indexkey);

          Object tmpobj = getPropertyValue(bean, propertyname);
          if (tmpobj instanceof List<?>) {
            List list = (List) tmpobj;
            list.add(value);
          } else if (tmpobj instanceof Map<?, ?>) {
            int classIndex = key.indexOf('{');
            String clazz = null;
            if (classIndex > -1) {
              clazz = key.substring(classIndex + 1, key.length() - 1);
              key = key.substring(0, classIndex);
            }
            Object hashKey = null;
            if ("String".equals(clazz)) {
              hashKey = key;
            }
            if (key == null) {
              throw new RFWCriticalException("RFW_ERR_200397", new String[] { propertyname, key });
            }

            ((Map) tmpobj).put(hashKey, value);
          }
        } else {
          Class<?> propertytype = getPropertyTypeByType(bean.getClass(), propertyname); // Procuramos pelo tipo para definir
          bean.getClass().getMethod("set" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), propertytype).invoke(bean, value);
        }
      }
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_000066", e);
    }
  }

  /**
   * Recupera a classe de uma propriedade do bean passado.<br>
   * Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente. Neste caso, será usado o método "get" da propriedade para descobrir sua classe. <b>Este método usará sempre a classe de retorno do método, ignorando a classe retornada pela instância passada!</b>
   *
   * @param beanclass Object objeto o qual o metodo GET será chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Class objeto retornado pelo método get do atributo. Null caso algum algum método GET retorne null.
   */
  public static Class<?> getPropertyTypeByType(Class<?> beanclass, String propertyname) throws RFWException {
    Objects.requireNonNull(beanclass, "O objeto beanclass recebido não pode ser nulo!");

    Class<?> returned;
    try {
      returned = null;
      int index = propertyname.indexOf(".");
      if (index > -1) {
        String firstproperty = propertyname.substring(0, index);
        Class<?> tmpclass = getPropertyTypeByType(beanclass, firstproperty);
        if (tmpclass != null) returned = getPropertyTypeByType(tmpclass, propertyname.substring(index + 1, propertyname.length()));
      } else {
        // Verifica se temos índice/chave, se tivermos mas é a última propriedade tornamos o objeto dentro da lista/hash
        int indexkey = propertyname.indexOf("[");
        if (indexkey > -1) {
          propertyname = propertyname.substring(0, indexkey);
          Class<?> tmpclass = getPropertyTypeByType(beanclass, propertyname); // Recupera a Hash/Lista deste atributo
          if (tmpclass != null) {
            if (List.class.isAssignableFrom(tmpclass)) {
              if (tmpclass.getGenericInterfaces().length > 0) tmpclass = getGenericFromReturnType(beanclass, propertyname)[0];
            } else if (Map.class.isAssignableFrom(tmpclass)) {
              tmpclass = getGenericFromReturnType(beanclass, propertyname)[1];
            }
            returned = tmpclass;
          }
        } else {
          try {
            returned = beanclass.getMethod("get" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).getReturnType();
          } catch (NoSuchMethodException e) {
            try {
              returned = beanclass.getMethod("is" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).getReturnType();
            } catch (NoSuchMethodException e2) {
              returned = beanclass.getMethod("are" + propertyname.substring(0, 1).toUpperCase() + propertyname.substring(1, propertyname.length()), (Class[]) null).getReturnType();
            }
          }
        }
      }
    } catch (Throwable e) {
      throw new RFWCriticalException("RFW_ERR_200480", e);
    }
    return returned;
  }

  /**
   * Este método retorna o tipo (classe) de um atributo, navegando por Neasted Properties no padrão do RFWDeprec, semelhante ao {@link #getPropertyTypeByType(Class, String)} mas ignorando as Listas/Hashs. Isto é, sempre que encontramos uma {@link List} ou {@link Map} no caminho o RFWDeprec retornará o tipo de objeto que é armazenado dentro dessas coleções. Para outros tipos, o tipo do objeto é retornado
   * normalmente.<br>
   * <br>
   * A resolução de qual é o objeto dentro da collection é feito da seguinte maneira:
   * <li>Verificamos se o atributo tem uma {@link RFWMetaRelationshipField} e o atributo {@link RFWMetaRelationshipField#targetRelationship()} definido. Caso verdadeiro, essa definição é utilizada.
   * <li>Verificamos os Generics definidos. Caso tenha essa classe é utilizada para continuar a busca.
   *
   * @param beanClass Classe inicial para percorrer o caminho até o atributo final.
   * @param propertyName Nome da Propriedade, com ou sem Neasted Properties.
   * @throws RFWException Lançado em Caso de Exception
   */
  @SuppressWarnings("unchecked")
  public static <VO extends RFWVO> Class<?> getPropertyTransparentType(Class<VO> beanClass, String propertyName) throws RFWException {
    Objects.requireNonNull(beanClass, "O atributo beanclass não pode ser nulo!");
    Objects.requireNonNull(propertyName, "O atributo propertyName não pode ser nulo!");

    // Limpamos o caminho pois só vamos trabalhar com a Classe dos objetos
    propertyName = getCleanPath(propertyName);

    int index = propertyName.indexOf(".");
    if (index > -1) {
      // Se temos neasted properties, chamamos recursivamente até chegar na última propriedade
      Class<VO> valueVOClass = (Class<VO>) getPropertyTransparentType(beanClass, propertyName.substring(0, index));
      // Se ainda temos Neasted Properties, mas o tipo retornado não é um RFWVO, Exception!!!
      if (valueVOClass == null || !RFWVO.class.isAssignableFrom(valueVOClass)) throw new RFWCriticalException("Impossível obter o caminho '${0}' na clase '${1}' se o tipo retornado não é um RFWVO!", new String[] { propertyName, beanClass.getCanonicalName() });
      return getPropertyTransparentType(valueVOClass, propertyName.substring(index + 1, propertyName.length()));
    } else {
      try {
        // Se não é uma Neasted Property, recuperamos a classe do atributo e verificamos as listas/hashs
        Field field = beanClass.getDeclaredField(propertyName);
        Class<?> type = field.getType();
        if (List.class.isAssignableFrom(type)) {
          // Procuramos a Annotation para pegar o atributo Target
          RFWMetaRelationshipField ann = field.getAnnotation(RFWMetaRelationshipField.class);
          if (ann != null && ann.targetRelationship() != RFWVO.class) {
            return ann.targetRelationship();
          } else {
            // Se Não temos a annotation ou o target, tentamos recuperar o generics da lista
            final Class<?>[] gi = getGenericsFromField(field);
            if (gi != null && gi.length > 0) return gi[0];
          }
        } else if (Map.class.isAssignableFrom(type)) {
          // Procuramos a Annotation para pegar o atributo Target
          RFWMetaRelationshipField ann = field.getAnnotation(RFWMetaRelationshipField.class);
          if (ann != null && ann.targetRelationship() != RFWVO.class) {
            return ann.targetRelationship();
          } else {
            // Se Não temos a annotation ou o target, tentamos recuperar o generics da lista
            final Class<?>[] gi = getGenericsFromField(field);
            if (gi != null && gi.length > 1) return gi[1]; // Retorna o índice do generics do Value e não da chave
          }
        } else {
          // Qualquer outro tipo é só retornar
          return type;
        }
      } catch (Throwable e) {
        throw new RFWCriticalException("Falha ao obter o atributo '${0}' da classe '${1}'.", new String[] { propertyName, beanClass.getCanonicalName() }, e);
      }
    }
    return null;
  }

  /**
   * Recupera as classes definidas no Generics da Declaração de um attributo (Field) de uma classe.
   *
   * @param field Atributo Field já recuperado por reflexão.
   * @return Conjunto de Classes que forem definidos no Generics da Declaração de um Attributo.
   * @throws RFWException
   */
  public static Class<?>[] getGenericsFromField(Field field) throws RFWException {
    Objects.requireNonNull(field, "Classe é obrigatórioa!");

    Class<?>[] ret = null;
    final Type type = field.getGenericType();
    if (type != null && type instanceof ParameterizedType) {
      Type[] acTypeArgs = ((ParameterizedType) type).getActualTypeArguments();
      if (acTypeArgs != null) {
        ret = new Class[acTypeArgs.length];
        for (int i = 0; i < acTypeArgs.length; i++) {
          ret[i] = (Class<?>) acTypeArgs[i];
        }
      }
    }
    return ret;
  }

  /**
   * Este método procura as definições dos Generics no retorn do método get da propriedade passada.<br>
   * Por exemplo, caso a propriedade da classe passada retorne uma List ou HashMap os Generics definidos serão encontrados e suas classes retornadas.
   *
   * @param beanclass Classe do Bean
   * @param propertyName nome da propriedade cujo retorno será avaliado.
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  @SuppressWarnings("rawtypes")
  public static Class<?>[] getGenericFromReturnType(Class<?> beanclass, String propertyName) throws RFWException {
    try {
      Class[] returned = null;
      Type returnType = null;
      try {
        returnType = beanclass.getMethod("get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length()), (Class[]) null).getGenericReturnType();
      } catch (NoSuchMethodException e) {
        try {
          returnType = beanclass.getMethod("is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length()), (Class[]) null).getGenericReturnType();
        } catch (NoSuchMethodException e2) {
          returnType = beanclass.getMethod("are" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length()), (Class[]) null).getGenericReturnType();
        }
      }

      if (returnType != null && returnType instanceof ParameterizedType) {
        Type[] acTypeArgs = ((ParameterizedType) returnType).getActualTypeArguments();
        if (acTypeArgs != null) {
          returned = new Class[acTypeArgs.length];
          for (int i = 0; i < acTypeArgs.length; i++) {
            returned[i] = (Class<?>) acTypeArgs[i];
          }
        }
      }
      return returned;
    } catch (Throwable e) {
      throw new RFWCriticalException("RFW_ERR_200480", e);
    }
  }

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
   * Recupera a classe de uma propriedade do bean passado. Caso a propriedade tenha varios nomes separados por pontos ".", estes serao divididos e recuperados recusivamente.<br>
   * Este método é semelhante ao {@link BUReflex#getPropertyTypeByObject(Class, String)}, a diferença é que se uma propriedade retornar nula, ele continua explorando o objeto pela classes de retorno do objeto (como feito pela classe {@link BUReflex#getPropertyTypeByType(Class, String)}.<br>
   *
   *
   * @param bean objeto o qual o metodo GET será chamado
   * @param propertyname String nome da propriedade que deseja-se obter o valor
   * @return Class objeto retornado pelo método get do atributo. Null caso algum algum método GET retorne null.
   */
  public static Class<?> getPropertyTypeByObjectAndType(Object bean, String propertyname) throws RFWException {
    if (bean == null) {
      throw new NullPointerException("O objeto bean recebido não pode ser nulo!");
    }
    Class<?> returned;
    try {
      returned = null;
      int index = propertyname.indexOf(".");
      if (index > -1) {
        Object tmpobj = getPropertyValue(bean, propertyname.substring(0, index));
        if (tmpobj == null) { // Se o objeto de retorno é nulo, apelamos para usar o tipo de retorno do método
          returned = getPropertyTypeByType(bean.getClass(), propertyname);
        } else { // Caso contrário, continuamos usando o objeto
          returned = getPropertyTypeByObjectAndType(tmpobj, propertyname.substring(index + 1, propertyname.length()));
        }
      } else {
        Object tmpobj = getPropertyValue(bean, propertyname);
        if (tmpobj == null) { // Se o objeto de retorno é nulo, apelamos para usar o tipo de retorno do método
          returned = getPropertyTypeByType(bean.getClass(), propertyname);
        } else { // Caso contrário, continuamos usando o objeto
          returned = tmpobj.getClass();
        }
      }
    } catch (Throwable e) {
      throw new RFWCriticalException("RFW_ERR_200480", e);
    }
    return returned;
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
   * Este método recebe um objeto e um nome de propriedade. Tentará encontrar uma RFWMeta Annotation neste atributo.
   *
   * @param voClass Classe a ser analizada
   * @param attribute Nome do atributo para encontrar a RFWMeta Annotation
   * @return uma annotation RFWMeta ou NULL caso nenhuma seja encontrada.
   * @throws RFWException
   */
  @SuppressWarnings("unchecked")
  public static Annotation getRFWMetaAnnotation(Class<? extends RFWVO> voClass, String attribute) throws RFWException {
    Annotation ann = null;
    Field field = null;

    // Se houver neasted properties, recuperamos a classe do penúltimo atributo, para que dele possamos recuperar o Field
    if (attribute.indexOf(".") > -1) {
      final int lindex = attribute.lastIndexOf(".");
      final Class<?> tmpClass = getPropertyTransparentType(voClass, attribute.substring(0, lindex));
      if (tmpClass == null || !RFWVO.class.isAssignableFrom(tmpClass)) throw new RFWCriticalException("Impossível obter o caminho '${0}' na clase '${1}' se o tipo retornado não é um RFWVO!", new String[] { attribute, voClass.getCanonicalName() });
      voClass = (Class<? extends RFWVO>) tmpClass;
      attribute = attribute.substring(lindex + 1, attribute.length());
    }

    // Continuamos normalmente agora que pegamos a classe correta para obter o atributo
    try {
      if (!"id".equals(attribute)) field = voClass.getDeclaredField(getCleanPath(attribute));
    } catch (Exception e) {
      throw new RFWCriticalException("Falha ao encontrar uma RFWMeta annotation no atributo '${0}' da classe '${1}'", new String[] { attribute, voClass.getCanonicalName() }, e);
    }
    if (field != null) {
      final Annotation[] anns = field.getDeclaredAnnotations();
      for (Annotation annotation : anns) {
        final String basepackage = RFWMetaStringField.class.getPackage().getName();
        if (annotation.annotationType().getCanonicalName().startsWith(basepackage + ".RFWMeta")) {
          ann = annotation;
          break;
        }
      }
    }
    return ann;
  }

  /**
   * Este método recebe um campo field diretamente e tentará encontrar uma RFWMeta Annotation neste atributo.
   *
   * @param field Objeto field da reflexão do Java para encontrar a RFWMeta Annotation
   * @return uma annotation RFWMeta ou NULL caso nenhuma seja encontrada.
   * @throws RFWException
   */
  public static Annotation getRFWMetaAnnotation(Field field) throws RFWException {
    Annotation ann = null;
    final Annotation[] anns = field.getDeclaredAnnotations();
    for (Annotation annotation : anns) {
      final String basepackage = RFWMetaStringField.class.getPackage().getName();
      if (annotation.annotationType().getCanonicalName().startsWith(basepackage + ".RFWMeta")) {
        ann = annotation;
        break;
      }
    }
    return ann;
  }

  /**
   * Procura um atributo em um RFWVO, procura se há alguma RFWMetaAnnotation no atributo, e se esta RFWMeta tiver o atributo "caption" retorna seu valor. Caso contrário retornará null.<br>
   * Este método não lança exceções caso o não tenha a RFWMeta, ou se a mesma não tiver o atributo caption.
   *
   * @param voClass Classe a ser analizada
   * @param attribute Atributo para procurar pela RFWMeta
   * @return O valor da Caption, ou NULL caso não tenha sido encontrado
   * @throws RFWException
   */
  public static String getRFWMetaAnnotationCaption(Class<? extends RFWVO> voClass, String attribute) throws RFWException {
    String caption = null;
    Annotation ann = getRFWMetaAnnotation(voClass, attribute);
    if (ann != null) {
      try {
        Method m = ann.getClass().getMethod("caption");
        caption = (String) m.invoke(ann);
      } catch (Exception e) {
        // Qualquer falha ao tentar obter o caption não é reportado nem tratado, apenas retornamos null
      }
    }
    return caption;
  }

  /**
   * Procura um atributo em um RFWVO, procura se há alguma RFWMetaAnnotation no atributo, e se esta RFWMeta tiver o atributo "preProcess" retorna seu valor. Caso contrário retornará null.<br>
   * Este método não lança exceções caso o não tenha a RFWMeta, ou se a mesma não tiver o atributo caption.
   *
   * @param voClass Classe a ser analizada
   * @param attribute Atributo para procurar pela RFWMeta
   * @return O valor da Caption, ou NULL caso não tenha sido encontrado
   * @throws RFWException
   */
  public static PreProcessOption[] getRFWMetaAnnotationPreProcess(Class<? extends RFWVO> voClass, String attribute) throws RFWException {
    PreProcessOption[] preProcess = null;
    Annotation ann = getRFWMetaAnnotation(voClass, attribute);
    if (ann != null) {
      try {
        Method m = ann.getClass().getMethod("preProcess");
        preProcess = (PreProcessOption[]) m.invoke(ann);
      } catch (Exception e) {
        try {
          Method m = ann.getClass().getMethod("preProcess");
          preProcess = new PreProcessOption[] { (PreProcessOption) m.invoke(ann) };
        } catch (Exception e2) {
          // Qualquer falha ao tentar obter o preProcess não é reportado nem tratado, apenas retornamos null
        }
      }
    }
    return preProcess;
  }

  /**
   * Procura um atributo em um RFWVO, procura se há alguma RFWMetaAnnotation no atributo, e se esta RFWMeta tiver o atributo "required" retorna seu valor. Caso contrário retornará null.<br>
   * Este método não lança exceções caso o não tenha a RFWMeta, ou se a mesma não tiver o atributo caption.
   *
   * @param voClass Classe a ser analizada
   * @param attribute Atributo para procurar pela RFWMeta
   * @return O valor da Caption, ou NULL caso não tenha sido encontrado
   * @throws RFWException
   */
  public static Boolean getRFWMetaAnnotationRequired(Class<? extends RFWVO> voClass, String attribute) throws RFWException {
    Boolean required = null;
    Annotation ann = getRFWMetaAnnotation(voClass, attribute);
    if (ann != null) {
      try {
        Method m = ann.getClass().getMethod("required");
        required = (Boolean) m.invoke(ann);
      } catch (Exception e) {
        // Qualquer falha ao tentar obter o caption não é reportado nem tratado, apenas retornamos null
      }
    }
    return required;
  }

  /**
   * Este método extrai o caminho "limpo" a partir de um caminho completo.<br>
   * O caminho limpo é o caminho dos atributos mas sem os colchetes de índices ou chaves de hash. Facilitando assim acomparação do atributo sendo tratado.<br>
   * Ex: "itemcodelist[0].itemvo" retorna "itemcodelist.itemvo". Ex: "itemcodehash{key}.itemvo" retorna "itemcodehash.itemvo".
   *
   * @param fullpath
   * @return
   */
  public static String getCleanPath(String fullpath) {
    return fullpath.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\{[^\\}]*\\}", "");
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
   * Este método tenta montar um caminho com os 'captions' das RFWMetaAnnotations recursivamente, no padrão "Caption 1 > caption 2 > Caption 3".<br>
   * Caso algum atirbuto não tenha a RFWMetaAnnotation o método lançará exception.<br>
   * Caso o caption esteja vazio (""), o passo será ignorado para evitar a concatenação "> >". <br>
   * Este método não considera os indeces de Lista ou chaves de Hash para colocar no caminho. Essa opção deve ser criada em outro método.
   *
   * @param voClass Classe do VO do começo do caminho.
   * @param attributePath Caminho partindo do voClass até o atributo desejado
   * @return
   * @throws RFWException
   */
  public static String getRFWMetaAnnotationFullCaption(Class<? extends RFWVO> voClass, String attributePath) throws RFWException {
    boolean showListIndex = true;
    int startindex = 0;
    StringBuilder buff = new StringBuilder(20);
    while (startindex < attributePath.length()) {
      int finalindex = attributePath.indexOf(".", startindex);
      if (finalindex == -1) finalindex = attributePath.length();
      if (buff.length() > 0) buff.append(" > ");
      final String path = attributePath.substring(0, finalindex);
      buff.append(BUReflex.getRFWMetaAnnotationCaption(voClass, path));
      if (showListIndex && path.matches(".*\\[[0-9]+\\]")) buff.append("[").append(Integer.parseInt(path.substring(path.indexOf("[") + 1, path.indexOf("]"))) + 1).append("]");
      startindex = finalindex + 1;
    }
    return buff.toString();
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
   * Este método vasculha o caminho de propriedades recebido em "propertyPath" e verifica se algum dos atributos retorna uma estrutura de conjunto de objetos (tipicamente uma lista, hashMap, collection, etc.). Caso encontre alguma e que no próprio caminho não especifique o índice ou chave (definidos com os caracteres "[]") o método retornará o caminho até o ponto em que o objeto de
   * "coleção/map/list" foi encontrado.<br>
   * Normalmente, este método retorna o caminho até onde o método {@link #getPropertyValue(Object, String)} funciona, retornando objetos, já que o {@link #getPropertyValue(Object, String)} retorna exception quando se depara com algum desses objetos sem a definição de índice ou que não seja o final do caminho.<br>
   * <br>
   * Exemplo:<br>
   * Imagine o caminho "vo1.vo2.vo3.list1.vo5.map1.vo7", onde lista1 sem uma lista de vo4 e map1 é uma hash de vo6. <br>
   * <br>
   * Caso seja passado um objeto vo1, com uma cadeia completa de objetos (nenhum objeto nulo), este método retornará o caminho "vo1.vo2.vo3.list1", já que não sabemos em qual dos vo4 (dentro da list1) devemos continuar a percorrer o caminho.<br>
   * <bR>
   * No entanto, se passad o caminho "vo1.vo2.vo3.list1[2].vo5.map1.vo7" para a mesma estrutura de objetos, este método retornará o caminho "vo1.vo2.vo3.list1[2].vo5.map1", já que agora o método sabe que ao se deparar com a list1 devemos continuar navegando dentro do objeto de índice 2. <br>
   * <Br>
   * <b>ATENÇÃO:</B> Note que este método, assim como o {@link #getPropertyValue(Object, String)} utiliza as instâncias dos objetos para navegar o caminho passado, logo se algum método retornar um objeto nulo o método não continuará navegando pelo caminho informado (retornando nulo).
   *
   * @param bean Objeto raiz por onde devemos procurar os objetos.
   * @param propertyPath Caminho a ser percorrido no objeto em busca de alguma estrutura de múltiplos objetos.
   * @return Caminho até onde foi encontramos uma estrutura em que não pudemos navegar mais, ou NULL caso em algum ponto do caminho nos deparemos com um objeto nulo impedindo de continuar a percorrer o caminho, ou se algum objeto retornou nulo no meio do caminho, nos impedindo de encontrar uma estrutura.
   * @throws RFWException Lançado caso ocorra algum problema durante a execução do método.
   */
  public static String findIterableInPath(Object bean, String propertyPath) throws RFWException {
    // Quebramos o caminho de acordo com os "."
    final String[] properties = propertyPath.split("\\."); // Cada properties já inclui seus próprios "[...]"
    // Iteramos o caminho para tentar obter o valor parte a parte em busca de um objeto iterável
    Object lastBean = bean;
    String property = "";
    for (int i = 0; i < properties.length; i++) {
      property += (property.length() > 0 ? '.' : "") + properties[i];
      lastBean = getPropertyValue(lastBean, properties[i]);
      if (lastBean == null) return null;
      if (lastBean instanceof List<?>) {
        return property;
      } else if (lastBean instanceof Map<?, ?>) {
        return property;
      }
    }
    return null;
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
   * Limpa o ID do objeto e recursivamente de todos os objetos de relacionamento do tipo composição.<br>
   * Este método tem a finalidade de duplicar/clonar um objeto, permitindo que ao ser persistido ele possa ser identificado como um objeto novo.<br>
   * Lembre-se que atributos que tenham a restrição de 'unique=true', continuarão impedindo que o novo objeto seja persistido. Nesses dados este método não faz nenhum tipo de alteração.
   *
   * @param vo RFWVO a ter seus IDs limpos
   * @throws RFWException
   */
  @SuppressWarnings("unchecked")
  public static <VO extends RFWVO> void cleanOwnIDs(VO vo) throws RFWException {
    vo.setId(null);
    for (Field field : vo.getClass().getDeclaredFields()) {
      final Annotation ann = BUReflex.getRFWMetaAnnotation(field);
      if (ann instanceof RFWMetaRelationshipField) {
        switch (((RFWMetaRelationshipField) ann).relationship()) {
          case INNER_ASSOCIATION:
          case MANY_TO_MANY:
          case PARENT_ASSOCIATION:
          case ASSOCIATION:
          case WEAK_ASSOCIATION:
            // Para esses casos não remove nem faz nada, pois não serão persistidos com o novo objeto, e sim só relacionados
            break;
          case COMPOSITION_TREE:
          case COMPOSITION: {
            final Object value = BUReflex.getPropertyValue(vo, field.getName());
            if (value != null) {
              if (value instanceof RFWVO) {
                cleanOwnIDs((VO) value);
              } else if (value instanceof Map) {
                for (Object mapValue : ((Map<?, ?>) value).values()) {
                  cleanOwnIDs((VO) mapValue);
                }
              } else if (value instanceof List) {
                for (Object listValue : (List<?>) value) {
                  cleanOwnIDs((VO) listValue);
                }
              } else {
                throw new RFWCriticalException("Impossível duplicar o VO! Tipo de atributo desconhecido no atributo '${0}' da classe '${1}'.", new String[] { field.getName(), vo.getClass().getCanonicalName() });
              }
            }
          }
            break;
        }
      }

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
      atts.add(getAttributePath("", basepath));
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
            atts.add(getAttributePath("", basepath));
          }
        } else if (Iterable.class.isInstance(obj1)) {
          Iterator<?> it1 = ((Iterable<?>) obj1).iterator();
          Iterator<?> it2 = ((Iterable<?>) obj2).iterator();
          int index = 0;
          while (true) {
            if (!it1.hasNext() && !it2.hasNext()) {
              break;
            } else if (it1.hasNext() ^ it2.hasNext()) {
              atts.add(getAttributePath("", basepath));
              break;
            } else {
              Object itObj1 = it1.next();
              Object itObj2 = it2.next();
              atts.addAll(compareRecursively(itObj1, itObj2, getAttributePath("", index, basepath), cache));
            }
            index++;
          }
        } else if (Map.class.isInstance(obj1)) {
          Map<?, ?> map1 = (Map<?, ?>) obj1;
          Map<?, ?> map2 = (Map<?, ?>) obj2;
          if (map1.size() != map2.size()) {
            atts.add(getAttributePath("", basepath));
          } else {
            for (Object key : map1.keySet()) {
              Object mapObj1 = map1.get(key);
              Object mapObj2 = map2.get(key);
              if (mapObj1 == null ^ mapObj2 == null) {
                atts.add(getAttributePath("", key, key.getClass(), basepath));
              } else if (mapObj1 != null && mapObj2 != null) {
                atts.addAll(compareRecursively(mapObj1, mapObj2, getAttributePath("", key, key.getClass(), basepath), cache));
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
                atts.addAll(compareRecursively(ret1, ret2, getAttributePath(attribute, basepath), cache));
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
   * Cria o caminho para um item dentro de uma hash.
   *
   * @param attribute Nome do atributo que contém a hash.
   * @param key Representação em String do objeto chave da hash.
   * @param clazz Classe da chave de Hash. Atualmente suporta apenas String e Long.
   * @param basepath Caminho base até chegar neste atributo.
   * @return
   */
  public static String getAttributePath(String attribute, Object key, Class<?> clazz, String basepath) {
    if (String.class.isAssignableFrom(clazz)) {
      key = key + "{String}";
    } else if (Long.class.isAssignableFrom(clazz)) {
      key = key + "{Long}";
    } else {
      throw new RFWRunTimeException("Classe não suportada como chave de hash!");
    }
    attribute = attribute + "[" + key + "]";
    if (basepath != null) {
      return basepath + "." + attribute;
    } else {
      return attribute;
    }
  }

  public static String getAttributePath(String attribute, String basepath) {
    if (basepath != null) {
      if (attribute != null && !"".equals(attribute)) {
        return basepath + "." + attribute;
      } else {
        return basepath;
      }
    } else {
      return attribute;
    }
  }

  public static String getAttributePath(String attribute, int index, String basepath) {
    attribute = attribute + "[" + index + "]";
    if (basepath != null) {
      return basepath + "." + attribute;
    } else {
      return attribute;
    }
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
   * Este método recebe um objeto e um nome de propriedade. Tentará encontrar uma RFWDAOEncrypt neste atributo.
   *
   * @param voClass Classe a ser analizada
   * @param attribute Nome do atributo para encontrar a RFWMeta Annotation
   * @return uma annotation RFWMeta ou NULL caso nenhuma seja encontrada.
   * @throws RFWException
   */
  @SuppressWarnings("unchecked")
  public static RFWDAOEncrypt getRFWDAOEncryptAnnotation(Class<? extends RFWVO> voClass, String attribute) throws RFWException {
    RFWDAOEncrypt ann = null;
    Field field = null;

    // Se houver neasted properties, recuperamos a classe do penúltimo atributo, para que dele possamos recuperar o Field
    if (attribute.indexOf(".") > -1) {
      final int lindex = attribute.lastIndexOf(".");
      final Class<?> tmpClass = getPropertyTransparentType(voClass, attribute.substring(0, lindex));
      if (tmpClass == null || !RFWVO.class.isAssignableFrom(tmpClass)) throw new RFWCriticalException("Impossível obter o caminho '${0}' na clase '${1}' se o tipo retornado não é um RFWVO!", new String[] { attribute, voClass.getCanonicalName() });
      voClass = (Class<? extends RFWVO>) tmpClass;
      attribute = attribute.substring(lindex + 1, attribute.length());
    }

    // Continuamos normalmente agora que pegamos a classe correta para obter o atributo
    try {
      if (!"id".equals(attribute)) field = voClass.getDeclaredField(getCleanPath(attribute));
    } catch (Exception e) {
      throw new RFWCriticalException("Falha ao encontrar uma RFWMeta annotation no atributo '${0}' da classe '${1}'", new String[] { attribute, voClass.getCanonicalName() }, e);
    }
    if (field != null) {
      ann = field.getAnnotation(RFWDAOEncrypt.class);
    }
    return ann;
  }

  /**
   * Procura um RFWVO em uma determinada lista pelo seu ID. <br>
   * Note que se houver mais do que um RFWVO com o mesmo ID, será retornado o primeiro encontrado.
   *
   * @param <VO> Tipo do Objeto que extenda {@link RFWVO}
   * @param list Lista de RFWVO
   * @param id ID do VO a ser encontrado.
   * @return Objeto com o ID desejado, ou nulo caso não tenha contrado nenhum.
   * @throws RFWException
   */
  public static <VO extends RFWVO> VO getRFWVOByID(List<VO> list, Long id) throws RFWException {
    for (VO vo : list) {
      if (vo.getId().equals(id)) return vo;
    }
    return null;
  }

  /**
   * Procura um RFWVO em uma lista caso o atributo passado seja 'equals' o valor. <br>
   * Note que se houver mais do que um RFWVO com o mesmo ID, será retornado o primeiro encontrado.
   *
   * @param <VO> Tipo do Objeto que extenda {@link RFWVO}
   * @param list Lista de RFWVO
   * @param path Caminho dos atirbutos aninhados até a propriedade que precisa ser validada.
   * @param value Valor para ser passado no equals e comparado. Caso seja passado 'null', o método verificará se o retorno da propriedade passada em 'path' também é nulo. OBS: Note que se qualquer parâmetro dos atirbutos aninhados retornar nulo, o resultado será 'true' caso value seja 'null'.
   * @return Primeiro objeto cujo atributo passado seja 'equals' o 'value' passado.
   * @throws RFWException
   */
  public static <VO extends RFWVO> VO getRFWVO(List<VO> list, String path, Object value) throws RFWException {
    RFWException[] ex = new RFWException[1];
    List<VO> filteredList = list.stream().filter(vo -> {
      try {
        Object v = BUReflex.getPropertyValue(vo, path);
        if (v == null && value == null) return true;
        if (v != null && v.equals(value)) return true;
      } catch (RFWException e) {
        ex[1] = e;
      }
      return false;
    }).collect(Collectors.toList());
    if (ex[0] != null) throw ex[0]; // se tiver uma exception dentro do bloco Lambda, lança ela fora
    if (filteredList.size() > 0) return filteredList.get(0);
    return null;
  }

  /**
   * Este método recupera a pilha dos métodos que foram sendo chamados até a chamada deste método. Cria o caminho dos métodos chamados como uma pilha de StackTrace
   *
   * @param offset permite "ocultar" as últimas chamadas do método. Por exemplo, se não se deseja registrar o próprio método getInvoker deve-se passar 1. Se não deseja registrar o método que chamou este Método, deve se passar 2, e assim por diante. Valor mínimo 0, vaor muito alto pode não sobrar nada na pilha para imprimir.
   * @param stacksize tamanho máximo da pilha para retornar. Pode limitar a quantidade máxima de linhas a serem retornadas.
   */
  public static final String getInvoker(int offset, int stacksize) {
    final StringBuilder buff = new StringBuilder();

    if (offset < 0) offset = 0;

    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
    for (int i = offset; i < stacktrace.length && i - offset < stacksize; i++) {
      buff.append('\t').append(stacktrace[i].getClassName()).append(".").append(stacktrace[i].getMethodName()).append("(").append(stacktrace[i].getFileName()).append(":").append(stacktrace[i].getLineNumber()).append(")").append("\r\n");
    }
    return buff.toString();
  }

  /**
   * Este método obtem uma classe de RFWVO e vasculha todos os objetos que são dependentes/relacionados a este objeto. Em outras palavras retorna os atributos que precisam ser recuperados para uma atualização.<br>
   * A busca consiste em recuperar todos os objetos que estejam associados (tipos ASSOCIATION, PARENT_ASSOCIATION ou MANY_TO_MANY), para que seja possível a detecção da desassociação caso o objeto não esteja mais presente. E recuperamos recursivamente os objetos de composição (tipo COMPOSITION) para o mesmo fim.
   *
   * @param clazz Classe do VO que vamos vasculhar
   * @return Lista com o caminho até a propriedade id dos objetos que precisam ser recupeados. Ex: 'users.companies.id'.
   * @throws RFWException
   */
  public static <VO extends RFWVO> String[] getRFWVOUpdateAttributes(Class<VO> clazz) throws RFWException {
    return getRFWVOUpdateAttributes(clazz, "").toArray(new String[0]);
  }

  /**
   * Este método obtem uma classe de RFWVO e vasculha todos os objetos que são dependentes/relacionados a este objeto. Em outras palavras retorna os atributos que precisam ser recuperados para uma atualização.<br>
   * A busca consiste em recuperar todos os objetos que estejam associados (tipos ASSOCIATION, PARENT_ASSOCIATION ou MANY_TO_MANY), para que seja possível a detecção da desassociação caso o objeto não esteja mais presente. E recuperamos recursivamente os objetos de composição (tipo COMPOSITION) para o mesmo fim.
   *
   * @param clazz Classe do VO que vamos vasculhar
   * @param path Caminho usado até chegar na classe sendo passada. Usado durante a recursão. Para obter os atributos do objeto raiz passe o valor "".
   * @return Lista com o caminho até a propriedade id dos objetos que precisam ser recupeados. Ex: 'users.companies.id'.
   * @throws RFWException
   */
  @SuppressWarnings("unchecked")
  private static <VO extends RFWVO> List<String> getRFWVOUpdateAttributes(Class<VO> clazz, String path) throws RFWException {
    final LinkedList<String> list = new LinkedList<>();

    // iteramos todos os fields da classe em busca da annotation RFWMetaRelationshipField
    for (Field field : clazz.getDeclaredFields()) {
      final RFWMetaRelationshipField ann = field.getAnnotation(RFWMetaRelationshipField.class);
      if (ann != null) {
        switch (ann.relationship()) {
          case WEAK_ASSOCIATION:
            // Nada para fazer, esse tipo de associação é como se não existisse para o RFWDAO.
            break;
          case COMPOSITION:
            list.add(addPath(path, field.getName()) + ".id");

            Class<? extends RFWVO> entityType = null;
            if (!ann.targetRelationship().equals(RFWVO.class)) {
              entityType = ann.targetRelationship();
            } else if (RFWVO.class.isAssignableFrom(field.getType())) {
              entityType = (Class<? extends RFWVO>) field.getType();
            }
            if (entityType == null) {
              throw new RFWCriticalException("Não foi possível encontrar o RFWVO no relacionamento da entidade '${0}' no atributo '${1}\". Relacionamentos com coleções é obrigatório a definição do atributo 'targetRelationship' na RFWMetaAnnotation.", new String[] { clazz.getCanonicalName(), field.getName() });
            }
            if (!clazz.equals(entityType)) {
              // Quando a classe atual e a da compoisção são as mesmas encontramos um loop, o que acontece quando utilizado em casos hierárquicos.
              // Neste caso paramos de buscar os atributos pq termina em loop eterno.
              list.addAll(getRFWVOUpdateAttributes(entityType, addPath(path, field.getName())));
            }
            break;
          case ASSOCIATION:
          case PARENT_ASSOCIATION:
          case INNER_ASSOCIATION:
          case MANY_TO_MANY:
            list.add(addPath(path, field.getName()) + ".id");
            break;
          case COMPOSITION_TREE:
            // Salvamos os caminhos encontratos na cache de CompositionTree para vasculhar melhor, mas não continuamos navegando mais.
            list.add(addPath(path, field.getName()) + ".id");
            break;
        }
      }
      final RFWMetaCollectionField colAnn = field.getAnnotation(RFWMetaCollectionField.class);
      if (colAnn != null) {
        list.add(addPath(path, field.getName()) + "@");
      }
    }
    return list;
  }

  /**
   * Método auxiliar para adicionar um caminho na estrutura existente.<br>
   *
   * @param path Caminho atual.
   * @param newAdd Pedaço a ser adicionado ao caminho atual.
   * @return Retorna o caminho pai. Ex:
   *         <li>Se passado "a.b.c.d.e" este método deve retornar "a.b.c.d".
   *         <li>Se passado "a" o método deve retornar "" já que vazio é caminho raiz.
   *         <li>Se passado "" retornamos null já que não temos um caminho pai.
   */
  public static String addPath(String path, String newAdd) {
    if (path == null || "".equals(path)) {
      return newAdd;
    } else {
      return path + "." + newAdd;
    }
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
