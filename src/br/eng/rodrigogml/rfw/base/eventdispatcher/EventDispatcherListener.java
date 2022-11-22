package br.eng.rodrigogml.rfw.base.eventdispatcher;

import java.util.HashMap;

/**
 * Description: Listener de eventos do EventDispatcher.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (27 de set de 2018)
 */
public interface EventDispatcherListener {

  /**
   * Chamado sempre que alguma parte do sistema disparar um evento.
   *
   * <b>ATENÇÃO:</B> O método não lança nenhuma exceção pois elas devem ser totalmente tratadas dentro dos listeners. Qualquer exception que vaze (como {@link RuntimeException} será logada como erro crítico e não atrapalhará o processo de notificação dos outros listeners nem desfará a ação atual.
   *
   * @param eventID Identificador do Evento. Normalmente uma STRING definida como constante na classe Framework com o prefixo "EVENT_".
   * @param params Hash com os parametros do evento. O primeiro argumento é uma chave String (como se fosse o nome da variável) e o conteúdo da Hash é qualquer objeto que o evento deseje passar. Para mais informações é necessário verificar a documentação do evento.
   */
  public void event(String eventID, HashMap<String, Object> params);

}
