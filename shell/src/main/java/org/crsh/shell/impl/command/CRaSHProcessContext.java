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

import org.crsh.command.CommandContext;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.text.Screenable;
import org.crsh.text.ScreenContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.text.ScreenBuffer;
import org.crsh.text.ScreenContextConsumer;
import org.crsh.text.Style;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

class CRaSHProcessContext implements CommandContext<Object>, Closeable {

  /** . */
  private final CRaSHSession session;

  /** . */
  private final ShellProcessContext processContext;

  /** . */
  private final ScreenContextConsumer adapter;

  /** . */
  private final ScreenBuffer buffer;

  /** . */
  private boolean useAlternateBuffer;

  CRaSHProcessContext(CRaSHSession session, final ShellProcessContext processContext) {

    // We use this chunk buffer to buffer stuff
    // but also because it optimises the chunks
    // which provides better perormances on the client
    final ScreenBuffer buffer = new ScreenBuffer(processContext);

    //
    final ScreenContextConsumer adapter = new ScreenContextConsumer(new ScreenContext() {
      public int getWidth() {
        return processContext.getWidth();
      }

      public int getHeight() {
        return processContext.getHeight();
      }

      @Override
      public Screenable append(CharSequence s) throws IOException {
        buffer.append(s);
        return this;
      }

      @Override
      public Appendable append(char c) throws IOException {
        buffer.append(c);
        return this;
      }

      @Override
      public Screenable append(CharSequence csq, int start, int end) throws IOException {
        buffer.append(csq, start, end);
        return this;
      }

      @Override
      public Screenable append(Style style) throws IOException {
        buffer.append(style);
        return this;
      }

      @Override
      public Screenable cls() throws IOException {
        buffer.cls();
        return this;
      }

      public void flush() throws IOException {
        buffer.flush();
      }
    });

    //
    this.session = session;
    this.processContext = processContext;
    this.adapter = adapter;
    this.useAlternateBuffer = false;
    this.buffer = buffer;
  }

  public boolean takeAlternateBuffer() throws IOException {
    return useAlternateBuffer = processContext.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return useAlternateBuffer = processContext.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return processContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
    return processContext.readLine(msg, echo);
  }

  public int getWidth() {
    return processContext.getWidth();
  }

  public int getHeight() {
    return processContext.getHeight();
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  @Override
  public Screenable append(CharSequence s) throws IOException {
    return append(s, 0, s.length());
  }

  @Override
  public Screenable append(char c) throws IOException {
    adapter.send();
    buffer.append(c);
    return this;
  }

  @Override
  public Screenable append(CharSequence csq, int start, int end) throws IOException {
    if (start < end) {
      adapter.send();
      buffer.append(csq, start, end);
    }
    return this;
  }

  @Override
  public Screenable append(Style style) throws IOException {
    adapter.provide(style);
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    buffer.cls();
    return this;
  }

  public void provide(Object element) throws IOException {
    adapter.provide(element);
  }

  public void flush() throws IOException {
    adapter.flush();
  }

  public Map<String, Object> getSession() {
    return session;
  }

  public Map<String, Object> getAttributes() {
    return session.crash.getContext().getAttributes();
  }

  public void close() throws IOException {
    if (useAlternateBuffer) {
      releaseAlternateBuffer();
    }
  }
}
