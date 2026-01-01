package br.eng.rodrigogml.rfw.base.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe utilitária do RFWDeprec para verificar o tipo do arquivo.<br>
 * Utiliza o {@link Files#probeContentType(Path)} para identificar o tipo do arquivo, mas dependendo do retornado tenta realizar algumas validações no conteúdo para identificar de que se trata o arquivo.<br>
 * <br>
 * <B>ATENÇÃO: NOTE QUE OS ARQUIVOS SÃO IDENTIFICADOS E CATEGORIZADOS POR ALGUNS ATRIBUTOS E/OU ESTRUTURA ESPECÍFICA, E NÃO SÃO VALIDADOS A FUNDO. O OBJETIVO PRINCIPAL É APENAS IDENTIFICAR E EXTRAIR ALGUMAS PARAMETROS NECESSÁRIOS PARA GUIAR AS OPERAÇÕES DO RFWDeprec.</B>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (26 de jun de 2019)
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
 */
@Deprecated
public class BUFileSniff {

  public static enum FileSniffType {
    /**
     * Tipo do arquivo é indefinido por esta classe ou não identificável.
     */
    UNKNOW,
    /**
     * Arquivo do tipo texto, que não pode ser melhor identificado pelo RFWDeprec.
     */
    TEXT_PLAIN,
    /**
     * Arquivo do tipo texto, com estrutura de arquivo utilizado para Lote de Pagamentos entre bancos.
     */
    TEXT_PLAIN_FEBRABAN_PAG
  }

  /**
   * Abre o arquivo e faz as verificações conhecidas para identificar o melhor possível o arquivo.
   *
   * @throws RFWException
   */
  public static FileSniffType sniff(String filePath) throws RFWException {
    return sniff(new File(filePath).getPath());
  }

  /**
   * Abre o arquivo e faz as verificações conhecidas para identificar o melhor possível o arquivo.
   *
   * @throws RFWException
   */
  public static FileSniffType sniff(Path filePath) throws RFWException {
    FileSniffType type = null;
    try {

      String fileType = Files.probeContentType(filePath);
      switch (fileType) {
        case "text/plain":
          type = FileSniffType.TEXT_PLAIN;
          if (sniffTextPlainFebrabanPag(filePath)) type = FileSniffType.TEXT_PLAIN_FEBRABAN_PAG;
        default:
          type = FileSniffType.UNKNOW;
      }
    } catch (IOException e) {
      throw new RFWCriticalException("Falha ao lêr o arquivo!", e);
    }
    return type;
  }

  /**
   * Verifica se é um arquivo de texto da Febraban de pagamentos
   *
   * @return Objeto contendo as propriedades do arquivo.
   * @throws RFWException
   */
  private static boolean sniffTextPlainFebrabanPag(Path filePath) throws RFWException {
    final Charset charset = BUFile.detectTextFileCharset(filePath.toString());

    // Vamos lêr o arquivo para que, de acordo com o conteúdo encontrado, identificamos o arquivo
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toString()), charset))) {

      boolean foundFileHeader = false;
      boolean inLote = false; // Indica se estamos dentro de um lote no momento
      boolean foundFileTrailer = false;

      String line = null;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty()) {
          // Se alguma linha for diferente de 240 (ignorando as vazias que podem existir no fim do arquivo), não consideramos mais um arquivo de febraban
          if (line.length() != 240) return false;

          // Verifica o tipo de registro da linha
          String tipoRegistro = line.substring(7, 8);
          if ("0".equals(tipoRegistro)) { // HEADER DE ARQUIVO
            if (foundFileTrailer) return false;
            if (foundFileHeader) return false;
            foundFileHeader = true;
          } else if ("1".equals(tipoRegistro)) { // HEADER DE LOTE
            if (foundFileTrailer) return false;
            if (!foundFileHeader) return false;
            if (inLote) return false;
            inLote = true;
          } else if ("3".equals(tipoRegistro)) { // REGISTRO DE DETALHE
            if (foundFileTrailer) return false;
            if (!inLote) return false;
          } else if ("5".equals(tipoRegistro)) { // TRAILER DE LOTE
            if (!inLote) return false;
            if (foundFileTrailer) return false;
            inLote = false;
          } else if ("9".equals(tipoRegistro)) { // TRAILER DE ARQUIVO
            if (!foundFileHeader) return false;
            if (foundFileTrailer) return false;
            foundFileTrailer = true;
          }
        }
      }

    } catch (IOException e) {
      throw new RFWCriticalException("Falha o lêr o arquivo", e);
    }

    return true;
  }
}
