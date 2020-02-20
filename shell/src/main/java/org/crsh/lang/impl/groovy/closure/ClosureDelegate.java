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
package org.crsh.lang.impl.groovy.closure;

import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.command.CommandContext;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.lang.impl.groovy.Helper;
import org.crsh.shell.Shell;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.util.SafeCallable;

/** @author Julien Viet */
class ClosureDelegate extends GroovyObjectSupport {

  /** . */
  private final CommandContext context;

  /** . */
  private final Object owner;

  public ClosureDelegate(CommandContext context, Object owner) {
    this.context = context;
    this.owner = owner;
  }

  public CommandContext getContext() {
    return context;
  }

  @Override
  public Object getProperty(String property) {
    if ("context".equals(property)) {
      return context;
    } else {
      Object value = Helper.resolveProperty(new InvocationContextImpl(context, ShellSafetyFactory.getCurrentThreadShellSafety()), property);
      if (value != null) {
        return value;
      } else {
        value = InvokerHelper.getProperty(owner, property);
      }
      return value;
    }
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    SafeCallable runnable = Helper.resolveMethodInvocation(new InvocationContextImpl(context, ShellSafetyFactory.getCurrentThreadShellSafety()), name, args);
    if (runnable != null) {
      return runnable.call();
    } else {
      return InvokerHelper.invokeMethod(owner, name, args);
    }
  }
}
