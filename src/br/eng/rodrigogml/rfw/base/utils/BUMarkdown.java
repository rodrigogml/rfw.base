package br.eng.rodrigogml.rfw.base.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe est�tica com m�todos utilit�rios para a interpreta��o da linguagem MarkDown.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (31 de out de 2020)
 */
public class BUMarkdown {

  /**
   * Construtor privado para classe est�tica.
   */
  private BUMarkdown() {
  }

  public static String convertMarkdownToHTML(String content) throws RFWException {
    final StringBuilder buff = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new StringReader(content))) {

      String line = null;

      boolean beforeBR = false; // Flags para indicar a linha anterior exige um <br>

      while ((line = reader.readLine()) != null) {
        if (line.length() > 0) {
          // Substitui todos os caracteres espec�ficos
          line = line.replaceAll("\\&", "&amp;");
          line = line.replaceAll("\\<", "&lt;");

          boolean nextBR = true;

          final String[] prefixSplit = line.split(" ");
          if (prefixSplit.length > 0) {
            String linePrefix = prefixSplit[0];
            // ##> Processa os formatores de in�cio de linha
            if ("=====".equals(linePrefix)) {
              line = new StringBuilder().append("<h5>").append(BUString.subString(line, 6, line.length())).append("</h5>").toString();
              beforeBR = false;
              nextBR = false;
            } else if ("====".equals(linePrefix)) {
              line = new StringBuilder().append("<h4>").append(BUString.subString(line, 5, line.length())).append("</h4>").toString();
              beforeBR = false;
              nextBR = false;
            } else if ("===".equals(linePrefix)) {
              line = new StringBuilder().append("<h3>").append(BUString.subString(line, 4, line.length())).append("</h3>").toString();
              beforeBR = false;
              nextBR = false;
            } else if ("==".equals(linePrefix)) {
              line = new StringBuilder().append("<h2>").append(BUString.subString(line, 3, line.length())).append("</h2>").toString();
              beforeBR = false;
              nextBR = false;
            } else if ("=".equals(linePrefix)) {
              line = new StringBuilder().append("<h1>").append(BUString.subString(line, 2, line.length())).append("</h1>").toString();
              beforeBR = false;
              nextBR = false;
            } else if ("**".equals(linePrefix)) {
              line = new StringBuilder().append("<li>").append(BUString.subString(line, 3, line.length())).append("</li>").toString();
              beforeBR = false;
              nextBR = false;
            } else if ("+#".equals(linePrefix)) {
              line = new StringBuilder().append("<ol>").append(BUString.subString(line, 3, line.length())).toString();
              beforeBR = false;
              nextBR = false;
            } else if ("-#".equals(linePrefix)) {
              line = new StringBuilder().append("</ol>").append(BUString.subString(line, 3, line.length())).toString();
              beforeBR = false;
              nextBR = false;
            } else if ("+*".equals(linePrefix)) {
              line = new StringBuilder().append("<ul>").append(BUString.subString(line, 3, line.length())).toString();
              beforeBR = false;
              nextBR = false;
            } else if ("-*".equals(linePrefix)) {
              line = new StringBuilder().append("</ul>").append(BUString.subString(line, 3, line.length())).toString();
              beforeBR = false;
              nextBR = false;
            } else if ("--".equals(linePrefix)) {
              line = new StringBuilder().append("<hr>").append(BUString.subString(line, 3, line.length())).toString();
              beforeBR = false;
              nextBR = false;
            }
          }

          // ##> Processa os formatores de conte�do de linha.
          // ####> Negrito e It�lico '''''
          {
            boolean on = false;
            int index = -1;
            while ((index = line.indexOf("'''''")) >= 0) {
              if (index >= 0) {
                if (!on) {
                  line = line.replaceFirst("'''''", "<strong><i>");
                } else {
                  line = line.replaceFirst("'''''", "</i></strong>");
                }
                on = !on;
              }
            }
          }

          // ####> Negrito '''
          {
            boolean on = false;
            int index = -1;
            while ((index = line.indexOf("'''")) >= 0) {
              if (index >= 0) {
                if (!on) {
                  line = line.replaceFirst("'''", "<strong>");
                } else {
                  line = line.replaceFirst("'''", "</strong>");
                }
                on = !on;
              }
            }
          }

          // ####> It�lico ''
          {
            boolean on = false;
            int index = -1;
            while ((index = line.indexOf("''")) >= 0) {
              if (index >= 0) {
                if (!on) {
                  line = line.replaceFirst("''", "<i>");
                } else {
                  line = line.replaceFirst("''", "</i>");
                }
                on = !on;
              }
            }
          }

          if (beforeBR) buff.append("<br>");
          buff.append(line);
          beforeBR = nextBR;
        }
      }
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao l�r String para converter para HTML", e);
    }
    return buff.toString();
  }

}
