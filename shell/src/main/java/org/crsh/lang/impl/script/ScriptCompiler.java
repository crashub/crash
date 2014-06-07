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
package org.crsh.lang.impl.script;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.spi.Completer;
import org.crsh.command.CommandContext;
import org.crsh.command.RuntimeContext;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.lang.spi.Compiler;
import org.crsh.lang.spi.ReplResponse;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.spi.CommandMatch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Julien Viet
 */
public class ScriptCompiler implements Compiler {

  /** . */
  private static final Set<String> EXT = Collections.singleton("script");

  /** . */
  static final ScriptCompiler instance = new ScriptCompiler();

  public static ScriptCompiler getInstance() {
    return instance;
  }

  @Override
  public Set<String> getExtensions() {
    return EXT;
  }

  @Override
  public CommandResolution compileCommand(final String name, final byte[] source) throws CommandException, NullPointerException {

    return new CommandResolution() {
      @Override
      public String getDescription() {
        return "";
      }
      @Override
      public Command<?> getCommand() throws CommandException {

        //
        final CommandDescriptor<Object> descriptor;
        try {
          descriptor = new CommandDescriptor<Object>(name, new Description()) {
            @Override
            public CommandDescriptor<Object> getOwner() {
              return null;
            }

            @Override
            public Map<String, ? extends CommandDescriptor<Object>> getSubordinates() {
              return Collections.emptyMap();
            }

            @Override
            public org.crsh.cli.impl.invocation.CommandInvoker<Object, ?> getInvoker(InvocationMatch<Object> match) {
              return new org.crsh.cli.impl.invocation.CommandInvoker<Object, Object>(match) {
                @Override
                public Class<Object> getReturnType() {
                  return Object.class;
                }

                @Override
                public Type getGenericReturnType() {
                  return Object.class;
                }

                @Override
                public Object invoke(Object command) throws InvocationException, SyntaxException {
                  throw new UnsupportedOperationException("Not used");
                }
              };
            }
          };
        }
        catch (IntrospectionException e) {
          throw new CommandException(ErrorKind.SYNTAX, "Script " + name + " failed unexpectedly", e);
        }

        return new Command<Object>() {
          @Override
          public CommandDescriptor<Object> getDescriptor() {
            return descriptor;
          }
          @Override
          protected Completer getCompleter(RuntimeContext context) {
            return null;
          }
          @Override
          protected CommandMatch<?, ?> resolve(InvocationMatch<Object> match) {
            return new CommandMatch<Void, Object>() {
              @Override
              public CommandInvoker<Void, Object> getInvoker() {
                return new CommandInvoker<Void, Object>() {

                  /** . */
                  private CommandContext<?> consumer;

                  @Override
                  public void provide(Void element) throws IOException {
                  }

                  @Override
                  public Class<Void> getConsumedType() {
                    return Void.class;
                  }

                  @Override
                  public void flush() throws IOException {
                    consumer.flush();
                  }

                  @Override
                  public Class<Object> getProducedType() {
                    return Object.class;
                  }

                  @Override
                  public void open(CommandContext<? super Object> consumer) {
                    this.consumer = consumer;
                  }

                  @Override
                  public void close() throws IOException, CommandException {

                    // Execute sequentially the script
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(source)));

                    // A bit nasty but well it's ok
                    ShellSession session = (ShellSession)consumer.getSession();

                    while (true) {
                      String request = reader.readLine();
                      if (request == null) {
                        break;
                      }
                      request = request.trim();
                      if (request.length() == 0) {
                        break;
                      }
                      ReplResponse response = ScriptRepl.getInstance().eval(session, request);
                      if (response instanceof ReplResponse.Response) {
                        ReplResponse.Response shellResponse = (ReplResponse.Response)response;
                        Exception ex = new Exception("Was not expecting response " + shellResponse.response);
                        throw new CommandException(ErrorKind.EVALUATION, "Failure when evaluating '" + request + "'  in script " + name, ex);
                      } else if (response instanceof ReplResponse.Invoke) {
                        ReplResponse.Invoke invokeResponse = (ReplResponse.Invoke)response;
                        CommandInvoker invoker =  invokeResponse.invoker;
                        invoker.invoke(consumer);
                      }
                    }

                    //
                    try {
                      consumer.close();
                    }
                    catch (Exception e) {
                      // ?
                    }

                    //
                    this.consumer = null;
                  }
                };
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
    };
  }

  @Override
  public String doCallBack(ShellSession session, String name, String defaultValue) {
    return null;
  }
}
