package br.eng.rodrigogml.rfw.base.utils;

import java.nio.charset.StandardCharsets;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.eng.rodrigogml.rfw.base.utils.BUIO;
import br.eng.rodrigogml.rfw.base.utils.BUMarkdown;
import br.eng.rodrigogml.rfw.base.utils.BUString;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BUMarkdownTest {

  @Test
  public void t00_convertToHTML() throws Throwable {
    String markdown = BUIO.readToString(BUMarkdownTest.class.getResourceAsStream("/resources/markdown/BUMarkdownTest_markdown.txt"), StandardCharsets.UTF_8);
    String html = BUIO.readToString(BUMarkdownTest.class.getResourceAsStream("/resources/markdown/BUMarkdownTest_html.txt"), StandardCharsets.UTF_8);

    String converted = BUMarkdown.convertMarkdownToHTML(markdown.toString());
    BUString.validateEqualsString(html, converted);
  }
}
