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

package org.crsh.text;

import org.crsh.shell.ScreenContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

public class RenderWriter extends Writer implements ScreenContext {

  /** . */
  private final ScreenContext out;

  /** . */
  private final Closeable closeable;

  /** . */
  private boolean closed;

  /** . */
  private boolean empty;

  public RenderWriter(ScreenContext out) throws NullPointerException {
    this(out, null);
  }

  public RenderWriter(ScreenContext out, Closeable closeable) throws NullPointerException {
    if (out == null) {
      throw new NullPointerException("No null appendable expected");
    }

    //
    this.out = out;
    this.empty = true;
    this.closeable = closeable;
  }

  public boolean isEmpty() {
    return empty;
  }

  public int getWidth() {
    return out.getWidth();
  }

  public int getHeight() {
    return out.getHeight();
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void write(Chunk chunk) throws IOException {
    provide(chunk);
  }

  public void provide(Chunk element) throws IOException {
    if (element instanceof Text) {
      Text text = (Text)element;
      empty &= text.getText().length() == 0;
    }
    out.write(element);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    if (len > 0) {
      Text text = new Text();
      text.buffer.append(cbuf, off, len);
      provide(text);
    }
  }

  @Override
  public void flush() throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    out.flush();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (closeable != null) {
        closeable.close();
      }
    }
  }
}
