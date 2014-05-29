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

package org.crsh.shell.impl.remoting;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.util.CloseableList;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerAutomaton implements Shell {

  /** . */
  final Logger log = Logger.getLogger(ServerAutomaton.class.getName());

  /** . */
  final ObjectInputStream in;

  /** . */
  final ObjectOutputStream out;

  /** . */
  ServerProcess process;

  /** . */
  final CloseableList listeners;

  public ServerAutomaton(ObjectOutputStream out, ObjectInputStream in) {
    CloseableList listeners = new CloseableList();
    listeners.add(in);
    listeners.add(out);

    //
    this.in = in;
    this.out = out;
    this.listeners = listeners;
  }

  public ServerAutomaton(InputStream in, OutputStream out) throws IOException {
    this(new ObjectOutputStream(out), new ObjectInputStream(in));
  }

  public ServerAutomaton addCloseListener(Closeable closeable) {
    listeners.add(closeable);
    return this;
  }

  public String getWelcome() {
    try {
      out.writeObject(new ClientMessage.GetWelcome());
      out.flush();
      return ((ServerMessage.Welcome)in.readObject()).value;
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public String getPrompt() {
    try {
      out.writeObject(new ClientMessage.GetPrompt());
      out.flush();
      return ((ServerMessage.Prompt)in.readObject()).value;
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public ShellProcess createProcess(String request) throws IllegalStateException {
    return new ServerProcess(this, request);
  }

  public CompletionMatch complete(String prefix) {
    try {
      out.writeObject(new ClientMessage.GetCompletion(prefix));
      out.flush();
      return ((ServerMessage.Completion)in.readObject()).value;
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public void close() {
    listeners.close();
  }

  void execute(ServerProcess process, ShellProcessContext processContext) throws IllegalStateException {

    if (this.process == null) {
      this.process = process;
    } else {
      throw new IllegalStateException();
    }

    //
    ShellResponse response = null;
    try {
      out.writeObject(new ClientMessage.Execute(processContext.getWidth(), processContext.getHeight(), process.line));
      out.flush();

      //
      while (response == null) {
        ServerMessage msg = (ServerMessage)in.readObject();
        if (msg instanceof ServerMessage.GetSize) {
          out.writeObject(new ClientMessage.SetSize(processContext.getWidth(), processContext.getHeight()));
          out.flush();
        } else if (msg instanceof ServerMessage.ReadLine) {
//            // This case should not really well supported ?
//            String request = (String)in.readObject();
//            boolean echo = (Boolean)in.readObject();
//            String line = processContext.readLine(request, echo);
//            out.writeObject(line);
//            out.flush();
//            break;
          throw new UnsupportedOperationException("Not handled");
        } else if (msg instanceof ServerMessage.UseAlternateBuffer) {
          processContext.takeAlternateBuffer();
        } else if (msg instanceof ServerMessage.UseMainBuffer) {
          processContext.releaseAlternateBuffer();
        } else if (msg instanceof ServerMessage.End) {
          response = ((ServerMessage.End)msg).response;
        } else if (msg instanceof ServerMessage.Chunk) {

          ServerMessage.Chunk chunk = ((ServerMessage.Chunk)msg);
          if (chunk instanceof ServerMessage.Chunk.Text) {
            processContext.append(((ServerMessage.Chunk.Text)chunk).payload);
          } else if (chunk instanceof ServerMessage.Chunk.Cls) {
            processContext.cls();
          } else {
            processContext.append(((ServerMessage.Chunk.Style)chunk).payload);
          }
        } else if (msg instanceof ServerMessage.Flush) {
          processContext.flush();
        } else {
          response = ShellResponse.internalError("Unexpected");
        }
      }
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Remoting issue", e);
      response = ShellResponse.internalError("Remoting issue", e);
    }
    finally {

      //
      this.process = null;

      //
      if (response != null) {
        processContext.end(response);
      } else {
        processContext.end(ShellResponse.internalError(""));
      }
    }
  }

  void cancel(ServerProcess process) throws IllegalStateException {
    if (process == this.process) {
      this.process = null;
      try {
        out.writeObject(new ClientMessage.Cancel());
        out.flush();
      }
      catch (IOException ignore) {
      }
    }
  }
}
