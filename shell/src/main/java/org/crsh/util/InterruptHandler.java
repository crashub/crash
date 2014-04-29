/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterruptHandler {

  /** . */
  private final Runnable runnable;

  /** . */
  private final Logger log = Logger.getLogger(InterruptHandler.class.getName());

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
      log.log(Level.SEVERE, "Could not install signal handler", e);
    }
  }
}