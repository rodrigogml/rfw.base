package br.eng.rodrigogml.rfw.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.vo.RFWRecursiveClonable;

/**
 * Description: Classe utilitária que ajuda a implementação da interface {@link RFWRecursiveClonable}.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (18/02/2016)
 */
public class RFWRecursiveClone {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static RFWRecursiveClonable cloneRecursive(RFWRecursiveClonable obj, HashMap<RFWRecursiveClonable, RFWRecursiveClonable> clonedObjects) throws RFWException {
    // Veririca se já estamos na hash, se estiver retornamos esse objeto
    RFWRecursiveClonable cloned = clonedObjects.get(obj);
    if (cloned != null) {
      return cloned;
    }

    // Se ainda não estamos na hash de objetos clonados, nos clonamos e incluimos na hash
    RFWRecursiveClonable clonedvo;
    try {
      try {
        // Primeiro sempre tentamos fazer uma nova instância ao invés de usar o método clone(). Dá mesmo conflito com as referências do Java
        clonedvo = obj.getClass().newInstance();
      } catch (Exception e) {
        clonedvo = (RFWRecursiveClonable) obj.clone();
      }
    } catch (CloneNotSupportedException e1) {
      throw new RFWCriticalException(e1);
    }
    clonedObjects.put(obj, clonedvo);
    // Recupera a lista de métodos desse objeto
    Method[] methods = obj.getClass().getMethods();
    // Itera essa lista atrás de métodos do tipo "get" ou "is"
    for (int i = 0; i < methods.length; i++) {
      String methodname = methods[i].getName();
      if (methodname.startsWith("get") || methodname.startsWith("is")) {
        Method methodget = methods[i];
        Class<?> returntype = methodget.getReturnType();
        Method methodset = null;
        try {
          int x = (methodname.startsWith("is") ? 2 : 3);
          methodset = obj.getClass().getMethod("set" + methodname.substring(x, methodname.length()), returntype);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
        // Verifica a existência do Método SET
        if (methodset != null) {
          // Verifica se o retorno do método get não é nulo
          try {
            Object gettedvalue = methodget.invoke(obj);
            if (gettedvalue != null) {
              // Verifica se o tipo de objeto é um dos que desejamos fazer o "deep clone"
              if (gettedvalue instanceof RFWRecursiveClonable) {
                RFWRecursiveClonable clonedvalue = ((RFWRecursiveClonable) gettedvalue).cloneRecursive(clonedObjects);
                methodset.invoke(clonedvo, clonedvalue);
              } else if (List.class.isAssignableFrom(gettedvalue.getClass())) {
                List clonedvalue = null;
                if (gettedvalue instanceof ArrayList) {
                  clonedvalue = (List) ((ArrayList) gettedvalue).clone();
                } else if (gettedvalue instanceof LinkedList) {
                  clonedvalue = (List) ((LinkedList) gettedvalue).clone();
                } else {
                  throw new RFWCriticalException("O RFWRecursiveClone não suporta a List do tipo '${0}'.", new String[] { gettedvalue.getClass().getCanonicalName() });
                }
                clonedvalue.clear();
                for (Object object : (List) gettedvalue) {
                  if (object instanceof RFWRecursiveClonable) {
                    clonedvalue.add(((RFWRecursiveClonable) object).cloneRecursive(clonedObjects));
                  } else {
                    clonedvalue.add(object);
                  }
                }
                methodset.invoke(clonedvo, clonedvalue);
              } else if (Map.class.isAssignableFrom(gettedvalue.getClass())) {
                Map clonedvalue = null;
                if (gettedvalue instanceof HashMap) {
                  clonedvalue = (Map) ((HashMap) gettedvalue).clone();
                  clonedvalue.clear();

                  // Clonamos não só o valor, mas também a chave, em alguns casos a chave da Hash pode ser um próprio RFWVO como referência
                  for (Object key : ((HashMap) gettedvalue).keySet()) {
                    Object mapValue = ((HashMap) gettedvalue).get(key); // Recupera antes de clonar a chave ou não encontramos nada.

                    if (key instanceof RFWRecursiveClonable) {
                      key = ((RFWRecursiveClonable) key).cloneRecursive(clonedObjects);
                    }

                    if (mapValue instanceof RFWRecursiveClonable) {
                      clonedvalue.put(key, ((RFWRecursiveClonable) mapValue).cloneRecursive(clonedObjects));
                    } else {
                      clonedvalue.put(key, mapValue);
                    }
                  }

                  methodset.invoke(clonedvo, clonedvalue);
                } else {
                  throw new RFWCriticalException("O RFWRecursiveClone não suporta a Map do tipo '${0}'.", new String[] { gettedvalue.getClass().getCanonicalName() });
                }
              } else if (gettedvalue instanceof String || gettedvalue instanceof Long || gettedvalue instanceof Integer || gettedvalue instanceof BigDecimal || gettedvalue instanceof LocalDate || gettedvalue instanceof LocalDateTime || gettedvalue instanceof LocalTime || gettedvalue instanceof Byte || gettedvalue instanceof Character || gettedvalue instanceof Boolean || gettedvalue instanceof Double || gettedvalue instanceof Float || gettedvalue instanceof Date || gettedvalue instanceof Enum<?>) {
                // Objetos imutáveis não precisam ser clonados, já que eles não sofrem alteração basta apontar o novo vo para o mesmo objeto.
                methodset.invoke(clonedvo, gettedvalue);
              } else if (gettedvalue.getClass().isArray() && gettedvalue.getClass().getComponentType().isPrimitive()) {
                // Se éum array de tipos primitivos apenas clonamos o array para que o array não seja o mesmo, mas os "objetos" dentro são imutáveis, logo não precisamos clona-los
                Object clonedvalue = null;
                // Tenho um if para cada tipo porque não encontrei um jeito de clona-los ou copia-los sem fazer o cast
                if (gettedvalue instanceof byte[])
                  clonedvalue = ((byte[]) gettedvalue).clone();
                else if (gettedvalue instanceof boolean[])
                  clonedvalue = ((boolean[]) gettedvalue).clone();
                else if (gettedvalue instanceof int[])
                  clonedvalue = ((int[]) gettedvalue).clone();
                else if (gettedvalue instanceof float[])
                  clonedvalue = ((float[]) gettedvalue).clone();
                else if (gettedvalue instanceof long[])
                  clonedvalue = ((long[]) gettedvalue).clone();
                else if (gettedvalue instanceof double[])
                  clonedvalue = ((double[]) gettedvalue).clone();
                else if (gettedvalue instanceof char[])
                  clonedvalue = ((char[]) gettedvalue).clone();
                else if (gettedvalue instanceof short[]) clonedvalue = ((short[]) gettedvalue).clone();

                if (clonedvalue != null) {
                  methodset.invoke(clonedvo, clonedvalue);
                } else {
                  throw new RFWCriticalException("RFW_ERR_200452", new String[] { gettedvalue.getClass().toString(), gettedvalue.getClass().getComponentType().toString() });
                }
              } else if (gettedvalue instanceof Serializable) {
                try {
                  // Como um último recurso para garantir uma duplicação do objeto utilizamos o Serializable, serializando o objeto e desserializando, enganamos o java sobre manter a mesma referência de memória do objeto.
                  // O problema com este método é que se algum objeto dentro do serializable não for "serializável" ele vai explodir. Neste caso vamos lançar um erro círitico, para que o desenvolvedor melhore o objeto ou esta implementação do RecursiveClone
                  final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  final ObjectOutputStream oo = new ObjectOutputStream(byteStream);
                  oo.writeObject(gettedvalue);
                  oo.flush();
                  oo.close();

                  ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(byteStream.toByteArray()));
                  Object clonedValue = input.readObject();
                  input.close();

                  methodset.invoke(clonedvo, clonedValue);

                  // Vou logar como um erro a utilização do Serializable para chamar a atenção dos objetos que estão usando essa definição. Assim podemos melhorar a implementação desse método com o tempo
                  RFWLogger.logError("Utilizado Serializable Clone para o objeto: " + gettedvalue.getClass().toString());
                } catch (Exception e) {
                  throw new RFWCriticalException("RFW_ERR_200450", new String[] { gettedvalue.getClass().toString() }, e);
                }
              }
            }
          } catch (IllegalArgumentException e) {
            RFWLogger.logException(e);
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            RFWLogger.logException(e);
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            RFWLogger.logException(e);
            e.printStackTrace();
          }
        }
      }
    }
    return clonedvo;
  }
}
