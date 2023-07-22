package br.eng.rodrigogml.rfw.base.utils;

import java.text.DateFormatSymbols;
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

import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;
import br.eng.rodrigogml.rfw.kernel.utils.RUDateTime;
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
    final boolean startOnFirstDayOfMonth = RUDateTime.isFirstDayOfMonth(startDate);
    final boolean endOnLastDayOfMonth = RUDateTime.isLastDayOfMonth(endDate);
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
}
