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
 * Description: Claase interceptadora da chamada da fachada para verificação de sessão, autorização e necessidade de rollback.<br>
 *
 * @author Rodrigo Leitão
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
      // Por padrão o Security Espera sempre que o primeiro parametro seja o UUID de uma transação válida. Só "pulamos" a validação da transação se o método chamado tiver a Annotation
      final Security annSec = ctx.getMethod().getAnnotation(Security.class);
      // Validamos a segurança de acordo com as definições do método
      SecurityAction action = SecurityAction.HASSESSION; // Define como a ação padrão caso não tenha a annotation
      if (annSec != null) action = annSec.action();
      SessionVO ssVO = null; // Salva a sessão se conseguir identifica-la
      switch (action) {
        case SKIP:
          // Não faz nada
          break;
        case HASSESSION: {
          // Verificamos se recebemos uma sessão válida
          int type = checkSession(ctx, parameters);
          if (type != 0 && type != 1) throw new RFWCriticalException("O método exige uma sessão válida! Nenhum UUID ou TOKEN válido foi encontrado!");
          // Se é uma chave válida, verificamos se a sessão é válida. Se a sessão não for válida o método lançará a exception
          ssVO = SessionManager.getSession((String) parameters[0]);
          // Se não deu exception, a transação é válida, registramos a sessão nesta Thread
          SessionManager.attachSessionToThread(Thread.currentThread(), ssVO.getUUID());
        }
          break;
        case HASKEY: {
          // Verificamos se recebemos uma sessão válida
          int type = checkSession(ctx, parameters);
          if (type != 0 && type != 1) throw new RFWCriticalException("O método exige uma sessão válida! Nenhum UUID ou TOKEN válido foi encontrado!");
          // Se é uma chave válida, verificamos se a sessão é válida. Se a sessão não for válida o método lançará a exception
          ssVO = SessionManager.getSession((String) parameters[0]);
          // Verificamos se o usuário tem a chave definida
          if (!ssVO.hasAccess(annSec.key())) throw new RFWWarningException("RFW_ERR_300070", new String[] { ssVO.getUser(), RUArray.concatArrayIntoString(annSec.key(), 999) });
          // Se não deu exception, a transação é válida, registramos a sessão nesta Thread
          SessionManager.attachSessionToThread(Thread.currentThread(), ssVO.getUUID());
        }
          break;
        case HASTOKENSESSION: {
          // Verificamos se recebemos uma sessão válida
          int type = checkSession(ctx, parameters);
          if (type != 1) throw new RFWCriticalException("O método exige uma autenticação por token! Nenhum Token válido foi encontrado!");
          // Se é uma chave válida, verificamos se a sessão é válida. Se a sessão não for válida o método lançará a exception
          ssVO = SessionManager.getSession((String) parameters[0]);
          // Se não deu exception, a transação é válida, registramos a sessão nesta Thread
          SessionManager.attachSessionToThread(Thread.currentThread(), ssVO.getUUID());
        }
          break;
      }

      try {
        // Faz o interceptor processar o cloneRecursive para todos os VOs e remover as chamadas espalhadas atualmente em algumas fachadas
        if (parameters != null) {
          final HashMap<RFWRecursiveClonable, RFWRecursiveClonable> clonedObjects = new HashMap<RFWRecursiveClonable, RFWRecursiveClonable>(); // Mantemos a mesma referência da hash no clone para que, se em dois parâmetros distintos do método forem passados o memo objeto (mesmo que aninhados) o clone detecte que são o mesmo e não crie mais intâncias diferentes para passar para o método
          for (int i = 0; i < parameters.length; i++) {
            Object object = parameters[i];
            if (object instanceof RFWRecursiveClonable) {
              parameters[i] = ((RFWRecursiveClonable) object).cloneRecursive(clonedObjects);
            }
          }
          ctx.setParameters(parameters);
        }
        // Invoca o método da fachada
        Object proceed = ctx.proceed();

        commited = true;

        return proceed;
      } catch (Throwable e) {
        // Força o Rollback em qualquer caso de exception. Algumas vezes a exception que chega aqui não é a do FrameWork, e sem forçar o rollback parte da transação continua terminando em Comited.
        if (context != null) context.setRollbackOnly();
        if (RFW.isDevelopmentEnvironment()) e.printStackTrace();
        // Deixa a exceção seguir normalmente
        throw e;
      }
    } finally {
      SessionManager.cleanThread(Thread.currentThread());
      EventDispatcher.endScope(commited);
    }
  }

  /**
   * Verifica se recebemos a UUID no primeiro parâmetro e se a sessão é valida.
   *
   * @param ctx Context da Chamada da Fachada para que seja possível retirar os parâmetreos sendo recebidos e informações para log.
   * @param parameters Parâmetros do método da fachada para tentar detectar o UUID.
   * @return 0 caso seja detectado um UUID, 1 caso seja detectado um Token de Acesso.
   * @throws RFWException Lançado caso não se encontre nem um UUID válido nem um Token de acesso.
   */
  private int checkSession(InvocationContext ctx, Object[] parameters) throws RFWException {
    Integer type = null;
    if (parameters == null || parameters.length < 1 || parameters[0] == null || !(parameters[0] instanceof String)) {
      throw new RFWCriticalException("ID de sessão inválida para chamada da fachada: ${0}", new String[] { ctx.getMethod().getDeclaringClass().getCanonicalName() + "#" + ctx.getMethod().getName() });
    } else if (((String) parameters[0]).matches(RUGenerators.UUID_REGEXP)) {
      type = 0;
    } else if (((String) parameters[0]).matches("(\\Q" + SessionManager.getTokenPrefix() + "\\E)?[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}")) {
      type = 1;
    }
    if (type == null) throw new RFWCriticalException("ID de sessão inválida para chamada da fachada: ${0}", new String[] { ctx.getMethod().getDeclaringClass().getCanonicalName() + "#" + ctx.getMethod().getName(), ((String) parameters[0]) });
    return type;
  }

}
