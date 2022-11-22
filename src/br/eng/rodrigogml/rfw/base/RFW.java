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
 * Description: Classe utilitária geral do RFW com métodos utilitários comuns genéricos.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (12 de out de 2020)
 */
public class RFW {

  /**
   * Constante com o BigDecimal de "100", para evitar sua construção o tempo todo.
   */
  public static final BigDecimal BIGHUNDRED = new BigDecimal("100");

  /**
   * Método padrão de arredondamento do sistema.<br>
   * Valor Padrão inicial: RoundingMode.HALF_EVEN.
   */
  private static RoundingMode roundingMode = RoundingMode.HALF_EVEN;

  /**
   * Locale padrão do sistema.<br>
   * Valor Padrão inicial: Locale("pt", "BR").
   */
  private static Locale locale = new Locale("pt", "BR");

  /**
   * ZoneID padrão do sistema.<br>
   * Valor padrão inicial: "America/Sao_Paulo.
   */
  private static ZoneId zoneId = ZoneId.of("America/Sao_Paulo");

  /**
   * Identificação do sistema.
   */
  private static String systemName = null;

  /**
   * Indicador se foi solicitado que o sistema do RFW deve finalizar.
   */
  private static boolean shuttingDown = false;

  /**
   * Construtor privado para uma classe completamente estática
   */
  private RFW() {
  }

  /**
   * Ao chamar este método, todos os serviços do RFW serão sinalizados para que finalizem seus serviços e Threads em andamento o mais rápido possível.<br>
   * A chamada deste método é irreversível.
   */
  public static void shutdownFW() {
    RFW.shuttingDown = true;
    RFWLogger.wakeUpCleannerThread();
  }

  /**
   * Inicializa o valor padrão do ZoneId utilizado no Sistema. Gera efeitos em todo o sistema que utilizar a mesma instância estática do RFW.<br>
   * Valor padrão inicial: "America/Sao_Paulo".
   *
   * @param zoneId
   */
  public static void initializeZoneID(ZoneId zoneId) {
    RFW.zoneId = zoneId;
  }

  /**
   * Inicializa o serviço do RFWLogger configurando seus parâmetros.<Br>
   * Note que este método de fato só define as configurações do RFW. Suas thread só são iniciadas a partir do primeiro log() realizado.<br>
   * <br>
   * <b>ATENÇÃO: </B>Depois de inicializado, o RFWLogger exige que o método {@link #shutdownFW()} seja chamado para que suas Threads sejam encerradas.
   *
   * @param timeToLive Define o tempo que uma entrada de LOG pode ficar na memória aguardando para ser consumida. Depois de passado do periodo definido aqui, a o próprio {@link RFWLogger} descartará a entrada do Log.<br>
   *          Tempo definido em segundos.
   * @param timeToClean Define de quanto em quanto tempo o {@link RFWLogger} executará a limpeza das entradas que não foram consumidas. Note que conforme os valores definidos uma entrada de log ficará aguardando ser consumida descartada entre no mínimo pelo valor definido como timeToLive e no máximo até timeToLive + timeToClean.<br>
   *          Tempo definido em segundos.
   * @throws RFWException
   */
  public static void initializeFWLogger(long timeToLive, long timeToClean) throws RFWException {
    RFWLogger.setTimeToLive(timeToLive);
    RFWLogger.setTimeToClean(timeToClean);
  }

  /**
   * Inicializa o serviço de Controle de Sessão do RFW.
   *
   * @param timeToLive Tempo que a sessão sobrevive sem atividade até ser descartada.<br>
   *          Valor em segundos.
   * @param backOperation Implementação da interface que fará a autenticação dos usuários e acesso por tokens.
   * @param tokenPrefix Prefixo dos tokens de acesso. Quando o sistema permite que máquinas se conectem à fachada, o acesso pode ser feito por um token ao invés do usuário/senha. Mas para que o {@link SessionManager} identifique que se trata de um Token e não de um UUID é preciso definir um prefixo de Token. <b>Caso não utilize acesso por token para máquinas, passe "" neste parâmetro por
   *          segurança, não passe NULL.</b>
   * @throws RFWException
   */
  public static void initializeSessionManager(long timeToLive, SessionBackOperation backOperation, String tokenPrefix) throws RFWException {
    SessionManager.setTimeToLive(timeToLive);
    SessionManager.setBackOperation(backOperation);
    SessionManager.setTokenPrefix(tokenPrefix);
  }

  /**
   * Inicializa o valor padrão do Locale utilizado no Sistema. Gera efeitos em todo o sistema que utilizar a mesma instância estática do RFW.<br>
   * Valor Padrão inicial: Locale("pt", "BR").
   *
   * @param locale
   */
  public static void initializeLocale(Locale locale) {
    RFW.locale = locale;
  }

  /**
   * Set método padrão de arredondamento do sistema.<br>
   * Valor Padrão inicial: RoundingMode.HALF_EVEN.
   *
   * @param roundingMode the new método padrão de arredondamento do sistema
   */
  public static void initializeRoundingMode(RoundingMode roundingMode) {
    RFW.roundingMode = roundingMode;
  }

  /**
   * Batiza o sistema com um nome. É recomendado que se crie um label utilizando "Nome" = "Versão", algo como "RFW v10.0.0". Algo preferencialmente curto que identifique o sistema e sua versão.<br>
   * Lembrando que esse nome deve abrangir o escopo da instância da classe estática {@link RFW}, uma vez que esse nome será utilizado para o mesmo escopo.<br>
   * A função deste nome é identificação, e será utilizado por exemplo pelo serviço {@link RFWLogger} para gerar tags nos relatórios criados.
   *
   * @param systemName
   */
  public static void initializeSystemName(String systemName) {
    RFW.systemName = systemName;
  }

  /**
   * Este método inicializa {@link RFWBundle} com um novo arquivo.<br>
   * Note que cada novo arquivo carregado é lido sobre o mesmo properties. Isso faz com que em caso de conflito de chaves o conteúdo do último arquivo lido se sobreponha. Embora pareça uma falha, a ideia é proposital, assim é possível substituir mensagens padrão do RFW pelo sistema sendo feito.
   *
   * @param bundleName
   * @throws RFWException
   */
  public static void initializeBundle(String bundleName) throws RFWException {
    RFWBundle.loadBundle(bundleName);
  }

  /**
   * Este método inicializa {@link EventDispatcher}.<br>
   *
   * @param eventThreadPriority Prioridade que as Threads de disparo dos eventos devem ser executadas. Valor padrão {@link Thread#MIN_PRIORITY}.
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
   * Get locale padrão do sistema.<br>
   * Valor Padrão inicial: Locale("pt", "BR").
   *
   * @return the locale padrão do sistema
   */
  public static Locale getLocale() {
    return locale;
  }

  /**
   * Get método padrão de arredondamento do sistema.<br>
   * Valor Padrão inicial: RoundingMode.HALF_EVEN.
   *
   * @return the método padrão de arredondamento do sistema
   */
  public static RoundingMode getRoundingMode() {
    return roundingMode;
  }

  /**
   * Get zoneId padrão do sistema.<br>
   * Valor padrão inicial: "America/Sao_Paulo.
   *
   * @return the zoneId padrão do sistema
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
   * Executa uma tarefa em outra thread. A idéia é facilitar a execução de algumas tarefas em uma thread paralela para liberar a execução do código principal. Muito útil para tarefas que precisam ser disparadas mas não precisamos do resultado imediato para continuar a execução do método principal.
   *
   * @param threadName Nome da Thread
   * @param daemon Define se a thread de execução deve ser daemon ou não. O sistema se encerra quando apenas Threads do tipo daemon estão em execução. Em outras palavras, threads daemon não precisam ser forçadas a terminar para que o sistema finalize.
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
          // Garante que o Timer morra antes de finalizar a tarefa. Se a tarefa não for encerrada o Timer mantém a thread ativa esperando com a task na fila.
          t.cancel(); // Cancela a tarefa atual
          t.purge(); // Remove a referencia dessa tarefa na "queue" do Timer. Ao não encontrar nada na queue o Timer permite que a Thread termine ao invés de ficar aguardando outro reinício.
        }
      }
    }, delay);
    return t;
  }

  /**
   * Este método simplifica a impressão em console quando estamos em desenvolvimento.<Br>
   * Tem a mesma função que o código:<br>
   *
   * <pre>
   * if (RFW.isDevelopmentEnvironment()) System.out.println(content);
   * </pre>
   *
   * @param content Conteúdo a ser impresso no console
   */
  public static void pDev(String content) {
    if (RFW.isDevelopmentEnvironment()) System.out.println(content);
  }

  /**
   * Este método simplifica a impressão em console quando estamos em desenvolvimento.<Br>
   * Tem a mesma função que o código:<br>
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
   * @return Recupera se o Framework foi sinalizado que deve finalizar. Quando true, todos os seviços e Thread do RFW devem se encerrar para que a aplicação faça um undeploy
   */
  public static boolean isShuttingDown() {
    return RFW.shuttingDown;
  }

  /**
   * Faz o mesmo que o {@link Thread#sleep(long)}, porém já captura o {@link InterruptedException} caso ele ocorra.<Br>
   * Para os casos em que a exception não é importante, deixa o código mais limpo.
   *
   * @param delay tempo em milisegundos que o código (Thread atual) deverá aguardar.
   */
  public static void sleep(long delay) {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
    }
  }
}
