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
package org.crsh.golo;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.parser.TokenMgrError;
import gololang.EvaluationEnvironment;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandCreationException;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.repl.EvalResponse;
import org.crsh.repl.Repl;
import org.crsh.repl.ReplSession;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.spi.ShellCommand;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Julien Viet */
public class GoloREPL extends CRaSHPlugin<Repl> implements Repl {

  /** . */
  private GoloClassLoader replLoader;

  public GoloREPL() {}

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public String getDescription() {
    return "The Golo REPL provides a Groovy interpreter able to interact with shell commands";
  }

  @Override
  public Repl getImplementation() {
    return this;
  }

  @Override
  public void init() {
    PluginContext context = getContext();
    ClassLoader loader = context.getLoader();
    replLoader = new GoloClassLoader(loader);
  }

  public String getName() {
    return "golo";
  }

  public EvalResponse eval(ReplSession session, String request) {

    try {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(replLoader);

      }
      finally {
        Thread.currentThread().setContextClassLoader(old);
      }
      return new EvalResponse.Invoke(buildCommandInvoker(session, request));
    }
    catch (GoloCompilationException e) {
      return new EvalResponse.Response(ShellResponse.evalError("Could not evaluate request", e));
    }
    catch (Exception e) {
      return new EvalResponse.Response(ShellResponse.internalError("Could not evaluate request", e));
    }
    catch (TokenMgrError error) {
      return new EvalResponse.Response(ShellResponse.internalError("Could not parse", error));
    }
  }

  public CompletionMatch complete(ReplSession session, String prefix) {
    return new CompletionMatch(Delimiter.EMPTY, Completion.create());
  }

  private CommandInvoker buildCommandInvoker(ReplSession session, String request) {
    String cmdLinePattern = "(\\S+)(.*)";
    Matcher matcher = Pattern.compile(cmdLinePattern).matcher(request);

    ShellCommand crashCmd = null;
    try {
      if(matcher.matches()) {
        String command = matcher.group(1);
        String line = matcher.group(2);
        crashCmd = session.getCommand(command);
        if (crashCmd != null) {
          return crashCmd.resolveInvoker(line);
        }
      }
    } catch (CommandCreationException e) {
      e.printStackTrace();
    }
    return new GoloCommandInvoker(session, request);
  }


  private class GoloCommandInvoker extends CommandInvoker<Void, Object> {

    private CommandContext<Object> foo;
    private final String request;
    private final ReplSession session;

    private GoloCommandInvoker(ReplSession session, String request) {
      this.session = session;
      this.request = request;
    }

    public void provide(Void element) throws IOException {
      throw new UnsupportedOperationException("Should not be invoked");
    }
    public Class<Void> getConsumedType() {
      return Void.class;
    }
    public void flush() throws IOException {
    }
    public Class<Object> getProducedType() {
      return Object.class;
    }

    public void open(CommandContext<? super Object> consumer) {
      this.foo = (CommandContext<Object>)consumer;
      String mainWrapper = "function run = -> " + request;
      Object o;
      try {
        EvaluationEnvironment environment = new EvaluationEnvironment();
        Class<?> code = (Class<?>) environment.anonymousModule(mainWrapper);
        Method main = code.getDeclaredMethod("run");
        o = main.invoke(null);
        if (o != null) {
          consumer.provide(o);
        }
      } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    public void close() throws IOException {
      foo.flush();
      foo.close();
    }
  }
}
