package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWValidationException;

/**
 * Description: DataFormatter para números de telefones.<br>
 * Atualmente formata para números no padrão nacional, no futuro podemos melhorar a implementação desta classe para receber um Location e formatar números de diferentes países.<br>
 * <br Referência: http://pt.wikipedia.org/wiki/N%C3%BAmeros_de_telefone_no_Brasil
 *
 * @deprecated Utilize a nova classe {@link RFWPhoneDataFormatter}
 * @author Rodrigo Leitão
 * @since 7.1.0 (12/05/2015)
 */
@Deprecated
// TODO rodrigogml me excluir quando não estiver mais em uso!
public class RFWPhoneDataFormatterOLD implements RFWDataFormatter<String, String> {

  /**
   * Indica se devemos usar o DDI no telefone formatado
   */
  boolean useDDI = false;

  /**
   * Indica se devemos usar o DDD no telefone formatado.
   */
  boolean useDDD = true;

  /**
   * Cria um DataFormatter para formatar números de telefones.<br>
   * A informação de número de telefone deve ter o seguinte formado <DDI>|<DDD>|<Telefone>, sendo que os 2 pipes são sempre obrigatórios, mesmo que não haja informação do DDI ou DDD. Não deve haver nenhuma pontuação de formatação, apenas números.
   *
   */
  public RFWPhoneDataFormatterOLD(boolean useDDI, boolean useDDD) {
    this.useDDI = useDDI;
    this.useDDD = useDDD;
  }

  /**
   * Como os telefones são exibidos em campos especiais para tratar o telefone, este método apenas devolve o conteúdo como recebido no VO, toda a "formatação" é tratada pelo próprio campo. Para exibir o telfone de forma formata em uma String verifique o método {@link #format(String)}.
   */
  @Override
  public String toPresentation(String value, Locale locale) {
    return value;
  }

  /**
   * Formata o valor como recebido do banco. Ex: "55|19|32363309"
   *
   * @param value
   * @return
   */
  public String format(String value) {
    String fValue = "";
    if (value != null) {
      // Para formatar para a apresentação consideramos que o valor é sempre válido pois deve ter vindo do VO.
      String[] p = value.split("[\\|]");

      if (this.useDDI) fValue = formatDDI(p[0]) + ' ';
      if (this.useDDD) fValue += '(' + formatDDD(p[0], p[1]) + ") ";
      fValue += formatNumber(p[0], p[1], p[2]);
    }
    return fValue;
  }

  /**
   * Aplica pontuação / formato em uma sequência de número que possa ser um telefone.
   *
   * @param ddi Código do País para que possaos distinguir os tipos de numeração de telefone usado em casa país.
   * @param ddd Código de área da região. Em conjunto com o código de país, é utilizado para dar o formato adequado a uma região.
   * @param number Número do telefone que seré formatado.
   * @return Número do telefone formatado. Note que as informaçães de DDI e DDD são utilizadas apenas para identificar a região e como o número deve aparecer, mas esses valores não serão retornados junto. Para formatar esses dados utilize em conjunto as funções {@link #formatDDI(String)} e {@link #formatDDD(String, String)}.
   */
  public static String formatNumber(String ddi, String ddd, String number) {
    if (ddi == null) ddi = "";
    ddi = ddi.replaceAll("[^0-9]*", "");
    if (ddd == null) ddd = "";
    ddd = ddd.replaceAll("[^0-9]*", "");
    if (number == null) number = "";
    number = number.replaceAll("[^0-9]*", "");

    String fValue = number;
    switch (ddi) {
      case "55": // BRASIL
        if (number.length() == 3) {
          // Com 3 dígitos são telefones de serviço retornamos somente o número, independente da configuração de DDI e DDD
          fValue = number;
        } else if (number.length() == 8) {
          // Com 8 números é um telefone "local", fixo ou móvel, apenas formatamos no padrão XXXX-XXXX
          fValue = number.substring(0, 4) + "-" + number.substring(4, 8);
        } else if (number.length() == 9) {
          // Com 9 números é o número dos celulares das regiões metropolitadas que tem o tal 9° dígito
          fValue = number.substring(0, 5) + "-" + number.substring(5, 9);
        } else if (number.length() >= 4 && number.substring(0, 4).matches("0[3589]00")) {
          // Com 11 dígitos também temos os números de 0800, 0300, 0500 e 0900 neste caso formatamos diferente
          fValue = number.substring(0, 4) + " " + number.substring(4, 7) + " " + number.substring(7, 11);
        }
        break;
    }
    return fValue;
  }

  /**
   * Formata um código de País para ser anexado em um telefone para ser exibido ao cliente.
   *
   * @param ddi Código do país a ser formatado.
   * @return Código do país formato.
   */
  public static String formatDDI(String ddi) {
    return '+' + ddi;
  }

  /**
   * Formata o código de área de um determinado país, para ser usado em conjunto com o restante do número para exibição ao usuário.
   *
   * @param ddi código do país. Utilizado apenas para identificar a área e formatar corretamente o DDD.
   * @param ddd Código de área a ser formatado.
   * @return Código de área formatado.
   */
  public static String formatDDD(String ddi, String ddd) {
    return ddd;
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

  /**
   * Faz a validação do número do telefone.
   *
   * @param ddi Indicador de país para validação do número.
   * @param ddd Indicador de área do telefone. Utilizado para validar o número.
   * @param number Número do telefone que será validado para o código de área e do país.
   * @throws RFWException Lançado caso não seja um número válido para o País e área passados.
   */
  public static void validateNumber(String ddi, String ddd, String number) throws RFWException {
    if (ddi == null) ddi = "";
    ddi = ddi.replaceAll("[^0-9]*", "");
    if (ddd == null) ddd = "";
    ddd = ddd.replaceAll("[^0-9]*", "");
    if (number == null) number = "";
    number = number.replaceAll("[^0-9]*", "");

    switch (ddi) {
      case "55": // BRASIL
        // O Brasil não tem distinção de número de acordo com a área.
        if ("".equals(ddd) && "".equals(number)) {
          // Se ambos estão sem conteúdo, consideramos que o campo não está preenchido nada a validar
        } else if ("".equals(ddd) && number.length() == 3 && number.charAt(0) == '1') {
          // Considerado número de serviço: tipo 190, 102, etc. Aceita
        } else if (number.length() < 8) {
          // Tirando os números de serviços, não temos números menores que 8 dígitos no Brasil.
          throw new RFWValidationException("RFW_ERR_300063", new String[] { ddi, ddd, number });
        } else if (number.length() == 8 && (number.charAt(0) == '2' || number.charAt(0) == '3' || number.charAt(0) == '4' || number.charAt(0) == '5' || number.charAt(0) == '7' || number.charAt(0) == '8' || number.charAt(0) == '9')) {
          // Telefone com 8 dígitos podem ser: de telefonia fixa e telefonia fixa rural (2, 3, 4 e 5) Exceto os teleofes que começam com 400; telefones de serviço com cobrança: começam com 400; telefones de celulares em cidades com menor população (8, 9); e Trunking ou radio (7);
        } else if (number.length() == 9 && (number.charAt(0) == '9')) {
          // Telefones com 9 dígitos são celulares de algumas cidades metropolitanas do país que ganharam o 9° dígito no celular. Por isso só aceita telefone com 9 digitos se o primeiro for um 9
        } else if (number.length() == 11 && (number.charAt(0) == '0' && number.charAt(2) == '0')) {
          // Existem números com 11 dígitos que são os de 0800 e 0900. Como o Número de Celular (9 digitos) + DDD (2 digitos) também dá 11.
          // Verificamos já no if acima se for 11 e for no padrão 0?0... "aceitamos o número" deixando entrar nesse IF e não executando os próximos 'Elses'
        } else if (number.length() >= 9) {
          throw new RFWValidationException("RFW_ERR_300063");
        }
        break;
      default:
        throw new RFWValidationException("RFW_ERR_300062", new String[] { ddi, ddd });
    }
  }

  /**
   * Validador para o telefone no formato do VO: "DDI|DDD|Telefone"
   *
   * @param value
   * @throws RFWEXception
   */
  public static void validateVOData(String value) throws RFWException {
    if (value != null && !"".equals(value) && !"||".equals(value)) {
      value = value.replaceAll("[^\\|0-9]", "");
      if (!value.matches("([1-9][0-9]{1,2})?\\|([1-9][0-9])?\\|[0-9]*")) {
        throw new RFWValidationException("RFW_ERR_200354");
      }
      // Incluimos um último pipe com um conteúdo (o '.') para que o split crie obrigatoriamente todos os elementos do array. pois se recebermos algo como "55||" o resultado é um Array sí com a posição 0, os campos vazios no final não são criados no array.
      String parts[] = (value + "|.").split("\\|");

      if ("".equals(parts[1])) {
        // Se não tiver DDD, o telefone deve ser de serviço (3) ou um 0800 (11)
        if (!"".equals(parts[2]) && parts[2].length() != 3 && parts[2].length() != 11) {
          throw new RFWValidationException("RFW_ERR_200355");
        }
      } else {
        validateDDD(parts[0], parts[1]);
      }
      validateNumber(parts[0], parts[1], parts[2]); // Valida a parte do telefone.
    }
  }

  @Override
  public String toVO(String formattedvalue, Locale locale) throws RFWException {
    // Como esperamos que que na UI o campo gere o telefone já no formato que utilizamos no VO, não há nada para converter, só devolter o dado já pronto (que já deve ter sido validado pelo método validate()
    // Só verificamos se recebemos tanto o DDD quando o Número vazios, neste caso retornamos nulo para otimizar o banco de dados.
    formattedvalue = formattedvalue.replaceAll("[^\\|0-9]", "");
    String parts[] = (formattedvalue + "|.").split("\\|");
    if ("".equals(parts[1]) && "".equals(parts[2])) {
      formattedvalue = null;
    }
    return formattedvalue;
  }

  /**
   * Este método valida o dado entrado pelo usuário na UI, como deve ser utilizado o RFWPhoneField já recebemos o valor correto do VO.
   */
  @Override
  public void validate(Object value, Locale locale) throws RFWException {
    // Como esperamos que que na UI o campo gere o telefone já no formato que utilizamos no VO, vamos validar como sendo o próprio dado do VO
    validateVOData((String) value);
  }

  @Override
  public int getMaxlenght() {
    return 20;
  }
}