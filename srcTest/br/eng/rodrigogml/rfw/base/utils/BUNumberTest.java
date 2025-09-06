package br.eng.rodrigogml.rfw.base.utils;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BUNumberTest {

  @Test
  public void t00_generateNumericSequence() throws Throwable {
    for (long i = 0; i < 10000L; i++) {
      String gen = RUGenerators.generateNumericSequence(8);
      assertEquals("O tamnho da sequencia gerada não é o esperado!", 8, gen.length());
    }
  }
}
