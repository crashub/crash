/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.analyzer.Analyzer;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.TypeResolver;

/**
 * A base command that should be subclasses by Groovy commands. For this matter it inherits the
 * {@link groovy.lang.GroovyObjectSupport} class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <C> the consumed type
 * @param <P> the produced type
 */
public abstract class CRaSHCommand<C, P> extends GroovyCommand implements ShellCommand<C, P> {

  /** . */
  private CommandContext<C, P> context;

  /** . */
  private boolean unquoteArguments;

  /** . */
  private Class<C> consumedType;

  /** . */
  private Class<P> producedType;

  /** . */
  private final ClassDescriptor<?> descriptor;

  protected CRaSHCommand() throws IntrospectionException {
    this.context = null;
    this.unquoteArguments = true;
    this.consumedType = (Class<C>)TypeResolver.resolve(getClass(), ShellCommand.class, 0);
    this.producedType = (Class<P>)TypeResolver.resolve(getClass(), ShellCommand.class, 1);
    this.descriptor = CommandDescriptor.create(getClass());
  }

  public Class<P> getProducedType() {
    return producedType;
  }

  public Class<C> getConsumedType() {
    return consumedType;
  }

  /**
   * Returns true if the command wants its arguments to be unquoted.
   *
   * @return true if arguments must be unquoted
   */
  public final boolean getUnquoteArguments() {
    return unquoteArguments;
  }

  public final void setUnquoteArguments(boolean unquoteArguments) {
    this.unquoteArguments = unquoteArguments;
  }

  protected final String readLine(String msg) {
    return readLine(msg, true);
  }

  protected final String readLine(String msg, boolean echo) {
    if (context == null) {
      throw new IllegalStateException("No current context");
    }
    return context.readLine(msg, echo);
  }

  @Override
  protected final CommandContext<?, ?> getContext() {
    return context;
  }

  public final void usage(ShellPrinter printer) {
    Description description = getClass().getAnnotation(Description.class);
    if (description != null) {
      printer.write(description.value());
      printer.write("\n");
    }

    //
    try {
      Class<?> clazz = getClass();
      ClassDescriptor<?> descriptor = CommandDescriptor.create(clazz);
      printer.print(descriptor.getUsage());
    }
    catch (IntrospectionException e) {
      throw new ScriptException(e.getMessage(), e);
    }
  }

  public final void execute(CommandContext<C, P> context, String... args) throws ScriptException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (args == null) {
      throw new NullPointerException();
    }

    // Remove surrounding quotes if there are
    if (unquoteArguments) {
      String[] foo = new String[args.length];
      for (int i = 0;i < args.length;i++) {
        String arg = args[i];
        if (arg.charAt(0) == '\'') {
          if (arg.charAt(arg.length() - 1) == '\'') {
            arg = arg.substring(1, arg.length() - 1);
          }
        } else if (arg.charAt(0) == '"') {
          if (arg.charAt(arg.length() - 1) == '"') {
            arg = arg.substring(1, arg.length() - 1);
          }
        }
        foo[i] = arg;
      }
      args = foo;
    }

    //
    try {
      // WTF
      Analyzer analyzer = new Analyzer(descriptor);
      StringBuilder s = new StringBuilder();
      for (String arg : args) {
        if (s.length() > 0) {
          s.append(" ");
        }
        s.append(arg);
      }
      analyzer.analyze(s.toString()).process(this);
    }
    catch (Exception e) {
      throw new ScriptException(e.getMessage(), e);
    }

    //
    try {
      this.context = context;

      //
      execute(context);
    }
    finally {
      this.context = null;
    }
  }

  protected void execute(CommandContext<C, P> context) throws ScriptException {

    //
    Object o = execute();

    //
    if (o != null) {
      context.getWriter().print(o);
    }
  }

  protected Object execute() throws ScriptException {
    return null;
  }
}
