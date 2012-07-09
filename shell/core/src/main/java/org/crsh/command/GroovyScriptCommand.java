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
package org.crsh.command;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.util.Strings;

import java.util.List;

/**
 * This class provides the base class for Groovy scripts. It should not be used directly as it is rather used
 * for configuring a Groovy {@link org.codehaus.groovy.control.CompilerConfiguration#setScriptBaseClass(String)} class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class GroovyScriptCommand extends Script implements ShellCommand, CommandInvoker<Void, Void> {

  /** . */
  private CommandContext context;

  /** . */
  private String[] args;

  public final Class<Void> getProducedType() {
    return Void.class;
  }

  public final Class<Void> getConsumedType() {
    return Void.class;
  }

  @Override
  public Object invokeMethod(String name, Object args) {

    //
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      if (context instanceof InvocationContext) {
        InvocationContext ic = (InvocationContext)context;
        CRaSH crash = (CRaSH)context.getSession().get("crash");
        if (crash != null) {
          ShellCommand cmd;
          try {
            cmd = crash.getCommand(name);
          }
          catch (NoSuchCommandException ce) {
            throw new InvokerInvocationException(ce);
          }
          if (cmd != null) {
            CommandDispatcher dispatcher = new CommandDispatcher(cmd, ic);
            return dispatcher.dispatch("", args);
          }
        }
      }

      //
      throw e;
    }
  }

  @Override
  public final Object getProperty(String property) {
    if ("out".equals(property)) {
      if (context instanceof InvocationContext<?, ?>) {
        return ((InvocationContext<?, ?>)context).getWriter();
      } else {
        return null;
      }
    } else if ("context".equals(property)) {
      return context;
    } else {
      if (context instanceof InvocationContext<?, ?>) {
        CRaSH crash = (CRaSH)context.getSession().get("crash");
        if (crash != null) {
          try {
            ShellCommand cmd = crash.getCommand(property);
            if (cmd != null) {
              return new CommandDispatcher(cmd, (InvocationContext<?, ?>)context);
            }
          } catch (NoSuchCommandException e) {
            throw new InvokerInvocationException(e);
          }
        }
      }

      //
      try {
        return super.getProperty(property);
      }
      catch (MissingPropertyException e) {
        return null;
      }
    }
  }

  public final CommandCompletion complete(CommandContext context, String line) {
    return new CommandCompletion(Delimiter.EMPTY, ValueCompletion.create());
  }

  public String describe(String line, DescriptionFormat mode) {
    return null;
  }

  public final void invoke(InvocationContext<Void, Void> context) throws ScriptException {

    // Set up current binding
    Binding binding = new Binding(context.getSession());

    // Set the args on the script
    binding.setProperty("args", args);

    //
    setBinding(binding);

    //
    this.context = context;
    try {
      //
      Object res = run();

      // Evaluate the closure
      if (res instanceof Closure) {
        Closure closure = (Closure)res;
        res = closure.call(args);
      }

      //
      if (res != null) {
        context.getWriter().print(res);
      }
    }
    catch (Exception t) {
      throw CRaSHCommand.toScript(t);
    }
    finally {
      this.context = null;
    }
  }

  public final CommandInvoker<?, ?> createInvoker(String line) {
    List<String> chunks = Strings.chunks(line);
    this.args = chunks.toArray(new String[chunks.size()]);
    return this;
  }
}
