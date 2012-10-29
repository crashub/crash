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

import org.crsh.cmdline.CommandCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.util.CloseableList;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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
        switch (msg) {
          case GET_WELCOME:
            String welcome = shell.getWelcome();
            out.writeObject(welcome);
            out.flush();
            break;
          case GET_PROMPT:
            String prompt = shell.getPrompt();
            out.writeObject(prompt);
            out.flush();
            break;
          case GET_COMPLETION:
            String prefix = (String)in.readObject();
            CommandCompletion completion = shell.complete(prefix);
            out.writeObject(completion);
            out.flush();
            break;
          case SET_SIZE:
            width = (Integer) in.readObject();
            height = (Integer) in.readObject();
            last = System.currentTimeMillis();
            break;
          case EXECUTE:
            width = (Integer) in.readObject();
            height = (Integer) in.readObject();
            last = System.currentTimeMillis();
            String line = (String)in.readObject();
            ShellProcess process = shell.createProcess(line);
            current = new ClientProcessContext(this, process);
            process.execute(current);
            break;
          case CANCEL:
            if (current != null) {
              current.process.cancel();
            }
            break;
          case CLOSE:
            close();
            break;
        }
      }
    }
    catch (Exception e) {
      // e.printStackTrace();
      //
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
