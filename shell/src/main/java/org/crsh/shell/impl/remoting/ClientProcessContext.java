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

import org.crsh.shell.ErrorKind;
import org.crsh.text.Screenable;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Style;
import org.crsh.util.Statement;

import java.io.IOException;
import java.util.ArrayList;

class ClientProcessContext implements ShellProcessContext {

  /** . */
  final ClientAutomaton client;

  /** . */
  final ShellProcess process;

  /** . */
  final ArrayList<ServerMessage.Chunk> buffer;

  /** . */
  private boolean closed;

  ClientProcessContext(ClientAutomaton client, ShellProcess process) {
    this.client = client;
    this.process = process;
    this.buffer = new ArrayList<ServerMessage.Chunk>(1000);
    this.closed = false;
  }

  /**
   * Ensure we have a recent size, the size is considered as recent if it's younger than 2 second, otherwise
   * send a get size message.
   */
  private void ensureSize() {
    if (System.currentTimeMillis() - client.last > 2000) {
      synchronized (this) {
        try {
          client.out.writeObject(new ServerMessage.GetSize());
          client.out.flush();
        }
        catch (Exception e) {
          //
        }
      }
    }
  }

  void execute() {
    try {
      process.execute(this);
    }
    catch(final Throwable t) {
      new Statement() {
        @Override
        protected void run() throws Throwable {
          // If it's not executing then we attempt to end it
          end(ShellResponse.error(ErrorKind.INTERNAL, "Unexpected process execution error", t));
        }
      }.all();
    }
  }

  public int getWidth() {
    if (!closed) {
      ensureSize();
      return client.getWidth();
    } else {
      return -1;
    }
  }

  public int getHeight() {
    if (!closed) {
      ensureSize();
      return client.getHeight();
    } else {
      return -1;
    }
  }

  public boolean takeAlternateBuffer() {
    if (!closed) {
      try {
        client.out.writeObject(new ServerMessage.UseAlternateBuffer());
        client.out.flush();
      }
      catch (Exception e) {
        //
      }
    }

    // For now we suppose any impl return true;
    return true;
  }

  public boolean releaseAlternateBuffer() {
    if (!closed) {
      try {
        client.out.writeObject(new ServerMessage.UseMainBuffer());
        client.out.flush();
      }
      catch (Exception e) {
        //
      }
    }

    // For now we suppose any impl return true;
    return true;
  }

  public String getProperty(String name) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
//    try {
//      client.out.writeObject(ServerMessage.READLINE);
//      client.out.writeObject(msg);
//      client.out.writeObject(echo);
//      client.out.flush();
//      return (String)client.in.readObject();
//    }
//    catch (Exception e) {
//      return null;
//    }
    return null;
  }

  @Override
  public Screenable append(CharSequence s) throws IOException {
    if (!closed) {
      buffer.add(new ServerMessage.Chunk.Text(s));
    }
    return this;
  }

  @Override
  public Screenable append(char c) throws IOException {
    return append(Character.toString(c));
  }

  @Override
  public Screenable append(CharSequence csq, int start, int end) throws IOException {
    return append(csq.subSequence(start, end));
  }

  @Override
  public Screenable append(Style style) throws IOException {
    if (!closed) {
      buffer.add(new ServerMessage.Chunk.Style(style));
    }
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    if (!closed) {
      buffer.add(new ServerMessage.Chunk.Cls());
    }
    return this;
  }

  public synchronized void flush() {
    if (!closed) {
      if (buffer.size() > 0) {
        try {
          for (ServerMessage.Chunk chunk : buffer) {
            client.out.writeObject(chunk);
          }
          client.out.writeObject(new ServerMessage.Flush());
          client.out.flush();
        }
        catch (IOException ignore) {
          //
        }
        finally {
          buffer.clear();
        }
      }
    }
  }

  public synchronized void end(ShellResponse response) {

    // It may have been cancelled concurrently
    if (client.current == this) {

      // Flush what we have in buffer first
      flush();

      // Send end message
      try {
        client.current = null;
        client.out.writeObject(new ServerMessage.End(response));
        client.out.flush();
      }
      catch (IOException ignore) {
        //
      }
      finally {
        closed = true;
        if (response instanceof ShellResponse.Close) {
          client.close();
        }
      }
    }
  }
}
