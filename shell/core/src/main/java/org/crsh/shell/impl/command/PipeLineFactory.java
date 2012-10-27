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

package org.crsh.shell.impl.command;

import org.crsh.command.CommandInvoker;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellProcessContext;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * A factory for a pipeline.
 */
class PipeLineFactory {

  /** . */
  final String line;

  /** . */
  final String name;

  /** . */
  final String rest;

  /** . */
  final PipeLineFactory next;

  public String getLine() {
    return line;
  }

  PipeLineFactory(String line, PipeLineFactory next) {

    Pattern p = Pattern.compile("^\\s*(\\S+)");
    java.util.regex.Matcher m = p.matcher(line);
    String name = null;
    String rest = null;
    if (m.find()) {
      name = m.group(1);
      rest = line.substring(m.end());
    }

    //
    this.name = name;
    this.rest = rest;
    this.line = line;
    this.next = next;
  }

  PipeLine create(CRaSHSession session) throws NoSuchCommandException {

    //
    LinkedList<PipeProxy> pipes = new LinkedList<PipeProxy>();
    for (PipeLineFactory current = this;current != null;current = current.next) {
      CommandInvoker commandInvoker = null;
      if (current.name != null) {
        ShellCommand command = session.crash.getCommand(current.name);
        if (command != null) {
          commandInvoker = command.resolveInvoker(current.rest);
        }
      }
      if (commandInvoker == null) {
        throw new NoSuchCommandException(current.name);
      }
      pipes.add(new PipeProxy(commandInvoker));
    }

    //
    return new PipeLine(pipes.toArray(new PipeFilter[pipes.size()]));

  }

/*
  PipeProxy makeProxy(CRaSHSession session) throws NoSuchCommandException {

    CommandInvoker invoker = null;
    if (name != null) {
      ShellCommand command = session.crash.getCommand(name);
      if (command != null) {
        invoker = command.resolveInvoker(rest);
      }
    }

    //
    if (invoker == null) {
      throw new NoSuchCommandException(name);
    }

    //
    InvocationContext next;
    if (this.next != null) {

      PipeProxy proxy = this.next.makeProxy(session);

      // Open the next
      // Try to do some type adaptation
      if (invoker.getProducedType() == Chunk.class) {
        if (proxy.command.getConsumedType() == Chunk.class) {
          next = this.next.makeProxy(session);
        } else {
          throw new UnsupportedOperationException("Not supported yet");
        }
      } else {
        if (invoker.getProducedType().isAssignableFrom(proxy.command.getConsumedType())) {
          next = this.next.makeProxy(session);
        } else {
          next = new Foo(this.next.makeProxy(session));
        }
      }
    } else {
      next = null;
    }

    //
    return new PipeProxy(invoker);
  }
*/

  PipeLineFactory getLast() {
    if (next != null) {
      return next.getLast();
    }
    return this;
  }

  //
  CRaSHProcess create(final CRaSHSession session, String request) {
    return new CRaSHProcess(session, request) {
      @Override
      ShellResponse doInvoke(final ShellProcessContext context) throws InterruptedException {

        //
        PipeLine proxy;
        try {
          proxy = create(crash);
        }
        catch (NoSuchCommandException e) {
          return ShellResponse.unknownCommand(e.getCommandName());
        }

        //
        try {
          proxy.invoke(new ProcessInvocationContext(session, context));
        }
        catch (ScriptException e) {
          // Should we handle InterruptedException here ?
          return build(e);
        } catch (Throwable t) {
          return build(t);
        }
        return ShellResponse.ok();
      }
    };
  }

  private ShellResponse.Error build(Throwable throwable) {
    ErrorType errorType;
    if (throwable instanceof ScriptException) {
      errorType = ErrorType.EVALUATION;
      Throwable cause = throwable.getCause();
      if (cause != null) {
        throwable = cause;
      }
    } else {
      errorType = ErrorType.INTERNAL;
    }
    String result;
    String msg = throwable.getMessage();
    if (throwable instanceof ScriptException) {
      if (msg == null) {
        result = name + ": failed";
      } else {
        result = name + ": " + msg;
      }
      return ShellResponse.error(errorType, result, throwable);
    } else {
      if (msg == null) {
        msg = throwable.getClass().getSimpleName();
      }
      if (throwable instanceof RuntimeException) {
        result = name + ": exception: " + msg;
      } else if (throwable instanceof Exception) {
        result = name + ": exception: " + msg;
      } else if (throwable instanceof java.lang.Error) {
        result = name + ": error: " + msg;
      } else {
        result = name + ": unexpected throwable: " + msg;
      }
      return ShellResponse.error(errorType, result, throwable);
    }
  }
}
