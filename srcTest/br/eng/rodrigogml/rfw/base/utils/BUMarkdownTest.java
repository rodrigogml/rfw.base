package br.eng.rodrigogml.rfw.base.utils;

import java.nio.charset.StandardCharsets;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BUMarkdownTest {

  @Test
  public void t00_convertToHTML() throws Throwable {
    String markdown = BUIO.readToString(BUMarkdownTest.class.getResourceAsStream("/resources/markdown/BUMarkdownTest_markdown.txt"), StandardCharsets.UTF_8);
    String html = BUIO.readToString(BUMarkdownTest.class.getResourceAsStream("/resources/markdown/BUMarkdownTest_html.txt"), StandardCharsets.UTF_8);

    String converted = BUMarkdown.convertMarkdownToHTML(markdown.toString());
    RUString.validateEqualsString(html, converted);
  }
}
