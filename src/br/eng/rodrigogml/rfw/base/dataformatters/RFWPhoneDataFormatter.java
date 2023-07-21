package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.dataformatters.RFWDataFormatter;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: DataFormatter para números de telefones.<br>
 * Atualmente formata para números no padrão nacional, no futuro podemos melhorar a implementação desta classe para receber um Location e formatar números de diferentes países.<br>
 * <br Referência: http://pt.wikipedia.org/wiki/N%C3%BAmeros_de_telefone_no_Brasil
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (12/05/2015)
 */
public class RFWPhoneDataFormatter implements RFWDataFormatter<String, String> {

  /**
   * Enumeração com os tipos de números de telefones reconhecidos por esta classe
   */
  public static enum PhoneType {
    /**
     * Número de telefone desconhecido pelo sistema.
     */
    UNKNOW,
    /**
     * Números de utilidades publicas brasileiras, como 190 (Polícia), 199 (Defesa Civíl)
     */
    BRAZIL_PUBLICUTILS,
    /**
     * Telefones residenciais brasileiros
     */
    BRAZIL_PHONE,
    /**
     * Telefones celulares brasileiros.
     */
    BRAZIL_MOBILE,
    /**
     * Telefones de serviços não geográficos, como 0800 (ligação grátis nacional), 0500 (Doações e utilidade pública)
     */
    BRAZIL_SERVICES
  }

  public RFWPhoneDataFormatter() {
  }

  /**
   * Este método tenta reconhecer o número realizando validações. Se o número não for reconhecido ou não for válido, retorna nulo.<br>
   * Note que este método no momento não tem suporte à números internacionais, mas se algum dia vier a ter, números pequenos e sem prefixos internacionais devem ser considerados prioritariamente como telefones nacionais.
   *
   * @param number número a ser identificado, validado e formatado
   * @return Retorna o tipo de telefone identificado.
   */
  public static PhoneType getPhoneType(String number) {
    if (number != null) {
      number = number.replaceAll("[^\\d]", ""); // Remove o que não for dígito

      switch (number.length()) { // A formatação é baseada no tamanho da enumeração
        case 3:
          switch (number) {
            case "100": // Secretaria dos Direitos Humanos
            case "128": // Serviços de Emergência no Mercosul
            case "151": // Fundação Procon
            case "152": // Ibama
            case "153": // Guarda Municipal
            case "156": // Serviço Público Municipal
            case "180": // Central de Atendimento à Mulher
            case "181": // Disque Denúncia
            case "185": // Salvamar
            case "188": // Centro de Valorização da Vida
            case "190": // Polícia Militar
            case "191": // Polícia Rodoviária Federal
            case "192": // Serviço de Atendimento Móvel de Urgência
            case "193": // Corpo de Bombeiros
            case "194": // Polícia Federal
            case "197": // Polícia Civil
            case "198": // Polícia Rodoviária Estadual
            case "199": // Defesa Civil
            case "102": // Auxílio à Lista Disponível apenas em Telefonia Fixa
            case "130": // Hora Certa Tarifado, Disponível apenas em Telefonia Fixa
            case "134": // Despertador Tarifado, Disponível apenas em Telefonia Fixa
              return PhoneType.BRAZIL_PUBLICUTILS;
            default:
              return PhoneType.UNKNOW;
          }
          // Ex: 190 - Telefones de serviços públicos
        case 4:
          if (number.startsWith("105")) {// 105X - Central de Relacionamento SMP X = Identificação da Operadora
            return PhoneType.BRAZIL_PUBLICUTILS;
          } else {
            return PhoneType.UNKNOW;
          }
        case 5:
          if (number.startsWith("103")) {// 103XX - Central de Relacionamento STFC XX = Código da Operadora
            return PhoneType.BRAZIL_PUBLICUTILS;
          } else if (number.startsWith("106")) {// 106XX - Central de Relacionamento DTH/TDT XX = Código da Prestadora
            return PhoneType.BRAZIL_PUBLICUTILS;
          } else {
            return PhoneType.UNKNOW;
          }
        case 8:
          // Ex: 3236-3309 - Telefone Fixo
          // Ex: 9999-5555 - Telefones celulares ainda sem o nono dígito (caindo em desuso por isso não considerado mais por este método)
          return PhoneType.BRAZIL_PHONE;
        case 9:
          // Ex: 98189-5510 - Telefone celular com o nono dígito
          return PhoneType.BRAZIL_MOBILE;
        case 10:
          // Ex: 19 3236-3309 - Telefones Fixos (ou celular antidos) com DDD
          return PhoneType.BRAZIL_PHONE;
        case 11:
          // Ex: 0800 543 1010 - Telefones de atendimento
          // Ex: 19 98189-5519 - Telefones Celular com DDD
          if (number.substring(0, 4).matches("0[3589]00")) { // Se começa com 0800, 0300, 0500 e 0900
            return PhoneType.BRAZIL_SERVICES;
          } else {
            return PhoneType.BRAZIL_MOBILE;
          }
        case 12:
          // Ex: +55 19 3236-3309 - Telefone fixo brasileiro com DDI e DDD
          return PhoneType.BRAZIL_PHONE;
        case 13:
          // Ex: +55 19 981895519 - Telefone celular com no no dígito com DDI e DDD
          return PhoneType.BRAZIL_MOBILE;
      }
    }
    return PhoneType.UNKNOW;
  }

  /**
   * Como os telefones são exibidos em campos especiais para tratar o telefone, este método apenas devolve o conteúdo como recebido no VO, toda a "formatação" é tratada pelo próprio campo. Para exibir o telfone de forma formata em uma String verifique o método {@link #format(String)}.
   */
  @Override
  public String toPresentation(String value, Locale locale) {
    return format(value);
  }

  /**
   * Este método formata o número recebido. Para definir uma formatação adequada ele primeiro tenta reconhecero tipo do número. Se nao reconehcer, aplica uma formatação padrão baseado no tamanho.
   *
   * @param number número a ser identificado, validado e formatado
   * @return Retorna o valor formatado
   */
  public String format(String number) {
    if (number == null) number = "";
    number = number.replaceAll("[^\\d]", ""); // Remove o que não for número
    switch (getPhoneType(number)) {
      case BRAZIL_MOBILE:
        switch (number.length()) {
          case 8:
            // Ex: 9999-5555 - Telefones celulares ainda sem o nono dígito
            return new StringBuilder().append(number.substring(0, 4)).append("-").append(number.substring(4, 8)).toString();
          case 9:
            // Ex: 98189-5510 - Telefone celular com o nono dígito
            return new StringBuilder().append(number.substring(0, 5)).append("-").append(number.substring(5, 9)).toString();
          case 10:
            // Ex: 19 81895519 - Telefone celular (sem o nono dígito) com com DDD
            return new StringBuilder().append(number.substring(0, 2)).append(" ").append(number.substring(2, 6)).append(" ").append(number.substring(6, 10)).toString();
          case 11:
            // Ex: 19 981895519 - Telefone celular com o nono dígito com DDD
            return new StringBuilder().append(number.substring(0, 2)).append(" ").append(number.substring(2, 7)).append(" ").append(number.substring(7, 11)).toString();
          case 13:
            // Ex: +55 19 981895519 - Telefone celular com no no dígito com DDI e DDD
            return new StringBuilder().append("+").append(number.substring(0, 2)).append(" ").append(number.substring(2, 4)).append(" ").append(number.substring(4, 8)).append("-").append(number.substring(8, 12)).toString();
        }
        break;
      case BRAZIL_PHONE:
        switch (number.length()) {
          case 8:
            // Ex: 3236-3309 - Telefone Fixo
            return new StringBuilder().append(number.substring(0, 4)).append("-").append(number.substring(4, 8)).toString();
          case 10:
            // Ex: 19 3236-3309 - Telefones Fixos (ou celular antidos) com DDD
            return new StringBuilder().append(number.substring(0, 2)).append(" ").append(number.substring(2, 6)).append("-").append(number.substring(6, 10)).toString();
          case 12:
            // Ex: +55 19 3236-3309 - Telefone fixo brasileiro com DDI e DDD
            return new StringBuilder().append("+").append(number.substring(0, 2)).append(" ").append(number.substring(2, 4)).append(" ").append(number.substring(4, 8)).append("-").append(number.substring(8, 12)).toString();
        }
        break;
      case BRAZIL_PUBLICUTILS:
        return new StringBuilder().append(number).toString();
      case BRAZIL_SERVICES:
        switch (number.length()) {
          case 11:
            return new StringBuilder().append(number.substring(0, 4)).append(" ").append(number.substring(4, 7)).append(" ").append(number.substring(7, 11)).toString();
        }
        break;
      case UNKNOW:
        // Se o número não é reconhecido, não faz nenhuma formatação
        break;
    }
    return number;
  }

  /**
   * Valida um DDD.
   *
   * @param ddi DDI para detectar qual é o pais a qual pertence o DDD para que seja possível validar se é válido.
   * @param ddd DDD código de área a ser validado.
   * @throws RFWException Lançado em caso de erro ou se o valor for inválido.
   */
  public static void validateDDD(String ddi, String ddd) throws RFWException {
    switch (ddi) {
      case "55": // BRASIL
        switch (ddd) {
          case "11":
          case "12":
          case "13":
          case "14":
          case "15":
          case "16":
          case "17":
          case "18":
          case "19":
          case "21":
          case "22":
          case "24":
          case "27":
          case "28":
          case "31":
          case "32":
          case "33":
          case "34":
          case "35":
          case "37":
          case "38":
          case "41":
          case "42":
          case "43":
          case "44":
          case "45":
          case "46":
          case "47":
          case "48":
          case "49":
          case "51":
          case "53":
          case "54":
          case "55":
          case "61":
          case "62":
          case "63":
          case "64":
          case "65":
          case "66":
          case "67":
          case "68":
          case "69":
          case "71":
          case "73":
          case "74":
          case "75":
          case "77":
          case "79":
          case "81":
          case "82":
          case "83":
          case "84":
          case "85":
          case "86":
          case "87":
          case "88":
          case "89":
          case "91":
          case "92":
          case "93":
          case "94":
          case "95":
          case "96":
          case "97":
          case "98":
          case "99":
            // DDDs válidos para o Brasi
            break;
          default:
            throw new RFWValidationException("DDD inválido!", new String[] { ddi, ddd });
        }
        break;
      default:
        throw new RFWValidationException("DDI desconhecido pelo RFWDeprec!", new String[] { ddi, ddd });
    }
  }

  @Override
  public String toVO(String value, Locale locale) throws RFWException {
    validate(value, locale);
    return value.replaceAll("[^\\d]", "");
  }

  /**
   * Este método valida o dado entrado pelo usuário na UI, como deve ser utilizado o RFWPhoneField já recebemos o valor correto do VO.
   */
  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    if (value != null && !"".equals(value)) {
      if (!(value instanceof String)) throw new RFWCriticalException("O Objeto recebido deveria ser uma String!");
      if (getPhoneType((String) value) == PhoneType.UNKNOW) throw new RFWValidationException("Número de telefone inválido!");
    }
  }

  @Override
  public int getMaxlenght() {
    return 20;
  }
}