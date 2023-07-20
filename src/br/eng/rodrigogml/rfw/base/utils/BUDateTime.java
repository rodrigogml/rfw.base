package br.eng.rodrigogml.rfw.base.utils;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import br.eng.rodrigogml.rfw.base.RFWDeprec;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.utils.RUString;

/**
 * Description: Classe utilit�ria para manusear datas.<br>
 *
 * @author Rodrigo Leit�o
 * @since 7.1.0 (02/03/2016)
 */
public class BUDateTime {

  /**
   * Construtor privado para classe est�tica
   */
  private BUDateTime() {
  }

  /**
   * Converte a {@link Timestamp} recebida para o LocalDate<br>
   *
   * @param stamp Data a ser convertida.
   * @return Objeto com o dia/hor�rio convertido para a zona solicitada, ou nulo se receber uma entrada nula.
   * @throws RFWException
   */
  public static LocalDate toLocalDate(Timestamp stamp) throws RFWException {
    if (stamp == null) return null;
    return stamp.toLocalDateTime().toLocalDate();
  }

  /**
   * Converte a {@link Timestamp} recebida para o LocalDateTime<br>
   *
   * @param stamp DataHora a ser convertida.
   * @return Objeto com o dia/hor�rio convertido para a zona solicitada, ou nulo se receber uma entrada nula.
   * @throws RFWException
   */
  public static LocalDateTime toLocalDateTime(Timestamp stamp) throws RFWException {
    if (stamp == null) return null;
    return stamp.toLocalDateTime();
  }

  /**
   * Chama o m�todo {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padr�es de entrada suportados na documenta��o do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, j� com a defini��o de fuso embutida.
   * @return Objeto convertido ou nulo caso receba uma entrada nula.
   * @throws RFWException
   */
  public static LocalDate parseLocalDate(String date) throws RFWException {
    if (date == null) return null;
    return toLocalDateTime(parseDate(date)).toLocalDate();
  }

  /**
   * Realiza o parser da String utilizando o {@link DateTimeFormatter}.<Br>
   *
   * @param date Data no formato String que precisa ser lida.
   * @param pattern no Formado espec�ficado pela documenta��o do m�todo {@link DateTimeFormatter#ofPattern(String)}.
   * @return Objeto Com o a Data.
   * @throws RFWException
   */
  public static LocalDate parseLocalDate(String date, String pattern) throws RFWException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return LocalDate.parse(date, formatter);
  }

  /**
   * Formata um {@link LocalDate} baseada em um pattern
   *
   * @param date Data a ser formatada
   * @param pattern Pattern a ser utilizado.
   * @return String com a data no formato desejado.
   */
  public static String formatLocalDate(LocalDate date, String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return date.format(formatter);
  }

  /**
   * Formata um {@link LocalDateTime} baseada em um pattern
   *
   * @param date Data/Hora a ser formatada
   * @param pattern Pattern a ser utilizado.
   * @return String com a data no formato desejado.
   */
  public static String formatLocalDateTime(LocalDateTime date, String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return date.format(formatter);
  }

  /**
   * Chama o m�todo {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padr�es de entrada suportados na documenta��o do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, j� com a defini��o de fuso embutida.
   * @param zoneID Especifica��o da Zona (fuso hor�rio) para o quam desejamos converter a hora.
   * @return Objeto convertido ou nulo caso receba date == null.
   * @throws RFWException
   */
  public static LocalDateTime parseLocalDateTime(String date, ZoneId zoneID) throws RFWException {
    if (date == null) return null;
    return toLocalDateTime(parseDate(date), zoneID);
  }

  /**
   * Chama o m�todo {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padr�es de entrada suportados na documenta��o do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, j� com a defini��o de fuso embutida.
   * @return retorna LocalDateTime com o hor�rio retirado da String. Se recebido o valor nulo, retorna nulo.
   * @throws RFWException
   */
  public static LocalDateTime parseLocalDateTime(String date) throws RFWException {
    if (date == null) return null;
    return toLocalDateTime(parseDate(date));
  }

  /**
   * Chama o m�todo {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padr�es de entrada suportados na documenta��o do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, j� com a defini��o de fuso embutida.
   * @param zoneID Especifica��o da Zona (fuso hor�rio) para o quam desejamos converter a hora.
   * @return Objeto convertido ou nulo casa date == null;
   * @throws RFWException
   */
  public static LocalDate parseLocalDate(String date, ZoneId zoneID) throws RFWException {
    if (date == null) return null;
    return toLocalDateTime(parseDate(date), zoneID).toLocalDate();
  }

  /**
   * Converte um {@link Date} em {@link LocalDate}.
   *
   * @param date Data a ser convertida em LocalDate
   * @param zone Zone para correta convers�o entre objetos temporais.
   * @return LocalDate conforme a zona, ou nulo caso date == null;
   */
  public static LocalDate toLocalDate(Date date, ZoneId zone) {
    if (date == null) return null;
    return date.toInstant().atZone(zone).toLocalDate();
  }

  /**
   * Converte um {@link Date} em {@link LocalDate}. Utiliza a Zona padr�o do sistema
   *
   * @param date Data a ser convertida em LocalDate
   * @return LocalDate conforme a zona, ou nulo caso a entrada seja nula.
   */
  public static LocalDate toLocalDate(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(RFW.getZoneId()).toLocalDate();
  }

  /**
   * Converte um {@link LocalDateTime} em {@link LocalDate}.
   *
   * @param date Data a ser convertida em LocalDate
   * @return LocalDate conforme o valor de entrada, ou nulo caso a entrada seja nula.
   */
  public static LocalDate toLocalDate(LocalDateTime date) {
    if (date == null) return null;
    return date.toLocalDate();
  }

  /**
   * Converte um {@link java.sql.Date} em {@link LocalDate}. Utiliza a Zona padr�o do sistema
   *
   * @param date Data a ser convertida em LocalDate
   * @return LocalDate conforme a zona ou null caso a entrada seja nula.
   */
  public static LocalDate toLocalDate(java.sql.Date date) {
    if (date == null) return null;
    return date.toLocalDate();
  }

  /**
   * Converte um {@link Date} em {@link LocalDateTime}. Utiliza a Zona padr�o do sistema {@link RFWDeprec#getZoneId()}.
   *
   * @param date Data a ser convertida em LocalDateTime
   * @return LocalDateTime conforme a zona, ou nulo se receber o valor nulo como par�metro.
   */
  public static LocalDateTime toLocalDateTime(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(RFW.getZoneId()).toLocalDateTime();
  }

  /**
   * Converte um {@link Date} em {@link LocalDateTime}.
   *
   * @param date Data a ser convertida em LocalDateTime
   * @param zone Zone para correta convers�o entre objetos temporais.
   * @return LocalDateTime conforme a zona, ou nulo caso a entrada seja nula.
   */
  public static LocalDateTime toLocalDateTime(Date date, ZoneId zone) {
    if (date == null) return null;
    return date.toInstant().atZone(zone).toLocalDateTime();
  }

  /**
   * Converte um {@link LocalDateTime} para {@link Date} utilizando a Zona do Sistema {@link RFWDeprec#getZoneId()}.
   *
   * @param dateTime Valor de Entrada a ser convertido
   * @return Valor Convertido ou nulo caso o valor de entrada seja nulo.
   */
  public static Date toDate(LocalDateTime dateTime) {
    if (dateTime == null) return null;
    return Date.from(dateTime.atZone(RFW.getZoneId()).toInstant());
  }

  /**
   * Converte um {@link LocalDateTime} para {@link Date} utilizando uma Zona personalizada.
   *
   * @param dateTime Valor de Entrada a ser convertido
   * @param zone Zona a ser utilizada.
   * @return Valor Convertido
   */
  public static Date toDate(LocalDateTime dateTime, ZoneId zone) {
    return Date.from(dateTime.atZone(zone).toInstant());
  }

  /**
   * Converte um {@link LocalDate} para {@link Date} utilizando a Zona do Sistema {@link RFWDeprec#getZoneId()}.<br>
   * � considerada a hora zero do dia passaro na convers�o para a Zona.
   *
   * @param date Valor de Entrada a ser convertido
   * @return Valor Convertido
   */
  public static Date toDate(LocalDate date) {
    return Date.from(date.atStartOfDay().atZone(RFW.getZoneId()).toInstant());
  }

  /**
   * Converte um {@link LocalDate} para {@link Date} utilizando uma Zona personalizada.<br>
   * � considerada a hora zero do dia passaro na convers�o para a Zona.
   *
   * @param date Valor de Entrada a ser convertido
   * @param zone Zona a ser utilizada.
   * @return Valor Convertido
   */
  public static Date toDate(LocalDate date, ZoneId zone) {
    return Date.from(date.atStartOfDay().atZone(zone).toInstant());
  }

  /**
   * Retorna o mesmo que o m�todo <code>date1.compareTo(date2)</code>, mas n�o considera o tempo, apenas ano, m�s e dia.
   *
   * @param date1
   * @param date2
   * @return
   */
  public static int compareDateWithoutTime(Date date1, Date date2) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date1);
    gc.set(Calendar.HOUR_OF_DAY, 0);
    gc.set(Calendar.MINUTE, 0);
    gc.set(Calendar.SECOND, 0);
    gc.set(Calendar.MILLISECOND, 0);
    date1 = gc.getTime();
    gc.setTime(date2);
    gc.set(Calendar.HOUR_OF_DAY, 0);
    gc.set(Calendar.MINUTE, 0);
    gc.set(Calendar.SECOND, 0);
    gc.set(Calendar.MILLISECOND, 0);
    date2 = gc.getTime();
    return date1.compareTo(date2);
  }

  /**
   * Este m�todo valida se uma data est� dentro de um determinado periodo.<br>
   * As datas do periodo s�o inclusivas, isto �, se date for igual a startPeriod ou endPeriod o m�todo retornar� true.
   *
   * @param date Data para averigua��o se est� dentro do per�odo.
   * @param startPeriod Data de in�cio do per�odo. Se passado nulo considera que o per�odo come�ou em "menos infinito".
   * @param endPeriod Data de fim do per�odo. Se passado nulo condera que o per�odo nunca termina.
   * @return true caso a data esteja dentro do perioro, false caso contr�rio.
   */
  public static boolean isInsidePeriod(Date date, Date startPeriod, Date endPeriod) {
    return (startPeriod == null || date.compareTo(startPeriod) >= 0) && (endPeriod == null || date.compareTo(endPeriod) <= 0);
  }

  /**
   * Este m�todo valida se uma data est� dentro de um determinado periodo.<br>
   * As datas do periodo s�o inclusivas, isto �, se date for igual a startPeriod ou endPeriod o m�todo retornar� true.<br>
   *
   * @param date Data para averigua��o se est� dentro do per�odo.
   * @param startPeriod Data de in�cio do per�odo. Se passado nulo considera que o per�odo come�ou em "menos infinito".
   * @param endPeriod Data de fim do per�odo. Se passado nulo condera que o per�odo nunca termina.
   * @return true caso a data esteja dentro do perioro, false caso contr�rio.
   */
  public static boolean isInsidePeriod(LocalDate date, LocalDate startPeriod, LocalDate endPeriod) {
    return (startPeriod == null || date.compareTo(startPeriod) >= 0) && (endPeriod == null || date.compareTo(endPeriod) <= 0);
  }

  /**
   * Este m�todo valida se uma data est� dentro de um determinado periodo.<br>
   * As datas do periodo s�o inclusivas, isto �, se date for igual a startPeriod ou endPeriod o m�todo retornar� true.
   *
   * @param date Data para averigua��o se est� dentro do per�odo.
   * @param startPeriod Data de in�cio do per�odo. Se passado nulo considera que o per�odo come�ou em "menos infinito".
   * @param endPeriod Data de fim do per�odo. Se passado nulo condera que o per�odo nunca termina.
   * @return true caso a data esteja dentro do perioro, false caso contr�rio.
   */
  public static boolean isInsidePeriod(LocalDateTime date, LocalDateTime startPeriod, LocalDateTime endPeriod) {
    return (startPeriod == null || date.compareTo(startPeriod) >= 0) && (endPeriod == null || date.compareTo(endPeriod) <= 0);
  }

  /**
   * Este m�todo recebe uma data e for�a a defini��o da hora = 23, minuto = 59 e segundos = 59 e milisegundos = 999.
   *
   * @param date Data a ter o hor�rio modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o hor�rio 23:59:59'999
   */
  public static Date setTimeTo235959(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.HOUR_OF_DAY, 23);
    gc.set(Calendar.MINUTE, 59);
    gc.set(Calendar.SECOND, 59);
    gc.set(Calendar.MILLISECOND, 999);
    return gc.getTime();
  }

  /**
   * Este m�todo recebe uma data e for�a a defini��o da hora = 23, minuto = 59 e segundos = 59 e nanosegundos = 999.999.999.
   *
   * @param date Data a ter o hor�rio modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o hor�rio 23:59:59'999
   */
  public static LocalDateTime setTimeTo235959(LocalDateTime date) {
    return date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
  }

  /**
   * Este m�todo recebe uma data e for�a a defini��o da hora = 00, minuto = 00 e segundos = 00 e milisegundos = 000.
   *
   * @param date Data a ter o hor�rio modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o hor�rio 00:00:00'000
   */
  public static Date setTimeTo000000(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.HOUR_OF_DAY, 00);
    gc.set(Calendar.MINUTE, 00);
    gc.set(Calendar.SECOND, 00);
    gc.set(Calendar.MILLISECOND, 000);
    return gc.getTime();
  }

  /**
   * Este m�todo recebe uma data e for�a a defini��o da hora = 00, minuto = 00 e segundos = 00 e nanosegundos = 000.000.000.
   *
   * @param date Data a ter o hor�rio modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o hor�rio 00:00:00'000
   */
  public static LocalDateTime setTimeTo000000(LocalDateTime date) {
    return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
  }

  /**
   * Este m�todo recebe uma data e for�a a defini��o de milisegundos = 000.
   *
   * @param date Data a ter o hor�rio modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o hor�rio 00:00:00'000
   */
  public static Date setTimeMillisTo000(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.MILLISECOND, 000);
    return gc.getTime();
  }

  /**
   * Este m�todo conta quandos minutos se passaram desde o tempo passado at� agora.
   *
   * @param miliseconds
   * @return
   */
  public static double countMinutesFrom(long miliseconds) {
    return (System.currentTimeMillis() - miliseconds) / 60000;
  }

  /**
   * Calcula o n�mero de meses entre uma data e outra. Note que mesmo que as datas sejam diferentes, se estiverem dentro do mesmo m�s e ano, o valor retornado ser� zero.<br>
   * Caso a data final esteja em um m�s anterior ao data inicial, o valor retornado ser� negativo.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static int calcDiferenceInMonths(LocalDate initialDate, LocalDate finalDate) {
    Period age = Period.between(initialDate.withDayOfMonth(1), finalDate.withDayOfMonth(1)); // Colocamos ambas as data no dia 1, para que o Period fa�a o c�lculo exatamente entre meses e n�o d� valores quebrados entre os dias.
    return age.getMonths() + age.getYears() * 12;
  }

  /**
   * Calcula o n�mero de dias entre uma data e outra. S� contabiliza o per�odo completo. (Arredonda para baixo)<br>
   *
   * @param initialDate data inicial (inclusivo)
   * @param finalDate data final (exclusivo).
   * @return total de dias entre a data inicial e a final, sem contar a data final. S� contabiliza dias completos, se, por conta do hor�rio, n�o formar 24h, o dia n�o � contabilizado.
   */
  public static long calcDiferenceInDays(LocalDate initialDate, LocalDate finalDate) {
    return ChronoUnit.DAYS.between(initialDate, finalDate);
  }

  /**
   * Calcula o n�mero de dias entre uma data e outra. S� contabiliza o per�odo completo. (Arredonda para baixo)<br>
   *
   * @param initialDate data inicial (inclusivo)
   * @param finalDate data final (exclusivo).
   * @return total de dias entre a data inicial e a final, sem contar a data final. S� contabiliza dias completos, se, por conta do hor�rio, n�o formar 24h, o dia n�o � contabilizado.
   */
  public static long calcDiferenceInDays(LocalDateTime initialDate, LocalDateTime finalDate) {
    return ChronoUnit.DAYS.between(initialDate, finalDate);
  }

  /**
   * Calcula o tempo entre duas datas dado em dias. Retorna o tempo negativo caso a data inicial seja futura em rela��o a data final. Embora n�o tenha tempo negativo, a refer�ncia negativo ajuda a validar a ordem cronol�gica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInDays(Date initialDate, Date finalDate) {
    return calcDiferenceInDays(initialDate.getTime(), finalDate.getTime());
  }

  /**
   * Calcula o tempo entre duas datas dado em Dias. Retorna o tempo negativo caso a data inicial seja futura em rela��o a data final. Embora n�o tenha tempo negativo, a refer�ncia negativo ajuda a validar a ordem cronol�gica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInDays(long initialDate, long finalDate) {
    long diff = finalDate - initialDate;
    return diff / 86400000d; // 1 dia = 86.400.000ms
  }

  /**
   * Calcula o tempo entre duas datas dado em horas. Retorna o tempo negativo caso a data inicial seja futura em rela��o a data final. Embora n�o tenha tempo negativo, a refer�ncia negativo ajuda a validar a ordem cronol�gica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInHours(Date initialDate, Date finalDate) {
    return calcDiferenceInHours(initialDate.getTime(), finalDate.getTime());
  }

  /**
   * Calcula o tempo entre duas datas dado em horas. Retorna o tempo negativo caso a data inicial seja futura em rela��o a data final. Embora n�o tenha tempo negativo, a refer�ncia negativo ajuda a validar a ordem cronol�gica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInHours(long initialDate, long finalDate) {
    long diff = finalDate - initialDate;
    return diff / 3600000d; // 1 hora = 3.600.000ms
  }

  /**
   * Calcula o tempo entre duas datas dado em minutos. Retorna o tempo negativo caso a data inicial seja futura em rela��o a data final. Embora n�o tenha tempo negativo, a refer�ncia negativo ajuda a validar a ordem cronol�gica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInMinutes(Date initialDate, Date finalDate) {
    return calcDiferenceInMinutes(initialDate.getTime(), finalDate.getTime());
  }

  /**
   * Calcula o tempo entre duas datas dado em minutos. Retorna o tempo negativo caso a data inicial seja futura em rela��o a data final. Embora n�o tenha tempo negativo, a refer�ncia negativo ajuda a validar a ordem cronol�gica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInMinutes(long initialDate, long finalDate) {
    long diff = finalDate - initialDate;
    return diff / 60000d; // 1 minuto = 60.000ms
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date apenas no hor�rio com o patern '23:59:59', ignorando qualquer que seja a data.
   *
   * @param time
   * @return
   */
  public static String formatTo235959(Date time) {
    return new SimpleDateFormat("HH:mm:ss").format(time);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'yyyyMMddHHmmss'.
   *
   * @param date
   * @return
   */
  public static String formatToyyyyMMddHHmmss(Date date) {
    return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'yyyyMMdd'.
   *
   * @param date
   * @return
   */
  public static String formatToyyyyMMdd(Date date) {
    return new SimpleDateFormat("yyyyMMdd").format(date);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'ddMMyyyy'.
   *
   * @param date
   * @return
   */
  public static String formatToddMMyyyy(Date date) {
    return new SimpleDateFormat("ddMMyyyy").format(date);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'ddMMyyyyHHmmss'.
   *
   * @param date
   * @return
   */
  public static String formatToddMMyyyyHHmmss(Date date) {
    return new SimpleDateFormat("ddMMyyyyHHmmss").format(date);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'dd/MM/yyyy HH:mm:ss'.
   *
   * @param date
   * @return
   */
  public static String formatTodd_MM_yyyy_HH_mm_ss(Date date) {
    return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'dd/MM/yyyy HH:mm:ss'.
   *
   * @param date
   * @return
   */
  public static String formatTodd_MM_yyyy_HH_mm_ss(LocalDateTime date) {
    return formatLocalDateTime(date, "dd/MM/yyyy HH:mm:ss");
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'yyyy-MM-dd'T'HH:mm:ssXXX' (Padr�o UTC utilizado no XML da NFe).
   *
   * @param date
   * @return
   */
  public static String formatToyyyy_MM_dd_T_HH_mm_ssXXX(Date date) {
    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'yyyy-MM-dd'T'HH:mm:ssXXX' (Padr�o UTC utilizado no XML da NFe).
   *
   * @param date
   * @return
   */
  public static String formatToyyyy_MM_dd_T_HH_mm_ssXXX(LocalDateTime date, ZoneId zoneId) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    return date.atZone(zoneId).format(formatter);
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'yyyyMMddHHmmss'.
   *
   * @param value
   * @return
   */
  public static Date parseFromyyyyMMddHHmmss(String value) throws RFWException {
    try {
      return new SimpleDateFormat("yyyyMMddHHmmss").parse(value);
    } catch (ParseException e) {
      throw new RFWCriticalException("Falha ao fazer o parse da data '${0}'!", e);
    }
  }

  /**
   * Este m�todo utiliza o SimpleDateFormat para formar o Date apenas no hor�rio com o patern '23:59:59', ignorando qualquer que seja a data.
   *
   * @param dLastConsult
   * @return
   */
  public static String formatTo235959(long timemillis) {
    return formatTo235959(new Date(timemillis));
  }

  /**
   * Simplifica a funcionalidade de somar/subtrair � uma date.
   *
   * @param date Data base para a opera��o.
   * @param period Define o periodo a ser adicionado, se ser� em dias, meses, horas, minutos, etc. Valores deste atributo podem ser encontrados em {@link Calendar}. Ex: {@link Calendar#MONTH}.
   * @param amount Defina a quantidade de per�odo que deve ser somado/subtra�do. N�mero negativos fazem com que o valor seja subtra�do da data base.
   * @return
   */
  public static Date calcDateAdd(Date date, int period, int amount) {
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    gc.add(period, amount);
    return gc.getTime();
  }

  /**
   * Retorna o dia do m�s de uma determinada data com base no calend�rio Gregoriano.
   *
   * @param date Data base extra��o do valor.
   * @return 1 para o primeiro dia do m�s, e assim sucessivamente at� 28, 29, 30 ou 31 para o �ltimo dia do m�s dependendo do m�s corrente.
   */
  public static int getDayOfMonth(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Retorna o m�s de uma determinada data com base no calend�rio Gregoriano.
   *
   * @param date Data base extra��o do valor.
   * @return 1 para o m�s de Janeiro, e segue sucessivamente at� 12 para o Dezembro.
   */
  public static int getMonth(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.MONTH) + 1; // Soma 1 porque a fun��o do Java retorna de 0 � 11.
  }

  /**
   * Retorna o ano de uma determinada data com base no calend�rio Gregoriano.
   *
   * @param date Data base extra��o do valor.
   * @return O n�mero do ano com 4 d�gitos.
   */
  public static int getYear(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.YEAR);
  }

  /**
   * Verifica se o dia de uma determinada data � o primeiro dia do m�s com base no calend�rio Gregoriano.
   *
   * @param date Data base para an�lise
   * @return true caso seja o primeiro dia do m�s, false caso contr�rio.
   */
  public static boolean isFirstDayOfMonth(Date date) {
    return getDayOfMonth(date) == 1;
  }

  /**
   * Verifica se o dia de uma determinada data � o �ltimo dia do m�s com base no calend�rio Gregoriano.
   *
   * @param date Data base para an�lise
   * @return true caso seja o �ltimo dia do m�s, false caso contr�rio.
   */
  public static boolean isLastDayOfMonth(Date date) {
    return getDayOfMonth(date) == getLastDayOfMonth(date);
  }

  /**
   * Retorna muda o dia para o primeiro dia do M�s da data passada.<br>
   * ATEN��O: este m�todo n�o altera o tempo. Se desejar colocar o primeiro momento do m�s utilize em conjunto com o m�todo {@link #setTimeTo000000(Date)};
   *
   * @param date Data de refer�ncia
   * @return
   */
  public static Date getFirstDateOfMonth(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.DAY_OF_MONTH, 1);
    return gc.getTime();
  }

  /**
   * Retorna muda o dia para o �ltimo dia do M�s da data passada.<br>
   * ATEN��O: este m�todo n�o altera o tempo. Se desejar colocar o �ltimo momento do m�s utilize em conjunto com o m�todo {@link #setsetTimeTo235959(Date)};
   *
   * @param date Data de refer�ncia
   * @return
   */
  public static Date getLastDateOfMonth(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.DAY_OF_MONTH, getLastDayOfMonth(date));
    return gc.getTime();
  }

  /**
   * Recupera o �ltimo dia do m�s de uma determinada data, de acordo com o calend�rio Gregoriano.
   *
   * @param date Data a ser examinada, ser� analizado o m�s e o ano desta data para determinar o �ltimo dia do m�s.
   * @return 28, 29, 30 ou 31 de acordo com o m�s e ano da data passada, indicando o �ltimo dia do m�s.
   */
  public static int getLastDayOfMonth(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  /**
   * Recupera o �ltimo dia do m�s de uma determinada data, de acordo com o calend�rio Gregoriano.
   *
   * @param date Data a ser examinada, ser� analizado o m�s e o ano desta data para determinar o �ltimo dia do m�s.
   * @return 28, 29, 30 ou 31 de acordo com o m�s e ano da data passada, indicando o �ltimo dia do m�s.
   */
  public static int getLastDayOfMonth(LocalDate date) {
    return date.plusMonths(1).withDayOfMonth(1).plusDays(-1).getDayOfMonth();
  }

  /**
   * Recupera um {@link LocalDate} com o �ltimo dia do m�s de uma determinada data, de acordo com o calend�rio Gregoriano.
   *
   * @param date Data a ser examinada, ser� analizado o m�s e o ano desta data para determinar o �ltimo dia do m�s.
   * @return LocalDate apontando a data do �ltimo dia do m�s.
   */
  public static LocalDate getLastLocalDateOfMonth(LocalDateTime date) {
    return getLastLocalDateOfMonth(date.toLocalDate());
  }

  /**
   * Recupera um {@link LocalDate} com o �ltimo dia do m�s de uma determinada data, de acordo com o calend�rio Gregoriano.
   *
   * @param date Data a ser examinada, ser� analizado o m�s e o ano desta data para determinar o �ltimo dia do m�s.
   * @return LocalDate apontando a data do �ltimo dia do m�s.
   */
  public static LocalDate getLastLocalDateOfMonth(LocalDate date) {
    return date.plusMonths(1).withDayOfMonth(1).plusDays(-1);
  }

  /**
   * Retorna o nome do m�s por extenso de acordo com o locale.
   *
   * @param locale Locale para saber como traduzir o nome do m�s.
   * @param Month M�s do calend�rio gregoriano para se obter o nome do m�s. O valor come�a em 1 para Janeiro e termina em 12 para Dezembro.
   * @return Nome do m�s por exenso de acordo com os par�metros passados.
   */
  public static String getMonthName(Locale locale, int month) {
    return new DateFormatSymbols(locale).getMonths()[month - 1];
  }

  /**
   * Retorna o nome curto (normalmente s� 3 letras) do m�s por extenso de acordo com o locale.
   *
   * @param locale Locale para saber como traduzir o nome do m�s.
   * @param Month M�s do calend�rio gregoriano para se obter o nome do m�s. O valor come�a em 1 para Janeiro e termina em 12 para Dezembro.
   * @return Nome do m�s por exenso de acordo com os par�metros passados.
   */
  public static String getMonthShortName(Locale locale, int month) {
    return new DateFormatSymbols(locale).getShortMonths()[month - 1];
  }

  /**
   * Formata um tempo em milisegundos em "HHH:MM:SS'mmm", onde HHH s�o horas, mesmo acima de 24 horas, MM os m�nutos, SS os segundos e mmm os milisegundos.<br>
   * Os vampos zerados ser�o eliminados do resultado. Por exemplo, se n�o tivermos horas, o resultado ser� "MM:SS'mmm"
   *
   * @param millis Tempo em milisegundos a ser formato para leitura humana.
   *
   * @return
   */
  public static String formatMillisToHuman(long millis) {
    final long h = TimeUnit.MILLISECONDS.toHours(millis);
    final long m = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(h);
    final long s = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m);
    final long ms = millis % 1000;
    StringBuilder sb = new StringBuilder();
    if (h > 0) sb.append(RUString.completeUntilLengthLeft("0", "" + h, 2)).append(":");
    if (h > 0 || m > 0) sb.append(RUString.completeUntilLengthLeft("0", "" + m, 2)).append(":");
    if (h > 0 || m > 0 || s > 0) sb.append(RUString.completeUntilLengthLeft("0", "" + s, 2)).append("'");
    sb.append(RUString.completeUntilLengthLeft("0", "" + ms, 3)).append("\"");
    return sb.toString();
  }

  /**
   * Formata um tempo em milisegundos utilizando o SimpleDateFormat
   *
   * @param pattern Pattern que segue o padr�o do {@link SimpleDateFormat}
   * @param millis Tempo em milisegundos para ser formatado
   * @return Tempo formatado
   */
  public static String formatMillis(String pattern, long millis) {
    SimpleDateFormat sf = new SimpleDateFormat(pattern);
    sf.setTimeZone(TimeZone.getTimeZone("UTC")); // joga para o UTC para que n�o considere o TimeZone do local. Caso contr�rio as horas do fuso v�o aparecer no resultado formatado.
    return sf.format(new Date(millis));
  }

  /**
   * Este m�todo gere o sufixo do nome do arquivo para padronizar o nome de acordo com um per�odo de datas.<br>
   * Casos implementados at� agora:<br>
   * <li>Se o per�odo � o m�s completo (do primeiro dia ao �ltimo): "Agosto-2017"</li>
   * <li>Demais casos: ddMMyyyy_ddMMyyyy
   *
   * @param startDate
   * @param endDate
   */
  public static String genNameByDate(Date startDate, Date endDate) {
    final boolean startOnFirstDayOfMonth = BUDateTime.isFirstDayOfMonth(startDate);
    final boolean endOnLastDayOfMonth = BUDateTime.isLastDayOfMonth(endDate);
    final boolean sameMonth = BUDateTime.getMonth(startDate) == BUDateTime.getMonth(endDate);
    final boolean sameYear = BUDateTime.getYear(startDate) == BUDateTime.getYear(endDate);

    final String sufix;
    if (startOnFirstDayOfMonth && endOnLastDayOfMonth && sameMonth && sameYear) {
      // Se o periodo come�a no primeiro dia do M�s e Termina no �ltimo dia do mesmo M�s
      sufix = BUDateTime.getMonthName(RFW.getLocale(), BUDateTime.getMonth(startDate)) + "-" + BUDateTime.getYear(startDate);
    } else {
      final String init = BUDateTime.formatToddMMyyyy(startDate);
      final String end = BUDateTime.formatToddMMyyyy(endDate);
      if (init.equals(end)) {
        sufix = init;
      } else {
        sufix = init + '_' + end;
      }
    }
    return sufix;
  }

  /**
   * Recupera o dia da semana de uma Data recebida.
   *
   * @param date Data para obter o dia da semana
   * @return Retorna um Integer indicando o dia da semana. O dia da semana pode ser comparado utilizando as constantes de {@link Calendar}:<Br>
   *         <li>{@link Calendar#MONDAY} - Segunda
   *         <li>{@link Calendar#TUESDAY} - Ter�a
   *         <li>{@link Calendar#WEDNESDAY} - Quarta
   *         <li>{@link Calendar#THURSDAY} - Quinta
   *         <li>{@link Calendar#FRIDAY} - Sexta
   *         <li>{@link Calendar#SATURDAY} - S�bado
   *         <li>{@link Calendar#SUNDAY} - Domingo
   */
  public static int getWeekDay(Date date) {
    final Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_WEEK);
  }

  /**
   * Retorna quantos dias os dois per�odos se sobrep�e.<br>
   * NOTE QUE TODAS AS DATA S�O INCLUSIVAS, por tanto, casos os per�odos estejam "encostados" e a data final de um periodo for igual a data inicial do outro periodo, o m�todo retornar� 1.
   *
   * @param period1Start Data de in�cio do primeiro per�odo.
   * @param period1End Data de fim do primeiro per�odo.
   * @param period2Start Data de in�cio do segundo per�odo.
   * @param period2End Data de fim do segundo per�odo.
   * @return N�mero de dias que os per�odos se sobrep�e.
   * @throws RFWException
   */
  public static long calcOverlappingDays(LocalDate period1Start, LocalDate period1End, LocalDate period2Start, LocalDate period2End) throws RFWException {
    // Validamos primeiro que os dois periodos estejam ok
    if (period1End.isBefore(period1Start)) throw new RFWValidationException("O data de fim do primeiro per�odo � anterior a data de in�cio!");
    if (period2End.isBefore(period2Start)) throw new RFWValidationException("O data de fim do segundo per�odo � anterior a data de in�cio!");

    // Se um termina antes do outro come�ar, n�o h� sobreposi��o
    if (period1End.isBefore(period2Start) || period2End.isBefore(period1Start)) return 0;

    // Eliminando os casos em que n�o h� sobreposi��o, a sobreprosi��o � o resultado da maior data de in�cio com a menor data de fim
    LocalDate start = period1Start;
    LocalDate end = period1End;
    if (period1Start.isBefore(period2Start)) start = period2Start;
    if (period1End.isAfter(period2End)) end = period2End;

    return ChronoUnit.DAYS.between(start, end.plusDays(1));
  }

  /**
   * Este m�todo interpreta todos os formatos j� encontrados de datas que podem vir na NFe para o formato do Java. Atualmente os formatos reconhecidos s�o:<br>
   * <li>"yyyy-MM-dd'T'HH:mm:ssXXX", onde XXX � algo como "-07:00" (Padr�o UTC)</li>
   * <li>"yyyy-MM-dd'T'HH:mm:ssZ", onde Z � algo como "-0700" (Padr�o UTC)</li>
   * <li>"yyyy-MM-dd'T'HH:mm:ss" (Padr�o UTC Sem TimeZone)</li>
   * <li>"yyyy-MM-dd"</li>
   * <li>"dd/MM/yyyy"</li>
   *
   * @param date Data com os valores recebidos na String. Na aus�ncia de um TimeZone � considerado que o TImeZone
   * @return
   * @throws RFWException
   */
  public static Date parseDate(String date) throws RFWException {
    if (date != null) {
      if (date.matches("[1-2][0-9]{3}\\-[0-1][0-9]\\-[0-3][0-9]")) {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7)) - 1;
        int day = Integer.parseInt(date.substring(8, 10));
        GregorianCalendar gc = new GregorianCalendar(year, month, day);
        return gc.getTime();
      } else if (date.matches("[1-2][0-9]{3}\\-[0-1][0-9]\\-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9](\\-|\\+)[0-2][0-9]:[0-5][0-9]")) {
        try {
          final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
          return df.parse(date);
        } catch (ParseException e) {
          throw new RFWCriticalException("Falha ao realizar o parser da data. Data '${0}'.", new String[] { date }, e);
        }
      } else if (date.matches("[1-2][0-9]{3}\\-[0-1][0-9]\\-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9](\\-|\\+)[0-2][0-9][0-5][0-9]")) {
        try {
          final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
          return df.parse(date);
        } catch (ParseException e) {
          throw new RFWCriticalException("Falha ao realizar o parser da data. Data '${0}'.", new String[] { date }, e);
        }
      } else if (date.matches("[1-2][0-9]{3}\\-[0-1][0-9]\\-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9]")) {
        try {
          final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          return df.parse(date);
        } catch (ParseException e) {
          throw new RFWCriticalException("Falha ao realizar o parser da data. Data '${0}'.", new String[] { date }, e);
        }
      } else if (date.matches("[0-3][0-9]/[0-1][0-9]/[1-2][0-9]{3}")) {
        try {
          final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
          return df.parse(date);
        } catch (ParseException e) {
          throw new RFWCriticalException("Falha ao realizar o parser da data. Data '${0}'.", new String[] { date }, e);
        }
      } else {
        throw new RFWValidationException("Formato da Data n�o est� em formato n�o suportado por este m�todo. Data: '${0}'", new String[] { date });
      }
    }
    return null;

  }

  /**
   * Cria um objeto Date com uma data espec�fica.
   *
   * @param year O ano a ser utilizado na data.
   * @param month O M�s do ano a ser utilizado. 1 para janeiro e 12 para Dezembro.
   * @param dayOfMonth Dia do m�s a set utilizado. 1 para dia 1, e assim sucessivamente.
   * @param hour Hora do dia a ser utilizado, variando de 0 � 23.
   * @param minute Minuto a ser utilizado na data.
   * @param second Segundos a ser utilizado na data.
   * @param milliseconds Milesegundos a ser utilizado na data.
   * @return Objeto Data com a data configurada.
   */
  public static Date createDate(int year, int month, int dayOfMonth, int hour, int minute, int second, int milliseconds) {
    final Calendar c = GregorianCalendar.getInstance();
    c.set(year, month - 1, dayOfMonth, hour, minute, second);
    c.set(Calendar.MILLISECOND, milliseconds);
    return c.getTime();
  }

  /**
   * Cria um objeto LocalDateTime com uma data espec�fica.
   *
   * @param year O ano a ser utilizado na data.
   * @param month O M�s do ano a ser utilizado. 1 para janeiro e 12 para Dezembro.
   * @param dayOfMonth Dia do m�s a set utilizado. 1 para dia 1, e assim sucessivamente.
   * @param hour Hora do dia a ser utilizado, variando de 0 � 23.
   * @param minute Minuto a ser utilizado na data.
   * @param second Segundos a ser utilizado na data.
   * @param milliseconds Milesegundos a ser utilizado na data.
   * @param nanoOfSecond Nanos segundos.
   * @return Objeto Data com a data configurada.
   */
  public static LocalDateTime createLocalDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, int milliseconds, int nanoOfSecond) {
    return LocalDateTime.of(year, month, dayOfMonth, hour, minute, milliseconds, nanoOfSecond);
  }

  /**
   * Cria um objeto LocalDate com uma data espec�fica.
   *
   * @param year O ano a ser utilizado na data.
   * @param month O M�s do ano a ser utilizado. 1 para janeiro e 12 para Dezembro.
   * @param dayOfMonth Dia do m�s a set utilizado. 1 para dia 1, e assim sucessivamente.
   * @return Objeto Data com a data configurada.
   */
  public static LocalDate createLocalDate(int year, int month, int dayOfMonth) {
    return LocalDate.of(year, month, dayOfMonth);
  }
}
