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
package org.crsh.shell.impl;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.crsh.command.CommandContext;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.shell.io.ShellPrinter;

import java.util.Collections;
import java.util.Map;

/**
 * This class provides the base class for Groovy scripts. It should not be used directly as it is rather used
 * for configuring a Groovy {@link org.codehaus.groovy.control.CompilerConfiguration#setScriptBaseClass(String)} class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class GroovyScriptCommand extends Script implements ShellCommand, CommandInvoker<Void, Void> {

  /** . */
  private String[] args;

  public final Class<Void> getProducedType() {
    return Void.class;
  }

  public final Class<Void> getConsumedType() {
    return Void.class;
  }

  @Override
  public final Object getProperty(String property) {
    try {
      return super.getProperty(property);
    }
    catch (MissingPropertyException e) {
      return null;
    }
  }

  public final void usage(ShellPrinter printer) {
    printer.print("Bare script: no usage");
  }

  public final Map<String, String> complete(CommandContext context, String line, String... chunks) {
    return Collections.emptyMap();
  }

  public final void invoke(InvocationContext<Void, Void> context) throws ScriptException {

    // Set up current binding
    Binding binding = new Binding(context.getAttributes());

    // Set the args on the script
    binding.setProperty("args", args);

    //
    setBinding(binding);

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

  public final CommandInvoker<?, ?> createInvoker(String line, String... args) {
    this.args = args;
    return this;
  }
}
