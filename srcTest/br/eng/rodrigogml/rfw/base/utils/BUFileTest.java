package br.eng.rodrigogml.rfw.base.utils;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

import br.eng.rodrigogml.rfw.base.utils.BUFile;

/**
 * Description: Classe de teste da classe {@link BUFile}.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (25 de nov de 2020)
 */
public class BUFileTest {

  @Test
  public void scrapPDFTest() throws Exception {
    String pdfPath = BUFileTest.class.getResource("/resources/SamplePDFFile.pdf").getPath();
    String scrapPath = BUFile.scrapPDFText(pdfPath, "== PAGE ${0} ==");

    boolean foundPageMarker1 = false;
    boolean foundPageMarker2 = false;
    boolean foundLine1 = false;

    try (BufferedReader reader = new BufferedReader(new FileReader(scrapPath))) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
        if ("== PAGE 1 ==".equals(line)) foundPageMarker1 = true;
        if ("== PAGE 2 ==".equals(line)) foundPageMarker2 = true;
        if ("OS ITENS ABAIXO FORAM RESGATADOS COM SUCESSO!".equals(line)) foundLine1 = true;
      }
    }

    assertTrue("O Marcador de Página 1 não foi encontrado!", foundPageMarker1);
    assertTrue("O Marcador de Página 2 não foi encontrado!", foundPageMarker2);
    assertTrue("A linha procurada 1 não foi encontrada!", foundLine1);
  }

}
