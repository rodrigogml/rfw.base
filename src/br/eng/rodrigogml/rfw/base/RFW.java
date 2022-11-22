package br.eng.rodrigogml.rfw.base;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import br.eng.rodrigogml.rfw.base.bundle.RFWBundle;
import br.eng.rodrigogml.rfw.base.eventdispatcher.EventDispatcher;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;
import br.eng.rodrigogml.rfw.base.logger.RFWLogger;
import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;
import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionBackOperation;
import br.eng.rodrigogml.rfw.base.utils.BUFile;;

/**
 * Description: Classe utilit�ria geral do RFW com m�todos utilit�rios comuns gen�ricos.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 */
public class RFW {

  /**
   * Constante com o BigDecimal de "100", para evitar sua constru��o o tempo todo.
   */
  public static final BigDecimal BIGHUNDRED = new BigDecimal("100");

  /**
   * M�todo padr�o de arredondamento do sistema.<br>
   * Valor Padr�o inicial: RoundingMode.HALF_EVEN.
   */
  private static RoundingMode roundingMode = RoundingMode.HALF_EVEN;

  /**
   * Locale padr�o do sistema.<br>
   * Valor Padr�o inicial: Locale("pt", "BR").
   */
  private static Locale locale = new Locale("pt", "BR");

  /**
   * ZoneID padr�o do sistema.<br>
   * Valor padr�o inicial: "America/Sao_Paulo.
   */
  private static ZoneId zoneId = ZoneId.of("America/Sao_Paulo");

  /**
   * Identifica��o do sistema.
   */
  private static String systemName = null;

  /**
   * Indicador se foi solicitado que o sistema do RFW deve finalizar.
   */
  private static boolean shuttingDown = false;

  /**
   * Construtor privado para uma classe completamente est�tica
   */
  private RFW() {
  }

  /**
   * Ao chamar este m�todo, todos os servi�os do RFW ser�o sinalizados para que finalizem seus servi�os e Threads em andamento o mais r�pido poss�vel.<br>
   * A chamada deste m�todo � irrevers�vel.
   */
  public static void shutdownFW() {
    RFW.shuttingDown = true;
    RFWLogger.wakeUpCleannerThread();
  }

  /**
   * Inicializa o valor padr�o do ZoneId utilizado no Sistema. Gera efeitos em todo o sistema que utilizar a mesma inst�ncia est�tica do RFW.<br>
   * Valor padr�o inicial: "America/Sao_Paulo".
   *
   * @param zoneId
   */
  public static void initializeZoneID(ZoneId zoneId) {
    RFW.zoneId = zoneId;
  }

  /**
   * Inicializa o servi�o do RFWLogger configurando seus par�metros.<Br>
   * Note que este m�todo de fato s� define as configura��es do RFW. Suas thread s� s�o iniciadas a partir do primeiro log() realizado.<br>
   * <br>
   * <b>ATEN��O: </B>Depois de inicializado, o RFWLogger exige que o m�todo {@link #shutdownFW()} seja chamado para que suas Threads sejam encerradas.
   *
   * @param timeToLive Define o tempo que uma entrada de LOG pode ficar na mem�ria aguardando para ser consumida. Depois de passado do periodo definido aqui, a o pr�prio {@link RFWLogger} descartar� a entrada do Log.<br>
   *          Tempo definido em segundos.
   * @param timeToClean Define de quanto em quanto tempo o {@link RFWLogger} executar� a limpeza das entradas que n�o foram consumidas. Note que conforme os valores definidos uma entrada de log ficar� aguardando ser consumida descartada entre no m�nimo pelo valor definido como timeToLive e no m�ximo at� timeToLive + timeToClean.<br>
   *          Tempo definido em segundos.
   * @throws RFWException
   */
  public static void initializeFWLogger(long timeToLive, long timeToClean) throws RFWException {
    RFWLogger.setTimeToLive(timeToLive);
    RFWLogger.setTimeToClean(timeToClean);
  }

  /**
   * Inicializa o servi�o de Controle de Sess�o do RFW.
   *
   * @param timeToLive Tempo que a sess�o sobrevive sem atividade at� ser descartada.<br>
   *          Valor em segundos.
   * @param backOperation Implementa��o da interface que far� a autentica��o dos usu�rios e acesso por tokens.
   * @param tokenPrefix Prefixo dos tokens de acesso. Quando o sistema permite que m�quinas se conectem � fachada, o acesso pode ser feito por um token ao inv�s do usu�rio/senha. Mas para que o {@link SessionManager} identifique que se trata de um Token e n�o de um UUID � preciso definir um prefixo de Token. <b>Caso n�o utilize acesso por token para m�quinas, passe "" neste par�metro por
   *          seguran�a, n�o passe NULL.</b>
   * @throws RFWException
   */
  public static void initializeSessionManager(long timeToLive, SessionBackOperation backOperation, String tokenPrefix) throws RFWException {
    SessionManager.setTimeToLive(timeToLive);
    SessionManager.setBackOperation(backOperation);
    SessionManager.setTokenPrefix(tokenPrefix);
  }

  /**
   * Inicializa o valor padr�o do Locale utilizado no Sistema. Gera efeitos em todo o sistema que utilizar a mesma inst�ncia est�tica do RFW.<br>
   * Valor Padr�o inicial: Locale("pt", "BR").
   *
   * @param locale
   */
  public static void initializeLocale(Locale locale) {
    RFW.locale = locale;
  }

  /**
   * Set m�todo padr�o de arredondamento do sistema.<br>
   * Valor Padr�o inicial: RoundingMode.HALF_EVEN.
   *
   * @param roundingMode the new m�todo padr�o de arredondamento do sistema
   */
  public static void initializeRoundingMode(RoundingMode roundingMode) {
    RFW.roundingMode = roundingMode;
  }

  /**
   * Batiza o sistema com um nome. � recomendado que se crie um label utilizando "Nome" = "Vers�o", algo como "RFW v10.0.0". Algo preferencialmente curto que identifique o sistema e sua vers�o.<br>
   * Lembrando que esse nome deve abrangir o escopo da inst�ncia da classe est�tica {@link RFW}, uma vez que esse nome ser� utilizado para o mesmo escopo.<br>
   * A fun��o deste nome � identifica��o, e ser� utilizado por exemplo pelo servi�o {@link RFWLogger} para gerar tags nos relat�rios criados.
   *
   * @param systemName
   */
  public static void initializeSystemName(String systemName) {
    RFW.systemName = systemName;
  }

  /**
   * Este m�todo inicializa {@link RFWBundle} com um novo arquivo.<br>
   * Note que cada novo arquivo carregado � lido sobre o mesmo properties. Isso faz com que em caso de conflito de chaves o conte�do do �ltimo arquivo lido se sobreponha. Embora pare�a uma falha, a ideia � proposital, assim � poss�vel substituir mensagens padr�o do RFW pelo sistema sendo feito.
   *
   * @param bundleName
   * @throws RFWException
   */
  public static void initializeBundle(String bundleName) throws RFWException {
    RFWBundle.loadBundle(bundleName);
  }

  /**
   * Este m�todo inicializa {@link EventDispatcher}.<br>
   *
   * @param eventThreadPriority Prioridade que as Threads de disparo dos eventos devem ser executadas. Valor padr�o {@link Thread#MIN_PRIORITY}.
   * @throws RFWException
   */
  public static void initializeEventDispatcher(Integer eventThreadPriority) throws RFWException {
    EventDispatcher.setEventThreadPriority(eventThreadPriority);
  }

  /**
   * Gets the date time.
   *
   * @return the date time
   */
  public static LocalDateTime getDateTime() {
    return LocalDateTime.now(getZoneId());
  }

  /**
   * Gets the time.
   *
   * @return the time
   */
  public static LocalTime getTime() {
    return LocalTime.now(getZoneId());
  }

  /**
   * Gets the date.
   *
   * @return the date
   */
  public static LocalDate getDate() {
    return LocalDate.now(getZoneId());
  }

  /**
   * Gets the date time formattter.
   *
   * @return the date time formattter
   */
  public static DateTimeFormatter getDateTimeFormattter() {
    return DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");
  }

  /**
   * Gets the date formattter.
   *
   * @return the date formattter
   */
  public static DateTimeFormatter getDateFormattter() {
    return DateTimeFormatter.ofPattern("dd/MM/uuuu");
  }

  /**
   * Get locale padr�o do sistema.<br>
   * Valor Padr�o inicial: Locale("pt", "BR").
   *
   * @return the locale padr�o do sistema
   */
  public static Locale getLocale() {
    return locale;
  }

  /**
   * Get m�todo padr�o de arredondamento do sistema.<br>
   * Valor Padr�o inicial: RoundingMode.HALF_EVEN.
   *
   * @return the m�todo padr�o de arredondamento do sistema
   */
  public static RoundingMode getRoundingMode() {
    return roundingMode;
  }

  /**
   * Get zoneId padr�o do sistema.<br>
   * Valor padr�o inicial: "America/Sao_Paulo.
   *
   * @return the zoneId padr�o do sistema
   */
  public static ZoneId getZoneId() {
    return zoneId;
  }

  /**
   * Checks if is development environment.
   *
   * @return true, if is development environment
   */
  public static boolean isDevelopmentEnvironment() {
    return BUFile.fileExists("c:\\dev.txt");
  }

  public static String getSystemName() {
    return systemName;
  }

  /**
   * Executa uma tarefa em outra thread. A id�ia � facilitar a execu��o de algumas tarefas em uma thread paralela para liberar a execu��o do c�digo principal. Muito �til para tarefas que precisam ser disparadas mas n�o precisamos do resultado imediato para continuar a execu��o do m�todo principal.
   *
   * @param threadName Nome da Thread
   * @param daemon Define se a thread de execu��o deve ser daemon ou n�o. O sistema se encerra quando apenas Threads do tipo daemon est�o em execu��o. Em outras palavras, threads daemon n�o precisam ser for�adas a terminar para que o sistema finalize.
   * @param delay Tempo em milisegundos para aguardar antes de executar a tarefa
   * @param task Tarefa a ser executada
   */
  public static Timer runLater(String threadName, boolean daemon, long delay, Runnable task) {
    Timer t = new Timer(threadName, daemon);
    t.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          // Executa a tarefa passada
          task.run();
        } finally {
          // Garante que o Timer morra antes de finalizar a tarefa. Se a tarefa n�o for encerrada o Timer mant�m a thread ativa esperando com a task na fila.
          t.cancel(); // Cancela a tarefa atual
          t.purge(); // Remove a referencia dessa tarefa na "queue" do Timer. Ao n�o encontrar nada na queue o Timer permite que a Thread termine ao inv�s de ficar aguardando outro rein�cio.
        }
      }
    }, delay);
    return t;
  }

  /**
   * Este m�todo simplifica a impress�o em console quando estamos em desenvolvimento.<Br>
   * Tem a mesma fun��o que o c�digo:<br>
   *
   * <pre>
   * if (RFW.isDevelopmentEnvironment()) System.out.println(content);
   * </pre>
   *
   * @param content Conte�do a ser impresso no console
   */
  public static void pDev(String content) {
    if (RFW.isDevelopmentEnvironment()) System.out.println(content);
  }

  /**
   * Este m�todo simplifica a impress�o em console quando estamos em desenvolvimento.<Br>
   * Tem a mesma fun��o que o c�digo:<br>
   *
   * <pre>
   * if (RFW.isDevelopmentEnvironment()) e.printStackTrace();
   * </pre>
   *
   * @param t Throwable a ser impresso no console
   */
  public static void pDev(Throwable t) {
    if (RFW.isDevelopmentEnvironment()) t.printStackTrace();
    ;
  }

  /**
   * @return Recupera se o Framework foi sinalizado que deve finalizar. Quando true, todos os sevi�os e Thread do RFW devem se encerrar para que a aplica��o fa�a um undeploy
   */
  public static boolean isShuttingDown() {
    return RFW.shuttingDown;
  }

  /**
   * Faz o mesmo que o {@link Thread#sleep(long)}, por�m j� captura o {@link InterruptedException} caso ele ocorra.<Br>
   * Para os casos em que a exception n�o � importante, deixa o c�digo mais limpo.
   *
   * @param delay tempo em milisegundos que o c�digo (Thread atual) dever� aguardar.
   */
  public static void sleep(long delay) {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
    }
  }
}
