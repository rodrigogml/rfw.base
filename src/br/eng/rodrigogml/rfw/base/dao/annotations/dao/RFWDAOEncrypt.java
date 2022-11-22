package br.eng.rodrigogml.rfw.base.dao.annotations.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotation usada para informar o RFWDAO que a informação deve ficar criptografada no banco de dados.<br>
 *
 * @author Rodrigo Leitão
 * @since 10.0.0 (12 de jul de 2018)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RFWDAOEncrypt {

  /**
   * Define uma chave String para encriptar o conteúdo no banco de dados. No VO o valor estará sempre descodificado, porém no banco evita que o conteúdo fique a mostra. Não é uma criptografia forte e é possível de ser quebrada, mas requer algum esforço.<br>
   * ATENÇÃO: CUIDADO AO ALTERAR ESSE VALOR QUANDO JÁ EXISTEM DADOS NO BANCO DE DADOS, ELES PRECISARÃO SER REDEFINIDOS OU SERÃO COMPLETAMENTE DESCONFIGURADOS.
   */
  String key() default "";
}
