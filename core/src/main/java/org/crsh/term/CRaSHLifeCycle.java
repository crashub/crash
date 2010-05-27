/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.term;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.crsh.jcr.NodeMetaClass;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellContext;

import javax.jcr.Node;
import java.beans.IntrospectionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CRaSHLifeCycle {

  /** . */
  private static final Object LOCK = new Object();

  /** . */
  private static boolean integrated = false;

  private static void integrate() {
    synchronized (LOCK) {
      if (!integrated) {
        try {
          MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
          Class<? extends Node> eXoNode = (Class<Node>)Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.impl.core.NodeImpl");
          NodeMetaClass mc2 = new NodeMetaClass(registry, eXoNode);
          mc2.initialize();
          registry.setMetaClass(eXoNode, mc2);
        }
        catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        catch (IntrospectionException e) {
          throw new RuntimeException(e);
        }
      }
      integrated = true;
    }
  }

  /** . */
  private ShellFactory builder;

  /** . */
  private final ShellContext context;

  /** . */
  private ExecutorService executor;

  protected CRaSHLifeCycle(ShellContext context) {
    if (context == null) {
      throw new NullPointerException();
    }

    //
    this.context = context;
  }

  public final void init() {
    integrate();
    ExecutorService executor = Executors.newFixedThreadPool(3);
    ShellFactory builder = new ShellFactory(context);

    //
    this.builder = builder;
    this.executor = executor;

    //
    try {
      doInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public final void destroy() {
    doDestroy();

    //
    executor.shutdownNow();

    //
    this.executor = null;
    this.builder = null;
  }

  public final ExecutorService getExecutor() {
    return executor;
  }

  public final ShellFactory getShellFactory() {
    return builder;
  }

  public final ShellContext getShellContext() {
    return context;
  }

  protected abstract void doInit() throws Exception;

  protected abstract void doDestroy();

}
