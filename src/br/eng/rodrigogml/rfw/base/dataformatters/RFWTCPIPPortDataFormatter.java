package br.eng.rodrigogml.rfw.base.dataformatters;

/**
 * Description: Classe para formatar e validar uma porta de conexão TCP/IP.<BR>
 * Basicamente permite os valores entre 1 e 65535. (Mais info em: http://pt.wikipedia.org/wiki/Anexo:Lista_de_portas_de_protocolos)
 *
 * @author Rodrigo Leitão
 * @since 7.0.0 (20/09/2014)
 */
public class RFWTCPIPPortDataFormatter extends RFWIntegerDataFormatter {

  private static RFWTCPIPPortDataFormatter instance = null;

  private RFWTCPIPPortDataFormatter() {
    super(1, 65535, true);
  }

  public static RFWTCPIPPortDataFormatter getInstance() {
    if (RFWTCPIPPortDataFormatter.instance == null) {
      RFWTCPIPPortDataFormatter.instance = new RFWTCPIPPortDataFormatter();
    }
    return RFWTCPIPPortDataFormatter.instance;
  }

}
