package br.eng.rodrigogml.rfw.base.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BUDateTimeTest {

  @Test
  public void t00_BUDateTime_convertDateTime() throws Throwable {
    String[] dates = {
        "1982-03-25T01:02:03-03:00",
        "1982-03-25T01:02:03-0300",
        "1982-03-25T00:02:03-0400",
        "1982-03-25T01:02:03"
    };

    Date date = new GregorianCalendar(1982, 2, 25, 1, 2, 3).getTime(); // OBS: NO GregorianCalentar o mês de Janeiro é o 0.
    LocalDateTime localDateTime = LocalDateTime.of(1982, Month.MARCH, 25, 1, 2, 3);
    LocalDate localDate = LocalDate.of(1982, Month.MARCH, 25);

    for (int i = 0; i < dates.length; i++) {
      Date tmpDate = BUDateTime.parseDate(dates[i]);
      assertTrue("Falha na Date: " + i, tmpDate.compareTo(date) == 0);

      LocalDateTime tmpLocalDateTime = BUDateTime.parseLocalDateTime(dates[i]);
      assertTrue("Falha na LocalDateTime: " + i, tmpLocalDateTime.compareTo(localDateTime) == 0);

      LocalDate tmpLocalDate = BUDateTime.parseLocalDate(dates[i]);
      assertTrue("Falha na LocalDate: " + i, tmpLocalDate.compareTo(localDate) == 0);
    }
  }

  @Test
  public void t01_BUDateTime_convertDate() throws Throwable {
    String[] dates = {
        "1982-03-25T00:00:00-03:00",
        "1982-03-25T00:00:00-0300",
        "1982-03-25T01:00:00-0200",
        "1982-03-24T23:00:00-0400",
        "1982-03-25T00:00:00",
        "1982-03-25",
    };

    Date date = new GregorianCalendar(1982, 2, 25).getTime(); // OBS: NO GregorianCalentar o mês de Janeiro é o 0.
    LocalDateTime localDateTime = LocalDateTime.of(1982, Month.MARCH, 25, 0, 0, 0);
    LocalDate localDate = LocalDate.of(1982, Month.MARCH, 25);

    for (int i = 0; i < dates.length; i++) {
      Date tmpDate = BUDateTime.parseDate(dates[i]);
      assertTrue("Falha na Date: " + i, tmpDate.compareTo(date) == 0);

      LocalDateTime tmpLocalDateTime = BUDateTime.parseLocalDateTime(dates[i]);
      assertTrue("Falha na LocalDateTime: " + i, tmpLocalDateTime.compareTo(localDateTime) == 0);

      LocalDate tmpLocalDate = BUDateTime.parseLocalDate(dates[i]);
      assertTrue("Falha na LocalDate: " + i, tmpLocalDate.compareTo(localDate) == 0);
    }
  }

  @Test
  public void t02_BUDateTime_calcDaysBetweenDates() throws Throwable {
    LocalDate d20220101 = LocalDate.of(2022, 1, 1);
    LocalDate d20220201 = LocalDate.of(2022, 2, 1);
    LocalDate d20220301 = LocalDate.of(2022, 3, 1);
    LocalDate d20220325 = LocalDate.of(2022, 3, 25);

    assertEquals(24, BUDateTime.calcDiferenceInDays(d20220301, d20220325));
    assertEquals(31, BUDateTime.calcDiferenceInDays(d20220101, d20220201));
    assertEquals(28, BUDateTime.calcDiferenceInDays(d20220201, d20220301));
  }

}
