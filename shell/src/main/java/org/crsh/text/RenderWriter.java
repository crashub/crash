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

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

public class RenderWriter extends Writer implements Screenable {

  /** . */
  final ScreenContext out;

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

  public RenderWriter append(CharSequence s) throws IOException {
    empty &= s.length() == 0;
    out.append(s);
    return this;
  }

  public Screenable append(Style style) throws IOException {
    out.append(style);
    return this;
  }

  public Screenable cls() throws IOException {
    out.cls();
    return this;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    if (len > 0) {
      out.append(new String(cbuf, off, len));
    }
  }

  @Override
  public void flush() throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    try {
      out.flush();
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      // e.printStackTrace();
      // just swallow ?
    }
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
