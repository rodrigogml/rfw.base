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
 * Description: Classe utilitária para manusear datas.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (02/03/2016)
 */
public class BUDateTime {

  /**
   * Construtor privado para classe estática
   */
  private BUDateTime() {
  }

  /**
   * Converte a {@link Timestamp} recebida para o LocalDate<br>
   *
   * @param stamp Data a ser convertida.
   * @return Objeto com o dia/horário convertido para a zona solicitada, ou nulo se receber uma entrada nula.
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
   * @return Objeto com o dia/horário convertido para a zona solicitada, ou nulo se receber uma entrada nula.
   * @throws RFWException
   */
  public static LocalDateTime toLocalDateTime(Timestamp stamp) throws RFWException {
    if (stamp == null) return null;
    return stamp.toLocalDateTime();
  }

  /**
   * Chama o método {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padrões de entrada suportados na documentação do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, já com a definição de fuso embutida.
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
   * @param pattern no Formado específicado pela documentação do método {@link DateTimeFormatter#ofPattern(String)}.
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
   * Chama o método {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padrões de entrada suportados na documentação do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, já com a definição de fuso embutida.
   * @param zoneID Especificação da Zona (fuso horário) para o quam desejamos converter a hora.
   * @return Objeto convertido ou nulo caso receba date == null.
   * @throws RFWException
   */
  public static LocalDateTime parseLocalDateTime(String date, ZoneId zoneID) throws RFWException {
    if (date == null) return null;
    return toLocalDateTime(parseDate(date), zoneID);
  }

  /**
   * Chama o método {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padrões de entrada suportados na documentação do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, já com a definição de fuso embutida.
   * @return retorna LocalDateTime com o horário retirado da String. Se recebido o valor nulo, retorna nulo.
   * @throws RFWException
   */
  public static LocalDateTime parseLocalDateTime(String date) throws RFWException {
    if (date == null) return null;
    return toLocalDateTime(parseDate(date));
  }

  /**
   * Chama o método {@link #parseDate(String)} e converte para um {@link LocalDateTime} considerando o ZoneID configurado em {@link RFWDeprec#getZoneId()}<br>
   * Verifique os padrões de entrada suportados na documentação do {@link #parseDate(String)}
   *
   * @param date Objeto data a ser convertido, já com a definição de fuso embutida.
   * @param zoneID Especificação da Zona (fuso horário) para o quam desejamos converter a hora.
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
   * @param zone Zone para correta conversão entre objetos temporais.
   * @return LocalDate conforme a zona, ou nulo caso date == null;
   */
  public static LocalDate toLocalDate(Date date, ZoneId zone) {
    if (date == null) return null;
    return date.toInstant().atZone(zone).toLocalDate();
  }

  /**
   * Converte um {@link Date} em {@link LocalDate}. Utiliza a Zona padrão do sistema
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
   * Converte um {@link java.sql.Date} em {@link LocalDate}. Utiliza a Zona padrão do sistema
   *
   * @param date Data a ser convertida em LocalDate
   * @return LocalDate conforme a zona ou null caso a entrada seja nula.
   */
  public static LocalDate toLocalDate(java.sql.Date date) {
    if (date == null) return null;
    return date.toLocalDate();
  }

  /**
   * Converte um {@link Date} em {@link LocalDateTime}. Utiliza a Zona padrão do sistema {@link RFWDeprec#getZoneId()}.
   *
   * @param date Data a ser convertida em LocalDateTime
   * @return LocalDateTime conforme a zona, ou nulo se receber o valor nulo como parâmetro.
   */
  public static LocalDateTime toLocalDateTime(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(RFW.getZoneId()).toLocalDateTime();
  }

  /**
   * Converte um {@link Date} em {@link LocalDateTime}.
   *
   * @param date Data a ser convertida em LocalDateTime
   * @param zone Zone para correta conversão entre objetos temporais.
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
   * É considerada a hora zero do dia passaro na conversão para a Zona.
   *
   * @param date Valor de Entrada a ser convertido
   * @return Valor Convertido
   */
  public static Date toDate(LocalDate date) {
    return Date.from(date.atStartOfDay().atZone(RFW.getZoneId()).toInstant());
  }

  /**
   * Converte um {@link LocalDate} para {@link Date} utilizando uma Zona personalizada.<br>
   * É considerada a hora zero do dia passaro na conversão para a Zona.
   *
   * @param date Valor de Entrada a ser convertido
   * @param zone Zona a ser utilizada.
   * @return Valor Convertido
   */
  public static Date toDate(LocalDate date, ZoneId zone) {
    return Date.from(date.atStartOfDay().atZone(zone).toInstant());
  }

  /**
   * Retorna o mesmo que o método <code>date1.compareTo(date2)</code>, mas não considera o tempo, apenas ano, mês e dia.
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
   * Este método valida se uma data está dentro de um determinado periodo.<br>
   * As datas do periodo são inclusivas, isto é, se date for igual a startPeriod ou endPeriod o método retornará true.
   *
   * @param date Data para averiguação se está dentro do período.
   * @param startPeriod Data de início do período. Se passado nulo considera que o período começou em "menos infinito".
   * @param endPeriod Data de fim do período. Se passado nulo condera que o período nunca termina.
   * @return true caso a data esteja dentro do perioro, false caso contrário.
   */
  public static boolean isInsidePeriod(Date date, Date startPeriod, Date endPeriod) {
    return (startPeriod == null || date.compareTo(startPeriod) >= 0) && (endPeriod == null || date.compareTo(endPeriod) <= 0);
  }

  /**
   * Este método valida se uma data está dentro de um determinado periodo.<br>
   * As datas do periodo são inclusivas, isto é, se date for igual a startPeriod ou endPeriod o método retornará true.<br>
   *
   * @param date Data para averiguação se está dentro do período.
   * @param startPeriod Data de início do período. Se passado nulo considera que o período começou em "menos infinito".
   * @param endPeriod Data de fim do período. Se passado nulo condera que o período nunca termina.
   * @return true caso a data esteja dentro do perioro, false caso contrário.
   */
  public static boolean isInsidePeriod(LocalDate date, LocalDate startPeriod, LocalDate endPeriod) {
    return (startPeriod == null || date.compareTo(startPeriod) >= 0) && (endPeriod == null || date.compareTo(endPeriod) <= 0);
  }

  /**
   * Este método valida se uma data está dentro de um determinado periodo.<br>
   * As datas do periodo são inclusivas, isto é, se date for igual a startPeriod ou endPeriod o método retornará true.
   *
   * @param date Data para averiguação se está dentro do período.
   * @param startPeriod Data de início do período. Se passado nulo considera que o período começou em "menos infinito".
   * @param endPeriod Data de fim do período. Se passado nulo condera que o período nunca termina.
   * @return true caso a data esteja dentro do perioro, false caso contrário.
   */
  public static boolean isInsidePeriod(LocalDateTime date, LocalDateTime startPeriod, LocalDateTime endPeriod) {
    return (startPeriod == null || date.compareTo(startPeriod) >= 0) && (endPeriod == null || date.compareTo(endPeriod) <= 0);
  }

  /**
   * Este método recebe uma data e força a definição da hora = 23, minuto = 59 e segundos = 59 e milisegundos = 999.
   *
   * @param date Data a ter o horário modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o horário 23:59:59'999
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
   * Este método recebe uma data e força a definição da hora = 23, minuto = 59 e segundos = 59 e nanosegundos = 999.999.999.
   *
   * @param date Data a ter o horário modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o horário 23:59:59'999
   */
  public static LocalDateTime setTimeTo235959(LocalDateTime date) {
    return date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
  }

  /**
   * Este método recebe uma data e força a definição da hora = 00, minuto = 00 e segundos = 00 e milisegundos = 000.
   *
   * @param date Data a ter o horário modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o horário 00:00:00'000
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
   * Este método recebe uma data e força a definição da hora = 00, minuto = 00 e segundos = 00 e nanosegundos = 000.000.000.
   *
   * @param date Data a ter o horário modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o horário 00:00:00'000
   */
  public static LocalDateTime setTimeTo000000(LocalDateTime date) {
    return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
  }

  /**
   * Este método recebe uma data e força a definição de milisegundos = 000.
   *
   * @param date Data a ter o horário modificado.
   * @return Novo objeto com a data recebida como parametro, mas com o horário 00:00:00'000
   */
  public static Date setTimeMillisTo000(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.MILLISECOND, 000);
    return gc.getTime();
  }

  /**
   * Este método conta quandos minutos se passaram desde o tempo passado até agora.
   *
   * @param miliseconds
   * @return
   */
  public static double countMinutesFrom(long miliseconds) {
    return (System.currentTimeMillis() - miliseconds) / 60000;
  }

  /**
   * Calcula o número de meses entre uma data e outra. Note que mesmo que as datas sejam diferentes, se estiverem dentro do mesmo mês e ano, o valor retornado será zero.<br>
   * Caso a data final esteja em um mês anterior ao data inicial, o valor retornado será negativo.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static int calcDiferenceInMonths(LocalDate initialDate, LocalDate finalDate) {
    Period age = Period.between(initialDate.withDayOfMonth(1), finalDate.withDayOfMonth(1)); // Colocamos ambas as data no dia 1, para que o Period faça o cálculo exatamente entre meses e não dê valores quebrados entre os dias.
    return age.getMonths() + age.getYears() * 12;
  }

  /**
   * Calcula o número de dias entre uma data e outra. Só contabiliza o período completo. (Arredonda para baixo)<br>
   *
   * @param initialDate data inicial (inclusivo)
   * @param finalDate data final (exclusivo).
   * @return total de dias entre a data inicial e a final, sem contar a data final. Só contabiliza dias completos, se, por conta do horário, não formar 24h, o dia não é contabilizado.
   */
  public static long calcDiferenceInDays(LocalDate initialDate, LocalDate finalDate) {
    return ChronoUnit.DAYS.between(initialDate, finalDate);
  }

  /**
   * Calcula o número de dias entre uma data e outra. Só contabiliza o período completo. (Arredonda para baixo)<br>
   *
   * @param initialDate data inicial (inclusivo)
   * @param finalDate data final (exclusivo).
   * @return total de dias entre a data inicial e a final, sem contar a data final. Só contabiliza dias completos, se, por conta do horário, não formar 24h, o dia não é contabilizado.
   */
  public static long calcDiferenceInDays(LocalDateTime initialDate, LocalDateTime finalDate) {
    return ChronoUnit.DAYS.between(initialDate, finalDate);
  }

  /**
   * Calcula o tempo entre duas datas dado em dias. Retorna o tempo negativo caso a data inicial seja futura em relação a data final. Embora não tenha tempo negativo, a referência negativo ajuda a validar a ordem cronológica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInDays(Date initialDate, Date finalDate) {
    return calcDiferenceInDays(initialDate.getTime(), finalDate.getTime());
  }

  /**
   * Calcula o tempo entre duas datas dado em Dias. Retorna o tempo negativo caso a data inicial seja futura em relação a data final. Embora não tenha tempo negativo, a referência negativo ajuda a validar a ordem cronológica.
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
   * Calcula o tempo entre duas datas dado em horas. Retorna o tempo negativo caso a data inicial seja futura em relação a data final. Embora não tenha tempo negativo, a referência negativo ajuda a validar a ordem cronológica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInHours(Date initialDate, Date finalDate) {
    return calcDiferenceInHours(initialDate.getTime(), finalDate.getTime());
  }

  /**
   * Calcula o tempo entre duas datas dado em horas. Retorna o tempo negativo caso a data inicial seja futura em relação a data final. Embora não tenha tempo negativo, a referência negativo ajuda a validar a ordem cronológica.
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
   * Calcula o tempo entre duas datas dado em minutos. Retorna o tempo negativo caso a data inicial seja futura em relação a data final. Embora não tenha tempo negativo, a referência negativo ajuda a validar a ordem cronológica.
   *
   * @param initialDate
   * @param finalDate
   * @return
   */
  public static double calcDiferenceInMinutes(Date initialDate, Date finalDate) {
    return calcDiferenceInMinutes(initialDate.getTime(), finalDate.getTime());
  }

  /**
   * Calcula o tempo entre duas datas dado em minutos. Retorna o tempo negativo caso a data inicial seja futura em relação a data final. Embora não tenha tempo negativo, a referência negativo ajuda a validar a ordem cronológica.
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
   * Este método utiliza o SimpleDateFormat para formar o Date apenas no horário com o patern '23:59:59', ignorando qualquer que seja a data.
   *
   * @param time
   * @return
   */
  public static String formatTo235959(Date time) {
    return new SimpleDateFormat("HH:mm:ss").format(time);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'yyyyMMddHHmmss'.
   *
   * @param date
   * @return
   */
  public static String formatToyyyyMMddHHmmss(Date date) {
    return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'yyyyMMdd'.
   *
   * @param date
   * @return
   */
  public static String formatToyyyyMMdd(Date date) {
    return new SimpleDateFormat("yyyyMMdd").format(date);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'ddMMyyyy'.
   *
   * @param date
   * @return
   */
  public static String formatToddMMyyyy(Date date) {
    return new SimpleDateFormat("ddMMyyyy").format(date);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'ddMMyyyyHHmmss'.
   *
   * @param date
   * @return
   */
  public static String formatToddMMyyyyHHmmss(Date date) {
    return new SimpleDateFormat("ddMMyyyyHHmmss").format(date);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'dd/MM/yyyy HH:mm:ss'.
   *
   * @param date
   * @return
   */
  public static String formatTodd_MM_yyyy_HH_mm_ss(Date date) {
    return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'dd/MM/yyyy HH:mm:ss'.
   *
   * @param date
   * @return
   */
  public static String formatTodd_MM_yyyy_HH_mm_ss(LocalDateTime date) {
    return formatLocalDateTime(date, "dd/MM/yyyy HH:mm:ss");
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'yyyy-MM-dd'T'HH:mm:ssXXX' (Padrão UTC utilizado no XML da NFe).
   *
   * @param date
   * @return
   */
  public static String formatToyyyy_MM_dd_T_HH_mm_ssXXX(Date date) {
    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um formato completo com o patern 'yyyy-MM-dd'T'HH:mm:ssXXX' (Padrão UTC utilizado no XML da NFe).
   *
   * @param date
   * @return
   */
  public static String formatToyyyy_MM_dd_T_HH_mm_ssXXX(LocalDateTime date, ZoneId zoneId) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    return date.atZone(zoneId).format(formatter);
  }

  /**
   * Este método utiliza o SimpleDateFormat para formar o Date em um TimeStamp com o patern 'yyyyMMddHHmmss'.
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
   * Este método utiliza o SimpleDateFormat para formar o Date apenas no horário com o patern '23:59:59', ignorando qualquer que seja a data.
   *
   * @param dLastConsult
   * @return
   */
  public static String formatTo235959(long timemillis) {
    return formatTo235959(new Date(timemillis));
  }

  /**
   * Simplifica a funcionalidade de somar/subtrair à uma date.
   *
   * @param date Data base para a operação.
   * @param period Define o periodo a ser adicionado, se será em dias, meses, horas, minutos, etc. Valores deste atributo podem ser encontrados em {@link Calendar}. Ex: {@link Calendar#MONTH}.
   * @param amount Defina a quantidade de período que deve ser somado/subtraído. Número negativos fazem com que o valor seja subtraído da data base.
   * @return
   */
  public static Date calcDateAdd(Date date, int period, int amount) {
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    gc.add(period, amount);
    return gc.getTime();
  }

  /**
   * Retorna o dia do mês de uma determinada data com base no calendário Gregoriano.
   *
   * @param date Data base extração do valor.
   * @return 1 para o primeiro dia do mês, e assim sucessivamente até 28, 29, 30 ou 31 para o último dia do mês dependendo do mês corrente.
   */
  public static int getDayOfMonth(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Retorna o mês de uma determinada data com base no calendário Gregoriano.
   *
   * @param date Data base extração do valor.
   * @return 1 para o mês de Janeiro, e segue sucessivamente até 12 para o Dezembro.
   */
  public static int getMonth(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.MONTH) + 1; // Soma 1 porque a função do Java retorna de 0 à 11.
  }

  /**
   * Retorna o ano de uma determinada data com base no calendário Gregoriano.
   *
   * @param date Data base extração do valor.
   * @return O número do ano com 4 dígitos.
   */
  public static int getYear(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.YEAR);
  }

  /**
   * Verifica se o dia de uma determinada data é o primeiro dia do mês com base no calendário Gregoriano.
   *
   * @param date Data base para análise
   * @return true caso seja o primeiro dia do mês, false caso contrário.
   */
  public static boolean isFirstDayOfMonth(Date date) {
    return getDayOfMonth(date) == 1;
  }

  /**
   * Verifica se o dia de uma determinada data é o último dia do mês com base no calendário Gregoriano.
   *
   * @param date Data base para análise
   * @return true caso seja o último dia do mês, false caso contrário.
   */
  public static boolean isLastDayOfMonth(Date date) {
    return getDayOfMonth(date) == getLastDayOfMonth(date);
  }

  /**
   * Retorna muda o dia para o primeiro dia do Mês da data passada.<br>
   * ATENÇÃO: este método não altera o tempo. Se desejar colocar o primeiro momento do mês utilize em conjunto com o método {@link #setTimeTo000000(Date)};
   *
   * @param date Data de referência
   * @return
   */
  public static Date getFirstDateOfMonth(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.DAY_OF_MONTH, 1);
    return gc.getTime();
  }

  /**
   * Retorna muda o dia para o último dia do Mês da data passada.<br>
   * ATENÇÃO: este método não altera o tempo. Se desejar colocar o último momento do mês utilize em conjunto com o método {@link #setsetTimeTo235959(Date)};
   *
   * @param date Data de referência
   * @return
   */
  public static Date getLastDateOfMonth(Date date) {
    final Calendar gc = GregorianCalendar.getInstance();
    gc.setTime(date);
    gc.set(Calendar.DAY_OF_MONTH, getLastDayOfMonth(date));
    return gc.getTime();
  }

  /**
   * Recupera o último dia do mês de uma determinada data, de acordo com o calendário Gregoriano.
   *
   * @param date Data a ser examinada, será analizado o mês e o ano desta data para determinar o último dia do mês.
   * @return 28, 29, 30 ou 31 de acordo com o mês e ano da data passada, indicando o último dia do mês.
   */
  public static int getLastDayOfMonth(Date date) {
    final Calendar c = GregorianCalendar.getInstance();
    c.setTime(date);
    return c.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  /**
   * Recupera o último dia do mês de uma determinada data, de acordo com o calendário Gregoriano.
   *
   * @param date Data a ser examinada, será analizado o mês e o ano desta data para determinar o último dia do mês.
   * @return 28, 29, 30 ou 31 de acordo com o mês e ano da data passada, indicando o último dia do mês.
   */
  public static int getLastDayOfMonth(LocalDate date) {
    return date.plusMonths(1).withDayOfMonth(1).plusDays(-1).getDayOfMonth();
  }

  /**
   * Recupera um {@link LocalDate} com o último dia do mês de uma determinada data, de acordo com o calendário Gregoriano.
   *
   * @param date Data a ser examinada, será analizado o mês e o ano desta data para determinar o último dia do mês.
   * @return LocalDate apontando a data do último dia do mês.
   */
  public static LocalDate getLastLocalDateOfMonth(LocalDateTime date) {
    return getLastLocalDateOfMonth(date.toLocalDate());
  }

  /**
   * Recupera um {@link LocalDate} com o último dia do mês de uma determinada data, de acordo com o calendário Gregoriano.
   *
   * @param date Data a ser examinada, será analizado o mês e o ano desta data para determinar o último dia do mês.
   * @return LocalDate apontando a data do último dia do mês.
   */
  public static LocalDate getLastLocalDateOfMonth(LocalDate date) {
    return date.plusMonths(1).withDayOfMonth(1).plusDays(-1);
  }

  /**
   * Retorna o nome do mês por extenso de acordo com o locale.
   *
   * @param locale Locale para saber como traduzir o nome do mês.
   * @param Month Mês do calendário gregoriano para se obter o nome do mês. O valor começa em 1 para Janeiro e termina em 12 para Dezembro.
   * @return Nome do mês por exenso de acordo com os parâmetros passados.
   */
  public static String getMonthName(Locale locale, int month) {
    return new DateFormatSymbols(locale).getMonths()[month - 1];
  }

  /**
   * Retorna o nome curto (normalmente só 3 letras) do mês por extenso de acordo com o locale.
   *
   * @param locale Locale para saber como traduzir o nome do mês.
   * @param Month Mês do calendário gregoriano para se obter o nome do mês. O valor começa em 1 para Janeiro e termina em 12 para Dezembro.
   * @return Nome do mês por exenso de acordo com os parâmetros passados.
   */
  public static String getMonthShortName(Locale locale, int month) {
    return new DateFormatSymbols(locale).getShortMonths()[month - 1];
  }

  /**
   * Formata um tempo em milisegundos em "HHH:MM:SS'mmm", onde HHH são horas, mesmo acima de 24 horas, MM os mínutos, SS os segundos e mmm os milisegundos.<br>
   * Os vampos zerados serão eliminados do resultado. Por exemplo, se não tivermos horas, o resultado será "MM:SS'mmm"
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
   * @param pattern Pattern que segue o padrão do {@link SimpleDateFormat}
   * @param millis Tempo em milisegundos para ser formatado
   * @return Tempo formatado
   */
  public static String formatMillis(String pattern, long millis) {
    SimpleDateFormat sf = new SimpleDateFormat(pattern);
    sf.setTimeZone(TimeZone.getTimeZone("UTC")); // joga para o UTC para que não considere o TimeZone do local. Caso contrário as horas do fuso vão aparecer no resultado formatado.
    return sf.format(new Date(millis));
  }

  /**
   * Este método gere o sufixo do nome do arquivo para padronizar o nome de acordo com um período de datas.<br>
   * Casos implementados até agora:<br>
   * <li>Se o período é o mês completo (do primeiro dia ao último): "Agosto-2017"</li>
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
      // Se o periodo começa no primeiro dia do Mês e Termina no último dia do mesmo Mês
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
   *         <li>{@link Calendar#TUESDAY} - Terça
   *         <li>{@link Calendar#WEDNESDAY} - Quarta
   *         <li>{@link Calendar#THURSDAY} - Quinta
   *         <li>{@link Calendar#FRIDAY} - Sexta
   *         <li>{@link Calendar#SATURDAY} - Sábado
   *         <li>{@link Calendar#SUNDAY} - Domingo
   */
  public static int getWeekDay(Date date) {
    final Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_WEEK);
  }

  /**
   * Retorna quantos dias os dois períodos se sobrepõe.<br>
   * NOTE QUE TODAS AS DATA SÃO INCLUSIVAS, por tanto, casos os períodos estejam "encostados" e a data final de um periodo for igual a data inicial do outro periodo, o método retornará 1.
   *
   * @param period1Start Data de início do primeiro período.
   * @param period1End Data de fim do primeiro período.
   * @param period2Start Data de início do segundo período.
   * @param period2End Data de fim do segundo período.
   * @return Número de dias que os períodos se sobrepõe.
   * @throws RFWException
   */
  public static long calcOverlappingDays(LocalDate period1Start, LocalDate period1End, LocalDate period2Start, LocalDate period2End) throws RFWException {
    // Validamos primeiro que os dois periodos estejam ok
    if (period1End.isBefore(period1Start)) throw new RFWValidationException("O data de fim do primeiro período é anterior a data de início!");
    if (period2End.isBefore(period2Start)) throw new RFWValidationException("O data de fim do segundo período é anterior a data de início!");

    // Se um termina antes do outro começar, não há sobreposição
    if (period1End.isBefore(period2Start) || period2End.isBefore(period1Start)) return 0;

    // Eliminando os casos em que não há sobreposição, a sobreprosição é o resultado da maior data de início com a menor data de fim
    LocalDate start = period1Start;
    LocalDate end = period1End;
    if (period1Start.isBefore(period2Start)) start = period2Start;
    if (period1End.isAfter(period2End)) end = period2End;

    return ChronoUnit.DAYS.between(start, end.plusDays(1));
  }

  /**
   * Este método interpreta todos os formatos já encontrados de datas que podem vir na NFe para o formato do Java. Atualmente os formatos reconhecidos são:<br>
   * <li>"yyyy-MM-dd'T'HH:mm:ssXXX", onde XXX é algo como "-07:00" (Padrão UTC)</li>
   * <li>"yyyy-MM-dd'T'HH:mm:ssZ", onde Z é algo como "-0700" (Padrão UTC)</li>
   * <li>"yyyy-MM-dd'T'HH:mm:ss" (Padrão UTC Sem TimeZone)</li>
   * <li>"yyyy-MM-dd"</li>
   * <li>"dd/MM/yyyy"</li>
   *
   * @param date Data com os valores recebidos na String. Na ausência de um TimeZone é considerado que o TImeZone
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
        throw new RFWValidationException("Formato da Data não está em formato não suportado por este método. Data: '${0}'", new String[] { date });
      }
    }
    return null;

  }

  /**
   * Cria um objeto Date com uma data específica.
   *
   * @param year O ano a ser utilizado na data.
   * @param month O Mês do ano a ser utilizado. 1 para janeiro e 12 para Dezembro.
   * @param dayOfMonth Dia do mês a set utilizado. 1 para dia 1, e assim sucessivamente.
   * @param hour Hora do dia a ser utilizado, variando de 0 à 23.
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
   * Cria um objeto LocalDateTime com uma data específica.
   *
   * @param year O ano a ser utilizado na data.
   * @param month O Mês do ano a ser utilizado. 1 para janeiro e 12 para Dezembro.
   * @param dayOfMonth Dia do mês a set utilizado. 1 para dia 1, e assim sucessivamente.
   * @param hour Hora do dia a ser utilizado, variando de 0 à 23.
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
   * Cria um objeto LocalDate com uma data específica.
   *
   * @param year O ano a ser utilizado na data.
   * @param month O Mês do ano a ser utilizado. 1 para janeiro e 12 para Dezembro.
   * @param dayOfMonth Dia do mês a set utilizado. 1 para dia 1, e assim sucessivamente.
   * @return Objeto Data com a data configurada.
   */
  public static LocalDate createLocalDate(int year, int month, int dayOfMonth) {
    return LocalDate.of(year, month, dayOfMonth);
  }
}
