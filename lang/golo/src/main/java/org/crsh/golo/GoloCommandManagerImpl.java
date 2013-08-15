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

import gololang.EvaluationEnvironment;
import org.crsh.cli.SyntaxException;
import org.crsh.cli.descriptor.ArgumentDescriptor;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.impl.ParameterType;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.ParameterMatch;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.type.ValueTypeFactory;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandCreationException;
import org.crsh.command.RuntimeContext;
import org.crsh.command.ScriptException;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.ErrorType;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandManager;
import org.crsh.shell.impl.command.spi.CommandResolution;
import org.crsh.shell.impl.command.spi.ShellCommand;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Julien Viet
 */
public class GoloCommandManagerImpl extends CRaSHPlugin<CommandManager> implements CommandManager {

  /** . */
  private static Set<String> EXT = Collections.singleton("golo");

  /** . */
  private final EvaluationEnvironment environment;

  /** . */
  private final PluginContext context;

  public GoloCommandManagerImpl(PluginContext context) {
    this.environment = new EvaluationEnvironment();
    this.context = context;
  }

  @Override
  public CommandManager getImplementation() {
    return this;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public Set<String> getExtensions() {
    return EXT;
  }

  private ShellCommand<Context> make(final String name, final Class<?> code) {

    for (final Method method : code.getDeclaredMethods()) {
      int modifiers = method.getModifiers();
      if (method.getName().equals("run") && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {

        //
        final CommandDescriptor<Context> descriptor = new CommandDescriptor<Context>(name, new Description()) {

          //
          Class<?>[] parameterTypes = method.getParameterTypes();
          ArgumentDescriptor[] arguments = new ArgumentDescriptor[parameterTypes.length];

          {
            for (int i = 0;i < parameterTypes.length;i++) {
              addParameter(arguments[i] = new ArgumentDescriptor(
                  "param" + i,
                  ParameterType.create(ValueTypeFactory.DEFAULT, String.class),
                  new Description(),
                  false,
                  false,
                  false,
                  null,
                  null
              ));
            }
          }

          @Override
          public CommandDescriptor<Context> getOwner() {
            return null;
          }

          @Override
          public Map<String, ? extends CommandDescriptor<Context>> getSubordinates() {
            return Collections.emptyMap();
          }

          @Override
          public CommandInvoker<Context, ?> getInvoker(final InvocationMatch<Context> match) {
            return new CommandInvoker<Context, Object>(match) {
              @Override
              public Class<Object> getReturnType() {
                return Object.class;
              }

              @Override
              public Type getGenericReturnType() {
                return Void.class;
              }

              @Override
              public Object invoke(Context context) throws InvocationException, SyntaxException {
                Object[] args = new Object[method.getParameterTypes().length];
                for (int i = 0;i < arguments.length;i++) {
                  ParameterMatch<ArgumentDescriptor> parameterMatch = match.getParameter(arguments[i]) ;
                  if (parameterMatch != null) {
                    args[i] = parameterMatch.computeValue();
                  }
                }
                try {
                  CRaSH.current.set(context);
                  return method.invoke(null, args);
                }
                catch (IllegalAccessException e) {
                  throw new InvocationException(e);
                }
                catch (InvocationTargetException e) {
                  throw new ScriptException(e.getCause());
                }
                finally {
                  CRaSH.current.remove();
                }
              }
            };
          }
        };

        //
        final HelpDescriptor<Context> helpDescriptor = new HelpDescriptor<>(descriptor);

        //
        return new ShellCommand<Context>() {
          @Override
          public CommandDescriptor<Context> getDescriptor() {
            return helpDescriptor;
          }

          @Override
          protected Completer getCompleter(RuntimeContext context) throws CommandCreationException {
            return null;
          }

          @Override
          protected Command<?, ?> resolveCommand(final InvocationMatch<Context> match) {
            return new Command<Void, Object>() {
              @Override
              public org.crsh.shell.impl.command.spi.CommandInvoker<Void, Object> getInvoker() throws CommandCreationException {
                return new org.crsh.shell.impl.command.spi.CommandInvoker<Void, Object>() {

                  /** . */
                  CommandContext<Object> consumer;

                  @Override
                  public void provide(Void element) throws IOException {
                    // Do nothing
                  }

                  @Override
                  public Class<Void> getConsumedType() {
                    return Void.class;
                  }

                  @Override
                  public void flush() throws IOException {

                  }

                  @Override
                  public Class<Object> getProducedType() {
                    return Object.class;
                  }

                  @Override
                  public void open(CommandContext<? super Object> consumer) {
                    this.consumer = (CommandContext<Object>)consumer;
                  }

                  @Override
                  public void close() throws IOException, UndeclaredThrowableException {
                    Object o = match.getInvoker().invoke(new Context(consumer));
                    if (o != null) {
                      consumer.provide(o);
                    }
                    consumer.flush();
                    consumer.close();
                  }
                };
              }

              @Override
              public InvocationMatch<?> getMatch() {
                return match;
              }

              @Override
              public Class<Object> getProducedType() {
                return Object.class;
              }

              @Override
              public Class<Void> getConsumedType() {
                return Void.class;
              }
            };
          }
        };
      }
    }

    //
    return null;
  }

//  private static

  @Override
  public CommandResolution resolveCommand(final String name, byte[] source) throws CommandCreationException, NullPointerException {

    StringBuilder tmp = new StringBuilder();

    // Override predefined stuff
    tmp.append("local function println = |obj| {  }");

    tmp.append(new String(source));

    Class<?> code;
    try {
      code = (Class<?>) environment.anonymousModule(new String(source));
    }
    catch (Throwable e) {
      throw new CommandCreationException(name, ErrorType.EVALUATION, e.getMessage(), e);
    }
    final ShellCommand<?> command = make(name, code);
    return new CommandResolution() {
      @Override
      public String getDescription() {
        return "";
      }
      @Override
      public ShellCommand getCommand() throws CommandCreationException {
        return command;
      }
    };
  }

  @Override
  public void init(HashMap<String, Object> session) {

  }

  @Override
  public void destroy(HashMap<String, Object> session) {
  }

  @Override
  public String doCallBack(HashMap<String, Object> session, String name, String defaultValue) {
    return null;
  }
}
