package br.eng.rodrigogml.rfw.base.dataformatters;

import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Esta classe é um padrão de definição de um DataFormatter usado pelo RFWDeprec.<br>
 * Um DataFormatter é uma classe que opera normalmente entre o bean (VOs) e os componentes de exibição para o usuário (como GUI, relatórios, etc.), fazendo o parser e a formatação dos dados para que sejam armazenados no Bean corretamente, enquanto que são exibidos na tela de maneira amigável para o usuário.<br>
 * além disso, esta classe carrega outras definições uteis, como validador para campos, tamanhos máximos digitáveis, etc. <br>
 * <b>Sempre que possível (quando não houver configurações, como casas decimais, negativos, etc.) implemente o DataFormatter como singleton para evitar múltiplas instancias denecessárias na memória.</b>
 *
 * @author Rodrigo Leitão
 * @since 4.1.0 (23/06/2011)
 */
public interface RFWDataFormatter<PRESENTATIONTYPE, VOTYPE> {

  /**
   * Método chamado sempre que precisar converter o dado do jeito como é gerenciado no sistema para ser apresentado para o usuário.<br>
   * <b>ATENÇÃO: </b>Para a apresentação, nuncca se retorna null! Para valores null, o valor formatado deve sempre ser "". Usualamente retornar null para exibição o usuário fará com que a palavra 'null' seja concatenada e exibida para o usuário final.
   *
   * @param value valor a ser formatado para a tela.
   * @param locale Localidade para ser usada na formatação caso necessario.
   * @return Valor em formato amigável para o usuário.
   */
  public abstract PRESENTATIONTYPE toPresentation(VOTYPE value, Locale locale) throws RFWException;

  /**
   * Converte uma informação dada pelo usuário (ou mesmo dada pelo Método toPresentation) para o objeto como é gerenciado no VO.
   *
   * @param formattedvalue valor formatado (ou não) como veio da tela, digitado pelo usuário. Pode precisar de validação, ou mesmo ser inválido.
   * @param locale Localidade para ser usada na formatação caso necessario.
   * @return
   * @throws RFWException Exceção a ser lançada quando não for possível fazer o parser do valor. Em caso de exceção o valor será anulado no bean.
   */
  public abstract VOTYPE toVO(PRESENTATIONTYPE formattedvalue, Locale locale) throws RFWException;

  /**
   * O Método de validação deve validar se o conteúdo pode ser recebido no Método toVO() e ser salvo com sucesso, ou seja, ser convertido do valor do usuário para o valor como deve ser salvo no VO.<br>
   * Esta validação nada tem a ver com a validação do VO, apenas se o valor recebido pode ser convertido e salvo no VO, se é um valor válido o valor formatado recebido.
   */
  public abstract void validate(Object value, Locale locale) throws RFWException;

  /**
   * Define o tamanho máximo formatado para o dado.<br>
   * Normalmente a informação formatada tende a ser maior do que o dado salvo no VO, essa informação tende a informar qual o tamanho máximo que a informação formatada pode ter. Seja para limitar o que o usuário poderá digitar, ou o tamanho que o dado formatado ocupará em um relatório ou outro tipo de midia.
   *
   * @return inteiro que determina a quantidade de dígitos no campo.
   */
  public abstract int getMaxlenght();

  /**
   * Este Método tem a função de "reformatar" o conteúdo do campo digitado pelo usuário e retornar o valor formatado para a própria apresentação novamente.<br>
   * A implementação padrão deste Método joga o conteúdo recebido da apresentação apra o Método {@link #toVO(Object, Locale)} e depois joga novamente para o Método {@link #toPresentation(Object, Locale)}. Pois realizando as duas conversões teremos o conteúdo formatado com sucesso para a apresentação. Mas pode ser substituido por uma implementaçõo mais eficiente dependendo da implementação.
   *
   * @param content conteúdo que o usuário entrou.
   * @param locale Locale do usuário para interpretação correta dos formatos utilizados pelo mesmo.
   * @return Valor já formatado, utilizando o próprio locale do usuário. Conforme implementação da Interface.
   * @throws RFWException Lançado em caso de erro de conversão ou o conteúdo entrado pelo usuário não seja válido. Na implementação padrão, retorna as exceptions que possam ocorrer nos Métodos {@link #toVO(Object, Locale)} ou {@link #toPresentation(Object, Locale)}
   */
  public default PRESENTATIONTYPE reformatPresentationContent(PRESENTATIONTYPE content, Locale locale) throws RFWException {
    // Convertemos para o VO, já que o Método de Presentation só é preparado apra receber um dado válidado conforme esperado do VO.
    this.validate(content, locale);
    final VOTYPE voValue = this.toVO(content, locale);
    // Reconvertemos para a PRESENTATION
    return this.toPresentation(voValue, locale);
  }
}
