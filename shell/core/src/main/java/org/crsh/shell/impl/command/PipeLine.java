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

import org.crsh.Pipe;
import org.crsh.command.CommandInvoker;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.PipeCommand;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellProcessContext;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;
import org.crsh.text.ChunkBuffer;
import org.crsh.text.RenderingContext;

import java.io.IOException;
import java.util.regex.Pattern;

class PipeLine {

  /** . */
  final String line;

  /** . */
  final String name;

  /** . */
  final String rest;

  /** . */
  private ShellCommand command;

  /** . */
  private CommandInvoker invoker;

  /** . */
  final PipeLine next;

  public String getLine() {
    return line;
  }

  PipeLine(String line, PipeLine next) {

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

  private static class PipeProxy extends PipeCommand {

    /** . */
    private final CRaSHSession session;

    /** . */
    private final ShellProcessContext context;

    /** . */
    private final PipeLine pipeLine;

    /** . */
    private Pipe next;

    /** . */
    private PipeCommand command;

    private PipeProxy(CRaSHSession session, ShellProcessContext context, PipeLine pipeLine) {
      this.session = session;
      this.context = context;
      this.pipeLine = pipeLine;
    }

    public void open() throws ScriptException {
      if (pipeLine.next != null) {

        // Open the next
        // Try to do some type adaptation
        if (pipeLine.invoker.getProducedType() == Chunk.class) {
          if (pipeLine.next.invoker.getConsumedType() == Chunk.class) {
            PipeProxy proxy = new PipeProxy(session, context, pipeLine.next);
            proxy.setPiped(true);
            next = proxy;
            proxy.open();
          } else {
            throw new UnsupportedOperationException("Not supported yet");
          }
        } else {
          if (pipeLine.invoker.getProducedType().isAssignableFrom(pipeLine.next.invoker.getConsumedType())) {
            PipeProxy proxy = new PipeProxy(session, context, pipeLine.next);
            proxy.setPiped(true);
            next = proxy;
            proxy.open();
          } else {
            final PipeProxy proxy = new PipeProxy(session, context, pipeLine.next);
            proxy.setPiped(true);
            proxy.open();
            next = new ChunkAdapter(new RenderingContext() {
              public int getWidth() {
                return context.getWidth();
              }
              public void provide(Chunk element) throws IOException {
                proxy.provide(element);
              }
              public void flush() throws IOException {
                proxy.flush();
              }
            });
          }
        }

      } else {

        // We use this chunk buffer to buffer stuff
        // but also because it optimises the chunks
        // which provides better perormances on the client
        final ChunkBuffer buffer = new ChunkBuffer(context);

        //
        next = new ChunkAdapter(new RenderingContext() {
          public int getWidth() {
            return context.getWidth();
          }
          public void provide(Chunk element) throws IOException {
            buffer.provide(element);
          }
          public void flush() throws IOException {
            buffer.flush();
          }
        });
      }

      //
      CRaSHInvocationContext invocationContext = new CRaSHInvocationContext(
          context,
          session,
          session.crash.getContext().getAttributes(),
          next);

      // Now open command
      command = pipeLine.invoker.invoke(invocationContext);
      command.setPiped(isPiped());
      command.open();
    }

    public void provide(Object element) throws IOException {
      if (pipeLine.invoker.getConsumedType().isInstance(element)) {
        command.provide(element);
      }
    }

    public void flush() throws IOException {

      // First flush the command
      command.flush();

      // Flush the next because the command may not call it
      next.flush();
    }

    public void close() throws ScriptException {
      command.close();
    }
  }

  PipeLine getLast() {
    if (next != null) {
      return next.getLast();
    }
    return this;
  }

   CRaSHProcess create(CRaSHSession session, String request) throws NoSuchCommandException {

     //
     CommandInvoker invoker = null;
     if (name != null) {
       command = session.crash.getCommand(name);
       if (command != null) {
         invoker = command.resolveInvoker(rest);
       }
     }

     //
     if (invoker == null) {
       throw new NoSuchCommandException(name);
     } else {
       this.invoker = invoker;
     }

     //
     if (next != null) {
      next.create(session, request);
    }
    return new CRaSHProcess(session, request) {
      @Override
      ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException {

        PipeProxy proxy = new PipeProxy(crash, context, PipeLine.this);

        try {
          proxy.open();
          proxy.flush();
          proxy.close();
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
