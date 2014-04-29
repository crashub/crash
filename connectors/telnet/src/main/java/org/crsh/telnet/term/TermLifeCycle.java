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
package org.crsh.telnet.term;

import org.crsh.plugin.PluginContext;
import org.crsh.telnet.term.spi.TermIOHandler;

import java.util.Iterator;

public abstract class TermLifeCycle {

  /** . */
  private final PluginContext context;

  protected TermLifeCycle(PluginContext context) {
    if (context == null) {
      throw new NullPointerException();
    }

    //
    this.context = context;
  }

  public final void init() {
    try {
      doInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public final void destroy() {
    doDestroy();
  }

  public final TermIOHandler getHandler() {
    Iterator<TermIOHandler> handlers = context.getPlugins(TermIOHandler.class).iterator();
    if (handlers.hasNext()) {
      return handlers.next();
    } else {
      return null;
    }
  }

  public final PluginContext getContext() {
    return context;
  }

  protected abstract void doInit() throws Exception;

  protected abstract void doDestroy();

}
