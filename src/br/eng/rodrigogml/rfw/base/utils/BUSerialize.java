package br.eng.rodrigogml.rfw.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe com métodos para auxiliar na serialização e desserialização de objetos.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (30/06/2015)
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
 */
@Deprecated
public class BUSerialize {

  private BUSerialize() {
  }

  /**
   * Serializa um objeto qualquer para um arquivo.
   *
   * @param obj Objeto a ser serializado.
   * @param filepath Caminho + nome do arquivo + extensão à receber o arquivo serializado.
   * @throws RFWException Lançado caso falhe em serializar o objeto.
   */
  public static void serializeToFile(Object obj, String filepath) throws RFWException {
    try {
      final FileOutputStream out = new FileOutputStream(filepath);
      // final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final ObjectOutputStream oo = new ObjectOutputStream(out);
      oo.writeObject(obj);
      oo.flush();
      oo.close();
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200330", e);
    }
  }

  /**
   * Serializa um objeto qualquer para um OutputStream.
   *
   * @param obj Objeto a ser serializado.
   * @param stream OutputStream para escrever os bytes do conteúdo da serialização.
   * @throws RFWException Lançado caso falhe em serializar o objeto.
   */
  public static byte[] serializeToByteArray(Object obj) throws RFWException {
    try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
      serializeToOutputStream(obj, stream);
      return stream.toByteArray();
    } catch (IOException e) {
      throw new RFWCriticalException("Ocorreu um erro ao serializar o objeto para o ByteArrayOutputStream.", e);
    }
  }

  /**
   * Serializa um objeto qualquer para um OutputStream.
   *
   * @param obj Objeto a ser serializado.
   * @param stream OutputStream para escrever os bytes do conteúdo da serialização.
   * @throws RFWException Lançado caso falhe em serializar o objeto.
   */
  public static void serializeToOutputStream(Object obj, OutputStream stream) throws RFWException {
    try (ObjectOutputStream oo = new ObjectOutputStream(stream)) {
      oo.writeObject(obj);
      oo.flush();
    } catch (Exception e) {
      throw new RFWCriticalException("Ocorreu um erro ao serializar o objeto para o OutputStream.", e);
    }
  }

  /**
   * Desserializa o objeto do arquivo.
   *
   * @param filepath Caminho completo do arquivo que contém o objeto.
   * @return Objeto desserializado.
   * @throws RFWException Lançado em caso de falha ao realizar a operação.
   */
  public static Object desserializeFromFile(String filepath) throws RFWException {
    Object obj = null;
    final File file = new File(filepath);
    if (file.exists()) {
      try {
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
        obj = input.readObject();
        input.close();
      } catch (Exception e) {
        throw new RFWCriticalException("RFW_ERR_200331", e);
      }
    }
    return obj;
  }

  /**
   * Desserializa o objeto de um array de bytes.
   *
   * @param filepath Caminho completo do arquivo que contém o objeto.
   * @return Objeto desserializado.
   * @throws RFWException Lançado em caso de falha ao realizar a operação.
   */
  public static Object desserializeFromByteArray(byte[] data) throws RFWException {
    return desserializeFromStream(new ByteArrayInputStream(data));
  }

  /**
   * Desserializa o objeto do arquivo.
   *
   * @param filepath Caminho completo do arquivo que contém o objeto.
   * @return Objeto desserializado.
   * @throws RFWException Lançado em caso de falha ao realizar a operação.
   */
  public static Object desserializeFromStream(InputStream in) throws RFWException {
    Object obj = null;
    try {
      ObjectInputStream input = new ObjectInputStream(in);
      obj = input.readObject();
      input.close();
    } catch (Exception e) {
      throw new RFWCriticalException("RFW_ERR_200331", e);
    }
    return obj;
  }

  /**
   * Serializa um objeto para XML utilizando o padrão JAXB do nativo do Java.
   *
   * @param object Objeto a ser serializado.
   * @param clazz Classe do Objeto a ser serializado
   * @throws RFWException
   */
  public static String serializeToXML(Object object, Class<?> clazz) throws RFWException {
    try {
      JAXBContext jc = JAXBContext.newInstance(clazz);
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      m.marshal(object, os);
      os.close();
      return new String(os.toByteArray());
    } catch (Throwable e) {
      throw new RFWCriticalException("RFW_ERR_200492", e);
    }
  }

  /**
   * Desserializa um objeto a partir do seu XML utilizando o padrão JAXB do nativo do Java.
   *
   * @param xml String contendo o XML para desserialização.
   * @param clazz Classe do Objeto que será montado a partir do XML.
   * @throws RFWException
   */
  public static Object desserializeFromXML(String xml, Class<?> clazz) throws RFWException {
    try {
      JAXBContext jc = JAXBContext.newInstance(clazz);
      Unmarshaller m = jc.createUnmarshaller();
      Object r = m.unmarshal(new StringReader(xml));
      return r;
    } catch (Throwable e) {
      throw new RFWCriticalException("RFW_ERR_200492", e);
    }
  }

}
