package br.eng.rodrigogml.rfw.base.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler.CustomMeasureUnit;
import br.eng.rodrigogml.rfw.base.measureruler.MeasureRuler.MeasureUnit;
import br.eng.rodrigogml.rfw.base.utils.BUReflex;
import br.eng.rodrigogml.rfw.base.utils.RUString;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: Classe est�tica para opera��es com o Bundle principal da aplica��o.<br>
 *
 * @author Rodrigo Leit�o
 * @since 10.0.0 (25 de jul de 2018)
 */
public class RFWBundle {

  /**
   * Refer�ncia para o Bundle.
   */
  private static Properties bundle = null;

  /**
   * Construtor privado para Classe Est�tica
   */
  private RFWBundle() {
  }

  /**
   * Recupera um bundle dos arquivos de properties carregas, e substitui seus par�metros.
   *
   * @param key Chave do Bundle para recuperar mensagem.
   * @param params Par�metros que ser�o substituidos na mensagem recuperada do arquivo de bundle. Os par�metros ser�o substituidos na mensagem conforme o padr�o ${i}, onde i � o �ndice do par�metro recebido.
   * @return Mengam do bundle com as substitui��es dos par�metros.
   */
  public static String get(String key, String... params) {
    String msg = null;
    try {
      // Busca a causa original, primeira exception
      if (key != null) msg = getReader().getProperty(key);

      // Substitui os parametros
      if (msg != null && params != null) {
        for (int i = 0; i < params.length; i++) {
          if (params[i] == null) params[i] = "<null>";
          msg = msg.replace("${" + i + "}", params[i]);
        }
      }

    } catch (Throwable e) {
      RFWLogger.logException(e, "RFWLogger");
      // N�o faz nada, s� garante que se falharmos em localizar a msg vamos garantir que o m�todo n�o falhe
    }
    return msg;
  }

  /**
   * Recupera a mensagem formatada a partir de uma RFWException.
   *
   * @param t Throwable para tecuperar a mensagem.
   * @return Texto com a mensagem do Bundle j� decodificada para exibi��o.
   */
  public static String get(Throwable t) {
    String msg = null;
    try {
      // Busca a causa original, primeira exception
      Throwable cause = t;
      while (cause.getCause() != null && cause != cause.getCause()) {
        cause = cause.getCause();
      }

      if (t instanceof RFWException) {
        RFWException e = (RFWException) t;
        msg = e.getExceptionCode();
        if (e.getExceptionCode() != null && e.getExceptionCode().matches("[A-Z0-9_]+_[0-9]{6}")) {
          String bundle = getReader().getProperty(e.getExceptionCode());
          // Se encontrou algo no bundle
          if (bundle != null) {
            msg = bundle;
            if (RFW.isDevelopmentEnvironment()) msg = msg + " [" + e.getExceptionCode() + "]";
          }
        }

        // Substitui os parametros
        final String[] params = e.getParams();
        if (msg != null && params != null) {
          for (int i = 0; i < params.length; i++) {
            if (params[i] == null) params[i] = "<null>";
            msg = msg.replace("${" + i + "}", params[i]);
          }
        }

        if (msg != null) {
          // Substitui vari�veis da exception de valida��o
          if (e instanceof RFWValidationException) {
            final RFWValidationException ve = (RFWValidationException) e;
            if (ve.getClassName() != null && ve.getFieldName() != null) {
              StringBuilder buff = new StringBuilder();
              for (int i = 0; i < ve.getFieldName().length; i++) {
                buff.append(ve.getFieldName()[i]).append(", ");
              }
              if (buff.length() > 0) msg = msg.replaceAll("\\$\\{fieldname\\}", buff.delete(buff.length() - 2, buff.length()).toString());
              msg = msg.replaceAll("\\$\\{classname\\}", ve.getClassName());
            }
          }
          msg = msg.replaceAll("\\$\\{cause\\}", cause.getClass().getCanonicalName());
        }
      } else {
        // Se n�o � uma RFWException tentamos montar a melhos msg de erro que conseguirmos baseano na exception do JAVA
        msg = cause.getClass().getCanonicalName() + (cause.getMessage() != null ? ": " + cause.getMessage() : "") + " at " + cause.getStackTrace()[0];
      }
      if (RFW.isDevelopmentEnvironment()) {
        // Se estamos no desenvolvimento vamos validar se a mensagem foi totalmente substituida e avisamos no console sobre o problema
        if (msg.matches(".*(\\$\\{\\w*\\}).*")) {
          RFW.pDev("N�o foi poss�vel encontrar valores para todos os campos na mensagem da Exception:");
          RFW.pDev(t);
        }
      }
    } catch (Throwable e1) {
      // N�o faz nada, s� garante que se falharmos em localizar a msg vamos garantir que o m�todo n�o falhe
      e1.printStackTrace();
    }
    return msg;
  }

  /**
   * Obtem a inst�ncia do Leitor do arquivo de Bundle. Instanceia ela se for a primeira chamada.
   *
   * @throws RFWException
   */
  private static Properties getReader() throws RFWException {
    if (bundle == null) {
      loadBundle(null); // tentamos carregar com o nome padr�o
    }
    return RFWBundle.bundle;
  }

  /**
   * Carrega um arquivo de Bundle para que o RFWBundle possa encontrar seu conte�do chave/valor pelo sistema todo.
   *
   * @param bundleName nome do arquivo de bundle. Normalmente o arquivo de bundle � colocado na raiz do c�digo fonte, e se passa apenas o nome do arquivo e exten��o. Ex: "rfwbundle.properties".
   * @throws RFWException
   */
  public static void loadBundle(String bundleName) throws RFWException {
    if (bundle == null && bundleName == null) {
      loadBundle("rfwbundle.properties"); // garante que a primeira chamada seja sempre com o bundleName do arquivo principal do RFWDeprec
      return;
    } else if (bundle != null && bundleName == null) {
      throw new RFWCriticalException("O nome do bundle n�o pode ser nulo!");
    }

    try (InputStream input = BUReflex.getResourceAsStream(bundleName)) {
      if (input != null) {
        if (bundle == null) {
          bundle = new Properties();
        }
        bundle.load(input);
      }
    } catch (IOException e) {
      throw new RFWCriticalException("N�o foi poss�vel encontrar o bundle '" + bundleName + "'.", e);
    }
  }

  /**
   * Recupera um Bundle definido para uma enumeration. Note que a chave da enumeration � definida conforme seu package, class, nome da enum e valor da enum.<br>
   * Para mais informa��es veja o m�todo {@link BUString#getEnumKey(Enum)} <br>
   * Caso o conte�do n�o seja encontrado no bundle, � lan�ado um {@link RFWLogger#logWarn(String)}
   *
   * @param value Enumeration
   * @return Bundle do enumeration, ou a pr�pria enumeration (caminho completo do objeto) caso a chave n�o seja encontrada no bundle.
   */
  public static String get(Enum<?> value) {
    String key = RUString.getEnumKey(value);
    String v = get(key);
    if (value != null && v == null) {
      RFWLogger.logWarn("A chave enum foi solicitada mas n�o encontramos um bundle: " + key);
      return key;
    }
    return v;
  }

  /**
   * Recupera um Bundle definido para uma das enumera��esd e MeasureUnit. De forma geral {@link MeasureUnit} funciona como uma enumeration, por�m inst�ncias do {@link CustomMeasureUnit} precisam de um tratamento diferente, seu texto � montado a partir das informa��es do pr�prio objeto.
   *
   * @param measureUnit Valor da MeasureUnit para recuperar o Bundle.
   * @return Texto para o usu�rio identificar a Unidade de medida.
   */
  public static String get(MeasureUnit measureUnit) {
    if (measureUnit instanceof CustomMeasureUnit) {
      CustomMeasureUnit mu = ((CustomMeasureUnit) measureUnit);
      return mu.getSymbol() + " (" + mu.name() + ")";
    } else {
      return get((Enum<?>) measureUnit);
    }
  }

}
