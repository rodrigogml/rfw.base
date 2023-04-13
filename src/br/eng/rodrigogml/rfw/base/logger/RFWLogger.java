package br.eng.rodrigogml.rfw.base.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import br.eng.rodrigogml.rfw.base.RFW;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWValidationGroupException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogEntry.RFWLogSeverity;
import br.eng.rodrigogml.rfw.base.preprocess.PreProcess;
import br.eng.rodrigogml.rfw.base.utils.BUReflex;

/**
 * Description: Esta classe gerencia as entradas de Log do sistema.<br>
 * Novas entradas podem ser feitas utilizanto os m�todos est�ticos de "log*" desta classe.<br>
 * As entradas ser�o acumuladas na mem�ria, esta classe n�o persiste nem salva seu conte�do em lugar algum.<br>
 * <b>� necess�rio que o sistema implemente algum tupo de thread para consumir as entradas deste objeto e persisti-las.
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 */
public class RFWLogger {

  /**
   * Lista com os registros logados.
   */
  private static LinkedList<RFWLogEntry> entriesList = new LinkedList<RFWLogEntry>();

  /**
   * De quanto em quanto tempo a Thread deve checar se h� entradas antigas na mem�ria do sistema. <br>
   * Tempo em SEGUNDOS. Valor padr�o 5 minutos.
   */
  private static long timeToClean = 300;

  /**
   * A partir de quanto tempo a desde o momento do seu registro uma entrada pode ficar em mem�ria aguardando persist�ncia.<br>
   * Tempo em SEGUNDOS. Valor padr�o 10 minutos.<br>
   * <bR>
   * Note que o tempo � avaliado em rela��o ao hor�rio padr�o do sistema {@link RFW#getDateTime()}. Caso o log esteja sendo feito por outro sistema, ou com um TimeZone diferente, talvez seja necess�rio implementar uma configura��o aqui no {@link RFWLogger} para definir o TimeZone de compara��o das entradas.
   */
  private static long timeToLive = 600;

  /**
   * Refer�ncia para a Thread de limpeza.
   */
  private static Thread cleannerThread = null;

  /**
   * Construtor privado para classe de m�todos est�ticos
   */
  private RFWLogger() {
  }

  private static void startCleannerThread() {
    // Criamos uma Thread para garatir que objetos n�o ser�o esquecidos na mem�ria.
    cleannerThread = new Thread("### RFWLogger Cleanner") {
      @Override
      public void run() {
        do {
          try {
            if (RFWLogger.entriesList.size() > 0) {
              List<RFWLogEntry> dumpList = RFWLogger.entriesList.parallelStream().filter(entry -> Duration.between(entry.getTime(), RFW.getDateTime()).getSeconds() > timeToLive).collect(Collectors.toList());
              RFWLogger.removeEntries(dumpList);
            }
          } catch (Throwable t) {
            RFWLogger.logException(t);
          }

          try {
            Thread.sleep(timeToClean * 1000);
          } catch (Exception e) {
          }
        } while (!RFW.isShuttingDown()); // Thread Daemon, queremos ela rodando para sempre, at� que o RFW sinalize que devemos desligar o framework
      }
    };
    cleannerThread.setPriority(Thread.MIN_PRIORITY);
    cleannerThread.setDaemon(true);
    cleannerThread.start();
  }

  /**
   * Realiza o log com a prioridade DEBUG.
   *
   * @param msg Mensagem a ser registrada
   */
  public final static void logDebug(String msg) {
    log(RFWLogSeverity.DEBUG, msg, null, null);
  }

  /**
   * Realiza o log com a prioridade INFO
   *
   * @param msg Mensagem a ser registrada
   */
  public final static void logInfo(String msg) {
    log(RFWLogSeverity.INFO, msg, null, null);
  }

  /**
   * Realiza o log com a prioridade WARN
   *
   * @param msg Mensagem a ser registrada
   */
  public final static void logWarn(String msg) {
    log(RFWLogSeverity.WARN, msg, getInvoker(), null);
  }

  /**
   * Realiza o log de uma mensagem para os desenvolvedores, registrando alguma informa��o para melhoria do c�digo no futuro.
   *
   * @param msg Mensagem a ser registrada
   */
  public final static void logImprovement(String msg) {
    log(RFWLogSeverity.DEV, msg, null, null);
  }

  /**
   * Realiza o log com a prioridade ERROR
   *
   * @param msg Mensagem a ser registrada
   */
  public final static void logError(String msg) {
    log(RFWLogSeverity.ERROR, msg, getInvoker(), null);
  }

  /**
   * Faz o log do conte�do de um objeto.
   *
   * @param msg Mensagem a ser colocada no registro do Log.
   * @param obj Objeto a ser impresso no anexo do Log.
   */
  public final static void logObject(String msg, Object obj) {
    String print = BUReflex.printObject(obj);
    log(RFWLogSeverity.OBJECT, msg, print + "\r\n========INVOKER:===========\r\n" + getInvoker(), null);
  }

  /**
   * Faz o log de uma exception.
   *
   * @param e Exce��o a ser Logada.
   */
  public synchronized final static void logException(Throwable e) {
    if (!(e instanceof RFWException) || !((RFWException) e).getLogged()) {
      RFWLogSeverity severity = RFWLogSeverity.EXCEPTION;
      if (e instanceof RFWValidationException || e instanceof RFWValidationGroupException) {
        severity = RFWLogSeverity.VALIDATION;
      }
      String exPoint = e.getStackTrace()[0].toString();
      log(severity, e.getLocalizedMessage(), convertExceptionToString(e), exPoint);
    }
  }

  /**
   * Realiza o log com a prioridade DEBUG.
   *
   * @param msg Mensagem a ser registrada
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public final static void logDebug(String msg, String... tags) {
    log(RFWLogSeverity.DEBUG, msg, null, null, tags);
  }

  /**
   * Realiza o log com a prioridade INFO
   *
   * @param msg Mensagem a ser registrada
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public final static void logInfo(String msg, String... tags) {
    log(RFWLogSeverity.INFO, msg, null, null, tags);
  }

  /**
   * Realiza o log com a prioridade WARN
   *
   * @param msg Mensagem a ser registrada
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public final static void logWarn(String msg, String... tags) {
    log(RFWLogSeverity.WARN, msg, getInvoker(), null, tags);
  }

  /**
   * Realiza o log de uma mensagem para os desenvolvedores, registrando alguma informa��o para melhoria do c�digo no futuro.
   *
   * @param msg Mensagem a ser registrada
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public final static void logImprovement(String msg, String... tags) {
    log(RFWLogSeverity.DEV, msg, null, null, tags);
  }

  /**
   * Realiza o log com a prioridade ERROR
   *
   * @param msg Mensagem a ser registrada
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public final static void logError(String msg, String... tags) {
    log(RFWLogSeverity.ERROR, msg, getInvoker(), null, tags);
  }

  /**
   * Faz o log do conte�do de um objeto.
   *
   * @param msg Mensagem a ser colocada no registro do Log.
   * @param obj Objeto a ser impresso no anexo do Log.
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public final static void logObject(String msg, Object obj, String... tags) {
    String print = BUReflex.printObject(obj);
    log(RFWLogSeverity.OBJECT, msg, print + "\r\n========INVOKER:===========\r\n" + getInvoker(), null, tags);
  }

  /**
   * Faz o log de uma exception.
   *
   * @param e Exce��o a ser Logada.
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  public synchronized final static void logException(Throwable e, String... tags) {
    if (!(e instanceof RFWException) || !((RFWException) e).getLogged()) {
      RFWLogSeverity severity = RFWLogSeverity.EXCEPTION;
      if (e instanceof RFWValidationException || e instanceof RFWValidationGroupException) {
        severity = RFWLogSeverity.VALIDATION;
      }
      String exPoint = e.getStackTrace()[0].toString();
      log(severity, e.getLocalizedMessage(), convertExceptionToString(e), exPoint, tags);
    }
  }

  /**
   * M�todo que cria os objetos de Log.
   *
   * @param severity Severidade do registro
   * @param msg Mensagem a ser registrada no log. Esta mensagem deve ser curta e o mais explicativa poss�vel.
   * @param content Conte�do em caso de conte�do anexo. (Grande Volume de Dados).
   * @param exPoint Define o ponto da exception, assim consegimos encontrar mais casos da mesma exception
   * @param tags permite que se adicione tags particulares ao Log. Tenha em mente que Tags s�o utilizadas para ajudar a filtrar v�rios eventos de uma mesma natureza, n�o jogue informa��es que s� aparecer�o em um �nico evento por vez nas tags. Cria um log de debug ou info para isso.
   */
  private final static void log(RFWLogSeverity severity, String msg, String content, String exPoint, String... tags) {
    RFWLogEntry log = null;
    try {
      if (RFWLogger.cleannerThread == null) startCleannerThread();

      log = new RFWLogEntry();
      log.setTime(RFW.getDateTime());
      log.setSeverity(severity);
      log.setExPoint(exPoint);
      log.setMessage(msg);
      log.setContent(content);

      if (tags != null) {
        for (String tag : tags) {
          log.addTag(tag);
        }
      }

      // Cria tags automaticamente
      if (RFW.getSystemName() != null) log.addTag(RFW.getSystemName());
      log.addTag("Thread:" + Thread.currentThread().getId() + "-" + Thread.currentThread().toString());

      // Sincroniza a adi��o do Log para que gerenciar a concorr�ncia com a remo��o dos logs j� persistidos.
      synchronized (RFWLogger.entriesList) {
        RFWLogger.entriesList.add(log);
      }
      RFW.pDev(log.toString());
    } catch (Throwable e) {
      System.out.println("###############################################################");
      System.out.println("###################### ERRO NO RFWLOGGER ######################");
      System.out.println("###############################################################");
      if (log != null) {
        try {
          // RFW.pDev(log.toString());
        } catch (Throwable e1) {
        }
      }
      System.out.println("---------------------------------------------------------------");
      // e.printStackTrace();

      System.out.println("###############################################################");
      System.out.println("###############################################################");
    }

  }

  /**
   * Este m�todo retorna quem foi que chamou a classe {@link RFWLogger}, para registrar de maneira f�cil onde no c�digo foi registrado cada registro.
   */
  private static final String getInvoker() {
    return BUReflex.getInvoker(3, 10);
  }

  /**
   * Converte uma exception em um formato de texto para ser anexado ao LOG.
   */
  public static final String convertExceptionToString(Throwable e) {
    StringWriter sw = new StringWriter();
    sw.write("<MESSAGE>");
    if (e instanceof RFWException) sw.write(((RFWException) e).getExceptioncode() + " - ");
    sw.write(e.getLocalizedMessage() + "</MESSAGE>\r\n");
    // Verifica os paremetros para exibir no LOG
    if (e instanceof RFWException && ((RFWException) e).getParams() != null) {
      for (String p : ((RFWException) e).getParams()) {
        sw.write("<PARAMETER>" + p + "</PARAMETER>\r\n");
      }
    }
    sw.write("<STACK>\r\n");
    e.printStackTrace(new PrintWriter(sw));
    sw.write("</STACK>\r\n");
    sw.flush();
    return sw.toString();
  }

  /**
   * Recupera a lista de registros aguardando serem consumidos. Note que � retornada uma lista clonada, qualquer altera��o nessa lista n�o altera a lista de registros do RFWLogger.<br>
   * Note tamb�m que essa lista retorna uma c�pia dos itens, mas n�o os remove do {@link RFWLogger}. Para remover as entradas e liberar recursos passe as entradas que devem ser removidas no m�todo {@link #removeEntries(List)}.
   *
   * @return Lista com todos os objetos na mem�ria do {@link RFWLogger}
   */
  public static ArrayList<RFWLogEntry> getEntriesList() {
    return new ArrayList<RFWLogEntry>(RFWLogger.entriesList);
  }

  /**
   * Este m�todo remove da lista do RFWLogger todas as entradas passadas.<br>
   * Note que este m�todo � sincronizado para que n�o ter problemas de concorr�ncia nem a perda de objetos.<br>
   * <br>
   * A recupera��o e remo��o � feita em duas etapas para que caso o sistema falhe em persistir o registro, ainda seja poss�vel encontra-lo dentro do {@link RFWLogger}.
   *
   * @param entries Lista com os objetos que devem ser removidos.
   */
  public static void removeEntries(List<RFWLogEntry> entries) {
    if (entries != null && entries.size() > 0) {
      synchronized (RFWLogger.entriesList) {
        entriesList.removeAll(entries);
      }
    }
  }

  /**
   * Recupera a lista de registros aguardando serem consumidos e j� os remove da do m�dulo. Note que � retornada uma lista clonada, qualquer altera��o nessa lista n�o altera a lista de registros do {@link RFWLogger}.<br>
   *
   * @return Lista com todos os objetos que foram encontrados e j� removidos da mem�ria.
   */
  public static ArrayList<RFWLogEntry> popEntriesList() {
    synchronized (RFWLogger.entriesList) {
      ArrayList<RFWLogEntry> entries = new ArrayList<RFWLogEntry>(RFWLogger.entriesList);

      if (entries != null && entries.size() > 0) {
        entriesList.removeAll(entries);
      }
      return entries;
    }
  }

  public static long getTimeToClean() {
    return timeToClean;
  }

  public static void setTimeToClean(long timeToClean) throws RFWException {
    PreProcess.requiredPositive(timeToClean);
    RFWLogger.timeToClean = timeToClean;
  }

  public static long getTimeToLive() {
    return timeToLive;
  }

  public static void setTimeToLive(long timeToLive) throws RFWException {
    PreProcess.requiredPositive(timeToLive);
    RFWLogger.timeToLive = timeToLive;
  }

  /**
   * For�a que a Thread de Cleanning acorde (caso esteja no sleep), testando assim se o sistema est� em shutdown e finalizar.<br>
   * Caso n�o esteja em shuttingdown, acaba for�ando uma execu��o de cleanning
   */
  public static void wakeUpCleannerThread() {
    try {
      if (RFWLogger.cleannerThread != null) RFWLogger.cleannerThread.interrupt();
    } catch (Exception e) {
    }
  }
}
