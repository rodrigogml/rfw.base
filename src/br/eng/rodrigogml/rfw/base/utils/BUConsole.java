package br.eng.rodrigogml.rfw.base.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;

/**
 * Description: Classe utilitária com métodos para facilitar a escrita de uma aplicação de Console.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (28 de mai de 2021)
 */
public class BUConsole {

  /**
   * Define a largura do console, utilizado nos cálculos de alinhamento de texto.<br>
   * Pode ser configurado pela aplicação.
   */
  private static int consoleWidth = 200;

  /**
   * Caracter(es) utilizado para quebra de linha.<br>
   * Padrão: \\r\\n
   */
  private static String lineBreak = "\r\n";

  /**
   * {@link OutputStream} utilizado para imprimir o conteúdo. <br>
   * Utiliza por padrão o valor obtido em {@link System#out}, mas pode ser substituído pelo usuário.
   */
  private static OutputStream output = System.out;

  /**
   * Scanner utilizado para lêr os inputs do usuário
   */
  private static Scanner sc = null;

  /**
   * Construtor privado apra classe puramente estática.
   */
  public BUConsole() {
  }

  /**
   * # define a largura do console, utilizado nos cálculos de alinhamento de texto.<br>
   * Pode ser configurado pela aplicação.
   *
   * @return the define a largura do console, utilizado nos cálculos de alinhamento de texto
   */
  public static int getConsoleWidth() {
    return consoleWidth;
  }

  /**
   * # define a largura do console, utilizado nos cálculos de alinhamento de texto.<br>
   * Pode ser configurado pela aplicação.
   *
   * @param consoleWidth the new define a largura do console, utilizado nos cálculos de alinhamento de texto
   */
  public static void setConsoleWidth(int consoleWidth) {
    BUConsole.consoleWidth = consoleWidth;
  }

  /**
   * Imprime um conteúdo de texto centralizado na tela e alinhado no centro.<br>
   * Este método não trunca nem centraliza a segunda linha, simplesmente deixa estourar caso o conteúdo seja maior que a largura do console.
   *
   * @param content Conteúdo a ser impresso.
   * @throws RFWException
   */
  public static void pCenter(String content) throws RFWException {
    int offset = (consoleWidth - content.length()) / 2;
    offset = Math.max(offset, 0);
    write(BUString.completeUntilLengthLeft(" ", "", offset) + content);
  }

  /**
   * Imprime um conteúdo de texto centralizado na tela, alinhado no centro e quebra a linha.<br>
   * Este método não trunca nem centraliza a segunda linha, simplesmente deixa estourar caso o conteúdo seja maior que a largura do console.
   *
   * @param content Conteúdo a ser impresso.
   * @throws RFWException
   */
  public static void pCenterLn(String content) throws RFWException {
    pCenter(content + lineBreak);
  }

  /**
   * Imprime um conteúdo de texto na tela e quebra a linha.<br>
   *
   * @param content Conteúdo a ser impresso.
   * @throws RFWException
   */
  public static void pLn(String content) throws RFWException {
    write(content + lineBreak);
  }

  /**
   * Imprime uma quebra de linha. Se a linha anterior não tiver sido quebrada, quebra a linha atual. Caso contrário cria uma linha em branco.
   *
   * @throws RFWException
   */
  public static void pBR() throws RFWException {
    write(lineBreak);
  }

  /**
   * Imprime um número X de quebras de linhas.<br>
   * Se a linha anterior não tiver sido quebrada, criar X linhas em branco, caso contrário quebra a linha tual e cria (X-1) linhas em branco.
   *
   * @throws RFWException
   */
  public static void pBR(int lines) throws RFWException {
    StringBuilder buff = new StringBuilder();
    for (int i = 0; i < lines; i++) {
      buff.append(lineBreak);
    }
    write(buff.toString());
  }

  /**
   * Faz a escrita de uma String em formato de bytes para o output.
   *
   * @param content Conteúdo a ser escrito
   * @throws RFWException
   */
  private static void write(String content) throws RFWException {
    try {
      output.write(content.getBytes(StandardCharsets.ISO_8859_1));
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao escrever no console!", e);
    }

  }

  /**
   * # {@link OutputStream} utilizado para imprimir o conteúdo. <br>
   * Utiliza por padrão o valor obtido em {@link System#out}, mas pode ser substituído pelo usuário.
   *
   * @return the {@link OutputStream} utilizado para imprimir o conteúdo
   */
  public static OutputStream getOutput() {
    return output;
  }

  /**
   * # {@link OutputStream} utilizado para imprimir o conteúdo. <br>
   * Utiliza por padrão o valor obtido em {@link System#out}, mas pode ser substituído pelo usuário.
   *
   * @param output the new {@link OutputStream} utilizado para imprimir o conteúdo
   */
  public static void setOutput(OutputStream output) {
    BUConsole.output = output;
  }

  /**
   * # caracter(es) utilizado para quebra de linha.<br>
   * Padrão: \\r\\n.
   *
   * @return the caracter(es) utilizado para quebra de linha
   */
  public static String getLineBreak() {
    return lineBreak;
  }

  /**
   * # caracter(es) utilizado para quebra de linha.<br>
   * Padrão: \\r\\n.
   *
   * @param lineBreak the new caracter(es) utilizado para quebra de linha
   */
  public static void setLineBreak(String lineBreak) {
    BUConsole.lineBreak = lineBreak;
  }

  /**
   * Este método tem como finalidade lêr os argumentos passados pelo usuário na linha de comando (inicialização do programa), encontrar um argumento específico e retornar os N argumentos seguintes.
   *
   * @param args Array com os argumentos recebudos na inicialização do programa (parâmetro do método main).
   * @param arg O argumento que deve ser produrado. O método procura sem case sensitive.
   * @param n Número de argumentos que devem ser recuperados DEPOIS do argumento encontrado.
   * @return Retorna nulo caso o array não tenha sido encontrado. Retorna um array com tamanho N caso existam N argumentos depois do argumento procurado. Caso encontre o a correspondência desejada, mas não nenhum argumento seja encontrado na frente retorna um array com tamanho 0. Retorna um array menor que N caso não encontre N argumentos à frente da correspondência.
   */
  public static String[] getArgs(String[] args, String arg, int n) {
    String[] p = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase(arg)) {
        i++;
        p = new String[Math.min(args.length - i, n)];
        for (int x = 0; x < p.length; x++) {
          p[x] = args[i];
          i++;
        }
        break;
      }
    }

    return p;
  }

  /**
   * Imprime um conteúdo com Prefoxo de alerta.
   *
   * @param content Conteúdo à ser impresso.
   * @throws RFWException
   */
  public static void pAlertLn(String content) throws RFWException {
    write("[!!!] " + content + lineBreak);
  }

  /**
   * Lê um conteúdo do Console até que o usuário pressione enter
   *
   * @param prompt Linha digitada anteriormente para solicitar a entrada do usuário.
   * @return String com o conteúdo digitado pelo usuário.
   * @throws RFWException
   */
  public static String askString(String prompt) throws RFWException {
    write(prompt + " ");
    if (System.console() == null) {
      // Se console é nulo, provavelmente estamos no eclipse e lêmos o conteúdo pelo Scanner
      if (sc == null) sc = new Scanner(System.in);
      return sc.nextLine();
    } else {
      return System.console().readLine();
    }
  }

  /**
   * Lê um conteúdo do Console mascarado como Senha
   *
   * @param prompt Linha digitada anteriormente para solicitar a entrada do usuário.
   * @return String com o conteúdo digitado pelo usuário.
   * @throws RFWException
   */
  public static String askPassword(String prompt) throws RFWException {
    write(prompt + " ");
    if (System.console() == null) {
      // Se console é nulo, provavelmente estamos no eclipse e lêmos o conteúdo pelo Scanner
      if (sc == null) sc = new Scanner(System.in);
      return sc.nextLine();
    } else {
      return new String(System.console().readPassword());
    }
  }

  /**
   * Limpa o console. (Não funciona dentro do Eclipse)
   *
   * @throws RFWException
   */
  public static void clear() throws RFWException {
    System.out.print("\033[H\033[2J" + lineBreak);
    System.out.flush();
  }

  /**
   * Este método imprime um modelo de menu com um título e colunas de opções conforme o tamanho so array.
   *
   * @param title Título do menu. Se passar nulo tem o mesmo efeito que o método {@link #pColumns(String[][])}.
   * @param menuOptions Opções do menu
   * @throws RFWException
   */
  public static void pMenu(String title, String[][] menuOptions) throws RFWException {
    if (title != null) {
      pMenuHeader(title);
    }
    pColumns(menuOptions);
    if (title != null) write(BUString.completeUntilLengthLeft("-", "", consoleWidth) + lineBreak);
  }

  /**
   * Imprime um cabeçalho de sessão, utilizado por exemplo pelo método {@link #pMenu(String, String[][])}
   *
   * @param title
   * @throws RFWException
   */
  public static void pMenuHeader(String title) throws RFWException {
    String bars = BUString.completeUntilLengthLeft("#", "", consoleWidth);
    write(bars + lineBreak);
    write(BUString.completeUntilLengthRight(" ", "# " + title, consoleWidth - 1) + "#" + lineBreak);
    write(bars + lineBreak);
  }

  /**
   * Escreve um conteúdo em colunas conforme a dimensão do array.
   *
   * @param content Array com o conteúdo para ser impresso em colunas.
   * @throws RFWException
   */
  public static void pColumns(String[][] content) throws RFWException {
    int[] cols = new int[content[0].length];
    for (int i = 0; i < cols.length; i++) {
      cols[i] = consoleWidth * (i + 1) / cols.length;
      if (i > 0) for (int x = i - 1; x >= 0; x--)
        cols[i] = cols[i] - cols[x];
    }
    for (int r = 0; r < content.length; r++) {
      for (int c = 0; c < content[r].length; c++) {
        write(BUString.completeOrTruncateUntilLengthRight(" ", content[r][c], cols[c]));
      }
      write(lineBreak);
    }
  }

  /**
   * Similar ao comando pause do DOS, faz uma pausa no sistema até que o usuário pressione ENTER para continuar.
   *
   * @throws RFWException
   */
  public static void pause() throws RFWException {
    askPassword("Pressione ENTER para continuar...");
  }

  /**
   * Imprime uma linha de separação repetindo a cadeia de caracteres recebida até chegar no tamanho da coluna. Caso a quantidades de caracteres não seja divisível pela largura da coluna, o conteúdo final é truncado.
   *
   * @param sepChars Caracteres que serão utilizados para criar a linha de separação.
   * @throws RFWException
   */
  public static void pSep(String sepChars) throws RFWException {
    write(BUString.truncate(BUString.completeOrTruncateUntilLengthRight(sepChars, "", consoleWidth), consoleWidth) + lineBreak);
  }
}
