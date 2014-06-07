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

import groovy.lang.GroovyShell;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.CommandContext;
import org.crsh.lang.spi.Language;
import org.crsh.lang.spi.ReplResponse;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.lang.impl.groovy.closure.PipeLineInvoker;
import org.crsh.lang.spi.Repl;
import org.crsh.cli.impl.line.LineParser;

import java.io.IOException;

/**
 * Groovy REPL implementation.
 *
 * @author Julien Viet
 */
public class GroovyRepl implements Repl {

  /** . */
  final GroovyLanguage lang;

  public GroovyRepl(GroovyLanguage lang) {
    // Force to load Groovy here or fail
    Object o = GroovyShell.class;
    this.lang = lang;
  }

  @Override
  public Language getLanguage() {
    return lang;
  }

  @Override
  public String getDescription() {
    return "The Groovy repl provides a Groovy interpreter able to interact with shell commands";
  }

  public ReplResponse eval(final ShellSession session, final String r2) {


    GroovyLineEscaper foo = new GroovyLineEscaper();
    LineParser parser = new LineParser(foo);
    parser.append(r2);
    final String request = foo.buffer.toString();


    //
    CommandInvoker<Void, Object> invoker = new CommandInvoker<Void, Object>() {
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
      CommandContext<Object> foo;
      public void open(CommandContext<? super Object> consumer) {
        this.foo = (CommandContext<Object>)consumer;
        GroovyShell shell = GroovyCompiler.getGroovyShell(session);
        ShellBinding binding = (ShellBinding)shell.getContext();
        binding.setCurrent(foo);
        Object o;
        try {
          o = shell.evaluate(request);
        }
        finally {
          binding.setCurrent(null);
        }
        if (o != null) {
          try {
            consumer.provide(o);
          }
          catch (IOException e) {
            throw new UnsupportedOperationException("handle me gracefully", e);
          }
        }
      }
      public void close() throws IOException {
        foo.flush();
        foo.close();
      }
    };
    return new ReplResponse.Invoke(invoker);
  }

  public CompletionMatch complete(ShellSession session, String prefix) {
    return new CompletionMatch(Delimiter.EMPTY, Completion.create());
  }
}
