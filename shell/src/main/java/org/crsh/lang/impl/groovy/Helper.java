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
package org.crsh.lang.impl.groovy;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.command.InvocationContext;
import org.crsh.lang.impl.groovy.closure.PipeLineClosure;
import org.crsh.lang.impl.groovy.closure.PipeLineInvoker;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;

import java.io.IOException;

/**
 * @author Julien Viet
 */
public class Helper {

  public static PipeLineClosure resolveCommandProperty(final InvocationContext context, final String property) {
    CRaSH crash = (CRaSH)context.getSession().get("crash");
    if (crash != null) {
      try {
        Command<?> cmd = crash.getCommand(property);
        if (cmd != null) {
          return new PipeLineClosure(context, property, cmd);
        } else {
          return null;
        }
      } catch (CommandException e) {
        throw new InvokerInvocationException(e);
      }
    } else {
      return null;
    }
  }

  public static Runnable resolveCommandInvocation(final InvocationContext context, final String name, final Object args) {
    CRaSH crash = (CRaSH)context.getSession().get("crash");
    if (crash != null) {
      final Command<?> cmd;
      try {
        cmd = crash.getCommand(name);
      }
      catch (CommandException ce) {
        throw new InvokerInvocationException(ce);
      }
      if (cmd != null) {
        return new Runnable() {
          @Override
          public void run() {
            PipeLineClosure closure = new PipeLineClosure(context, name, cmd);
            PipeLineInvoker evaluation = closure.bind(args);
            try {
              evaluation.invoke(context);
            }
            catch (IOException e) {
              throw new GroovyRuntimeException(e);
            }
            catch (CommandException e) {
              throw new GroovyRuntimeException(e.getCause());
            }
          }
        };
      }
    }
    return null;
  }
}
