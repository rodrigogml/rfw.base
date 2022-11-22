package br.eng.rodrigogml.rfw.base.utils;

/**
 * Description: Classe utilitária para os métodos auxiliares do Wiki do RFW.<br>
 * O objetivo deste Wiki é ter um padrão parecido com o do wikipedia que permita que relatórios sejam escritos em um formato comum do sistema, e que ele possa ser traduzido para outros formatos, como os padrões de impressão direto em impressora, HTML, XML, PDF, RichText, etc.
 *
 * @author Rodrigo Leitão
 * @since 10.0 (30 de nov. de 2021)
 */
public class BUWiki {

  /**
   * Construtor privado, classe utilitaria totalmente estática.
   */
  private BUWiki() {
  }

  /**
   * Recupera conteúdo em HTML que pode ser exibido para o usuário para ensina-lo a utilizar o RFWWiki.
   *
   * @return Texto em formato HTML com tutorial para ensinar o RFWWiki.
   */
  public static String getFWWikiManual_HTML() {
    StringBuilder buff = new StringBuilder();
    buff.append("<b>Formatação de Texto:</b>");
    buff.append("<ul>");
    buff.append("<b>'''</b> Inicia/termina negrito.<br>");
    buff.append("<b>''</b> Inicia/termina itálico.<br>");
    buff.append("</ul>");
    buff.append("<b>Lista com Marcadores e Lista Numerada:</b>");
    buff.append("<ul>");
    buff.append("<b>+#</b> Colocado no início da linha, inicia uma lista numerada.<br>");
    buff.append("<b>-#</b> No início da linha, termina uma lista numerada.<br>");
    buff.append("<b>+*</b> No início da linha, inicia uma lista de itens (marcadores).<br>");
    buff.append("<b>-*</b> No início da linha, termina uma lista de itens (marcadores).<br>");
    buff.append("<b>**</b> No início da linha, escreve um item de lista.<br>");
    buff.append("</ul>");
    return buff.toString();
  }

}
