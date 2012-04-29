package org.crsh.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Use sun.misc.Handler proprietary API for handling INT signal. This class use reflection and does not
 * import sun.misc.* package.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class InterruptHandler {

  /** . */
  private final Runnable runnable;

  /** . */
  private final Logger log = LoggerFactory.getLogger(InterruptHandler.class);

  private final InvocationHandler handler = new InvocationHandler() {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (method.getName().equals("hashCode") && parameterTypes.length == 0) {
        return System.identityHashCode(runnable);
      } else if (method.getName().equals("equals") && parameterTypes.length == 1 && parameterTypes[0] == Object.class) {
        return runnable.equals(args[0]);
      } else if (method.getName().equals("toString") && parameterTypes.length == 0) {
        return runnable.toString();
      } else if (method.getName().equals("handle")) {
        runnable.run();
        return null;
      } else {
        throw new UnsupportedOperationException("Method " + method + " not implemented");
      }
    }
  };

  public InterruptHandler(Runnable runnable) {
    this.runnable = runnable;
  }

  public void install() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Class<?> signalHandlerClass;
    Class<?> signalClass;
    Method handle;
    Object INT;
    try {
      signalHandlerClass = cl.loadClass("sun.misc.SignalHandler");
      signalClass = cl.loadClass("sun.misc.Signal");
      handle = signalClass.getDeclaredMethod("handle", signalClass, signalHandlerClass);
      Constructor ctor = signalClass.getConstructor(String.class);
      INT = ctor.newInstance("INT");
    }
    catch (Exception e) {
      return;
    }

    //
    Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{signalHandlerClass}, handler);

    //
    try {
      handle.invoke(null, INT, proxy);
    }
    catch (Exception e) {
      log.error("Could not install signal handler", e);
    }
  }
}
