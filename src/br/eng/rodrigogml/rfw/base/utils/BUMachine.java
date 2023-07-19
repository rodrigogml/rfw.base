package br.eng.rodrigogml.rfw.base.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;

import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe utilit�ria para m�todos que interagem com a m�quina (pc), como recuperar IP, MAC Address, Discos, Capacidades de Processamento, etc.<BR>
 *
 * @author Rodrigo Leit�o
 * @since 5.0.0 (11/05/2012)
 */
public class BUMachine {

  /**
   * Tipos de Sistema Operacional detect�veis por esta classe.
   */
  public static enum OSType {
    WINDOWS, LINUX, MAC, UNKNOW
  }

  private BUMachine() {
  }

  /**
   * Recupera a placa "padr�o" (ou primeira) que o java encontrar e retorna sua MAC Address.
   *
   * @return MAC Address da m�quina no formaxo XX-XX-XX-XX-XX.
   * @throws RFWException Caso n�o consiga recuperar as informa��es necess�rias.
   */
  public static String getLocalHostMacAddress() throws RFWException {
    try {
      InetAddress ip = InetAddress.getLocalHost();
      NetworkInterface network = NetworkInterface.getByInetAddress(ip);

      byte[] mac = network.getHardwareAddress();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < mac.length; i++) {
        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
      }
      return sb.toString();
    } catch (UnknownHostException e) {
      throw new RFWCriticalException("RFW_ERR_200057", e);
    } catch (SocketException e) {
      throw new RFWCriticalException("RFW_ERR_200057", e);
    }
  }

  /**
   * Retorna true se o programa estiver rodando atualmente em uma arquitetura de 32 bits.
   */
  public static boolean isJava32Bit() {
    return "32".equals(System.getProperty("sun.arch.data.model"));
  }

  /**
   * Retorna true se o programa estiver rodando atualmente em uma arquitetura de 64 bits.
   */
  public static boolean isJava64Bit() {
    return "64".equals(System.getProperty("sun.arch.data.model"));
  }

  /**
   * Retorna o modelo da arquitetura em que o programa est� rodando.
   */
  public static String getJavaArchitetureModel() {
    return System.getProperty("sun.arch.data.model");
  }

  /**
   * Recupera a vers�o da JVM atual
   */
  public static String getJavaVMVersion() {
    return ManagementFactory.getRuntimeMXBean().getVmVersion();
  }

  /**
   * Recupera a vers�o do Runtime do Java. System.getProperty("java.runtime.version"). Exemplos:
   * <li>"12+33", ou
   * <li>"1.8.0_201-b09", ou
   * <li>"1.5.0_22-b03"
   *
   * @return
   */
  public static String getJavaRuntimeVersion() {
    return System.getProperty("java.runtime.version");
  }

  /**
   * Retorna o sistema operacional em que estamos rodando.
   *
   * @return Retorna o equivalente a propriedade "os.name" do sistema.
   */
  public static String getOperatingSystem() {
    return System.getProperty("os.name");
  }

  /**
   * Tentamos detectar o sistema operacional com base no valor retornado em {@link #getOperatingSystem()}. Este m�todo precisa ser melhorado a medida que novos sistemas operacionais forem criados (novas vers�es podem retornar diferentes valores e atrapalhar a detec��o.
   *
   * @return Sistema Operacional Detectado ou {@link OSType#UNKNOW} caso n�o tenha detectado o sistema Operacional.
   */
  public static OSType getOperatingSystemType() {
    OSType detectedOS;
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
      detectedOS = OSType.MAC;
    } else if (os.indexOf("win") >= 0) {
      detectedOS = OSType.WINDOWS;
    } else if (os.indexOf("nux") >= 0) {
      detectedOS = OSType.LINUX;
    } else {
      detectedOS = OSType.UNKNOW;
    }
    return detectedOS;
  }

}
