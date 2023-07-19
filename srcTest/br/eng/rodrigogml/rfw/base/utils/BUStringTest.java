package br.eng.rodrigogml.rfw.base.utils;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BUStringTest {

  @Test
  public void t00_validateRemoveLeadingZeros() throws RFWException {
    assertEquals("1234", RUString.removeLeadingZeros("0001234"));
    assertEquals(null, RUString.removeLeadingZeros(null));
    assertEquals("12340000", RUString.removeLeadingZeros("00012340000"));
    assertEquals(" 00012340000", RUString.removeLeadingZeros(" 00012340000"));
    assertEquals("", RUString.removeLeadingZeros("000000000000000000"));
  }

  @Test
  public void t01_parseCSVLine() throws RFWException {
    String line = "10,AU,Australia";
    String[] p = RUString.parseCSVLine(line, ',', '"');
    assertEquals("10", p[0]);
    assertEquals("AU", p[1]);
    assertEquals("Australia", p[2]);

    line = "11,AU,Aus\"\"tralia";
    p = RUString.parseCSVLine(line, ',', '"');
    assertEquals("11", p[0]);
    assertEquals("AU", p[1]);
    assertEquals("Aus\"tralia", p[2]);

    line = "\"12\",\"AU\",\"Australia\"";
    p = RUString.parseCSVLine(line, ',', '"');
    assertEquals("12", p[0]);
    assertEquals("AU", p[1]);
    assertEquals("Australia", p[2]);

    line = "\"13\",\"AU\",\"Aus\"\"tralia\"";
    p = RUString.parseCSVLine(line, ',', '"');
    assertEquals("13", p[0]);
    assertEquals("AU", p[1]);
    assertEquals("Aus\"tralia", p[2]);

    line = "\"14\",\"AU\",\"Aus,tralia\"";
    p = RUString.parseCSVLine(line, ',', '"');
    assertEquals("14", p[0]);
    assertEquals("AU", p[1]);
    assertEquals("Aus,tralia", p[2]);
  }

  @Test
  public void t02_genStringWithLength() throws RFWException {
    assertEquals("          ", RUString.genString(10, " "));
    assertEquals("          ", RUString.genString(5, "  "));
    assertEquals("rarararara", RUString.genString(5, "ra"));
  }

  @Test
  public void t03_testStringToNull() throws RFWException {
    assertEquals("Rodrigo Leitão", RUString.replaceDoubleSpaces(RUString.replaceTabsByUniqueSpace("Rodrigo\tLeitão")).trim());
    assertEquals("Rodrigo Leitão", RUString.replaceDoubleSpaces(RUString.replaceTabsByUniqueSpace("   Rodrigo      Leitão     ")).trim());
    assertEquals("Rodrigo Leitão", RUString.replaceDoubleSpaces(RUString.replaceTabsByUniqueSpace("   Rodrigo   \t\t\t   Leitão     ")).trim());
  }

  @Test
  public void t04_extractDecimalValues() {
    String init = "bla bla blasxe";
    String end = "asdalsdfadf asdf asdf af";

    String[] values = new String[] { "1,24", "1.456,00", "41.455.245,6", "1.455.245,6", "1455245,6" };

    // Itera todos os valores considerando a String no começo da linha, no fim da linha, sozinha na linha, ou com conteúdo no início e no fim
    for (int i = 0; i < values.length; i++) {
      assertEquals("Falha no Valor: " + values[i], values[i], RUString.extractDecimalValues(values[i], 1, true));
      assertEquals("Falha no Valor: " + values[i], values[i], RUString.extractDecimalValues(init + values[i], 1, true));
      assertEquals("Falha no Valor: " + values[i], values[i], RUString.extractDecimalValues(values[i] + end, 1, true));
      assertEquals("Falha no Valor: " + values[i], values[i], RUString.extractDecimalValues(init + values[i] + end, 1, true));
    }

  }
}
