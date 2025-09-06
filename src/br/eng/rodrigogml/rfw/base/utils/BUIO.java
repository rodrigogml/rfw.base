package br.eng.rodrigogml.rfw.base.utils;

import java.io.File;
import java.util.ArrayList;

import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Classe utilitária com métodos de IO.<br>
 *
 * @author Rodrigo Leitão
 * @since 7.1.0 (8 de mai de 2019)
 * @deprecated TODOS OS MÉTODOS DAS CLASSES UTILITÁRIAS DO RFW.BASE DEVEM SER MIGRADAS PARA AS CLASSES DO RFW.KERNEL QUANDO NÃO DEPENDEREM DE BIBLIOTECA EXTERNA. QUANDO DEPENDENREM DE BIBILIOTECA EXTERNA DEVEM SER AVALIADAS E CRIADO PROJETOS UTILITÁRIOS ESPECÍFICOS PARA A FUNCIONALIDADE.
 */
@Deprecated
public class BUIO {

  /**
   * Construtor privado para classe utilitária.
   */
  private BUIO() {
  }

  /**
   * Este método lança uma execução java baseada na estrutura atual (se estamos em um jar ou executando classes em pasta aberta como nos momentos de development), e lança uma nova execução.<br>
   * Note que este método NÃO FORÇA A FINALIZAÇÃO DESTA APLICAÇÃO, algo como System.exit(0). Isso pq depois que este método é chamado pode ser necessário ainda executar algum processo de finalização. <br>
   * <br>
   * ATENÇÃO ESTE MÉTODO PRECISA DE MAIS ATENÇÃO E ALGUNS REPAROS... NÃO FUNCIONA BEM TODOS OS CENÁRIOS, Ex; execução em JAR, execução dentor e fora do eclipse, etc...
   *
   * @param executableClass
   * @throws RFWException
   */
  public static void restartApplication(Class<?> executableClass) throws RFWException {
    try {
      final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
      final File currentJar = new File(executableClass.getProtectionDomain().getCodeSource().getLocation().toURI());

      if (currentJar.getName().endsWith(".jar")) {
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
      } else {
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-classpath");
        String classPath = currentJar.getPath();
        if (RFW.isDevelopmentEnvironment()) {
          // Incluímos a pasta de libs na pasta anterior a de classes, que de forma geral dos projetos do RFWDeprec para aplicações, é onde os jars de dependências são copiados
          classPath += File.pathSeparator + currentJar.getPath() + File.separator + ".." + File.separator + "libs" + File.separator + "*";
        }
        command.add(classPath);
        command.add(executableClass.getCanonicalName());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
      }
    } catch (Throwable t) {
      throw new RFWCriticalException("Falha ao montar o comando para reiniciar a aplicação.");
    }
  }

}
