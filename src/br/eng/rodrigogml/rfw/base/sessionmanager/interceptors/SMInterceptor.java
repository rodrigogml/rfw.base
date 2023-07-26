package br.eng.rodrigogml.rfw.base.sessionmanager.interceptors;

import java.util.HashMap;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import br.eng.rodrigogml.rfw.base.sessionmanager.SessionManager;
import br.eng.rodrigogml.rfw.base.sessionmanager.annotations.Security;
import br.eng.rodrigogml.rfw.base.sessionmanager.annotations.Security.SecurityAction;
import br.eng.rodrigogml.rfw.base.sessionmanager.interfaces.SessionVO;
import br.eng.rodrigogml.rfw.kernel.RFW;
import br.eng.rodrigogml.rfw.kernel.eventdispatcher.EventDispatcher;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWCriticalException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWWarningException;
import br.eng.rodrigogml.rfw.kernel.utils.RUArray;
import br.eng.rodrigogml.rfw.kernel.utils.RUGenerators;
import br.eng.rodrigogml.rfw.kernel.vo.RFWRecursiveClonable;

/**
 * Description: Claase interceptadora da chamada da fachada para verifica��o de sess�o, autoriza��o e necessidade de rollback.<br>
 *
 * @author Rodrigo Leit�o
 * @since 10.0.0 (12 de jul de 2018)
 */
@Interceptor
public class SMInterceptor {

  @Resource
  private SessionContext context;

  @AroundInvoke
  public Object businessIntercept(InvocationContext ctx) throws Exception {
    EventDispatcher.beginScope();

    Object[] parameters = ctx.getParameters();

    boolean commited = false;

    try {
      // Por padr�o o Security Espera sempre que o primeiro parametro seja o UUID de uma transa��o v�lida. S� "pulamos" a valida��o da transa��o se o m�todo chamado tiver a Annotation
      final Security annSec = ctx.getMethod().getAnnotation(Security.class);
      // Validamos a seguran�a de acordo com as defini��es do m�todo
      SecurityAction action = SecurityAction.HASSESSION; // Define como a a��o padr�o caso n�o tenha a annotation
      if (annSec != null) action = annSec.action();
      SessionVO ssVO = null; // Salva a sess�o se conseguir identifica-la
      switch (action) {
        case SKIP:
          // N�o faz nada
          break;
        case HASSESSION: {
          // Verificamos se recebemos uma sess�o v�lida
          int type = checkSession(ctx, parameters);
          if (type != 0 && type != 1) throw new RFWCriticalException("O m�todo exige uma sess�o v�lida! Nenhum UUID ou TOKEN v�lido foi encontrado!");
          // Se � uma chave v�lida, verificamos se a sess�o � v�lida. Se a sess�o n�o for v�lida o m�todo lan�ar� a exception
          ssVO = SessionManager.getSession((String) parameters[0]);
          // Se n�o deu exception, a transa��o � v�lida, registramos a sess�o nesta Thread
          SessionManager.attachSessionToThread(Thread.currentThread(), ssVO.getUUID());
        }
          break;
        case HASKEY: {
          // Verificamos se recebemos uma sess�o v�lida
          int type = checkSession(ctx, parameters);
          if (type != 0 && type != 1) throw new RFWCriticalException("O m�todo exige uma sess�o v�lida! Nenhum UUID ou TOKEN v�lido foi encontrado!");
          // Se � uma chave v�lida, verificamos se a sess�o � v�lida. Se a sess�o n�o for v�lida o m�todo lan�ar� a exception
          ssVO = SessionManager.getSession((String) parameters[0]);
          // Verificamos se o usu�rio tem a chave definida
          if (!ssVO.hasAccess(annSec.key())) throw new RFWWarningException("RFW_ERR_300070", new String[] { ssVO.getUser(), RUArray.concatArrayIntoString(annSec.key(), 999) });
          // Se n�o deu exception, a transa��o � v�lida, registramos a sess�o nesta Thread
          SessionManager.attachSessionToThread(Thread.currentThread(), ssVO.getUUID());
        }
          break;
        case HASTOKENSESSION: {
          // Verificamos se recebemos uma sess�o v�lida
          int type = checkSession(ctx, parameters);
          if (type != 1) throw new RFWCriticalException("O m�todo exige uma autentica��o por token! Nenhum Token v�lido foi encontrado!");
          // Se � uma chave v�lida, verificamos se a sess�o � v�lida. Se a sess�o n�o for v�lida o m�todo lan�ar� a exception
          ssVO = SessionManager.getSession((String) parameters[0]);
          // Se n�o deu exception, a transa��o � v�lida, registramos a sess�o nesta Thread
          SessionManager.attachSessionToThread(Thread.currentThread(), ssVO.getUUID());
        }
          break;
      }

      try {
        // Faz o interceptor processar o cloneRecursive para todos os VOs e remover as chamadas espalhadas atualmente em algumas fachadas
        if (parameters != null) {
          final HashMap<RFWRecursiveClonable, RFWRecursiveClonable> clonedObjects = new HashMap<RFWRecursiveClonable, RFWRecursiveClonable>(); // Mantemos a mesma refer�ncia da hash no clone para que, se em dois par�metros distintos do m�todo forem passados o memo objeto (mesmo que aninhados) o clone detecte que s�o o mesmo e n�o crie mais int�ncias diferentes para passar para o m�todo
          for (int i = 0; i < parameters.length; i++) {
            Object object = parameters[i];
            if (object instanceof RFWRecursiveClonable) {
              parameters[i] = ((RFWRecursiveClonable) object).cloneRecursive(clonedObjects);
            }
          }
          ctx.setParameters(parameters);
        }
        // Invoca o m�todo da fachada
        Object proceed = ctx.proceed();

        commited = true;

        return proceed;
      } catch (Throwable e) {
        // For�a o Rollback em qualquer caso de exception. Algumas vezes a exception que chega aqui n�o � a do FrameWork, e sem for�ar o rollback parte da transa��o continua terminando em Comited.
        if (context != null) context.setRollbackOnly();
        if (RFW.isDevelopmentEnvironment()) e.printStackTrace();
        // Deixa a exce��o seguir normalmente
        throw e;
      }
    } finally {
      SessionManager.cleanThread(Thread.currentThread());
      EventDispatcher.endScope(commited);
    }
  }

  /**
   * Verifica se recebemos a UUID no primeiro par�metro e se a sess�o � valida.
   *
   * @param ctx Context da Chamada da Fachada para que seja poss�vel retirar os par�metreos sendo recebidos e informa��es para log.
   * @param parameters Par�metros do m�todo da fachada para tentar detectar o UUID.
   * @return 0 caso seja detectado um UUID, 1 caso seja detectado um Token de Acesso.
   * @throws RFWException Lan�ado caso n�o se encontre nem um UUID v�lido nem um Token de acesso.
   */
  private int checkSession(InvocationContext ctx, Object[] parameters) throws RFWException {
    Integer type = null;
    if (parameters == null || parameters.length < 1 || parameters[0] == null || !(parameters[0] instanceof String)) {
      throw new RFWCriticalException("ID de sess�o inv�lida para chamada da fachada: ${0}", new String[] { ctx.getMethod().getDeclaringClass().getCanonicalName() + "#" + ctx.getMethod().getName() });
    } else if (((String) parameters[0]).matches(RUGenerators.UUID_REGEXP)) {
      type = 0;
    } else if (((String) parameters[0]).matches("(\\Q" + SessionManager.getTokenPrefix() + "\\E)?[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}")) {
      type = 1;
    }
    if (type == null) throw new RFWCriticalException("ID de sess�o inv�lida para chamada da fachada: ${0}", new String[] { ctx.getMethod().getDeclaringClass().getCanonicalName() + "#" + ctx.getMethod().getName(), ((String) parameters[0]) });
    return type;
  }

}
