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
package org.crsh.console;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.CLS;
import org.crsh.text.Chunk;
import org.crsh.text.Style;
import org.crsh.text.Text;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A process execution state machine.
 *
 * @author Julien Viet
 */
class ProcessHandler extends Plugin implements ShellProcessContext {

  /**
   * A thread reading a line.
   */
  class Reader {
    final Thread thread;
    final Editor editor;
    final ArrayBlockingQueue<String> line;
    Reader(Thread thread, boolean echo) {
      this.thread = thread;
      this.editor = new Editor(console, echo);
      this.line = new ArrayBlockingQueue<String>(1);
    }
  }

  /** . */
  final Console console;

  /** . */
  final ShellProcess process;

  /** Weather or not a thread is reading a line callback. */
  final AtomicReference<Reader> editor;

  ProcessHandler(Console console, ShellProcess process) {
    this.console = console;
    this.process = process;
    this.editor = new AtomicReference<Reader>();
  }

  @Override
  public boolean takeAlternateBuffer() throws IOException {
    return console.driver.takeAlternateBuffer();
  }

  @Override
  public boolean releaseAlternateBuffer() throws IOException {
    return console.driver.releaseAlternateBuffer();
  }

  @Override
  public String getProperty(String propertyName) {
    return null;
  }

  @Override
  public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
    Reader waiter = new Reader(Thread.currentThread(), echo);
    if (editor.compareAndSet(null, waiter)) {
      if (msg != null && msg.length() > 0) {
        console.driver.write(msg);
        console.driver.flush();
      }
      console.iterate();
      try {
        return waiter.line.take();
      } finally {
        editor.set(null);
      }
    } else {
      throw new IllegalStateException("A thread is already reading the line");
    }
  }

  @Override
  public int getWidth() {
    return console.driver.getWidth();
  }

  @Override
  public int getHeight() {
    return console.driver.getHeight();
  }

  @Override
  public void write(Chunk chunk) throws IOException {
    if (chunk instanceof Text) {
      console.driver.write(((Text)chunk).getText());
    } else if (chunk instanceof Style) {
      console.driver.write(((Style)chunk));
    } else if (chunk instanceof CLS) {
      console.driver.cls();
    }
  }

  @Override
  public void flush() throws IOException {
    console.driver.flush();
  }


  @Override
  public void end(ShellResponse response) {

    // Interrupt reader
    Reader reader = editor.get();
    if (reader != null) {
      reader.thread.interrupt();
    }

    //
    if (response instanceof ShellResponse.Close) {

    } else {

    }

    // Write message
    try {
      String msg = response.getMessage();
      if (msg.length() > 0) {
        console.driver.write(msg);
      }
      console.driver.writeCRLF();
      console.driver.flush();
    }
    catch (IOException e) {
      // Log it
    }

    //
    if (response instanceof ShellResponse.Close) {
      console.close();
    } else {
      // Put back editor and redraw prompt
      console.edit();
      console.iterate();
    }
  }
}
