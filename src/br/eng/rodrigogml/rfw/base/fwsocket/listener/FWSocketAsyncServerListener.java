package br.eng.rodrigogml.rfw.base.fwsocket.listener;

import br.eng.rodrigogml.rfw.base.fwsocket.FWSocketObjectMap;

/**
 * Description: Listener de eventos do {@link FWSocketAsyncServerListener}.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.0.0 (13/11/2014)
 */
public interface FWSocketAsyncServerListener {

  /**
   * Chamado sempre que o Socket receber um novo Objeto.<br>
   *
   * @param clientid Identificador do Client de quem recebemos o comando. Ser� NULL quando a classe � usada como cliente.
   * @param tcproperties Objeto recebido atrav�s da comunica��o.
   */
  public void received(Long clientid, FWSocketObjectMap tcproperties);

}
