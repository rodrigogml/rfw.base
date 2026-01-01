package br.eng.rodrigogml.rfw.base.fwsocket.listener;

import br.eng.rodrigogml.rfw.base.fwsocket.FWSocketObjectMap;

/**
 * Description: Listener de eventos do FWSocketSyncServer.<BR>
 *
 * @author Rodrigo Leitão
 * @since 7.0.0 (13/11/2014)
 */
public interface FWSocketSyncServerListener {

  /**
   * Chamado sempre que o Socket receber um novo Objeto.<br>
   *
   * @param tcProperties Objeto recebido através da comunicação.
   * @return Objeto a ser retornado "resposta" para o client. Mesmo que seja uma comunicação Assíncrona, o retorno se != de null será passado automaticamente para o método de envio.
   */
  public FWSocketObjectMap received(FWSocketObjectMap tcProperties) throws Exception;

}
