package br.eng.rodrigogml.rfw.base.preprocess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta.RFWMetaRelationshipField;
import br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta.RFWMetaStringField;
import br.eng.rodrigogml.rfw.base.dao.annotations.rfwmeta.RFWMetaRelationshipField.RelationshipTypes;
import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.utils.BUReflex;
import br.eng.rodrigogml.rfw.base.utils.BUString;
import br.eng.rodrigogml.rfw.base.vo.RFWVO;

/**
 * Description: Classe com métodos de pré processamento de variáveis para facilitar o processamento dos objetos.<br>
 *
 * @author Rodrigo Leitão
 * @since 2.0 (JUN / 2007)
 */
public final class PreProcess {

  /**
   * Esta Enum permite definir com uma constante o processimento a ser utilizado dentro das annotations e outras definições do validation.
   *
   * @author rodrigo
   *
   */
  public static enum PreProcessOption {
    /**
     * Não aplica nenhum pré-processamento no conteúdo.
     */
    NONE,
    /**
     * Passa o conteúdo da String para caixa alta.
     */
    STRING_UPPERCASE,
    /**
     * Limpa a String:
     * <li>remove todos os espaços múltiplos dentro do texto deixando apenas um único;
     * <li>remove espeços no começo e no fim da string;
     * <li>Caso a String resulte em "" o valor é convertido para NULO.
     */
    STRING_SPACESCLEAN_TO_NULL,
    /**
     * Caso a string seja maior que o valor permitido na RFWMetaAnotation, trunca o conteúdo no limite.
     */
    STRING_TRUNCATE,
    /**
     * Define para os campos data que esta deve ser atualizada a data atual do sistema.
     */
    DATE_TONOW,
  }

  /**
   * Construtor privado para classe estática.
   */
  private PreProcess() {
  }

  /**
   * Processa o VO completamente e recursivamente procurando pelas RFWMetaAnnotations com definição de atributo 'preProcess()' cujo tipo seja {@link PreProcessOption}.<br>
   * Todo o processamento é feito diretamente no objeto recebido. Por isso não é retorno deste método.
   *
   * @param vo VO a ser preprocessado para a validação.
   * @throws RFWException Este método não deve retornar exceptions pois não faz nenhum tipo de validação, apenas prepara a informação se ela existir. No entando devido as operações de reflexão, as exceptions podem ser lançadas.
   */
  public static <VO extends RFWVO> void processVO(VO vo) throws RFWException {
    if (vo != null) {
      // Iteramos todos os atributos do VO em busca das RFWMetaAnnotations
      final Field[] fields = vo.getClass().getDeclaredFields();
      for (Field field : fields) {
        Object value = null;
        try {
          value = BUReflex.getPropertyValue(vo, field.getName());
        } catch (Exception e) {
          // Esta exception pode ocorrer quando a propriedade não tem o método de GET! por isso só ignoramos
        }

        final PreProcessOption[] ppList = BUReflex.getRFWMetaAnnotationPreProcess(vo.getClass(), field.getName());
        if (ppList != null) {
          final Class<?> clazz = BUReflex.getPropertyTypeByObjectAndType(vo, field.getName());
          for (int i = 0; i < ppList.length; i++) {
            PreProcessOption preProcess = ppList[i];
            if (preProcess != PreProcessOption.NONE) {
              switch (preProcess) {
                case NONE:
                  // nada a fazer. Nem chega a entrar aqui pq o IF já filtra, só deixamos aqui para completar o switch. É importante que o NONE esteja no IF pq assim já evitamos que o SET seja feito quando não há alteração, deixando o código mais rápido.
                  break;
                case STRING_SPACESCLEAN_TO_NULL:
                  if (!String.class.isAssignableFrom(clazz)) throw new RFWCriticalException("O preProcess '${0}' não é válido para o tipo do atributo '${1}' da classe '${2}'", new String[] { preProcess.toString(), field.getName(), vo.getClass().getCanonicalName() });
                  value = processStringToNull((String) value);
                  break;
                case STRING_TRUNCATE:
                  if (!String.class.isAssignableFrom(clazz)) throw new RFWCriticalException("O preProcess '${0}' não é válido para o tipo do atributo '${1}' da classe '${2}'", new String[] { preProcess.toString(), field.getName(), vo.getClass().getCanonicalName() });
                  if (value != null) {
                    Annotation ann = BUReflex.getRFWMetaAnnotation(field);
                    int maxLength = 0;
                    if (ann instanceof RFWMetaStringField) {
                      maxLength = ((RFWMetaStringField) ann).maxlength();
                    } else {
                      throw new RFWCriticalException("O preProcess '${0}' não é tem suporte para a annotation '${3}' do atributo '${1}' da classe '${2}'", new String[] { preProcess.toString(), field.getName(), vo.getClass().getCanonicalName(), ann.getClass().getCanonicalName() });
                    }
                    value = BUString.truncate((String) value, maxLength);
                  }
                  break;
                case STRING_UPPERCASE:
                  if (!String.class.isAssignableFrom(clazz)) throw new RFWCriticalException("O preProcess '${0}' não é válido para o tipo do atributo '${1}' da classe '${2}'", new String[] { preProcess.toString(), field.getName(), vo.getClass().getCanonicalName() });
                  if (value != null) {
                    value = ((String) value).toUpperCase();
                  }
                  break;
                case DATE_TONOW:
                  if (LocalDate.class.isAssignableFrom(clazz)) {
                    value = RFW.getDate();
                  } else if (LocalTime.class.isAssignableFrom(clazz)) {
                    value = RFW.getTime();
                  } else if (LocalDateTime.class.isAssignableFrom(clazz)) {
                    value = RFW.getDateTime();
                  } else if (Date.class.isAssignableFrom(clazz)) {
                    // value = new Date();
                    throw new RFWCriticalException("Por definição o framework não permite mais utilizar o objeto 'java.util.Date'. Verifique seu código e substituia por LocalDate, LocalTime ou LocalDateTime!");
                  } else {
                    throw new RFWCriticalException("O preProcess '${0}' não é válido para o tipo do atributo '${1}' da classe '${2}'", new String[] { preProcess.toString(), field.getName(), vo.getClass().getCanonicalName() });
                  }
                  break;
              }
              try {
                BUReflex.setPropertyValue(vo, field.getName(), value, false);
              } catch (Exception e) {
                // Esta exception pode ocorrer quando não temos o método de SET, só ignoramos poide pode ser alguma propriedade só de leitura
              }
            }
          }
        }

        if (value != null) {
          // Verifica se temos a annotation de relationship, e se for um relacionamento de composição (em que o objeto é manipulado junto) temos de processar suas informações também
          final RFWMetaRelationshipField relAnn = field.getAnnotation(RFWMetaRelationshipField.class);
          if (relAnn != null && (relAnn.relationship() == RelationshipTypes.COMPOSITION || relAnn.relationship() == RelationshipTypes.MANY_TO_MANY)) {
            if (RFWVO.class.isAssignableFrom(value.getClass())) {
              PreProcess.processVO((RFWVO) value);
            } else if (Collection.class.isAssignableFrom(value.getClass())) {
              for (Object obj : (Collection<?>) value) {
                if (obj instanceof RFWVO) PreProcess.processVO((RFWVO) obj);
              }
            } else if (Map.class.isAssignableFrom(value.getClass())) {
              for (Object obj : ((Map<?, ?>) value).values()) {
                if (obj instanceof RFWVO) PreProcess.processVO((RFWVO) obj);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Processa a string passada, removendo espaços (do começo, do final e espaços duplos entre as palavras) e convertendo para null quando a string for vazia.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static String processStringToNull(String value) {
    if (value != null) {
      value = BUString.replaceFakeSpacesByUniqueSpace(value);
      value = BUString.replaceTabsByUniqueSpace(value);
      value = BUString.replaceDoubleSpaces(value);
      value = value.trim();
      if (value.length() == 0) {
        value = null;
      }
    }
    return value;
  }

  /**
   * Processa a string passada, removendo espaços e convertendo para uma string em branco (tamanho zero) quando a string tiver apenas espaços em branco ou for nula.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static String processStringToZeroLenght(String value) {
    if (value == null) {
      value = "";
    } else {
      value = BUString.replaceDoubleSpaces(value).trim();
    }
    return value;
  }

  /**
   * Processa qualquer número inteiro para nulo, caso seja menor ou igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Integer processIntegerToNullIfZeroOrNegative(Integer value) {
    if (value != null && value <= 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número inteiro para nulo, caso seja menor que zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Integer processIntegerToNullIfNegative(Integer value) {
    if (value != null && value < 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número float para nulo, caso seja menor ou igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Float processFloatToNullIfZeroOrNegative(Float value) {
    if (value != null && value <= 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número BigDecimal para nulo, caso seja menor ou igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static BigDecimal processBigDecimalToNullIfZeroOrNegative(BigDecimal value) {
    if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número double para nulo, caso seja menor ou igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Double processDoubleToNullIfZeroOrNegative(Double value) {
    if (value != null && value <= 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número Long para nulo, caso seja menor ou igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Long processLongToNullIfZeroOrNegative(Long value) {
    if (value != null && value <= 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número Long para nulo, caso seja menor que zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Long processLongToNullIfNegative(Long value) {
    if (value != null && value < 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número float para zero, caso seja nulo ou negativo.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Float processFloatToZeroIfNullOrNegative(Float value) {
    if (value == null || value < 0) {
      value = 0f;
    }
    return value;
  }

  /**
   * Processa qualquer número double para zero, caso seja nulo ou negativo.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Double processDoubleToZeroIfNullOrNegative(Double value) {
    if (value == null || value < 0) {
      value = 0d;
    }
    return value;
  }

  /**
   * Processa qualquer número float para nulo, caso seja igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Float processFloatToNullIfZero(Float value) {
    if (value != null && value == 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número float para nulo, caso seja menor que zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Float processFloatToNullIfNegative(Float value) {
    if (value != null && value < 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número BigDecimal para nulo, caso seja menor que zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static BigDecimal processBigDecimalToNullIfNegative(BigDecimal value) {
    if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número double para nulo, caso seja igual a zero.
   *
   * @param value valor a ser processado
   * @return valor processado
   */
  public static Double processDoubleToNullIfZero(Double value) {
    if (value != null && value == 0) {
      value = null;
    }
    return value;
  }

  /**
   * Processa qualquer número float para conter apenas duas casas decimais. Arredonda para o lado mais justo.
   *
   * @param value Valor para formatar como monetário.
   * @return Valor arredondado para apenas 2 casas decimais.
   */
  public static Float processCurrency(Float value) {
    if (value != null) {
      value = (float) Math.round(value * 100) / 100;
    }
    return value;
  }

  /**
   * Processa qualquer número BigDecimal para conter apenas duas casas decimais. Arredonda usando a definição padrão do sistema (classe {@link RFW}).
   *
   * @param value Valor a ser arredondado.
   * @return Valor arredondado.
   */
  public static BigDecimal processCurrency(BigDecimal value) {
    if (value != null) {
      value = value.setScale(2, RFW.getRoundingMode());
    }
    return value;
  }

  /**
   * Processa um array de 'byte' para nulo, caso tenha tamanho zero.
   *
   * @param value Array para ser processado.
   * @return próprio array, ou null caso o array tenha tamanho zero.
   */
  public static byte[] processByteArrayToNullIfZeroLength(byte[] value) {
    if (value != null) {
      if (value.length == 0) {
        value = null;
      }
    }
    return value;
  }

  /**
   * Procesa uma data e retorna null caso a data seja posterior a atual.
   *
   * @param date data a ser processada.
   * @return Retorna a data se ela for passada, ou null se for uma data futura.
   */
  public static Date processDateToNullIfFutureDate(Date date) {
    if (date != null) {
      if (date.compareTo(new Date()) > 0) {
        date = null;
      }
    }
    return date;
  }

  /**
   * Valida se o valor passado é positivo.
   *
   * @throws RFWException
   */
  public static void requiredPositive(Long value) throws RFWException {
    requiredPositive(value, "Esperado um valor positivo.");
  }

  /**
   * Valida se o valor passado é positivo.
   *
   * @throws RFWException
   */
  public static void requiredPositive(Long value, String message) throws RFWException {
    if (value == null || value <= 0) throw new RFWValidationException(message);
  }

  /**
   * Valida se o objeto passado não é nulo. Caso seja lança uma {@link RFWValidationException} com mensagem padrão.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNonNull(Object value) throws RFWException {
    requiredNonNull(value, "O valor não pode ser nulo!");
  }

  /**
   * Valida se o objeto passado não é nulo. Caso seja lança {@link RFWValidationException} com mensagem personalizada.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNonNull(Object value, String msg) throws RFWException {
    if (value == null) throw new RFWValidationException(msg);
  }

  /**
   * Valida se o objeto passado não é nulo. Caso seja lança exceção com mensagem padrão.<br>
   * Lança uma exception Crítica.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNonNullCritical(Object value) throws RFWException {
    requiredNonNullCritical(value, "O valor não pode ser nulo!");
  }

  /**
   * Valida se o objeto passado não é nulo. Caso seja lança exceção com mensagem personalizada.<br>
   * Lança uma exception Crítica.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNonNullCritical(Object value, String msg) throws RFWException {
    if (value == null) throw new RFWCriticalException(msg);
  }

  /**
   * Valida se o objeto passado é nulo. Caso seja lança uma {@link RFWValidationException} com mensagem padrão.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNull(Object value) throws RFWException {
    requiredNull(value, "O valor deve ser nulo!");
  }

  /**
   * Valida se o objeto passado é nulo. Caso seja lança {@link RFWValidationException} com mensagem personalizada.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNull(Object value, String msg) throws RFWException {
    if (value != null) throw new RFWValidationException(msg);
  }

  /**
   * Valida se o objeto passado é nulo. Caso seja lança exceção com mensagem padrão.<br>
   * Lança uma exception Crítica.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNullCritical(Object value) throws RFWException {
    requiredNullCritical(value, "O valor deve ser nulo!");
  }

  /**
   * Valida se o objeto passado não é nulo. Caso seja lança exceção com mensagem personalizada.<br>
   * Lança uma exception Crítica.
   *
   * @param value Objeto a ser testado.
   */
  public static void requiredNullCritical(Object value, String msg) throws RFWException {
    if (value != null) throw new RFWCriticalException(msg);
  }

  /**
   * Valida se dois objetos são "equals()".<br>
   * Se ambos forem nulos o método considera que são válidos pois NULL == NULL.<br>
   * Lança uma exception Crítica.
   *
   * @param obj1 Objeto para teste.
   * @param obj2 Objeto para teste.
   * @param msg Mesagem de Erro da Exception.
   * @throws RFWException
   */
  public static void requiredEqualsCritical(Object obj1, Object obj2, String msg) throws RFWException {
    if (obj1 == null && obj2 == null) {
    } else {
      if (obj1 == null || !obj1.equals(obj2)) {
        throw new RFWCriticalException(msg);
      }
    }
  }

  /**
   * Garante que o array recebido não é nulo e nem vazio, tem o tamanho maior ou igual a 1.<br>
   * Note que este método não verificará se o conteúdo dentro do array é nulo, só se o array é nulo e de tamanho maior que zero.
   *
   *
   * @param array Array para validação
   * @param msg Mensagem da Exception caso não passe na validação.
   * @throws RFWCriticalException
   */
  public static <T> void requiredNonEmptyCritical(T[] array, String msg) throws RFWException {
    if (array == null || array.length == 0) throw new RFWCriticalException(msg);
  }

  /**
   * Valida se um valor está entre dois valores INCLUSIVE, ou seja a condição é "minValue <= value <= maxValue".<br>
   * Lança uma exception Crítica.
   *
   * @param value Valor a ser verificado. Se nulo lança a exception como se a condição tivesse dado falso.
   * @param minValue Menor valor aceito. Não pode ser nulo.
   * @param maxValue Maior valor aceito. Não pode ser nulo.
   * @param msg Mensagem personalizada de erro.
   * @throws RFWException
   */
  public static void requiredBetweenCritical(Number value, Number minValue, Number maxValue, String msg) throws RFWException {
    requiredNonNullCritical(minValue);
    requiredNonNullCritical(maxValue);
    if (value == null || value.doubleValue() < minValue.doubleValue() || value.doubleValue() > maxValue.doubleValue()) throw new RFWCriticalException(msg);
  }

  /**
   *
   * @param values Valores para análise.
   * @return retorna o primeiro objeto não nulo do array
   */
  @SafeVarargs
  public static <T> T coalesce(T... values) {
    for (T i : values)
      if (i != null) return i;
    return null;
  }

  /**
   * Valida se a coleção não é nula nem vazia. Caso a validação falhe uma {@link RFWCriticalException} é lançada.
   *
   * @param <T>
   * @param list Coleção a ser validada.
   * @throws RFWValidationException
   */
  public static <T> void requiredNonNullNorEmpty(Collection<T> list) throws RFWValidationException {
    if (list == null) {
      throw new RFWValidationException("A lista não pode ser nula ou vazia!");
    } else if (list.size() == 0) {
      throw new RFWValidationException("A lista não pode ser vazia!");
    }
  }

  /**
   * Valida se a coleção não é nula nem vazia. Caso a validação falhe uma {@link RFWCriticalException} é lançada.
   *
   * @param <T>
   * @param list Coleção a ser validada.
   * @param message Mensagem para a Exception.
   * @throws RFWValidationException
   */
  public static <T> void requiredNonNullNorEmpty(Collection<T> list, String message) throws RFWValidationException {
    if (list == null || list.size() == 0) {
      throw new RFWValidationException(message);
    }
  }

  /**
   * Valida se a coleção não é nula nem vazia. Caso a validação falhe uma {@link RFWCriticalException} é lançada.
   *
   * @param <T>
   * @param list Coleção a ser validada.
   * @throws RFWCriticalException
   */
  public static <T> void requiredNonNullNorEmptyCritical(Collection<T> list) throws RFWCriticalException {
    if (list == null) {
      throw new RFWCriticalException("A lista não pode ser nula ou vazia!");
    } else if (list.size() == 0) {
      throw new RFWCriticalException("A lista não pode ser vazia!");
    }
  }

  /**
   * Valida se a coleção não é nula nem vazia. Caso a validação falhe uma {@link RFWCriticalException} é lançada.
   *
   * @param <T>
   * @param list Coleção a ser validada.
   * @param message Mensagem para a Exception.
   * @throws RFWCriticalException
   */
  public static <T> void requiredNonNullNorEmptyCritical(Collection<T> list, String message) throws RFWCriticalException {
    if (list == null || list.size() == 0) {
      throw new RFWCriticalException(message);
    }
  }
}
