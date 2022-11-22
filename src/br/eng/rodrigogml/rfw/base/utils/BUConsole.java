package br.eng.rodrigogml.rfw.base.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import br.eng.rodrigogml.rfw.base.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.base.exceptions.RFWException;

/**
 * Description: Classe utilit�ria com m�todos para facilitar a escrita de uma aplica��o de Console.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (28 de mai de 2021)
 */
public class BUConsole {

  /**
   * Define a largura do console, utilizado nos c�lculos de alinhamento de texto.<br>
   * Pode ser configurado pela aplica��o.
   */
  private static int consoleWidth = 200;

  /**
   * Caracter(es) utilizado para quebra de linha.<br>
   * Padr�o: \\r\\n
   */
  private static String lineBreak = "\r\n";

  /**
   * {@link OutputStream} utilizado para imprimir o conte�do. <br>
   * Utiliza por padr�o o valor obtido em {@link System#out}, mas pode ser substitu�do pelo usu�rio.
   */
  private static OutputStream output = System.out;

  /**
   * Scanner utilizado para l�r os inputs do usu�rio
   */
  private static Scanner sc = null;

  /**
   * Construtor privado apra classe puramente est�tica.
   */
  public BUConsole() {
  }

  /**
   * # define a largura do console, utilizado nos c�lculos de alinhamento de texto.<br>
   * Pode ser configurado pela aplica��o.
   *
   * @return the define a largura do console, utilizado nos c�lculos de alinhamento de texto
   */
  public static int getConsoleWidth() {
    return consoleWidth;
  }

  /**
   * # define a largura do console, utilizado nos c�lculos de alinhamento de texto.<br>
   * Pode ser configurado pela aplica��o.
   *
   * @param consoleWidth the new define a largura do console, utilizado nos c�lculos de alinhamento de texto
   */
  public static void setConsoleWidth(int consoleWidth) {
    BUConsole.consoleWidth = consoleWidth;
  }

  /**
   * Imprime um conte�do de texto centralizado na tela e alinhado no centro.<br>
   * Este m�todo n�o trunca nem centraliza a segunda linha, simplesmente deixa estourar caso o conte�do seja maior que a largura do console.
   *
   * @param content Conte�do a ser impresso.
   * @throws RFWException
   */
  public static void pCenter(String content) throws RFWException {
    int offset = (consoleWidth - content.length()) / 2;
    offset = Math.max(offset, 0);
    write(BUString.completeUntilLengthLeft(" ", "", offset) + content);
  }

  /**
   * Imprime um conte�do de texto centralizado na tela, alinhado no centro e quebra a linha.<br>
   * Este m�todo n�o trunca nem centraliza a segunda linha, simplesmente deixa estourar caso o conte�do seja maior que a largura do console.
   *
   * @param content Conte�do a ser impresso.
   * @throws RFWException
   */
  public static void pCenterLn(String content) throws RFWException {
    pCenter(content + lineBreak);
  }

  /**
   * Imprime um conte�do de texto na tela e quebra a linha.<br>
   *
   * @param content Conte�do a ser impresso.
   * @throws RFWException
   */
  public static void pLn(String content) throws RFWException {
    write(content + lineBreak);
  }

  /**
   * Imprime uma quebra de linha. Se a linha anterior n�o tiver sido quebrada, quebra a linha atual. Caso contr�rio cria uma linha em branco.
   *
   * @throws RFWException
   */
  public static void pBR() throws RFWException {
    write(lineBreak);
  }

  /**
   * Imprime um n�mero X de quebras de linhas.<br>
   * Se a linha anterior n�o tiver sido quebrada, criar X linhas em branco, caso contr�rio quebra a linha tual e cria (X-1) linhas em branco.
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
   * @param content Conte�do a ser escrito
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
   * # {@link OutputStream} utilizado para imprimir o conte�do. <br>
   * Utiliza por padr�o o valor obtido em {@link System#out}, mas pode ser substitu�do pelo usu�rio.
   *
   * @return the {@link OutputStream} utilizado para imprimir o conte�do
   */
  public static OutputStream getOutput() {
    return output;
  }

  /**
   * # {@link OutputStream} utilizado para imprimir o conte�do. <br>
   * Utiliza por padr�o o valor obtido em {@link System#out}, mas pode ser substitu�do pelo usu�rio.
   *
   * @param output the new {@link OutputStream} utilizado para imprimir o conte�do
   */
  public static void setOutput(OutputStream output) {
    BUConsole.output = output;
  }

  /**
   * # caracter(es) utilizado para quebra de linha.<br>
   * Padr�o: \\r\\n.
   *
   * @return the caracter(es) utilizado para quebra de linha
   */
  public static String getLineBreak() {
    return lineBreak;
  }

  /**
   * # caracter(es) utilizado para quebra de linha.<br>
   * Padr�o: \\r\\n.
   *
   * @param lineBreak the new caracter(es) utilizado para quebra de linha
   */
  public static void setLineBreak(String lineBreak) {
    BUConsole.lineBreak = lineBreak;
  }

  /**
   * Este m�todo tem como finalidade l�r os argumentos passados pelo usu�rio na linha de comando (inicializa��o do programa), encontrar um argumento espec�fico e retornar os N argumentos seguintes.
   *
   * @param args Array com os argumentos recebudos na inicializa��o do programa (par�metro do m�todo main).
   * @param arg O argumento que deve ser produrado. O m�todo procura sem case sensitive.
   * @param n N�mero de argumentos que devem ser recuperados DEPOIS do argumento encontrado.
   * @return Retorna nulo caso o array n�o tenha sido encontrado. Retorna um array com tamanho N caso existam N argumentos depois do argumento procurado. Caso encontre o a correspond�ncia desejada, mas n�o nenhum argumento seja encontrado na frente retorna um array com tamanho 0. Retorna um array menor que N caso n�o encontre N argumentos � frente da correspond�ncia.
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
   * Imprime um conte�do com Prefoxo de alerta.
   *
   * @param content Conte�do � ser impresso.
   * @throws RFWException
   */
  public static void pAlertLn(String content) throws RFWException {
    write("[!!!] " + content + lineBreak);
  }

  /**
   * L� um conte�do do Console at� que o usu�rio pressione enter
   *
   * @param prompt Linha digitada anteriormente para solicitar a entrada do usu�rio.
   * @return String com o conte�do digitado pelo usu�rio.
   * @throws RFWException
   */
  public static String askString(String prompt) throws RFWException {
    write(prompt + " ");
    if (System.console() == null) {
      // Se console � nulo, provavelmente estamos no eclipse e l�mos o conte�do pelo Scanner
      if (sc == null) sc = new Scanner(System.in);
      return sc.nextLine();
    } else {
      return System.console().readLine();
    }
  }

  /**
   * L� um conte�do do Console mascarado como Senha
   *
   * @param prompt Linha digitada anteriormente para solicitar a entrada do usu�rio.
   * @return String com o conte�do digitado pelo usu�rio.
   * @throws RFWException
   */
  public static String askPassword(String prompt) throws RFWException {
    write(prompt + " ");
    if (System.console() == null) {
      // Se console � nulo, provavelmente estamos no eclipse e l�mos o conte�do pelo Scanner
      if (sc == null) sc = new Scanner(System.in);
      return sc.nextLine();
    } else {
      return new String(System.console().readPassword());
    }
  }

  /**
   * Limpa o console. (N�o funciona dentro do Eclipse)
   *
   * @throws RFWException
   */
  public static void clear() throws RFWException {
    System.out.print("\033[H\033[2J" + lineBreak);
    System.out.flush();
  }

  /**
   * Este m�todo imprime um modelo de menu com um t�tulo e colunas de op��es conforme o tamanho so array.
   *
   * @param title T�tulo do menu. Se passar nulo tem o mesmo efeito que o m�todo {@link #pColumns(String[][])}.
   * @param menuOptions Op��es do menu
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
   * Imprime um cabe�alho de sess�o, utilizado por exemplo pelo m�todo {@link #pMenu(String, String[][])}
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
   * Escreve um conte�do em colunas conforme a dimens�o do array.
   *
   * @param content Array com o conte�do para ser impresso em colunas.
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
   * Similar ao comando pause do DOS, faz uma pausa no sistema at� que o usu�rio pressione ENTER para continuar.
   *
   * @throws RFWException
   */
  public static void pause() throws RFWException {
    askPassword("Pressione ENTER para continuar...");
  }

  /**
   * Imprime uma linha de separa��o repetindo a cadeia de caracteres recebida at� chegar no tamanho da coluna. Caso a quantidades de caracteres n�o seja divis�vel pela largura da coluna, o conte�do final � truncado.
   *
   * @param sepChars Caracteres que ser�o utilizados para criar a linha de separa��o.
   * @throws RFWException
   */
  public static void pSep(String sepChars) throws RFWException {
    write(BUString.truncate(BUString.completeOrTruncateUntilLengthRight(sepChars, "", consoleWidth), consoleWidth) + lineBreak);
  }
}
