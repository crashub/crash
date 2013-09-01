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
import org.crsh.shell.ShellResponse;
import org.crsh.util.CloseableList;
import org.crsh.util.Statement;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ClientAutomaton implements Runnable {

  /** . */
  final Shell shell;

  /** . */
  final ObjectOutputStream out;

  /** . */
  final ObjectInputStream in;

  /** . */
  ClientProcessContext current;

  /** . */
  final CloseableList listeners;

  /** . */
  Integer width;

  /** . */
  Integer height;

  /** . */
  long last;

  public ClientAutomaton(ObjectOutputStream out, ObjectInputStream in, Shell shell) {
    CloseableList listeners = new CloseableList();
    listeners.add(in);
    listeners.add(out);

    //
    this.in = in;
    this.out = out;
    this.shell = shell;
    this.listeners = listeners;
    this.width = null;
    this.height = null;
  }

  public ClientAutomaton(InputStream in,OutputStream out, Shell shell) throws IOException {
    this(new ObjectOutputStream(out), new ObjectInputStream(in), shell);
  }

  public ClientAutomaton addCloseListener(Closeable closeable) {
    listeners.add(closeable);
    return this;
  }

  public void run() {
    try {
      while (!listeners.isClosed()) {
        ClientMessage msg = (ClientMessage)in.readObject();

        //
        if (msg instanceof ClientMessage.GetWelcome) {
          String welcome = shell.getWelcome();
          out.writeObject(new ServerMessage.Welcome(welcome));
          out.flush();
        } else if (msg instanceof ClientMessage.GetPrompt) {
          String prompt = shell.getPrompt();
          out.writeObject(new ServerMessage.Prompt(prompt));
          out.flush();
        } else if (msg instanceof ClientMessage.GetCompletion) {
          String prefix = ((ClientMessage.GetCompletion)msg).prefix;
          CompletionMatch completion = shell.complete(prefix);
          out.writeObject(new ServerMessage.Completion(completion));
          out.flush();
        } else if (msg instanceof ClientMessage.SetSize) {
          ClientMessage.SetSize setSize = (ClientMessage.SetSize)msg;
          width = setSize.width;
          height = setSize.height;
          last = System.currentTimeMillis();
        } else if (msg instanceof ClientMessage.Execute) {
          ClientMessage.Execute execute = (ClientMessage.Execute)msg;
          width = execute.width;
          height = execute.height;
          last = System.currentTimeMillis();
          current = new ClientProcessContext(this, shell.createProcess(execute.line));
          current.execute();
        } else if (msg instanceof ClientMessage.Cancel) {
          if (current != null) {

            // For now we
            // 1/ end the context
            // 2/ cancel the process
            // it is not the best strategy instead we should
            // 1/ cancel the process
            // 2/ wait a few milli seconds
            // 3/ if it's not ended then we end it

            final ClientProcessContext context = current;
            Statement statements = new Statement() {
              @Override
              protected void run() throws Throwable {
                context.end(ShellResponse.cancelled());
              }
            }.with(new Statement() {
              @Override
              protected void run() throws Throwable {
                context.process.cancel();
              }
            });
            statements.all();
          }
        } else if (msg instanceof ClientMessage.Close) {
          close();
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      //
    }
    finally {
      close();
    }
  }

  void close() {
    listeners.close();
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
