package br.eng.rodrigogml.rfw.base.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BUGeneratorsTest {

  @Test
  public void t00_generateString() {
    // Testa a geração de 100 Strings
    for (int i = 0; i < 100; i++) {
      Assert.assertTrue("String gerada fora do padrão prometido!", RUGenerators.generateString(300).matches("[0-9a-zA-Z]{300}"));
    }
  }

  @Test
  public void t01_generateUUID() throws Throwable {
    for (int i = 0; i < 1000; i++) {
      String uuid = RUGenerators.generateUUID();
      // System.out.println(uuid);
      assertTrue("UUID fora do padrão esperado!", uuid.matches("[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}"));
    }
  }
}
