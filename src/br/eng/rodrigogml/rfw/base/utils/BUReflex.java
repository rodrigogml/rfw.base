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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.vo.GVO;
import br.eng.rodrigogml.rfw.kernel.vo.RFWVO;

/**
 * Description: Classe com utilitários para tratar objetos apartir de reflexão.<br>
 *
 * @author Rodrigo Leitão
 * @since 3.0.0 (SET / 2009)
 * @deprecated Movido para o RFW.Kernel na classe RUReflex
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
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
