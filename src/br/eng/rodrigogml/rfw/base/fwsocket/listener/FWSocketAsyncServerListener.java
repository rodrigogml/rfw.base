package br.eng.rodrigogml.rfw.base.fwsocket.listener;

import br.eng.rodrigogml.rfw.base.fwsocket.FWSocketObjectMap;

/**
 * Description: Listener de eventos do {@link FWSocketAsyncServerListener}.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.0.0 (13/11/2014)
 */
public interface FWSocketAsyncServerListener {

  /**
   * Chamado sempre que o Socket receber um novo Objeto.<br>
   *
   * @param clientid Identificador do Client de quem recebemos o comando. Será NULL quando a classe é usada como cliente.
   * @param tcproperties Objeto recebido através da comunicação.
   */
  public void received(Long clientid, FWSocketObjectMap tcproperties);

}
