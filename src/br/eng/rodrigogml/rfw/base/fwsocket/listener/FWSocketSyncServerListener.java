package br.eng.rodrigogml.rfw.base.fwsocket.listener;

import br.eng.rodrigogml.rfw.base.fwsocket.FWSocketObjectMap;

/**
 * Description: Listener de eventos do FWSocketSyncServer.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 7.0.0 (13/11/2014)
 */
public interface FWSocketSyncServerListener {

  /**
   * Chamado sempre que o Socket receber um novo Objeto.<br>
   *
   * @param tcProperties Objeto recebido atrav�s da comunica��o.
   * @return Objeto a ser retornado "resposta" para o client. Mesmo que seja uma comunica��o Ass�ncrona, o retorno se != de null ser� passado automaticamente para o m�todo de envio.
   */
  public FWSocketObjectMap received(FWSocketObjectMap tcProperties) throws Exception;

}
